package com.sneakylink.service;

import com.sneakylink.dto.AdminStatsResponse;
import com.sneakylink.dto.CheckoutRequest;
import com.sneakylink.dto.FulfillmentOptionsResponse;
import com.sneakylink.exception.InsufficientStockException;
import com.sneakylink.model.*;
import com.sneakylink.repository.CartItemRepository;
import com.sneakylink.repository.OrderRepository;
import com.sneakylink.repository.ShoeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ShoeRepository shoeRepository;
    private final FulfillmentService fulfillmentService;

    public OrderService(OrderRepository orderRepository,
                        CartItemRepository cartItemRepository,
                        ShoeRepository shoeRepository,
                        FulfillmentService fulfillmentService) {
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.shoeRepository = shoeRepository;
        this.fulfillmentService = fulfillmentService;
    }

    @Transactional
    public CustomerOrder checkout(String customerKey, CheckoutRequest request) {
        List<CartItem> cartItems = cartItemRepository.findByCustomerKeyOrderByAddedAtAsc(customerKey);
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Your cart is empty. Add footwear before checking out.");
        }

        if (request.fulfillmentType() != FulfillmentType.IN_STORE_PICKUP) {
            if (isBlank(request.shippingAddress()) || isBlank(request.shippingCity()) || isBlank(request.shippingPostalCode())) {
                throw new IllegalArgumentException("Shipping address, city, and postal code are required for delivery.");
            }
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        CustomerOrder order = new CustomerOrder();
        order.setOrderNumber(generateOrderNumber());
        order.setCustomerKey(customerKey);
        order.setCustomerName(request.customerName().trim());
        order.setCustomerEmail(request.customerEmail().trim());
        order.setCustomerPhone(request.customerPhone().trim());
        order.setFulfillmentType(request.fulfillmentType());
        order.setPaymentMethod(request.paymentMethod().trim());
        order.setPaymentLast4(extractLast4(request.cardNumber()));

        for (CartItem ci : cartItems) {
            Shoe shoe = shoeRepository.findById(ci.getShoe().getId())
                    .orElseThrow(() -> new InsufficientStockException("Shoe model no longer available."));
            if (shoe.getStockQuantity() < ci.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for " + shoe.getName() + " (available: " + shoe.getStockQuantity() + ").");
            }
            shoe.setStockQuantity(shoe.getStockQuantity() - ci.getQuantity());
            shoeRepository.save(shoe);

            subtotal = subtotal.add(shoe.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
            order.addItem(new OrderItem(
                    shoe.getId(),
                    shoe.getName(),
                    shoe.getColorway(),
                    ci.getSelectedSize(),
                    ci.getQuantity(),
                    shoe.getPrice()
            ));
        }

        FulfillmentOptionsResponse options = fulfillmentService.calculateOptions(subtotal);
        BigDecimal shippingFee;

        if (request.fulfillmentType() == FulfillmentType.IN_STORE_PICKUP) {
            shippingFee = BigDecimal.ZERO;
            String chosenWindow = !isBlank(request.selectedPickupWindow())
                    ? request.selectedPickupWindow().trim()
                    : options.pickupWindows().get(0);
            order.setPickupWindowLabel(chosenWindow);
            order.setShippingAddress(options.storeName() + " - " + options.storeAddress());
            order.setStatus(OrderStatus.READY_FOR_PICKUP);
        } else if (request.fulfillmentType() == FulfillmentType.EXPRESS_DELIVERY) {
            shippingFee = options.expressShippingFee();
            order.setEstimatedDeliveryDate(options.expressDeliveryDate());
            order.setDeliveryWindowLabel(options.expressDeliveryLabel());
            order.setShippingAddress(request.shippingAddress().trim());
            order.setShippingCity(request.shippingCity().trim());
            order.setShippingPostalCode(request.shippingPostalCode().trim());
            order.setStatus(OrderStatus.CONFIRMED);
        } else {
            shippingFee = options.standardShippingFee();
            order.setEstimatedDeliveryDate(options.standardDeliveryEnd());
            order.setDeliveryWindowLabel(options.standardDeliveryLabel());
            order.setShippingAddress(request.shippingAddress().trim());
            order.setShippingCity(request.shippingCity().trim());
            order.setShippingPostalCode(request.shippingPostalCode().trim());
            order.setStatus(OrderStatus.CONFIRMED);
        }

        order.setSubtotal(subtotal);
        order.setShippingFee(shippingFee);
        order.setTotalAmount(subtotal.add(shippingFee));

        CustomerOrder saved = orderRepository.save(order);
        cartItemRepository.deleteByCustomerKey(customerKey);
        return saved;
    }

    public List<CustomerOrder> getOrdersForCustomer(String customerKey) {
        return orderRepository.findByCustomerKeyOrderByCreatedAtDesc(customerKey);
    }

    public List<CustomerOrder> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    public AdminStatsResponse getAdminStats() {
        List<Shoe> allShoes = shoeRepository.findAll();
        long totalUnits = allShoes.stream().mapToLong(Shoe::getStockQuantity).sum();
        long lowStock = shoeRepository.countByStockQuantityLessThanEqual(5);
        long outOfStock = shoeRepository.countByStockQuantityEquals(0);

        List<CustomerOrder> orders = orderRepository.findAll();
        BigDecimal revenue = orders.stream()
                .map(CustomerOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new AdminStatsResponse(
                allShoes.size(),
                totalUnits,
                lowStock,
                outOfStock,
                orders.size(),
                revenue
        );
    }

    private String generateOrderNumber() {
        String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String randPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "SL-" + datePart + "-" + randPart;
    }

    private String extractLast4(String cardNumber) {
        if (cardNumber == null) return "N/A";
        String digits = cardNumber.replaceAll("\\D", "");
        if (digits.length() < 4) return "N/A";
        return digits.substring(digits.length() - 4);
    }

    private boolean isBlank(String val) {
        return val == null || val.isBlank();
    }
}

package com.sneakylink.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private CustomerOrder order;

    @Column(nullable = false)
    private Long shoeId;

    @Column(nullable = false, length = 140)
    private String shoeName;

    @Column(nullable = false, length = 80)
    private String colorway;

    @Column(nullable = false, length = 16)
    private String selectedSize;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    public OrderItem() {
    }

    public OrderItem(Long shoeId, String shoeName, String colorway, String selectedSize, Integer quantity, BigDecimal unitPrice) {
        this.shoeId = shoeId;
        this.shoeName = shoeName;
        this.colorway = colorway;
        this.selectedSize = selectedSize;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public Long getId() { return id; }
    public CustomerOrder getOrder() { return order; }
    public void setOrder(CustomerOrder order) { this.order = order; }

    public Long getShoeId() { return shoeId; }
    public String getShoeName() { return shoeName; }
    public String getColorway() { return colorway; }
    public String getSelectedSize() { return selectedSize; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
}

package com.sneakylink.service;

import com.sneakylink.dto.FulfillmentOptionsResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class FulfillmentService {

    public static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("250.00");
    public static final BigDecimal STANDARD_SHIPPING_FEE = new BigDecimal("12.00");
    public static final BigDecimal EXPRESS_SHIPPING_FEE = new BigDecimal("25.00");

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy");

    private final String storeName;
    private final String storeAddress;

    public FulfillmentService(
            @Value("${sneakylink.store.name:SneakyLink Flagship Showroom}") String storeName,
            @Value("${sneakylink.store.address:420 Cobalt Ave, Suite 100}") String storeAddress
    ) {
        this.storeName = storeName;
        this.storeAddress = storeAddress;
    }

    public FulfillmentOptionsResponse calculateOptions(BigDecimal subtotal) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        LocalDate stdStart = addBusinessDays(today, 3);
        LocalDate stdEnd = addBusinessDays(today, 5);
        LocalDate expressDate = addBusinessDays(today, now.getHour() < 15 ? 1 : 2);

        BigDecimal standardFee = (subtotal != null && subtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0)
                ? BigDecimal.ZERO
                : STANDARD_SHIPPING_FEE;

        String stdLabel = stdStart.format(DATE_FMT) + " - " + stdEnd.format(DATE_FMT);
        String expLabel = "Guaranteed by " + expressDate.format(DATE_FMT);

        List<String> pickupWindows = buildPickupWindows(now);

        return new FulfillmentOptionsResponse(
                stdStart,
                stdEnd,
                stdLabel,
                standardFee,
                expressDate,
                expLabel,
                EXPRESS_SHIPPING_FEE,
                storeName,
                storeAddress,
                pickupWindows
        );
    }

    public LocalDate addBusinessDays(LocalDate start, int businessDays) {
        LocalDate date = start;
        int added = 0;
        while (added < businessDays) {
            date = date.plusDays(1);
            if (date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY) {
                added++;
            }
        }
        return date;
    }

    private List<String> buildPickupWindows(LocalDateTime now) {
        List<String> windows = new ArrayList<>();
        LocalDate today = now.toLocalDate();
        int currentHour = now.getHour();

        String[][] slots = {
                {"10", "10:00 AM - 12:00 PM"},
                {"12", "12:00 PM - 2:00 PM"},
                {"14", "2:00 PM - 4:00 PM"},
                {"16", "4:00 PM - 6:00 PM"}
        };

        // Offer remaining same-day windows with at least 2 hours of prep time
        for (String[] slot : slots) {
            int slotStart = Integer.parseInt(slot[0]);
            if (currentHour + 2 <= slotStart) {
                windows.add("Today (" + today.format(DATE_FMT) + ") | " + slot[1]);
            }
        }

        // Always offer next two days' pickup windows
        for (int offset = 1; offset <= 2; offset++) {
            LocalDate nextDay = today.plusDays(offset);
            String prefix = (offset == 1 ? "Tomorrow" : nextDay.getDayOfWeek().name().substring(0, 1) + nextDay.getDayOfWeek().name().substring(1).toLowerCase())
                    + " (" + nextDay.format(DATE_FMT) + ") | ";
            for (String[] slot : slots) {
                if (windows.size() < 8) {
                    windows.add(prefix + slot[1]);
                }
            }
        }
        return windows;
    }
}

package com.web.appts.utils;

import com.web.appts.DTO.OrderDto;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

public final class OrderSortingUtils {
    private static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";

    private OrderSortingUtils() {
    }

    public static void sortByDeliveryOrCreationDateDesc(List<OrderDto> orders) {
        sortByDeliveryOrCreationDateDesc(orders, DEFAULT_DATE_PATTERN);
    }

    public static void sortByDeliveryOrCreationDateDesc(List<OrderDto> orders, String... datePatterns) {
        sortByDateDesc(
                orders,
                OrderDto::getDeliveryDate,
                OrderDto::getCreationDate,
                datePatterns
        );
    }

    public static <T> void sortByDateDesc(
            List<T> records,
            Function<T, String> primaryDateAccessor,
            Function<T, String> fallbackDateAccessor,
            String... datePatterns
    ) {
        if (records == null || records.isEmpty()) {
            return;
        }

        records.sort(Comparator.comparing((T record) -> {
            if (record == null) {
                return null;
            }
            String primary = primaryDateAccessor.apply(record);
            return (primary != null && !primary.isEmpty()) ? primary : fallbackDateAccessor.apply(record);
        }, (date1, date2) -> {
            Date parsedDate1 = parseWithFallback(date1, datePatterns);
            Date parsedDate2 = parseWithFallback(date2, datePatterns);
            return Comparator.nullsLast(Date::compareTo).compare(parsedDate2, parsedDate1);
        }));
    }

    private static Date parseWithFallback(String dateValue, String... datePatterns) {
        if (dateValue == null) {
            return null;
        }

        for (String pattern : datePatterns) {
            try {
                return new SimpleDateFormat(pattern).parse(dateValue);
            } catch (Exception ignored) {
            }
        }

        return null;
    }
}

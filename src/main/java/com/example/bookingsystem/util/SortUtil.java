package com.example.bookingsystem.util;

import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses sort parameters of the form {@code sort=field,dir&sort=field2,dir2}
 * (also accepts a single {@code sort=field,dir} value).
 */
public final class SortUtil {

    private SortUtil() {}

    public static Sort parseSort(String[] sort) {
        if (sort == null || sort.length == 0) {
            return Sort.unsorted();
        }
        List<Sort.Order> orders = new ArrayList<>();
        for (String entry : sort) {
            if (entry == null || entry.isBlank()) {
                continue;
            }
            String[] parts = entry.split(",");
            String property = parts[0].trim();
            if (property.isEmpty()) {
                continue;
            }
            Sort.Direction direction = parts.length > 1 && parts[1].trim().equalsIgnoreCase("desc")
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            orders.add(new Sort.Order(direction, property));
        }
        return orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
    }
}

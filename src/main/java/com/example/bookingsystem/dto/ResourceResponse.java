package com.example.bookingsystem.dto;


import java.math.BigDecimal;

public record ResourceResponse(
    Long id,
    String name,
    String description,
    boolean available,
    BigDecimal pricePerHour
) {}

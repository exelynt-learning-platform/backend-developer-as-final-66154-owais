package com.example.bookingsystem.dto;


public record ResourceResponse(
    Long id,
    String name,
    String description,
    boolean available
) {}
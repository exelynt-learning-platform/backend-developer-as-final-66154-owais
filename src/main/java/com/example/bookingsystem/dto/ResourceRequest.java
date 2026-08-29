package com.example.bookingsystem.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResourceRequest(
    @NotBlank @Size(max = 100) String name,
    @Size(max = 500) String description,
    boolean available
) {}

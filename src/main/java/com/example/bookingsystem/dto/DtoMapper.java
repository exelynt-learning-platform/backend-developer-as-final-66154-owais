package com.example.bookingsystem.dto;


import com.example.bookingsystem.entity.Reservation;
import com.example.bookingsystem.entity.Resource;

public class DtoMapper {

    public static ResourceResponse toResourceResponse(Resource resource) {
        return new ResourceResponse(
                resource.getId(),
                resource.getName(),
                resource.getDescription(),
                resource.isAvailable(),
                resource.getPricePerHour()
        );
    }

    public static ReservationResponse toReservationResponse(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getUser().getId(),
                reservation.getResource().getId(),
                reservation.getResource().getName(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getPrice(),
                reservation.getStatus()
        );
    }
}

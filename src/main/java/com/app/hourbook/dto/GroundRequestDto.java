package com.app.hourbook.dto;

import java.time.LocalTime;
import java.util.List;

import com.app.hourbook.enums.Amenities;
import com.app.hourbook.enums.GroundType;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GroundRequestDto {

    @NotBlank(message = "Ground name is required")
    private String name;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Price per hour is required")
    @DecimalMin(value = "1.0", message = "Price must be greater than zero")
    private Double pricePerHour;

    @NotBlank(message = "Sports is required")
    private String sports;

    @NotNull(message = "Ground type is required")
    private GroundType type;

    @NotEmpty(message = "Select at least one amenity")
    private List<Amenities> amenities;

    @NotEmpty(message = "Upload at least one image")
    private List<String> imageUrls;

    @NotNull(message = "Vendor Id is required")
    private Integer vendorId;

    /*
     * Ground operating hours.
     * Required so that user-side slot generation
     * (UserBookingServiceImpl.getAvailableSlots) always
     * has valid opening/closing times to work with.
     */

    @NotNull(message = "Opening time is required")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime openingTime;

    @NotNull(message = "Closing time is required")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime closingTime;

    /*
     * Version 1: HourBook currently supports fixed-duration slots.
     * Defaults to 60 if not supplied.
     */
    private Integer slotDurationMinutes = 60;

}
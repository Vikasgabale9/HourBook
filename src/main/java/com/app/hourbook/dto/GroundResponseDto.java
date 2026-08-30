package com.app.hourbook.dto;

import java.time.LocalTime;
import java.util.List;

import com.app.hourbook.enums.Amenities;
import com.app.hourbook.enums.GroundStatus;
import com.app.hourbook.enums.GroundType;

import lombok.Data;

@Data
public class GroundResponseDto {

    private Integer id;

    private String name;

    private String location;

    private Double pricePerHour;

    private String sports;

    private GroundType type;

    private List<Amenities> amenities;

    private List<String> imageUrls;

    private Boolean isActive;

    private GroundStatus status;

    private Integer vendorId;

    private String vendorPhoneNumber;

    private LocalTime openingTime;

    private LocalTime closingTime;

    private Integer slotDurationMinutes;

}
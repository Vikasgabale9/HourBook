package com.app.hourbook.dto.booking;

import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingSlotDto {

    private LocalTime startTime;

    private LocalTime endTime;

    private boolean available;
}
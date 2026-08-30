package com.app.hourbook.dto;

public record VerifyOtpResponse(
        Integer userId,
        String phoneNumber,String token
) {
}
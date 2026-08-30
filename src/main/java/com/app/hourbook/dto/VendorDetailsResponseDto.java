package com.app.hourbook.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorDetailsResponseDto {

    private Integer id;
    
    private Integer AuthId;

    private String fullName;

    private String email;

    private String phoneNumber;

    
}
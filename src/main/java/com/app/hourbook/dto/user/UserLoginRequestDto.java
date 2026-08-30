package com.app.hourbook.dto.user;

import lombok.Data;

@Data
public class UserLoginRequestDto {

    private String loginId;

    private String password;

}
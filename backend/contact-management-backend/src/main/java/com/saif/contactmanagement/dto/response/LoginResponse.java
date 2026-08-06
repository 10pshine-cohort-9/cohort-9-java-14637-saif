package com.saif.contactmanagement.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String accessToken;

    private String tokenType;

    private Long expiresIn;

    private UserResponse user;

}

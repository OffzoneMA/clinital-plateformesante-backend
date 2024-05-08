package com.clinitalPlatform.models;

import lombok.Data;

@Data
public class JwtTokens {
    private final String accessToken;
    private final String refreshToken;

    public JwtTokens(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}

package com.gdschongik.gdsc.domain.auth.dto;

public record TokenPairDto(String accessToken, String refreshToken) {

    public static TokenPairDto of(String accessToken, String refreshToken) {
        return new TokenPairDto(accessToken, refreshToken);
    }
}

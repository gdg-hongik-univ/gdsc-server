package com.gdschongik.gdsc.global.common.constant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
public enum JwtConstant {
    ACCESS_TOKEN(Constants.ACCESS_TOKEN_COOKIE_NAME),
    REFRESH_TOKEN(Constants.REFRESH_TOKEN_COOKIE_NAME);

    private final String cookieName;

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Constants {
        public static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
        public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    }
}

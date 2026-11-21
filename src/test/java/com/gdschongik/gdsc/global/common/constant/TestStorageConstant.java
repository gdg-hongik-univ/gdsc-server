package com.gdschongik.gdsc.global.common.constant;

import java.time.Duration;

public class TestStorageConstant {

    public static final String R2_ENDPOINT = "https://r2.test.invalid";
    public static final String R2_BUCKET = "test-bucket";
    public static final String R2_ACCESS_KEY_ID = "test-access-key";
    public static final String R2_SECRET_ACCESS_KEY = "test-secret-key";
    public static final Duration UPLOAD_URL_TTL = Duration.ofMinutes(15);
    public static final Duration DOWNLOAD_URL_TTL = Duration.ofMinutes(30);
    public static final String UPLOAD_PATH = "member";
    public static final String CONTENT_TYPE = "image/png";

    private TestStorageConstant() {}
}

package com.gdschongik.gdsc.global.common.constant;

import java.time.Duration;

public class StorageConstant {

    public static final String R2_REGION = "auto";
    public static final String R2_PROPERTY_PREFIX = "storage.r2.";
    public static final Duration PRESIGNED_URL_MIN_TTL = Duration.ofSeconds(1);
    public static final Duration PRESIGNED_URL_MAX_TTL = Duration.ofDays(7);
    public static final String OBJECT_KEY_DELIMITER = "/";

    private StorageConstant() {}
}

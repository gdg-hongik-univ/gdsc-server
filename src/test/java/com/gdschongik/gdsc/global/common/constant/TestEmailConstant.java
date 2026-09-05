package com.gdschongik.gdsc.global.common.constant;

public class TestEmailConstant {

    public static final String CODE = "042917";
    public static final String WRONG_CODE = "9999999"; // 7자리 존재할 수 없는 코드
    public static final long TTL = 60L;
    public static final int MAX_ATTEMPT_COUNT = 5;
    public static final long RESEND_WAIT_TIME_SECONDS = 60L;

    private TestEmailConstant() {}
}

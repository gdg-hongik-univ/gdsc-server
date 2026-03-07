package com.gdschongik.gdsc.global.common.constant;

public class RegexConstant {

    public static final String STUDENT_ID = "^[A-C]{1}[0-9]{6}$";
    public static final String PHONE = "^010-[0-9]{4}-[0-9]{4}$";
    public static final String PHONE_WITHOUT_HYPHEN = "^010[0-9]{8}$";
    public static final String NICKNAME = "[ㄱ-ㅣ가-힣]{1,6}$";
    public static final String DEPARTMENT = "^D[0-9]{3}$";
    public static final String HONGIK_EMAIL = "^[^\\W&=+'+,<>]+(\\.[^\\W&=+'+,<>]+)*@g\\.hongik\\.ac\\.kr$";
    public static final String DATETIME = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String ZONED_DATETIME = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    public static final String DATE = "yyyy-MM-dd";
    public static final String ACADEMIC_YEAR = "^[0-9]{4}$";
    public static final String GITHUB_REPOSITORY =
            "^https:\\/\\/github\\.com\\/[A-Za-z0-9]+(-[A-Za-z0-9]+)*\\/[A-Za-z0-9._-]+$";

    public static final String ATTENDANCE_NUMBER = "^[0-9]{4}$";

    private RegexConstant() {}
}

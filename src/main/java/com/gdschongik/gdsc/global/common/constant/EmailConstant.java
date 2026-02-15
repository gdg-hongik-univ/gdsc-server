package com.gdschongik.gdsc.global.common.constant;

public class EmailConstant {

    public static final String VERIFY_EMAIL_API_ENDPOINT = "/onboarding/verify-email?%s=";
    public static final String VERIFY_EMAIL_REQUEST_PARAMETER_KEY = "token";
    public static final String HONGIK_UNIV_MAIL_DOMAIN = "@g.hongik.ac.kr";
    public static final String SENDER_PERSONAL = "GDGoC Hongik";
    public static final String SENDER_ADDRESS = "gdsc.hongik@gmail.com";
    public static final String VERIFICATION_EMAIL_SUBJECT = "GDGoC Hongik 이메일 인증 코드입니다.";
    public static final String TOKEN_EMAIL_NAME = "email";

    private EmailConstant() {}
}

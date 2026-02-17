package com.gdschongik.gdsc.global.common.constant;

public class EmailConstant {

    public static final String VERIFY_UNIV_EMAIL_API_ENDPOINT = "/onboarding/verify-univ-email?%s=";
    public static final String VERIFY_EMAIL_API_ENDPOINT = "/onboarding/verify-email?%s=";
    public static final String VERIFY_EMAIL_REQUEST_PARAMETER_KEY = "token";
    public static final String HONGIK_UNIV_MAIL_DOMAIN = "@g.hongik.ac.kr";
    public static final String SENDER_PERSONAL = "GDG Hongik Univ. Core Team";
    public static final String SENDER_ADDRESS = "gdsc.hongik@gmail.com";
    public static final String VERIFICATION_UNIV_EMAIL_SUBJECT = "GDG Hongik Univ. 재학생 인증 메일입니다.";
    public static final String VERIFICATION_EMAIL_SUBJECT = "GDG Hongik Univ. 본인 인증 메일입니다.";
    public static final String TOKEN_EMAIL_NAME = "email";

    private EmailConstant() {}
}

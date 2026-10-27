package com.gdschongik.gdsc.global.common.constant;

import java.time.Duration;

public class EmailConstant {

    public static final String HONGIK_UNIV_MAIL_DOMAIN = "@g.hongik.ac.kr";
    public static final String SENDER_PERSONAL = "GDG Hongik Univ. Core Team";
    public static final String SENDER_ADDRESS = "gdsc.hongik@gmail.com";
    public static final String VERIFICATION_UNIV_EMAIL_SUBJECT = "GDG Hongik Univ. 재학생 인증 메일입니다.";
    public static final String VERIFICATION_EMAIL_SUBJECT = "GDG Hongik Univ. 본인 인증 메일입니다.";
    public static final Duration VERIFICATION_CODE_TTL = Duration.ofMinutes(5);
    public static final Duration VERIFICATION_CODE_RESEND_WAIT_TIME = Duration.ofSeconds(60);

    /**
     * 본인 인증 메일 템플릿입니다. 코드 유효 시간(분)과 인증 코드 순으로 포맷팅합니다.
     */
    public static final String EMAIL_VERIFICATION_CODE_MAIL_CONTENT =
            """
<div style='font-family: "Roboto", sans-serif; margin: 40px; background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);'>
    <h3 style='color: #202124;'>GDG Hongik Univ. 본인 인증 메일</h3>
    <p style='color: #5f6368;'>안녕하세요!</p>
    <p style='color: #5f6368;'>GDG Hongik Univ.의 오픈 커뮤니티에 가입해주셔서 대단히 감사드립니다.</p>
    <p style='color: #5f6368;'>아래의 인증 코드를 입력하여 본인 인증을 완료해주세요. 코드는 %d분 동안 유효합니다.</p>
    <div style='display: inline-block; background-color: #F8F9FA; color: #202124; border-left: 4px solid #4285F4; padding: 12px 28px; margin: 20px 0; border-radius: 0 4px 4px 0; font-weight: 500; font-size: 28px; letter-spacing: 8px; font-family: monospace;'>%s</div>
    <p style='color: #5f6368;'>감사합니다.<br>GDG Hongik Univ. Core Team</p>
</div>
""";

    /**
     * 재학생 인증 메일 템플릿입니다. 코드 유효 시간(분)과 인증 코드 순으로 포맷팅합니다.
     */
    public static final String UNIV_EMAIL_VERIFICATION_CODE_MAIL_CONTENT =
            """
<div style='font-family: "Roboto", sans-serif; margin: 40px; background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);'>
    <h3 style='color: #202124;'>GDG Hongik Univ. 재학생 인증 메일</h3>
    <p style='color: #5f6368;'>안녕하세요!</p>
    <p style='color: #5f6368;'>GDG Hongik Univ.의 오픈 커뮤니티에 가입해주셔서 대단히 감사드립니다.</p>
    <p style='color: #5f6368;'>아래의 인증 코드를 입력하여 재학생 인증을 완료해주세요. 코드는 %d분 동안 유효합니다.</p>
    <div style='display: inline-block; background-color: #F8F9FA; color: #202124; border-left: 4px solid #4285F4; padding: 12px 28px; margin: 20px 0; border-radius: 0 4px 4px 0; font-weight: 500; font-size: 28px; letter-spacing: 8px; font-family: monospace;'>%s</div>
    <p style='color: #5f6368;'>감사합니다.<br>GDG Hongik Univ. Core Team</p>
</div>
""";

    private EmailConstant() {}
}

package com.gdschongik.gdsc.domain.email.application;

import com.gdschongik.gdsc.domain.email.dao.EmailVerificationRepository;
import com.gdschongik.gdsc.domain.email.domain.service.UnivEmailValidator;
import com.gdschongik.gdsc.domain.member.dao.MemberRepository;
import com.gdschongik.gdsc.global.property.JwtProperty;
import com.gdschongik.gdsc.global.util.MemberUtil;
import com.gdschongik.gdsc.global.util.email.EmailVerificationTokenUtil;
import com.gdschongik.gdsc.global.util.email.MailSender;
import com.gdschongik.gdsc.global.util.email.VerificationLinkUtil;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailVerificationLinkSendService {

    private final MemberRepository memberRepository;
    private final EmailVerificationRepository emailVerificationRepository;

    private final MailSender mailSender;
    private final UnivEmailValidator univEmailValidator;
    private final EmailVerificationTokenUtil emailVerificationTokenUtil;
    private final VerificationLinkUtil verificationLinkUtil;
    private final MemberUtil memberUtil;
    private final JwtProperty jwtProperty;

    public static final Duration VERIFICATION_TOKEN_TIME_TO_LIVE = Duration.ofMinutes(30);

    private static final String NOTIFICATION_MESSAGE =
            """
<div style='font-family: "Roboto", sans-serif; margin: 40px; background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);'>
    <h3 style='color: #202124;'>GDGoC Hongik 재학생 인증 메일</h3>
    <p style='color: #5f6368;'>안녕하세요!</p>
    <p style='color: #5f6368;'>GDGoC Hongik 커뮤니티에 지원해주셔서 대단히 감사드립니다.</p>
    <p style='color: #5f6368;'>아래의 버튼을 클릭하여 재학생 인증을 완료해주세요. 링크는 %d분 동안 유효합니다.</p>
    <a href='%s' style='display: inline-block; background-color: #4285F4; color: white; padding: 12px 24px; margin: 20px 0; border-radius: 4px; text-decoration: none; font-weight: 500;'>재학생 인증하기</a>
    <p style='color: #5f6368;'>감사합니다.<br>GDGoC Hongik Team</p>
</div>
""";

    public void send(Long previousMemberId) {
        // TODO: 로직 구현
    }

    private String writeMailContentWithVerificationLink(String verificationLink) {
        return NOTIFICATION_MESSAGE.formatted(VERIFICATION_TOKEN_TIME_TO_LIVE.toMinutes(), verificationLink);
    }
}

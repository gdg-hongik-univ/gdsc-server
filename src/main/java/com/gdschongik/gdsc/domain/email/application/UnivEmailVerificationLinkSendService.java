package com.gdschongik.gdsc.domain.email.application;

import static com.gdschongik.gdsc.global.common.constant.EmailConstant.*;
import static com.gdschongik.gdsc.global.common.constant.EmailConstant.VERIFICATION_EMAIL_SUBJECT;

import com.gdschongik.gdsc.domain.email.dao.UnivEmailVerificationRepository;
import com.gdschongik.gdsc.domain.email.domain.UnivEmailVerification;
import com.gdschongik.gdsc.domain.email.domain.service.UnivEmailValidator;
import com.gdschongik.gdsc.domain.member.dao.MemberRepository;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.global.common.constant.JwtConstant;
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
public class UnivEmailVerificationLinkSendService {

    private final MemberRepository memberRepository;
    private final UnivEmailVerificationRepository univEmailVerificationRepository;

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

    public void send(String univEmail) {
        boolean isUnivEmailDuplicate = memberRepository.existsByUnivEmail(univEmail);

        univEmailValidator.validateSendUnivEmailVerificationLink(univEmail, isUnivEmailDuplicate);

        String verificationToken = generateVerificationToken(univEmail);
        String verificationLink = verificationLinkUtil.createLink(VERIFY_UNIV_EMAIL_API_ENDPOINT, verificationToken);
        String mailContent = writeMailContentWithVerificationLink(verificationLink);

        mailSender.send(univEmail, VERIFICATION_EMAIL_SUBJECT, mailContent);

        log.info("[UnivEmailVerificationLinkSendService] 학생 인증 메일 발송: univEmail={}", univEmail);
    }

    private String generateVerificationToken(String univEmail) {
        final Member currentMember = memberUtil.getCurrentMember();
        String verificationToken =
                emailVerificationTokenUtil.generateEmailVerificationToken(currentMember.getId(), univEmail);

        JwtProperty.TokenProperty emailVerificationTokenProperty =
                jwtProperty.getToken().get(JwtConstant.EMAIL_VERIFICATION_TOKEN);

        UnivEmailVerification univEmailVerification = UnivEmailVerification.of(
                currentMember.getId(), verificationToken, emailVerificationTokenProperty.expirationTime());
        univEmailVerificationRepository.save(univEmailVerification);

        return verificationToken;
    }

    private String writeMailContentWithVerificationLink(String verificationLink) {
        return NOTIFICATION_MESSAGE.formatted(VERIFICATION_TOKEN_TIME_TO_LIVE.toMinutes(), verificationLink);
    }
}

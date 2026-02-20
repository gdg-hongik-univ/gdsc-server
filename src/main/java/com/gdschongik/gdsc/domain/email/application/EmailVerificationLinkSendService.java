package com.gdschongik.gdsc.domain.email.application;

import static com.gdschongik.gdsc.global.common.constant.EmailConstant.*;

import com.gdschongik.gdsc.domain.email.dao.EmailVerificationRepository;
import com.gdschongik.gdsc.domain.email.domain.EmailVerification;
import com.gdschongik.gdsc.domain.member.dao.MemberRepository;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.global.common.constant.JwtConstant;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.global.exception.ErrorCode;
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
    private final EmailVerificationTokenUtil emailVerificationTokenUtil;
    private final VerificationLinkUtil verificationLinkUtil;
    private final MemberUtil memberUtil;
    private final JwtProperty jwtProperty;

    public static final Duration VERIFICATION_TOKEN_TIME_TO_LIVE = Duration.ofMinutes(30);

    private static final String NOTIFICATION_MESSAGE =
            """
<div style='font-family: "Roboto", sans-serif; margin: 40px; background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);'>
    <h3 style='color: #202124;'>GDG Hongik Univ. 본인 인증 메일</h3>
    <p style='color: #5f6368;'>안녕하세요!</p>
    <p style='color: #5f6368;'>아래의 버튼을 클릭하여 본인 인증을 완료해주세요. 링크는 %d분 동안 유효합니다.</p>
    <a href='%s' style='display: inline-block; background-color: #4285F4; color: white; padding: 12px 24px; margin: 20px 0; border-radius: 4px; text-decoration: none; font-weight: 500;'>본인 인증하기</a>
    <p style='color: #5f6368;'>감사합니다.<br>GDG Hongik Univ. Core Team</p>
</div>
""";

    public void sendPreviousMemberVerificationLink(Long previousMemberId) {
        Member currentMember = memberUtil.getCurrentMember();
        Member previousMember = memberRepository
                .findById(previousMemberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        validateMemberDuplicate(previousMember, currentMember);

        String verificationToken = generateVerificationToken(currentMember, previousMember);
        String verificationLink = verificationLinkUtil.createLink(VERIFY_EMAIL_API_ENDPOINT, verificationToken);
        String mailContent = writeMailContentWithVerificationLink(verificationLink);

        mailSender.send(previousMember.getEmail(), VERIFICATION_EMAIL_SUBJECT, mailContent);

        log.info("[EmailVerificationLinkSendService] 본인 인증 메일 발송: email={}", previousMember.getEmail());
    }

    private void validateMemberDuplicate(Member currentMember, Member previousMember) {
        if (currentMember.getId().equals(previousMember.getId())) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_SAME_MEMBER);
        }
    }

    private String generateVerificationToken(Member currentMember, Member previousMember) {
        String verificationToken = emailVerificationTokenUtil.generateEmailVerificationToken(
                currentMember.getId(), previousMember.getEmail());

        JwtProperty.TokenProperty emailVerificationTokenProperty =
                jwtProperty.getToken().get(JwtConstant.EMAIL_VERIFICATION_TOKEN);

        EmailVerification emailVerification = EmailVerification.of(
                currentMember.getId(),
                previousMember.getId(),
                verificationToken,
                emailVerificationTokenProperty.expirationTime());
        emailVerificationRepository.save(emailVerification);

        return verificationToken;
    }

    private String writeMailContentWithVerificationLink(String verificationLink) {
        return NOTIFICATION_MESSAGE.formatted(VERIFICATION_TOKEN_TIME_TO_LIVE.toMinutes(), verificationLink);
    }
}

package com.gdschongik.gdsc.domain.email.application;

import static com.gdschongik.gdsc.global.common.constant.EmailConstant.*;

import com.gdschongik.gdsc.domain.email.dao.UnivEmailVerificationRepository;
import com.gdschongik.gdsc.domain.email.dao.VerificationAttemptCounter;
import com.gdschongik.gdsc.domain.email.domain.UnivEmailVerification;
import com.gdschongik.gdsc.domain.email.domain.service.UnivEmailValidator;
import com.gdschongik.gdsc.domain.email.domain.service.VerificationCodeGenerator;
import com.gdschongik.gdsc.domain.member.dao.MemberRepository;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.global.util.MemberUtil;
import com.gdschongik.gdsc.global.util.email.MailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UnivEmailVerificationCodeSendService {

    private final MemberRepository memberRepository;
    private final UnivEmailVerificationRepository univEmailVerificationRepository;
    private final VerificationAttemptCounter verificationAttemptCounter;

    private final MailSender mailSender;
    private final UnivEmailValidator univEmailValidator;
    private final MemberUtil memberUtil;
    private final VerificationCodeGenerator verificationCodeGenerator;

    private static final String NOTIFICATION_MESSAGE =
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

    public void send(String univEmail) {
        boolean isUnivEmailDuplicate = memberRepository.existsByUnivEmail(univEmail);
        univEmailValidator.validateSendUnivEmailVerificationCode(univEmail, isUnivEmailDuplicate);

        String verificationCode = verificationCodeGenerator.generate();
        Member currentMember = memberUtil.getCurrentMember();
        UnivEmailVerification univEmailVerification = UnivEmailVerification.create(
                currentMember.getId(), univEmail, verificationCode, VERIFICATION_CODE_TTL.toSeconds());
        univEmailVerificationRepository.save(univEmailVerification);
        verificationAttemptCounter.initializeUnivEmailVerificationAttemptCount(
                currentMember.getId(), VERIFICATION_CODE_TTL.toSeconds());

        String mailContent = writeMailContentWithVerificationCode(verificationCode);
        mailSender.send(univEmail, VERIFICATION_UNIV_EMAIL_SUBJECT, mailContent);

        log.info("[UnivEmailVerificationCodeSendService] 재학생 인증 메일 발송: memberId={}", currentMember.getId());
    }

    private String writeMailContentWithVerificationCode(String verificationCode) {
        return NOTIFICATION_MESSAGE.formatted(VERIFICATION_CODE_TTL.toMinutes(), verificationCode);
    }
}

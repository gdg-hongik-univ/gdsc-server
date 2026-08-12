package com.gdschongik.gdsc.domain.email.application;

import static com.gdschongik.gdsc.global.common.constant.EmailConstant.*;

import com.gdschongik.gdsc.domain.email.dao.UnivEmailVerificationRepository;
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

    private final MailSender mailSender;
    private final UnivEmailValidator univEmailValidator;
    private final MemberUtil memberUtil;
    private final VerificationCodeGenerator verificationCodeGenerator;

    private static final long VERIFICATION_CODE_TTL_SECONDS = 60;

    private static final String NOTIFICATION_MESSAGE =
            """
<div style='font-family: "Roboto", sans-serif; margin: 40px; background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);'>
    <h3 style='color: #202124;'>GDG Hongik Univ. 재학생 인증 메일</h3>
    <p style='color: #5f6368;'>안녕하세요!</p>
    <p style='color: #5f6368;'>GDG Hongik Univ.의 오픈 커뮤니티에 가입해주셔서 대단히 감사드립니다.</p>
    <p style='color: #5f6368;'>아래의 코드를 입력하여 재학생 인증을 완료해주세요. 코드는 %d초 동안 유효합니다.</p>
    <p style='display: inline-block; background-color: #f1f3f4; color: #202124; padding: 12px 24px; margin: 20px 0; border-radius: 4px; font-size: 32px; font-weight: 700; letter-spacing: 8px;'>%s</p>
    <p style='color: #5f6368;'>감사합니다.<br>GDG Hongik Univ. Core Team</p>
</div>
""";

    public void send(String univEmail) {
        boolean isUnivEmailDuplicate = memberRepository.existsByUnivEmail(univEmail);
        univEmailValidator.validateSendUnivEmailVerificationCode(univEmail, isUnivEmailDuplicate);

        String verificationCode = verificationCodeGenerator.generate();
        Member currentMember = memberUtil.getCurrentMember();
        UnivEmailVerification univEmailVerification = UnivEmailVerification.create(
                currentMember.getId(), univEmail, verificationCode, VERIFICATION_CODE_TTL_SECONDS);
        univEmailVerificationRepository.save(univEmailVerification);

        String mailContent = writeMailContentWithVerificationCode(verificationCode);
        mailSender.send(univEmail, VERIFICATION_UNIV_EMAIL_SUBJECT, mailContent);

        log.info("[UnivEmailVerificationCodeSendService] 학생 인증 메일 발송: univEmail={}", univEmail);
    }

    private String writeMailContentWithVerificationCode(String verificationCode) {
        return NOTIFICATION_MESSAGE.formatted(VERIFICATION_CODE_TTL_SECONDS, verificationCode);
    }
}

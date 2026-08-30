package com.gdschongik.gdsc.domain.email.application;

import static com.gdschongik.gdsc.global.common.constant.EmailConstant.*;

import com.gdschongik.gdsc.domain.email.dao.EmailVerificationRepository;
import com.gdschongik.gdsc.domain.email.dao.VerificationAttemptCounter;
import com.gdschongik.gdsc.domain.email.domain.EmailVerification;
import com.gdschongik.gdsc.domain.email.domain.service.EmailValidator;
import com.gdschongik.gdsc.domain.email.domain.service.VerificationCodeGenerator;
import com.gdschongik.gdsc.domain.member.dao.MemberRepository;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.global.exception.ErrorCode;
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
public class EmailVerificationCodeSendService {

    private final MemberRepository memberRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final VerificationAttemptCounter verificationAttemptCounter;

    private final MailSender mailSender;
    private final MemberUtil memberUtil;
    private final EmailValidator emailValidator;
    private final VerificationCodeGenerator verificationCodeGenerator;

    // TODO: 기획에서 확정된 메일 양식으로 교체. 현재는 코드가 보이기만 하는 임시 디자인
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

    public void sendPreviousMemberVerificationCode(Long previousMemberId) {
        Member currentMember = memberUtil.getCurrentMember();
        Member previousMember = memberRepository
                .findById(previousMemberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        emailValidator.validateSendEmailVerificationCode(currentMember.getId(), previousMemberId);

        // 코드 저장
        String verificationCode = verificationCodeGenerator.generate();
        EmailVerification emailVerification = EmailVerification.create(
                currentMember.getId(), previousMemberId, verificationCode, VERIFICATION_CODE_TTL.toSeconds());
        emailVerificationRepository.save(emailVerification);
        verificationAttemptCounter.initializeEmailVerificationAttemptCount(
                currentMember.getId(), VERIFICATION_CODE_TTL.toSeconds());

        // 이메일 발송
        String mailContent = writeMailContentWithVerificationCode(verificationCode);
        mailSender.send(previousMember.getEmail(), VERIFICATION_EMAIL_SUBJECT, mailContent);

        log.info(
                "[EmailVerificationCodeSendService] 본인 인증 메일 발송: currentMemberId={}, previousMemberId={}",
                currentMember.getId(),
                previousMemberId);
    }

    private String writeMailContentWithVerificationCode(String verificationCode) {
        return NOTIFICATION_MESSAGE.formatted(VERIFICATION_CODE_TTL.toMinutes(), verificationCode);
    }
}

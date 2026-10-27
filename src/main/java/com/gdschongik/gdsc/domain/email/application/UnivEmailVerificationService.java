package com.gdschongik.gdsc.domain.email.application;

import static com.gdschongik.gdsc.global.common.constant.EmailConstant.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.email.dao.UnivEmailVerificationRepository;
import com.gdschongik.gdsc.domain.email.dao.VerificationAttemptCounter;
import com.gdschongik.gdsc.domain.email.domain.UnivEmailVerification;
import com.gdschongik.gdsc.domain.email.domain.service.UnivEmailValidator;
import com.gdschongik.gdsc.domain.email.domain.service.VerificationCodeGenerator;
import com.gdschongik.gdsc.domain.email.dto.request.UnivEmailVerificationRequest;
import com.gdschongik.gdsc.domain.member.dao.MemberRepository;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.global.exception.CustomException;
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
public class UnivEmailVerificationService {

    private final MemberRepository memberRepository;
    private final UnivEmailVerificationRepository univEmailVerificationRepository;
    private final VerificationAttemptCounter verificationAttemptCounter;

    private final MailSender mailSender;
    private final MemberUtil memberUtil;
    private final UnivEmailValidator univEmailValidator;
    private final VerificationCodeGenerator verificationCodeGenerator;

    public void sendUnivEmailVerificationCode(String univEmail) {
        Member currentMember = memberUtil.getCurrentMember();
        Long secondsSinceLastSent = calculateSecondsSinceLastSent(currentMember.getId());
        boolean isUnivEmailDuplicate = memberRepository.existsByUnivEmail(univEmail);
        univEmailValidator.validateSendUnivEmailVerificationCode(secondsSinceLastSent, univEmail, isUnivEmailDuplicate);

        String verificationCode = verificationCodeGenerator.generate();
        UnivEmailVerification univEmailVerification = UnivEmailVerification.create(
                currentMember.getId(), univEmail, verificationCode, VERIFICATION_CODE_TTL.toSeconds());
        univEmailVerificationRepository.save(univEmailVerification);
        verificationAttemptCounter.initializeUnivEmailVerificationAttemptCount(
                currentMember.getId(), VERIFICATION_CODE_TTL.toSeconds());

        String mailContent = writeMailContentWithVerificationCode(verificationCode);
        mailSender.send(univEmail, VERIFICATION_UNIV_EMAIL_SUBJECT, mailContent);

        log.info("[UnivEmailVerificationService] 재학생 인증 메일 발송: memberId={}", currentMember.getId());
    }

    @Transactional
    public void verifyMemberUnivEmail(UnivEmailVerificationRequest request) {
        Member currentMember = memberUtil.getCurrentMember();
        UnivEmailVerification univEmailVerification = univEmailVerificationRepository
                .findById(currentMember.getId())
                .orElseThrow(() -> new CustomException(UNIV_EMAIL_VERIFICATION_CODE_NOT_SENT));
        long attemptCount = verificationAttemptCounter.increaseUnivEmailVerificationAttemptCount(currentMember.getId());
        univEmailValidator.validateUnivEmailVerificationCode(univEmailVerification, request.code(), attemptCount);

        // TODO: 어플리케이션 이벤트 발행 방식으로 변경
        currentMember.completeUnivEmailVerification(univEmailVerification.getUnivEmail());
        memberRepository.save(currentMember);
        univEmailVerificationRepository.delete(univEmailVerification);

        log.info("[UnivEmailVerificationService] 재학생 인증 완료: memberId={}", currentMember.getId());
    }

    private Long calculateSecondsSinceLastSent(Long memberId) {
        UnivEmailVerification univEmailVerification =
                univEmailVerificationRepository.findById(memberId).orElse(null);

        if (univEmailVerification == null) {
            return null;
        }

        return VERIFICATION_CODE_TTL.toSeconds() - univEmailVerification.getTtl();
    }

    private String writeMailContentWithVerificationCode(String verificationCode) {
        return VERIFICATION_CODE_MAIL_CONTENT.formatted(VERIFICATION_CODE_TTL.toMinutes(), verificationCode);
    }
}

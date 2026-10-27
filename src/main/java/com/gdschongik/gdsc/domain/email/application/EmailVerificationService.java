package com.gdschongik.gdsc.domain.email.application;

import static com.gdschongik.gdsc.global.common.constant.EmailConstant.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.email.dao.EmailVerificationRepository;
import com.gdschongik.gdsc.domain.email.dao.VerificationAttemptCounter;
import com.gdschongik.gdsc.domain.email.domain.EmailVerification;
import com.gdschongik.gdsc.domain.email.domain.event.PreviousEmailVerifiedEvent;
import com.gdschongik.gdsc.domain.email.domain.service.EmailValidator;
import com.gdschongik.gdsc.domain.email.domain.service.VerificationCodeGenerator;
import com.gdschongik.gdsc.domain.email.dto.request.PreviousEmailVerificationRequest;
import com.gdschongik.gdsc.domain.member.dao.MemberRepository;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.global.util.MemberUtil;
import com.gdschongik.gdsc.global.util.email.MailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailVerificationService {

    private final MemberRepository memberRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final VerificationAttemptCounter verificationAttemptCounter;

    private final MailSender mailSender;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final MemberUtil memberUtil;
    private final EmailValidator emailValidator;
    private final VerificationCodeGenerator verificationCodeGenerator;

    public void sendPreviousMemberVerificationCode(Long previousMemberId) {
        Member currentMember = memberUtil.getCurrentMember();
        Member previousMember =
                memberRepository.findById(previousMemberId).orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
        Long secondsSinceLastSent = calculateSecondsSinceLastSent(currentMember.getId());
        emailValidator.validateSendEmailVerificationCode(secondsSinceLastSent, currentMember.getId(), previousMemberId);

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
                "[EmailVerificationService] 본인 인증 메일 발송: currentMemberId={}, previousMemberId={}",
                currentMember.getId(),
                previousMemberId);
    }

    @Transactional
    public Long verifyPreviousMemberEmail(PreviousEmailVerificationRequest request) {
        Long currentMemberId = memberUtil.getCurrentMemberId();
        EmailVerification emailVerification = emailVerificationRepository
                .findById(currentMemberId)
                .orElseThrow(() -> new CustomException(EMAIL_VERIFICATION_CODE_NOT_SENT));
        long attemptCount = verificationAttemptCounter.increaseEmailVerificationAttemptCount(currentMemberId);
        emailValidator.validateEmailVerificationCode(emailVerification, request.code(), attemptCount);
        emailVerificationRepository.delete(emailVerification);

        applicationEventPublisher.publishEvent(new PreviousEmailVerifiedEvent(
                emailVerification.getCurrentMemberId(), emailVerification.getPreviousMemberId()));
        log.info(
                "[EmailVerificationService] 이메일 인증 완료: currentMemberId={}, previousMemberId={}",
                emailVerification.getCurrentMemberId(),
                emailVerification.getPreviousMemberId());
        return emailVerification.getPreviousMemberId();
    }

    private Long calculateSecondsSinceLastSent(Long currentMemberId) {
        EmailVerification emailVerification =
                emailVerificationRepository.findById(currentMemberId).orElse(null);

        if (emailVerification == null) {
            return null;
        }

        return VERIFICATION_CODE_TTL.toSeconds() - emailVerification.getTtl();
    }

    private String writeMailContentWithVerificationCode(String verificationCode) {
        return EMAIL_VERIFICATION_CODE_MAIL_CONTENT.formatted(VERIFICATION_CODE_TTL.toMinutes(), verificationCode);
    }
}

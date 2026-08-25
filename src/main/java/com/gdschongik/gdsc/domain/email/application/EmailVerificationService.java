package com.gdschongik.gdsc.domain.email.application;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.email.dao.EmailVerificationRepository;
import com.gdschongik.gdsc.domain.email.domain.EmailVerification;
import com.gdschongik.gdsc.domain.email.domain.event.PreviousEmailVerifiedEvent;
import com.gdschongik.gdsc.domain.email.dto.request.PreviousEmailVerificationRequest;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.global.util.MemberUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
// TODO SendCodeService와 통합 검토
public class EmailVerificationService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final MemberUtil memberUtil;

    @Transactional
    public Long verifyPreviousMemberEmail(PreviousEmailVerificationRequest request) {
        EmailVerification emailVerification = emailVerificationRepository
                .findById(memberUtil.getCurrentMemberId())
                .orElseThrow(() -> new CustomException(EMAIL_VERIFICATION_CODE_NOT_SENT));
        emailVerification.validateCode(request.code());
        emailVerificationRepository.delete(emailVerification);

        applicationEventPublisher.publishEvent(new PreviousEmailVerifiedEvent(
                emailVerification.getCurrentMemberId(), emailVerification.getPreviousMemberId()));
        log.info(
                "[EmailVerificationService] 이메일 인증 완료: currentMemberId={}, previousMemberId={}",
                emailVerification.getCurrentMemberId(),
                emailVerification.getPreviousMemberId());
        return emailVerification.getPreviousMemberId();
    }
}

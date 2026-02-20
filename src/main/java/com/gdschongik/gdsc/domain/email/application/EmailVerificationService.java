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
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EmailVerificationService {

    private final MemberUtil memberUtil;
    private final EmailVerificationRepository emailVerificationRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public Long verifyPreviousMemberEmail(PreviousEmailVerificationRequest request) {
        Long currentMemberId = memberUtil.getCurrentMemberId();
        EmailVerification emailVerification = emailVerificationRepository
                .findById(currentMemberId)
                .orElseThrow(() -> new CustomException(EMAIL_NOT_SENT));
        emailVerification.verify(request.token());

        applicationEventPublisher.publishEvent(
                new PreviousEmailVerifiedEvent(currentMemberId, emailVerification.getPreviousMemberId()));
        log.info(
                "[EmailVerificationService] 이메일 인증 완료: currentMemberId={}, previousMemberId={}",
                currentMemberId,
                emailVerification.getPreviousMemberId());
        return emailVerification.getPreviousMemberId();
    }
}

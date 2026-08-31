package com.gdschongik.gdsc.domain.email.domain.service;

import static com.gdschongik.gdsc.global.common.constant.EmailConstant.VERIFICATION_CODE_RESEND_WAIT_TIME;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.email.domain.EmailVerification;
import com.gdschongik.gdsc.global.annotation.DomainService;
import com.gdschongik.gdsc.global.exception.CustomException;
import jakarta.annotation.Nullable;

@DomainService
public class EmailValidator {

    private static final int MAX_ATTEMPT_COUNT = 5;

    /**
     * 본인 인증 코드 발송 전 재발송 대기 시간과 과거 계정을 검증합니다.
     *
     * @param secondsSinceLastSend 마지막 발송 이후 경과한 시간(초). 이전 발송 이력이 없다면 null
     * @param currentMemberId 현재 로그인한 유저의 아이디
     * @param previousMemberId 유저의 옛 계정 아이디
     */
    public void validateSendEmailVerificationCode(
            @Nullable Long secondsSinceLastSend, Long currentMemberId, Long previousMemberId) {
        if (secondsSinceLastSend != null && secondsSinceLastSend < VERIFICATION_CODE_RESEND_WAIT_TIME.toSeconds()) {
            throw new CustomException(EMAIL_VERIFICATION_CODE_RESEND_WAIT_TIME_NOT_PASSED);
        }

        if (currentMemberId.equals(previousMemberId)) {
            throw new CustomException(EMAIL_VERIFICATION_SAME_MEMBER);
        }
    }

    /**
     * 본인 인증 코드를 검증합니다.
     *
     * @param emailVerification 인증 정보
     * @param code 입력한 인증 코드
     * @param attemptCount 저장소에서 원자적으로 증가시킨 현재 시도 횟수
     */
    public void validateEmailVerificationCode(EmailVerification emailVerification, String code, long attemptCount) {
        if (attemptCount > MAX_ATTEMPT_COUNT) {
            throw new CustomException(EMAIL_VERIFICATION_CODE_ATTEMPT_EXCEEDED);
        }

        emailVerification.validateCode(code);
    }
}

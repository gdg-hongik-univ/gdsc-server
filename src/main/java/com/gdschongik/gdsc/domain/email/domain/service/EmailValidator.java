package com.gdschongik.gdsc.domain.email.domain.service;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.email.domain.EmailVerification;
import com.gdschongik.gdsc.global.annotation.DomainService;
import com.gdschongik.gdsc.global.exception.CustomException;

@DomainService
public class EmailValidator {

    private static final int MAX_ATTEMPT_COUNT = 5;

    /**
     * 본인 인증 코드 발송 전 과거 계정을 검증합니다.
     *
     * @param currentMemberId 현재 로그인한 유저의 아이디
     * @param previousMemberId 유저의 옛 계정 아이디
     */
    public void validateSendEmailVerificationCode(Long currentMemberId, Long previousMemberId) {
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

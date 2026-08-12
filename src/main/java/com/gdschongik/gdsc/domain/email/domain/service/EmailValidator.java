package com.gdschongik.gdsc.domain.email.domain.service;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.global.annotation.DomainService;
import com.gdschongik.gdsc.global.exception.CustomException;

@DomainService
public class EmailValidator {

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
}

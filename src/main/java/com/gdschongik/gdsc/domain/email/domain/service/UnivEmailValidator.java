package com.gdschongik.gdsc.domain.email.domain.service;

import static com.gdschongik.gdsc.global.common.constant.EmailConstant.HONGIK_UNIV_MAIL_DOMAIN;
import static com.gdschongik.gdsc.global.common.constant.RegexConstant.HONGIK_EMAIL;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.email.domain.UnivEmailVerification;
import com.gdschongik.gdsc.global.annotation.DomainService;
import com.gdschongik.gdsc.global.exception.CustomException;
import java.util.Optional;

@DomainService
public class UnivEmailValidator {

    public void validateSendUnivEmailVerificationLink(String email, boolean isUnivEmailDuplicate) {
        if (!email.contains(HONGIK_UNIV_MAIL_DOMAIN)) {
            throw new CustomException(UNIV_EMAIL_DOMAIN_MISMATCH);
        }

        if (!email.matches(HONGIK_EMAIL)) {
            throw new CustomException(UNIV_EMAIL_FORMAT_MISMATCH);
        }

        if (isUnivEmailDuplicate) {
            throw new CustomException(UNIV_EMAIL_ALREADY_SATISFIED);
        }
    }

    /**
     * redis 안의 존재하는 메일인증 정보로 학교 메일을 검증
     * 1. 토큰이 비었는데 인증하려할 시 에러 (인증메일을 보내지 않았거나, 만료된 경우)
     * 2. 토큰이 redis에 저장된 토큰과 다르면 만료되었다는 에러 (메일 여러번 보낸 경우)
     */
    public void validateUnivEmailVerification(
            Optional<UnivEmailVerification> optionalUnivEmailVerification, String currentToken) {
        if (optionalUnivEmailVerification.isEmpty()) {
            throw new CustomException(EMAIL_NOT_SENT);
        }

        if (!optionalUnivEmailVerification.get().getVerificationToken().equals(currentToken)) {
            throw new CustomException(EXPIRED_EMAIL_VERIFICATION_TOKEN);
        }
    }
}

package com.gdschongik.gdsc.domain.email.domain.service;

import static com.gdschongik.gdsc.global.common.constant.EmailConstant.HONGIK_UNIV_MAIL_DOMAIN;
import static com.gdschongik.gdsc.global.common.constant.RegexConstant.HONGIK_EMAIL;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.global.annotation.DomainService;
import com.gdschongik.gdsc.global.exception.CustomException;

@DomainService
public class UnivEmailValidator {

    /**
     * 학교 메일 인증 전 메일을 검증합니다.
     *
     * @param email 학교 메일
     * @param isUnivEmailDuplicate 이미 가입된 메일이 있는지 여부 (DB 조회 값)
     */
    public void validateSendUnivEmailVerificationCode(String email, boolean isUnivEmailDuplicate) {
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
}

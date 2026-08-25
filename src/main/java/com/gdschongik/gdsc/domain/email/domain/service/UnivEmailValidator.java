package com.gdschongik.gdsc.domain.email.domain.service;

import static com.gdschongik.gdsc.global.common.constant.EmailConstant.HONGIK_UNIV_MAIL_DOMAIN;
import static com.gdschongik.gdsc.global.common.constant.RegexConstant.HONGIK_EMAIL;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.email.domain.UnivEmailVerification;
import com.gdschongik.gdsc.global.annotation.DomainService;
import com.gdschongik.gdsc.global.exception.CustomException;

@DomainService
public class UnivEmailValidator {

    private static final int MAX_ATTEMPT_COUNT = 5;

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

    /**
     * 재학생 인증 코드를 검증합니다.
     *
     * @param univEmailVerification 인증 정보
     * @param code 입력한 인증 코드
     * @param attemptCount 저장소에서 원자적으로 증가시킨 현재 시도 횟수
     */
    public void validateUnivEmailVerificationCode(
            UnivEmailVerification univEmailVerification, String code, long attemptCount) {
        if (attemptCount > MAX_ATTEMPT_COUNT) {
            throw new CustomException(EMAIL_VERIFICATION_CODE_ATTEMPT_EXCEEDED);
        }

        univEmailVerification.validateCode(code);
    }
}

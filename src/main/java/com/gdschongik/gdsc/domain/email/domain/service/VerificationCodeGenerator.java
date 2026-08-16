package com.gdschongik.gdsc.domain.email.domain.service;

import com.gdschongik.gdsc.global.annotation.DomainService;
import java.security.SecureRandom;

@DomainService
public class VerificationCodeGenerator {

    private static final int CODE_BOUND = 1_000_000;
    private static final String CODE_FORMAT = "%06d";

    private final SecureRandom secureRandom;

    public VerificationCodeGenerator(SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
    }

    /**
     * 메일 인증에 사용할 6자리 숫자 코드를 생성합니다.
     * 앞자리가 0인 코드도 유효하므로 문자열로 코드를 생성합니다.
     */
    public String generate() {
        return String.format(
                CODE_FORMAT,
                secureRandom.nextInt(CODE_BOUND)); // 0 ~ 999,999 사이의 무작위 숫자를 6자리 정수로 포맷팅 ex) 1234 -> 001234
    }
}

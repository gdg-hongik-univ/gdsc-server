package com.gdschongik.gdsc.domain.email.domain;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.global.exception.CustomException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@RedisHash(value = "univEmailVerification")
public class UnivEmailVerification {

    @Id
    private Long memberId;

    private String univEmail;

    private String code;

    @TimeToLive
    private long ttl;

    @Builder(access = AccessLevel.PRIVATE)
    private UnivEmailVerification(Long memberId, String univEmail, String code, long ttl) {
        this.memberId = memberId;
        this.univEmail = univEmail;
        this.code = code;
        this.ttl = ttl;
    }

    public static UnivEmailVerification create(Long memberId, String univEmail, String code, long ttl) {
        return UnivEmailVerification.builder()
                .memberId(memberId)
                .univEmail(univEmail)
                .code(code)
                .ttl(ttl)
                .build();
    }

    // TODO: 무차별 대입 방어. 시도 횟수 제한 및 실패 누적 시 인증정보 무효화 필요
    public void validateCode(String code) {
        if (!this.code.equals(code)) {
            throw new CustomException(EMAIL_VERIFICATION_CODE_MISMATCH);
        }
    }
}

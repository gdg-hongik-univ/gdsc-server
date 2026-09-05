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
@RedisHash(value = "emailVerification")
public class EmailVerification {

    @Id
    private Long currentMemberId;

    private Long previousMemberId;

    private String code;

    @TimeToLive
    private long ttl;

    @Builder(access = AccessLevel.PRIVATE)
    private EmailVerification(Long currentMemberId, Long previousMemberId, String code, long ttl) {
        this.currentMemberId = currentMemberId;
        this.previousMemberId = previousMemberId;
        this.code = code;
        this.ttl = ttl;
    }

    public static EmailVerification create(Long currentMemberId, Long previousMemberId, String code, long ttl) {
        return EmailVerification.builder()
                .currentMemberId(currentMemberId)
                .previousMemberId(previousMemberId)
                .code(code)
                .ttl(ttl)
                .build();
    }

    public void validateCode(String code) {
        if (!this.code.equals(code)) {
            throw new CustomException(EMAIL_VERIFICATION_CODE_MISMATCH);
        }
    }
}

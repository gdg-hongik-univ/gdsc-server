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
    private String verificationToken;

    private Long currentMemberId;

    private Long previousMemberId;

    @TimeToLive
    private long ttl;

    @Builder(access = AccessLevel.PRIVATE)
    private EmailVerification(String verificationToken, Long currentMemberId, Long previousMemberId, long ttl) {
        this.verificationToken = verificationToken;
        this.currentMemberId = currentMemberId;
        this.previousMemberId = previousMemberId;
        this.ttl = ttl;
    }

    public static EmailVerification of(
            String verificationToken, Long currentMemberId, Long previousMemberId, long ttl) {
        return EmailVerification.builder()
                .verificationToken(verificationToken)
                .currentMemberId(currentMemberId)
                .previousMemberId(previousMemberId)
                .ttl(ttl)
                .build();
    }

    public void verify(String token) {
        if (!this.verificationToken.equals(token)) {
            throw new CustomException(EXPIRED_EMAIL_VERIFICATION_TOKEN);
        }
    }
}

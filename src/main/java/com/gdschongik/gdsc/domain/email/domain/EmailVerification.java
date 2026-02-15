package com.gdschongik.gdsc.domain.email.domain;

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
    private Long memberId;

    private Long previousMemberId;

    private String verificationToken;

    @TimeToLive
    private long ttl;

    @Builder(access = AccessLevel.PRIVATE)
    private EmailVerification(Long memberId, Long previousMemberId, String verificationToken, long ttl) {
        this.memberId = memberId;
        this.previousMemberId = previousMemberId;
        this.verificationToken = verificationToken;
        this.ttl = ttl;
    }

    public static EmailVerification of(Long memberId, Long previousMemberId, String verificationToken, long ttl) {
        return EmailVerification.builder()
                .memberId(memberId)
                .previousMemberId(previousMemberId)
                .verificationToken(verificationToken)
                .ttl(ttl)
                .build();
    }
}

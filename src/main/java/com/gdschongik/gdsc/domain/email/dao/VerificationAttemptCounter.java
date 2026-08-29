package com.gdschongik.gdsc.domain.email.dao;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 인증 코드의 시도 횟수를 관리합니다.
 *
 * <p> 애플리케이션에서 조회 후 증가시키면 동시성 문제가 발생하므로 레디스의 원자적 증가 연산을 사용합니다.
 */
@Component
@RequiredArgsConstructor
public class VerificationAttemptCounter {

    private static final String EMAIL_VERIFICATION_ATTEMPT_KEY_PREFIX = "emailVerificationAttempt";
    private static final String UNIV_EMAIL_VERIFICATION_ATTEMPT_KEY_PREFIX = "univEmailVerificationAttempt";
    private static final String INITIAL_ATTEMPT_COUNT = "0";

    private final StringRedisTemplate stringRedisTemplate;

    public void initializeEmailVerificationAttemptCount(Long memberId, long ttlSeconds) {
        String emailKey = generateKey(EMAIL_VERIFICATION_ATTEMPT_KEY_PREFIX, memberId);
        stringRedisTemplate.opsForValue().set(emailKey, INITIAL_ATTEMPT_COUNT, Duration.ofSeconds(ttlSeconds));
    }

    public void initializeUnivEmailVerificationAttemptCount(Long memberId, long ttlSeconds) {
        String univEmailKey = generateKey(UNIV_EMAIL_VERIFICATION_ATTEMPT_KEY_PREFIX, memberId);
        stringRedisTemplate.opsForValue().set(univEmailKey, INITIAL_ATTEMPT_COUNT, Duration.ofSeconds(ttlSeconds));
    }

    public long increaseEmailVerificationAttemptCount(Long memberId) {
        String emailKey = generateKey(EMAIL_VERIFICATION_ATTEMPT_KEY_PREFIX, memberId);
        return stringRedisTemplate.opsForValue().increment(emailKey);
    }

    public long increaseUnivEmailVerificationAttemptCount(Long memberId) {
        String univEmailKey = generateKey(UNIV_EMAIL_VERIFICATION_ATTEMPT_KEY_PREFIX, memberId);
        return stringRedisTemplate.opsForValue().increment(univEmailKey);
    }

    private String generateKey(String keyPrefix, Long memberId) {
        return "%s:%d".formatted(keyPrefix, memberId);
    }
}

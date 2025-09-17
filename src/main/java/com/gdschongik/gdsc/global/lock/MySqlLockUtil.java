package com.gdschongik.gdsc.global.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * MySQL 네임드 락 기능을 이용해 락을 제어하는 LockUtil 구현체입니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MySqlLockUtil implements LockUtil {

    private final JdbcTemplate jdbcTemplate;

    private static final String GET_LOCK_QUERY = "SELECT GET_LOCK(?, ?)";
    private static final String RELEASE_LOCK_QUERY = "SELECT RELEASE_LOCK(?)";

    /**
     * MySQL 네임드 락을 획득합니다.
     * @param lockName 락 이름
     * @param timeoutSec 최대 대기 시간(초)
     * @return 락 획득 성공 여부
     */
    public boolean acquireLock(String lockName, long timeoutSec) {
        Integer result = jdbcTemplate.queryForObject(GET_LOCK_QUERY, Integer.class, lockName, timeoutSec);
        boolean acquired = result != null && result == 1; // GET_LOCK 결과: 1(성공), 0(타임아웃), null(에러)

        if (acquired) {
            log.info("[DistributedLock] 락 획득 성공: {}", lockName);
        } else {
            log.info("[DistributedLock] 락 획득 실패: {}", lockName);
        }

        return acquired;
    }

    /**
     * MySQL 네임드 락을 해제합니다.
     * @param lockName 락 이름
     * @return 락 해제 성공 여부
     */
    public boolean releaseLock(String lockName) {
        Integer result = jdbcTemplate.queryForObject(RELEASE_LOCK_QUERY, Integer.class, lockName);
        boolean released = result != null && result == 1; // RELEASE_LOCK 결과: 1(성공), 0(타임아웃), null(에러)

        if (released) {
            log.info("[DistributedLock] 락 해제 성공: {}", lockName);
        } else {
            log.info("[DistributedLock] 락 해제 실패: {}", lockName);
        }

        return released;
    }
}

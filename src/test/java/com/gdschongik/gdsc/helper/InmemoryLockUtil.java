package com.gdschongik.gdsc.helper;

import com.gdschongik.gdsc.global.lock.LockUtil;
import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import lombok.extern.slf4j.Slf4j;

/**
 * 테스트를 위한 인메모리 락 유틸 구현체입니다.
 */
@Slf4j
public class InmemoryLockUtil implements LockUtil {

    private final Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    /**
     * 인메모리 락을 획득합니다.
     */
    public boolean acquireLock(Connection conn, String key, long timeoutSec) {
        ReentrantLock lock = locks.computeIfAbsent(key, k -> new ReentrantLock());

        try {
            boolean acquired = lock.tryLock(timeoutSec, TimeUnit.SECONDS);

            if (acquired) {
                log.info("[InMemoryLockUtil] 락 획득: {}", key);
            } else {
                log.info("[InMemoryLockUtil] 락 획득 실패: {}", key);
            }

            return acquired;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[InMemoryLockUtil] 락 획득 중 인터럽트 발생: {}", key, e);
            return false;
        }
    }

    /**
     * 인메모리 락을 해제합니다.
     */
    public boolean releaseLock(Connection conn, String lockName) {
        ReentrantLock lock = locks.get(lockName);

        if (lock == null) {
            log.info("[InMemoryLockUtil] 락 해제 실패, 존재하지 않는 락: {}", lockName);
            return false;
        }

        try {
            lock.unlock();
            log.info("[InMemoryLockUtil] 락 해제: {}", lockName);
            return true;
        } catch (IllegalMonitorStateException e) {
            log.error("[InMemoryLockUtil] 락 해제 실패, 현재 스레드가 락을 소유하고 있지 않음: {}", lockName, e);
            return false;
        }
    }
}

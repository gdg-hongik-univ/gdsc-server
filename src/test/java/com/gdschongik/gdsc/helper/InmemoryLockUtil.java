package com.gdschongik.gdsc.helper;

import com.gdschongik.gdsc.global.lock.LockUtil;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

/**
 * 테스트를 위한 인메모리 락 유틸 구현체입니다.
 */
@Slf4j
public class InmemoryLockUtil implements LockUtil {

    private final Map<String, Boolean> locks = new ConcurrentHashMap<>();

    /**
     * 인메모리 락을 획득합니다.
     * @param ignored 락 획득 대기 시간(초), 인메모리 구현에서는 사용하지 않음.
     */
    public boolean acquireLock(String key, long ignored) {
        Boolean previous = locks.putIfAbsent(key, true);
        boolean acquired = previous == null;

        if (acquired) {
            log.info("[InMemoryLockUtil] 락 획득: {}", key);
        } else {
            log.info("[InMemoryLockUtil] 락 획득 실패: {}", key);
        }

        return acquired;
    }

    /**
     * 인메모리 락을 해제합니다.
     */
    public boolean releaseLock(String lockName) {
        Boolean removed = locks.remove(lockName);
        boolean released = removed != null;

        if (released) {
            log.info("[InMemoryLockUtil] 락 해제: {}", lockName);
        } else {
            log.info("[InMemoryLockUtil] 락 해제 실패: {}", lockName);
        }

        return released;
    }
}

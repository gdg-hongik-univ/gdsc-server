package com.gdschongik.gdsc.global.lock;

public interface LockUtil {

    /**
     * 락을 획득합니다.
     * @param key 락 식별자
     * @param timeoutSec 최대 대기 시간(초)
     * @return 락 획득 성공 여부
     */
    boolean acquireLock(String key, long timeoutSec);

    /**
     * 락을 해제합니다.
     * @param key 락 식별자
     * @return 락 해제 성공 여부
     */
    boolean releaseLock(String key);
}

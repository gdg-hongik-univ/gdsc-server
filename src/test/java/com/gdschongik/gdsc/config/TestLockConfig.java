package com.gdschongik.gdsc.config;

import com.gdschongik.gdsc.global.lock.InmemoryLockUtil;
import com.gdschongik.gdsc.global.lock.LockUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * 테스트 환경에서 인메모리 락을 사용하기 위한 설정 클래스입니다.
 * PostgreSQL 락 유틸이 구현이 완료될 때까지 테스트용 설정을 임시 deprecated 처리하였습니다.
 */
@Deprecated
public class TestLockConfig {

    @Primary
    @Bean
    public LockUtil inmemoryLockUtil() {
        return new InmemoryLockUtil();
    }
}

package com.gdschongik.gdsc.config;

import com.gdschongik.gdsc.global.lock.LockUtil;
import com.gdschongik.gdsc.helper.InmemoryLockUtil;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestLockConfig {

    @Primary
    @Bean
    public LockUtil inmemoryLockUtil() {
        return new InmemoryLockUtil();
    }
}

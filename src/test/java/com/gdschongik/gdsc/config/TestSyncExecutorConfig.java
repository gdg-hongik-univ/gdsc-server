package com.gdschongik.gdsc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

public class TestSyncExecutorConfig {

    @Primary
    @Bean
    public TaskExecutor syncTaskExecutor() {
        return new SyncTaskExecutor();
    }
}

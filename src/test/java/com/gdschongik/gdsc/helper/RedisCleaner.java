package com.gdschongik.gdsc.helper;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Profile("test")
@Component
@RequiredArgsConstructor
public class RedisCleaner {

    private final RedisTemplate redisTemplate;

    public void execute() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
    }
}

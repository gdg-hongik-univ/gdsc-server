package com.gdschongik.gdsc.helper;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("test")
@Component
public class RedisCleaner implements InitializingBean {
    private RedisClient redisClient;
    private StatefulRedisConnection<String, String> connection;
    private RedisCommands<String, String> commands;

    @Override
    public void afterPropertiesSet() {
        RedisURI redisUri = RedisURI.Builder.redis("localhost", 6379).build();
        redisClient = RedisClient.create(redisUri);
        connection = redisClient.connect();
        commands = connection.sync();
    }

    public void execute() {
        commands.flushdb();
    }
}

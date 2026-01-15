package com.gdschongik.gdsc.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.net.ServerSocket;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import redis.embedded.RedisServer;

@TestConfiguration
public class TestRedisConfig {

    private int port;

    private RedisServer redisServer;

    @PostConstruct
    public void redisServer() throws IOException {
        port = findAvailablePort();
        redisServer = new RedisServer(port);
        redisServer.start();
    }

    @PreDestroy
    public void stopRedis() throws IOException {
        if (redisServer != null) {
            redisServer.stop();
        }
    }

    @Bean
    @Primary
    public RedisConnectionFactory testConnectionFactory() {
        return new LettuceConnectionFactory("localhost", port);
    }

    /**
     * 현재 PC/서버에서 사용가능한 포트 조회
     */
    private int findAvailablePort() {
        try (ServerSocket socket = new ServerSocket(0)) { // 사용 가능한 TCP 포트 할당
            socket.setReuseAddress(false); // 포트 재사용 방지
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to allocate a TCP port", e);
        }
    }
}

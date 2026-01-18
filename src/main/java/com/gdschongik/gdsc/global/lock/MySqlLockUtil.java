package com.gdschongik.gdsc.global.lock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    @Override
    public boolean acquireLock(Connection conn, String key, long timeout) {
        try (PreparedStatement pstmt = conn.prepareStatement(GET_LOCK_QUERY)) {
            pstmt.setString(1, key);
            pstmt.setLong(2, timeout);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int result = rs.getInt(1);
                    log.info("[MySqlLockUtil] 락 획득 성공: {}", key);
                    return result == 1;
                }
            }
        } catch (SQLException e) {
            log.info("[MySqlLockUtil] 락 획득 실패: {}", key);
        }

        return false;
    }

    @Override
    public boolean releaseLock(Connection conn, String key) {
        try (PreparedStatement pstmt = conn.prepareStatement(RELEASE_LOCK_QUERY)) {
            pstmt.setString(1, key);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int result = rs.getInt(1); // 1:성공, 0:권한없음, NULL:없음
                    log.info("[MySqlLockUtil] 락 해제 성공: {}", key);
                    return result == 1;
                }
            }
        } catch (SQLException e) {
            log.info("[MySqlLockUtil] 락 해제 실패: {}", key);
        }
        return false;
    }
}

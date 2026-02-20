package com.gdschongik.gdsc.global.lock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * MySQL 네임드 락 기능을 이용해 락을 제어하는 LockUtil 구현체입니다.
 *
 * PostgreSQL 마이그레이션에 따라 deprecated 처리되었습니다.
 * TODO: 구현 참고하여 PostgreSQL Advisory Lock 기능을 이용한 새로운 LockUtil 구현체 작성 필요
 */
@Deprecated
@Slf4j
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

            return executeAcquireLock(pstmt, key);
        } catch (SQLException e) {
            log.info("[MySqlLockUtil] 락 획득 실패: {}", key);
            return false;
        }
    }

    private boolean executeAcquireLock(PreparedStatement pstmt, String key) throws SQLException {
        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next() && rs.getInt(1) == 1) {
                log.info("[MySqlLockUtil] 락 획득 성공: {}", key);
                return true;
            } else {
                log.info("[MySqlLockUtil] 락 획득 실패: {}", key);
                return false;
            }
        }
    }

    @Override
    public boolean releaseLock(Connection conn, String key) {
        try (PreparedStatement pstmt = conn.prepareStatement(RELEASE_LOCK_QUERY)) {
            pstmt.setString(1, key);
            return executeReleaseLock(pstmt, key);
        } catch (SQLException e) {
            log.info("[MySqlLockUtil] 락 해제 실패: {}", key);
            return false;
        }
    }

    private boolean executeReleaseLock(PreparedStatement pstmt, String key) throws SQLException {
        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next() && rs.getInt(1) == 1) {
                log.info("[MySqlLockUtil] 락 해제 성공: {}", key);
                return true;
            } else {
                log.info("[MySqlLockUtil] 락 해제 실패: {}", key);
                return false;
            }
        }
    }
}

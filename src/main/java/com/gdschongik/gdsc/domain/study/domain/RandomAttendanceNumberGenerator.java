package com.gdschongik.gdsc.domain.study.domain;

import java.security.SecureRandom;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

/**
 * 네 자리의 랜덤한 출석번호를 생성합니다.
 */
@Component
public class RandomAttendanceNumberGenerator implements AttendanceNumberGenerator {

    public static final int MIN_ORIGIN = 1000;
    public static final int MAX_BOUND = 10000;

    @Override
    @SneakyThrows
    public String generate() {
        return String.valueOf(SecureRandom.getInstanceStrong()
                .ints(MIN_ORIGIN, MAX_BOUND)
                .findFirst()
                .orElseThrow());
    }
}

package com.gdschongik.gdsc.global.common.constant;

import com.gdschongik.gdsc.domain.common.vo.Period;
import java.time.LocalDateTime;

public class EventConstant {

    private EventConstant() {}

    public static final String EVENT_NAME = "2025년 1학기 새내기 배움터";
    public static final String APPLICATION_DESCRIPTION = "2025년 1학기 새내기 배움터 신청 폼입니다. 신청 기간은 3월 1일부터 3월 14일까지입니다.";
    public static final String VENUE = "홍익대학교 제 4공학관(T동) 608호";
    public static final Period EVENT_APPLICATION_PERIOD =
            Period.of(LocalDateTime.of(2025, 3, 1, 0, 0), LocalDateTime.of(2025, 3, 14, 23, 59));
    public static final int MAIN_EVENT_MAX_APPLICATION_COUNT = 100;
    public static final int AFTER_PARTY_MAX_APPLICATION_COUNT = 100;
}

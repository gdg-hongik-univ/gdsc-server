package com.gdschongik.gdsc.global.common.constant;

import com.gdschongik.gdsc.domain.common.vo.Period;
import com.gdschongik.gdsc.domain.event.domain.UsageStatus;
import java.time.LocalDateTime;

public class EventConstant {

    private EventConstant() {}

    public static final String EVENT_NAME = "2025년 1학기 새내기 배움터";
    public static final String APPLICATION_DESCRIPTION =
            "2025년 1학기 새내기 배움터 신청 폼입니다. 진행일시는 3월 15일, 신청 기간은 3월 1일부터 3월 14일까지입니다.";
    public static final String VENUE = "홍익대학교 제 4공학관(T동) 608호";
    public static final LocalDateTime EVENT_START_AT = LocalDateTime.of(2025, 3, 15, 0, 0);
    public static final LocalDateTime EVENT_APPLICATION_START_AT = LocalDateTime.of(2025, 3, 1, 0, 0);
    public static final LocalDateTime EVENT_APPLICATION_END_AT = LocalDateTime.of(2025, 3, 14, 23, 59);
    public static final Period EVENT_APPLICATION_PERIOD =
            Period.of(EVENT_APPLICATION_START_AT, EVENT_APPLICATION_END_AT);
    public static final UsageStatus REGULAR_ROLE_ONLY_STATUS = UsageStatus.DISABLED;
    public static final UsageStatus AFTER_PARTY_STATUS = UsageStatus.ENABLED;
    public static final UsageStatus PRE_PAYMENT_STATUS = UsageStatus.ENABLED;
    public static final UsageStatus POST_PAYMENT_STATUS = UsageStatus.DISABLED;
    public static final UsageStatus RSVP_QUESTION_STATUS = UsageStatus.DISABLED;
    public static final int MAIN_EVENT_MAX_APPLICATION_COUNT = 100;
    public static final int AFTER_PARTY_MAX_APPLICATION_COUNT = 100;
}

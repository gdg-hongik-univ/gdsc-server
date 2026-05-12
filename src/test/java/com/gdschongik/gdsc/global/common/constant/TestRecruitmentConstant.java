package com.gdschongik.gdsc.global.common.constant;

import com.gdschongik.gdsc.domain.common.vo.Money;
import com.gdschongik.gdsc.domain.common.vo.Period;
import com.gdschongik.gdsc.domain.recruitment.domain.RoundType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TestRecruitmentConstant {
    // 1차 모집 상수
    public static final String RECRUITMENT_ROUND_NAME = "2024학년도 1학기 1차 모집";
    public static final LocalDateTime START_DATE = LocalDateTime.of(2024, 3, 2, 0, 0);
    public static final LocalDateTime BETWEEN_START_AND_END_DATE = LocalDateTime.of(2024, 3, 3, 0, 0);
    public static final LocalDateTime WRONG_END_DATE = LocalDateTime.of(2024, 3, 2, 0, 0);
    public static final LocalDateTime END_DATE = LocalDateTime.of(2024, 3, 5, 0, 0);
    public static final Period START_TO_END_PERIOD = Period.of(START_DATE, END_DATE);

    public static final Money FEE = Money.from(20000L);
    public static final BigDecimal FEE_AMOUNT = BigDecimal.valueOf(20000);
    public static final String FEE_NAME = "2024학년도 1학기 정회원 회비";
    public static final RoundType ROUND_TYPE = RoundType.FIRST;

    // 2차 모집 상수
    public static final String ROUND_TWO_RECRUITMENT_NAME = "2024학년도 1학기 2차 모집";
    public static final LocalDateTime ROUND_TWO_START_DATE = LocalDateTime.of(2024, 3, 8, 0, 0);
    public static final LocalDateTime ROUND_TWO_END_DATE = LocalDateTime.of(2024, 3, 10, 0, 0);
    public static final Period ROUND_TWO_START_TO_END_PERIOD = Period.of(ROUND_TWO_START_DATE, ROUND_TWO_END_DATE);

    private TestRecruitmentConstant() {}
}

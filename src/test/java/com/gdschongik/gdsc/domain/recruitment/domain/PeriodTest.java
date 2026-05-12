package com.gdschongik.gdsc.domain.recruitment.domain;

import static com.gdschongik.gdsc.global.common.constant.TestRecruitmentConstant.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.DATE_PRECEDENCE_INVALID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gdschongik.gdsc.domain.common.vo.Period;
import com.gdschongik.gdsc.global.exception.CustomException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class PeriodTest {

    @Nested
    class Period생성시 {
        @Test
        void 시작일이_종료일보다_앞서면_성공한다() {
            // when
            Period period = Period.of(START_DATE, END_DATE);

            // then
            assertThat(period.getStartDate()).isEqualTo(START_DATE);
            assertThat(period.getEndDate()).isEqualTo(END_DATE);
        }

        @Test
        void 종료일이_시작일보다_앞서면_실패한다() {
            // when & then
            assertThatThrownBy(() -> {
                        Period.of(END_DATE, START_DATE);
                    })
                    .isInstanceOf(CustomException.class)
                    .hasMessage(DATE_PRECEDENCE_INVALID.getMessage());
        }

        @Test
        void 종료일이_시작일과_같으면_실패한다() {
            // when & then
            assertThatThrownBy(() -> {
                        Period.of(START_DATE, WRONG_END_DATE);
                    })
                    .isInstanceOf(CustomException.class)
                    .hasMessage(DATE_PRECEDENCE_INVALID.getMessage());
        }
    }
}

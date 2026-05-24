package com.gdschongik.gdsc.domain.recruitment.domain;

import static com.gdschongik.gdsc.global.common.constant.TestRecruitmentConstant.*;
import static com.gdschongik.gdsc.global.common.constant.TestTemporalConstant.*;
import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.domain.common.vo.Period;
import com.gdschongik.gdsc.domain.common.vo.Semester;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class RecruitmentTest {

    @Nested
    class 학기생성시 {
        @Test
        void Period가_제대로_생성된다() {
            // given
            Period period = Period.of(SEMESTER_START_DATE, SEMESTER_END_DATE);

            // when
            Recruitment recruitment = Recruitment.create(
                    FEE_NAME,
                    FEE,
                    Period.of(SEMESTER_START_DATE, SEMESTER_END_DATE),
                    Semester.of(ACADEMIC_YEAR, SEMESTER_TYPE));

            // then
            assertThat(recruitment.getSemesterPeriod().getStartDate()).isEqualTo(SEMESTER_START_DATE);
            assertThat(recruitment.getSemesterPeriod().getEndDate()).isEqualTo(SEMESTER_END_DATE);
            assertThat(recruitment.getSemesterPeriod().equals(period)).isTrue();
        }
    }
}

package com.gdschongik.gdsc.domain.recruitment.domain;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.domain.recruitment.domain.service.RecruitmentValidator;
import com.gdschongik.gdsc.global.exception.CustomException;
import org.junit.jupiter.api.Test;

public class RecruitmentValidatorTest {

    RecruitmentValidator recruitmentValidator = new RecruitmentValidator();

    @Test
    void 학년도_학기가_모두_중복되는_리쿠르팅이라면_실패한다() {
        // given
        boolean isRecruitmentSemesterOverlap = true;
        boolean isRecruitmentPeriodOverlap = false;

        // when & then
        assertThatThrownBy(() -> recruitmentValidator.validateRecruitmentCreate(
                        isRecruitmentSemesterOverlap, isRecruitmentPeriodOverlap))
                .isInstanceOf(CustomException.class)
                .hasMessage(RECRUITMENT_SEMESTER_OVERLAP.getMessage());
    }

    @Test
    void 기간이_중복되는_리쿠르팅이라면_실패한다() {
        // given
        boolean isRecruitmentSemesterOverlap = false;
        boolean isRecruitmentPeriodOverlap = true;

        // when & then
        assertThatThrownBy(() -> recruitmentValidator.validateRecruitmentCreate(
                        isRecruitmentSemesterOverlap, isRecruitmentPeriodOverlap))
                .isInstanceOf(CustomException.class)
                .hasMessage(RECRUITMENT_PERIOD_OVERLAP.getMessage());
    }
}

package com.gdschongik.gdsc.domain.recruitment.domain;

import static com.gdschongik.gdsc.global.common.constant.RecruitmentConstant.*;
import static com.gdschongik.gdsc.global.common.constant.TemporalConstant.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.domain.common.vo.Period;
import com.gdschongik.gdsc.domain.common.vo.Semester;
import com.gdschongik.gdsc.domain.recruitment.domain.service.RecruitmentRoundValidator;
import com.gdschongik.gdsc.global.exception.CustomException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class RecruitmentRoundValidatorTest {

    RecruitmentRoundValidator recruitmentRoundValidator = new RecruitmentRoundValidator();

    @Nested
    class 모집회차_생성시 {

        @Test
        void 모집_시작일과_종료일의_연도가_입력된_학년도와_다르다면_실패한다() {
            // given
            Recruitment recruitment = Recruitment.create(
                    FEE_NAME,
                    FEE,
                    Period.of(LocalDateTime.of(2025, 3, 2, 0, 0), LocalDateTime.of(2025, 8, 31, 0, 0)),
                    Semester.of(2025, SEMESTER_TYPE));

            // when & then
            assertThatThrownBy(() -> recruitmentRoundValidator.validateRecruitmentRoundCreate(
                            START_DATE, END_DATE, ROUND_TYPE, recruitment, List.of()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(RECRUITMENT_PERIOD_NOT_WITHIN_TWO_WEEKS.getMessage());
        }

        @Test
        void 학기_시작일과_종료일의_학기가_입력된_학기와_다르다면_실패한다() {
            // given
            Recruitment recruitment = Recruitment.create(
                    FEE_NAME,
                    FEE,
                    Period.of(LocalDateTime.of(2024, 9, 1, 0, 0), LocalDateTime.of(2025, 2, 28, 0, 0)),
                    SEMESTER);

            // when & then
            assertThatThrownBy(() -> recruitmentRoundValidator.validateRecruitmentRoundCreate(
                            START_DATE, END_DATE, ROUND_TYPE, recruitment, List.of()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(RECRUITMENT_PERIOD_NOT_WITHIN_TWO_WEEKS.getMessage());
        }

        @Test
        void 모집_시작일과_종료일이_학기_시작일로부터_2주_이내에_있지_않다면_실패한다() {
            // given
            Recruitment recruitment = Recruitment.create(FEE_NAME, FEE, START_TO_END_PERIOD, SEMESTER);

            // when & then
            assertThatThrownBy(() -> recruitmentRoundValidator.validateRecruitmentRoundCreate(
                            START_DATE, LocalDateTime.of(2024, 4, 10, 0, 0), ROUND_TYPE, recruitment, List.of()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(RECRUITMENT_PERIOD_NOT_WITHIN_TWO_WEEKS.getMessage());
        }

        @Test
        void 학년도_학기_차수가_모두_중복되면_실패한다() {
            // given
            Recruitment recruitment = Recruitment.create(FEE_NAME, FEE, START_TO_END_PERIOD, SEMESTER);

            RecruitmentRound recruitmentRound =
                    RecruitmentRound.create(RECRUITMENT_ROUND_NAME, ROUND_TYPE, START_TO_END_PERIOD, recruitment);

            // when & then
            assertThatThrownBy(() -> recruitmentRoundValidator.validateRecruitmentRoundCreate(
                            LocalDateTime.of(2024, 3, 8, 0, 0),
                            LocalDateTime.of(2024, 3, 10, 0, 0),
                            ROUND_TYPE,
                            recruitment,
                            List.of(recruitmentRound)))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(RECRUITMENT_ROUND_TYPE_OVERLAP.getMessage());
        }

        @Test
        void RoundType_1차가_없을때_2차를_생성하려_하면_실패한다() {
            // given
            Recruitment recruitment = Recruitment.create(FEE_NAME, FEE, START_TO_END_PERIOD, SEMESTER);

            // when & then
            assertThatThrownBy(() -> recruitmentRoundValidator.validateRecruitmentRoundCreate(
                            START_DATE, END_DATE, RoundType.SECOND, recruitment, List.of()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ROUND_ONE_DOES_NOT_EXIST.getMessage());
        }

        @Test
        void 기간이_중복되는_모집회차가_있다면_실패한다() {
            // given
            Recruitment recruitment = Recruitment.create(FEE_NAME, FEE, START_TO_END_PERIOD, SEMESTER);

            RecruitmentRound recruitmentRound =
                    RecruitmentRound.create(RECRUITMENT_ROUND_NAME, ROUND_TYPE, START_TO_END_PERIOD, recruitment);

            // when & then
            assertThatThrownBy(() -> recruitmentRoundValidator.validateRecruitmentRoundCreate(
                            START_DATE, END_DATE, ROUND_TYPE, recruitment, List.of(recruitmentRound)))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(PERIOD_OVERLAP.getMessage());
        }
    }

    @Nested
    class 모집회차_수정시 {

        @Test
        void 기간이_중복되는_모집회차가_있다면_실패한다() {
            // given
            Recruitment recruitment = Recruitment.create(FEE_NAME, FEE, START_TO_END_PERIOD, SEMESTER);

            RecruitmentRound firstRound =
                    RecruitmentRound.create(RECRUITMENT_ROUND_NAME, ROUND_TYPE, START_TO_END_PERIOD, recruitment);
            ReflectionTestUtils.setField(firstRound, "id", 1L);

            RecruitmentRound secondRound = RecruitmentRound.create(
                    RECRUITMENT_ROUND_NAME, RoundType.SECOND, ROUND_TWO_START_TO_END_PERIOD, recruitment);
            ReflectionTestUtils.setField(secondRound, "id", 2L);

            // when & then
            assertThatThrownBy(() -> recruitmentRoundValidator.validateRecruitmentRoundUpdate(
                            START_DATE,
                            ROUND_TWO_END_DATE,
                            LocalDateTime.now(),
                            RoundType.SECOND,
                            secondRound,
                            List.of(firstRound)))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(PERIOD_OVERLAP.getMessage());
        }

        @Test
        void 차수가_중복되는_모집회차가_있다면_실패한다() {
            // given
            Recruitment recruitment = Recruitment.create(FEE_NAME, FEE, START_TO_END_PERIOD, SEMESTER);

            RecruitmentRound firstRound =
                    RecruitmentRound.create(RECRUITMENT_ROUND_NAME, ROUND_TYPE, START_TO_END_PERIOD, recruitment);
            ReflectionTestUtils.setField(firstRound, "id", 1L);

            RecruitmentRound secondRound = RecruitmentRound.create(
                    RECRUITMENT_ROUND_NAME, RoundType.SECOND, ROUND_TWO_START_TO_END_PERIOD, recruitment);
            ReflectionTestUtils.setField(secondRound, "id", 2L);

            // when & then
            assertThatThrownBy(() -> recruitmentRoundValidator.validateRecruitmentRoundUpdate(
                            ROUND_TWO_START_DATE,
                            ROUND_TWO_END_DATE,
                            LocalDateTime.now(),
                            ROUND_TYPE,
                            secondRound,
                            List.of(firstRound)))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(RECRUITMENT_ROUND_TYPE_OVERLAP.getMessage());
        }

        @Test
        void 모집_시작일과_종료일이_학기_시작일로부터_2주_이내에_있지_않다면_실패한다() {
            // given
            Recruitment recruitment = Recruitment.create(FEE_NAME, FEE, START_TO_END_PERIOD, SEMESTER);

            RecruitmentRound firstRound =
                    RecruitmentRound.create(RECRUITMENT_ROUND_NAME, ROUND_TYPE, START_TO_END_PERIOD, recruitment);
            ReflectionTestUtils.setField(firstRound, "id", 1L);

            // when & then
            assertThatThrownBy(() -> recruitmentRoundValidator.validateRecruitmentRoundUpdate(
                            START_DATE,
                            LocalDateTime.of(2024, 4, 10, 0, 0),
                            LocalDateTime.now(),
                            ROUND_TYPE,
                            firstRound,
                            List.of()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(RECRUITMENT_PERIOD_NOT_WITHIN_TWO_WEEKS.getMessage());
        }

        @Test
        void RoundType_1차를_2차로_수정하려_하면_실패한다() {
            // given
            Recruitment recruitment = Recruitment.create(FEE_NAME, FEE, START_TO_END_PERIOD, SEMESTER);

            RecruitmentRound firstRound =
                    RecruitmentRound.create(RECRUITMENT_ROUND_NAME, ROUND_TYPE, START_TO_END_PERIOD, recruitment);
            ReflectionTestUtils.setField(firstRound, "id", 1L);

            // when & then
            assertThatThrownBy(() -> recruitmentRoundValidator.validateRecruitmentRoundUpdate(
                            START_DATE, END_DATE, LocalDateTime.now(), RoundType.SECOND, firstRound, List.of()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ROUND_ONE_DOES_NOT_EXIST.getMessage());
        }

        @Test
        void 모집_시작일이_지났다면_수정_실패한다() {
            // given
            Recruitment recruitment = Recruitment.create(FEE_NAME, FEE, START_TO_END_PERIOD, SEMESTER);

            RecruitmentRound recruitmentRound =
                    RecruitmentRound.create(RECRUITMENT_ROUND_NAME, ROUND_TYPE, START_TO_END_PERIOD, recruitment);
            long recruitmentRoundId = 1L;
            ReflectionTestUtils.setField(recruitmentRound, "id", recruitmentRoundId);

            // when & then
            assertThatThrownBy(() -> recruitmentRoundValidator.validateRecruitmentRoundUpdate(
                            START_DATE, END_DATE, LocalDateTime.now(), ROUND_TYPE, recruitmentRound, List.of()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(RECRUITMENT_ROUND_START_DATE_ALREADY_PASSED.getMessage());
        }
    }
}

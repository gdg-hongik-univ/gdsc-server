package com.gdschongik.gdsc.domain.recruitment.application;

import static com.gdschongik.gdsc.global.common.constant.RecruitmentConstant.*;
import static com.gdschongik.gdsc.global.common.constant.TemporalConstant.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.domain.common.vo.Period;
import com.gdschongik.gdsc.domain.recruitment.dao.RecruitmentRepository;
import com.gdschongik.gdsc.domain.recruitment.dao.RecruitmentRoundRepository;
import com.gdschongik.gdsc.domain.recruitment.domain.Recruitment;
import com.gdschongik.gdsc.domain.recruitment.domain.RecruitmentRound;
import com.gdschongik.gdsc.domain.recruitment.dto.request.RecruitmentCreateRequest;
import com.gdschongik.gdsc.domain.recruitment.dto.request.RecruitmentRoundCreateRequest;
import com.gdschongik.gdsc.domain.recruitment.dto.request.RecruitmentRoundUpdateRequest;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.helper.IntegrationTest;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AdminRecruitmentServiceTest extends IntegrationTest {

    @Autowired
    private AdminRecruitmentService adminRecruitmentService;

    @Autowired
    private RecruitmentRepository recruitmentRepository;

    @Autowired
    private RecruitmentRoundRepository recruitmentRoundRepository;

    @Nested
    class 리쿠르팅_생성시 {

        @Test
        void 성공한다() {
            // given
            Recruitment previousRecruitment =
                    Recruitment.create(FEE_NAME, FEE, Period.of(SEMESTER_START_DATE, SEMESTER_END_DATE), SEMESTER);
            recruitmentRepository.save(previousRecruitment);

            RecruitmentCreateRequest request = new RecruitmentCreateRequest(
                    SEMESTER_START_DATE.plusYears(1), // 이전 리쿠르팅의 다음 연도로 설정
                    SEMESTER_END_DATE.plusYears(1),
                    ACADEMIC_YEAR + 1,
                    SEMESTER_TYPE,
                    FEE_AMOUNT,
                    FEE_NAME);

            // when
            adminRecruitmentService.createRecruitment(request);

            // then
            assertThat(recruitmentRepository.findAll()).hasSize(2);
        }

        @Test
        void 학기와_학년이_중복되는_리쿠르팅이_존재한다면_실패한다() {
            // given
            Recruitment previousRecruitment =
                    Recruitment.create(FEE_NAME, FEE, Period.of(SEMESTER_START_DATE, SEMESTER_END_DATE), SEMESTER);
            recruitmentRepository.save(previousRecruitment);

            RecruitmentCreateRequest request = new RecruitmentCreateRequest(
                    SEMESTER_START_DATE.plusYears(1),
                    SEMESTER_END_DATE.plusYears(1),
                    ACADEMIC_YEAR, // 학기와 학년 중복
                    SEMESTER_TYPE,
                    FEE_AMOUNT,
                    FEE_NAME);

            // when & then
            assertThatThrownBy(() -> adminRecruitmentService.createRecruitment(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(RECRUITMENT_SEMESTER_OVERLAP.getMessage());
        }

        @Test
        void 학기_기간이_겹치는_리쿠르팅이_존재한다면_실패한다() {
            // given
            Recruitment previousRecruitment =
                    Recruitment.create(FEE_NAME, FEE, Period.of(SEMESTER_START_DATE, SEMESTER_END_DATE), SEMESTER);
            recruitmentRepository.save(previousRecruitment);

            RecruitmentCreateRequest request = new RecruitmentCreateRequest(
                    SEMESTER_START_DATE, // 이전 리쿠르팅과 학기 기간 중복
                    SEMESTER_END_DATE,
                    ACADEMIC_YEAR + 1,
                    SEMESTER_TYPE,
                    FEE_AMOUNT,
                    FEE_NAME);

            // when & then
            assertThatThrownBy(() -> adminRecruitmentService.createRecruitment(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(RECRUITMENT_PERIOD_OVERLAP.getMessage());
        }
    }

    @Nested
    class 모집회차_생성시 {

        @Test
        void 학년도와_학기가_일치하는_리쿠르팅이_존재하지_않는다면_실패한다() {
            // given
            RecruitmentRoundCreateRequest request = new RecruitmentRoundCreateRequest(
                    ACADEMIC_YEAR, SEMESTER_TYPE, RECRUITMENT_ROUND_NAME, START_DATE, END_DATE, ROUND_TYPE);

            // when & then
            assertThatThrownBy(() -> adminRecruitmentService.createRecruitmentRound(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(RECRUITMENT_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 모집회차_수정시 {
        @Test
        void 성공한다() {
            // given
            LocalDateTime now = LocalDateTime.now().withNano(0);
            Recruitment recruitment = Recruitment.create(FEE_NAME, FEE, Period.of(now, now.plusMonths(3)), SEMESTER);
            recruitmentRepository.save(recruitment);

            RecruitmentRound recruitmentRound = RecruitmentRound.create(
                    RECRUITMENT_ROUND_NAME, ROUND_TYPE, Period.of(now.plusDays(1), now.plusDays(2)), recruitment);
            recruitmentRoundRepository.save(recruitmentRound);

            RecruitmentRoundUpdateRequest request = new RecruitmentRoundUpdateRequest(
                    ACADEMIC_YEAR, SEMESTER_TYPE, "수정된 모집회차 이름", now.plusDays(2), now.plusDays(3), ROUND_TYPE);

            // when
            adminRecruitmentService.updateRecruitmentRound(recruitmentRound.getId(), request);

            // then
            RecruitmentRound updatedRecruitmentRound = recruitmentRoundRepository
                    .findById(recruitmentRound.getId())
                    .get();
            assertThat(updatedRecruitmentRound.getName()).isEqualTo(request.name());
            assertThat(updatedRecruitmentRound.getPeriod().getStartDate()).isEqualTo(request.startDate());
            assertThat(updatedRecruitmentRound.getPeriod().getEndDate()).isEqualTo(request.endDate());
        }

        @Test
        void 모집회차가_존재하지_않는다면_실패한다() {
            // given
            RecruitmentRoundUpdateRequest request = new RecruitmentRoundUpdateRequest(
                    ACADEMIC_YEAR, SEMESTER_TYPE, RECRUITMENT_ROUND_NAME, START_DATE, END_DATE, ROUND_TYPE);

            // when & then
            assertThatThrownBy(() -> adminRecruitmentService.updateRecruitmentRound(1L, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(RECRUITMENT_ROUND_NOT_FOUND.getMessage());
        }
    }
}

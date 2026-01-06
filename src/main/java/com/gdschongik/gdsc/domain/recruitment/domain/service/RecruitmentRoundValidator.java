package com.gdschongik.gdsc.domain.recruitment.domain.service;

import static com.gdschongik.gdsc.global.common.constant.TemporalConstant.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.recruitment.domain.Recruitment;
import com.gdschongik.gdsc.domain.recruitment.domain.RecruitmentRound;
import com.gdschongik.gdsc.domain.recruitment.domain.RoundType;
import com.gdschongik.gdsc.global.annotation.DomainService;
import com.gdschongik.gdsc.global.exception.CustomException;
import java.time.LocalDateTime;
import java.util.List;

@DomainService
public class RecruitmentRoundValidator {

    public void validateRecruitmentRoundCreate(
            LocalDateTime startDate,
            LocalDateTime endDate,
            RoundType roundType,
            Recruitment recruitment,
            List<RecruitmentRound> recruitmentRoundsInThisSemester) {
        validatePeriodWithinTwoWeeks(startDate, endDate, recruitment);
        validatePeriodOverlap(recruitmentRoundsInThisSemester, startDate, endDate);
        validateRoundOverlap(recruitmentRoundsInThisSemester, roundType);
        validateRoundOneExist(recruitmentRoundsInThisSemester, roundType);
    }

    /**
     * 수정하려는 모집회차의 차수와 기간은 수정 후에 유효하지 않으므로,
     * 변경하려는 값들은 다른 모집회차들과 차수, 기간이 겹치는지 검증해야 합니다.
     * 따라서, 수정하려는 모집회차와 이를 제외한 다른 모집회차들을 분리하여 매개변수로 받습니다.
     *
     * @param currentRecruitmentRound: 수정하려는 모집회차
     * @param otherRecruitmentRounds: 동일 리쿠르팅을 참조하는 모집회차 중 수정하려는 모집회차를 제외한 나머지 모집회차
     */
    public void validateRecruitmentRoundUpdate(
            LocalDateTime startDate,
            LocalDateTime endDate,
            LocalDateTime now,
            RoundType roundType,
            RecruitmentRound currentRecruitmentRound,
            List<RecruitmentRound> otherRecruitmentRounds) {
        validatePeriodWithinTwoWeeks(startDate, endDate, currentRecruitmentRound.getRecruitment());
        validatePeriodOverlap(otherRecruitmentRounds, startDate, endDate);
        validateRoundOverlap(otherRecruitmentRounds, roundType);
        validateRoundOneToTwo(currentRecruitmentRound.getRoundType(), roundType);
        currentRecruitmentRound.validatePeriodNotStarted(now);
    }

    private void validatePeriodWithinTwoWeeks(LocalDateTime startDate, LocalDateTime endDate, Recruitment recruitment) {
        LocalDateTime semesterStartDate = recruitment.getSemesterPeriod().getStartDate();

        validateDateTimeWithinTwoWeeks(startDate, semesterStartDate);
        validateDateTimeWithinTwoWeeks(endDate, semesterStartDate);
    }

    private void validateDateTimeWithinTwoWeeks(LocalDateTime dateTime, LocalDateTime semesterStartDate) {
        if (semesterStartDate.minusWeeks(PRE_SEMESTER_TERM).isAfter(dateTime)
                || semesterStartDate.plusWeeks(PRE_SEMESTER_TERM).isBefore(dateTime)) {
            throw new CustomException(RECRUITMENT_PERIOD_NOT_WITHIN_TWO_WEEKS);
        }
    }

    private void validatePeriodOverlap(
            List<RecruitmentRound> recruitmentRounds, LocalDateTime startDate, LocalDateTime endDate) {
        recruitmentRounds.forEach(
                recruitmentRound -> recruitmentRound.getPeriod().validatePeriodOverlap(startDate, endDate));
    }

    // 학년도, 학기, 모집회차가 모두 같은 경우
    private void validateRoundOverlap(List<RecruitmentRound> recruitmentRounds, RoundType roundType) {
        recruitmentRounds.stream()
                .filter(recruitmentRound -> recruitmentRound.getRoundType() == roundType)
                .findAny()
                .ifPresent(ignored -> {
                    throw new CustomException(RECRUITMENT_ROUND_TYPE_OVERLAP);
                });
    }

    // 1차 모집이 없는데 2차 모집을 생성하려고 하는 경우
    private void validateRoundOneExist(List<RecruitmentRound> recruitmentRounds, RoundType roundType) {
        if (roundType.isSecond() && recruitmentRounds.stream().noneMatch(RecruitmentRound::isFirstRound)) {
            throw new CustomException(ROUND_ONE_DOES_NOT_EXIST);
        }
    }

    // 1차 모집을 비워둬서는 안되므로, 1차 모집을 2차 모집으로 수정하려고 하는 경우 예외 발생
    private void validateRoundOneToTwo(RoundType previousRoundType, RoundType newRoundType) {
        if (previousRoundType.isFirst() && newRoundType.isSecond()) {
            throw new CustomException(ROUND_ONE_DOES_NOT_EXIST);
        }
    }
}

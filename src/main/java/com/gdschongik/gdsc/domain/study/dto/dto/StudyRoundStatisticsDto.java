package com.gdschongik.gdsc.domain.study.dto.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record StudyRoundStatisticsDto(
        @Schema(description = "스터디 회차") Integer round,
        @Schema(description = "출석률") Long attendanceRate,
        @Schema(description = "과제 제출률") Long assignmentSubmissionRate) {

    public static StudyRoundStatisticsDto of(Integer round, Long attendanceRate, Long assignmentSubmissionRate) {
        return new StudyRoundStatisticsDto(round, attendanceRate, assignmentSubmissionRate);
    }
}

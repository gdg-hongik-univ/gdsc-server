package com.gdschongik.gdsc.domain.study.dto.response;

import com.gdschongik.gdsc.domain.study.dto.dto.StudyRoundStatisticsDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record StudyStatisticsResponse(
        @Schema(description = "스터디 전체 수강생 수") Long totalStudentCount,
        @Schema(description = "스터디 수료 수강생 수") Long completeStudentCount,
        @Schema(description = "평균 출석률") Long averageAttendanceRate,
        @Schema(description = "평균 과제 제출률") Long averageAssignmentSubmissionRate,
        @Schema(description = "스터디 수료율") Long studyCompleteRate,
        @Schema(description = "주차별 출석률 및 과제 제출률") List<StudyRoundStatisticsDto> studyRoundStatisticsDtos) {

    public static StudyStatisticsResponse of(
            Long totalStudentCount,
            Long completeStudentCount,
            Long averageAttendanceRate,
            Long averageAssignmentSubmissionRate,
            List<StudyRoundStatisticsDto> studyRoundStatisticsDtos) {
        return new StudyStatisticsResponse(
                totalStudentCount,
                completeStudentCount,
                averageAttendanceRate,
                averageAssignmentSubmissionRate,
                totalStudentCount == 0 ? 0 : Math.round(completeStudentCount / (double) totalStudentCount * 100),
                studyRoundStatisticsDtos);
    }
}

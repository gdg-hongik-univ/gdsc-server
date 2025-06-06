package com.gdschongik.gdsc.domain.study.dto.response;

import static com.gdschongik.gdsc.domain.study.domain.SubmissionFailureType.NOT_SUBMITTED;

import com.gdschongik.gdsc.domain.common.vo.Period;
import com.gdschongik.gdsc.domain.study.domain.AssignmentHistory;
import com.gdschongik.gdsc.domain.study.domain.Difficulty;
import com.gdschongik.gdsc.domain.study.domain.StudyDetail;
import com.gdschongik.gdsc.domain.study.domain.StudyStatus;
import com.gdschongik.gdsc.domain.study.domain.SubmissionFailureType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record StudyStudentCurriculumResponse(
        Long studyDetailId,
        @Schema(description = "기간") Period period,
        @Schema(description = "주차수") Long week,
        @Schema(description = "제목") String title,
        @Schema(description = "설명") String description,
        @Schema(description = "커리큘럼 상태") StudyStatus curriculumStatus,
        @Schema(description = "난이도") Difficulty difficulty,
        @Schema(description = "출석 상태") AttendanceStatusResponse attendanceStatus,
        @Schema(description = "과제 개설 상태") StudyStatus assignmentStatus,
        @Schema(description = "과제 제출 상태") AssignmentSubmissionStatusResponse assignmentSubmissionStatus,
        @Schema(description = "과제 실패 타입") SubmissionFailureType submissionFailureType,
        @Schema(description = "과제 제출 링크") String submissionLink) {

    public static StudyStudentCurriculumResponse of(
            StudyDetail studyDetail, AssignmentHistory assignmentHistory, boolean isAttended, LocalDateTime now) {
        return new StudyStudentCurriculumResponse(
                studyDetail.getId(),
                studyDetail.getPeriod(),
                studyDetail.getWeek(),
                studyDetail.getCurriculum().getTitle(),
                studyDetail.getCurriculum().getDescription(),
                studyDetail.getCurriculum().getStatus(),
                studyDetail.getCurriculum().getDifficulty(),
                AttendanceStatusResponse.of(studyDetail, now.toLocalDate(), isAttended),
                studyDetail.getAssignment().getStatus(),
                AssignmentSubmissionStatusResponse.of(assignmentHistory, studyDetail),
                assignmentHistory != null ? assignmentHistory.getSubmissionFailureType() : NOT_SUBMITTED,
                assignmentHistory != null ? assignmentHistory.getSubmissionLink() : null);
    }
}

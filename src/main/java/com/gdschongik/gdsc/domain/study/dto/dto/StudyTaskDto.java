package com.gdschongik.gdsc.domain.study.dto.dto;

import static com.gdschongik.gdsc.domain.study.dto.dto.StudyTaskDto.StudyTaskType.*;

import com.gdschongik.gdsc.domain.study.domain.AssignmentHistory;
import com.gdschongik.gdsc.domain.study.domain.AssignmentHistoryStatus;
import com.gdschongik.gdsc.domain.study.domain.AttendanceStatus;
import com.gdschongik.gdsc.domain.study.domain.StudySession;
import com.gdschongik.gdsc.domain.study.domain.StudyType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public record StudyTaskDto(
        Long studySessionId,
        @Schema(description = "현 회차수") Integer round,
        @Schema(description = "태스크 타입") StudyTaskType taskType,
        @Schema(description = "마감 시각") LocalDateTime endDate,
        @Schema(description = "출석 상태 (출석타입일 때만 사용)") AttendanceStatus attendanceStatus,
        @Schema(description = "과제 제목 (과제타입일 때만 사용)") String assignmentTitle,
        @Schema(description = "과제 제출 상태 (과제타입일 때만 사용)") AssignmentHistoryStatus assignmentSubmissionStatus) {

    /**
     * 출석 타입의 task를 위한 메서드입니다.
     */
    public static StudyTaskDto of(StudySession studySession, StudyType type, boolean isAttended, LocalDateTime now) {
        return new StudyTaskDto(
                studySession.getId(),
                studySession.getPosition(),
                ATTENDANCE,
                type == StudyType.ASSIGNMENT
                        ? null
                        : studySession.getLessonPeriod().getEndDate(),
                AttendanceStatus.of(studySession, type, isAttended, now),
                null,
                null);
    }

    /**
     * 과제 타입의 task를 위한 메서드입니다.
     */
    public static StudyTaskDto of(StudySession studySession, AssignmentHistory assignmentHistory, LocalDateTime now) {

        return new StudyTaskDto(
                studySession.getId(),
                studySession.getPosition(),
                ASSIGNMENT,
                studySession.getAssignmentPeriod().getEndDate(),
                null,
                studySession.getAssignmentTitle(),
                AssignmentHistoryStatus.of(assignmentHistory, studySession, now));
    }

    @Getter
    @RequiredArgsConstructor
    public enum StudyTaskType {
        ATTENDANCE("출석"),
        ASSIGNMENT("과제");

        private final String value;
    }
}

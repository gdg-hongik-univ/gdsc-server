package com.gdschongik.gdsc.domain.study.dto.response;

import static com.gdschongik.gdsc.domain.study.dto.response.StudyTodoResponse.StudyTodoType.*;

import com.gdschongik.gdsc.domain.study.domain.*;
import com.gdschongik.gdsc.domain.study.dto.dto.AssignmentHistoryDto;
import com.gdschongik.gdsc.domain.study.dto.dto.StudyHistorySimpleDto;
import com.gdschongik.gdsc.domain.study.dto.dto.StudySessionStudentDto;
import com.gdschongik.gdsc.domain.study.dto.dto.StudySimpleDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public record StudyTodoResponse(
        @Schema(description = "스터디 상세 정보") StudySimpleDto study,
        @Schema(description = "스터디 히스토리 정보") StudyHistorySimpleDto studyHistory,
        @Schema(description = "스터디 세션 정보") StudySessionStudentDto studySession,
        @Schema(description = "할 일 타입") StudyTodoResponse.StudyTodoType todoType,
        @Schema(description = "마감 시각") LocalDateTime deadLine,
        @Schema(description = "출석 상태 (출석타입일 때만 사용)") AttendanceStatus attendanceStatus,
        @Schema(description = "과제 정보 (과제타입일 때만 사용)") AssignmentHistoryDto assignmentHistory,
        @Schema(description = "과제 제출 상태 (과제타입일 때만 사용)") AssignmentHistoryStatus assignmentHistoryStatus) {
    public static StudyTodoResponse attendanceType(
            Study study, StudySession studySession, List<Attendance> attendances, LocalDateTime now) {
        return new StudyTodoResponse(
                StudySimpleDto.from(study),
                null,
                StudySessionStudentDto.of(studySession),
                ATTENDANCE,
                studySession.getLessonPeriod().getEndDate(),
                AttendanceStatus.of(studySession, study.getType(), isAttended(studySession, attendances), now),
                null,
                null);
    }

    public static StudyTodoResponse assignmentType(
            Study study,
            StudyHistory studyHistory,
            StudySession studySession,
            List<AssignmentHistory> assignmentHistories,
            LocalDateTime now) {
        AssignmentHistory assignmentHistory = getSubmittedAssignment(assignmentHistories, studySession);
        return new StudyTodoResponse(
                StudySimpleDto.from(study),
                StudyHistorySimpleDto.from(studyHistory),
                StudySessionStudentDto.of(studySession),
                ASSIGNMENT,
                studySession.getAssignmentPeriod().getEndDate(),
                null,
                assignmentHistory != null ? AssignmentHistoryDto.from(assignmentHistory) : null,
                AssignmentHistoryStatus.of(assignmentHistory, studySession, now));
    }

    private static boolean isAttended(StudySession studySession, List<Attendance> attendances) {
        return attendances.stream()
                .anyMatch(attendance -> attendance.getStudySession().getId().equals(studySession.getId()));
    }

    private static AssignmentHistory getSubmittedAssignment(
            List<AssignmentHistory> assignmentHistories, StudySession studySession) {
        return assignmentHistories.stream()
                .filter(assignmentHistory ->
                        assignmentHistory.getStudySession().getId().equals(studySession.getId()))
                .findFirst()
                .orElse(null);
    }

    @Getter
    @RequiredArgsConstructor
    public enum StudyTodoType {
        ATTENDANCE("출석"),
        ASSIGNMENT("과제");

        private final String value;
    }
}

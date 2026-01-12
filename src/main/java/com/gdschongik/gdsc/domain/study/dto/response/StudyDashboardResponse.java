package com.gdschongik.gdsc.domain.study.dto.response;

import com.gdschongik.gdsc.domain.study.domain.*;
import com.gdschongik.gdsc.domain.study.dto.dto.StudyHistoryDto;
import com.gdschongik.gdsc.domain.study.dto.dto.StudySessionMyDto;
import java.time.LocalDateTime;
import java.util.List;

public record StudyDashboardResponse(StudyHistoryDto studyHistory, List<StudySessionMyDto> sessions) {
    public static StudyDashboardResponse of(
            Study study,
            StudyHistory studyHistory,
            List<Attendance> attendances,
            List<AssignmentHistory> assignmentHistories,
            LocalDateTime now) {
        List<StudySessionMyDto> studySessions = study.getStudySessions().stream()
                .map(studySession -> StudySessionMyDto.of(
                        studySession,
                        getAssignmentHistory(studySession, assignmentHistories),
                        study.getType(),
                        isAttended(studySession, attendances),
                        now))
                .toList();

        return new StudyDashboardResponse(StudyHistoryDto.from(studyHistory), studySessions);
    }

    private static boolean isAttended(StudySession studySession, List<Attendance> attendances) {
        return attendances.stream()
                .anyMatch(attendance -> attendance.getStudySession().getId().equals(studySession.getId()));
    }

    private static AssignmentHistory getAssignmentHistory(
            StudySession studySession, List<AssignmentHistory> assignmentHistories) {
        return assignmentHistories.stream()
                .filter(assignmentHistory -> isEquals(studySession, assignmentHistory))
                .filter(assignmentHistory ->
                        isCommittedAtWithinAssignmentPeriodIfExist(assignmentHistory, studySession))
                .findFirst()
                .orElse(null);
    }

    private static boolean isEquals(StudySession studySession, AssignmentHistory assignmentHistory) {
        return assignmentHistory.getStudySession().getId().equals(studySession.getId());
    }

    /**
     * 과제 제출 이력이 있는 경우, 제출 시간이 과제 제출 기간 내에 있는지 확인합니다.
     * 과제 제출 이력이 없는 경우, 항상 true를 반환합니다.
     *
     * @see AssignmentHistoryStatus
     */
    private static boolean isCommittedAtWithinAssignmentPeriodIfExist(
            AssignmentHistory assignmentHistory, StudySession studySession) {
        if (assignmentHistory == null) {
            return true;
        }

        LocalDateTime committedAt = assignmentHistory.getCommittedAt();
        if (committedAt == null) {
            return true;
        }

        return studySession.getAssignmentPeriod().isWithin(committedAt);
    }
}

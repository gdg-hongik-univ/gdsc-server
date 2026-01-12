package com.gdschongik.gdsc.domain.study.dto.dto;

import com.gdschongik.gdsc.domain.study.domain.AssignmentHistory;
import com.gdschongik.gdsc.domain.study.domain.AssignmentHistoryStatus;
import com.gdschongik.gdsc.domain.study.domain.AttendanceStatus;
import com.gdschongik.gdsc.domain.study.domain.StudySession;
import com.gdschongik.gdsc.domain.study.domain.StudyType;
import jakarta.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.Optional;

public record StudySessionMyDto(
        StudySessionStudentDto session,
        AttendanceStatus attendanceStatus,
        AssignmentHistoryStatus assignmentHistoryStatus,
        AssignmentHistoryDto assignmentHistory) {
    public static StudySessionMyDto of(
            StudySession studySession,
            @Nullable AssignmentHistory assignmentHistory,
            StudyType studyType,
            boolean isAttended,
            LocalDateTime now) {
        return new StudySessionMyDto(
                StudySessionStudentDto.of(studySession),
                AttendanceStatus.of(studySession, studyType, isAttended, now),
                AssignmentHistoryStatus.of(assignmentHistory, studySession, now),
                Optional.ofNullable(assignmentHistory)
                        .map(AssignmentHistoryDto::from)
                        .orElse(null));
    }
}

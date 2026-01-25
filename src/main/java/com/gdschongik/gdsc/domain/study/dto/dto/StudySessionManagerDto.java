package com.gdschongik.gdsc.domain.study.dto.dto;

import com.gdschongik.gdsc.domain.common.vo.Period;
import com.gdschongik.gdsc.domain.study.domain.StudySession;

/**
 * 스터디 회차 관리자 DTO입니다. 출결번호가 포함되어 있습니다.
 */
public record StudySessionManagerDto(
        Long studySessionId,
        Integer position,
        String lessonTitle,
        String description,
        String lessonAttendanceNumber,
        Period lessonPeriod,
        String assignmentTitle,
        String assignmentDescriptionLink,
        Period assignmentPeriod,
        Long studyId) {
    public static StudySessionManagerDto from(StudySession studySession) {
        return new StudySessionManagerDto(
                studySession.getId(),
                studySession.getPosition(),
                studySession.getLessonTitle(),
                studySession.getDescription(),
                studySession.getLessonAttendanceNumber(),
                studySession.getLessonPeriod(),
                studySession.getAssignmentTitle(),
                studySession.getAssignmentDescriptionLink(),
                studySession.getAssignmentPeriod(),
                studySession.getStudy().getId());
    }
}

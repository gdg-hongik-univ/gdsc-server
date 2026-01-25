package com.gdschongik.gdsc.domain.study.dto.dto;

import com.gdschongik.gdsc.domain.common.vo.Period;
import com.gdschongik.gdsc.domain.study.domain.StudySession;

/**
 * 스터디 회차 학생 DTO입니다. 출결번호가 포함되어 있지 않습니다.
 */
public record StudySessionStudentDto(
        Long studySessionId,
        Integer position,
        String lessonTitle,
        String description,
        Period lessonPeriod,
        String assignmentTitle,
        String assignmentDescriptionLink,
        Period assignmentPeriod,
        Long studyId) {
    public static StudySessionStudentDto of(StudySession studySession) {
        return new StudySessionStudentDto(
                studySession.getId(),
                studySession.getPosition(),
                studySession.getLessonTitle(),
                studySession.getDescription(),
                studySession.getLessonPeriod(),
                studySession.getAssignmentTitle(),
                studySession.getAssignmentDescriptionLink(),
                studySession.getAssignmentPeriod(),
                studySession.getStudy().getId());
    }
}

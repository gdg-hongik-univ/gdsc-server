package com.gdschongik.gdsc.domain.study.dto.dto;

import com.gdschongik.gdsc.domain.common.vo.Semester;
import com.gdschongik.gdsc.domain.study.domain.Study;
import com.gdschongik.gdsc.domain.study.domain.StudyType;

public record StudySimpleDto(
        Long studyId, String studyName, StudyType studyType, Semester semester, Long mentorId, String mentorName) {
    public static StudySimpleDto from(Study study) {
        return new StudySimpleDto(
                study.getId(),
                study.getTitle(),
                study.getType(),
                study.getSemester(),
                study.getMentor().getId(),
                study.getMentor().getName());
    }
}

package com.gdschongik.gdsc.domain.study.dto.dto;

import com.gdschongik.gdsc.domain.common.vo.Period;
import com.gdschongik.gdsc.domain.common.vo.Semester;
import com.gdschongik.gdsc.domain.study.domain.Study;
import com.gdschongik.gdsc.domain.study.domain.StudyType;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 스터디 학생 DTO입니다. 디스코드 관련 ID가 포함되어 있지 않습니다.
 */
public record StudyCommonDto(
        Long studyId,
        StudyType type,
        String title,
        String description,
        String descriptionNotionLink,
        Semester semester,
        Integer totalRound,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        Period applicationPeriod,
        LocalDateTime openingDate,
        Long mentorId,
        String mentorName,
        Integer minAssignmentLength) {
    public static StudyCommonDto from(Study study) {
        return new StudyCommonDto(
                study.getId(),
                study.getType(),
                study.getTitle(),
                study.getDescription(),
                study.getDescriptionNotionLink(),
                study.getSemester(),
                study.getTotalRound(),
                study.getDayOfWeek(),
                study.getStartTime(),
                study.getEndTime(),
                study.getApplicationPeriod(),
                study.getOpeningDate(),
                study.getMentor().getId(),
                study.getMentor().getName(),
                study.getMinAssignmentLength());
    }
}

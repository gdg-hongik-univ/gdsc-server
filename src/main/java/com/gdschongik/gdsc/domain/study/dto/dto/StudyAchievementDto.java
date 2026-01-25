package com.gdschongik.gdsc.domain.study.dto.dto;

import com.gdschongik.gdsc.domain.study.domain.AchievementType;
import com.gdschongik.gdsc.domain.study.domain.StudyAchievement;

public record StudyAchievementDto(Long studyAchievementId, AchievementType type, Long studentId, Long studyId) {
    public static StudyAchievementDto from(StudyAchievement studyAchievement) {
        return new StudyAchievementDto(
                studyAchievement.getId(),
                studyAchievement.getType(),
                studyAchievement.getStudent().getId(),
                studyAchievement.getStudy().getId());
    }
}

package com.gdschongik.gdsc.domain.study.dao;

import com.gdschongik.gdsc.domain.study.domain.AchievementType;
import com.gdschongik.gdsc.domain.study.domain.StudyAchievement;
import java.util.List;

public interface StudyAchievementCustomRepository {

    List<StudyAchievement> findByStudyIdAndMemberIds(Long studyId, List<Long> memberIds);

    void deleteByStudyAndAchievementTypeAndMemberIds(
            Long studyId, AchievementType achievementType, List<Long> memberIds);

    long countByStudyIdAndAchievementTypeAndStudentIds(
            Long studyId, AchievementType achievementType, List<Long> studentIds);
}

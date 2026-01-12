package com.gdschongik.gdsc.domain.study.dao;

import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.study.domain.StudyAchievement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyAchievementRepository
        extends JpaRepository<StudyAchievement, Long>, StudyAchievementCustomRepository {
    List<StudyAchievement> findAllByStudent(Member student);
}

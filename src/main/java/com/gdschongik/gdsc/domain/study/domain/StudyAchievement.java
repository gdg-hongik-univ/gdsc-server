package com.gdschongik.gdsc.domain.study.domain;

import com.gdschongik.gdsc.domain.common.model.BaseEntity;
import com.gdschongik.gdsc.domain.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "study_achievement_v2",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"member_id", "study_v2_id", "type"})})
public class StudyAchievement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_achievement_v2_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private AchievementType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_v2_id")
    private Study study;

    @Builder(access = AccessLevel.PRIVATE)
    private StudyAchievement(AchievementType type, Member student, Study study) {
        this.type = type;
        this.student = student;
        this.study = study;
    }

    public static StudyAchievement create(AchievementType type, Member student, Study study) {
        return StudyAchievement.builder()
                .type(type)
                .student(student)
                .study(study)
                .build();
    }
}

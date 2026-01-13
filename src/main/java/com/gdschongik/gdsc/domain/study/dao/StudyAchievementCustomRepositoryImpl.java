package com.gdschongik.gdsc.domain.study.dao;

import static com.gdschongik.gdsc.domain.study.domain.QStudyAchievement.*;

import com.gdschongik.gdsc.domain.study.domain.AchievementType;
import com.gdschongik.gdsc.domain.study.domain.StudyAchievement;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StudyAchievementCustomRepositoryImpl implements StudyAchievementCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<StudyAchievement> findByStudyIdAndMemberIds(Long studyId, List<Long> memberIds) {
        return queryFactory
                .selectFrom(studyAchievement)
                .where(eqStudyId(studyId), containsStudentId(memberIds))
                .fetch();
    }

    @Override
    public void deleteByStudyAndAchievementTypeAndMemberIds(
            Long studyId, AchievementType achievementType, List<Long> memberIds) {
        queryFactory
                .delete(studyAchievement)
                .where(eqStudyId(studyId), eqAchievementType(achievementType), containsStudentId(memberIds))
                .execute();
    }

    @Override
    public long countByStudyIdAndAchievementTypeAndStudentIds(
            Long studyId, AchievementType achievementType, List<Long> studentIds) {
        return Objects.requireNonNull(queryFactory
                .select(studyAchievement.count())
                .from(studyAchievement)
                .where(eqStudyId(studyId), eqAchievementType(achievementType), containsStudentId(studentIds))
                .fetchOne());
    }

    private BooleanExpression eqStudyId(Long studyId) {
        return studyId != null ? studyAchievement.study.id.eq(studyId) : null;
    }

    private BooleanExpression eqAchievementType(AchievementType achievementType) {
        return achievementType != null ? studyAchievement.type.eq(achievementType) : null;
    }

    private BooleanExpression containsStudentId(List<Long> memberIds) {
        return memberIds != null ? studyAchievement.student.id.in(memberIds) : null;
    }
}

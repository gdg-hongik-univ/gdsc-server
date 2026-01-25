package com.gdschongik.gdsc.domain.study.dao;

import static com.gdschongik.gdsc.domain.study.domain.QAssignmentHistory.*;
import static com.gdschongik.gdsc.domain.study.domain.QStudySession.*;

import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.study.domain.AssignmentHistory;
import com.gdschongik.gdsc.domain.study.domain.Study;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AssignmentHistoryCustomRepositoryImpl implements AssignmentHistoryCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<AssignmentHistory> findByMemberAndStudy(Member member, Study study) {
        return queryFactory
                .selectFrom(assignmentHistory)
                .innerJoin(assignmentHistory.studySession)
                .where(eqMemberId(member.getId()).and(eqStudyId(study.getId())))
                .fetch();
    }

    @Override
    public List<AssignmentHistory> findByStudyIdAndMemberIds(Long studyId, List<Long> memberIds) {
        return queryFactory
                .selectFrom(assignmentHistory)
                .innerJoin(assignmentHistory.studySession, studySession)
                .fetchJoin()
                .where(assignmentHistory.member.id.in(memberIds), eqStudyId(studyId))
                .fetch();
    }

    @Override
    public void deleteByStudyIdAndMemberId(Long studyId, Long memberId) {
        queryFactory
                .delete(assignmentHistory)
                .where(eqMemberId(memberId).and(eqStudyId(studyId)))
                .execute();
    }

    private BooleanExpression eqMemberId(Long memberId) {
        return memberId != null ? assignmentHistory.member.id.eq(memberId) : null;
    }

    private BooleanExpression eqStudyId(Long studyId) {
        return studyId != null ? assignmentHistory.studySession.study.id.eq(studyId) : null;
    }
}

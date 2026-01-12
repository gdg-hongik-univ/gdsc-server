package com.gdschongik.gdsc.domain.study.dao;

import static com.gdschongik.gdsc.domain.study.domain.QAttendance.*;
import static com.gdschongik.gdsc.domain.study.domain.QStudySession.*;

import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.study.domain.Attendance;
import com.gdschongik.gdsc.domain.study.domain.Study;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AttendanceCustomRepositoryImpl implements AttendanceCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Attendance> findFetchByMemberAndStudy(Member member, Study study) {
        return queryFactory
                .selectFrom(attendance)
                .innerJoin(attendance.studySession)
                .fetchJoin()
                .where(eqMemberId(member.getId()), eqStudyId(study.getId()))
                .fetch();
    }

    @Override
    public List<Attendance> findByStudyIdAndMemberIds(Long studyId, List<Long> memberIds) {
        return queryFactory
                .selectFrom(attendance)
                .innerJoin(attendance.studySession, studySession)
                .fetchJoin()
                .where(attendance.student.id.in(memberIds), eqStudyId(studyId))
                .fetch();
    }

    @Override
    public void deleteByStudyIdAndMemberId(Long studyId, Long memberId) {
        queryFactory
                .delete(attendance)
                .where(eqStudyId(studyId), eqMemberId(memberId))
                .execute();
    }

    private BooleanExpression eqMemberId(Long memberId) {
        return memberId != null ? attendance.student.id.eq(memberId) : null;
    }

    private BooleanExpression eqStudyId(Long studyId) {
        return studyId != null ? attendance.studySession.study.id.eq(studyId) : null;
    }
}

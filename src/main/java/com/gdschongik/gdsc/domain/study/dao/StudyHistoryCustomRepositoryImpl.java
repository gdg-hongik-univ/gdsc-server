package com.gdschongik.gdsc.domain.study.dao;

import static com.gdschongik.gdsc.domain.coupon.domain.QIssuedCoupon.*;
import static com.gdschongik.gdsc.domain.member.domain.QMember.*;
import static com.gdschongik.gdsc.domain.study.domain.QStudyHistory.*;

import com.gdschongik.gdsc.domain.study.domain.StudyHistory;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class StudyHistoryCustomRepositoryImpl implements StudyHistoryCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public long countByStudyIdAndStudentIds(Long studyId, List<Long> studentIds) {
        return Objects.requireNonNull(queryFactory
                .select(studyHistory.count())
                .from(studyHistory)
                .where(eqStudyId(studyId), studyHistory.student.id.in(studentIds))
                .fetchOne());
    }

    @Override
    public List<StudyHistory> findAllByStudyIdAndStudentIds(Long studyId, List<Long> studentIds) {
        return queryFactory
                .selectFrom(studyHistory)
                .where(eqStudyId(studyId), studyHistory.student.id.in(studentIds))
                .fetch();
    }

    @Override
    public Page<StudyHistory> findByStudyId(Long studyId, Pageable pageable) {
        List<StudyHistory> fetch = queryFactory
                .selectFrom(studyHistory)
                .innerJoin(studyHistory.student, member)
                .where(eqStudyId(studyId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(studyHistory.count())
                .from(studyHistory)
                .innerJoin(studyHistory.student, member)
                .where(eqStudyId(studyId));

        return PageableExecutionUtils.getPage(fetch, pageable, countQuery::fetchOne);
    }

    private BooleanExpression eqStudyId(Long studyId) {
        return studyHistory.study.id.eq(studyId);
    }
}

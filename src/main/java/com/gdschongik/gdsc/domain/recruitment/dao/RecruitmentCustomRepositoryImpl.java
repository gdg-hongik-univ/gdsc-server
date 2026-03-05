package com.gdschongik.gdsc.domain.recruitment.dao;

import static com.gdschongik.gdsc.domain.recruitment.domain.QRecruitment.recruitment;

import com.gdschongik.gdsc.domain.recruitment.domain.Recruitment;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RecruitmentCustomRepositoryImpl implements RecruitmentCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Recruitment> findCurrentRecruitment(LocalDateTime now) {
        return Optional.ofNullable(queryFactory
                .selectFrom(recruitment)
                .where(isWithinSemesterPeriod(now))
                .fetchOne());
    }

    private BooleanExpression isWithinSemesterPeriod(LocalDateTime date) {
        return recruitment.semesterPeriod.startDate.loe(date).and(recruitment.semesterPeriod.endDate.goe(date));
    }

    @Override
    public boolean existsPeriodOverlapping(LocalDateTime startDate, LocalDateTime endDate) {
        Integer fetchOne = queryFactory
                .selectOne()
                .from(recruitment)
                .where(isOverlappingPeriod(startDate, endDate))
                .fetchFirst();

        return fetchOne != null;
    }

    private BooleanExpression isOverlappingPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return recruitment.semesterPeriod.startDate.loe(endDate).and(recruitment.semesterPeriod.endDate.gt(startDate));
    }
}

package com.gdschongik.gdsc.domain.study.dao;

import static com.gdschongik.gdsc.domain.study.domain.QStudy.*;

import com.gdschongik.gdsc.domain.study.domain.Study;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StudyRepositoryImpl implements StudyCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Study> findFetchById(Long id) {
        return Optional.ofNullable(queryFactory
                .selectFrom(study)
                .join(study.studySessions)
                .fetchJoin()
                .where(study.id.eq(id))
                .fetchOne());
    }

    @Override
    public Optional<Study> findFetchBySessionId(Long sessionId) {
        return Optional.ofNullable(queryFactory
                .selectFrom(study)
                .join(study.studySessions)
                .fetchJoin()
                .where(study.studySessions.any().id.eq(sessionId))
                .fetchOne());
    }

    @Override
    public List<Study> findFetchAll() {
        return queryFactory
                .selectFrom(study)
                .join(study.studySessions)
                .fetchJoin()
                .join(study.mentor)
                .fetchJoin()
                .fetch();
    }
}

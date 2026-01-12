package com.gdschongik.gdsc.domain.study.dao;

import static com.gdschongik.gdsc.domain.study.domain.QStudyAnnouncement.*;

import com.gdschongik.gdsc.domain.study.domain.StudyAnnouncement;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StudyAnnouncementCustomRepositoryImpl implements StudyAnnouncementCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<StudyAnnouncement> findAllByStudyIdsOrderByCreatedAtDesc(List<Long> studyIds) {
        return queryFactory
                .selectFrom(studyAnnouncement)
                .where(studyAnnouncement.study.id.in(studyIds))
                .orderBy(studyAnnouncement.createdAt.desc())
                .fetch();
    }
}

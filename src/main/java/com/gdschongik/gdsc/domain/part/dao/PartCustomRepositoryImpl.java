package com.gdschongik.gdsc.domain.part.dao;

import static com.gdschongik.gdsc.domain.part.domain.QPart.part;

import com.gdschongik.gdsc.domain.part.dto.response.PartSemesterResponse;
import com.gdschongik.gdsc.domain.part.dto.response.QPartSemesterResponse;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PartCustomRepositoryImpl implements PartCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<PartSemesterResponse> findAllPartSemesters() {
        return queryFactory
                .selectDistinct(new QPartSemesterResponse(part.semester))
                .from(part)
                .orderBy(part.semester.academicYear.desc(), part.semester.semesterType.desc())
                .fetch();
    }
}

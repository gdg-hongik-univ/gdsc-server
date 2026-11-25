package com.gdschongik.gdsc.domain.part.dto.response;

import com.gdschongik.gdsc.domain.common.model.SemesterType;
import com.gdschongik.gdsc.domain.common.vo.Semester;
import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;

public record PartSemesterResponse(
        @Schema(description = "학년도") Integer academicYear, @Schema(description = "학기") SemesterType semesterType) {

    @QueryProjection
    public PartSemesterResponse(Semester semester) {
        this(
                semester == null ? null : semester.getAcademicYear(),
                semester == null ? null : semester.getSemesterType());
    }
}

package com.gdschongik.gdsc.global.common.constant;

import com.gdschongik.gdsc.domain.common.model.SemesterType;
import com.gdschongik.gdsc.domain.common.vo.Semester;
import com.gdschongik.gdsc.domain.part.domain.PartType;
import java.util.List;

public class TestPartConstant {

    private TestPartConstant() {}

    public static final Semester PART_SEMESTER = Semester.of(2026, SemesterType.SECOND);
    public static final PartType PART_TYPE = PartType.BACKEND;
    public static final String PART_THUMBNAIL_URL = "https://example.com/part-thumbnail.png";
    public static final String PART_LEAD_NAME = "김홍익";
    public static final List<String> PART_INTRODUCTIONS = List.of("Spring 기반 백엔드 개발을 학습합니다.");
    public static final List<String> PART_RECOMMENDATIONS = List.of("백엔드 개발에 관심 있는 분께 추천합니다.");
    public static final String PART_SESSION_TITLE = "Spring 기초";
    public static final String PART_SESSION_DESCRIPTION = "Spring의 핵심 개념을 학습합니다.";
    public static final boolean PART_SESSION_IS_HIGHLIGHTED = true;
}

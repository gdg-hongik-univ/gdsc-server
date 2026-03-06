package com.gdschongik.gdsc.global.common.constant;

import com.gdschongik.gdsc.domain.common.model.SemesterType;
import com.gdschongik.gdsc.domain.common.vo.Period;
import com.gdschongik.gdsc.domain.common.vo.Semester;
import com.gdschongik.gdsc.domain.study.domain.StudyType;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class StudyConstant {
    private StudyConstant() {}

    public static final String STUDY_TITLE = "스터디 제목";
    public static final String STUDY_DESCRIPTION = "스터디 설명";
    public static final String STUDY_DESCRIPTION_NOTION_LINK = "https://gdghongik.com/2025-1-backend-study";
    public static final Semester STUDY_SEMESTER = Semester.of(2025, SemesterType.FIRST);
    public static final Long TOTAL_WEEK = 8L;
    public static final int TOTAL_ROUND = 8;
    public static final StudyType ONLINE_STUDY = StudyType.ONLINE;
    public static final StudyType ASSIGNMENT_STUDY = StudyType.ASSIGNMENT;
    public static final DayOfWeek DAY_OF_WEEK = DayOfWeek.FRIDAY;
    public static final LocalTime STUDY_START_TIME = LocalTime.of(19, 0, 0);
    public static final LocalTime STUDY_END_TIME = LocalTime.of(20, 0, 0);
    public static final Period STUDY_APPLICATION_PERIOD =
            Period.of(LocalDateTime.of(2025, 3, 1, 0, 0), LocalDateTime.of(2025, 3, 14, 23, 59));
    public static final String STUDY_DISCORD_CHANNEL_ID = "12345678";
    public static final String STUDY_DISCORD_ROLE_ID = "12345678";

    // StudyDetail
    public static final String ATTENDANCE_NUMBER = "1234";

    // Assignment
    public static final int MIN_ASSIGNMENT_CONTENT_LENGTH = 200;
    public static final String ASSIGNMENT_TITLE = "testTitle";
    public static final String DESCRIPTION_LINK = "www.link.com";

    // Study (2024-09-01 ~ 2024-10-27)
    public static final LocalDateTime STUDY_START_DATETIME = LocalDateTime.of(2024, 9, 1, 0, 0);
    public static final LocalDateTime STUDY_END_DATETIME = STUDY_START_DATETIME.plusWeeks(8);
    public static final Period STUDY_ONGOING_PERIOD = Period.of(STUDY_START_DATETIME, STUDY_END_DATETIME);
    public static final String STUDY_NOTION_LINK = "notionLink";
    public static final String STUDY_INTRODUCTION = "introduction";

    // StudyDetail (1주차: 2024-09-01 ~ 2024-09-08)
    public static final LocalDateTime STUDY_DETAIL_START_DATETIME = STUDY_START_DATETIME;
    public static final LocalDateTime STUDY_DETAIL_END_DATETIME = STUDY_DETAIL_START_DATETIME.plusWeeks(1);
    public static final LocalDateTime STUDY_ASSIGNMENT_DEADLINE_DATETIME = STUDY_DETAIL_END_DATETIME;

    // Curriculum
    public static final String CURRICULUM_TITLE = "curriculumTitle";
    public static final String CURRICULUM_DESCRIPTION = "curriculumDescription";

    // AssignmentHistory
    public static final String SUBMISSION_LINK = "https://github.com/ownername/reponame/blob/main/week1/wil.md";
    public static final String COMMIT_HASH = "aa11bb22cc33";
    public static final Integer CONTENT_LENGTH = 2000;
    public static final LocalDateTime COMMITTED_AT = LocalDateTime.of(2024, 9, 8, 0, 0);

    // StudyHistory
    public static final String REPOSITORY_LINK = "ownername/reponame";
}

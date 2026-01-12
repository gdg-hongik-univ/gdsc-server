package com.gdschongik.gdsc.domain.study.domain;

import static com.gdschongik.gdsc.global.common.constant.StudyConstant.*;
import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.helper.FixtureHelper;
import org.junit.jupiter.api.Test;

class StudyFactoryTest {

    FixtureHelper fixtureHelper = new FixtureHelper();
    StudyFactory studyFactory = new StudyFactory();

    static class FixedAttendanceNumberGenerator implements AttendanceNumberGenerator {
        @Override
        public String generate() {
            return "0000";
        }
    }

    @Test
    void 스터디_생성시_설정한_총_회차만큼_스터디회차가_생성된다() {
        // given
        Member mentor = fixtureHelper.createMentor(1L);
        AttendanceNumberGenerator generator = new FixedAttendanceNumberGenerator();
        int totalRound = 8;

        // when
        Study study = studyFactory.create(
                StudyType.OFFLINE,
                STUDY_TITLE,
                STUDY_SEMESTER,
                totalRound,
                DAY_OF_WEEK,
                STUDY_START_TIME,
                STUDY_END_TIME,
                STUDY_APPLICATION_PERIOD,
                STUDY_DISCORD_CHANNEL_ID,
                STUDY_DISCORD_ROLE_ID,
                mentor,
                generator,
                MIN_ASSIGNMENT_CONTENT_LENGTH);

        // then
        assertThat(study.getStudySessions()).hasSize(8);
    }

    @Test
    void 과제_스터디_생성시_라이브_세션_관련_필드가_제외된다() {
        // given
        Member mentor = fixtureHelper.createMentor(1L);

        // when
        Study study = studyFactory.create(
                StudyType.ASSIGNMENT,
                STUDY_TITLE,
                STUDY_SEMESTER,
                TOTAL_ROUND,
                DAY_OF_WEEK,
                STUDY_START_TIME,
                STUDY_END_TIME,
                STUDY_APPLICATION_PERIOD,
                STUDY_DISCORD_CHANNEL_ID,
                STUDY_DISCORD_ROLE_ID,
                mentor,
                null,
                MIN_ASSIGNMENT_CONTENT_LENGTH);

        // then
        assertThat(study.getDayOfWeek()).isNull();
        assertThat(study.getStartTime()).isNull();
        assertThat(study.getEndTime()).isNull();
    }

    @Test
    void 스터디_생성시_스터디회차는_순서대로_position이_지정되어_생성된다() {
        // given
        Member mentor = fixtureHelper.createMentor(1L);
        AttendanceNumberGenerator generator = new FixedAttendanceNumberGenerator();
        int totalRound = 8;

        // when
        Study study = studyFactory.create(
                StudyType.OFFLINE,
                STUDY_TITLE,
                STUDY_SEMESTER,
                totalRound,
                DAY_OF_WEEK,
                STUDY_START_TIME,
                STUDY_END_TIME,
                STUDY_APPLICATION_PERIOD,
                STUDY_DISCORD_CHANNEL_ID,
                STUDY_DISCORD_ROLE_ID,
                mentor,
                generator,
                MIN_ASSIGNMENT_CONTENT_LENGTH);

        // then
        assertThat(study.getStudySessions())
                .extracting(StudySession::getPosition)
                .containsExactly(1, 2, 3, 4, 5, 6, 7, 8);
    }

    @Test
    void 라이브_스터디_생성시_각_스터디회차에_출석번호가_생성된다() {
        // given
        Member mentor = fixtureHelper.createMentor(1L);
        AttendanceNumberGenerator generator = new FixedAttendanceNumberGenerator();

        // when
        Study study = studyFactory.create(
                StudyType.OFFLINE,
                STUDY_TITLE,
                STUDY_SEMESTER,
                TOTAL_ROUND,
                DAY_OF_WEEK,
                STUDY_START_TIME,
                STUDY_END_TIME,
                STUDY_APPLICATION_PERIOD,
                STUDY_DISCORD_CHANNEL_ID,
                STUDY_DISCORD_ROLE_ID,
                mentor,
                generator,
                MIN_ASSIGNMENT_CONTENT_LENGTH);

        // then
        assertThat(study.getStudySessions())
                .extracting(StudySession::getLessonAttendanceNumber)
                .containsOnly("0000");
    }

    @Test
    void 과제_스터디_생성시_스터디회차에_출석번호가_생성되지_않는다() {
        // given
        Member mentor = fixtureHelper.createMentor(1L);

        // when
        Study study = studyFactory.create(
                StudyType.ASSIGNMENT,
                STUDY_TITLE,
                STUDY_SEMESTER,
                TOTAL_ROUND,
                DAY_OF_WEEK,
                STUDY_START_TIME,
                STUDY_END_TIME,
                STUDY_APPLICATION_PERIOD,
                STUDY_DISCORD_CHANNEL_ID,
                STUDY_DISCORD_ROLE_ID,
                mentor,
                null,
                MIN_ASSIGNMENT_CONTENT_LENGTH);

        // then
        assertThat(study.getStudySessions())
                .extracting(StudySession::getLessonAttendanceNumber)
                .containsOnly((String) null);
    }
}

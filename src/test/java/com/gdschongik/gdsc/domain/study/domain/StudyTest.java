package com.gdschongik.gdsc.domain.study.domain;

import static com.gdschongik.gdsc.global.common.constant.StudyConstant.MIN_ASSIGNMENT_CONTENT_LENGTH;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.domain.common.vo.Period;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.helper.FixtureHelper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class StudyTest {

    FixtureHelper fixtureHelper = new FixtureHelper();

    private Study createStudy(StudyType type, Long studyId, Long firstStudySessionId, Long mentorId) {
        return fixtureHelper.createStudy(type, studyId, firstStudySessionId, mentorId);
    }

    @Nested
    class 스터디_수정할때 {

        @Test
        void 성공한다() {
            // given
            Study study = createStudy(StudyType.OFFLINE, 1L, 1L, 1L);

            String updatedTitle = "수정된 제목";
            String updatedFirstSessionTitle = "수정된 1회차 스터디 제목";

            StudyUpdateCommand command = new StudyUpdateCommand(
                    updatedTitle,
                    null,
                    null,
                    null,
                    null,
                    null,
                    MIN_ASSIGNMENT_CONTENT_LENGTH,
                    List.of(new StudyUpdateCommand.Session(
                            1L, updatedFirstSessionTitle, null, null, null, null, null)));

            // when
            study.update(command);

            // then
            assertThat(study.getTitle()).isEqualTo(updatedTitle);

            StudySession firstSession = study.getStudySessions().get(0);
            assertThat(firstSession).isNotNull();
            assertThat(firstSession.getLessonTitle()).isEqualTo(updatedFirstSessionTitle);
        }

        @Test
        void 과제_스터디_스터디회차의_수업기간이_null이_아니면_실패한다() {
            // given
            Study study = createStudy(StudyType.ASSIGNMENT, 1L, 1L, 1L);

            Period lessonPeriodToUpdate =
                    Period.of(LocalDateTime.of(2025, 1, 1, 0, 0), LocalDateTime.of(2025, 1, 8, 0, 0));
            StudyUpdateCommand command = new StudyUpdateCommand(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    MIN_ASSIGNMENT_CONTENT_LENGTH,
                    List.of(new StudyUpdateCommand.Session(1L, null, null, lessonPeriodToUpdate, null, null, null)));

            // when & then
            assertThatThrownBy(() -> study.update(command))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(STUDY_NOT_UPDATABLE_LESSON_FIELD_NOT_NULL.getMessage());
        }

        @Test
        void 존재하지_않는_스터디회차_ID를_전달하면_실패한다() {
            // given
            Study study = createStudy(StudyType.OFFLINE, 1L, 1L, 1L);

            StudyUpdateCommand command = new StudyUpdateCommand(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    MIN_ASSIGNMENT_CONTENT_LENGTH,
                    List.of(new StudyUpdateCommand.Session(9999L, null, null, null, null, null, null)));

            // when & then
            assertThatThrownBy(() -> study.update(command))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(STUDY_NOT_UPDATABLE_SESSION_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 스터디_수정시_스터디회차의_수업_시작일시_순차성을_검증할때 {

        private StudyUpdateCommand.Session createSession(Long sessionId, LocalDate date) {
            // 수업 시간을 null로 지정할 수 잇음
            if (date == null) {
                return new StudyUpdateCommand.Session(sessionId, null, null, null, null, null, null);
            }

            // 아니라면, 18:00 ~ 20:00 고정
            LocalDateTime startDateTime = date.atTime(18, 0);
            return new StudyUpdateCommand.Session(
                    sessionId, null, null, Period.of(startDateTime, startDateTime.plusHours(2)), null, null, null);
        }

        private StudyUpdateCommand createCommand(LocalDate... dates) {
            List<StudyUpdateCommand.Session> sessions = IntStream.range(0, dates.length)
                    .mapToObj(i -> createSession((long) (i + 1), dates[i]))
                    .toList();
            return new StudyUpdateCommand(null, null, null, null, null, null, MIN_ASSIGNMENT_CONTENT_LENGTH, sessions);
        }

        @Test
        void 역순인_경우_실패한다() {
            // given
            LocalDate date = LocalDate.of(2025, 1, 1);
            Study study = createStudy(StudyType.OFFLINE, 1L, 1L, 1L);

            // 1회차: 1/3, 2회차: 1/2
            LocalDate firstLessonDate = date.plusDays(2);
            LocalDate secondLessonDate = date.plusDays(1);

            StudyUpdateCommand command = createCommand(firstLessonDate, secondLessonDate);

            // when & then
            assertThatThrownBy(() -> study.update(command))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(STUDY_NOT_UPDATABLE_LESSON_PERIOD_NOT_SEQUENTIAL.getMessage());
        }

        @Test
        void 같은_시작_시간인_경우_실패한다() {
            // given
            LocalDate date = LocalDate.of(2025, 1, 1);
            Study study = createStudy(StudyType.OFFLINE, 1L, 1L, 1L);

            // 1회차: 1/1, 2회차: 1/1
            LocalDate firstLessonDate = date;
            LocalDate secondLessonDate = date;

            StudyUpdateCommand command = createCommand(firstLessonDate, secondLessonDate);

            // when & then
            assertThatThrownBy(() -> study.update(command))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(STUDY_NOT_UPDATABLE_LESSON_PERIOD_NOT_SEQUENTIAL.getMessage());
        }

        @Test
        void 중간에_null이_있어도_성공한다() {
            // given
            LocalDate date = LocalDate.of(2025, 1, 1);
            Study study = createStudy(StudyType.OFFLINE, 1L, 1L, 1L);

            // 1회차: 1/2, 2회차: null, 3회차: 1/4
            LocalDate firstLessonDate = date.plusDays(1);
            LocalDate secondLessonDate = null;
            LocalDate thirdLessonDate = date.plusDays(3);

            StudyUpdateCommand command = createCommand(firstLessonDate, secondLessonDate, thirdLessonDate);

            // when & then
            assertThatNoException().isThrownBy(() -> study.update(command));
        }

        @Test
        void 모두_null인_경우_성공한다() {
            // given
            Study study = createStudy(StudyType.OFFLINE, 1L, 1L, 1L);

            LocalDate firstLessonDate = null;
            LocalDate secondLessonDate = null;

            StudyUpdateCommand command = createCommand(firstLessonDate, secondLessonDate);

            // when & then
            assertThatNoException().isThrownBy(() -> study.update(command));
        }

        @Test
        void 정순인_경우_성공한다() {
            // given
            LocalDate date = LocalDate.of(2025, 1, 1);
            Study study = createStudy(StudyType.OFFLINE, 1L, 1L, 1L);

            // 1회차: 1/2, 2회차: 1/3
            LocalDate firstLessonDate = date.plusDays(1);
            LocalDate secondLessonDate = date.plusDays(2);

            StudyUpdateCommand command = createCommand(firstLessonDate, secondLessonDate);

            // when & then
            assertThatNoException().isThrownBy(() -> study.update(command));
        }
    }
}

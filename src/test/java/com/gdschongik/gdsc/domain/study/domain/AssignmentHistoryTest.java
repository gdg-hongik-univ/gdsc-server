package com.gdschongik.gdsc.domain.study.domain;

import static com.gdschongik.gdsc.global.common.constant.StudyConstant.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.domain.common.vo.Period;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.helper.FixtureHelper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AssignmentHistoryTest {

    FixtureHelper fixtureHelper = new FixtureHelper();

    private Member createMember(Long id) {
        return fixtureHelper.createAssociateMember(id);
    }

    private Study createStudyWithMentor(Long mentorId) {
        Period applicationPeriod = Period.of(STUDY_START_DATETIME.minusDays(7), STUDY_START_DATETIME.minusDays(1));
        return fixtureHelper.createStudyWithMentor(mentorId, STUDY_ONGOING_PERIOD, applicationPeriod);
    }

    private StudyDetail createStudyDetailWithAssignment(Study study) {
        return fixtureHelper.createStudyDetailWithAssignment(
                study, STUDY_DETAIL_START_DATETIME, STUDY_DETAIL_END_DATETIME, STUDY_ASSIGNMENT_DEADLINE_DATETIME);
    }

    @Nested
    class 빈_과제이력_생성할때 {

        @Test
        void 제출상태는_FAILURE이다() {
            // given
            Member member = createMember(1L);
            Study study = createStudyWithMentor(1L);
            StudyDetail studyDetail = createStudyDetailWithAssignment(study);

            // when
            AssignmentHistory assignmentHistory = AssignmentHistory.create(studyDetail, member);

            // then
            assertThat(assignmentHistory.getSubmissionStatus()).isEqualTo(AssignmentSubmissionStatus.FAILURE);
        }

        @Test
        void 실패사유는_NOT_SUBMITTED이다() {
            // given
            Member member = createMember(1L);
            Study study = createStudyWithMentor(1L);
            StudyDetail studyDetail = createStudyDetailWithAssignment(study);

            // when
            AssignmentHistory assignmentHistory = AssignmentHistory.create(studyDetail, member);

            // then
            assertThat(assignmentHistory.getSubmissionFailureType()).isEqualTo(SubmissionFailureType.NOT_SUBMITTED);
        }
    }

    @Nested
    class 과제이력_제출_성공할때 {

        @Test
        void 제출상태는_SUCCESS이다() {
            // given
            Member member = createMember(1L);
            Study study = createStudyWithMentor(1L);
            StudyDetail studyDetail = createStudyDetailWithAssignment(study);
            AssignmentHistory assignmentHistory = AssignmentHistory.create(studyDetail, member);

            // when
            assignmentHistory.success(SUBMISSION_LINK, COMMIT_HASH, CONTENT_LENGTH, COMMITTED_AT);
        }

        @Test
        void 실패사유는_NONE이다() {
            // given
            Member member = createMember(1L);
            Study study = createStudyWithMentor(1L);
            StudyDetail studyDetail = createStudyDetailWithAssignment(study);
            AssignmentHistory assignmentHistory = AssignmentHistory.create(studyDetail, member);

            // when
            assignmentHistory.success(SUBMISSION_LINK, COMMIT_HASH, CONTENT_LENGTH, COMMITTED_AT);

            // then
            assertThat(assignmentHistory.getSubmissionFailureType()).isEqualTo(SubmissionFailureType.NONE);
        }
    }

    @Nested
    class 과제이력_제출_실패할때 {

        @Test
        void 제출상태는_FAILURE이다() {
            // given
            Member member = createMember(1L);
            Study study = createStudyWithMentor(1L);
            StudyDetail studyDetail = createStudyDetailWithAssignment(study);
            AssignmentHistory assignmentHistory = AssignmentHistory.create(studyDetail, member);
            assignmentHistory.success(SUBMISSION_LINK, COMMIT_HASH, CONTENT_LENGTH, COMMITTED_AT);

            // when
            assignmentHistory.fail(SubmissionFailureType.WORD_COUNT_INSUFFICIENT);

            // then
            assertThat(assignmentHistory.getSubmissionStatus()).isEqualTo(AssignmentSubmissionStatus.FAILURE);
        }

        @Test
        void 실패사유는_NOT_SUBMITTED가_될수없다() {
            // given
            Member member = createMember(1L);
            Study study = createStudyWithMentor(1L);
            StudyDetail studyDetail = createStudyDetailWithAssignment(study);
            AssignmentHistory assignmentHistory = AssignmentHistory.create(studyDetail, member);
            assignmentHistory.success(SUBMISSION_LINK, COMMIT_HASH, CONTENT_LENGTH, COMMITTED_AT);

            // when, then
            assertThatThrownBy(() -> assignmentHistory.fail(SubmissionFailureType.NOT_SUBMITTED))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ASSIGNMENT_FAILURE_TYPE_INVALID.getMessage());
        }

        @Test
        void 실패사유는_NONE이_될수없다() {
            // given
            Member member = createMember(1L);
            Study study = createStudyWithMentor(1L);
            StudyDetail studyDetail = createStudyDetailWithAssignment(study);
            AssignmentHistory assignmentHistory = AssignmentHistory.create(studyDetail, member);
            assignmentHistory.success(SUBMISSION_LINK, COMMIT_HASH, CONTENT_LENGTH, COMMITTED_AT);

            // when, then
            assertThatThrownBy(() -> assignmentHistory.fail(SubmissionFailureType.NONE))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ASSIGNMENT_FAILURE_TYPE_INVALID.getMessage());
        }

        @Test
        void 기존_제출정보는_삭제된다() {
            // given
            Member member = createMember(1L);
            Study study = createStudyWithMentor(1L);
            StudyDetail studyDetail = createStudyDetailWithAssignment(study);
            AssignmentHistory assignmentHistory = AssignmentHistory.create(studyDetail, member);
            assignmentHistory.success(SUBMISSION_LINK, COMMIT_HASH, CONTENT_LENGTH, COMMITTED_AT);

            // when
            assignmentHistory.fail(SubmissionFailureType.WORD_COUNT_INSUFFICIENT);

            // then
            assertThat(assignmentHistory.getSubmissionLink()).isNull();
            assertThat(assignmentHistory.getCommitHash()).isNull();
            assertThat(assignmentHistory.getContentLength()).isNull();
            assertThat(assignmentHistory.getCommittedAt()).isNull();
        }
    }
}

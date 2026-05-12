package com.gdschongik.gdsc.domain.study.application;

import static com.gdschongik.gdsc.global.common.constant.StudyConstant.*;
import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.domain.common.model.SemesterType;
import com.gdschongik.gdsc.domain.common.vo.Period;
import com.gdschongik.gdsc.domain.common.vo.Semester;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.member.domain.MemberRole;
import com.gdschongik.gdsc.domain.study.domain.Study;
import com.gdschongik.gdsc.domain.study.domain.StudyUpdateCommand;
import com.gdschongik.gdsc.domain.study.dto.dto.StudySimpleDto;
import com.gdschongik.gdsc.domain.study.dto.response.StudyTodoResponse;
import com.gdschongik.gdsc.helper.IntegrationTest;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class StudentStudyServiceTest extends IntegrationTest {

    @Autowired
    StudentStudyService studentStudyService;

    @Nested
    class 내_수강중인_스터디를_조회할때 {

        @Test
        void 진행중인_학기가_없으면_빈_리스트를_반환한다() {
            // given
            Member member = createAssociateMember();
            logoutAndReloginAs(member.getId(), MemberRole.ASSOCIATE);

            // when
            List<StudySimpleDto> response = studentStudyService.getMyCurrentStudies();

            // then
            assertThat(response).isEmpty();
        }

        @Test
        void 진행중인_학기와_일치하는_스터디만_반환한다() {
            // given
            Member member = createAssociateMember();
            logoutAndReloginAs(member.getId(), MemberRole.ASSOCIATE);

            LocalDateTime now = LocalDateTime.now();
            Semester currentSemester = Semester.of(2026, SemesterType.FIRST);
            Semester previousSemester = Semester.of(2025, SemesterType.SECOND);

            createRecruitment(currentSemester, now.minusDays(1), now.plusDays(1));

            Study currentStudy = createStudy("현재 스터디", currentSemester, member);
            Study previousStudy = createStudy("이전 스터디", previousSemester, member);
            createStudyHistory(member, currentStudy);
            createStudyHistory(member, previousStudy);

            // when
            List<StudySimpleDto> response = studentStudyService.getMyCurrentStudies();

            // then
            assertThat(response).extracting(StudySimpleDto::studyId).containsExactly(currentStudy.getId());
            assertThat(response).extracting(StudySimpleDto::studyName).containsExactly("현재 스터디");
        }
    }

    @Nested
    class 내_모든_스터디_할일을_조회할때 {

        @Test
        void 진행중인_학기가_없으면_빈_리스트를_반환한다() {
            // given
            Member member = createAssociateMember();
            logoutAndReloginAs(member.getId(), MemberRole.ASSOCIATE);

            // when
            List<StudyTodoResponse> response = studentStudyService.getMyStudiesTodos();

            // then
            assertThat(response).isEmpty();
        }

        @Test
        void 진행중인_학기와_일치하는_스터디의_할일만_반환한다() {
            // given
            Member member = createAssociateMember();
            logoutAndReloginAs(member.getId(), MemberRole.ASSOCIATE);

            LocalDateTime now = LocalDateTime.now();
            Semester currentSemester = Semester.of(2026, SemesterType.FIRST);
            Semester previousSemester = Semester.of(2025, SemesterType.SECOND);

            createRecruitment(currentSemester, now.minusDays(1), now.plusDays(1));

            Study currentStudy = createStudy("현재 스터디", currentSemester, member);
            Study previousStudy = createStudy("이전 스터디", previousSemester, member);
            createStudyHistory(member, currentStudy);
            createStudyHistory(member, previousStudy);
            updateSessionPeriodToNow(currentStudy, now);
            updateSessionPeriodToNow(previousStudy, now);

            // when
            List<StudyTodoResponse> response = studentStudyService.getMyStudiesTodos();

            // then
            assertThat(response).hasSize(2);
            assertThat(response)
                    .extracting(studyTodoResponse -> studyTodoResponse.study().studyId())
                    .containsOnly(currentStudy.getId());
            assertThat(response)
                    .extracting(StudyTodoResponse::todoType)
                    .containsExactlyInAnyOrder(
                            StudyTodoResponse.StudyTodoType.ATTENDANCE, StudyTodoResponse.StudyTodoType.ASSIGNMENT);
        }
    }

    private void updateSessionPeriodToNow(Study study, LocalDateTime now) {
        var session = study.getStudySessions().get(0);
        Period currentPeriod = Period.of(now.minusHours(1), now.plusHours(1));

        session.update(new StudyUpdateCommand.Session(
                session.getId(), "진행 중인 세션", "설명", currentPeriod, "진행 중인 과제", DESCRIPTION_LINK, currentPeriod));
        studyRepository.save(study);
    }
}

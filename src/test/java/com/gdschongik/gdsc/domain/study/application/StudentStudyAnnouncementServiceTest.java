package com.gdschongik.gdsc.domain.study.application;

import static com.gdschongik.gdsc.global.common.constant.StudyConstant.*;
import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.domain.common.model.SemesterType;
import com.gdschongik.gdsc.domain.common.vo.Semester;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.member.domain.MemberRole;
import com.gdschongik.gdsc.domain.study.domain.Study;
import com.gdschongik.gdsc.domain.study.domain.StudyAnnouncement;
import com.gdschongik.gdsc.domain.study.dto.response.StudyAnnouncementResponse;
import com.gdschongik.gdsc.helper.IntegrationTest;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class StudentStudyAnnouncementServiceTest extends IntegrationTest {

    @Autowired
    StudentStudyAnnouncementService studentStudyAnnouncementService;

    @Nested
    class 내가_수강중인_모든_스터디_공지를_조회할때 {

        @Test
        void 진행중인_학기가_없으면_빈_리스트를_반환한다() {
            // given
            Member member = createAssociateMember();
            logoutAndReloginAs(member.getId(), MemberRole.ASSOCIATE);

            // when
            List<StudyAnnouncementResponse> result = studentStudyAnnouncementService.getStudiesAnnouncements();

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void 진행중인_학기와_일치하는_스터디의_공지들만_반환한다() {
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

            StudyAnnouncement currentAnnouncement = createStudyAnnouncement("현재 공지", DESCRIPTION_LINK, currentStudy);
            createStudyAnnouncement("이전 공지", DESCRIPTION_LINK, previousStudy);

            // when
            List<StudyAnnouncementResponse> result = studentStudyAnnouncementService.getStudiesAnnouncements();

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).study().studyId()).isEqualTo(currentStudy.getId());
            assertThat(result.get(0).studyAnnouncement().studyAnnouncementId())
                    .isEqualTo(currentAnnouncement.getId());
            assertThat(result.get(0).studyAnnouncement().title()).isEqualTo("현재 공지");
        }
    }
}

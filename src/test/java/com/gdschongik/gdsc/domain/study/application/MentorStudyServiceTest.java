package com.gdschongik.gdsc.domain.study.application;

import static com.gdschongik.gdsc.global.common.constant.StudyConstant.MIN_ASSIGNMENT_CONTENT_LENGTH;
import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.member.domain.MemberRole;
import com.gdschongik.gdsc.domain.study.domain.Study;
import com.gdschongik.gdsc.domain.study.domain.StudyType;
import com.gdschongik.gdsc.domain.study.dto.request.StudyUpdateRequest;
import com.gdschongik.gdsc.helper.IntegrationTest;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MentorStudyServiceTest extends IntegrationTest {

    @Autowired
    MentorStudyService mentorStudyService;

    @Nested
    class 스터디_수정할때 {

        @Test
        void 스터디_스터디회차에_변경사항이_모두_반영된다() {
            // given
            Member mentor = createMentor();
            logoutAndReloginAs(1L, MemberRole.REGULAR);
            createStudy(StudyType.OFFLINE, mentor);

            var request = new StudyUpdateRequest(
                    "수정된 제목",
                    null,
                    null,
                    null,
                    null,
                    null,
                    MIN_ASSIGNMENT_CONTENT_LENGTH,
                    List.of(new StudyUpdateRequest.StudySessionUpdateDto(
                            1L, "수정된 1회차 스터디 제목", null, null, null, null, null)));

            // when
            mentorStudyService.updateStudy(1L, request);

            // then
            Optional<Study> optionalStudy = studyRepository.findFetchById(1L);
            assertThat(optionalStudy).isPresent();

            Study study = optionalStudy.get();
            assertThat(study.getTitle()).isEqualTo("수정된 제목");
            assertThat(study.getStudySessions().get(0).getLessonTitle()).isEqualTo("수정된 1회차 스터디 제목");
        }
    }
}

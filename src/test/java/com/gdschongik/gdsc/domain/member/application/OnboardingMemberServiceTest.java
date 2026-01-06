package com.gdschongik.gdsc.domain.member.application;

import static com.gdschongik.gdsc.global.common.constant.MemberConstant.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.gdschongik.gdsc.domain.common.vo.Period;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.member.domain.MemberRole;
import com.gdschongik.gdsc.domain.member.dto.response.MemberDashboardResponse;
import com.gdschongik.gdsc.domain.recruitment.application.OnboardingRecruitmentService;
import com.gdschongik.gdsc.domain.recruitment.domain.RecruitmentRound;
import com.gdschongik.gdsc.helper.IntegrationTest;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class OnboardingMemberServiceTest extends IntegrationTest {

    @Autowired
    private OnboardingMemberService onboardingMemberService;

    @Nested
    class 대시보드_조회할때 {

        /**
         * {@link Period#isOpen()}에서 LocalDateTime.now()를 사용하기 때문에 고정된 리쿠르팅을 반환하도록 설정
         * @see OnboardingRecruitmentService#findCurrentRecruitmentRound()
         */
        @BeforeEach
        void setUp() {
            RecruitmentRound recruitmentRound = createRecruitmentRound();
            when(onboardingRecruitmentService.findCurrentRecruitmentRound())
                    .thenReturn(Optional.ofNullable(recruitmentRound));
        }

        @Test
        void 정회원_미신청시_멤버십_응답은_null이다() {
            // given
            createMember();
            logoutAndReloginAs(1L, MemberRole.ASSOCIATE);

            // when
            MemberDashboardResponse response = onboardingMemberService.getDashboard();

            // then
            assertThat(response.currentMembership()).isNull();
        }

        @Test
        void 기본정보_미작성시_멤버_기본정보는_모두_null이다() {
            // given
            memberRepository.save(Member.createGuest(OAUTH_ID));
            logoutAndReloginAs(1L, MemberRole.ASSOCIATE);

            // when
            MemberDashboardResponse response = onboardingMemberService.getDashboard();

            // then - 전체 필드가 null인지 확인
            assertThat(response.member().info()).hasAllNullFieldsOrProperties();
        }
    }
}

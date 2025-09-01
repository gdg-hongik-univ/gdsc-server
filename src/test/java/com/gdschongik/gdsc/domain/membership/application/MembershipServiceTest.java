package com.gdschongik.gdsc.domain.membership.application;

import static com.gdschongik.gdsc.domain.member.domain.MemberRole.ASSOCIATE;
import static com.gdschongik.gdsc.global.common.constant.RecruitmentConstant.*;
import static com.gdschongik.gdsc.global.common.constant.TemporalConstant.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.domain.common.vo.Money;
import com.gdschongik.gdsc.domain.recruitment.domain.RecruitmentRound;
import com.gdschongik.gdsc.domain.recruitment.domain.RoundType;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.helper.IntegrationTest;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class MembershipServiceTest extends IntegrationTest {

    @Autowired
    private MembershipService membershipService;

    @Nested
    class 멤버십_가입신청시 {
        @Test
        void RecruitmentRound가_없다면_실패한다() {
            // given
            createMember();
            logoutAndReloginAs(1L, ASSOCIATE);
            Long recruitmentRoundId = 1L;

            // when & then
            assertThatThrownBy(() -> membershipService.submitMembership(recruitmentRoundId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(RECRUITMENT_ROUND_NOT_FOUND.getMessage());
        }

        @Test
        void 멤버십을_1차모집시_생성했지만_정회원_가입조건을_만족하지_않았다면_2차모집에서_멤버십_가입신청에_성공한다() {
            // given
            createAssociateMember();
            logoutAndReloginAs(1L, ASSOCIATE);

            RecruitmentRound firstRound = createRecruitmentRound(
                    RECRUITMENT_ROUND_NAME,
                    LocalDateTime.now().minusDays(5),
                    LocalDateTime.now().plusDays(1),
                    ACADEMIC_YEAR,
                    SEMESTER_TYPE,
                    ROUND_TYPE,
                    Money.from(20000L));

            RecruitmentRound secondRound = createRecruitmentRound(
                    RECRUITMENT_ROUND_NAME,
                    LocalDateTime.now().minusDays(1),
                    LocalDateTime.now().plusDays(1),
                    ACADEMIC_YEAR,
                    SEMESTER_TYPE,
                    RoundType.SECOND,
                    Money.from(20000L));

            // when & then
            membershipService.submitMembership(firstRound.getId());

            assertThatCode(() -> membershipService.submitMembership(secondRound.getId()))
                    .doesNotThrowAnyException();
        }
    }
}

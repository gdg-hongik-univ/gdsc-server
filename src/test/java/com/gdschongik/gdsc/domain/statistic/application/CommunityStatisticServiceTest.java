package com.gdschongik.gdsc.domain.statistic.application;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.statistic.dao.CommunityStatisticRepository;
import com.gdschongik.gdsc.domain.statistic.domain.CommunityStatistic;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.helper.IntegrationTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CommunityStatisticServiceTest extends IntegrationTest {

    @Autowired
    private CommunityStatisticService communityStatisticService;

    @Autowired
    private CommunityStatisticRepository communityStatisticRepository;

    @Nested
    class 누적_세미나_수_갱신시 {

        @Test
        void 성공한다() {
            // given
            CommunityStatistic communityStatistic = CommunityStatistic.create(0, 0, 0);
            communityStatisticRepository.save(communityStatistic);

            Member adminMember = createAdminMember();

            Integer newTotalSeminarCount = 5;

            // when
            communityStatisticService.updateTotalSeminarCount(adminMember.getDiscordUsername(), newTotalSeminarCount);

            // then
            Integer totalSeminarCount = communityStatisticRepository
                    .findTopByOrderByIdAsc()
                    .orElseThrow(() -> new CustomException(COMMUNITY_STATISTIC_NOT_FOUND))
                    .getTotalSeminarCount();
            assertThat(totalSeminarCount).isEqualTo(newTotalSeminarCount);
        }

        @Test
        void 어드민이_아니라면_실패한다() {
            // given
            CommunityStatistic communityStatistic = CommunityStatistic.create(0, 0, 0);
            communityStatisticRepository.save(communityStatistic);

            Member member = createAssociateMember();

            Integer newTotalSeminarCount = 5;

            // when & then
            assertThatThrownBy(() -> communityStatisticService.updateTotalSeminarCount(
                            member.getDiscordUsername(), newTotalSeminarCount))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(INVALID_ROLE.getMessage());
        }
    }
}

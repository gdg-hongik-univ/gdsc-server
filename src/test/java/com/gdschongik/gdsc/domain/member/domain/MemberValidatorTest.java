package com.gdschongik.gdsc.domain.member.domain;

import static com.gdschongik.gdsc.global.common.constant.RecruitmentConstant.*;
import static com.gdschongik.gdsc.global.common.constant.TemporalConstant.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.domain.common.vo.Period;
import com.gdschongik.gdsc.domain.member.domain.service.MemberValidator;
import com.gdschongik.gdsc.domain.recruitment.domain.Recruitment;
import com.gdschongik.gdsc.domain.recruitment.domain.RecruitmentRound;
import com.gdschongik.gdsc.global.exception.CustomException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class MemberValidatorTest {

    MemberValidator memberValidator = new MemberValidator();

    @Nested
    class 준회원으로_일괄_강등시 {

        @Test
        void 해당_학기에_이미_시작된_모집기간이_있다면_실패한다() {
            // given
            Recruitment recruitment = Recruitment.create(FEE_NAME, FEE, START_TO_END_PERIOD, SEMESTER);
            LocalDateTime now = LocalDateTime.now();
            RecruitmentRound recruitmentRound = RecruitmentRound.create(
                    RECRUITMENT_ROUND_NAME, ROUND_TYPE, Period.of(now.minusDays(1), now.plusDays(1)), recruitment);
            List<RecruitmentRound> recruitmentRounds = List.of(recruitmentRound);

            // when & then
            assertThatThrownBy(() -> memberValidator.validateMemberDemote(recruitmentRounds, now))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(RECRUITMENT_ROUND_START_DATE_ALREADY_PASSED.getMessage());
        }

        @Test
        void 해당_학기에_모집회차가_존재하지_않는다면_실패한다() {
            // given
            List<RecruitmentRound> recruitmentRounds = List.of();
            LocalDateTime now = LocalDateTime.now();

            // when & then
            assertThatThrownBy(() -> memberValidator.validateMemberDemote(recruitmentRounds, now))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(RECRUITMENT_ROUND_NOT_FOUND.getMessage());
        }
    }
}

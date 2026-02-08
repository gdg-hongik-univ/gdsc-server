package com.gdschongik.gdsc.domain.membership.domain;

import static com.gdschongik.gdsc.domain.member.domain.Department.*;
import static com.gdschongik.gdsc.domain.member.domain.Member.*;
import static com.gdschongik.gdsc.global.common.constant.MemberConstant.*;
import static com.gdschongik.gdsc.global.common.constant.RecruitmentConstant.*;
import static com.gdschongik.gdsc.global.common.constant.TemporalConstant.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.domain.common.model.SemesterType;
import com.gdschongik.gdsc.domain.common.vo.Money;
import com.gdschongik.gdsc.domain.common.vo.Period;
import com.gdschongik.gdsc.domain.common.vo.Semester;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.membership.domain.service.MembershipValidator;
import com.gdschongik.gdsc.domain.recruitment.domain.Recruitment;
import com.gdschongik.gdsc.domain.recruitment.domain.RecruitmentRound;
import com.gdschongik.gdsc.global.exception.CustomException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class MembershipValidatorTest {

    MembershipValidator membershipValidator = new MembershipValidator();

    private Member createAssociateMember(Long id) {
        Member member = createGuest(OAUTH_ID);
        member.updateInfo(STUDENT_ID, NAME, PHONE_NUMBER, D022, EMAIL);
        member.completeUnivEmailVerification(UNIV_EMAIL);
        member.verifyDiscord(DISCORD_USERNAME, NICKNAME, DISCORD_ID);
        member.advanceToAssociate();
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    private RecruitmentRound createRecruitmentRound(
            Integer academicYear,
            SemesterType semesterType,
            Money fee,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        Recruitment recruitment = Recruitment.create(
                FEE_NAME,
                fee,
                Period.of(SEMESTER_START_DATE, SEMESTER_END_DATE),
                Semester.of(academicYear, semesterType));
        return RecruitmentRound.create(RECRUITMENT_ROUND_NAME, ROUND_TYPE, Period.of(startDate, endDate), recruitment);
    }

    @Nested
    class 멤버십_가입신청시 {

        @Test
        void 역할이_GUEST라면_멤버십_가입신청에_실패한다() {
            // given
            Member member = createGuest(OAUTH_ID);

            RecruitmentRound recruitmentRound =
                    createRecruitmentRound(ACADEMIC_YEAR, SEMESTER_TYPE, FEE, START_DATE, END_DATE);

            // when & then
            assertThatThrownBy(() -> membershipValidator.validateMembershipSubmit(member, recruitmentRound, false))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MEMBERSHIP_NOT_APPLICABLE.getMessage());
        }

        @Test
        void 해당_리쿠르팅회차의_모집기간이_아니라면_실패한다() {
            // given
            Member member = createAssociateMember(1L);

            RecruitmentRound recruitmentRound =
                    createRecruitmentRound(ACADEMIC_YEAR, SEMESTER_TYPE, FEE, START_DATE, END_DATE);

            // when & then
            assertThatThrownBy(() -> membershipValidator.validateMembershipSubmit(member, recruitmentRound, false))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MEMBERSHIP_RECRUITMENT_ROUND_NOT_OPEN.getMessage());
        }

        @Test
        void 해당_학기에_이미_멤버십을_생성한_적이_있다면_실패한다() {
            // given
            Member member = createAssociateMember(1L);

            RecruitmentRound recruitmentRound =
                    createRecruitmentRound(ACADEMIC_YEAR, SEMESTER_TYPE, FEE, START_DATE, END_DATE);

            Membership membership = Membership.create(member, recruitmentRound);

            // when & then
            assertThatThrownBy(() -> membershipValidator.validateMembershipSubmit(member, recruitmentRound, true))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MEMBERSHIP_ALREADY_SUBMITTED.getMessage());
        }
    }
}

package com.gdschongik.gdsc.domain.member.domain;

import static com.gdschongik.gdsc.domain.common.model.RequirementStatus.*;
import static com.gdschongik.gdsc.domain.member.domain.Department.*;
import static com.gdschongik.gdsc.domain.member.domain.MemberRole.*;
import static com.gdschongik.gdsc.domain.member.domain.MemberStatus.*;
import static com.gdschongik.gdsc.global.common.constant.MemberConstant.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.global.exception.CustomException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MemberTest {

    @Nested
    class 게스트_회원가입시 {

        @Test
        void MemberRole은_GUEST이다() {
            // given
            Member member = Member.createGuest(OAUTH_ID);

            // when
            MemberRole role = member.getRole();

            // then
            assertThat(role).isEqualTo(MemberRole.GUEST);
        }

        @Test
        void MemberStatus는_NORMAL이다() {
            // given
            Member member = Member.createGuest(OAUTH_ID);

            // when
            MemberStatus status = member.getStatus();

            // then
            assertThat(status).isEqualTo(MemberStatus.NORMAL);
        }

        @Test
        void 모든_준회원_가입조건은_인증되지_않은_상태이다() {
            // given
            Member member = Member.createGuest(OAUTH_ID);

            // when
            AssociateRequirement requirement = member.getAssociateRequirement();

            // then
            assertThat(requirement.getUnivStatus()).isEqualTo(UNSATISFIED);
            assertThat(requirement.getDiscordStatus()).isEqualTo(UNSATISFIED);
            assertThat(requirement.getInfoStatus()).isEqualTo(UNSATISFIED);
        }
    }

    @Nested
    class 준회원_가입조건_인증시도시 {

        @Test
        void 기본회원정보_작성시_준회원_가입조건중_기본정보_인증상태가_인증된다() {
            // given
            Member member = Member.createGuest(OAUTH_ID);

            // when
            member.updateInfo(STUDENT_ID, NAME, PHONE_NUMBER, D022, EMAIL);

            // then
            AssociateRequirement requirement = member.getAssociateRequirement();
            assertThat(requirement.getInfoStatus()).isEqualTo(SATISFIED);
        }

        @Test
        void 재학생이메일_인증시_준회원_가입조건중_재학생이메일_인증상태가_인증된다() {
            // given
            Member member = Member.createGuest(OAUTH_ID);

            // when
            member.completeUnivEmailVerification(UNIV_EMAIL);

            // then
            AssociateRequirement requirement = member.getAssociateRequirement();
            assertThat(requirement.getUnivStatus()).isEqualTo(SATISFIED);
        }

        @Test
        void 디스코드_인증시_준회원_가입조건중_디스코드_인증상태가_인증된다() {
            // given
            Member member = Member.createGuest(OAUTH_ID);

            // when
            member.verifyDiscord(DISCORD_USERNAME, NICKNAME);

            // then
            AssociateRequirement requirement = member.getAssociateRequirement();
            assertThat(requirement.getDiscordStatus()).isEqualTo(SATISFIED);
        }
    }

    @Nested
    class 준회원으로_승급시도시 {

        @Test
        void 기본_회원정보_작성하지_않았으면_실패한다() {
            // given
            Member member = Member.createGuest(OAUTH_ID);

            member.verifyDiscord(DISCORD_USERNAME, NICKNAME);
            member.completeUnivEmailVerification(UNIV_EMAIL);

            // when & then
            assertThatThrownBy(member::advanceToAssociate)
                    .isInstanceOf(CustomException.class)
                    .hasMessage(INFO_NOT_SATISFIED.getMessage());
        }

        @Test
        void 디스코드_인증하지_않았으면_실패한다() {
            // given
            Member member = Member.createGuest(OAUTH_ID);

            member.updateInfo(STUDENT_ID, NAME, PHONE_NUMBER, D022, EMAIL);
            member.completeUnivEmailVerification(UNIV_EMAIL);

            // when & then
            assertThatThrownBy(member::advanceToAssociate)
                    .isInstanceOf(CustomException.class)
                    .hasMessage(DISCORD_NOT_SATISFIED.getMessage());
        }

        @Test
        void 이미_준회원으로_승급_돼있으면_실패한다() {
            // given
            Member member = Member.createGuest(OAUTH_ID);

            member.updateInfo(STUDENT_ID, NAME, PHONE_NUMBER, D022, EMAIL);
            member.completeUnivEmailVerification(UNIV_EMAIL);
            member.verifyDiscord(DISCORD_USERNAME, NICKNAME);
            member.advanceToAssociate();

            // when & then
            assertThatThrownBy(member::advanceToAssociate)
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MEMBER_ALREADY_ASSOCIATE.getMessage());
        }

        @Test
        void 모든_준회원_가입조건이_인증되었으면_성공한다() {
            // given
            Member member = Member.createGuest(OAUTH_ID);

            member.updateInfo(STUDENT_ID, NAME, PHONE_NUMBER, D022, EMAIL);
            member.completeUnivEmailVerification(UNIV_EMAIL);
            member.verifyDiscord(DISCORD_USERNAME, NICKNAME);

            // when
            member.advanceToAssociate();

            // then
            assertThat(member.getRole()).isEqualTo(ASSOCIATE);
        }
    }

    @Nested
    class 회원탈퇴시 {

        @Test
        void 이미_탈퇴한_유저면_실패한다() {
            // given
            Member member = Member.createGuest(OAUTH_ID);

            member.withdraw();

            // when & then
            assertThatThrownBy(member::withdraw)
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MEMBER_DELETED.getMessage());
        }

        @Test
        void 회원탈퇴시_이전에_탈퇴하지_않은_유저면_성공한다() {
            // given
            Member member = Member.createGuest(OAUTH_ID);

            // when
            member.withdraw();

            // then
            assertThat(member.getStatus()).isEqualTo(DELETED);
        }
    }

    @Nested
    class 회원수정시 {
        @Test
        void 탈퇴하지_않은_유저면_성공한다() {
            // given
            Member member = Member.createGuest(OAUTH_ID);

            member.updateInfo(MODIFIED_STUDENT_ID, NAME, PHONE_NUMBER, D022, UNIV_EMAIL, DISCORD_USERNAME, NICKNAME);

            // then
            assertThat(member.getStudentId()).isEqualTo(MODIFIED_STUDENT_ID);
        }

        @Test
        void 탈퇴한_유저면_실패한다() {
            // given
            Member member = Member.createGuest(OAUTH_ID);

            member.withdraw();

            // when & then
            assertThatThrownBy(() -> {
                        member.updateInfo(
                                MODIFIED_STUDENT_ID, NAME, PHONE_NUMBER, D022, UNIV_EMAIL, DISCORD_USERNAME, NICKNAME);
                    })
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MEMBER_DELETED.getMessage());
        }
    }

    @Test
    void 디스코드인증시_탈퇴한_유저면_실패한다() {
        // given
        Member member = Member.createGuest(OAUTH_ID);

        member.withdraw();

        // when & then
        assertThatThrownBy(() -> {
                    member.verifyDiscord(DISCORD_USERNAME, NICKNAME);
                })
                .isInstanceOf(CustomException.class)
                .hasMessage(MEMBER_DELETED.getMessage());
    }

    @Nested
    class 정회원으로_승급_시도시 {
        @Test
        void 이미_정회원이라면_실패한다() {
            // given
            Member member = Member.createGuest(OAUTH_ID);

            member.updateInfo(STUDENT_ID, NAME, PHONE_NUMBER, D022, EMAIL);
            member.completeUnivEmailVerification(UNIV_EMAIL);
            member.verifyDiscord(DISCORD_USERNAME, NICKNAME);
            member.advanceToAssociate();
            member.advanceToRegular();

            // when & then
            assertThatThrownBy(member::advanceToRegular)
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MEMBER_ALREADY_REGULAR.getMessage());
        }

        @Test
        void MemberRole이_GUEST_이라면_실패한다() {
            // given
            Member member = Member.createGuest(OAUTH_ID);

            // when & then
            assertThatThrownBy(member::advanceToRegular)
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MEMBER_NOT_ASSOCIATE.getMessage());
        }

        @Test
        void 준회원이라면_성공한다() {
            // given
            Member member = Member.createGuest(OAUTH_ID);

            member.updateInfo(STUDENT_ID, NAME, PHONE_NUMBER, D022, EMAIL);
            member.completeUnivEmailVerification(UNIV_EMAIL);
            member.verifyDiscord(DISCORD_USERNAME, NICKNAME);
            member.advanceToAssociate();

            // when
            member.advanceToRegular();

            // then
            assertThat(member.getRole()).isEqualTo(REGULAR);
        }
    }

    @Nested
    class 비회원으로_강등시 {

        @Test
        void 성공한다() {
            // given
            Member member = Member.createGuest(OAUTH_ID);

            member.updateInfo(STUDENT_ID, NAME, PHONE_NUMBER, D022, EMAIL);
            member.completeUnivEmailVerification(UNIV_EMAIL);
            member.verifyDiscord(DISCORD_USERNAME, NICKNAME);
            member.advanceToAssociate();

            // when
            member.demoteToGuest();

            // then
            assertThat(member)
                    .extracting(
                            Member::getRole,
                            Member::getUnivEmail,
                            Member::getName,
                            Member::getDepartment,
                            Member::getStudentId,
                            Member::getPhone,
                            Member::getDiscordId,
                            Member::getNickname,
                            Member::getDiscordUsername)
                    .containsExactly(GUEST, null, null, null, null, null, null, null, null);
            assertThat(member.getAssociateRequirement())
                    .extracting(
                            AssociateRequirement::getDiscordStatus,
                            AssociateRequirement::getInfoStatus,
                            AssociateRequirement::getUnivStatus)
                    .containsExactly(UNSATISFIED, UNSATISFIED, UNSATISFIED);
        }
    }
}

package com.gdschongik.gdsc.domain.event.domain;

import static com.gdschongik.gdsc.domain.member.domain.Department.*;
import static com.gdschongik.gdsc.global.common.constant.MemberConstant.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.helper.FixtureHelper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ParticipantRoleTest {

    private final FixtureHelper fixtureHelper = new FixtureHelper();

    @Nested
    class 참여자_역할_생성시 {

        @Test
        void 정회원은_REGULAR를_반환한다() {
            // given
            Member regularMember = fixtureHelper.createRegularMember(1L);
            EventParticipation eventParticipation = createEventParticipation(regularMember);

            // when
            ParticipantRole result = ParticipantRole.of(eventParticipation, regularMember);

            // then
            assertThat(result).isEqualTo(ParticipantRole.REGULAR);
        }

        @Test
        void 준회원은_ASSOCIATE를_반환한다() {
            // given
            Member associateMember = fixtureHelper.createAssociateMember(1L);
            EventParticipation eventParticipation = createEventParticipation(associateMember);

            // when
            ParticipantRole result = ParticipantRole.of(eventParticipation, associateMember);

            // then
            assertThat(result).isEqualTo(ParticipantRole.ASSOCIATE);
        }

        @Test
        void 기본회원정보를_작성한_게스트는_GUEST를_반환한다() {
            // given
            Member guestMember = fixtureHelper.createGuestMember(1L);
            guestMember.updateBasicMemberInfo(STUDENT_ID, NAME, PHONE_NUMBER, D022, EMAIL);

            EventParticipation eventParticipation = createEventParticipation(guestMember);

            // when
            ParticipantRole result = ParticipantRole.of(eventParticipation, guestMember);

            // then
            assertThat(result).isEqualTo(ParticipantRole.GUEST);
        }

        @Test
        void 비회원은_NON_MEMBER를_반환한다() {
            // given
            EventParticipation eventParticipation = createEventParticipation(null);

            // when
            ParticipantRole result = ParticipantRole.of(eventParticipation, null);

            // then
            assertThat(result).isEqualTo(ParticipantRole.NON_MEMBER);
        }

        @Test
        void 회원이_null이고_이벤트참여정보의_멤버_ID가_존재할때_예외를_발생시킨다() {
            // given
            Member ignored = fixtureHelper.createRegularMember(1L);
            EventParticipation participationWithRegistered = createEventParticipation(ignored);

            // when & then
            assertThatThrownBy(() -> ParticipantRole.of(participationWithRegistered, null))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(PARTICIPANT_ROLE_NOT_CREATABLE_BOTH_EXISTENCE_MISMATCH.getMessage());
        }

        @Test
        void 회원이_존재하지만_이벤트참여정보의_멤버_ID가_null일때_예외를_발생시킨다() {
            // given
            Member registeredRegularMember = fixtureHelper.createRegularMember(1L);
            EventParticipation participationWithUnregistered = createEventParticipation(null);

            // when & then
            assertThatThrownBy(() -> ParticipantRole.of(participationWithUnregistered, registeredRegularMember))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(PARTICIPANT_ROLE_NOT_CREATABLE_BOTH_EXISTENCE_MISMATCH.getMessage());
        }

        @Test
        void 이벤트참여정보와_회원의_ID가_다를때_예외를_발생시킨다() {
            // given
            Member member1 = fixtureHelper.createRegularMember(1L);
            Member member2 = fixtureHelper.createRegularMember(2L);
            EventParticipation eventParticipation = createEventParticipation(member1);

            // when & then
            assertThatThrownBy(() -> ParticipantRole.of(eventParticipation, member2))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(PARTICIPANT_ROLE_NOT_CREATABLE_BOTH_ID_MISMATCH.getMessage());
        }
    }

    private EventParticipation createEventParticipation(Member member) {
        if (member != null) {
            return EventParticipation.createOnlineForRegistered(
                    member,
                    AfterPartyApplicationStatus.NOT_APPLIED,
                    AfterPartyAttendanceStatus.NOT_ATTENDED,
                    PaymentStatus.UNPAID,
                    PaymentStatus.UNPAID,
                    null);
        } else {
            return EventParticipation.createOnlineForUnregistered(
                    Participant.of(NAME, STUDENT_ID, PHONE_NUMBER),
                    AfterPartyApplicationStatus.NOT_APPLIED,
                    AfterPartyAttendanceStatus.NOT_ATTENDED,
                    PaymentStatus.UNPAID,
                    PaymentStatus.UNPAID,
                    null);
        }
    }
}

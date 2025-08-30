package com.gdschongik.gdsc.domain.event.domain;

import static com.gdschongik.gdsc.global.common.constant.EventConstant.*;
import static com.gdschongik.gdsc.global.common.constant.MemberConstant.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.helper.FixtureHelper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class EventParticipationDomainServiceTest {

    EventParticipationDomainService domainService = new EventParticipationDomainService();
    FixtureHelper fixtureHelper = new FixtureHelper();

    @Nested
    class 회원이_온라인으로_신청하는_경우 {

        @Test
        void 신청_기간이_아닌경우_실패한다() {
            // given
            Member member = fixtureHelper.createRegularMember(1L);
            AfterPartyApplicationStatus status = AfterPartyApplicationStatus.APPLIED;
            // 신청 기간 (25년 3월 1일 ~ 3월 14일)
            Event event = fixtureHelper.createEvent(
                    1L,
                    REGULAR_ROLE_ONLY_STATUS,
                    AFTER_PARTY_STATUS,
                    PRE_PAYMENT_STATUS,
                    POST_PAYMENT_STATUS,
                    RSVP_QUESTION_STATUS);
            LocalDateTime invalidDate = LocalDateTime.of(2025, 4, 1, 0, 0);

            // when & then
            assertThatThrownBy(() -> domainService.applyEventForRegistered(member, status, event, invalidDate))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(EVENT_NOT_APPLICABLE_APPLICATION_PERIOD_INVALID.getMessage());
        }

        @Test
        void 정회원만_참석_가능한_행사에_정회원이_아닌_유저가_신청하면_실패한다() {
            // given
            Member guestMember = fixtureHelper.createGuestMember(1L);
            AfterPartyApplicationStatus status = AfterPartyApplicationStatus.APPLIED;
            Event event = fixtureHelper.createEvent(
                    1L,
                    UsageStatus.ENABLED, // 정회원 전용 신청 폼
                    AFTER_PARTY_STATUS,
                    PRE_PAYMENT_STATUS,
                    POST_PAYMENT_STATUS,
                    RSVP_QUESTION_STATUS);
            LocalDateTime now = LocalDateTime.of(2025, 3, 1, 0, 0);

            // when & then
            assertThatThrownBy(() -> domainService.applyEventForRegistered(guestMember, status, event, now))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(EVENT_NOT_APPLICABLE_NOT_REGULAR_ROLE.getMessage());
        }

        @Test
        void 뒤풀이가_있는_행사에_뒤풀이_신청여부가_NONE이면_실패한다() {
            // given
            Member member = fixtureHelper.createRegularMember(1L);
            AfterPartyApplicationStatus noneStatus = AfterPartyApplicationStatus.NONE;
            Event event = fixtureHelper.createEvent(
                    1L,
                    REGULAR_ROLE_ONLY_STATUS,
                    UsageStatus.ENABLED, // 뒤풀이 활성화
                    PRE_PAYMENT_STATUS,
                    POST_PAYMENT_STATUS,
                    RSVP_QUESTION_STATUS);
            LocalDateTime now = LocalDateTime.of(2025, 3, 1, 0, 0);

            // when & then
            assertThatThrownBy(() -> domainService.applyEventForRegistered(member, noneStatus, event, now))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(EVENT_NOT_APPLICABLE_AFTER_PARTY_NONE.getMessage());
        }

        @Test
        void 뒤풀이가_없는_행사에_뒤풀이_신청을_하면_실패한다() {
            // given
            Member member = fixtureHelper.createRegularMember(1L);
            AfterPartyApplicationStatus appliedStatus = AfterPartyApplicationStatus.APPLIED;
            Event event = fixtureHelper.createEvent(
                    1L,
                    REGULAR_ROLE_ONLY_STATUS,
                    UsageStatus.DISABLED, // 뒤풀이 비활성화
                    UsageStatus.DISABLED,
                    UsageStatus.DISABLED,
                    RSVP_QUESTION_STATUS);
            LocalDateTime now = LocalDateTime.of(2025, 3, 1, 0, 0);

            // when & then
            assertThatThrownBy(() -> domainService.applyEventForRegistered(member, appliedStatus, event, now))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(EVENT_NOT_APPLICABLE_AFTER_PARTY_DISABLED.getMessage());
        }

        @Test
        void 기본_정보가_작성되지_않은_회원이_신청하면_실패한다() {
            // given
            Member guestMember = fixtureHelper.createGuestMember(1L); // 기본 정보 미작성
            AfterPartyApplicationStatus status = AfterPartyApplicationStatus.APPLIED;
            Event event = fixtureHelper.createEvent(
                    1L,
                    REGULAR_ROLE_ONLY_STATUS,
                    AFTER_PARTY_STATUS,
                    PRE_PAYMENT_STATUS,
                    POST_PAYMENT_STATUS,
                    RSVP_QUESTION_STATUS);
            LocalDateTime now = LocalDateTime.of(2025, 3, 1, 0, 0);

            // when & then
            assertThatThrownBy(() -> domainService.applyEventForRegistered(guestMember, status, event, now))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(EVENT_NOT_APPLICABLE_MEMBER_INFO_NOT_SATISFIED.getMessage());
        }
    }

    @Nested
    class 비회원이_온라인으로_신청하는_경우 {

        @Test
        void 신청_기간이_아닌경우_실패한다() {
            // given
            Participant participant = Participant.of(NAME, STUDENT_ID, PHONE_NUMBER);
            AfterPartyApplicationStatus status = AfterPartyApplicationStatus.APPLIED;
            // 신청 기간 (25년 3월 1일 ~ 3월 14일)
            Event event = fixtureHelper.createEvent(
                    1L,
                    REGULAR_ROLE_ONLY_STATUS,
                    AFTER_PARTY_STATUS,
                    PRE_PAYMENT_STATUS,
                    POST_PAYMENT_STATUS,
                    RSVP_QUESTION_STATUS);
            LocalDateTime invalidDate = LocalDateTime.of(2025, 4, 1, 0, 0);

            // when & then
            assertThatThrownBy(() ->
                            domainService.applyEventForUnregistered(participant, status, event, invalidDate, true))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(EVENT_NOT_APPLICABLE_APPLICATION_PERIOD_INVALID.getMessage());
        }

        @Test
        void 정회원만_참석_가능한_행사에_신청하면_실패한다() {
            // given
            Participant participant = Participant.of(NAME, STUDENT_ID, PHONE_NUMBER);
            AfterPartyApplicationStatus status = AfterPartyApplicationStatus.APPLIED;
            Event event = fixtureHelper.createEvent(
                    1L,
                    UsageStatus.ENABLED, // 정회원 전용 신청 폼
                    AFTER_PARTY_STATUS,
                    PRE_PAYMENT_STATUS,
                    POST_PAYMENT_STATUS,
                    RSVP_QUESTION_STATUS);
            LocalDateTime now = LocalDateTime.of(2025, 3, 1, 0, 0);

            // when & then
            assertThatThrownBy(() -> domainService.applyEventForUnregistered(participant, status, event, now, true))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(EVENT_NOT_APPLICABLE_NOT_REGULAR_ROLE.getMessage());
        }

        @Test
        void 뒤풀이가_있는_행사에_뒤풀이_신청여부가_NONE이면_실패한다() {
            // given
            Participant participant = Participant.of(NAME, STUDENT_ID, PHONE_NUMBER);
            AfterPartyApplicationStatus noneStatus = AfterPartyApplicationStatus.NONE;
            Event event = fixtureHelper.createEvent(
                    1L,
                    REGULAR_ROLE_ONLY_STATUS,
                    UsageStatus.ENABLED, // 뒤풀이 활성화
                    PRE_PAYMENT_STATUS,
                    POST_PAYMENT_STATUS,
                    RSVP_QUESTION_STATUS);
            LocalDateTime now = LocalDateTime.of(2025, 3, 1, 0, 0);

            // when & then
            assertThatThrownBy(() -> domainService.applyEventForUnregistered(participant, noneStatus, event, now, true))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(EVENT_NOT_APPLICABLE_AFTER_PARTY_NONE.getMessage());
        }

        @Test
        void 뒤풀이가_없는_행사에_뒤풀이_신청을_하면_실패한다() {
            // given
            Participant participant = Participant.of(NAME, STUDENT_ID, PHONE_NUMBER);
            AfterPartyApplicationStatus appliedStatus = AfterPartyApplicationStatus.APPLIED;
            Event event = fixtureHelper.createEvent(
                    1L,
                    REGULAR_ROLE_ONLY_STATUS,
                    UsageStatus.DISABLED, // 뒤풀이 비활성화
                    UsageStatus.DISABLED,
                    UsageStatus.DISABLED,
                    RSVP_QUESTION_STATUS);
            LocalDateTime now = LocalDateTime.of(2025, 3, 1, 0, 0);

            // when & then
            assertThatThrownBy(
                            () -> domainService.applyEventForUnregistered(participant, appliedStatus, event, now, true))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(EVENT_NOT_APPLICABLE_AFTER_PARTY_DISABLED.getMessage());
        }

        @Test
        void 기본_정보가_작성된_학번으로_신청하면_실패한다() {
            // given
            Participant participant = Participant.of(NAME, STUDENT_ID, PHONE_NUMBER);
            AfterPartyApplicationStatus status = AfterPartyApplicationStatus.APPLIED;
            Event event = fixtureHelper.createEvent(
                    1L,
                    REGULAR_ROLE_ONLY_STATUS,
                    AFTER_PARTY_STATUS,
                    PRE_PAYMENT_STATUS,
                    POST_PAYMENT_STATUS,
                    RSVP_QUESTION_STATUS);
            LocalDateTime now = LocalDateTime.of(2025, 3, 1, 0, 0);

            // when & then
            assertThatThrownBy(() -> domainService.applyEventForUnregistered(participant, status, event, now, true))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(EVENT_NOT_APPLICABLE_MEMBER_INFO_SATISFIED.getMessage());
        }
    }

    @Nested
    class 회원이_뒤풀이_현장등록으로_신청하는_경우 {

        @Test
        void 정회원만_참석_가능한_행사에_정회원이_아닌_유저가_신청하면_실패한다() {
            // given
            Member guestMember = fixtureHelper.createGuestMember(1L);
            Event event = fixtureHelper.createEvent(
                    1L,
                    UsageStatus.ENABLED, // 정회원 전용 신청 폼
                    AFTER_PARTY_STATUS,
                    PRE_PAYMENT_STATUS,
                    POST_PAYMENT_STATUS,
                    RSVP_QUESTION_STATUS);

            // when & then
            assertThatThrownBy(() -> domainService.joinOnsiteForRegistered(guestMember, event))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(EVENT_NOT_APPLICABLE_NOT_REGULAR_ROLE.getMessage());
        }
    }

    @Nested
    class 비회원이_뒤풀이_현장등록으로_신청하는_경우 {

        @Test
        void 정회원만_참석_가능한_행사에_신청하면_실패한다() {
            // given
            Participant participant = Participant.of(NAME, STUDENT_ID, PHONE_NUMBER);
            Event event = fixtureHelper.createEvent(
                    1L,
                    UsageStatus.ENABLED, // 정회원 전용 신청 폼
                    AFTER_PARTY_STATUS,
                    PRE_PAYMENT_STATUS,
                    POST_PAYMENT_STATUS,
                    RSVP_QUESTION_STATUS);

            // when & then
            assertThatThrownBy(() -> domainService.joinOnsiteForUnregistered(participant, event, true))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(EVENT_NOT_APPLICABLE_NOT_REGULAR_ROLE.getMessage());
        }
    }

    @Nested
    class 회원이_수동등록으로_신청하는_경우 {

        @Test
        void 정회원만_참석_가능한_행사에_정회원이_아닌_유저가_신청하면_실패한다() {
            // given
            Member guestMember = fixtureHelper.createGuestMember(1L);
            Event event = fixtureHelper.createEvent(
                    1L,
                    UsageStatus.ENABLED, // 정회원 전용 신청 폼
                    AFTER_PARTY_STATUS,
                    PRE_PAYMENT_STATUS,
                    POST_PAYMENT_STATUS,
                    RSVP_QUESTION_STATUS);

            // when & then
            assertThatThrownBy(() -> domainService.applyManualForRegistered(guestMember, event))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(EVENT_NOT_APPLICABLE_NOT_REGULAR_ROLE.getMessage());
        }

        @Test
        void 뒤풀이가_있는_행사의_경우_뒤풀이_신청_상태가_APPLIED로_설정된다() {
            // given
            Member member = fixtureHelper.createRegularMember(1L);
            Event eventWithAfterParty = fixtureHelper.createEvent(
                    1L,
                    REGULAR_ROLE_ONLY_STATUS,
                    UsageStatus.ENABLED, // 뒤풀이 활성화
                    PRE_PAYMENT_STATUS,
                    POST_PAYMENT_STATUS,
                    RSVP_QUESTION_STATUS);

            // when
            EventParticipation participation = domainService.applyManualForRegistered(member, eventWithAfterParty);

            // then
            assertThat(participation.getAfterPartyApplicationStatus()).isEqualTo(AfterPartyApplicationStatus.APPLIED);
            assertThat(participation.getMainEventApplicationStatus()).isEqualTo(MainEventApplicationStatus.NOT_APPLIED);
        }

        @Test
        void 뒤풀이가_없는_행사의_경우_뒤풀이_신청_상태가_NONE으로_설정된다() {
            // given
            Member member = fixtureHelper.createRegularMember(1L);
            Event eventWithoutAfterParty = fixtureHelper.createEvent(
                    1L,
                    REGULAR_ROLE_ONLY_STATUS,
                    UsageStatus.DISABLED, // 뒤풀이 비활성화
                    UsageStatus.DISABLED,
                    UsageStatus.DISABLED,
                    RSVP_QUESTION_STATUS);

            // when
            EventParticipation participation = domainService.applyManualForRegistered(member, eventWithoutAfterParty);

            // then
            assertThat(participation.getAfterPartyApplicationStatus()).isEqualTo(AfterPartyApplicationStatus.NONE);
            assertThat(participation.getMainEventApplicationStatus()).isEqualTo(MainEventApplicationStatus.NOT_APPLIED);
        }

        @Test
        void 기본_정보가_작성되지_않은_회원이_신청하면_실패한다() {
            // given
            Member guestMember = fixtureHelper.createGuestMember(1L); // 기본 정보 미작성
            Event event = fixtureHelper.createEvent(
                    1L,
                    REGULAR_ROLE_ONLY_STATUS,
                    AFTER_PARTY_STATUS,
                    PRE_PAYMENT_STATUS,
                    POST_PAYMENT_STATUS,
                    RSVP_QUESTION_STATUS);

            // when & then
            assertThatThrownBy(() -> domainService.applyManualForRegistered(guestMember, event))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(EVENT_NOT_APPLICABLE_MEMBER_INFO_NOT_SATISFIED.getMessage());
        }
    }

    @Nested
    class 비회원이_수동등록으로_신청하는_경우 {

        @Test
        void 정회원만_참석_가능한_행사에_신청하면_실패한다() {
            // given
            Participant participant = Participant.of(NAME, STUDENT_ID, PHONE_NUMBER);
            Event event = fixtureHelper.createEvent(
                    1L,
                    UsageStatus.ENABLED, // 정회원 전용 신청 폼
                    AFTER_PARTY_STATUS,
                    PRE_PAYMENT_STATUS,
                    POST_PAYMENT_STATUS,
                    RSVP_QUESTION_STATUS);

            // when & then
            assertThatThrownBy(() -> domainService.applyManualForUnregistered(participant, event, true))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(EVENT_NOT_APPLICABLE_NOT_REGULAR_ROLE.getMessage());
        }

        @Test
        void 뒤풀이가_있는_행사의_경우_뒤풀이_신청_상태가_APPLIED로_설정된다() {
            // given
            Participant participant = Participant.of(NAME, STUDENT_ID, PHONE_NUMBER);
            Event eventWithAfterParty = fixtureHelper.createEvent(
                    1L,
                    REGULAR_ROLE_ONLY_STATUS,
                    UsageStatus.ENABLED, // 뒤풀이 활성화
                    PRE_PAYMENT_STATUS,
                    POST_PAYMENT_STATUS,
                    RSVP_QUESTION_STATUS);

            // when
            EventParticipation participation =
                    domainService.applyManualForUnregistered(participant, eventWithAfterParty, true);

            // then
            assertThat(participation.getAfterPartyApplicationStatus()).isEqualTo(AfterPartyApplicationStatus.APPLIED);
            assertThat(participation.getMainEventApplicationStatus()).isEqualTo(MainEventApplicationStatus.NOT_APPLIED);
        }

        @Test
        void 뒤풀이가_없는_행사의_경우_뒤풀이_신청_상태가_NONE으로_설정된다() {
            // given
            Participant participant = Participant.of(NAME, STUDENT_ID, PHONE_NUMBER);
            Event eventWithoutAfterParty = fixtureHelper.createEvent(
                    1L,
                    REGULAR_ROLE_ONLY_STATUS,
                    UsageStatus.DISABLED, // 뒤풀이 비활성화
                    UsageStatus.DISABLED,
                    UsageStatus.DISABLED,
                    RSVP_QUESTION_STATUS);

            // when
            EventParticipation participation =
                    domainService.applyManualForUnregistered(participant, eventWithoutAfterParty, true);

            // then
            assertThat(participation.getAfterPartyApplicationStatus()).isEqualTo(AfterPartyApplicationStatus.NONE);
            assertThat(participation.getMainEventApplicationStatus()).isEqualTo(MainEventApplicationStatus.NOT_APPLIED);
        }

        @Test
        void 기본_정보가_작성된_학번으로_신청하면_실패한다() {
            // given
            Participant participant = Participant.of(NAME, STUDENT_ID, PHONE_NUMBER);
            Event event = fixtureHelper.createEvent(
                    1L,
                    REGULAR_ROLE_ONLY_STATUS,
                    AFTER_PARTY_STATUS,
                    PRE_PAYMENT_STATUS,
                    POST_PAYMENT_STATUS,
                    RSVP_QUESTION_STATUS);

            // when & then
            assertThatThrownBy(() -> domainService.applyManualForUnregistered(participant, event, true))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(EVENT_NOT_APPLICABLE_MEMBER_INFO_SATISFIED.getMessage());
        }
    }
}

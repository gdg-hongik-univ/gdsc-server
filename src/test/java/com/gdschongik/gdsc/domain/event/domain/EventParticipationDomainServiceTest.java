package com.gdschongik.gdsc.domain.event.domain;

import static com.gdschongik.gdsc.global.common.constant.EventConstant.*;
import static org.assertj.core.api.Assertions.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

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
    class 가입된_유저가_온라인으로_신청하는_경우 {

        @Test
        void 신청_기간이_아닌경우_실패한다() {
            // given
            Member member = fixtureHelper.createRegularMember(1L);
            AfterPartyApplicationStatus status = AfterPartyApplicationStatus.APPLIED;
            Event event = Event.create(
                    EVENT_NAME,
                    VENUE,
                    APPLICATION_DESCRIPTION,
                    EVENT_APPLICATION_PERIOD, // 신청 기간 (25년 3월 1일 ~ 3월 14일)
                    REGULAR_ROLE_ONLY_STATUS,
                    AFTER_PARTY_STATUS,
                    PRE_PAYMENT_STATUS,
                    POST_PAYMENT_STATUS,
                    RSVP_QUESTION_STATUS,
                    MAIN_EVENT_MAX_APPLICATION_COUNT,
                    AFTER_PARTY_MAX_APPLICATION_COUNT);
            LocalDateTime invalidDate = LocalDateTime.of(2025, 4, 1, 0, 0);

            // when & then
            assertThatThrownBy(() -> domainService.applyEventForRegistered(member, status, event, invalidDate))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(EVENT_NOT_APPLIABLE_APPLICATION_PERIOD_INVALID.getMessage());
        }

        @Test
        void 정회원만_참석_가능한_행사에_정회원이_아닌_유저가_신청하면_실패한다() {
            // given
            Member guestMember = fixtureHelper.createGuestMember(1L);
            AfterPartyApplicationStatus status = AfterPartyApplicationStatus.APPLIED;
            Event event = Event.create(
                    EVENT_NAME,
                    VENUE,
                    APPLICATION_DESCRIPTION,
                    EVENT_APPLICATION_PERIOD,
                    UsageStatus.ENABLED, // 정회원 전용 신청 폼
                    AFTER_PARTY_STATUS,
                    PRE_PAYMENT_STATUS,
                    POST_PAYMENT_STATUS,
                    RSVP_QUESTION_STATUS,
                    MAIN_EVENT_MAX_APPLICATION_COUNT,
                    AFTER_PARTY_MAX_APPLICATION_COUNT);
            LocalDateTime now = LocalDateTime.of(2025, 3, 1, 0, 0);

            // when & then
            assertThatThrownBy(() -> domainService.applyEventForRegistered(guestMember, status, event, now))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(EVENT_NOT_APPLIABLE_NOT_REGULAR_ROLE.getMessage());
        }

        @Test
        void 뒤풀이가_있는_행사에_뒤풀이_신청여부가_NONE이면_실패한다() {
            // given
            Member member = fixtureHelper.createRegularMember(1L);
            AfterPartyApplicationStatus noneStatus = AfterPartyApplicationStatus.NONE;
            Event event = Event.create(
                    EVENT_NAME,
                    VENUE,
                    APPLICATION_DESCRIPTION,
                    EVENT_APPLICATION_PERIOD,
                    REGULAR_ROLE_ONLY_STATUS,
                    UsageStatus.ENABLED, // 뒤풀이 활성화
                    PRE_PAYMENT_STATUS,
                    POST_PAYMENT_STATUS,
                    RSVP_QUESTION_STATUS,
                    MAIN_EVENT_MAX_APPLICATION_COUNT,
                    AFTER_PARTY_MAX_APPLICATION_COUNT);
            LocalDateTime now = LocalDateTime.of(2025, 3, 1, 0, 0);

            // when & then
            assertThatThrownBy(() -> domainService.applyEventForRegistered(member, noneStatus, event, now))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(EVENT_NOT_APPLIABLE_AFTER_PARTY_NONE.getMessage());
        }

        @Test
        void 뒤풀이가_없는_행사에_뒤풀이_신청을_하면_실패한다() {
            // given
            Member member = fixtureHelper.createRegularMember(1L);
            AfterPartyApplicationStatus appliedStatus = AfterPartyApplicationStatus.APPLIED;
            Event event = Event.create(
                    EVENT_NAME,
                    VENUE,
                    APPLICATION_DESCRIPTION,
                    EVENT_APPLICATION_PERIOD,
                    REGULAR_ROLE_ONLY_STATUS,
                    UsageStatus.DISABLED, // 뒤풀이 비활성화
                    UsageStatus.DISABLED,
                    UsageStatus.DISABLED,
                    RSVP_QUESTION_STATUS,
                    MAIN_EVENT_MAX_APPLICATION_COUNT,
                    AFTER_PARTY_MAX_APPLICATION_COUNT);
            LocalDateTime now = LocalDateTime.of(2025, 3, 1, 0, 0);

            // when & then
            assertThatThrownBy(() -> domainService.applyEventForRegistered(member, appliedStatus, event, now))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(EVENT_NOT_APPLIABLE_AFTER_PARTY_NOT_NONE.getMessage());
        }
    }
}

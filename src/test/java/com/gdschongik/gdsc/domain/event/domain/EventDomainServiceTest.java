package com.gdschongik.gdsc.domain.event.domain;

import static com.gdschongik.gdsc.global.common.constant.EventConstant.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.domain.event.domain.service.EventDomainService;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.helper.FixtureHelper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class EventDomainServiceTest {

    EventDomainService eventDomainService = new EventDomainService();
    FixtureHelper fixtureHelper = new FixtureHelper();

    @Nested
    class 이벤트_기본정보를_수정할_때 {

        @Test
        void 신청자가_존재하는데_정회원_전용_이벤트_여부를_변경하면_실패한다() {
            // given
            Event event = fixtureHelper.createEventWithAfterParty(1L, UsageStatus.DISABLED); // 정회원 전용 X

            UsageStatus newRegularRoleOnlyStatus = UsageStatus.ENABLED; // 정회원 전용 O
            long currentMainEventApplicants = 10; // 이미 신청한 인원
            long currentAfterPartyApplicants = 10;

            // when & then
            assertThatThrownBy(() -> eventDomainService.updateBasicInfo(
                            event,
                            EVENT_NAME,
                            VENUE,
                            EVENT_START_AT,
                            EVENT_APPLICATION_PERIOD,
                            newRegularRoleOnlyStatus,
                            null,
                            null,
                            currentMainEventApplicants,
                            currentAfterPartyApplicants))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(EVENT_NOT_UPDATABLE_ALREADY_EXISTS_APPLICANT.getMessage());
        }

        @Test
        void 현재_신청인원보다_최대_신청인원을_많게_변경하는_경우_성공한다() {
            // given
            Event event = fixtureHelper.createEventWithAfterParty(1L, REGULAR_ROLE_ONLY_STATUS);

            Integer newMainEventMaxApplicantCount = 15;
            Integer newAfterPartyMaxApplicantCount = 15;
            long currentMainEventApplicants = 10;
            long currentAfterPartyApplicants = 10;

            // when
            eventDomainService.updateBasicInfo(
                    event,
                    EVENT_NAME,
                    VENUE,
                    EVENT_START_AT,
                    EVENT_APPLICATION_PERIOD,
                    REGULAR_ROLE_ONLY_STATUS,
                    newMainEventMaxApplicantCount,
                    newAfterPartyMaxApplicantCount,
                    currentMainEventApplicants,
                    currentAfterPartyApplicants);

            // then
            assertThat(event.getMainEventMaxApplicantCount()).isEqualTo(newMainEventMaxApplicantCount);
        }

        @Test
        void 최대_신청인원_제한을_없애는_경우_성공한다() {
            // given
            Event event = fixtureHelper.createEventWithAfterParty(1L, REGULAR_ROLE_ONLY_STATUS);

            Integer newMainEventMaxApplicantCount = null;
            Integer newAfterPartyMaxApplicantCount = null;
            long currentMainEventApplicants = 10;
            long currentAfterPartyApplicants = 10;

            // when
            eventDomainService.updateBasicInfo(
                    event,
                    EVENT_NAME,
                    VENUE,
                    EVENT_START_AT,
                    EVENT_APPLICATION_PERIOD,
                    REGULAR_ROLE_ONLY_STATUS,
                    newMainEventMaxApplicantCount,
                    newAfterPartyMaxApplicantCount,
                    currentMainEventApplicants,
                    currentAfterPartyApplicants);

            // then
            assertThat(event.getMainEventMaxApplicantCount()).isNull();
        }

        @Test
        void 현재_신청인원보다_최대_신청인원을_적게_변경하는_경우_실패한다() {
            // given
            Event event = fixtureHelper.createEventWithAfterParty(1L, REGULAR_ROLE_ONLY_STATUS);

            Integer newMainEventMaxApplicantCount = 0;
            Integer newAfterPartyMaxApplicantCount = 0;
            long currentMainEventApplicants = 10;
            long currentAfterPartyApplicants = 10;

            // when & then
            assertThatThrownBy(() -> eventDomainService.updateBasicInfo(
                            event,
                            EVENT_NAME,
                            VENUE,
                            EVENT_START_AT,
                            EVENT_APPLICATION_PERIOD,
                            REGULAR_ROLE_ONLY_STATUS,
                            newMainEventMaxApplicantCount,
                            newAfterPartyMaxApplicantCount,
                            currentMainEventApplicants,
                            currentAfterPartyApplicants))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(EVENT_NOT_UPDATABLE_MAX_APPLICANT_COUNT_INVALID.getMessage());
        }
    }

    @Nested
    class 이벤트_폼_정보를_수정할_때 {

        @Test
        void 신청자가_존재하는데_수정을_시도하면_실패한다() {
            // given
            Event event = fixtureHelper.createEventWithAfterParty(1L, UsageStatus.DISABLED);
            boolean eventParticipationExists = true; // 이미 신청자가 존재

            // when & then
            assertThatThrownBy(() -> eventDomainService.updateFormInfo(
                            event,
                            APPLICATION_DESCRIPTION,
                            AFTER_PARTY_STATUS,
                            PRE_PAYMENT_STATUS,
                            POST_PAYMENT_STATUS,
                            RSVP_QUESTION_STATUS,
                            NOTICE_CONFIRM_QUESTION_STATUS,
                            eventParticipationExists))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(EVENT_NOT_UPDATABLE_ALREADY_EXISTS_APPLICANT.getMessage());
        }
    }

    @Nested
    class 참가자가_이벤트_정보를_조회할때 {

        @Test
        void 신청기간이_아닌데_조회하면_실패한다() {
            // given
            Event event = fixtureHelper.createEventWithAfterParty(1L, UsageStatus.DISABLED);
            long currentMainEventApplicantCount = 0;
            // 신청 기간 밖의 시간
            LocalDateTime now = EVENT_APPLICATION_PERIOD.getEndDate().plusDays(1);

            // when & then
            assertThatThrownBy(() ->
                            eventDomainService.validateParticipantViewable(event, now, currentMainEventApplicantCount))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(EVENT_NOT_VIEWABLE_OUTSIDE_APPLICATION_PERIOD.getMessage());
        }

        @Test
        void 최대_신청인원을_초과했으면_실패한다() {
            // given
            Event event = fixtureHelper.createEventWithAfterParty(1L, UsageStatus.DISABLED);
            long currentMainEventApplicantCount = MAIN_EVENT_MAX_APPLICATION_COUNT + 1; // 최대 신청 인원 초과
            LocalDateTime now = EVENT_APPLICATION_PERIOD.getStartDate();

            // when & then
            assertThatThrownBy(() ->
                            eventDomainService.validateParticipantViewable(event, now, currentMainEventApplicantCount))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(EVENT_NOT_VIEWABLE_MAX_APPLICANT_COUNT_EXCEEDED.getMessage());
        }
    }
}

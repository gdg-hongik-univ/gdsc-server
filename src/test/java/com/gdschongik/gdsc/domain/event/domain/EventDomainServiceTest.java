package com.gdschongik.gdsc.domain.event.domain;

import static com.gdschongik.gdsc.global.common.constant.EventConstant.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.domain.event.domain.service.EventDomainService;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.helper.FixtureHelper;
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
}

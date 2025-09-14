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
    class 이벤트를_수정할_때 {
        @Test
        void 현재_신청인원보다_최대_신청인원을_많게_변경하는_경우_성공한다() {
            // given
            Event event = fixtureHelper.createEventWithAfterParty(1L, REGULAR_ROLE_ONLY_STATUS);

            Integer newMainEventMaxApplicantCount = 15;
            Integer newAfterPartyMaxApplicantCount = 15;
            long currentMainEventApplicants = 10;
            long currentAfterPartyApplicants = 10;

            // when
            eventDomainService.update(
                    event,
                    EVENT_NAME,
                    VENUE,
                    EVENT_START_AT,
                    APPLICATION_DESCRIPTION,
                    EVENT_APPLICATION_PERIOD,
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
            eventDomainService.update(
                    event,
                    EVENT_NAME,
                    VENUE,
                    EVENT_START_AT,
                    APPLICATION_DESCRIPTION,
                    EVENT_APPLICATION_PERIOD,
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
            assertThatThrownBy(() -> eventDomainService.update(
                            event,
                            EVENT_NAME,
                            VENUE,
                            EVENT_START_AT,
                            APPLICATION_DESCRIPTION,
                            EVENT_APPLICATION_PERIOD,
                            newMainEventMaxApplicantCount,
                            newAfterPartyMaxApplicantCount,
                            currentMainEventApplicants,
                            currentAfterPartyApplicants))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(EVENT_NOT_UPDATABLE_MAX_APPLICANT_COUNT_INVALID.getMessage());
        }
    }
}

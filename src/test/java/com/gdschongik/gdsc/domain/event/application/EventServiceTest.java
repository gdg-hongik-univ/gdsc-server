package com.gdschongik.gdsc.domain.event.application;

import static com.gdschongik.gdsc.global.common.constant.EventConstant.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.domain.event.domain.Event;
import com.gdschongik.gdsc.domain.event.domain.UsageStatus;
import com.gdschongik.gdsc.domain.event.dto.request.EventCreateRequest;
import com.gdschongik.gdsc.domain.event.dto.request.EventUpdateBasicInfoRequest;
import com.gdschongik.gdsc.domain.event.dto.request.EventUpdateFormInfoRequest;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.helper.IntegrationTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class EventServiceTest extends IntegrationTest {

    @Autowired
    private EventService eventService;

    @Nested
    class 이벤트_생성시 {

        @Test
        void 성공한다() {
            // given
            var request = new EventCreateRequest(
                    EVENT_NAME,
                    VENUE,
                    EVENT_START_AT,
                    EVENT_DESCRIPTION,
                    EVENT_APPLICATION_PERIOD,
                    REGULAR_ROLE_ONLY_STATUS,
                    MAIN_EVENT_MAX_APPLICATION_COUNT,
                    AFTER_PARTY_MAX_APPLICATION_COUNT);

            // when & then
            assertThatCode(() -> eventService.createEvent(request)).doesNotThrowAnyException();
        }
    }

    @Nested
    class 이벤트_기본_정보_수정시 {

        @Test
        void 존재하지_않는_이벤트일_경우_실패한다() {
            // given
            Long invalidId = 999L;
            var request = new EventUpdateBasicInfoRequest(
                    EVENT_NAME,
                    VENUE,
                    EVENT_START_AT,
                    EVENT_DESCRIPTION,
                    EVENT_APPLICATION_PERIOD,
                    REGULAR_ROLE_ONLY_STATUS,
                    MAIN_EVENT_MAX_APPLICATION_COUNT,
                    AFTER_PARTY_MAX_APPLICATION_COUNT);

            // when & then
            assertThatThrownBy(() -> eventService.updateEventBasicInfo(invalidId, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(EVENT_NOT_FOUND.getMessage());
        }

        @Test
        void 성공한다() {
            // given
            Event event = createEvent();
            Long eventId = event.getId();

            String updatedName = "수정된 행사 이름";
            var request = new EventUpdateBasicInfoRequest(
                    updatedName,
                    VENUE,
                    EVENT_START_AT,
                    EVENT_DESCRIPTION,
                    EVENT_APPLICATION_PERIOD,
                    REGULAR_ROLE_ONLY_STATUS,
                    MAIN_EVENT_MAX_APPLICATION_COUNT,
                    AFTER_PARTY_MAX_APPLICATION_COUNT);

            // when
            eventService.updateEventBasicInfo(eventId, request);

            // then
            assertThat(eventRepository.findById(eventId).get().getName()).isEqualTo(updatedName);
        }
    }

    @Nested
    class 이벤트_폼_정보_수정시 {

        @Test
        void 존재하지_않는_이벤트일_경우_실패한다() {
            // given
            Long invalidId = 999L;
            var request = new EventUpdateFormInfoRequest(
                    AFTER_PARTY_STATUS,
                    PRE_PAYMENT_STATUS,
                    POST_PAYMENT_STATUS,
                    RSVP_QUESTION_STATUS,
                    NOTICE_CONFIRM_QUESTION_STATUS);

            // when & then
            assertThatThrownBy(() -> eventService.updateEventFormInfo(invalidId, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(EVENT_NOT_FOUND.getMessage());
        }

        @Test
        void 성공한다() {
            // given
            Event event = createEvent();
            Long eventId = event.getId();

            UsageStatus updatedRsvpQuestionStatus = UsageStatus.ENABLED;
            var request = new EventUpdateFormInfoRequest(
                    AFTER_PARTY_STATUS,
                    PRE_PAYMENT_STATUS,
                    POST_PAYMENT_STATUS,
                    updatedRsvpQuestionStatus,
                    NOTICE_CONFIRM_QUESTION_STATUS);

            // when
            eventService.updateEventFormInfo(eventId, request);

            // then
            assertThat(eventRepository.findById(eventId).get().getRsvpQuestionStatus())
                    .isEqualTo(updatedRsvpQuestionStatus);
        }
    }
}

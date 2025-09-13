package com.gdschongik.gdsc.domain.event.application;

import static com.gdschongik.gdsc.global.common.constant.EventConstant.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.domain.event.domain.Event;
import com.gdschongik.gdsc.domain.event.dto.request.EventCreateRequest;
import com.gdschongik.gdsc.domain.event.dto.request.EventUpdateRequest;
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
                    EVENT_APPLICATION_PERIOD,
                    REGULAR_ROLE_ONLY_STATUS,
                    MAIN_EVENT_MAX_APPLICATION_COUNT,
                    AFTER_PARTY_MAX_APPLICATION_COUNT);

            // when & then
            assertThatCode(() -> eventService.createEvent(request)).doesNotThrowAnyException();
        }
    }

    @Nested
    class 이벤트_수정시 {

        @Test
        void 존재하지_않는_이벤트일_경우_실패한다() {
            // given
            String updatedName = "수정된 행사 이름";
            var request = new EventUpdateRequest(
                    updatedName,
                    VENUE,
                    EVENT_START_AT,
                    APPLICATION_DESCRIPTION,
                    EVENT_APPLICATION_PERIOD,
                    MAIN_EVENT_MAX_APPLICATION_COUNT,
                    AFTER_PARTY_MAX_APPLICATION_COUNT);

            // when & then
            assertThatThrownBy(() -> eventService.updateEvent(1L, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(EVENT_NOT_FOUND.getMessage());
        }

        @Test
        void 성공한다() {
            // given
            Event event = createEvent();
            Long eventId = event.getId();

            String updatedName = "수정된 행사 이름";
            var request = new EventUpdateRequest(
                    updatedName,
                    VENUE,
                    EVENT_START_AT,
                    APPLICATION_DESCRIPTION,
                    EVENT_APPLICATION_PERIOD,
                    MAIN_EVENT_MAX_APPLICATION_COUNT,
                    AFTER_PARTY_MAX_APPLICATION_COUNT);

            // when
            eventService.updateEvent(eventId, request);

            // then
            assertThat(eventRepository.findById(eventId).get().getName()).isEqualTo(updatedName);
        }
    }
}

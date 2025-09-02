package com.gdschongik.gdsc.domain.event.application;

import static com.gdschongik.gdsc.global.common.constant.EventConstant.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.domain.event.dto.request.EventCreateRequest;
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
                    APPLICATION_DESCRIPTION,
                    EVENT_APPLICATION_PERIOD,
                    REGULAR_ROLE_ONLY_STATUS,
                    AFTER_PARTY_STATUS,
                    PRE_PAYMENT_STATUS,
                    POST_PAYMENT_STATUS,
                    RSVP_QUESTION_STATUS,
                    MAIN_EVENT_MAX_APPLICATION_COUNT,
                    AFTER_PARTY_MAX_APPLICATION_COUNT);

            // when & then
            assertThatCode(() -> eventService.createEvent(request)).doesNotThrowAnyException();
        }
    }
}

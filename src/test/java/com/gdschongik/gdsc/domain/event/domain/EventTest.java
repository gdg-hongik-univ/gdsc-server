package com.gdschongik.gdsc.domain.event.domain;

import static com.gdschongik.gdsc.global.common.constant.EventConstant.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class EventTest {

    @Nested
    class 행사_생성시 {

        @Test
        void 뒤풀이활성상태가_ENABLED인_이벤트가_생성된다() {
            // when
            Event event = Event.create(
                    EVENT_NAME,
                    VENUE,
                    EVENT_START_AT,
                    EVENT_APPLICATION_PERIOD,
                    REGULAR_ROLE_ONLY_STATUS,
                    MAIN_EVENT_MAX_APPLICATION_COUNT,
                    AFTER_PARTY_MAX_APPLICATION_COUNT);

            // then
            assertThat(event.getName()).isEqualTo(EVENT_NAME);
            assertThat(event.getVenue()).isEqualTo(VENUE);
            assertThat(event.getStartAt()).isEqualTo(EVENT_START_AT);
            assertThat(event.getApplicationPeriod()).isEqualTo(EVENT_APPLICATION_PERIOD);
            assertThat(event.getRegularRoleOnlyStatus()).isEqualTo(REGULAR_ROLE_ONLY_STATUS);
            assertThat(event.getAfterPartyStatus()).isEqualTo(UsageStatus.ENABLED);
            assertThat(event.getPrePaymentStatus()).isEqualTo(UsageStatus.DISABLED);
            assertThat(event.getPostPaymentStatus()).isEqualTo(UsageStatus.DISABLED);
            assertThat(event.getRsvpQuestionStatus()).isEqualTo(UsageStatus.DISABLED);
            assertThat(event.getNoticeConfirmQuestionStatus()).isEqualTo(UsageStatus.DISABLED);
            assertThat(event.getMainEventMaxApplicantCount()).isEqualTo(MAIN_EVENT_MAX_APPLICATION_COUNT);
            assertThat(event.getAfterPartyMaxApplicantCount()).isEqualTo(AFTER_PARTY_MAX_APPLICATION_COUNT);
        }
    }
}

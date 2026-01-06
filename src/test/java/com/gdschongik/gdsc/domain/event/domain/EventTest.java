package com.gdschongik.gdsc.domain.event.domain;

import static com.gdschongik.gdsc.domain.event.domain.UsageStatus.DISABLED;
import static com.gdschongik.gdsc.domain.event.domain.UsageStatus.ENABLED;
import static com.gdschongik.gdsc.global.common.constant.EventConstant.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.helper.FixtureHelper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class EventTest {

    private FixtureHelper fixtureHelper = new FixtureHelper();

    @Nested
    class 행사_생성시 {

        @Test
        void 뒤풀이활성상태가_ENABLED인_이벤트가_생성된다() {
            // when
            Event event = Event.create(
                    EVENT_NAME,
                    VENUE,
                    EVENT_START_AT,
                    EVENT_DESCRIPTION,
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
            assertThat(event.getAfterPartyStatus()).isEqualTo(ENABLED);
            assertThat(event.getPrePaymentStatus()).isEqualTo(DISABLED);
            assertThat(event.getPostPaymentStatus()).isEqualTo(DISABLED);
            assertThat(event.getRsvpQuestionStatus()).isEqualTo(DISABLED);
            assertThat(event.getNoticeConfirmQuestionStatus()).isEqualTo(DISABLED);
            assertThat(event.getMainEventMaxApplicantCount()).isEqualTo(MAIN_EVENT_MAX_APPLICATION_COUNT);
            assertThat(event.getAfterPartyMaxApplicantCount()).isEqualTo(AFTER_PARTY_MAX_APPLICATION_COUNT);
        }
    }

    @Nested
    class 폼_정보_수정시 {

        @Test
        void 뒤풀이가_비활성화인데_결제_관련_상태가_활성화되면_실패한다() {
            // given
            Event event = fixtureHelper.createEventWithAfterParty(1L, REGULAR_ROLE_ONLY_STATUS);

            // when & then
            assertThatThrownBy(() -> event.updateFormInfo(
                            DISABLED, // 뒤풀이 비활성화
                            ENABLED, // 사전 결제 활성화
                            ENABLED,
                            RSVP_QUESTION_STATUS,
                            NOTICE_CONFIRM_QUESTION_STATUS))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(EVENT_NOT_UPDATABLE_PAYMENT_STATUS_INVALID.getMessage());
        }

        @Test
        void 뒤풀이가_비활성화되면_뒤풀이_인원제한이_초기화된다() {
            // given
            Event event = fixtureHelper.createEventWithAfterParty(1L, REGULAR_ROLE_ONLY_STATUS);
            assertThat(event.getAfterPartyMaxApplicantCount()).isEqualTo(AFTER_PARTY_MAX_APPLICATION_COUNT);

            // when
            event.updateFormInfo(
                    DISABLED, // 뒤풀이 비활성화
                    DISABLED,
                    DISABLED,
                    RSVP_QUESTION_STATUS,
                    NOTICE_CONFIRM_QUESTION_STATUS);

            // then
            assertThat(event.getAfterPartyStatus()).isEqualTo(DISABLED);
            assertThat(event.getAfterPartyMaxApplicantCount()).isNull();
        }
    }
}

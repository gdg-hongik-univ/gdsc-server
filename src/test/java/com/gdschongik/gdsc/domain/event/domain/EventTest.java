package com.gdschongik.gdsc.domain.event.domain;

import static com.gdschongik.gdsc.global.common.constant.EventConstant.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.global.exception.CustomException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class EventTest {

    @Nested
    class 행사_생성시 {

        @Test
        void 뒤풀이가_없는_행사에_선입금_혹은_후정산이_활성화되면_실패한다() {
            // given
            UsageStatus afterPartyStatus = UsageStatus.DISABLED; // 뒤풀이 비활성화
            UsageStatus prePaymentStatus = UsageStatus.ENABLED; // 선입금 활성화
            UsageStatus postPaymentStatus = UsageStatus.ENABLED; // 후정산 활성화

            // when & then
            assertThatThrownBy(() -> Event.create(
                            EVENT_NAME,
                            VENUE,
                            APPLICATION_DESCRIPTION,
                            EVENT_APPLICATION_PERIOD,
                            REGULAR_ROLE_ONLY_STATUS,
                            afterPartyStatus,
                            prePaymentStatus,
                            postPaymentStatus,
                            RSVP_QUESTION_STATUS,
                            MAIN_EVENT_MAX_APPLICATION_COUNT,
                            AFTER_PARTY_MAX_APPLICATION_COUNT))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(EVENT_NOT_CREATABLE_PAYMENT_ENABLED.getMessage());
        }

        @Test
        void 선입금과_후정산이_둘_다_활성화되면_실패한다() {
            // given
            UsageStatus afterPartyStatus = UsageStatus.ENABLED; // 뒤풀이 활성화
            UsageStatus prePaymentStatus = UsageStatus.ENABLED; // 선입금 활성화
            UsageStatus postPaymentStatus = UsageStatus.ENABLED; // 후정산 활성화

            // when & then
            assertThatThrownBy(() -> Event.create(
                            EVENT_NAME,
                            VENUE,
                            APPLICATION_DESCRIPTION,
                            EVENT_APPLICATION_PERIOD,
                            REGULAR_ROLE_ONLY_STATUS,
                            afterPartyStatus,
                            prePaymentStatus,
                            postPaymentStatus,
                            RSVP_QUESTION_STATUS,
                            MAIN_EVENT_MAX_APPLICATION_COUNT,
                            AFTER_PARTY_MAX_APPLICATION_COUNT))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(EVENT_NOT_CREATABLE_PAYMENTS_BOTH_ENABLED.getMessage());
        }
    }
}

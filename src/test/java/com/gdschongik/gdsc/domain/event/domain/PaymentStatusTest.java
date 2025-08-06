package com.gdschongik.gdsc.domain.event.domain;

import static com.gdschongik.gdsc.global.common.constant.EventConstant.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.gdschongik.gdsc.helper.FixtureHelper;
import org.junit.jupiter.api.Test;

public class PaymentStatusTest {

    private FixtureHelper fixtureHelper = new FixtureHelper();

    @Test
    void 선입금이_활성화된_이벤트일_경우_선입금_기본값은_NOT_PAID이다() {
        // given
        Event event = fixtureHelper.createEvent(
                1L,
                UsageStatus.DISABLED,
                UsageStatus.ENABLED, // 뒤풀이 활성화
                UsageStatus.ENABLED, // 선입금 활성화
                UsageStatus.DISABLED,
                UsageStatus.DISABLED);

        // when
        PaymentStatus status = PaymentStatus.getInitialPrePaymentStatus(event);

        // then
        assertThat(status).isEqualTo(PaymentStatus.UNPAID);
    }

    @Test
    void 선입금이_비활성화된_이벤트일_경우_선입금_기본값은_NONE이다() {
        // given
        Event event = fixtureHelper.createEvent(
                1L,
                UsageStatus.DISABLED,
                UsageStatus.ENABLED, // 뒤풀이 활성화
                UsageStatus.DISABLED, // 선입금 비활성화
                UsageStatus.DISABLED,
                UsageStatus.DISABLED);

        // when
        PaymentStatus status = PaymentStatus.getInitialPrePaymentStatus(event);

        // then
        assertThat(status).isEqualTo(PaymentStatus.NONE);
    }

    @Test
    void 후정산이_활성화된_이벤트일_경우_후정산_기본값은_NOT_PAID이다() {
        // given
        Event event = fixtureHelper.createEvent(
                1L,
                UsageStatus.DISABLED,
                UsageStatus.ENABLED, // 뒤풀이 활성화
                UsageStatus.DISABLED,
                UsageStatus.ENABLED, // 후정산 활성화
                UsageStatus.DISABLED);

        // when
        PaymentStatus status = PaymentStatus.getInitialPostPaymentStatus(event);

        // then
        assertThat(status).isEqualTo(PaymentStatus.UNPAID);
    }

    @Test
    void 후정산이_비활성화된_이벤트일_경우_후정산_기본값은_NONE이다() {
        // given
        Event event = fixtureHelper.createEvent(
                1L,
                UsageStatus.DISABLED,
                UsageStatus.ENABLED, // 뒤풀이 활성화
                UsageStatus.DISABLED,
                UsageStatus.DISABLED, // 후정산 비활성화
                UsageStatus.DISABLED);

        // when
        PaymentStatus status = PaymentStatus.getInitialPostPaymentStatus(event);

        // then
        assertThat(status).isEqualTo(PaymentStatus.NONE);
    }
}

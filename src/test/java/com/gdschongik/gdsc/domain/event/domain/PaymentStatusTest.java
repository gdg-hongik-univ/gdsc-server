package com.gdschongik.gdsc.domain.event.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.gdschongik.gdsc.helper.FixtureHelper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class PaymentStatusTest {

    private FixtureHelper fixtureHelper = new FixtureHelper();

    @Test
    void 선입금이_활성화된_이벤트일_경우_선입금_기본값은_NOT_PAID이다() {
        // given
        Event event = createEventWithPrePaymentEnabled();

        // when
        PaymentStatus status = PaymentStatus.getInitialPrePaymentStatus(event);

        // then
        assertThat(status).isEqualTo(PaymentStatus.UNPAID);
    }

    @Test
    void 선입금이_비활성화된_이벤트일_경우_선입금_기본값은_NONE이다() {
        // given
        Event event = fixtureHelper.createEventWithAfterParty(1L, UsageStatus.DISABLED);

        // when
        PaymentStatus status = PaymentStatus.getInitialPrePaymentStatus(event);

        // then
        assertThat(status).isEqualTo(PaymentStatus.NONE);
    }

    @Test
    void 후정산이_활성화된_이벤트일_경우_후정산_기본값은_NOT_PAID이다() {
        // given
        Event event = createEventWithPostPaymentEnabled();

        // when
        PaymentStatus status = PaymentStatus.getInitialPostPaymentStatus(event);

        // then
        assertThat(status).isEqualTo(PaymentStatus.UNPAID);
    }

    @Test
    void 후정산이_비활성화된_이벤트일_경우_후정산_기본값은_NONE이다() {
        // given
        Event event = fixtureHelper.createEventWithAfterParty(1L, UsageStatus.DISABLED);

        // when
        PaymentStatus status = PaymentStatus.getInitialPostPaymentStatus(event);

        // then
        assertThat(status).isEqualTo(PaymentStatus.NONE);
    }

    private Event createEventWithPrePaymentEnabled() {
        Event event = fixtureHelper.createEventWithAfterParty(1L, UsageStatus.DISABLED);
        ReflectionTestUtils.setField(event, "prePaymentStatus", UsageStatus.ENABLED);
        return event;
    }

    private Event createEventWithPostPaymentEnabled() {
        Event event = fixtureHelper.createEventWithAfterParty(1L, UsageStatus.DISABLED);
        ReflectionTestUtils.setField(event, "postPaymentStatus", UsageStatus.ENABLED);
        return event;
    }
}

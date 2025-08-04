package com.gdschongik.gdsc.domain.event.domain;

import static com.gdschongik.gdsc.global.common.constant.EventConstant.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class AfterPartyAttendanceStatusTest {

    @Test
    void 뒤풀이가_없는_이벤트는_초기값이_NONE이다() {
        // given
        Event event = Event.create(
                EVENT_NAME,
                VENUE,
                APPLICATION_DESCRIPTION,
                EVENT_APPLICATION_PERIOD,
                UsageStatus.DISABLED,
                UsageStatus.DISABLED, // 뒤풀이 비활성화
                UsageStatus.DISABLED,
                UsageStatus.DISABLED,
                UsageStatus.DISABLED,
                MAIN_EVENT_MAX_APPLICATION_COUNT,
                AFTER_PARTY_MAX_APPLICATION_COUNT);

        // when
        AfterPartyAttendanceStatus status = AfterPartyAttendanceStatus.getInitialStatus(event);

        // then
        assertThat(status).isEqualTo(AfterPartyAttendanceStatus.NONE);
    }

    @Test
    void 뒤풀이가_있는_이벤트는_초기값이_NOT_ATTENDED이다() {
        // given
        Event event = Event.create(
                EVENT_NAME,
                VENUE,
                APPLICATION_DESCRIPTION,
                EVENT_APPLICATION_PERIOD,
                UsageStatus.DISABLED,
                UsageStatus.ENABLED, // 뒤풀이 활성화
                UsageStatus.DISABLED,
                UsageStatus.DISABLED,
                UsageStatus.DISABLED,
                MAIN_EVENT_MAX_APPLICATION_COUNT,
                AFTER_PARTY_MAX_APPLICATION_COUNT);

        // when
        AfterPartyAttendanceStatus status = AfterPartyAttendanceStatus.getInitialStatus(event);

        // then
        assertThat(status).isEqualTo(AfterPartyAttendanceStatus.NOT_ATTENDED);
    }
}

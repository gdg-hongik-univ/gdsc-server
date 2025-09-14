package com.gdschongik.gdsc.domain.event.domain;

import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.helper.FixtureHelper;
import org.junit.jupiter.api.Test;

public class AfterPartyAttendanceStatusTest {

    private FixtureHelper fixtureHelper = new FixtureHelper();

    @Test
    void 뒤풀이가_없는_이벤트는_초기값이_NONE이다() {
        // given
        Event event = fixtureHelper.createEventWithoutAfterParty(1L);

        // when
        AfterPartyAttendanceStatus status = AfterPartyAttendanceStatus.getInitialStatus(event);

        // then
        assertThat(status).isEqualTo(AfterPartyAttendanceStatus.NONE);
    }

    @Test
    void 뒤풀이가_있는_이벤트는_초기값이_NOT_ATTENDED이다() {
        // given
        Event event = fixtureHelper.createEventWithAfterParty(1L, UsageStatus.DISABLED);

        // when
        AfterPartyAttendanceStatus status = AfterPartyAttendanceStatus.getInitialStatus(event);

        // then
        assertThat(status).isEqualTo(AfterPartyAttendanceStatus.NOT_ATTENDED);
    }
}

package com.gdschongik.gdsc.domain.event.domain;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.domain.event.domain.service.EventDomainService;
import com.gdschongik.gdsc.global.exception.CustomException;
import org.junit.jupiter.api.Test;

public class EventDomainServiceTest {

    EventDomainService eventDomainService = new EventDomainService();

    @Test
    void 현재_신청인원보다_최대_신청인원을_많게_변경하는_경우_성공한다() {
        // given
        int currentApplicants = 10;
        Integer newMaxApplicantCount = 15;

        // when & then
        assertThatCode(() ->
                        eventDomainService.validateWhenUpdateMaxApplicantCount(currentApplicants, newMaxApplicantCount))
                .doesNotThrowAnyException();
    }

    @Test
    void 최대_신청인원_제한을_없애는_경우_성공한다() {
        // given
        int currentApplicants = 10;
        Integer newMaxApplicantCount = null;

        // when & then
        assertThatCode(() ->
                        eventDomainService.validateWhenUpdateMaxApplicantCount(currentApplicants, newMaxApplicantCount))
                .doesNotThrowAnyException();
    }

    @Test
    void 현재_신청인원보다_최대_신청인원을_적게_변경하는_경우_실패한다() {
        // given
        int currentApplicants = 10;
        Integer newMaxApplicantCount = 5;

        // when & then
        assertThatThrownBy(() ->
                        eventDomainService.validateWhenUpdateMaxApplicantCount(currentApplicants, newMaxApplicantCount))
                .isInstanceOf(CustomException.class)
                .hasMessage(EVENT_NOT_UPDATABLE_MAX_APPLICANT_COUNT_INVALID.getMessage());
    }
}

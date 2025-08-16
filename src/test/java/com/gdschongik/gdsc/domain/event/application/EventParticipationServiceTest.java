package com.gdschongik.gdsc.domain.event.application;

import static com.gdschongik.gdsc.global.common.constant.EventConstant.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.domain.event.dao.EventParticipationRepository;
import com.gdschongik.gdsc.domain.event.dao.EventRepository;
import com.gdschongik.gdsc.domain.event.domain.AfterPartyApplicationStatus;
import com.gdschongik.gdsc.domain.event.domain.AfterPartyAttendanceStatus;
import com.gdschongik.gdsc.domain.event.domain.Event;
import com.gdschongik.gdsc.domain.event.domain.EventParticipation;
import com.gdschongik.gdsc.domain.event.domain.PaymentStatus;
import com.gdschongik.gdsc.domain.event.dto.request.EventParticipantQueryOption;
import com.gdschongik.gdsc.domain.event.dto.response.EventApplicantResponse;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.helper.IntegrationTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

class EventParticipationServiceTest extends IntegrationTest {

    @Autowired
    private EventParticipationService eventParticipationService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventParticipationRepository eventParticipationRepository;

    @Nested
    class 이벤트_신청자_목록_조회할때 {

        @Test
        void 이벤트_ID가_일치하는_신청자만_조회된다() {
            // given
            Event event1 = createEvent();
            Event event2 = createEvent();

            Member member1 = createMember("C000001", "김홍익1");
            Member member2 = createMember("C000002", "김홍익2");
            Member member3 = createMember("B000001", "김홍익3");

            // event1에 신청한 사용자들
            createEventParticipation(event1, member1);
            createEventParticipation(event1, member2);

            // event2에 신청한 사용자
            createEventParticipation(event2, member3);

            EventParticipantQueryOption queryOption = new EventParticipantQueryOption(null, null, null);

            // when
            Page<EventApplicantResponse> result =
                    eventParticipationService.getEventApplicants(event1.getId(), queryOption, PageRequest.of(0, 10));

            // then
            assertThat(result.getContent())
                    .hasSize(2)
                    .extracting(response -> response.participant().getStudentId())
                    .containsExactlyInAnyOrder("C000001", "C000002");
        }

        @Test
        void 학번_키워드를_포함하는_신청자만_조회된다() {
            // given
            Event event = createEvent();

            Member memberC1 = createMember("C000001", "김홍익1");
            Member memberC2 = createMember("C000002", "김홍익2");
            Member memberB1 = createMember("B000001", "김홍익3");
            Member memberB2 = createMember("B000002", "김홍익4");

            createEventParticipation(event, memberC1);
            createEventParticipation(event, memberC2);
            createEventParticipation(event, memberB1);
            createEventParticipation(event, memberB2);

            EventParticipantQueryOption queryOption = new EventParticipantQueryOption(null, "C", null);

            // when
            Page<EventApplicantResponse> result =
                    eventParticipationService.getEventApplicants(event.getId(), queryOption, PageRequest.of(0, 10));

            // then
            assertThat(result.getContent())
                    .hasSize(2)
                    .extracting(response -> response.participant().getStudentId())
                    .containsExactlyInAnyOrder("C000001", "C000002");
        }

        @Test
        void createdAt_오름차순으로_정렬된다() {
            // given
            Event event = createEvent();

            Member member1 = createMember("C000001", "김홍익1");
            Member member2 = createMember("C000002", "김홍익2");
            Member member3 = createMember("C000003", "김홍익3");

            // (3, 1, 2) 순서로 생성
            EventParticipation participation3 = createEventParticipation(event, member3);
            EventParticipation participation1 = createEventParticipation(event, member1);
            EventParticipation participation2 = createEventParticipation(event, member2);

            EventParticipantQueryOption queryOption = new EventParticipantQueryOption(null, null, null);
            PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("createdAt").ascending());

            // when
            Page<EventApplicantResponse> result =
                    eventParticipationService.getEventApplicants(event.getId(), queryOption, pageRequest);

            // then - (3, 1, 2) 순서로 조회되는지 확인
            assertThat(result.getContent())
                    .hasSize(3)
                    .extracting(EventApplicantResponse::eventParticipationId)
                    .containsExactly(participation3.getId(), participation1.getId(), participation2.getId());
        }

        @Test
        void 지원하지_않는_정렬_조건이면_예외가_발생한다() {
            // given
            Event event = createEvent();

            EventParticipantQueryOption queryOption = new EventParticipantQueryOption(null, null, null);
            PageRequest pageRequest =
                    PageRequest.of(0, 10, Sort.by("invalidField").ascending());

            // when & then
            assertThatThrownBy(
                            () -> eventParticipationService.getEventApplicants(event.getId(), queryOption, pageRequest))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(SORT_NOT_SUPPORTED.getMessage());
        }
    }

    private Event createEvent() {
        Event event = Event.create(
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
        return eventRepository.save(event);
    }

    private Member createMember(String studentId, String name) {
        Member member = createAssociateMember();
        ReflectionTestUtils.setField(member, "studentId", studentId);
        ReflectionTestUtils.setField(member, "name", name);
        return memberRepository.save(member);
    }

    private EventParticipation createEventParticipation(Event event, Member member) {
        EventParticipation eventParticipation = EventParticipation.createOnlineForRegistered(
                member,
                AfterPartyApplicationStatus.NOT_APPLIED,
                AfterPartyAttendanceStatus.NOT_ATTENDED,
                PaymentStatus.NONE,
                PaymentStatus.NONE,
                event);
        return eventParticipationRepository.save(eventParticipation);
    }
}

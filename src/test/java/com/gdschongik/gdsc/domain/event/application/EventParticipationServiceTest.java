package com.gdschongik.gdsc.domain.event.application;

import static com.gdschongik.gdsc.domain.event.domain.AfterPartyAttendanceStatus.*;
import static com.gdschongik.gdsc.domain.event.domain.UsageStatus.DISABLED;
import static com.gdschongik.gdsc.global.common.constant.EventConstant.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.domain.event.dao.EventParticipationRepository;
import com.gdschongik.gdsc.domain.event.dao.EventRepository;
import com.gdschongik.gdsc.domain.event.domain.AfterPartyApplicationStatus;
import com.gdschongik.gdsc.domain.event.domain.AfterPartyAttendanceStatus;
import com.gdschongik.gdsc.domain.event.domain.Event;
import com.gdschongik.gdsc.domain.event.domain.EventParticipation;
import com.gdschongik.gdsc.domain.event.domain.Participant;
import com.gdschongik.gdsc.domain.event.domain.ParticipantRole;
import com.gdschongik.gdsc.domain.event.domain.PaymentStatus;
import com.gdschongik.gdsc.domain.event.dto.request.AfterPartyAttendRequest;
import com.gdschongik.gdsc.domain.event.dto.request.EventParticipantQueryOption;
import com.gdschongik.gdsc.domain.event.dto.request.EventParticipationDeleteRequest;
import com.gdschongik.gdsc.domain.event.dto.response.EventApplicantResponse;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.helper.IntegrationTest;
import java.util.List;
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

        @Test
        void 비회원_신청자가_조회된다() {
            // given
            Event event = createEvent();
            Participant unregisteredParticipant = Participant.of("김홍익", "B000001", "01012345678");

            // 비회원 신청
            createUnregisteredEventParticipation(event, unregisteredParticipant);

            EventParticipantQueryOption queryOption = new EventParticipantQueryOption(null, null, null);

            // when
            Page<EventApplicantResponse> result =
                    eventParticipationService.getEventApplicants(event.getId(), queryOption, PageRequest.of(0, 10));

            // then
            EventApplicantResponse unregisteredResponse = result.getContent().get(0);
            assertThat(unregisteredResponse.participantRole()).isEqualTo(ParticipantRole.NON_MEMBER);
            assertThat(unregisteredResponse.discordUsername()).isNull();
            assertThat(unregisteredResponse.nickname()).isNull();
        }
    }

    @Nested
    class 뒤풀이_참석_처리할때 {
        @Test
        void 신청자의_뒤풀이_참석_상태가_ATTENDED가_된다() {
            // given
            Event event = createEvent();
            Member member = createMember();
            EventParticipation eventParticipation = createEventParticipation(event, member);
            AfterPartyAttendRequest request = new AfterPartyAttendRequest(List.of(eventParticipation.getId()));

            // when
            eventParticipationService.attendAfterParty(request);

            // then
            EventParticipation afterPartyAttended = eventParticipationRepository
                    .findById(eventParticipation.getId())
                    .get();
            assertThat(afterPartyAttended.getAfterPartyAttendanceStatus()).isEqualTo(ATTENDED);
        }

        @Test
        void 뒤풀이가_비활성_상태라면_예외가_발생한다() {
            // given
            Event event = createAfterPartyDisabledEvent();
            Member member = createMember();
            EventParticipation eventParticipation = createEventParticipation(event, member);
            AfterPartyAttendRequest request = new AfterPartyAttendRequest(List.of(eventParticipation.getId()));

            // when & then
            assertThatThrownBy(() -> eventParticipationService.attendAfterParty(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(EVENT_AFTER_PARTY_DISABLED.getMessage());
        }
    }

    @Nested
    class 이벤트_신청_정보_삭제시 {

        @Test
        void 성공한다() {
            // given
            Event event = createEvent();
            Member member = createMember("C00000", "김홍익");
            createEventParticipation(event, member);

            var request = new EventParticipationDeleteRequest(List.of(1L));
            // when
            eventParticipationService.deleteEventParticipations(request);

            // then
            assertThat(eventParticipationRepository.findAll()).isEmpty(); // 신청 정보가 삭제된다
        }

        @Test
        void 존재하지_않는_신청자_ID가_포함된_경우_예외가_발생한다() {
            // given
            Event event = createEvent();
            Member member = createMember("C000001", "김홍익");
            createEventParticipation(event, member);

            var request = new EventParticipationDeleteRequest(List.of(1L, 999L)); // 999L은 존재하지 않는 ID

            // when & then
            assertThatThrownBy(() -> eventParticipationService.deleteEventParticipations(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(PARTICIPATION_NOT_DELETABLE_INVALID_IDS.getMessage());
        }

        @Test
        void 중복된_ID가_있는_경우_예외가_발생한다() {
            // given
            Event event = createEvent();
            Member member1 = createMember("C000001", "김홍익1");
            Member member2 = createMember("C000002", "김홍익2");

            createEventParticipation(event, member1);
            createEventParticipation(event, member2);

            var request = new EventParticipationDeleteRequest(List.of(1L, 1L)); // 중복된 ID

            // when & then
            assertThatThrownBy(() -> eventParticipationService.deleteEventParticipations(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(PARTICIPATION_NOT_DELETABLE_INVALID_IDS.getMessage());
        }
    }

    @Nested
    class 뒤풀이_정산_처리할_때 {
        @Test
        void 신청자의_뒤풀이_정산_상태가_PAID가_된다() {
            // given
            Event event = createEvent();
            Member member = createMember();
            EventParticipation eventParticipation = createEventParticipation(event, member);

            // when
            eventParticipationService.checkPostPayment(eventParticipation.getId());

            // then
            EventParticipation afterPartyPostPaid = eventParticipationRepository
                    .findById(eventParticipation.getId())
                    .get();
            assertThat(afterPartyPostPaid.getPostPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        }

        @Test
        void 뒤풀이가_비활성화_상태라면_에러가_발생한다() {
            // given
            Event event = createAfterPartyDisabledEvent();
            Member member = createMember();
            EventParticipation eventParticipation = createEventParticipation(event, member);

            // when & then
            assertThatThrownBy(() -> eventParticipationService.checkPostPayment(eventParticipation.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(EVENT_AFTER_PARTY_DISABLED.getMessage());
        }

        @Test
        void 존재하지_않는_참여정보라면_에러가_발생한다() {
            // when & then
            assertThatThrownBy(() -> eventParticipationService.checkPostPayment(9999L))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(PARTICIPATION_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 뒤풀이_전체_정산_처리할_때 {
        @Test
        void 모든_신청자의_뒤풀이_정산_상태가_PAID가_된다() {
            // given
            Event event = createEvent();
            Member member1 = createMember("C000001", "김홍익1");
            Member member2 = createMember("C000002", "김홍익2");
            createEventParticipation(event, member1);
            createEventParticipation(event, member2);

            // when
            eventParticipationService.checkAllPostPayment(event.getId());

            // then
            List<EventParticipation> eventParticipations = eventParticipationRepository.findAllByEvent(event);
            eventParticipations.forEach(eventParticipation -> {
                assertThat(eventParticipation.getPostPaymentStatus()).isEqualTo(PaymentStatus.PAID);
            });
        }

        @Test
        void 뒤풀이가_비활성화_상태라면_에러가_발생한다() {
            // given
            Event event = createAfterPartyDisabledEvent();

            // when & then
            assertThatThrownBy(() -> eventParticipationService.checkAllPostPayment(event.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(EVENT_AFTER_PARTY_DISABLED.getMessage());
        }

        @Test
        void 존재하지_않는_이벤트라면_에러가_발생한다() {
            // when & then
            assertThatThrownBy(() -> eventParticipationService.checkAllPostPayment(9999L))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(EVENT_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 뒤풀이_정산_처리_취소할_때 {
        @Test
        void 신청자의_뒤풀이_정산_상태가_UNPAID가_된다() {
            // given
            Event event = createEvent();
            Member member = createMember();
            EventParticipation eventParticipation = createPostPaidEventParticipation(event, member);

            // when
            eventParticipationService.uncheckPostPayment(eventParticipation.getId());

            // then
            EventParticipation afterPartyPostPaid = eventParticipationRepository
                    .findById(eventParticipation.getId())
                    .get();
            assertThat(afterPartyPostPaid.getPostPaymentStatus()).isEqualTo(PaymentStatus.UNPAID);
        }

        @Test
        void 뒤풀이가_비활성화_상태라면_에러가_발생한다() {
            // given
            Event event = createAfterPartyDisabledEvent();
            Member member = createMember();
            EventParticipation eventParticipation = createEventParticipation(event, member);

            // when & then
            assertThatThrownBy(() -> eventParticipationService.uncheckPostPayment(eventParticipation.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(EVENT_AFTER_PARTY_DISABLED.getMessage());
        }

        @Test
        void 존재하지_않는_참여정보라면_에러가_발생한다() {
            // when & then
            assertThatThrownBy(() -> eventParticipationService.uncheckPostPayment(9999L))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(PARTICIPATION_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 뒤풀이_전체_정산_취소할_때 {
        @Test
        void 모든_신청자의_뒤풀이_정산_상태가_UNPAID가_된다() {
            // given
            Event event = createEvent();
            Member member1 = createMember("C000001", "김홍익1");
            Member member2 = createMember("C000002", "김홍익2");
            createPostPaidEventParticipation(event, member1);
            createPostPaidEventParticipation(event, member2);

            // when
            eventParticipationService.uncheckAllPostPayment(event.getId());

            // then
            List<EventParticipation> eventParticipations = eventParticipationRepository.findAllByEvent(event);
            eventParticipations.forEach(eventParticipation -> {
                assertThat(eventParticipation.getPostPaymentStatus()).isEqualTo(PaymentStatus.UNPAID);
            });
        }

        @Test
        void 뒤풀이가_비활성화_상태라면_에러가_발생한다() {
            // given
            Event event = createAfterPartyDisabledEvent();

            // when & then
            assertThatThrownBy(() -> eventParticipationService.checkAllPostPayment(event.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(EVENT_AFTER_PARTY_DISABLED.getMessage());
        }

        @Test
        void 존재하지_않는_이벤트라면_에러가_발생한다() {
            // when & then
            assertThatThrownBy(() -> eventParticipationService.checkAllPostPayment(9999L))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(EVENT_NOT_FOUND.getMessage());
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

    private Event createAfterPartyDisabledEvent() {
        Event event = Event.create(
                EVENT_NAME,
                VENUE,
                EVENT_START_AT,
                APPLICATION_DESCRIPTION,
                EVENT_APPLICATION_PERIOD,
                REGULAR_ROLE_ONLY_STATUS,
                DISABLED,
                DISABLED,
                DISABLED,
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

    private EventParticipation createPostPaidEventParticipation(Event event, Member member) {
        EventParticipation eventParticipation = EventParticipation.createOnlineForRegistered(
                member,
                AfterPartyApplicationStatus.NOT_APPLIED,
                AfterPartyAttendanceStatus.NOT_ATTENDED,
                PaymentStatus.NONE,
                PaymentStatus.PAID,
                event);
        return eventParticipationRepository.save(eventParticipation);
    }

    private EventParticipation createUnregisteredEventParticipation(Event event, Participant participant) {
        EventParticipation eventParticipation = EventParticipation.createOnlineForUnregistered(
                participant,
                AfterPartyApplicationStatus.NOT_APPLIED,
                AfterPartyAttendanceStatus.NOT_ATTENDED,
                PaymentStatus.NONE,
                PaymentStatus.NONE,
                event);
        return eventParticipationRepository.save(eventParticipation);
    }
}

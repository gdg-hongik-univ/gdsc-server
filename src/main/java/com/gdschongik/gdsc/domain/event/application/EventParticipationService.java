package com.gdschongik.gdsc.domain.event.application;

import com.gdschongik.gdsc.domain.event.dao.EventParticipationRepository;
import com.gdschongik.gdsc.domain.event.dao.EventRepository;
import com.gdschongik.gdsc.domain.event.domain.Event;
import com.gdschongik.gdsc.domain.event.domain.EventParticipation;
import com.gdschongik.gdsc.domain.event.domain.EventParticipationDomainService;
import com.gdschongik.gdsc.domain.event.dto.dto.EventParticipableMemberDto;
import com.gdschongik.gdsc.domain.event.dto.request.AfterPartyPostPaymentCheckRequest;
import com.gdschongik.gdsc.domain.event.dto.request.EventParticipantQueryOption;
import com.gdschongik.gdsc.domain.event.dto.response.EventApplicantResponse;
import com.gdschongik.gdsc.domain.member.dao.MemberRepository;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.global.exception.ErrorCode;
import java.util.List;
import java.util.function.Predicate;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventParticipationService {

    private final EventRepository eventRepository;
    private final EventParticipationRepository eventParticipationRepository;
    private final MemberRepository memberRepository;
    private final EventParticipationDomainService eventParticipationDomainService;

    @Transactional(readOnly = true)
    public Page<EventApplicantResponse> getEventApplicants(
            Long eventId, EventParticipantQueryOption queryOption, Pageable pageable) {
        return eventParticipationRepository.findEventApplicants(eventId, queryOption, pageable);
    }

    public List<EventParticipableMemberDto> searchParticipableMembers(Long eventId, String name) {
        Event event =
                eventRepository.findById(eventId).orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));

        List<Member> membersBySameName = memberRepository.findAllByName(name);

        List<Long> memberIds = membersBySameName.stream().map(Member::getId).toList();

        List<EventParticipation> participations =
                eventParticipationRepository.findAllByEventAndMemberIdIn(event, memberIds);

        return membersBySameName.stream()
                .filter(isThisMemberAllowedToParticipate(event))
                .map(member -> EventParticipableMemberDto.from(member, isNotAppliedToEvent(participations, member)))
                .toList();
    }

    @Transactional
    public void checkPostPayment(@Valid AfterPartyPostPaymentCheckRequest request) {
        List<Long> eventParticipationIds = request.eventParticipationIds();
        List<EventParticipation> eventParticipations = eventParticipationRepository.findAllById(eventParticipationIds);
        Event event = eventParticipations.get(0).getEvent();

        eventParticipationDomainService.validateAfterPartyEnabled(event);

        eventParticipations.forEach(EventParticipation::checkPostPayment);

        log.info("[EventParticipationService] 뒤풀이 정산 처리: eventParticipationIds={}", eventParticipationIds);
    }

    private static Predicate<Member> isThisMemberAllowedToParticipate(Event event) {
        return switch (event.getRegularRoleOnlyStatus()) {
            case ENABLED -> Member::isRegular;
            case DISABLED -> always -> true;
        };
    }

    private static boolean isNotAppliedToEvent(List<EventParticipation> participations, Member member) {
        return participations.stream()
                .noneMatch(participation -> participation.getMemberId().equals(member.getId()));
    }
}

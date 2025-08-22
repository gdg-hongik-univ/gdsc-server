package com.gdschongik.gdsc.domain.event.application;

import com.gdschongik.gdsc.domain.event.dao.EventParticipationRepository;
import com.gdschongik.gdsc.domain.event.dao.EventRepository;
import com.gdschongik.gdsc.domain.event.domain.Event;
import com.gdschongik.gdsc.domain.event.domain.EventParticipation;
import com.gdschongik.gdsc.domain.event.domain.EventParticipationDomainService;
import com.gdschongik.gdsc.domain.event.dto.dto.EventParticipableMemberDto;
import com.gdschongik.gdsc.domain.event.dto.request.AfterPartyAttendRequest;
import com.gdschongik.gdsc.domain.event.dto.request.EventParticipantQueryOption;
import com.gdschongik.gdsc.domain.event.dto.response.EventApplicantResponse;
import com.gdschongik.gdsc.domain.member.dao.MemberRepository;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.global.exception.ErrorCode;
import com.gdschongik.gdsc.global.util.MemberUtil;
import java.util.List;
import java.util.function.Predicate;
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
    private final MemberUtil memberUtil;
    private final EventParticipationDomainService eventParticipationDomainService;
    private final EventParticipationRepository eventParticipationRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public Page<EventApplicantResponse> getEventApplicants(
            Long eventId, EventParticipantQueryOption queryOption, Pageable pageable) {
        return eventParticipationRepository.findEventApplicants(eventId, queryOption, pageable);
    }

    @Transactional(readOnly = true)
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
    public void attendAfterParty(AfterPartyAttendRequest request) {
        Member currentMember = memberUtil.getCurrentMember();
        eventParticipationDomainService.validateAdminPermission(currentMember);

        List<Long> eventParticipationIds = request.eventParticipationIds();
        eventParticipationRepository.findAllById(eventParticipationIds).forEach(EventParticipation::attendAfterParty);

        log.info("[EventParticipationService] 뒤풀이 참석 처리: eventParticipationIds={}", eventParticipationIds);
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

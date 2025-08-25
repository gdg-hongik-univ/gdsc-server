package com.gdschongik.gdsc.domain.event.application;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.event.dao.EventParticipationRepository;
import com.gdschongik.gdsc.domain.event.dao.EventRepository;
import com.gdschongik.gdsc.domain.event.domain.Event;
import com.gdschongik.gdsc.domain.event.domain.EventParticipation;
import com.gdschongik.gdsc.domain.event.domain.EventParticipationDomainService;
import com.gdschongik.gdsc.domain.event.dto.dto.EventParticipableMemberDto;
import com.gdschongik.gdsc.domain.event.dto.dto.EventParticipationDto;
import com.gdschongik.gdsc.domain.event.dto.request.AfterPartyAttendRequest;
import com.gdschongik.gdsc.domain.event.dto.request.AfterPartyPostPaymentCheckRequest;
import com.gdschongik.gdsc.domain.event.dto.request.AfterPartyPostPaymentUncheckRequest;
import com.gdschongik.gdsc.domain.event.dto.request.EventParticipantQueryOption;
import com.gdschongik.gdsc.domain.event.dto.request.EventParticipationDeleteRequest;
import com.gdschongik.gdsc.domain.event.dto.response.AfterPartyAttendanceResponse;
import com.gdschongik.gdsc.domain.event.dto.response.EventApplicantResponse;
import com.gdschongik.gdsc.domain.member.dao.MemberRepository;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.global.exception.CustomException;
import jakarta.validation.Valid;
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
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new CustomException(EVENT_NOT_FOUND));

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
        List<Long> eventParticipationIds = request.eventParticipationIds();
        List<EventParticipation> eventParticipations = eventParticipationRepository.findAllById(eventParticipationIds);
        Event event = eventParticipations.get(0).getEvent();

        eventParticipationDomainService.validateAfterPartyEnabled(event);

        eventParticipations.forEach(EventParticipation::attendAfterParty);

        log.info("[EventParticipationService] 뒤풀이 참석 처리: eventParticipationIds={}", eventParticipationIds);
    }

    @Transactional(readOnly = true)
    public AfterPartyAttendanceResponse getAfterPartyAttendances(Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new CustomException(EVENT_NOT_FOUND));
        validateEventEnabledForAfterParty(event);

        List<EventParticipation> eventParticipations = eventParticipationRepository.findAllByEvent(event);

        long attendedAfterApplyingCount = eventParticipations.stream()
                .filter(eventParticipation -> eventParticipation
                                .getAfterPartyApplicationStatus()
                                .isApplied()
                        && eventParticipation.getAfterPartyAttendanceStatus().isAttended())
                .count();

        long notAttendedAfterApplyingCount = eventParticipations.stream()
                .filter(eventParticipation -> eventParticipation
                                .getAfterPartyApplicationStatus()
                                .isApplied()
                        && eventParticipation.getAfterPartyAttendanceStatus().isNotAttended())
                .count();

        long onSiteApplicationCount = eventParticipations.stream()
                .filter(eventParticipation -> eventParticipation
                                .getAfterPartyApplicationStatus()
                                .isNotApplied()
                        && eventParticipation.getAfterPartyAttendanceStatus().isAttended())
                .count();

        List<EventParticipationDto> eventParticipationDtos =
                eventParticipations.stream().map(EventParticipationDto::from).toList();

        return AfterPartyAttendanceResponse.of(
                attendedAfterApplyingCount,
                notAttendedAfterApplyingCount,
                onSiteApplicationCount,
                eventParticipationDtos);
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

    @Transactional
    public void uncheckPostPayment(@Valid AfterPartyPostPaymentUncheckRequest request) {
        List<Long> eventParticipationIds = request.eventParticipationIds();
        List<EventParticipation> eventParticipations = eventParticipationRepository.findAllById(eventParticipationIds);
        Event event = eventParticipations.get(0).getEvent();

        eventParticipationDomainService.validateAfterPartyEnabled(event);

        eventParticipations.forEach(EventParticipation::uncheckPostPayment);

        log.info("[EventParticipationService] 뒤풀이 정산 취소 처리: eventParticipationIds={}", eventParticipationIds);
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

    private void validateEventEnabledForAfterParty(Event event) {
        if (event.getAfterPartyStatus().isDisabled()) {
            throw new CustomException(PARTICIPATION_NOT_READABLE_AFTER_PARTY_DISABLED);
        }
    }

    @Transactional
    public void deleteEventParticipations(EventParticipationDeleteRequest request) {
        List<EventParticipation> participations =
                eventParticipationRepository.findAllById(request.eventParticipationIds());
        validateRequestParticipationIds(request.eventParticipationIds(), participations);

        eventParticipationRepository.deleteAll(participations);
        log.info(
                "[EventParticipationService] 행사 참여 정보 삭제 완료: eventParticipationIds={}",
                request.eventParticipationIds());
    }

    // 요청 ID에 해당하는 참여정보가 존재하지 않거나 중복이 있는지 검증
    private void validateRequestParticipationIds(List<Long> requestIds, List<EventParticipation> participations) {
        if (requestIds.size() != participations.size()) {
            throw new CustomException(PARTICIPATION_NOT_DELETABLE_INVALID_IDS);
        }
    }
}

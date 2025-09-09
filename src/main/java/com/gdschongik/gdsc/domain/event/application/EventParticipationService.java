package com.gdschongik.gdsc.domain.event.application;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;
import static java.time.LocalDateTime.*;

import com.gdschongik.gdsc.domain.event.dao.EventParticipationRepository;
import com.gdschongik.gdsc.domain.event.dao.EventRepository;
import com.gdschongik.gdsc.domain.event.domain.Event;
import com.gdschongik.gdsc.domain.event.domain.EventParticipation;
import com.gdschongik.gdsc.domain.event.domain.Participant;
import com.gdschongik.gdsc.domain.event.domain.service.EventParticipationDomainService;
import com.gdschongik.gdsc.domain.event.dto.dto.EventParticipableMemberDto;
import com.gdschongik.gdsc.domain.event.dto.dto.EventParticipationDto;
import com.gdschongik.gdsc.domain.event.dto.request.AfterPartyAttendRequest;
import com.gdschongik.gdsc.domain.event.dto.request.AfterPartyStatusUpdateRequest;
import com.gdschongik.gdsc.domain.event.dto.request.AfterPartyStatusesUpdateRequest;
import com.gdschongik.gdsc.domain.event.dto.request.AfterPartyUpdateTarget;
import com.gdschongik.gdsc.domain.event.dto.request.EventApplyRequest;
import com.gdschongik.gdsc.domain.event.dto.request.EventManualApplyRequest;
import com.gdschongik.gdsc.domain.event.dto.request.EventParticipantQueryOption;
import com.gdschongik.gdsc.domain.event.dto.request.EventParticipationDeleteRequest;
import com.gdschongik.gdsc.domain.event.dto.request.EventRegisteredManualApplyRequest;
import com.gdschongik.gdsc.domain.event.dto.request.EventUnregisteredManualApplyRequest;
import com.gdschongik.gdsc.domain.event.dto.response.AfterPartyApplicantResponse;
import com.gdschongik.gdsc.domain.event.dto.response.AfterPartyAttendanceResponse;
import com.gdschongik.gdsc.domain.event.dto.response.EventApplicantResponse;
import com.gdschongik.gdsc.domain.member.dao.MemberRepository;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.global.exception.ErrorCode;
import java.util.List;
import java.util.Optional;
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
    public AfterPartyApplicantResponse getAfterPartyApplicants(
            Long eventId, EventParticipantQueryOption queryOption, Pageable pageable) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new CustomException(EVENT_NOT_FOUND));
        validateEventEnabledForAfterParty(event);
        return eventParticipationRepository.findAfterPartyApplicants(eventId, queryOption, pageable);
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

        validateRequestParticipationsSameEvent(eventParticipations);

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
    public void confirmAfterPartyStatus(Long eventParticipationId, AfterPartyStatusUpdateRequest request) {
        EventParticipation eventParticipation = eventParticipationRepository
                .findById(eventParticipationId)
                .orElseThrow(() -> new CustomException(PARTICIPATION_NOT_FOUND));

        confirmAfterPartyStatusByAfterPartyUpdateTarget(eventParticipation, request.afterPartyUpdateTarget());

        log.info(
                "[EventParticipationService] 뒤풀이 참석/정산 확인 처리: eventParticipationId={}, afterPartyUpdateTarget={}",
                eventParticipationId,
                request.afterPartyUpdateTarget());
    }

    @Transactional
    public void confirmAllAfterPartyStatus(AfterPartyStatusesUpdateRequest request) {
        Event event =
                eventRepository.findById(request.eventId()).orElseThrow(() -> new CustomException(EVENT_NOT_FOUND));
        List<EventParticipation> eventParticipations = eventParticipationRepository.findAllByEvent(event);

        eventParticipations.forEach(eventParticipation ->
                confirmAfterPartyStatusByAfterPartyUpdateTarget(eventParticipation, request.afterPartyUpdateTarget()));

        log.info(
                "[EventParticipationService] 뒤풀이 참석/정산 전체 확인 처리: eventId={}, afterPartyUpdateTarget={}",
                request.eventId(),
                request.afterPartyUpdateTarget());
    }

    @Transactional
    public void revokeAfterPartyStatusConfirm(Long eventParticipationId, AfterPartyStatusUpdateRequest request) {
        EventParticipation eventParticipation = eventParticipationRepository
                .findById(eventParticipationId)
                .orElseThrow(() -> new CustomException(PARTICIPATION_NOT_FOUND));

        revokeAfterPartyStatusByAfterPartyUpdateTarget(eventParticipation, request.afterPartyUpdateTarget());

        log.info(
                "[EventParticipationService] 뒤풀이 참석/정산 확인 취소 처리: eventParticipationId={}, afterPartyUpdateTarget={}",
                eventParticipationId,
                request.afterPartyUpdateTarget());
    }

    @Transactional
    public void revokeAllAfterPartyStatusConfirm(AfterPartyStatusesUpdateRequest request) {
        Event event =
                eventRepository.findById(request.eventId()).orElseThrow(() -> new CustomException(EVENT_NOT_FOUND));
        List<EventParticipation> eventParticipations = eventParticipationRepository.findAllByEvent(event);

        eventParticipations.forEach(eventParticipation ->
                revokeAfterPartyStatusByAfterPartyUpdateTarget(eventParticipation, request.afterPartyUpdateTarget()));

        log.info(
                "[EventParticipationService] 뒤풀이 참석 / 정산 현황 전체 확인 취소 처리: eventId={}, afterPartyUpdateTarget={}",
                request.eventId(),
                request.afterPartyUpdateTarget());
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

    @Deprecated(forRemoval = true)
    @Transactional
    public void applyManualForRegistered(EventRegisteredManualApplyRequest request) {
        Event event =
                eventRepository.findById(request.eventId()).orElseThrow(() -> new CustomException(EVENT_NOT_FOUND));
        Member member = memberRepository
                .findById(request.memberId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        EventParticipation participation = eventParticipationDomainService.applyManualForRegistered(member, event);
        eventParticipationRepository.save(participation);

        log.info(
                "[EventParticipationService] 행사 수동 신청 (회원): eventId={}, memberId={}",
                request.eventId(),
                request.memberId());
    }

    @Deprecated(forRemoval = true)
    @Transactional
    public void applyManualForUnregistered(EventUnregisteredManualApplyRequest request) {
        Event event =
                eventRepository.findById(request.eventId()).orElseThrow(() -> new CustomException(EVENT_NOT_FOUND));

        boolean infoStatusSatisfiedMemberExists = memberRepository.existsInfoStatusSatisfiedMemberByStudentId(
                request.participant().getStudentId());

        EventParticipation participation = eventParticipationDomainService.applyManualForUnregistered(
                request.participant(), event, infoStatusSatisfiedMemberExists);
        eventParticipationRepository.save(participation);

        log.info(
                "[EventParticipationService] 행사 수동 신청 (비회원): eventId={}, participant={}",
                request.eventId(),
                request.participant());
    }

    @Transactional
    public void applyManual(EventManualApplyRequest request) {
        Event event =
                eventRepository.findById(request.eventId()).orElseThrow(() -> new CustomException(EVENT_NOT_FOUND));

        Participant participant = request.participant();
        Member memberByParticipant =
                memberRepository.findByStudentId(participant.getStudentId()).orElse(null);

        EventParticipation participation =
                eventParticipationDomainService.applyManual(participant, memberByParticipant, event);
        eventParticipationRepository.save(participation);

        log.info(
                "[EventParticipationService] 행사 수동 신청: eventId={}, studentId={}",
                request.eventId(),
                participant.getStudentId());
    }

    private void validateEventEnabledForAfterParty(Event event) {
        if (!event.afterPartyExists()) {
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

    // 요청 ID에 해당하는 참여정보가 모두 같은 이벤트에 대한 참여 정보인지 검증
    private void validateRequestParticipationsSameEvent(List<EventParticipation> participations) {
        Event event = participations.get(0).getEvent();
        boolean hasDifferentEvent = participations.stream()
                .anyMatch(participation -> !participation.getEvent().equals(event));

        if (hasDifferentEvent) {
            throw new CustomException(PARTICIPATION_NOT_UPDATABLE_DIFFERENT_EVENT);
        }
    }

    private void confirmAfterPartyStatusByAfterPartyUpdateTarget(
            EventParticipation participation, AfterPartyUpdateTarget afterPartyUpdateTarget) {
        switch (afterPartyUpdateTarget) {
            case ATTENDANCE -> participation.attendAfterParty();
            case PRE_PAYMENT -> participation.confirmPrePayment();
            case POST_PAYMENT -> participation.confirmPostPayment();
        }
    }

    private void revokeAfterPartyStatusByAfterPartyUpdateTarget(
            EventParticipation participation, AfterPartyUpdateTarget afterPartyUpdateTarget) {
        switch (afterPartyUpdateTarget) {
            case ATTENDANCE -> participation.revokeAttendance();
            case PRE_PAYMENT -> participation.revokePrePayment();
            case POST_PAYMENT -> participation.revokePostPayment();
        }
    }

    @Transactional
    public void applyEventParticipation(EventApplyRequest request) {
        Event event =
                eventRepository.findById(request.eventId()).orElseThrow(() -> new CustomException(EVENT_NOT_FOUND));

        Participant participant = request.participant();
        Optional<Member> optionalMember = memberRepository.findByStudentId(participant.getStudentId());

        EventParticipation eventParticipation;
        if (optionalMember.isPresent()
                && optionalMember.get().getAssociateRequirement().isInfoSatisfied()) {
            eventParticipation = eventParticipationDomainService.applyEventForRegistered(
                    optionalMember.get(), request.afterPartyApplicationStatus(), event, now());
        } else {
            eventParticipation = eventParticipationDomainService.applyEventForUnregistered(
                    participant, request.afterPartyApplicationStatus(), event, now());
        }
        eventParticipationRepository.save(eventParticipation);

        log.info(
                "[EventParticipationService] 이벤트 참여 신청: eventId={}, memberStudentId={}",
                event.getId(),
                participant.getStudentId());
    }
}

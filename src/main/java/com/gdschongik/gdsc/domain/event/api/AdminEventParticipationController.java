package com.gdschongik.gdsc.domain.event.api;

import com.gdschongik.gdsc.domain.event.application.EventParticipationService;
import com.gdschongik.gdsc.domain.event.dto.dto.EventParticipableMemberDto;
import com.gdschongik.gdsc.domain.event.dto.request.AfterPartyAttendRequest;
import com.gdschongik.gdsc.domain.event.dto.request.EventParticipantQueryOption;
import com.gdschongik.gdsc.domain.event.dto.request.EventParticipationDeleteRequest;
import com.gdschongik.gdsc.domain.event.dto.request.EventRegisteredApplyRequest;
import com.gdschongik.gdsc.domain.event.dto.request.EventUnregisteredApplyRequest;
import com.gdschongik.gdsc.domain.event.dto.response.AfterPartyApplicantResponse;
import com.gdschongik.gdsc.domain.event.dto.response.AfterPartyAttendanceResponse;
import com.gdschongik.gdsc.domain.event.dto.response.EventApplicantResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Event Participation - Admin", description = "어드민 행사 신청 관리 API입니다.")
@RestController
@RequestMapping("/admin/event-participations")
@RequiredArgsConstructor
public class AdminEventParticipationController {

    private final EventParticipationService eventParticipationService;

    @Operation(summary = "행사 신청 정보 삭제", description = "행사 신청 정보를 삭제합니다.")
    @DeleteMapping
    public ResponseEntity<Void> deleteEventParticipations(@Valid @RequestBody EventParticipationDeleteRequest request) {
        eventParticipationService.deleteEventParticipations(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "행사 신청자 목록 조회", description = "해당 행사의 신청자 목록을 조회합니다")
    @GetMapping("/applicants")
    public ResponseEntity<Page<EventApplicantResponse>> getEventApplicants(
            @RequestParam(name = "event") Long eventId,
            @ParameterObject EventParticipantQueryOption queryOption,
            @ParameterObject Pageable pageable) {
        var response = eventParticipationService.getEventApplicants(eventId, queryOption, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "뒤풀이 신청자 목록 조회", description = "해당 행사의 뒤풀이 신청자 목록과 결제 관련 통계를 조회합니다.")
    @GetMapping("/after-party/applicants")
    public ResponseEntity<AfterPartyApplicantResponse> getAfterPartyApplicants(
            @RequestParam(name = "event") Long eventId,
            @ParameterObject EventParticipantQueryOption queryOption,
            @ParameterObject Pageable pageable) {
        // TODO: 서비스 로직 구현
        var response = new AfterPartyApplicantResponse(null, null);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "뒤풀이 참석자 조회", description = "해당 행사의 뒤풀이 신청자 목록과 참석자 통계를 조회합니다. 현장 출석체크 용으로 사용합니다.")
    @GetMapping("/after-party/attendances")
    public ResponseEntity<AfterPartyAttendanceResponse> getAfterPartyAttendances(
            @RequestParam(name = "event") Long eventId) {
        var response = eventParticipationService.getAfterPartyAttendances(eventId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "뒤풀이 참석 처리", description = "뒤풀이에 참석 처리합니다.")
    @PutMapping("/after-party/attend")
    public ResponseEntity<Void> attendAfterParty(@Valid @RequestBody AfterPartyAttendRequest request) {
        eventParticipationService.attendAfterParty(request);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "행사 참여 가능 멤버 검색",
            description =
                    """
					해당 이벤트에 참여 가능한 멤버를 이름으로 검색합니다.
					멤버 정보 및 해당 이벤트 기 신청 상태를 반환합니다.
					이름과 정확히 일치하는 멤버만 검색하며, 동명이인인 경우 여러 건을 반환합니다.""")
    @GetMapping("/members/participable/search")
    public ResponseEntity<List<EventParticipableMemberDto>> searchParticipableMembers(
            @RequestParam(name = "event") Long eventId, @RequestParam(name = "name") String name) {
        var response = eventParticipationService.searchParticipableMembers(eventId, name);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "행사 수동 신청 (회원)", description = "관리자가 회원의 정보를 바탕으로 행사를 수동으로 신청 처리합니다.")
    @PostMapping("/apply/manual/registered")
    public ResponseEntity<Void> applyManualForRegistered(@Valid @RequestBody EventRegisteredApplyRequest request) {
        eventParticipationService.applyManualForRegistered(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "행사 수동 신청 (비회원)", description = "관리자가 비회원의 정보를 바탕으로 행사를 수동으로 신청 처리합니다.")
    @PostMapping("/apply/manual/unregistered")
    public ResponseEntity<Void> applyManualForUnregistered(@Valid @RequestBody EventUnregisteredApplyRequest request) {
        eventParticipationService.applyManualForUnregistered(request);
        return ResponseEntity.ok().build();
    }
}

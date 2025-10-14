package com.gdschongik.gdsc.domain.event.api;

import com.gdschongik.gdsc.domain.event.application.EventParticipationService;
import com.gdschongik.gdsc.domain.event.dto.request.EventApplyOnlineRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Event Participation - Participant", description = "참가자용 이벤트 참여 정보 관리 API입니다.")
@RestController
@RequestMapping("/participant/event-participations")
@RequiredArgsConstructor
public class ParticipantEventParticipationController {

    private final EventParticipationService eventParticipationService;

    @Operation(summary = "이벤트 참여 신청 폼 제출", description = "이벤트 참여 신청 폼을 제출합니다.")
    @PostMapping("/apply")
    public ResponseEntity<Void> applyEventParticipation(@Valid @RequestBody EventApplyOnlineRequest request) {
        eventParticipationService.applyOnline(request);
        return ResponseEntity.ok().build();
    }
}

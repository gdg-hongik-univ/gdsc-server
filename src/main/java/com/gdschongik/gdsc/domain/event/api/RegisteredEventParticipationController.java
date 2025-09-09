package com.gdschongik.gdsc.domain.event.api;

import com.gdschongik.gdsc.domain.event.application.EventParticipationService;
import com.gdschongik.gdsc.domain.event.dto.request.EventRegisteredApplyRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Event Participation - Registered", description = "회원용 이벤트 참여 정보 관리 API입니다.")
@RestController
@RequestMapping("/registered/event-participations")
@RequiredArgsConstructor
public class RegisteredEventParticipationController {

    private final EventParticipationService eventParticipationService;

    @Operation(
            summary = "이벤트 참여 신청 폼 제출",
            description = """
			이벤트 참여 신청 폼을 제출합니다.
			사용자가 로그인 후에 제출하는 회원일 때만 본 API를 사용할 수 있습니다.""")
    @PostMapping("/submit")
    public ResponseEntity<Void> submitEventParticipation(@Valid @RequestBody EventRegisteredApplyRequest request) {
        eventParticipationService.submitEventParticipationForRegistered(request);
        return ResponseEntity.ok().build();
    }
}

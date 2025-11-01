package com.gdschongik.gdsc.domain.event.api;

import com.gdschongik.gdsc.domain.event.application.EventService;
import com.gdschongik.gdsc.domain.event.dto.dto.EventDto;
import com.gdschongik.gdsc.domain.event.dto.response.EventParticipableResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Event - Common", description = "공통 행사 API입니다.")
@RestController
@RequestMapping("/common/events")
@RequiredArgsConstructor
public class CommonEventController {

    private final EventService eventService;

    @Operation(summary = "행사 조회", description = "행사를 조회합니다.")
    @GetMapping("/{eventId}")
    public ResponseEntity<EventDto> getEvent(@PathVariable Long eventId) {
        var response = eventService.getEvent(eventId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "행사 신청 가능 여부 확인", description = "행사 신청 가능 여부를 확인합니다.")
    @GetMapping("/{eventId}/is-participable")
    public ResponseEntity<EventParticipableResponse> isEventParticipable(@PathVariable Long eventId) {
        var response = eventService.isEventParticipable(eventId);
        return ResponseEntity.ok(response);
    }
}

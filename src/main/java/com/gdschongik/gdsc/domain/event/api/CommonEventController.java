package com.gdschongik.gdsc.domain.event.api;

import com.gdschongik.gdsc.domain.event.application.EventService;
import com.gdschongik.gdsc.domain.event.dto.dto.EventDto;
import com.gdschongik.gdsc.domain.event.dto.request.EventValidateApplicableRequest;
import com.gdschongik.gdsc.domain.event.dto.response.EventValidateApplicableResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping("/validate-applicable")
    public ResponseEntity<EventValidateApplicableResponse> validateEventApplicable(
            @Valid @RequestBody EventValidateApplicableRequest request) {
        var response = eventService.validateEventApplicable(request);
        return ResponseEntity.ok(response);
    }
}

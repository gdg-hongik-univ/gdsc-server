package com.gdschongik.gdsc.domain.event.api;

import com.gdschongik.gdsc.domain.event.application.EventService;
import com.gdschongik.gdsc.domain.event.dto.request.EventCreateRequest;
import com.gdschongik.gdsc.domain.event.dto.request.EventUpdateBasicInfoRequest;
import com.gdschongik.gdsc.domain.event.dto.request.EventUpdateFormInfoRequest;
import com.gdschongik.gdsc.domain.event.dto.response.EventCreateResponse;
import com.gdschongik.gdsc.domain.event.dto.response.EventResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Event - Admin", description = "어드민 행사 관리 API입니다.")
@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class AdminEventController {

    private final EventService eventService;

    @Operation(summary = "행사 목록 조회", description = "행사 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<Page<EventResponse>> getEvents(@ParameterObject Pageable pageable) {
        var response = eventService.getEvents(pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "행사 생성", description = "행사를 생성합니다.")
    @PostMapping
    public ResponseEntity<EventCreateResponse> createEvent(@Valid @RequestBody EventCreateRequest request) {
        var response = eventService.createEvent(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "이벤트 검색", description = "이벤트를 검색합니다.")
    @GetMapping("/search")
    public ResponseEntity<Page<EventResponse>> searchEvent(
            @RequestParam String name, @ParameterObject Pageable pageable) {
        var response = eventService.searchEvent(name, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "이벤트 기본 정보 수정", description = "이벤트 기본 정보를 수정합니다.")
    @PutMapping("/{eventId}/basic-info")
    public ResponseEntity<Void> updateEventBasicInfo(
            @PathVariable Long eventId, @Valid @RequestBody EventUpdateBasicInfoRequest request) {
        eventService.updateEventBasicInfo(eventId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "이벤트 폼 정보 수정", description = "이벤트 신청 폼 관련 정보를 수정합니다.")
    @PutMapping("/{eventId}/form-info")
    public ResponseEntity<Void> updateEventFormInfo(
            @PathVariable Long eventId, @Valid @RequestBody EventUpdateFormInfoRequest request) {
        eventService.updateEventFormInfo(eventId, request);
        return ResponseEntity.ok().build();
    }
}

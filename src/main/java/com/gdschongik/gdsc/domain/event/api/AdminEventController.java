package com.gdschongik.gdsc.domain.event.api;

import com.gdschongik.gdsc.domain.event.application.EventService;
import com.gdschongik.gdsc.domain.event.dto.request.EventCreateRequest;
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
    public ResponseEntity<Void> createEvent(@Valid @RequestBody EventCreateRequest request) {
        eventService.createEvent(request);
        return ResponseEntity.ok().build();
    }
}

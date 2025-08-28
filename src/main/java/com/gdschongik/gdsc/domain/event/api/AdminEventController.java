package com.gdschongik.gdsc.domain.event.api;

import com.gdschongik.gdsc.domain.event.application.EventService;
import com.gdschongik.gdsc.domain.event.dto.response.EventResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Event - Admin", description = "어드민 행사 관리 API입니다.")
@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class AdminEventController {

    private final EventService eventService;

    @Operation(summary = "행사 목록 조회", description = "행사 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<EventResponse>> getEvents(@ParameterObject Sort sort) {
        var response = eventService.getEvents(sort);
        return ResponseEntity.ok(response);
    }
}

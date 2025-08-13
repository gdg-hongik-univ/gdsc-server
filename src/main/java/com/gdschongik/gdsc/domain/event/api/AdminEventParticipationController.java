package com.gdschongik.gdsc.domain.event.api;

import com.gdschongik.gdsc.domain.event.dto.request.EventParticipationDeleteRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Event Participation - Admin", description = "어드민 행사 신청 관리 API입니다.")
@RestController
@RequestMapping("/admin/event-participations")
@RequiredArgsConstructor
public class AdminEventParticipationController {

    @Operation(summary = "행사 신청 정보 삭제", description = "행사 신청 정보를 삭제합니다.")
    @DeleteMapping
    public ResponseEntity<Void> deleteEventParticipations(@Valid @RequestBody EventParticipationDeleteRequest request) {
        // TODO: 서비스 로직 구현
        return ResponseEntity.ok().build();
    }
}

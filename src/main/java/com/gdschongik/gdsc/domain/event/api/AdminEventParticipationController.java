package com.gdschongik.gdsc.domain.event.api;

import com.gdschongik.gdsc.domain.event.domain.AfterPartyApplicationStatus;
import com.gdschongik.gdsc.domain.event.domain.AfterPartyAttendanceStatus;
import com.gdschongik.gdsc.domain.event.domain.MainEventApplicationStatus;
import com.gdschongik.gdsc.domain.event.domain.PaymentStatus;
import com.gdschongik.gdsc.domain.event.dto.EventParticipationDto;
import com.gdschongik.gdsc.domain.event.dto.ParticipantDto;
import com.gdschongik.gdsc.domain.event.dto.request.EventParticipantQueryOption;
import com.gdschongik.gdsc.domain.event.dto.request.EventParticipationDeleteRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

    @Operation(summary = "뒤풀이 신청 정보 조회", description = "뒤풀이 신청 정보를 조회합니다.")
    @GetMapping("/after-party")
    public ResponseEntity<Page<EventParticipationDto>> getAfterPartyParticipations(
            @RequestParam(name = "eventId") Long eventId,
            @ParameterObject EventParticipantQueryOption queryOption,
            @ParameterObject Pageable pageable) {

        // TODO: 임시 응답 제거 후 서비스 로직 구현
        var exampleContent = List.of(new EventParticipationDto(
                1L,
                new ParticipantDto("김홍익", "C123456", "010-1234-5678"),
                1L,
                MainEventApplicationStatus.APPLIED,
                AfterPartyApplicationStatus.APPLIED,
                AfterPartyAttendanceStatus.NOT_ATTENDED,
                PaymentStatus.UNPAID,
                PaymentStatus.UNPAID));

        var exampleResponse = new PageImpl<>(exampleContent, pageable, 1L);
        return ResponseEntity.ok(exampleResponse);
    }
}

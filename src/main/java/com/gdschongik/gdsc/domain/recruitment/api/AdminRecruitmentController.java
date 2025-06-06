package com.gdschongik.gdsc.domain.recruitment.api;

import com.gdschongik.gdsc.domain.recruitment.application.AdminRecruitmentService;
import com.gdschongik.gdsc.domain.recruitment.dto.request.RecruitmentCreateRequest;
import com.gdschongik.gdsc.domain.recruitment.dto.request.RecruitmentRoundCreateRequest;
import com.gdschongik.gdsc.domain.recruitment.dto.request.RecruitmentRoundUpdateRequest;
import com.gdschongik.gdsc.domain.recruitment.dto.response.AdminRecruitmentResponse;
import com.gdschongik.gdsc.domain.recruitment.dto.response.AdminRecruitmentRoundResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Recruitment - Admin", description = "어드민 리쿠르팅 관리 API입니다.")
@RestController
@RequestMapping("/admin/recruitments")
@RequiredArgsConstructor
public class AdminRecruitmentController {

    private final AdminRecruitmentService adminRecruitmentService;

    @Operation(summary = "리쿠르팅 생성", description = "새로운 리쿠르팅을 생성합니다.")
    @PostMapping
    public ResponseEntity<Void> createRecruitment(@Valid @RequestBody RecruitmentCreateRequest request) {
        adminRecruitmentService.createRecruitment(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "리쿠르팅 목록 조회", description = "전체 리쿠르팅 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<AdminRecruitmentResponse>> getAllRecruitments() {
        List<AdminRecruitmentResponse> response = adminRecruitmentService.getAllRecruitments();
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "모집회차 목록 조회", description = "전체 모집회차 목록을 조회합니다.")
    @GetMapping("/rounds")
    public ResponseEntity<List<AdminRecruitmentRoundResponse>> getAllRecruitmentRounds() {
        List<AdminRecruitmentRoundResponse> response = adminRecruitmentService.getAllRecruitmentRounds();
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "모집회차 생성", description = "새로운 모집회차를 생성합니다. 모집기간은 학기 시작일로부터 2주 이내입니다.")
    @PostMapping("/rounds")
    public ResponseEntity<Void> createRecruitmentRound(@Valid @RequestBody RecruitmentRoundCreateRequest request) {
        adminRecruitmentService.createRecruitmentRound(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "모집회차 수정", description = "기존 모집회차를 수정합니다. 학년도와 학기는 수정 대상이 아닙니다.")
    @PutMapping("/rounds/{recruitmentRoundId}")
    public ResponseEntity<Void> updateRecruitmentRound(
            @PathVariable Long recruitmentRoundId, @Valid @RequestBody RecruitmentRoundUpdateRequest request) {
        adminRecruitmentService.updateRecruitmentRound(recruitmentRoundId, request);
        return ResponseEntity.ok().build();
    }
}

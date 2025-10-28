package com.gdschongik.gdsc.domain.member.api;

import com.gdschongik.gdsc.domain.member.application.OnboardingMemberService;
import com.gdschongik.gdsc.domain.member.dto.request.MemberInfoRequest;
import com.gdschongik.gdsc.domain.member.dto.response.MemberDashboardResponse;
import com.gdschongik.gdsc.domain.member.dto.response.MemberInfoResponse;
import com.gdschongik.gdsc.domain.member.dto.response.MemberUnivStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Member - Onboarding", description = "회원 온보딩 API입니다.")
@RestController
@RequestMapping("/onboarding/members")
@RequiredArgsConstructor
public class OnboardingMemberController {

    private final OnboardingMemberService onboardingMemberService;

    @Operation(summary = "내 대시보드 조회", description = "내 대시보드를 조회합니다. 2차 MVP 기능입니다.")
    @GetMapping("/me/dashboard")
    public ResponseEntity<MemberDashboardResponse> getDashboard() {
        MemberDashboardResponse response = onboardingMemberService.getDashboard();
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "재학생 인증 여부 확인", description = "재학생 인증 여부를 확인합니다.")
    @GetMapping("/me/univ-verification")
    public ResponseEntity<MemberUnivStatusResponse> checkUnivVerification() {
        MemberUnivStatusResponse response = onboardingMemberService.checkUnivVerificationStatus();
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "기본 회원정보 작성", description = "기본 회원정보를 작성합니다.")
    @PostMapping("/me/info")
    public ResponseEntity<Void> updateMemberInfo(@Valid @RequestBody MemberInfoRequest request) {
        onboardingMemberService.updateMemberInfo(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "기본 회원정보 조회", description = "기본 회원정보를 조회합니다.")
    @GetMapping("/me/info")
    public ResponseEntity<MemberInfoResponse> getMemberInfo() {
        MemberInfoResponse response = onboardingMemberService.getMemberInfo();
        return ResponseEntity.ok().body(response);
    }
}

package com.gdschongik.gdsc.domain.email.api;

import com.gdschongik.gdsc.domain.email.application.UnivEmailVerificationLinkSendService;
import com.gdschongik.gdsc.domain.email.application.UnivEmailVerificationService;
import com.gdschongik.gdsc.domain.email.dto.request.UnivEmailVerificationLinkSendRequest;
import com.gdschongik.gdsc.domain.email.dto.request.UnivEmailVerificationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Univ Email - Onboarding", description = "학교 인증 메일 인증 API입니다.")
@RestController
@RequestMapping("/onboarding")
@RequiredArgsConstructor
public class OnboardingUnivEmailController {

    private final UnivEmailVerificationLinkSendService univEmailVerificationLinkSendService;
    private final UnivEmailVerificationService univEmailVerificationService;

    @Operation(summary = "학교 인증 메일 발송 요청", description = "학교 인증 메일 발송을 요청합니다.")
    @PostMapping("/send-verify-univ-email")
    public ResponseEntity<Void> sendUnivEmailVerificationLink(
            @Valid @RequestBody UnivEmailVerificationLinkSendRequest request) {
        univEmailVerificationLinkSendService.send(request.univEmail());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "학교 인증 메일 인증하기", description = "학교 인증 메일을 인증합니다.")
    @PatchMapping("/verify-univ-email")
    public ResponseEntity<Void> sendUnivEmailVerificationLink(
            @RequestBody @Valid UnivEmailVerificationRequest request) {
        univEmailVerificationService.verifyMemberUnivEmail(request);
        return ResponseEntity.ok().build();
    }
}

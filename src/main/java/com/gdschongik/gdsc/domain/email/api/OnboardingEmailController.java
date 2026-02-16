package com.gdschongik.gdsc.domain.email.api;

import com.gdschongik.gdsc.domain.auth.application.JwtService;
import com.gdschongik.gdsc.domain.auth.dto.TokenPairDto;
import com.gdschongik.gdsc.domain.email.application.EmailVerificationLinkSendService;
import com.gdschongik.gdsc.domain.email.application.EmailVerificationService;
import com.gdschongik.gdsc.domain.email.dto.request.PreviousEmailVerificationLinkSendRequest;
import com.gdschongik.gdsc.domain.email.dto.request.PreviousEmailVerificationRequest;
import com.gdschongik.gdsc.global.util.CookieUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Email - Onboarding", description = "메일 본인 인증 API입니다.")
@RestController
@RequestMapping("/onboarding")
@RequiredArgsConstructor
public class OnboardingEmailController {

    private final EmailVerificationLinkSendService emailVerificationLinkSendService;
    private final EmailVerificationService emailVerificationService;
    private final JwtService jwtService;
    private final CookieUtil cookieUtil;

    @Operation(summary = "과거 계정 본인 인증 메일 발송 요청", description = "깃허브 계정 변경 전 과거 계정의 본인 인증 메일 발송을 요청합니다.")
    @PostMapping("/send-verify-email")
    public ResponseEntity<Void> sendPreviousEmailVerificationLink(
            @Valid @RequestBody PreviousEmailVerificationLinkSendRequest request) {
        emailVerificationLinkSendService.sendPreviousMemberVerificationLink(request.previousMemberId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "과거 계정 본인 인증 메일 인증하기", description = "과거 계정의 본인 인증 메일을 인증합니다.")
    @PatchMapping("/verify-email")
    public ResponseEntity<Void> verifyPreviousMemberEmail(
            @RequestBody @Valid PreviousEmailVerificationRequest request, HttpServletResponse response) {
        Long previousMemberId = emailVerificationService.verifyPreviousMemberEmail(request);
        TokenPairDto tokenPair = jwtService.issueTokenPair(previousMemberId);
        cookieUtil.addTokenCookies(response, tokenPair.accessToken(), tokenPair.refreshToken());
        return ResponseEntity.ok().build();
    }
}

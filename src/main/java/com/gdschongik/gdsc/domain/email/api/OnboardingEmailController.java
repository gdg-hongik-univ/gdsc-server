package com.gdschongik.gdsc.domain.email.api;

import com.gdschongik.gdsc.domain.auth.application.JwtService;
import com.gdschongik.gdsc.domain.auth.dto.AccessTokenDto;
import com.gdschongik.gdsc.domain.auth.dto.RefreshTokenDto;
import com.gdschongik.gdsc.domain.email.application.EmailVerificationLinkSendService;
import com.gdschongik.gdsc.domain.email.application.EmailVerificationService;
import com.gdschongik.gdsc.domain.email.dto.request.EmailVerificationLinkSendRequest;
import com.gdschongik.gdsc.domain.email.dto.request.EmailVerificationRequest;
import com.gdschongik.gdsc.global.security.MemberAuthInfo;
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

    @Operation(summary = "본인 인증 메일 발송 요청", description = "본인 인증 메일 발송을 요청합니다.")
    @PostMapping("/send-verify-email")
    public ResponseEntity<Void> sendEmailVerificationLink(
            @Valid @RequestBody EmailVerificationLinkSendRequest request) {
        emailVerificationLinkSendService.send(request.previousMemberId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "본인 인증 메일 인증하기", description = "본인 인증 메일을 인증합니다.")
    @PatchMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(
            @RequestBody @Valid EmailVerificationRequest request, HttpServletResponse response) {
        MemberAuthInfo previousMemberAuthInfo = emailVerificationService.verifyMemberEmail(request);
        AccessTokenDto accessTokenDto = jwtService.createAccessToken(previousMemberAuthInfo);
        RefreshTokenDto refreshTokenDto = jwtService.createRefreshToken(previousMemberAuthInfo.memberId());
        cookieUtil.addTokenCookies(response, accessTokenDto.tokenValue(), refreshTokenDto.tokenValue());
        return ResponseEntity.ok().build();
    }
}

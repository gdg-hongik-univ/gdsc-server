package com.gdschongik.gdsc.domain.email.application;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.domain.email.dao.EmailVerificationRepository;
import com.gdschongik.gdsc.domain.email.domain.EmailVerification;
import com.gdschongik.gdsc.domain.email.dto.request.PreviousEmailVerificationRequest;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.helper.IntegrationTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class EmailVerificationServiceTest extends IntegrationTest {

    @Autowired
    private EmailVerificationCodeSendService emailVerificationCodeSendService;

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    private String sendCodeAndGetCode(Member currentMember, Member previousMember) {
        emailVerificationCodeSendService.sendPreviousMemberVerificationCode(previousMember.getId());
        return emailVerificationRepository
                .findById(currentMember.getId())
                .orElseThrow()
                .getCode();
    }

    @Nested
    class 본인_인증_코드_발송시 {

        @Test
        void 현재_계정과_과거_계정이_같으면_실패한다() {
            // given
            Member currentMember = createMember();
            logoutAndReloginAs(currentMember.getId(), currentMember.getRole());

            // when & then
            assertThatThrownBy(() ->
                            emailVerificationCodeSendService.sendPreviousMemberVerificationCode(currentMember.getId()))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(EMAIL_VERIFICATION_SAME_MEMBER.getMessage());
        }

        @Test
        void 레디스에_인증_코드가_저장된다() {
            // given
            Member previousMember = createMember();
            Member currentMember = createGuestMember();
            logoutAndReloginAs(currentMember.getId(), currentMember.getRole());

            // when
            emailVerificationCodeSendService.sendPreviousMemberVerificationCode(previousMember.getId());

            // then
            EmailVerification emailVerification =
                    emailVerificationRepository.findById(currentMember.getId()).orElseThrow();
            assertThat(emailVerification.getCode()).isNotEmpty();
            assertThat(emailVerification.getCurrentMemberId()).isEqualTo(currentMember.getId());
            assertThat(emailVerification.getPreviousMemberId()).isEqualTo(previousMember.getId());
        }
    }

    @Nested
    class 본인_인증_코드_검증시 {

        @Test
        void 레디스에_인증정보가_존재하지_않으면_실패한다() {
            // given
            Member currentMember = createGuestMember();
            logoutAndReloginAs(currentMember.getId(), currentMember.getRole());
            PreviousEmailVerificationRequest request = new PreviousEmailVerificationRequest("123456");

            // when & then
            assertThatThrownBy(() -> emailVerificationService.verifyPreviousMemberEmail(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(EMAIL_VERIFICATION_CODE_NOT_SENT.getMessage());
        }

        @Test
        void 인증코드가_일치하면_과거_계정_아이디를_반환한다() {
            // given
            Member previousMember = createMember();
            Member currentMember = createGuestMember();
            logoutAndReloginAs(currentMember.getId(), currentMember.getRole());
            String verificationCode = sendCodeAndGetCode(currentMember, previousMember);

            // when
            Long previousMemberId = emailVerificationService.verifyPreviousMemberEmail(
                    new PreviousEmailVerificationRequest(verificationCode));

            // then
            assertThat(previousMemberId).isEqualTo(previousMember.getId());
        }

        @Test
        void 인증에_성공하면_레디스의_인증정보가_삭제된다() {
            // given
            Member previousMember = createMember();
            Member currentMember = createGuestMember();
            logoutAndReloginAs(currentMember.getId(), currentMember.getRole());
            String verificationCode = sendCodeAndGetCode(currentMember, previousMember);

            // when
            emailVerificationService.verifyPreviousMemberEmail(new PreviousEmailVerificationRequest(verificationCode));

            // then
            assertThat(emailVerificationRepository.findById(currentMember.getId()))
                    .isEmpty();
        }

        @Test
        void 인증에_성공한_코드는_재사용할_수_없다() {
            // given
            Member previousMember = createMember();
            Member currentMember = createGuestMember();
            logoutAndReloginAs(currentMember.getId(), currentMember.getRole());
            String verificationCode = sendCodeAndGetCode(currentMember, previousMember);
            emailVerificationService.verifyPreviousMemberEmail(new PreviousEmailVerificationRequest(verificationCode));

            // when & then
            PreviousEmailVerificationRequest request = new PreviousEmailVerificationRequest(verificationCode);
            assertThatThrownBy(() -> emailVerificationService.verifyPreviousMemberEmail(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(EMAIL_VERIFICATION_CODE_NOT_SENT.getMessage());
        }
    }
}

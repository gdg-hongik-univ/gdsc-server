package com.gdschongik.gdsc.domain.email.application;

import static com.gdschongik.gdsc.global.common.constant.EmailConstant.*;
import static com.gdschongik.gdsc.global.common.constant.TestEmailConstant.*;
import static com.gdschongik.gdsc.global.common.constant.TestMemberConstant.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.domain.email.dao.UnivEmailVerificationRepository;
import com.gdschongik.gdsc.domain.email.domain.UnivEmailVerification;
import com.gdschongik.gdsc.domain.email.dto.request.UnivEmailVerificationRequest;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.helper.IntegrationTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class UnivEmailVerificationServiceTest extends IntegrationTest {

    @Autowired
    private UnivEmailVerificationCodeSendService univEmailVerificationCodeSendService;

    @Autowired
    private UnivEmailVerificationService univEmailVerificationService;

    @Autowired
    private UnivEmailVerificationRepository univEmailVerificationRepository;

    private void passResendWaitTime(Long memberId) {
        UnivEmailVerification univEmailVerification =
                univEmailVerificationRepository.findById(memberId).orElseThrow();
        long remainingSeconds = VERIFICATION_CODE_TTL.toSeconds() - VERIFICATION_CODE_RESEND_WAIT_TIME.toSeconds();
        univEmailVerificationRepository.save(UnivEmailVerification.create(
                univEmailVerification.getMemberId(),
                univEmailVerification.getUnivEmail(),
                univEmailVerification.getCode(),
                remainingSeconds));
    }

    private String sendCodeAndGetCode(Member member, String univEmail) {
        univEmailVerificationCodeSendService.send(univEmail);
        return univEmailVerificationService
                .getUnivEmailVerificationFromRedis(member.getId())
                .orElseThrow()
                .getCode();
    }

    @Nested
    class 재학생_인증_코드_발송시 {

        @Test
        void 재발송_대기_시간이_지나지_않았으면_실패한다() {
            // given
            Member member = createGuestMember();
            logoutAndReloginAs(member.getId(), member.getRole());
            univEmailVerificationCodeSendService.send(UNIV_EMAIL);

            // when & then
            assertThatThrownBy(() -> univEmailVerificationCodeSendService.send(UNIV_EMAIL))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(EMAIL_VERIFICATION_CODE_RESEND_WAIT_TIME_NOT_PASSED.getMessage());
        }

        @Test
        void 재발송_대기_시간이_지났으면_성공한다() {
            // given
            Member member = createGuestMember();
            logoutAndReloginAs(member.getId(), member.getRole());
            sendCodeAndGetCode(member, UNIV_EMAIL);
            passResendWaitTime(member.getId());

            // when & then
            assertThatCode(() -> univEmailVerificationCodeSendService.send(UNIV_EMAIL))
                    .doesNotThrowAnyException();
        }

        @Test
        void 발송_검증에_실패하면_재발송_대기_시간이_적용되지_않는다() {
            // given
            Member member = createGuestMember();
            logoutAndReloginAs(member.getId(), member.getRole());
            String invalidUnivEmail = "test@naver.com";
            assertThatThrownBy(() -> univEmailVerificationCodeSendService.send(invalidUnivEmail))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(UNIV_EMAIL_DOMAIN_MISMATCH.getMessage());

            // when & then
            assertThatCode(() -> univEmailVerificationCodeSendService.send(UNIV_EMAIL))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    class 재학생_메일_인증시 {

        @Test
        void 레디스에_이메일인증정보가_존재하지_않으면_실패한다() {
            // given
            Member member = createGuestMember();
            logoutAndReloginAs(member.getId(), member.getRole());
            UnivEmailVerificationRequest request = new UnivEmailVerificationRequest("123456");

            // when & then
            assertThatThrownBy(() -> univEmailVerificationService.verifyMemberUnivEmail(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(UNIV_EMAIL_VERIFICATION_CODE_NOT_SENT.getMessage());
        }

        @Test
        void 인증코드가_일치하면_학교_메일_인증이_완료된다() {
            // given
            Member member = createGuestMember();
            logoutAndReloginAs(member.getId(), member.getRole());
            String verificationCode = sendCodeAndGetCode(member, UNIV_EMAIL);

            // when
            univEmailVerificationService.verifyMemberUnivEmail(new UnivEmailVerificationRequest(verificationCode));

            // then
            Member verifiedMember = memberRepository.findById(member.getId()).orElseThrow();
            assertThat(verifiedMember.getUnivEmail()).isEqualTo(UNIV_EMAIL);
            assertThat(verifiedMember.getAssociateRequirement().isUnivSatisfied())
                    .isTrue();
        }

        @Test
        void 인증에_성공하면_레디스의_인증정보가_삭제된다() {
            // given
            Member member = createGuestMember();
            logoutAndReloginAs(member.getId(), member.getRole());
            String verificationCode = sendCodeAndGetCode(member, UNIV_EMAIL);

            // when
            univEmailVerificationService.verifyMemberUnivEmail(new UnivEmailVerificationRequest(verificationCode));

            // then
            assertThat(univEmailVerificationService.getUnivEmailVerificationFromRedis(member.getId()))
                    .isEmpty();
        }

        @Test
        void 최대_시도_횟수를_초과하면_인증에_실패한다() {
            // given
            Member member = createGuestMember();
            logoutAndReloginAs(member.getId(), member.getRole());
            String verificationCode = sendCodeAndGetCode(member, UNIV_EMAIL);
            UnivEmailVerificationRequest wrongRequest = new UnivEmailVerificationRequest(WRONG_CODE);
            for (int i = 0; i < MAX_ATTEMPT_COUNT; i++) {
                assertThatThrownBy(() -> univEmailVerificationService.verifyMemberUnivEmail(wrongRequest))
                        .isInstanceOf(CustomException.class);
            }

            // when & then
            UnivEmailVerificationRequest request = new UnivEmailVerificationRequest(verificationCode);
            assertThatThrownBy(() -> univEmailVerificationService.verifyMemberUnivEmail(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(EMAIL_VERIFICATION_CODE_ATTEMPT_EXCEEDED.getMessage());
        }

        @Test
        void 인증_코드를_재발송하면_시도_횟수가_초기화된다() {
            // given
            Member member = createGuestMember();
            logoutAndReloginAs(member.getId(), member.getRole());
            sendCodeAndGetCode(member, UNIV_EMAIL);
            passResendWaitTime(member.getId());
            UnivEmailVerificationRequest wrongRequest = new UnivEmailVerificationRequest(WRONG_CODE);
            for (int i = 0; i < MAX_ATTEMPT_COUNT; i++) {
                assertThatThrownBy(() -> univEmailVerificationService.verifyMemberUnivEmail(wrongRequest))
                        .isInstanceOf(CustomException.class);
            }

            // when
            String reissuedCode = sendCodeAndGetCode(member, UNIV_EMAIL);
            univEmailVerificationService.verifyMemberUnivEmail(new UnivEmailVerificationRequest(reissuedCode));

            // then
            Member verifiedMember = memberRepository.findById(member.getId()).orElseThrow();
            assertThat(verifiedMember.getAssociateRequirement().isUnivSatisfied())
                    .isTrue();
        }

        @Test
        void 인증에_성공한_코드는_재사용할_수_없다() {
            // given
            Member member = createGuestMember();
            logoutAndReloginAs(member.getId(), member.getRole());
            String verificationCode = sendCodeAndGetCode(member, UNIV_EMAIL);
            univEmailVerificationService.verifyMemberUnivEmail(new UnivEmailVerificationRequest(verificationCode));

            // when & then
            UnivEmailVerificationRequest request = new UnivEmailVerificationRequest(verificationCode);
            assertThatThrownBy(() -> univEmailVerificationService.verifyMemberUnivEmail(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(UNIV_EMAIL_VERIFICATION_CODE_NOT_SENT.getMessage());
        }
    }
}

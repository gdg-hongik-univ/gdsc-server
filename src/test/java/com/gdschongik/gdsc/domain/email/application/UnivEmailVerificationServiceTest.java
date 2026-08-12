package com.gdschongik.gdsc.domain.email.application;

import static com.gdschongik.gdsc.global.common.constant.TestMemberConstant.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

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

    private String sendCodeAndGetCode(Member member, String univEmail) {
        univEmailVerificationCodeSendService.send(univEmail);
        return univEmailVerificationService
                .getUnivEmailVerificationFromRedis(member.getId())
                .orElseThrow()
                .getCode();
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
        void 인증코드와_레디스에_존재하는_인증정보의_코드가_다르면_실패한다() {
            // given
            Member member = createGuestMember();
            logoutAndReloginAs(member.getId(), member.getRole());

            // when
            String oldVerificationCode = sendCodeAndGetCode(member, UNIV_EMAIL);
            UnivEmailVerificationRequest request = new UnivEmailVerificationRequest(oldVerificationCode);
            univEmailVerificationCodeSendService.send("b123456@g.hongik.ac.kr");

            // then
            assertThatThrownBy(() -> univEmailVerificationService.verifyMemberUnivEmail(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(EMAIL_VERIFICATION_CODE_MISMATCH.getMessage());
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

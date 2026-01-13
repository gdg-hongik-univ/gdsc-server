package com.gdschongik.gdsc.domain.email.application;

import static com.gdschongik.gdsc.global.common.constant.MemberConstant.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gdschongik.gdsc.domain.email.dto.request.UnivEmailVerificationRequest;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.global.util.email.EmailVerificationTokenUtil;
import com.gdschongik.gdsc.helper.IntegrationTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class UnivEmailVerificationServiceTest extends IntegrationTest {

    @Autowired
    private UnivEmailVerificationLinkSendService univEmailVerificationLinkSendService;

    @Autowired
    private UnivEmailVerificationService univEmailVerificationService;

    @Autowired
    private EmailVerificationTokenUtil emailVerificationTokenUtil;

    @Nested
    class 재학생_메일_인증시 {

        @Test
        void 레디스에_이메일인증정보가_존재하지_않으면_실패한다() {
            // given
            Member member = createGuestMember();
            memberRepository.save(member);
            String verificationToken =
                    emailVerificationTokenUtil.generateEmailVerificationToken(member.getId(), UNIV_EMAIL);
            UnivEmailVerificationRequest request = new UnivEmailVerificationRequest(verificationToken);

            // when & then
            assertThatThrownBy(() -> univEmailVerificationService.verifyMemberUnivEmail(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(EMAIL_NOT_SENT.getMessage());
        }

        @Test
        void 인증토큰과_레디스에_존재하는_인증정보의_토큰이_다르면_실패한다() {
            // given
            Member member = createGuestMember();
            logoutAndReloginAs(member.getId(), member.getRole());

            // when
            univEmailVerificationLinkSendService.send(UNIV_EMAIL);

            String oldVerificationToken = univEmailVerificationService
                    .getUnivEmailVerificationFromRedis(member.getId())
                    .get()
                    .getVerificationToken();
            UnivEmailVerificationRequest request = new UnivEmailVerificationRequest(oldVerificationToken);
            univEmailVerificationLinkSendService.send("b123456@g.hongik.ac.kr");

            // then
            assertThatThrownBy(() -> univEmailVerificationService.verifyMemberUnivEmail(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(EXPIRED_EMAIL_VERIFICATION_TOKEN.getMessage());
        }
    }
}

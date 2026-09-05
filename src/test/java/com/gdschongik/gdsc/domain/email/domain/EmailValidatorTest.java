package com.gdschongik.gdsc.domain.email.domain;

import static com.gdschongik.gdsc.global.common.constant.TestEmailConstant.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.domain.email.domain.service.EmailValidator;
import com.gdschongik.gdsc.global.exception.CustomException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class EmailValidatorTest {

    private static final Long CURRENT_MEMBER_ID = 1L;
    private static final Long PREVIOUS_MEMBER_ID = 2L;

    EmailValidator emailValidator = new EmailValidator();

    private EmailVerification createEmailVerification() {
        return EmailVerification.create(CURRENT_MEMBER_ID, PREVIOUS_MEMBER_ID, CODE, TTL);
    }

    @Nested
    class 본인_인증_코드_발송시 {

        @Test
        void 재발송_대기_시간이_지나지_않았으면_실패한다() {
            // when & then
            assertThatThrownBy(() -> emailValidator.validateSendEmailVerificationCode(
                            RESEND_WAIT_TIME_SECONDS - 1, CURRENT_MEMBER_ID, PREVIOUS_MEMBER_ID))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(EMAIL_VERIFICATION_CODE_RESEND_WAIT_TIME_NOT_PASSED.getMessage());
        }

        @Test
        void 이전_발송_이력이_없으면_성공한다() {
            // when & then
            assertThatCode(() -> emailValidator.validateSendEmailVerificationCode(
                            null, CURRENT_MEMBER_ID, PREVIOUS_MEMBER_ID))
                    .doesNotThrowAnyException();
        }

        @Test
        void 현재_계정과_과거_계정이_같으면_실패한다() {
            // when & then
            assertThatThrownBy(() -> emailValidator.validateSendEmailVerificationCode(
                            null, CURRENT_MEMBER_ID, CURRENT_MEMBER_ID))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(EMAIL_VERIFICATION_SAME_MEMBER.getMessage());
        }
    }

    @Nested
    class 본인_인증_코드_검증시 {

        @Test
        void 시도_횟수가_남아있고_코드가_일치하면_성공한다() {
            // given
            EmailVerification emailVerification = createEmailVerification();

            // when & then
            assertThatCode(() ->
                            emailValidator.validateEmailVerificationCode(emailVerification, CODE, MAX_ATTEMPT_COUNT))
                    .doesNotThrowAnyException();
        }

        @Test
        void 시도_횟수가_남아있고_코드가_일치하지_않으면_실패한다() {
            // given
            EmailVerification emailVerification = createEmailVerification();

            // when & then
            assertThatThrownBy(() -> emailValidator.validateEmailVerificationCode(
                            emailVerification, WRONG_CODE, MAX_ATTEMPT_COUNT))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(EMAIL_VERIFICATION_CODE_MISMATCH.getMessage());
        }

        @Test
        void 최대_시도_횟수를_초과하면_코드가_일치해도_실패한다() {
            // given
            EmailVerification emailVerification = createEmailVerification();

            // when & then
            assertThatThrownBy(() -> emailValidator.validateEmailVerificationCode(
                            emailVerification, CODE, MAX_ATTEMPT_COUNT + 1))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(EMAIL_VERIFICATION_CODE_ATTEMPT_EXCEEDED.getMessage());
        }
    }
}

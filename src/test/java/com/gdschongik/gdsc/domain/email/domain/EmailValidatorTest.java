package com.gdschongik.gdsc.domain.email.domain;

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

    @Nested
    class 본인_인증_코드_발송시 {

        @Test
        void 현재_계정과_과거_계정이_같으면_실패한다() {
            // when & then
            assertThatThrownBy(() ->
                            emailValidator.validateSendEmailVerificationCode(CURRENT_MEMBER_ID, CURRENT_MEMBER_ID))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(EMAIL_VERIFICATION_SAME_MEMBER.getMessage());
        }

        @Test
        void 현재_계정과_과거_계정이_다르면_성공한다() {
            // when & then
            assertThatCode(() ->
                            emailValidator.validateSendEmailVerificationCode(CURRENT_MEMBER_ID, PREVIOUS_MEMBER_ID))
                    .doesNotThrowAnyException();
        }
    }
}

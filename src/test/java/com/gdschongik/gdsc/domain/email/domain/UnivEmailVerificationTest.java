package com.gdschongik.gdsc.domain.email.domain;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.global.exception.CustomException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UnivEmailVerificationTest {

    private static final Long MEMBER_ID = 1L;
    private static final String UNIV_EMAIL = "test@g.hongik.ac.kr";
    private static final String CODE = "042917";
    private static final long TTL = 60L;

    @Nested
    class 인증_코드_검증시 {

        @Test
        void 코드가_일치하면_성공한다() {
            // given
            UnivEmailVerification univEmailVerification =
                    UnivEmailVerification.create(MEMBER_ID, UNIV_EMAIL, CODE, TTL);

            // when & then
            assertThatCode(() -> univEmailVerification.validateCode(CODE)).doesNotThrowAnyException();
        }

        @Test
        void 코드가_일치하지_않으면_실패한다() {
            // given
            UnivEmailVerification univEmailVerification =
                    UnivEmailVerification.create(MEMBER_ID, UNIV_EMAIL, CODE, TTL);

            // when & then
            assertThatThrownBy(() -> univEmailVerification.validateCode("999999"))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(EMAIL_VERIFICATION_CODE_MISMATCH.getMessage());
        }
    }
}

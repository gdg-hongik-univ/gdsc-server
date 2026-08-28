package com.gdschongik.gdsc.domain.email.domain;

import static com.gdschongik.gdsc.global.common.constant.TestEmailConstant.*;
import static com.gdschongik.gdsc.global.common.constant.TestMemberConstant.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.global.exception.CustomException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UnivEmailVerificationTest {

    private static final Long MEMBER_ID = 1L;

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
            assertThatThrownBy(() -> univEmailVerification.validateCode(WRONG_CODE))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(EMAIL_VERIFICATION_CODE_MISMATCH.getMessage());
        }
    }
}

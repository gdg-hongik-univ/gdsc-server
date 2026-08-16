package com.gdschongik.gdsc.domain.email.domain;

import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.domain.email.domain.service.VerificationCodeGenerator;
import java.security.SecureRandom;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class VerificationCodeGeneratorTest {

    @Nested
    class 인증_코드_생성시 {

        @Test
        void 난수를_6자리_0패딩_문자열로_변환한다() {
            // given
            VerificationCodeGenerator verificationCodeGenerator =
                    new VerificationCodeGenerator(new FixedSecureRandom(0, 1234, 999999));

            // when
            List<String> codes = List.of(
                    verificationCodeGenerator.generate(),
                    verificationCodeGenerator.generate(),
                    verificationCodeGenerator.generate());

            // then
            assertThat(codes).containsExactly("000000", "001234", "999999");
        }

        @Test
        void 생성할때마다_난수를_새로_요청한다() {
            // given
            VerificationCodeGenerator verificationCodeGenerator =
                    new VerificationCodeGenerator(new FixedSecureRandom(1, 2, 3));

            // when
            List<String> codes = List.of(
                    verificationCodeGenerator.generate(),
                    verificationCodeGenerator.generate(),
                    verificationCodeGenerator.generate());

            // then
            assertThat(codes).containsExactly("000001", "000002", "000003");
        }
    }

    /**
     * 지정한 값을 순서대로 반환하는 테스트용 SecureRandom
     */
    static class FixedSecureRandom extends SecureRandom {

        private final int[] values;
        private int index = 0;

        FixedSecureRandom(int... values) {
            this.values = values;
        }

        @Override
        public int nextInt(int bound) {
            return values[index++];
        }
    }
}

package com.gdschongik.gdsc.domain.email.domain;

import static com.gdschongik.gdsc.global.common.constant.RegexConstant.VERIFICATION_CODE;
import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.domain.email.domain.service.VerificationCodeGenerator;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class VerificationCodeGeneratorTest {

    VerificationCodeGenerator verificationCodeGenerator = new VerificationCodeGenerator();

    @Nested
    class 인증_코드_생성시 {

        @Test
        void 항상_6자리_숫자_문자열을_반환한다() {
            // when & then
            IntStream.range(0, 1000).forEach(i -> assertThat(verificationCodeGenerator.generate())
                    .matches(VERIFICATION_CODE));
        }

        @Test
        void 앞자리가_0인_코드도_생성된다() {
            // when
            boolean hasLeadingZero = IntStream.range(0, 1000)
                    .mapToObj(i -> verificationCodeGenerator.generate())
                    .anyMatch(code -> code.startsWith("0"));

            // then
            assertThat(hasLeadingZero).isTrue();
        }

        @Test
        void 매번_다른_코드가_생성된다() {
            // when
            long distinctCount = IntStream.range(0, 100)
                    .mapToObj(i -> verificationCodeGenerator.generate())
                    .distinct()
                    .count();

            // then
            assertThat(distinctCount).isGreaterThan(1);
        }
    }
}

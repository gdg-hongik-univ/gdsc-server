package com.gdschongik.gdsc.domain.email.domain;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.domain.email.domain.service.UnivEmailValidator;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UnivEmailValidatorTest {

    private static final Long MEMBER_ID = 1L;
    private static final String UNIV_EMAIL = "test@g.hongik.ac.kr";
    private static final String CODE = "042917";
    private static final String WRONG_CODE = "999999";
    private static final long TTL = 60L;
    private static final int MAX_ATTEMPT_COUNT = 5;

    UnivEmailValidator univEmailValidator = new UnivEmailValidator();

    private UnivEmailVerification createUnivEmailVerification() {
        return UnivEmailVerification.create(MEMBER_ID, UNIV_EMAIL, CODE, TTL);
    }

    @Test
    @DisplayName("'g.hongik.ac.kr' 도메인을 가진 이메일을 검증할 수 있다.")
    void validateEmailDomainTest() {
        // given
        String hongikDomainEmail = "te-st@g.hongik.ac.kr";

        // when & then
        assertThatCode(() -> univEmailValidator.validateSendUnivEmailVerificationCode(hongikDomainEmail, false))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"test@naver.com", "test@mail.hongik.ac.kr", "test@gmail.com", "test@gg.hongik.ac.kr"})
    @DisplayName("'g.hongik.ac.kr'가 아닌 도메인을 가진 이메일을 입력하면 예외를 발생시킨다.")
    void validateEmailDomainMismatchTest(String email) {
        // when & then
        assertThatThrownBy(() -> univEmailValidator.validateSendUnivEmailVerificationCode(email, false))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.UNIV_EMAIL_DOMAIN_MISMATCH.getMessage());
    }

    @Test
    @DisplayName("Email의 '@' 앞 부분에는 연속되지 않은 점이 포함될 수 있다.")
    void validateEmailFormatWithDotsTest() {
        // given
        String email = "t.e.s.t@g.hongik.ac.kr";

        // when & then
        assertThatCode(() -> univEmailValidator.validateSendUnivEmailVerificationCode(email, false))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "te&st@g.hongik.ac.kr",
                "te=st@g.hongik.ac.kr",
                "te'st@g.hongik.ac.kr",
                "te+st@g.hongik.ac.kr",
                "te,st@g.hongik.ac.kr",
                "te<st@g.hongik.ac.kr",
                "te>st@g.hongik.ac.kr"
            })
    @DisplayName("Email의 '@' 앞 부분에 '&', '=', ''', '+', ',', '<', '>'가 포함되는 경우 예외를 발생시킨다.")
    void validateEmailFormatMismatchTest(String email) {
        // when & then
        assertThatThrownBy(() -> univEmailValidator.validateSendUnivEmailVerificationCode(email, false))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.UNIV_EMAIL_FORMAT_MISMATCH.getMessage());
    }

    @Test
    @DisplayName("Email의 '@' 앞 부분에 '.'이 2개 연속 오는 경우 예외를 발생시킨다.")
    void validateEmailFormatMismatchWithDotsTest() {
        // given
        String email = "te..st@g.hongik.ac.kr";

        // when & then
        assertThatThrownBy(() -> univEmailValidator.validateSendUnivEmailVerificationCode(email, false))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.UNIV_EMAIL_FORMAT_MISMATCH.getMessage());
    }

    @Test
    void 이미_가입된_재학생_메일이라면_실패한다() {
        // given
        String hongikDomainEmail = "test@g.hongik.ac.kr";

        // when & then
        assertThatThrownBy(() -> univEmailValidator.validateSendUnivEmailVerificationCode(hongikDomainEmail, true))
                .isInstanceOf(CustomException.class)
                .hasMessage(UNIV_EMAIL_ALREADY_SATISFIED.getMessage());
    }

    @Nested
    class 재학생_인증_코드_검증시 {

        @Test
        void 시도_횟수가_남아있고_코드가_일치하면_성공한다() {
            // given
            UnivEmailVerification univEmailVerification = createUnivEmailVerification();

            // when & then
            assertThatCode(() -> univEmailValidator.validateUnivEmailVerificationCode(
                            univEmailVerification, CODE, MAX_ATTEMPT_COUNT))
                    .doesNotThrowAnyException();
        }

        @Test
        void 시도_횟수가_남아있고_코드가_일치하지_않으면_실패한다() {
            // given
            UnivEmailVerification univEmailVerification = createUnivEmailVerification();

            // when & then
            assertThatThrownBy(() -> univEmailValidator.validateUnivEmailVerificationCode(
                            univEmailVerification, WRONG_CODE, MAX_ATTEMPT_COUNT))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(EMAIL_VERIFICATION_CODE_MISMATCH.getMessage());
        }

        @Test
        void 최대_시도_횟수를_초과하면_코드가_일치해도_실패한다() {
            // given
            UnivEmailVerification univEmailVerification = createUnivEmailVerification();

            // when & then
            assertThatThrownBy(() -> univEmailValidator.validateUnivEmailVerificationCode(
                            univEmailVerification, CODE, MAX_ATTEMPT_COUNT + 1))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(EMAIL_VERIFICATION_CODE_ATTEMPT_EXCEEDED.getMessage());
        }
    }
}

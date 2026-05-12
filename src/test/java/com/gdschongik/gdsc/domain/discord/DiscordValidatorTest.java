package com.gdschongik.gdsc.domain.discord;

import static com.gdschongik.gdsc.global.common.constant.TestDiscordConstant.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.domain.discord.domain.DiscordVerificationCode;
import com.gdschongik.gdsc.domain.discord.domain.service.DiscordValidator;
import com.gdschongik.gdsc.global.exception.CustomException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class DiscordValidatorTest {

    DiscordValidator discordValidator = new DiscordValidator();

    @Nested
    class 디스코드_연동시 {

        @Test
        void 인증코드가_일치하지_않는다면_실패한다() {
            // given
            DiscordVerificationCode discordVerificationCode =
                    DiscordVerificationCode.create(DISCORD_USERNAME, DISCORD_CODE, DISCORD_CODE_TTL);

            // when & then
            assertThatThrownBy(() ->
                            discordValidator.validateVerifyDiscordCode(1235, discordVerificationCode, false, false))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(DISCORD_CODE_MISMATCH.getMessage());
        }

        @Test
        void 이미_존재하는_디스코드_유저네임이라면_실패한다() {
            // given
            DiscordVerificationCode discordVerificationCode =
                    DiscordVerificationCode.create(DISCORD_USERNAME, DISCORD_CODE, DISCORD_CODE_TTL);

            // when & then
            assertThatThrownBy(() -> discordValidator.validateVerifyDiscordCode(
                            DISCORD_CODE, discordVerificationCode, true, false))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(MEMBER_DISCORD_USERNAME_DUPLICATE.getMessage());
        }

        @Test
        void 이미_존재하는_닉네임이라면_실패한다() {
            // given
            DiscordVerificationCode discordVerificationCode =
                    DiscordVerificationCode.create(DISCORD_USERNAME, DISCORD_CODE, DISCORD_CODE_TTL);

            // when & then
            assertThatThrownBy(() -> discordValidator.validateVerifyDiscordCode(
                            DISCORD_CODE, discordVerificationCode, false, true))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(MEMBER_NICKNAME_DUPLICATE.getMessage());
        }
    }
}

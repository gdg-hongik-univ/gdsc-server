package com.gdschongik.gdsc.domain.discord.application;

import static com.gdschongik.gdsc.domain.discord.domain.DiscordVerificationCode.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.discord.dao.DiscordVerificationCodeRepository;
import com.gdschongik.gdsc.domain.discord.domain.DiscordVerificationCode;
import com.gdschongik.gdsc.domain.discord.domain.service.DiscordValidator;
import com.gdschongik.gdsc.domain.discord.dto.request.DiscordLinkRequest;
import com.gdschongik.gdsc.domain.discord.dto.response.DiscordCheckDuplicateResponse;
import com.gdschongik.gdsc.domain.discord.dto.response.DiscordCheckJoinResponse;
import com.gdschongik.gdsc.domain.discord.dto.response.DiscordVerificationCodeResponse;
import com.gdschongik.gdsc.domain.member.dao.MemberRepository;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.global.util.DiscordUtil;
import com.gdschongik.gdsc.global.util.MemberUtil;
import java.security.SecureRandom;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnboardingDiscordService {

    public static final long DISCORD_CODE_TTL_SECONDS = 300L;

    private final DiscordVerificationCodeRepository discordVerificationCodeRepository;
    private final MemberUtil memberUtil;
    private final DiscordUtil discordUtil;
    private final MemberRepository memberRepository;
    private final DiscordValidator discordValidator;

    @Transactional
    public DiscordVerificationCodeResponse createVerificationCode(String discordUsername) {

        Integer code = generateRandomCode();
        DiscordVerificationCode discordVerificationCode =
                DiscordVerificationCode.create(discordUsername, code, DISCORD_CODE_TTL_SECONDS);

        discordVerificationCodeRepository.save(discordVerificationCode);

        return DiscordVerificationCodeResponse.of(code, DISCORD_CODE_TTL_SECONDS);
    }

    @SneakyThrows
    private static Integer generateRandomCode() {
        return SecureRandom.getInstanceStrong()
                .ints(MIN_CODE_RANGE, MAX_CODE_RANGE + 1)
                .findFirst()
                .orElseThrow();
    }

    @Transactional
    public void verifyDiscordCode(DiscordLinkRequest request) {
        DiscordVerificationCode discordVerificationCode = discordVerificationCodeRepository
                .findById(request.discordUsername())
                .orElseThrow(() -> new CustomException(DISCORD_CODE_NOT_FOUND));

        boolean isDiscordUsernameDuplicate = isDiscordUsernameDuplicate(request.discordUsername());
        boolean isNicknameDuplicate = isNicknameDuplicate(request.nickname());

        discordValidator.validateVerifyDiscordCode(
                request.code(), discordVerificationCode, isDiscordUsernameDuplicate, isNicknameDuplicate);

        discordVerificationCodeRepository.delete(discordVerificationCode);

        final Member currentMember = memberUtil.getCurrentMember();
        currentMember.verifyDiscord(request.discordUsername(), request.nickname());

        updateDiscordId(request.discordUsername(), currentMember);

        memberRepository.save(currentMember);

        log.info("[OnboardingDiscordService] 디스코드 연동: memberId={}", currentMember.getId());
    }

    private void updateDiscordId(String discordUsername, Member currentMember) {
        String discordId = discordUtil.getMemberIdByUsername(discordUsername);
        currentMember.updateDiscordId(discordId);
    }

    @Transactional(readOnly = true)
    public DiscordCheckDuplicateResponse checkUsernameDuplicate(String discordUsername) {
        boolean isDuplicate = isDiscordUsernameDuplicate(discordUsername);
        return DiscordCheckDuplicateResponse.from(isDuplicate);
    }

    private boolean isDiscordUsernameDuplicate(String discordUsername) {
        if (isMyDiscordUsername(discordUsername)) {
            return false;
        }
        return memberRepository.existsByDiscordUsername(discordUsername);
    }

    private boolean isMyDiscordUsername(String discordUsername) {
        Member currentMember = memberUtil.getCurrentMember();
        String currentDiscordUsername = currentMember.getDiscordUsername();
        return discordUsername.equals(currentDiscordUsername);
    }

    @Transactional(readOnly = true)
    public DiscordCheckDuplicateResponse checkNicknameDuplicate(String nickname) {
        boolean isDuplicate = isNicknameDuplicate(nickname);
        return DiscordCheckDuplicateResponse.from(isDuplicate);
    }

    private boolean isNicknameDuplicate(String nickname) {
        if (isMyNickname(nickname)) {
            return false;
        }
        return memberRepository.existsByNickname(nickname);
    }

    private boolean isMyNickname(String nickname) {
        Member currentMember = memberUtil.getCurrentMember();
        String currentNickname = currentMember.getNickname();
        return nickname.equals(currentNickname);
    }

    public DiscordCheckJoinResponse checkServerJoined(String discordUsername) {
        boolean isJoined =
                discordUtil.getOptionalMemberByUsername(discordUsername).isPresent();
        return DiscordCheckJoinResponse.from(isJoined);
    }
}

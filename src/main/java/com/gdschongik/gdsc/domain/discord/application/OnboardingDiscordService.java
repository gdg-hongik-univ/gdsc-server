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
import com.gdschongik.gdsc.domain.member.domain.service.MemberDomainService;
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
    private final MemberDomainService memberDomainService;

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

        Member currentMember = memberUtil.getCurrentMember();

        boolean isDiscordUsernameDuplicate = isDiscordUsernameDuplicate(request.discordUsername(), currentMember);
        boolean isNicknameDuplicate = isNicknameDuplicate(request.nickname(), currentMember);

        discordValidator.validateVerifyDiscordCode(
                request.code(), discordVerificationCode, isDiscordUsernameDuplicate, isNicknameDuplicate);

        discordVerificationCodeRepository.delete(discordVerificationCode);

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
        Member currentMember = memberUtil.getCurrentMember();
        boolean isDuplicate = isDiscordUsernameDuplicate(discordUsername, currentMember);
        return DiscordCheckDuplicateResponse.from(isDuplicate);
    }

    private boolean isDiscordUsernameDuplicate(String discordUsername, Member currentMember) {
        boolean alreadyExists = memberRepository.existsByDiscordUsername(discordUsername);
        return memberDomainService.conflictsWithOtherDiscordUsername(currentMember, discordUsername, alreadyExists);
    }

    @Transactional(readOnly = true)
    public DiscordCheckDuplicateResponse checkNicknameDuplicate(String nickname) {
        Member currentMember = memberUtil.getCurrentMember();
        boolean isDuplicate = isNicknameDuplicate(nickname, currentMember);
        return DiscordCheckDuplicateResponse.from(isDuplicate);
    }

    private boolean isNicknameDuplicate(String nickname, Member currentMember) {
        boolean alreadyExists = memberRepository.existsByNickname(nickname);
        return memberDomainService.conflictsWithOtherNickname(currentMember, nickname, alreadyExists);
    }

    public DiscordCheckJoinResponse checkServerJoined(String discordUsername) {
        boolean isJoined =
                discordUtil.getOptionalMemberByUsername(discordUsername).isPresent();
        return DiscordCheckJoinResponse.from(isJoined);
    }
}

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
    public void linkDiscord(DiscordLinkRequest request) {
        DiscordVerificationCode discordVerificationCode = discordVerificationCodeRepository
                .findById(request.discordUsername())
                .orElseThrow(() -> new CustomException(DISCORD_CODE_NOT_FOUND));

        Member currentMember = memberUtil.getCurrentMember();
        boolean discordUsernameAlreadyExists = memberRepository.existsByDiscordUsername(request.discordUsername());
        boolean nicknameAlreadyExists = memberRepository.existsByNickname(request.nickname());

        boolean isDiscordUsernameDuplicate = memberDomainService.determineDiscordUsernameDuplicate(
                currentMember, request.discordUsername(), discordUsernameAlreadyExists);
        boolean isNicknameDuplicate = memberDomainService.determineNicknameDuplicate(
                currentMember, request.nickname(), nicknameAlreadyExists);

        discordValidator.validateVerifyDiscordCode(
                request.code(), discordVerificationCode, isDiscordUsernameDuplicate, isNicknameDuplicate);

        discordVerificationCodeRepository.delete(discordVerificationCode);

        String discordId = discordUtil.getMemberIdByUsername(request.discordUsername());

        verifyOrChangeDiscord(currentMember, request.discordUsername(), request.nickname(), discordId);
    }

    private void verifyOrChangeDiscord(Member member, String discordUsername, String nickname, String discordId) {
        boolean isDiscordAlreadySatisfied = member.getAssociateRequirement().isDiscordSatisfied();

        if (isDiscordAlreadySatisfied) {
            member.changeDiscord(discordUsername, nickname, discordId);
            memberRepository.save(member);
            log.info("[OnboardingDiscordService] 디스코드 재연동: memberId={}", member.getId());
        } else {
            member.verifyDiscord(discordUsername, nickname, discordId);
            memberRepository.save(member);
            log.info("[OnboardingDiscordService] 디스코드 연동: memberId={}", member.getId());
        }
    }

    @Transactional(readOnly = true)
    public DiscordCheckDuplicateResponse checkUsernameDuplicate(String discordUsername) {
        Member currentMember = memberUtil.getCurrentMember();
        boolean discordUsernameAlreadyExists = memberRepository.existsByDiscordUsername(discordUsername);

        boolean isDuplicate = memberDomainService.determineDiscordUsernameDuplicate(
                currentMember, discordUsername, discordUsernameAlreadyExists);

        return DiscordCheckDuplicateResponse.from(isDuplicate);
    }

    @Transactional(readOnly = true)
    public DiscordCheckDuplicateResponse checkNicknameDuplicate(String nickname) {
        Member currentMember = memberUtil.getCurrentMember();
        boolean nicknameAlreadyExists = memberRepository.existsByNickname(nickname);

        boolean isDuplicate =
                memberDomainService.determineNicknameDuplicate(currentMember, nickname, nicknameAlreadyExists);

        return DiscordCheckDuplicateResponse.from(isDuplicate);
    }

    public DiscordCheckJoinResponse checkServerJoined(String discordUsername) {
        boolean isJoined =
                discordUtil.getOptionalMemberByUsername(discordUsername).isPresent();
        return DiscordCheckJoinResponse.from(isJoined);
    }
}

package com.gdschongik.gdsc.domain.member.domain.service;

import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.global.annotation.DomainService;

@DomainService
public class MemberDomainService {

    /**
     * 주어진 member를 제외한 다른 멤버와의 디스코드 유저네임 중복 여부를 판단합니다.
     * 주어진 member의 디스코드 유저네임이 아니면서 이미 존재하는 유저네임이라면 중복이라고 판단합니다.
     */
    public boolean determineDiscordUsernameDuplicate(
            Member member, String discordUsernameToUse, boolean discordUsernameAlreadyExists) {
        boolean isMyDiscordUsername =
                discordUsernameToUse != null && discordUsernameToUse.equals(member.getDiscordUsername());
        return !isMyDiscordUsername && discordUsernameAlreadyExists;
    }

    /**
     * 주어진 member를 제외한 다른 멤버와의 닉네임 중복 여부를 판단합니다.
     * 주어진 member의 닉네임이 아니면서 이미 존재하는 닉네임이라면 중복이라고 판단합니다.
     */
    public boolean determineNicknameDuplicate(Member member, String nicknameToUse, boolean nicknameAlreadyExists) {
        boolean isMyNickname = nicknameToUse != null && nicknameToUse.equals(member.getNickname());
        return !isMyNickname && nicknameAlreadyExists;
    }
}

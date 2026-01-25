package com.gdschongik.gdsc.domain.discord.application.handler;

import static com.gdschongik.gdsc.global.common.constant.DiscordConstant.*;

import com.gdschongik.gdsc.domain.member.domain.event.MemberDemotedToAssociateEvent;
import com.gdschongik.gdsc.domain.member.domain.event.MemberDiscordIdUpdatedEvent;
import com.gdschongik.gdsc.global.util.DiscordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberDiscordRoleRevokeHandler implements SpringEventHandler {

    private final DiscordUtil discordUtil;

    @Override
    public void delegate(Object context) {
        if (context instanceof MemberDemotedToAssociateEvent event) {
            revokeDiscordRole(event.memberId(), event.discordId());
        } else if (context instanceof MemberDiscordIdUpdatedEvent event) {
            revokeDiscordRole(event.memberId(), event.previousDiscordId());
        }
    }

    public void revokeDiscordRole(Long memberId, String discordId) {
        Guild guild = discordUtil.getCurrentGuild();
        Member member = discordUtil.getMemberById(discordId);
        Role role = discordUtil.findRoleByName(MEMBER_ROLE_NAME);

        guild.removeRoleFromMember(member, role).queue();

        log.info(
                "[MemberDiscordRoleRevokeHandler] 디스코드 서버 정회원 역할 제거 완료: memberId={}, discordId={}",
                memberId,
                discordId);
    }
}

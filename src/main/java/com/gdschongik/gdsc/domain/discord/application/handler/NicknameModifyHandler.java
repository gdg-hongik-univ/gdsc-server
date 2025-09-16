package com.gdschongik.gdsc.domain.discord.application.handler;

import com.gdschongik.gdsc.domain.discord.application.CommonDiscordService;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NicknameModifyHandler implements DiscordEventHandler {

    private final CommonDiscordService commonDiscordService;

    @Override
    public void delegate(GenericEvent genericEvent) {
        GuildMemberUpdateNicknameEvent event = (GuildMemberUpdateNicknameEvent) genericEvent;

        Member member = event.getMember();
        Guild guild = event.getGuild();
        String discordUsername = member.getUser().getName();

        String originalNickname = commonDiscordService.getNicknameByDiscordUsername(discordUsername);
        String newNickname = event.getNewNickname();

        if (originalNickname == null) {
            throw new CustomException(ErrorCode.DISCORD_NOT_SIGNUP);
        }

        if (newNickname == null) {
            throw new CustomException(ErrorCode.DISCORD_NICKNAME_NOT_NULL);
        }

        if (newNickname.equals(originalNickname)) {
            return;
        }

        guild.modifyNickname(member, originalNickname).queue();
    }
}

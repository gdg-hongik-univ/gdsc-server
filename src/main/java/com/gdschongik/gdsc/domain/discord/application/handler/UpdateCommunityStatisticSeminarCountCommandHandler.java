package com.gdschongik.gdsc.domain.discord.application.handler;

import static com.gdschongik.gdsc.global.common.constant.DiscordConstant.*;

import com.gdschongik.gdsc.domain.statistic.application.CommunityStatisticService;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateCommunityStatisticSeminarCountCommandHandler implements DiscordEventHandler {

    private final CommunityStatisticService communityStatisticService;

    @Override
    public void delegate(GenericEvent genericEvent) {
        SlashCommandInteractionEvent event = (SlashCommandInteractionEvent) genericEvent;
        event.deferReply(true).setContent(DEFER_MESSAGE_UPDATE_SEMINAR_COUNT).queue();

        String currentMemberDiscordUsername = event.getUser().getName();
        Integer totalSeminarCount = Objects.requireNonNull(event.getOption(OPTION_NAME_SEMINAR_COUNT))
                .getAsInt();

        communityStatisticService.updateTotalSeminarCount(currentMemberDiscordUsername, totalSeminarCount);

        event.getHook()
                .sendMessage(REPLY_MESSAGE_UPDATE_SEMINAR_COUNT)
                .setEphemeral(true)
                .queue();
    }
}

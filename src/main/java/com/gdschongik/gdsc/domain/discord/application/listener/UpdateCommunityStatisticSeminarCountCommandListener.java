package com.gdschongik.gdsc.domain.discord.application.listener;

import static com.gdschongik.gdsc.global.common.constant.DiscordConstant.*;

import com.gdschongik.gdsc.domain.discord.application.handler.UpdateCommunityStatisticSeminarCountCommandHandler;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Component
@Listener
@RequiredArgsConstructor
public class UpdateCommunityStatisticSeminarCountCommandListener extends ListenerAdapter {

    private final UpdateCommunityStatisticSeminarCountCommandHandler updateCommunityStatisticSeminarCountCommandHandler;

    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals(COMMAND_NAME_UPDATE_SEMINAR_COUNT)) {
            updateCommunityStatisticSeminarCountCommandHandler.delegate(event);
        }
    }
}

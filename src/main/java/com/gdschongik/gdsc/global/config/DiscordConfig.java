package com.gdschongik.gdsc.global.config;

import static com.gdschongik.gdsc.global.common.constant.DiscordConstant.*;
import static net.dv8tion.jda.api.interactions.commands.OptionType.*;

import com.gdschongik.gdsc.domain.discord.application.listener.ListenerBeanPostProcessor;
import com.gdschongik.gdsc.global.property.DiscordProperty;
import com.gdschongik.gdsc.global.util.DiscordUtil;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@RequiredArgsConstructor
public class DiscordConfig {

    private final DiscordProperty discordProperty;

    @Bean
    @SneakyThrows
    @ConditionalOnProperty(value = "discord.enabled", havingValue = "true", matchIfMissing = true)
    public JDA jda() {
        JDA jda = JDABuilder.createDefault(discordProperty.getToken())
                .setActivity(Activity.playing(DISCORD_BOT_STATUS_CONTENT))
                .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .build();

        // TODO: ready 후 리스너 빈이 초기화되면 이벤트 수신 불가하므로, @PostConstruct로 ReadyListener 사용할 수 있도록 변경
        Objects.requireNonNull(jda.awaitReady().getGuildById(discordProperty.getServerId()))
                .updateCommands()
                .addCommands(Commands.slash(COMMAND_NAME_ISSUING_CODE, COMMAND_DESCRIPTION_ISSUING_CODE))
                .addCommands(Commands.slash(COMMAND_NAME_BATCH_DISCORD_ID, COMMAND_DESCRIPTION_BATCH_DISCORD_ID))
                .addCommands(
                        Commands.slash(COMMAND_NAME_ADVANCE_FAILED_MEMBER, COMMAND_DESCRIPTION_ADVANCE_FAILED_MEMBER))
                .addCommands(Commands.slash(COMMAND_NAME_ASSIGN_ADMIN_ROLE, COMMAND_DESCRIPTION_ASSIGN_ADMIN_ROLE)
                        .addOption(STRING, OPTION_NAME_ASSIGN_ADMIN_ROLE, OPTION_DESCRIPTION_ASSIGN_ADMIN_ROLE, true))
                .addCommands(Commands.slash(COMMAND_NAME_ASSIGN_STUDY_ROLE, COMMAND_DESCRIPTION_ASSIGN_STUDY_ROLE)
                        .addOption(STRING, OPTION_NAME_STUDY_TITLE, OPTION_DESCRIPTION_STUDY_TITLE, true)
                        .addOption(INTEGER, OPTION_NAME_STUDY_YEAR, OPTION_DESCRIPTION_STUDY_YEAR, true)
                        .addOption(STRING, OPTION_NAME_STUDY_SEMESTER, OPTION_DESCRIPTION_STUDY_SEMESTER, true))
                .addCommands(Commands.slash(COMMAND_NAME_UPDATE_SEMINAR_COUNT, COMMAND_DESCRIPTION_UPDATE_SEMINAR_COUNT)
                        .addOption(INTEGER, OPTION_NAME_SEMINAR_COUNT, OPTION_DESCRIPTION_SEMINAR_COUNT, true))
                .queue();

        return jda;
    }

    @Bean
    @ConditionalOnBean(JDA.class)
    public ListenerBeanPostProcessor listenerBeanPostProcessor(JDA jda) {
        return new ListenerBeanPostProcessor(jda);
    }

    @Bean
    @ConditionalOnBean(JDA.class)
    public DiscordUtil discordUtil(JDA jda, DiscordProperty discordProperty) {
        return new DiscordUtil(jda, discordProperty);
    }

    @Bean
    @Order(1)
    public DiscordUtil fallbackDiscordUtil() {
        return new DiscordUtil(null, null);
    }
}

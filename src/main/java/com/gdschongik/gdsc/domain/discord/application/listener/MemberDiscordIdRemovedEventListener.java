package com.gdschongik.gdsc.domain.discord.application.listener;

import com.gdschongik.gdsc.domain.discord.application.handler.MemberDiscordRoleRevokeHandler;
import com.gdschongik.gdsc.domain.member.domain.event.MemberDiscordIdRemovedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberDiscordIdRemovedEventListener {

    private final MemberDiscordRoleRevokeHandler memberDiscordRoleRevokeHandler;

    @ApplicationModuleListener
    public void removeDiscordId(MemberDiscordIdRemovedEvent event) {
        log.info(
                "[MemberDiscordIdRemovedEventListener] 회원 디스코드 ID 제거 이벤트 수신: memberId={}, discordId={}",
                event.memberId(),
                event.discordId());
        memberDiscordRoleRevokeHandler.delegate(event);
    }
}

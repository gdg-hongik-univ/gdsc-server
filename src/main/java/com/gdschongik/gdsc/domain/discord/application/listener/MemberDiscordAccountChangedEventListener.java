package com.gdschongik.gdsc.domain.discord.application.listener;

import com.gdschongik.gdsc.domain.discord.application.handler.MemberDiscordRoleRevokeHandler;
import com.gdschongik.gdsc.domain.member.domain.event.MemberDiscordAccountChangedEvent;
import com.gdschongik.gdsc.domain.member.domain.event.MemberDiscordIdRemovedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberDiscordAccountChangedEventListener {

    private final MemberDiscordRoleRevokeHandler memberDiscordRoleRevokeHandler;

    @ApplicationModuleListener
    public void changeDiscordAccount(MemberDiscordAccountChangedEvent event) {
        log.info(
                "[MemberDiscordAccountChangedEventListener] 회원 디스코드 계정 변경 이벤트 수신: memberId={}, previousDiscordId={}, currentDiscordId={}",
                event.memberId(),
                event.previousDiscordId(),
                event.currentDiscordId());
        memberDiscordRoleRevokeHandler.delegate(event);
    }
}

package com.gdschongik.gdsc.domain.event.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Event - Participant", description = "참가자용 행사 관리 API입니다.")
@RestController
@RequestMapping("/participant/events")
@RequiredArgsConstructor
public class ParticipantEventController {}

package com.gdschongik.gdsc.domain.study.dto.response;

import com.gdschongik.gdsc.domain.study.dto.dto.StudyCommonDto;
import com.gdschongik.gdsc.domain.study.dto.dto.StudySessionStudentDto;
import java.util.List;

public record StudyStudentResponse(StudyCommonDto study, List<StudySessionStudentDto> studySessions) {}

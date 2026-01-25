package com.gdschongik.gdsc.domain.study.dto.response;

import com.gdschongik.gdsc.domain.study.domain.Study;
import com.gdschongik.gdsc.domain.study.dto.dto.StudyManagerDto;
import com.gdschongik.gdsc.domain.study.dto.dto.StudySessionManagerDto;
import java.util.List;

public record StudyManagerResponse(StudyManagerDto study, List<StudySessionManagerDto> studySessions) {
    public static StudyManagerResponse from(Study study) {
        return new StudyManagerResponse(
                StudyManagerDto.from(study),
                study.getStudySessions().stream()
                        .map(StudySessionManagerDto::from)
                        .toList());
    }
}

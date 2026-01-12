package com.gdschongik.gdsc.domain.study.dto.dto;

import com.gdschongik.gdsc.domain.study.domain.StudyHistory;

public record StudyHistorySimpleDto(String repositoryLink) {
    public static StudyHistorySimpleDto from(StudyHistory studyHistory) {
        return new StudyHistorySimpleDto(studyHistory.getRepositoryLink());
    }
}

package com.gdschongik.gdsc.domain.study.dto.dto;

import com.gdschongik.gdsc.domain.study.domain.StudyHistory;
import com.gdschongik.gdsc.domain.study.domain.StudyHistoryStatus;

public record StudyHistoryDto(
        Long studyHistoryId, StudyHistoryStatus status, String githubLink, Long memberId, Long studyId) {
    public static StudyHistoryDto from(StudyHistory studyHistory) {
        return new StudyHistoryDto(
                studyHistory.getId(),
                studyHistory.getStatus(),
                studyHistory.getRepositoryLink(),
                studyHistory.getStudent().getId(),
                studyHistory.getStudy().getId());
    }
}

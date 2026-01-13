package com.gdschongik.gdsc.domain.study.dto.response;

import com.gdschongik.gdsc.domain.study.domain.Study;
import com.gdschongik.gdsc.domain.study.domain.StudyAchievement;
import com.gdschongik.gdsc.domain.study.domain.StudyHistory;
import com.gdschongik.gdsc.domain.study.dto.dto.StudyAchievementDto;
import com.gdschongik.gdsc.domain.study.dto.dto.StudyCommonDto;
import com.gdschongik.gdsc.domain.study.dto.dto.StudyHistoryDto;
import java.util.List;

public record StudyHistoryMyResponse(
        StudyHistoryDto studyHistory, StudyCommonDto study, List<StudyAchievementDto> achievements) {
    public static StudyHistoryMyResponse of(
            StudyHistory studyHistory, Study study, List<StudyAchievement> achievements) {
        return new StudyHistoryMyResponse(
                StudyHistoryDto.from(studyHistory),
                StudyCommonDto.from(study),
                achievements.stream().map(StudyAchievementDto::from).toList());
    }
}

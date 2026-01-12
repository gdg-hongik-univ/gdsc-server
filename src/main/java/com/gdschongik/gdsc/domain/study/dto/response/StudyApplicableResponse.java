package com.gdschongik.gdsc.domain.study.dto.response;

import com.gdschongik.gdsc.domain.study.domain.Study;
import com.gdschongik.gdsc.domain.study.domain.StudyHistory;
import com.gdschongik.gdsc.domain.study.dto.dto.StudyCommonDto;
import java.util.List;

public record StudyApplicableResponse(List<Long> appliedStudyIds, List<StudyCommonDto> applicableStudies) {
    public static StudyApplicableResponse of(List<StudyHistory> studyHistories, List<Study> applicableStudies) {
        return new StudyApplicableResponse(
                studyHistories.stream()
                        .map(studyHistory -> studyHistory.getStudy().getId())
                        .toList(),
                applicableStudies.stream().map(StudyCommonDto::from).toList());
    }
}

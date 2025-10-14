package com.gdschongik.gdsc.domain.studyv2.application;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.studyv2.dao.AssignmentHistoryV2Repository;
import com.gdschongik.gdsc.domain.studyv2.dao.AttendanceV2Repository;
import com.gdschongik.gdsc.domain.studyv2.dao.StudyV2Repository;
import com.gdschongik.gdsc.domain.studyv2.domain.StudyV2;
import com.gdschongik.gdsc.domain.studyv2.dto.dto.StudyCommonDto;
import com.gdschongik.gdsc.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommonStudyServiceV2 {

    private final StudyV2Repository studyV2Repository;
    private final AttendanceV2Repository attendanceV2Repository;
    private final AssignmentHistoryV2Repository assignmentHistoryV2Repository;

    public StudyCommonDto getStudyInformation(Long studyId) {
        StudyV2 study =
                studyV2Repository.findFetchById(studyId).orElseThrow(() -> new CustomException(STUDY_NOT_FOUND));
        return StudyCommonDto.from(study);
    }

    @Transactional
    public void deleteAttendance(Long studyId, Long memberId) {
        attendanceV2Repository.deleteByStudyIdAndMemberId(studyId, memberId);
    }

    @Transactional
    public void deleteAssignmentHistory(Long studyId, Long memberId) {
        assignmentHistoryV2Repository.deleteByStudyIdAndMemberId(studyId, memberId);
    }
}

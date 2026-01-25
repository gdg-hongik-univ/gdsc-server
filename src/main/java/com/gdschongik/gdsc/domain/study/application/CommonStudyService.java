package com.gdschongik.gdsc.domain.study.application;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.study.dao.AssignmentHistoryRepository;
import com.gdschongik.gdsc.domain.study.dao.AttendanceRepository;
import com.gdschongik.gdsc.domain.study.dao.StudyRepository;
import com.gdschongik.gdsc.domain.study.domain.Study;
import com.gdschongik.gdsc.domain.study.dto.dto.StudyCommonDto;
import com.gdschongik.gdsc.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommonStudyService {

    private final StudyRepository studyRepository;
    private final AttendanceRepository attendanceRepository;
    private final AssignmentHistoryRepository assignmentHistoryRepository;

    public StudyCommonDto getStudyInformation(Long studyId) {
        Study study = studyRepository.findFetchById(studyId).orElseThrow(() -> new CustomException(STUDY_NOT_FOUND));
        return StudyCommonDto.from(study);
    }

    @Transactional
    public void deleteAttendance(Long studyId, Long memberId) {
        attendanceRepository.deleteByStudyIdAndMemberId(studyId, memberId);
    }

    @Transactional
    public void deleteAssignmentHistory(Long studyId, Long memberId) {
        assignmentHistoryRepository.deleteByStudyIdAndMemberId(studyId, memberId);
    }
}

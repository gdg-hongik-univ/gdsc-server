package com.gdschongik.gdsc.domain.study.application;

import static com.gdschongik.gdsc.global.exception.ErrorCode.STUDY_NOT_FOUND;

import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.study.dao.AttendanceRepository;
import com.gdschongik.gdsc.domain.study.dao.StudyHistoryRepository;
import com.gdschongik.gdsc.domain.study.dao.StudyRepository;
import com.gdschongik.gdsc.domain.study.domain.Attendance;
import com.gdschongik.gdsc.domain.study.domain.Study;
import com.gdschongik.gdsc.domain.study.domain.StudySession;
import com.gdschongik.gdsc.domain.study.domain.service.AttendanceValidator;
import com.gdschongik.gdsc.domain.study.dto.request.AttendanceCreateRequest;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.global.util.MemberUtil;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentAttendanceService {

    private final MemberUtil memberUtil;
    private final StudyRepository studyRepository;
    private final AttendanceRepository attendanceRepository;
    private final StudyHistoryRepository studyHistoryRepository;
    private final AttendanceValidator attendanceValidator;

    @Transactional
    public void attend(Long studySessionId, AttendanceCreateRequest request) {
        Member currentMember = memberUtil.getCurrentMember();
        Study study = studyRepository
                .findFetchBySessionId(studySessionId)
                .orElseThrow(() -> new CustomException(STUDY_NOT_FOUND));
        StudySession studySession = study.getStudySession(studySessionId);

        LocalDateTime now = LocalDateTime.now();

        boolean isAppliedToStudy = studyHistoryRepository.existsByStudentAndStudy(currentMember, study);
        boolean isAlreadyAttended = attendanceRepository.existsByStudentAndStudySession(currentMember, studySession);

        attendanceValidator.validateAttendance(
                studySession, request.attendanceNumber(), isAppliedToStudy, isAlreadyAttended, now);

        Attendance attendance = Attendance.create(currentMember, studySession);
        attendanceRepository.save(attendance);

        log.info(
                "[StudentAttendanceService] 스터디 출석체크: attendanceId={}, memberId={}",
                attendance.getId(),
                currentMember.getId());
    }
}

package com.gdschongik.gdsc.domain.study.dao;

import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.study.domain.Attendance;
import com.gdschongik.gdsc.domain.study.domain.StudySession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<Attendance, Long>, AttendanceCustomRepository {
    boolean existsByStudentAndStudySession(Member student, StudySession studySession);

    long countByStudySessionId(Long studySessionId);
}

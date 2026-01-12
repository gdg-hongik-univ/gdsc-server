package com.gdschongik.gdsc.domain.study.dao;

import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.study.domain.Study;
import com.gdschongik.gdsc.domain.study.domain.StudyHistory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyHistoryRepository extends JpaRepository<StudyHistory, Long>, StudyHistoryCustomRepository {
    Optional<StudyHistory> findByStudentAndStudy(Member student, Study study);

    boolean existsByStudentAndStudy(Member currentMember, Study study);

    List<StudyHistory> findAllByStudent(Member student);

    List<StudyHistory> findAllByStudy(Study study);

    Page<StudyHistory> findByStudyId(Long studyId, Pageable pageable);
}

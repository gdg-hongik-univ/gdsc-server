package com.gdschongik.gdsc.domain.study.dao;

import com.gdschongik.gdsc.domain.study.domain.Study;
import com.gdschongik.gdsc.domain.study.domain.StudyHistory;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudyHistoryCustomRepository {

    long countByStudyIdAndStudentIds(Long studyId, List<Long> studentIds);

    List<StudyHistory> findAllByStudyIdAndStudentIds(Long studyId, List<Long> studentIds);

    List<StudyHistory> findAllByStudy(Study study);

    Page<StudyHistory> findByStudyId(Long studyId, Pageable pageable);
}

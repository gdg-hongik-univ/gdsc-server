package com.gdschongik.gdsc.domain.study.dao;

import com.gdschongik.gdsc.domain.study.domain.StudyAnnouncement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyAnnouncementRepository
        extends JpaRepository<StudyAnnouncement, Long>, StudyAnnouncementCustomRepository {

    List<StudyAnnouncement> findAllByStudyIdOrderByCreatedAtDesc(Long studyId);
}

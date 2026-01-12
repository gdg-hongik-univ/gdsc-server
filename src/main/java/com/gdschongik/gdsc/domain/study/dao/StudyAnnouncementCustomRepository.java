package com.gdschongik.gdsc.domain.study.dao;

import com.gdschongik.gdsc.domain.study.domain.StudyAnnouncement;
import java.util.List;

public interface StudyAnnouncementCustomRepository {

    List<StudyAnnouncement> findAllByStudyIdsOrderByCreatedAtDesc(List<Long> studyIds);
}

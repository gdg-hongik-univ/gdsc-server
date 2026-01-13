package com.gdschongik.gdsc.domain.study.dto.response;

import com.gdschongik.gdsc.domain.study.domain.StudyAnnouncement;
import com.gdschongik.gdsc.domain.study.dto.dto.StudyAnnouncementDto;
import com.gdschongik.gdsc.domain.study.dto.dto.StudyCommonDto;

public record StudyAnnouncementResponse(StudyCommonDto study, StudyAnnouncementDto studyAnnouncement) {
    public static StudyAnnouncementResponse from(StudyAnnouncement studyAnnouncement) {
        return new StudyAnnouncementResponse(
                StudyCommonDto.from(studyAnnouncement.getStudy()), StudyAnnouncementDto.from(studyAnnouncement));
    }
}

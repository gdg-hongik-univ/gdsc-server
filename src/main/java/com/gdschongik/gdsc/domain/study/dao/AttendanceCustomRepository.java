package com.gdschongik.gdsc.domain.study.dao;

import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.study.domain.Attendance;
import com.gdschongik.gdsc.domain.study.domain.Study;
import java.util.List;

public interface AttendanceCustomRepository {
    List<Attendance> findFetchByMemberAndStudy(Member member, Study study);

    List<Attendance> findByStudyIdAndMemberIds(Long studyId, List<Long> memberIds);

    void deleteByStudyIdAndMemberId(Long studyId, Long memberId);
}

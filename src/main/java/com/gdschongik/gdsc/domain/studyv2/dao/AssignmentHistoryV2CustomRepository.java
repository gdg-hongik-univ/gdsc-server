package com.gdschongik.gdsc.domain.studyv2.dao;

import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.studyv2.domain.AssignmentHistoryV2;
import com.gdschongik.gdsc.domain.studyv2.domain.StudyV2;
import java.util.List;

public interface AssignmentHistoryV2CustomRepository {
    List<AssignmentHistoryV2> findByMemberAndStudy(Member member, StudyV2 study);

    List<AssignmentHistoryV2> findByStudyIdAndMemberIds(Long studyId, List<Long> memberIds);

    void deleteByStudyIdAndMemberId(Long studyId, Long memberId);
}

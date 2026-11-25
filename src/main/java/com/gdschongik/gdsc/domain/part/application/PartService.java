package com.gdschongik.gdsc.domain.part.application;

import com.gdschongik.gdsc.domain.common.model.SemesterType;
import com.gdschongik.gdsc.domain.common.vo.Semester;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.member.domain.service.MemberValidator;
import com.gdschongik.gdsc.domain.part.dao.PartRepository;
import com.gdschongik.gdsc.domain.part.domain.Part;
import com.gdschongik.gdsc.domain.part.domain.PartSession;
import com.gdschongik.gdsc.domain.part.dto.request.PartCreateRequest;
import com.gdschongik.gdsc.domain.part.dto.response.PartResponse;
import com.gdschongik.gdsc.domain.part.dto.response.PartSemesterResponse;
import com.gdschongik.gdsc.global.util.MemberUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartService {

    private final PartRepository partRepository;
    private final MemberUtil memberUtil;
    private final MemberValidator memberValidator;

    @Transactional
    public void createPart(PartCreateRequest request) {
        Member currentMember = memberUtil.getCurrentMember();
        memberValidator.validateAdminPermission(currentMember.getManageRole());

        Part part = Part.create(
                request.semester(),
                request.partType(),
                request.thumbnailUrl(),
                request.leadName(),
                request.introductions(),
                request.recommendations());

        List<PartCreateRequest.Item> sessions = request.partSessions();
        for (int index = 0; index < sessions.size(); index++) {
            PartCreateRequest.Item session = sessions.get(index);
            PartSession.create(index + 1, session.title(), session.description(), session.isHighlighted(), part);
        }

        partRepository.save(part);

        log.info("[PartService] 파트 생성: partId={}", part.getId());
    }

    /**
     * 파트가 하나 이상 개설된 학기를 중복 없이 최신순으로 반환합니다.
     * 같은 학년도에서는 2학기가 1학기보다 먼저 나옵니다.
     */
    public List<PartSemesterResponse> getAllPartSemesters() {
        return partRepository.findAllPartSemesters();
    }

    /**
     * 학년도와 학기가 모두 일치하는 파트를 소개, 추천 대상, 세션과 함께 반환합니다.
     * 일치하는 파트가 없으면 빈 목록을 반환합니다.
     */
    public List<PartResponse> getAllPartsBySemester(Integer academicYear, SemesterType semesterType) {
        Semester semester = Semester.of(academicYear, semesterType);
        return partRepository.findAllBySemester(semester).stream()
                .map(PartResponse::from)
                .toList();
    }
}

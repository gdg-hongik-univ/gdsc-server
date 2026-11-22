package com.gdschongik.gdsc.domain.part.application;

import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.member.domain.service.MemberValidator;
import com.gdschongik.gdsc.domain.part.dao.PartRepository;
import com.gdschongik.gdsc.domain.part.domain.Part;
import com.gdschongik.gdsc.domain.part.dto.request.PartCreateRequest;
import com.gdschongik.gdsc.global.util.MemberUtil;
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
        partRepository.save(part);

        log.info("[PartService] 파트 생성: partId={}", part.getId());
    }
}

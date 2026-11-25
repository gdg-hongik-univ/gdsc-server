package com.gdschongik.gdsc.domain.part.application;

import static com.gdschongik.gdsc.global.common.constant.TestPartConstant.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.part.dao.PartRepository;
import com.gdschongik.gdsc.domain.part.domain.Part;
import com.gdschongik.gdsc.domain.part.dto.request.PartCreateRequest;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.helper.IntegrationTest;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

class PartServiceTest extends IntegrationTest {

    @Autowired
    private PartService partService;

    @Autowired
    private PartRepository partRepository;

    @Nested
    class 파트_생성할때 {

        @Test
        @Transactional
        void 성공한다() {
            // given
            Member member = createAdminMember();
            logoutAndReloginAs(member.getId(), member.getRole());

            var request = new PartCreateRequest(
                    PART_SEMESTER,
                    PART_TYPE,
                    PART_THUMBNAIL_URL,
                    PART_LEAD_NAME,
                    PART_INTRODUCTIONS,
                    PART_RECOMMENDATIONS,
                    List.of(new PartCreateRequest.Item(
                            PART_SESSION_TITLE, PART_SESSION_DESCRIPTION, PART_SESSION_IS_HIGHLIGHTED)));

            // when
            partService.createPart(request);

            // then
            Optional<Part> optionalPart = partRepository.findById(1L);
            assertThat(optionalPart).isPresent();

            Part part = optionalPart.get();
            assertThat(part.getPartSessions()).hasSizeGreaterThan(0);
        }

        @Test
        void 어드민이_아니라면_실패한다() {
            // given
            Member member = createAssociateMember();
            logoutAndReloginAs(member.getId(), member.getRole());

            var request = new PartCreateRequest(
                    PART_SEMESTER,
                    PART_TYPE,
                    PART_THUMBNAIL_URL,
                    PART_LEAD_NAME,
                    PART_INTRODUCTIONS,
                    PART_RECOMMENDATIONS,
                    List.of(new PartCreateRequest.Item(
                            PART_SESSION_TITLE, PART_SESSION_DESCRIPTION, PART_SESSION_IS_HIGHLIGHTED)));

            // when & then
            assertThatThrownBy(() -> partService.createPart(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(INVALID_ROLE.getMessage());
        }
    }
}

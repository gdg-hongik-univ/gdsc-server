package com.gdschongik.gdsc.domain.part.application;

import static com.gdschongik.gdsc.global.common.constant.TestPartConstant.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.part.dao.PartRepository;
import com.gdschongik.gdsc.domain.part.dto.request.PartCreateRequest;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.helper.IntegrationTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PartServiceTest extends IntegrationTest {

    @Autowired
    private PartService partService;

    @Autowired
    private PartRepository partRepository;

    @Nested
    class 파트_생성할때 {

        @Test
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
                    PART_RECOMMENDATIONS);

            // when
            partService.createPart(request);

            // then
            assertThat(partRepository.findAll()).hasSize(1);
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
                    PART_RECOMMENDATIONS);

            // when & then
            assertThatThrownBy(() -> partService.createPart(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(INVALID_ROLE.getMessage());
        }
    }
}

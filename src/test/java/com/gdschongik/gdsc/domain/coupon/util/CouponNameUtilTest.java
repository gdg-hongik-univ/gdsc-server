package com.gdschongik.gdsc.domain.coupon.util;

import static com.gdschongik.gdsc.global.common.constant.StudyConstant.*;
import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.domain.study.domain.Study;
import com.gdschongik.gdsc.helper.FixtureHelper;
import org.junit.jupiter.api.Test;

class CouponNameUtilTest {

    CouponNameUtil couponNameUtil = new CouponNameUtil();
    FixtureHelper fixtureHelper = new FixtureHelper();

    @Test
    void 스터디_수료_쿠폰_이름이_생성된다() {
        // given
        Study study = fixtureHelper.createStudy(ONLINE_STUDY, 1L, 1L, 1L);

        // when
        String couponName = couponNameUtil.generateStudyCompletionCouponName(study);

        // then
        assertThat(couponName).isEqualTo("2025-1 스터디 제목 수료 쿠폰");
    }
}

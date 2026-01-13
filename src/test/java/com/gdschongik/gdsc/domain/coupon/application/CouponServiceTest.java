package com.gdschongik.gdsc.domain.coupon.application;

import static com.gdschongik.gdsc.domain.coupon.domain.CouponType.*;
import static com.gdschongik.gdsc.global.common.constant.CouponConstant.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;
import static java.math.BigDecimal.*;
import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.domain.common.vo.Money;
import com.gdschongik.gdsc.domain.coupon.dao.CouponRepository;
import com.gdschongik.gdsc.domain.coupon.dao.IssuedCouponRepository;
import com.gdschongik.gdsc.domain.coupon.domain.Coupon;
import com.gdschongik.gdsc.domain.coupon.domain.CouponType;
import com.gdschongik.gdsc.domain.coupon.domain.IssuedCoupon;
import com.gdschongik.gdsc.domain.coupon.dto.request.CouponCreateRequest;
import com.gdschongik.gdsc.domain.coupon.dto.request.CouponIssueRequest;
import com.gdschongik.gdsc.domain.coupon.dto.request.IssuedCouponQueryOption;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.study.domain.Study;
import com.gdschongik.gdsc.domain.study.domain.StudyHistory;
import com.gdschongik.gdsc.domain.study.domain.StudyType;
import com.gdschongik.gdsc.global.exception.CustomException;
import com.gdschongik.gdsc.global.util.DiscordUtil;
import com.gdschongik.gdsc.helper.IntegrationTest;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

class CouponServiceTest extends IntegrationTest {

    @Autowired
    CouponService couponService;

    @Autowired
    CouponRepository couponRepository;

    @Autowired
    IssuedCouponRepository issuedCouponRepository;

    @MockBean
    DiscordUtil discordUtil;

    @Nested
    class 쿠폰_생성할때 {

        @Test
        void 성공한다() {
            // given
            CouponCreateRequest request = new CouponCreateRequest(COUPON_NAME, ONE, ADMIN, null);

            // when
            couponService.createCoupon(request);

            // then
            assertThat(couponRepository.findById(1L)).isPresent();
        }

        @Test
        void 할인금액이_양수가_아니라면_실패한다() {
            // given
            CouponCreateRequest request = new CouponCreateRequest(COUPON_NAME, ZERO, ADMIN, null);

            // when & then
            assertThatThrownBy(() -> couponService.createCoupon(request))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(COUPON_DISCOUNT_AMOUNT_NOT_POSITIVE.getMessage());
        }
    }

    @Nested
    class 쿠폰_발급할때 {

        @Test
        void 성공한다() {
            // given
            CouponCreateRequest request = new CouponCreateRequest(COUPON_NAME, ONE, ADMIN, null);
            couponService.createCoupon(request);

            createMember();
            createMember();

            CouponIssueRequest issueRequest = new CouponIssueRequest(1L, List.of(1L, 2L));

            // when
            couponService.createIssuedCoupon(issueRequest);

            // then
            assertThat(issuedCouponRepository.findAll()).hasSize(2);
        }

        @Test
        void 존재하지_않는_유저이면_제외하고_성공한다() {
            // given
            CouponCreateRequest request = new CouponCreateRequest(COUPON_NAME, ONE, ADMIN, null);
            couponService.createCoupon(request);

            createMember();
            createMember();

            CouponIssueRequest issueRequest = new CouponIssueRequest(1L, List.of(1L, 2L, 3L));

            // when
            couponService.createIssuedCoupon(issueRequest);

            // then
            assertThat(issuedCouponRepository.findAll()).hasSize(2);
        }

        @Test
        void 존재하지_않는_쿠폰이면_실패한다() {
            // given
            CouponCreateRequest request = new CouponCreateRequest(COUPON_NAME, ONE, ADMIN, null);
            couponService.createCoupon(request);

            createMember();
            createMember();

            CouponIssueRequest issueRequest = new CouponIssueRequest(2L, List.of(1L, 2L));

            // when & then
            assertThatThrownBy(() -> couponService.createIssuedCoupon(issueRequest))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(COUPON_NOT_FOUND.getMessage());
        }
    }

    @Nested
    class 쿠폰_회수할때 {

        @Test
        void 성공한다() {
            // given
            CouponCreateRequest request = new CouponCreateRequest(COUPON_NAME, ONE, ADMIN, null);
            couponService.createCoupon(request);

            createMember();
            CouponIssueRequest issueRequest = new CouponIssueRequest(1L, List.of(1L));
            couponService.createIssuedCoupon(issueRequest);

            // when
            couponService.revokeIssuedCoupon(1L);

            // then
            assertThat(issuedCouponRepository.findAll()).hasSize(1).first().satisfies(issuedCoupon -> assertThat(
                            issuedCoupon.getHasRevoked())
                    .isTrue());
        }

        @Test
        void 존재하지_않는_발급쿠폰이면_실패한다() {
            // given
            CouponCreateRequest request = new CouponCreateRequest(COUPON_NAME, ONE, ADMIN, null);
            couponService.createCoupon(request);

            createMember();
            CouponIssueRequest issueRequest = new CouponIssueRequest(1L, List.of(1L));
            couponService.createIssuedCoupon(issueRequest);

            // when & then
            assertThatThrownBy(() -> couponService.revokeIssuedCoupon(2L))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ISSUED_COUPON_NOT_FOUND.getMessage());
        }

        @Test
        void 이미_회수한_발급쿠폰이면_실패한다() {
            // given
            CouponCreateRequest request = new CouponCreateRequest(COUPON_NAME, ONE, ADMIN, null);
            couponService.createCoupon(request);

            createMember();
            CouponIssueRequest issueRequest = new CouponIssueRequest(1L, List.of(1L));
            couponService.createIssuedCoupon(issueRequest);

            issuedCouponRepository.findById(1L).ifPresent(coupon -> {
                coupon.revoke();
                issuedCouponRepository.save(coupon);
            });

            // when & then
            assertThatThrownBy(() -> couponService.revokeIssuedCoupon(1L))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(COUPON_NOT_REVOKABLE_ALREADY_REVOKED.getMessage());
        }

        @Test
        void 이미_사용한_발급쿠폰이면_실패한다() {
            // given
            CouponCreateRequest request = new CouponCreateRequest(COUPON_NAME, ONE, ADMIN, null);
            couponService.createCoupon(request);

            createMember();
            CouponIssueRequest issueRequest = new CouponIssueRequest(1L, List.of(1L));
            couponService.createIssuedCoupon(issueRequest);

            issuedCouponRepository.findById(1L).ifPresent(coupon -> {
                coupon.use(LocalDateTime.now());
                issuedCouponRepository.save(coupon);
            });

            // when & then
            assertThatThrownBy(() -> couponService.revokeIssuedCoupon(1L))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(COUPON_NOT_REVOKABLE_ALREADY_USED.getMessage());
        }
    }

    @Nested
    class 스터디_수료_쿠폰_발급시 {

        @Test
        void 발급쿠폰을_생성한다() {
            // given
            Member student = createRegularMember();
            Member mentor = createMentor();
            LocalDateTime now = LocalDateTime.now();
            Study study = createStudy(StudyType.OFFLINE, mentor);

            StudyHistory studyHistory = createStudyHistory(student, study);

            // when
            couponService.createAndIssueCouponByStudyHistories(List.of(1L));

            // then
            Page<IssuedCoupon> issuedCoupons = issuedCouponRepository.findAllIssuedCoupons(
                    new IssuedCouponQueryOption(null, null, null, null, null, null), PageRequest.of(0, 10));
            assertThat(issuedCoupons).hasSize(1);
        }

        @Test
        void 수료_쿠폰이_이미_존재한다면_새로_생성하지_않는다() {
            // given
            Member student = createRegularMember();
            Member mentor = createMentor();
            LocalDateTime now = LocalDateTime.now();
            Study study = createStudy(StudyType.OFFLINE, mentor);

            StudyHistory studyHistory = createStudyHistory(student, study);

            // when
            Coupon coupon = couponRepository.save(
                    Coupon.createAutomatic(COUPON_NAME, Money.FIVE_THOUSAND, CouponType.STUDY_COMPLETION, study));

            couponService.createAndIssueCouponByStudyHistories(List.of(student.getId()));

            // then
            List<Coupon> coupons = couponRepository.findAll();
            assertThat(coupons).hasSize(1);
        }

        @Test
        void 수료_쿠폰이_없다면_새로_생성한다() {
            // given
            Member student = createRegularMember();
            Member mentor = createMentor();
            LocalDateTime now = LocalDateTime.now();
            Study study = createStudy(StudyType.OFFLINE, mentor);

            StudyHistory studyHistory = createStudyHistory(student, study);

            // when
            couponService.createAndIssueCouponByStudyHistories(List.of(1L));

            // then
            List<Coupon> coupons = couponRepository.findAll();
            assertThat(coupons).hasSize(1);
        }
    }

    @Nested
    class 스터디_수료_쿠폰_회수시 {

        @Test
        void 발급쿠폰을_회수한다() {
            // given
            Member student = createRegularMember();
            Member mentor = createMentor();
            LocalDateTime now = LocalDateTime.now();
            Study study = createStudy(StudyType.OFFLINE, mentor);

            StudyHistory studyHistory = createStudyHistory(student, study);

            couponService.createAndIssueCouponByStudyHistories(List.of(1L));

            // when
            couponService.revokeStudyCompletionCouponByStudyHistoryId(student.getId());

            // then
            IssuedCoupon issuedCoupon = issuedCouponRepository.findById(1L).orElseThrow();
            assertThat(issuedCoupon.getHasRevoked()).isTrue();
        }
    }
}

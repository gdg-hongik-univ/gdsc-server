package com.gdschongik.gdsc.domain.coupon.domain;

import static com.gdschongik.gdsc.domain.coupon.domain.CouponType.*;
import static com.gdschongik.gdsc.global.common.constant.TestCouponConstant.*;
import static com.gdschongik.gdsc.global.common.constant.TestMemberConstant.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;
import static java.math.BigDecimal.*;
import static org.assertj.core.api.Assertions.*;

import com.gdschongik.gdsc.domain.common.vo.Money;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.global.exception.CustomException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class IssuedCouponTest {

    @Nested
    class 발급쿠폰_사용할때 {

        @Test
        void 성공하면_사용여부는_true이다() {
            // given
            Coupon coupon = Coupon.createAutomatic(COUPON_NAME, Money.from(ONE), ADMIN, null);
            Member member = Member.createGuest(OAUTH_ID);
            IssuedCoupon issuedCoupon = IssuedCoupon.create(coupon, member);
            LocalDateTime now = LocalDateTime.now();

            // when
            issuedCoupon.use(now);

            // then
            assertThat(issuedCoupon.hasUsed()).isTrue();
        }

        @Test
        void 이미_사용한_쿠폰이면_실패한다() {
            // given
            Coupon coupon = Coupon.createAutomatic(COUPON_NAME, Money.from(ONE), ADMIN, null);
            Member member = Member.createGuest(OAUTH_ID);
            IssuedCoupon issuedCoupon = IssuedCoupon.create(coupon, member);
            LocalDateTime now = LocalDateTime.now();
            issuedCoupon.use(now);

            // when & then
            assertThatThrownBy(() -> issuedCoupon.use(now))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(COUPON_NOT_USABLE_ALREADY_USED.getMessage());
        }

        @Test
        void 이미_회수한_쿠폰이면_실패한다() {
            // given
            Coupon coupon = Coupon.createAutomatic(COUPON_NAME, Money.from(ONE), ADMIN, null);
            Member member = Member.createGuest(OAUTH_ID);
            IssuedCoupon issuedCoupon = IssuedCoupon.create(coupon, member);
            issuedCoupon.revoke();
            LocalDateTime now = LocalDateTime.now();

            // when & then
            assertThatThrownBy(() -> issuedCoupon.use(now))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(COUPON_NOT_USABLE_REVOKED.getMessage());
        }
    }

    @Nested
    class 발급쿠폰_회수할때 {

        @Test
        void 성공하면_회수여부는_true이다() {
            // given
            Coupon coupon = Coupon.createAutomatic(COUPON_NAME, Money.from(ONE), ADMIN, null);
            Member member = Member.createGuest(OAUTH_ID);
            IssuedCoupon issuedCoupon = IssuedCoupon.create(coupon, member);

            // when
            issuedCoupon.revoke();

            // then
            assertThat(issuedCoupon.getHasRevoked()).isTrue();
        }

        @Test
        void 이미_회수한_발급쿠폰이면_실패한다() {
            // given
            Coupon coupon = Coupon.createAutomatic(COUPON_NAME, Money.from(ONE), ADMIN, null);
            Member member = Member.createGuest(OAUTH_ID);
            IssuedCoupon issuedCoupon = IssuedCoupon.create(coupon, member);
            issuedCoupon.revoke();

            // when & then
            assertThatThrownBy(issuedCoupon::revoke)
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(COUPON_NOT_REVOKABLE_ALREADY_REVOKED.getMessage());
        }

        @Test
        void 이미_사용한_발급쿠폰이면_실패한다() {
            // given
            Coupon coupon = Coupon.createAutomatic(COUPON_NAME, Money.from(ONE), ADMIN, null);
            Member member = Member.createGuest(OAUTH_ID);
            IssuedCoupon issuedCoupon = IssuedCoupon.create(coupon, member);
            issuedCoupon.use(LocalDateTime.now());

            // when & then
            assertThatThrownBy(issuedCoupon::revoke)
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(COUPON_NOT_REVOKABLE_ALREADY_USED.getMessage());
        }
    }
}

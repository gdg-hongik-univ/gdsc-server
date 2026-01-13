package com.gdschongik.gdsc.domain.coupon.dao;

import com.gdschongik.gdsc.domain.coupon.domain.CouponType;
import com.gdschongik.gdsc.domain.coupon.domain.IssuedCoupon;
import com.gdschongik.gdsc.domain.coupon.dto.request.IssuedCouponQueryOption;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.study.domain.Study;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IssuedCouponCustomRepository {

    Page<IssuedCoupon> findAllIssuedCoupons(IssuedCouponQueryOption queryOption, Pageable pageable);

    Optional<IssuedCoupon> findUnrevokedIssuedStudyCoupon(CouponType couponType, Member member, Study study);
}

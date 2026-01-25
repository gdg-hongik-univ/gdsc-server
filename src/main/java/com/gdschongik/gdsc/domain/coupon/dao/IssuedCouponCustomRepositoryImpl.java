package com.gdschongik.gdsc.domain.coupon.dao;

import static com.gdschongik.gdsc.domain.coupon.domain.QCoupon.*;
import static com.gdschongik.gdsc.domain.coupon.domain.QIssuedCoupon.issuedCoupon;

import com.gdschongik.gdsc.domain.coupon.domain.CouponType;
import com.gdschongik.gdsc.domain.coupon.domain.IssuedCoupon;
import com.gdschongik.gdsc.domain.coupon.dto.request.IssuedCouponQueryOption;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.study.domain.Study;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class IssuedCouponCustomRepositoryImpl implements IssuedCouponCustomRepository, IssuedCouponQueryMethod {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<IssuedCoupon> findAllIssuedCoupons(IssuedCouponQueryOption queryOption, Pageable pageable) {
        List<IssuedCoupon> fetch = queryFactory
                .selectFrom(issuedCoupon)
                .where(matchesQueryOption(queryOption))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(issuedCoupon.createdAt.desc())
                .fetch();

        JPAQuery<Long> countQuery =
                queryFactory.select(issuedCoupon.count()).from(issuedCoupon).where(matchesQueryOption(queryOption));

        return PageableExecutionUtils.getPage(fetch, pageable, countQuery::fetchOne);
    }

    @Override
    public Optional<IssuedCoupon> findUnrevokedIssuedStudyCoupon(CouponType couponType, Member member, Study study) {
        return Optional.ofNullable(queryFactory
                .selectFrom(issuedCoupon)
                .leftJoin(issuedCoupon.coupon, coupon)
                .where(eqMember(member)
                        .and(coupon.study.eq(study))
                        .and(hasRevoked(false))
                        .and(coupon.couponType.eq(couponType)))
                .fetchFirst());
    }
}

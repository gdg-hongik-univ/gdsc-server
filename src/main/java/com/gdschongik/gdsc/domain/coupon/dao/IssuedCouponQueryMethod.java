package com.gdschongik.gdsc.domain.coupon.dao;

import static com.gdschongik.gdsc.domain.coupon.domain.QIssuedCoupon.issuedCoupon;

import com.gdschongik.gdsc.domain.coupon.dto.request.IssuedCouponQueryOption;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;

public interface IssuedCouponQueryMethod {

    default BooleanExpression eqStudentId(String studentId) {
        return studentId != null ? issuedCoupon.member.studentId.containsIgnoreCase(studentId) : null;
    }

    default BooleanExpression eqMemberName(String memberName) {
        return memberName != null ? issuedCoupon.member.name.containsIgnoreCase(memberName) : null;
    }

    default BooleanExpression eqMember(Member member) {
        return member != null ? issuedCoupon.member.eq(member) : null;
    }

    default BooleanExpression eqPhone(String phone) {
        return phone != null ? issuedCoupon.member.phone.contains(phone.replaceAll("-", "")) : null;
    }

    default BooleanExpression eqCouponName(String couponName) {
        return couponName != null ? issuedCoupon.coupon.name.containsIgnoreCase(couponName) : null;
    }

    default BooleanExpression hasUsed(Boolean hasUsed) {
        if (hasUsed == null) {
            return null;
        }
        return hasUsed ? issuedCoupon.usedAt.isNotNull() : issuedCoupon.usedAt.isNull();
    }

    default BooleanExpression hasRevoked(Boolean hasRevoked) {
        if (hasRevoked == null) {
            return null;
        }
        return hasRevoked ? issuedCoupon.hasRevoked.isTrue() : issuedCoupon.hasRevoked.isFalse();
    }

    default BooleanBuilder matchesQueryOption(IssuedCouponQueryOption queryOption) {
        return new BooleanBuilder()
                .and(eqStudentId(queryOption.studentId()))
                .and(eqMemberName(queryOption.memberName()))
                .and(eqPhone(queryOption.phone()))
                .and(eqCouponName(queryOption.couponName()))
                .and(hasUsed(queryOption.hasUsed()))
                .and(hasRevoked(queryOption.hasRevoked()));
    }
}

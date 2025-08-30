package com.gdschongik.gdsc.domain.event.dto.dto;

import com.querydsl.core.annotations.QueryProjection;

public record AfterPartyApplicantCountDto(
        long afterPartyAppliedCount,
        long prePaymentPaidCount,
        long afterPartyAttendedCount,
        long postPaymentPaidCount) {

    @QueryProjection
    public AfterPartyApplicantCountDto(
            long afterPartyAppliedCount,
            long prePaymentPaidCount,
            long afterPartyAttendedCount,
            long postPaymentPaidCount) {
        this.afterPartyAppliedCount = afterPartyAppliedCount;
        this.prePaymentPaidCount = prePaymentPaidCount;
        this.afterPartyAttendedCount = afterPartyAttendedCount;
        this.postPaymentPaidCount = postPaymentPaidCount;
    }
}

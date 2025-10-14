package com.gdschongik.gdsc.domain.member.domain;

import static com.gdschongik.gdsc.domain.common.model.RequirementStatus.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.common.model.RequirementStatus;
import com.gdschongik.gdsc.global.exception.CustomException;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssociateRequirement {

    @Enumerated(EnumType.STRING)
    private RequirementStatus univStatus;

    @Enumerated(EnumType.STRING)
    private RequirementStatus discordStatus;

    @Enumerated(EnumType.STRING)
    private RequirementStatus infoStatus;

    @Builder(access = AccessLevel.PRIVATE)
    private AssociateRequirement(
            RequirementStatus univStatus, RequirementStatus discordStatus, RequirementStatus infoStatus) {
        this.univStatus = univStatus;
        this.discordStatus = discordStatus;
        this.infoStatus = infoStatus;
    }

    public static AssociateRequirement unsatisfied() {
        return AssociateRequirement.builder()
                .univStatus(UNSATISFIED)
                .discordStatus(UNSATISFIED)
                .infoStatus(UNSATISFIED)
                .build();
    }

    // 상태 변경 로직

    public void verifyUniv() {
        univStatus = SATISFIED;
    }

    public void verifyDiscord() {
        discordStatus = SATISFIED;
    }

    public void verifyInfo() {
        infoStatus = SATISFIED;
    }

    // 데이터 전달 로직

    public boolean isUnivSatisfied() {
        return univStatus == SATISFIED;
    }

    private boolean isDiscordSatisfied() {
        return discordStatus == SATISFIED;
    }

    public boolean isInfoSatisfied() {
        return infoStatus == SATISFIED;
    }

    // 검증 로직

    public void validateAllSatisfied() {
        if (!isUnivSatisfied()) {
            throw new CustomException(UNIV_NOT_SATISFIED);
        }

        if (!isDiscordSatisfied()) {
            throw new CustomException(DISCORD_NOT_SATISFIED);
        }

        if (!isInfoSatisfied()) {
            throw new CustomException(BASIC_INFO_NOT_SATISFIED);
        }
    }

    public void checkVerifiableUniv() {
        if (isUnivSatisfied()) {
            throw new CustomException(EMAIL_ALREADY_SATISFIED);
        }
    }

    /**
     * 모든 준회원 조건을 강등합니다.
     */
    public void demoteAssociateRequirement() {
        discordStatus = UNSATISFIED;
        infoStatus = UNSATISFIED;
        univStatus = UNSATISFIED;
    }
}

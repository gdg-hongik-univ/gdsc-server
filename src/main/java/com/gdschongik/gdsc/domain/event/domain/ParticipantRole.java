package com.gdschongik.gdsc.domain.event.domain;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.global.exception.CustomException;
import jakarta.annotation.Nullable;

public enum ParticipantRole {
    NON_MEMBER,
    GUEST,
    ASSOCIATE,
    REGULAR,
    ;

    /**
     * 이벤트 참여정보 및 (존재하는 경우) 멤버 역할을 기반으로 참여자 역할을 결정합니다.
     *
     * @param member 이벤트 참여정보의 멤버 ID 필드가 null이 아닌 경우, 해당 ID의 멤버입니다. 멤버 ID가 없는 경우 null입니다.
     */
    public static ParticipantRole of(EventParticipation participation, @Nullable Member member) {
        validateParticipationAndMember(participation, member);

        if (member == null) {
            return NON_MEMBER;
        }

        if (member.isRegular()) {
            return REGULAR;
        }

        if (member.isAssociate()) {
            return ASSOCIATE;
        }

        return GUEST;
    }

    private static void validateParticipationAndMember(EventParticipation participation, @Nullable Member member) {
        boolean memberExists = member != null;
        boolean participationMemberExists = participation.getMemberId() != null;

        if (memberExists != participationMemberExists) {
            throw new CustomException(PARTICIPANT_ROLE_NOT_CREATABLE_BOTH_EXISTENCE_MISMATCH);
        }

        if (memberExists && !participation.getMemberId().equals(member.getId())) {
            throw new CustomException(PARTICIPANT_ROLE_NOT_CREATABLE_BOTH_ID_MISMATCH);
        }
    }
}

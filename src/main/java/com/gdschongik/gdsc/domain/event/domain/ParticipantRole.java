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
     * 참여자 역할을 반환합니다.
     *
     * @param member 이벤트 참여정보의 멤버 ID 필드가 null이 아닌 경우, 해당 ID의 멤버입니다. 멤버 ID가 없는 경우 null입니다.
     * @throws CustomException 이벤트 참여정보
     */
    public static ParticipantRole of(EventParticipation participation, @Nullable Member member) {
        validateParticipationBelongsToMember(participation, member);

        if (isBothMemberNotNull(participation, member) && member.isRegular()) {
            return REGULAR;
        }

        if (isBothMemberNotNull(participation, member) && member.isAssociate()) {
            return ASSOCIATE;
        }

        if (isBothMemberNotNull(participation, member) && member.isGuest()) {
            return GUEST;
        }

        if (member == null && participation.getMemberId() == null) {
            return NON_MEMBER;
        }

        throw new CustomException(PARTICIPANT_ROLE_NOT_CREATABLE_INVALID_PARAM);
    }

    /**
     * 이벤트 참여정보의 멤버 정보와 인자의 멤버 정보가 모두 존재해야 유효한 역할로 판정할 수 있습니다.
     * 해당 조건에 부합하지 않는 경우, 마지막에 INVALID_PARAM 예외를 발생시킵니다.
     */
    private static boolean isBothMemberNotNull(EventParticipation participation, @Nullable Member member) {
        return member != null && participation.getMemberId() != null;
    }

    /**
     * 이벤트 참여정보의 멤버 정보 및 인자로 넘어온 멤버 정보가 둘 다 존재한다면
     * 두 정보가 동일한 멤버의 정보인지 검증합니다.
     */
    private static void validateParticipationBelongsToMember(
            EventParticipation participation, @Nullable Member member) {
        if (member == null || participation.getMemberId() == null) {
            return;
        }

        if (!participation.getMemberId().equals(member.getId())) {
            throw new CustomException(PARTICIPANT_ROLE_NOT_CREATABLE_INVALID_OWNERSHIP);
        }
    }
}

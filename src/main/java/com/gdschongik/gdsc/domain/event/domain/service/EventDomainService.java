package com.gdschongik.gdsc.domain.event.domain.service;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.common.vo.Period;
import com.gdschongik.gdsc.domain.event.domain.Event;
import com.gdschongik.gdsc.global.annotation.DomainService;
import com.gdschongik.gdsc.global.exception.CustomException;
import jakarta.annotation.Nullable;
import java.time.LocalDateTime;

@DomainService
public class EventDomainService {

    /**
     * 이벤트를 변경합니다.
     * @param mainEventMaxApplicantCount 변경하려는 본 행사 최대 신청자 수. (인원 제한이 없는 경우 null)
     * @param afterPartyMaxApplicantCount 변경하려는 뒤풀이 최대 신청자 수. (인원 제한이 없는 경우 null)
     * @param currentMainEventApplicantCount 현재 본 행사 신청자 수. EventParticipationRepository 조회 데이터
     * @param currentAfterPartyApplicantCount 현재 뒤풀이 신청자 수. EventParticipationRepository 조회 데이터
     */
    public void update(
            Event event,
            String name,
            String venue,
            LocalDateTime startAt,
            String applicationDescription,
            Period applicationPeriod,
            @Nullable Integer mainEventMaxApplicantCount,
            @Nullable Integer afterPartyMaxApplicantCount,
            long currentMainEventApplicantCount,
            long currentAfterPartyApplicantCount) {
        validateUpdateMaxApplicantCount(currentMainEventApplicantCount, mainEventMaxApplicantCount);
        validateUpdateMaxApplicantCount(currentAfterPartyApplicantCount, afterPartyMaxApplicantCount);

        event.update(
                name,
                venue,
                startAt,
                applicationDescription,
                applicationPeriod,
                mainEventMaxApplicantCount,
                afterPartyMaxApplicantCount);
    }

    /**
     * 이벤트의 최대 신청자 수를 변경할 때, 현재 신청자 수보다 낮게 설정하는 경우 예외를 발생시킵니다.
     */
    private void validateUpdateMaxApplicantCount(long currentApplicantCount, @Nullable Integer newMaxApplicantCount) {
        if (newMaxApplicantCount == null) { // 인원 제한을 없애는 경우 항상 허용
            return;
        }

        if (currentApplicantCount > newMaxApplicantCount) {
            throw new CustomException(EVENT_NOT_UPDATABLE_MAX_APPLICANT_COUNT_INVALID);
        }
    }
}

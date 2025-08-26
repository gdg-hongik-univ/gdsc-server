package com.gdschongik.gdsc.global.exception;

import static org.springframework.http.HttpStatus.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    INTERNAL_ERROR(INTERNAL_SERVER_ERROR, "내부 서버 에러가 발생했습니다. 관리자에게 문의 바랍니다."),
    METHOD_ARGUMENT_NULL(BAD_REQUEST, "인자는 null이 될 수 없습니다."),
    METHOD_ARGUMENT_NOT_VALID(BAD_REQUEST, "인자가 유효하지 않습니다."),
    REGEX_VIOLATION(BAD_REQUEST, "정규표현식을 위반했습니다."),
    FORBIDDEN_ACCESS(FORBIDDEN, "접근 권한이 없습니다."),
    SORT_NOT_SUPPORTED(BAD_REQUEST, "지원되지 않는 정렬 기준입니다."),

    // Auth
    INVALID_JWT_TOKEN(UNAUTHORIZED, "유효하지 않은 JWT 토큰입니다."),
    EXPIRED_JWT_TOKEN(UNAUTHORIZED, "만료된 JWT 토큰입니다."),
    AUTH_NOT_EXIST(INTERNAL_SERVER_ERROR, "시큐리티 인증 정보가 존재하지 않습니다."),
    AUTH_NOT_PARSABLE(INTERNAL_SERVER_ERROR, "시큐리티 인증 정보 파싱에 실패했습니다."),
    INVALID_PASSWORD(UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
    INVALID_ROLE(FORBIDDEN, "권한이 없습니다."),

    // Parameter
    INVALID_QUERY_PARAMETER(BAD_REQUEST, "잘못된 쿼리 파라미터입니다."),

    // Money
    MONEY_AMOUNT_NOT_NULL(INTERNAL_SERVER_ERROR, "금액은 null이 될 수 없습니다."),

    // Period
    PERIOD_OVERLAP(CONFLICT, "기간이 중복됩니다."),
    PERIOD_DATE_NOT_NULL(BAD_REQUEST, "시작일, 종료일은 null이 될 수 없습니다."),

    // Member
    MEMBER_NOT_FOUND(NOT_FOUND, "존재하지 않는 커뮤니티 멤버입니다."),
    MEMBER_DELETED(CONFLICT, "탈퇴한 회원입니다."),
    MEMBER_FORBIDDEN(CONFLICT, "차단된 회원입니다."),
    MEMBER_ALREADY_ASSOCIATE(CONFLICT, "이미 준회원 역할에 해당하는 회원입니다."),
    MEMBER_ALREADY_REGULAR(CONFLICT, "이미 정회원 역할에 해당하는 회원입니다."),
    MEMBER_DISCORD_USERNAME_DUPLICATE(CONFLICT, "이미 등록된 디스코드 유저네임입니다."),
    MEMBER_NICKNAME_DUPLICATE(CONFLICT, "이미 사용중인 닉네임입니다."),
    MEMBER_NOT_APPLIED(CONFLICT, "가입신청서를 제출하지 않은 회원입니다."),
    MEMBER_NOT_ASSOCIATE(CONFLICT, "준회원이 아닌 회원입니다."),

    // Requirement
    UNIV_NOT_SATISFIED(CONFLICT, "재학생 인증이 완료되지 않았습니다."),
    DISCORD_NOT_SATISFIED(CONFLICT, "디스코드 인증이 완료되지 않았습니다."),
    EMAIL_ALREADY_SATISFIED(CONFLICT, "이미 이메일 인증된 회원입니다."),
    BASIC_INFO_NOT_SATISFIED(CONFLICT, "기본 회원정보 작성이 완료되지 않았습니다."),

    // Univ Email Verification
    UNIV_EMAIL_ALREADY_SATISFIED(CONFLICT, "이미 가입된 재학생 메일입니다."),
    UNIV_EMAIL_FORMAT_MISMATCH(BAD_REQUEST, "형식에 맞지 않는 재학생 메일입니다."),
    UNIV_EMAIL_DOMAIN_MISMATCH(BAD_REQUEST, "재학생 메일의 도메인이 맞지 않습니다."),
    MESSAGING_EXCEPTION(BAD_REQUEST, "수신자 이메일이 올바르지 않습니다."),
    VERIFICATION_CODE_NOT_FOUND(NOT_FOUND, "재학생 인증 코드가 존재하지 않습니다."),
    EMAIL_NOT_SENT(BAD_REQUEST, "재학생 인증 메일이 발송되지 않았습니다."),
    EXPIRED_EMAIL_VERIFICATION_TOKEN(BAD_REQUEST, "이메일 인증 토큰이 만료되었습니다."),
    INVALID_EMAIL_VERIFICATION_TOKEN(UNAUTHORIZED, "유효하지 않은 이메일 인증 토큰입니다."),

    // Discord
    DISCORD_INVALID_CODE_RANGE(INTERNAL_SERVER_ERROR, "디스코드 인증코드는 4자리 숫자여야 합니다."),
    DISCORD_CODE_NOT_FOUND(NOT_FOUND, "해당 유저네임으로 발급된 디스코드 인증코드가 존재하지 않습니다."),
    DISCORD_CODE_MISMATCH(CONFLICT, "디스코드 인증코드가 일치하지 않습니다."),
    DISCORD_ROLE_NOT_FOUND(NOT_FOUND, "디스코드 역할을 찾을 수 없습니다."),
    DISCORD_NOT_SIGNUP(INTERNAL_SERVER_ERROR, "아직 가입신청서를 작성하지 않은 회원입니다."),
    DISCORD_NICKNAME_NOTNULL(INTERNAL_SERVER_ERROR, "닉네임은 빈 값이 될 수 없습니다."),
    DISCORD_MEMBER_NOT_FOUND(NOT_FOUND, "디스코드 멤버를 찾을 수 없습니다."),
    DISCORD_CHANNEL_NOT_FOUND(NOT_FOUND, "디스코드 채널을 찾을 수 없습니다."),

    // Membership
    PAYMENT_NOT_SATISFIED(CONFLICT, "회비 납부가 완료되지 않았습니다."),
    MEMBERSHIP_NOT_APPLICABLE(CONFLICT, "멤버십 가입을 신청할 수 없는 회원입니다."),
    MEMBERSHIP_ALREADY_SUBMITTED(CONFLICT, "이미 이번 학기에 멤버십 가입을 신청한 회원입니다."),
    MEMBERSHIP_ALREADY_SATISFIED(CONFLICT, "이미 이번 학기에 정회원 승급을 완료한 회원입니다."),
    MEMBERSHIP_NOT_FOUND(NOT_FOUND, "해당 멤버십이 존재하지 않습니다."),
    MEMBERSHIP_RECRUITMENT_ROUND_NOT_OPEN(CONFLICT, "리크루팅 회차 모집기간이 아닙니다."),
    MEMBERSHIP_PAYMENT_NOT_REVOCABLE_NOT_SATISFIED(CONFLICT, "회비납부를 완료한 경우에만 멤버십 회비납부상태를 취소할 수 있습니다."),

    // Recruitment
    DATE_PRECEDENCE_INVALID(BAD_REQUEST, "종료일이 시작일과 같거나 앞설 수 없습니다."),
    RECRUITMENT_OVERLAP(BAD_REQUEST, "해당 학기에 이미 리크루팅이 존재합니다."),
    RECRUITMENT_NOT_FOUND(NOT_FOUND, "리크루팅이 존재하지 않습니다."),
    RECRUITMENT_PERIOD_NOT_WITHIN_TWO_WEEKS(BAD_REQUEST, "모집 시작일과 종료일이 학기 시작일로부터 2주 이내에 있지 않습니다."),

    // RecruitmentRound
    RECRUITMENT_ROUND_NOT_FOUND(NOT_FOUND, "모집회차가 존재하지 않습니다."),
    RECRUITMENT_ROUND_TYPE_OVERLAP(BAD_REQUEST, "모집 차수가 중복됩니다."),
    RECRUITMENT_ROUND_STARTDATE_ALREADY_PASSED(BAD_REQUEST, "이미 모집 시작일이 지난 모집회차입니다."),
    ROUND_ONE_DOES_NOT_EXIST(CONFLICT, "1차 모집이 존재하지 않습니다."),

    // Coupon
    COUPON_DISCOUNT_AMOUNT_NOT_POSITIVE(CONFLICT, "쿠폰의 할인 금액은 0보다 커야 합니다."),
    COUPON_NOT_USABLE_ALREADY_USED(CONFLICT, "이미 사용한 쿠폰은 사용할 수 없습니다."),
    COUPON_NOT_USABLE_REVOKED(CONFLICT, "회수된 쿠폰은 사용할 수 없습니다."),
    COUPON_NOT_REVOKABLE_ALREADY_REVOKED(CONFLICT, "이미 회수된 쿠폰은 다시 회수할 수 없습니다."),
    COUPON_NOT_REVOKABLE_ALREADY_USED(CONFLICT, "이미 사용한 쿠폰은 회수할 수 없습니다."),
    COUPON_NOT_FOUND(NOT_FOUND, "존재하지 않는 쿠폰입니다."),
    ISSUED_COUPON_NOT_FOUND(NOT_FOUND, "존재하지 않는 발급쿠폰입니다."),

    // Study
    STUDY_APPLICATION_START_DATE_INVALID(CONFLICT, "스터디 신청기간 시작일이 스터디 시작일보다 빠릅니다."),
    STUDY_MENTOR_IS_UNAUTHORIZED(CONFLICT, "게스트인 회원은 멘토로 지정할 수 없습니다."),
    STUDY_ACCESS_NOT_ALLOWED(FORBIDDEN, "관리자 또는 멘토 역할이 아닌 회원은 이 작업을 수행할 수 없습니다."),
    STUDY_MENTOR_INVALID(CONFLICT, "사용자가 해당 스터디의 멘토가 아닙니다."),
    ON_OFF_LINE_STUDY_TIME_IS_ESSENTIAL(CONFLICT, "온오프라인 스터디는 스터디 시간이 필요합니다."),
    STUDY_TIME_INVALID(CONFLICT, "스터디종료 시각이 스터디시작 시각보다 빠릅니다."),
    ASSIGNMENT_STUDY_CAN_NOT_INPUT_STUDY_TIME(CONFLICT, "과제 스터디는 스터디 시간을 입력할 수 없습니다."),
    STUDY_NOT_FOUND(NOT_FOUND, "존재하지 않는 스터디입니다."),
    STUDY_SESSION_NOT_FOUND(NOT_FOUND, "존재하지 않는 스터디 회차입니다."),
    STUDY_NOT_APPLICABLE(CONFLICT, "스터디 신청기간이 아닙니다."),
    STUDY_NOT_CANCELABLE_APPLICATION_PERIOD(CONFLICT, "스터디 신청기간이 아니라면 취소할 수 없습니다."),
    STUDY_NOT_CREATABLE_NOT_LIVE(INTERNAL_SERVER_ERROR, "온라인 및 오프라인 타입만 라이브 스터디로 생성할 수 있습니다."),
    STUDY_NOT_UPDATABLE_SESSION_NOT_FOUND(NOT_FOUND, "해당 스터디에 수정하려는 스터디 회차 ID가 존재하지 않습니다."),
    STUDY_NOT_UPDATABLE_LESSON_FIELD_NOT_NULL(CONFLICT, "과제 스터디는 수업 관련 필드를 null로 설정할 수 없습니다."),
    STUDY_NOT_UPDATABLE_LESSON_PERIOD_NOT_SEQUENTIAL(CONFLICT, "뒷 수업의 시작시간은 앞 수업의 시작시간보다 빠를 수 없습니다."),
    STUDY_NOT_DELETABLE_FK_CONSTRAINT(CONFLICT, "관련 데이터가 존재하여 스터디를 삭제할 수 없습니다."),

    // StudyDetail
    STUDY_DETAIL_NOT_FOUND(NOT_FOUND, "존재하지 않는 스터디 상세 정보입니다."),
    STUDY_DETAIL_UPDATE_RESTRICTED_TO_MENTOR(CONFLICT, "해당 스터디의 멘토만 수정할 수 있습니다."),
    STUDY_DETAIL_ASSIGNMENT_INVALID_DEADLINE(CONFLICT, "마감기한이 지난 과제의 마감기한을 수정할 수 없습니다"),
    STUDY_DETAIL_ASSIGNMENT_INVALID_UPDATE_DEADLINE(CONFLICT, "수정하려고 하는 과제의 마감기한은 기존의 마감기한보다 빠르면 안됩니다."),
    STUDY_DETAIL_ID_INVALID(CONFLICT, "수정하려는 스터디 상세정보가 서버에 존재하지 않거나 유효하지 않습니다."),
    STUDY_DETAIL_CURRICULUM_SIZE_MISMATCH(BAD_REQUEST, "스터디 커리큘럼의 총 개수가 일치하지 않습니다."),

    // StudyHistory
    STUDY_HISTORY_NOT_FOUND(NOT_FOUND, "존재하지 않는 스터디 수강 기록입니다."),
    STUDY_HISTORY_DUPLICATE(CONFLICT, "이미 해당 스터디를 신청했습니다."),
    STUDY_HISTORY_ONGOING_ALREADY_EXISTS(CONFLICT, "이미 진행중인 스터디가 있습니다."),
    STUDY_HISTORY_REPOSITORY_NOT_UPDATABLE_ASSIGNMENT_ALREADY_SUBMITTED(CONFLICT, "이미 제출한 과제가 있으므로 레포지토리를 수정할 수 없습니다."),
    STUDY_HISTORY_REPOSITORY_NOT_UPDATABLE_OWNER_MISMATCH(CONFLICT, "레포지토리 소유자가 현재 멤버와 다릅니다."),
    STUDY_HISTORY_NOT_APPLIED_STUDENT_EXISTS(CONFLICT, "해당 스터디에 신청하지 않은 멤버가 있습니다."),

    // StudyAnnouncement
    STUDY_ANNOUNCEMENT_NOT_FOUND(NOT_FOUND, "존재하지 않는 스터디 공지입니다."),

    // StudyAchievement
    STUDY_ACHIEVEMENT_ALREADY_EXISTS(CONFLICT, "이미 우수 스터디원으로 지정된 스터디원이 존재합니다."),

    // Attendance
    STUDY_SESSION_NOT_ATTENDABLE_DATE_MISMATCH(CONFLICT, "강의일이 아니면 출석체크할 수 없습니다."),
    STUDY_SESSION_NOT_ATTENDABLE_PERIOD_MISMATCH(CONFLICT, "강의 시간이 아니면 출석체크할 수 없습니다."),
    ATTENDANCE_NUMBER_MISMATCH(CONFLICT, "출석번호가 일치하지 않습니다."),
    STUDY_DETAIL_ALREADY_ATTENDED(CONFLICT, "이미 출석 처리된 스터디입니다."),
    STUDY_SESSION_ALREADY_ATTENDED(CONFLICT, "이미 출석 처리된 스터디 회차입니다."),

    // Order
    ORDER_NOT_FOUND(NOT_FOUND, "주문이 존재하지 않습니다."),
    ORDER_MEMBERSHIP_MEMBER_MISMATCH(CONFLICT, "주문 대상 멤버십의 멤버와 현재 로그인한 멤버가 일치하지 않습니다."),
    ORDER_MEMBERSHIP_ALREADY_PAID(CONFLICT, "주문 대상 멤버십의 회비가 이미 납부되었습니다."),
    ORDER_RECRUITMENT_PERIOD_INVALID(CONFLICT, "주문 대상 멤버십의 리크루팅의 지원기간이 아닙니다."),
    ORDER_ISSUED_COUPON_MEMBER_MISMATCH(CONFLICT, "주문 시 사용할 발급쿠폰의 멤버와 현재 로그인한 멤버가 일치하지 않습니다."),
    ORDER_TOTAL_AMOUNT_MISMATCH(CONFLICT, "주문 금액은 리쿠르팅 회비와 일치해야 합니다."),
    ORDER_DISCOUNT_AMOUNT_NOT_ZERO(CONFLICT, "쿠폰 미사용시 할인 금액은 0이어야 합니다."),
    ORDER_DISCOUNT_AMOUNT_MISMATCH(CONFLICT, "쿠폰 사용시 할인 금액은 쿠폰의 할인 금액과 일치해야 합니다."),
    ORDER_ALREADY_COMPLETED(CONFLICT, "이미 완료된 주문입니다."),
    ORDER_COMPLETE_AMOUNT_MISMATCH(CONFLICT, "주문 최종결제금액이 주문완료요청의 결제금액과 일치하지 않습니다."),
    ORDER_COMPLETE_MEMBER_MISMATCH(CONFLICT, "주문자와 현재 로그인한 멤버가 일치하지 않습니다."),
    ORDER_COMPLETED_PAID_NOT_FOUND(NOT_FOUND, "존재하지 않는 주문이거나, 완료되지 않은 유료 주문입니다."),
    ORDER_CANCEL_NOT_COMPLETED(CONFLICT, "완료되지 않은 주문은 취소할 수 없습니다."),
    ORDER_CANCEL_FREE_ORDER(CONFLICT, "무료 주문은 취소할 수 없습니다."),
    ORDER_CANCEL_RESPONSE_NOT_FOUND(INTERNAL_SERVER_ERROR, "주문 결제가 취소되었지만, 응답에 취소 정보가 존재하지 않습니다. 관리자에게 문의 바랍니다."),
    ORDER_FREE_FINAL_PAYMENT_NOT_ZERO(CONFLICT, "무료 주문의 최종결제금액은 0원이어야 합니다."),

    // Order - MoneyInfo
    ORDER_FINAL_PAYMENT_AMOUNT_MISMATCH(CONFLICT, "주문 최종결제금액은 주문총액에서 할인금액을 뺀 값이어야 합니다."),

    // Assignment
    ASSIGNMENT_INVALID_FAILURE_TYPE(CONFLICT, "유효하지 않은 제출 실패사유입니다."),
    ASSIGNMENT_DEADLINE_INVALID(CONFLICT, "과제 마감 기한이 현재보다 빠릅니다."),
    ASSIGNMENT_STUDY_NOT_APPLIED(CONFLICT, "해당 스터디에 대한 수강신청 기록이 존재하지 않습니다."),
    ASSIGNMENT_SUBMIT_NOT_STARTED(CONFLICT, "아직 과제가 시작되지 않았습니다."),
    ASSIGNMENT_SUBMIT_NOT_PUBLISHED(CONFLICT, "아직 과제가 등록되지 않았습니다."),
    ASSIGNMENT_SUBMIT_CANCELED(CONFLICT, "과제 휴강 주간에는 과제를 제출할 수 없습니다."),
    ASSIGNMENT_SUBMIT_DEADLINE_PASSED(CONFLICT, "과제 마감 기한이 지났습니다."),

    // AssignmentHistory (Assignment V2)
    ASSIGNMENT_HISTORY_NOT_WITHIN_PERIOD(INTERNAL_SERVER_ERROR, "제출상태 변환 시 제출기한에 포함되지 않는 제출이력을 인자로 받을 수 없습니다."),

    // Github
    GITHUB_REPOSITORY_NOT_FOUND(NOT_FOUND, "존재하지 않는 레포지토리입니다."),
    GITHUB_CONTENT_NOT_FOUND(NOT_FOUND, "존재하지 않는 파일입니다."),
    GITHUB_FILE_READ_FAILED(INTERNAL_SERVER_ERROR, "깃허브 파일 읽기에 실패했습니다."),
    GITHUB_COMMIT_DATE_FETCH_FAILED(INTERNAL_SERVER_ERROR, "깃허브 커밋 날짜 조회에 실패했습니다."),
    GITHUB_USER_NOT_FOUND(NOT_FOUND, "존재하지 않는 깃허브 유저입니다."),

    // Excel
    EXCEL_WORKSHEET_WRITE_FAILED(INTERNAL_SERVER_ERROR, "엑셀 워크시트 작성에 실패했습니다."),

    // Event
    EVENT_NOT_FOUND(NOT_FOUND, "존재하지 않는 이벤트입니다."),
    EVENT_NOT_CREATABLE_PAYMENT_ENABLED(CONFLICT, "뒤풀이 상태가 비활성화된 경우, 선입금 및 후정산 상태도 비활성화 되어야 합니다."),
    EVENT_NOT_CREATABLE_PAYMENTS_BOTH_ENABLED(CONFLICT, "선입금과 후정산은 동시에 활성화될 수 없습니다."),
    EVENT_NOT_APPLICABLE_NOT_REGULAR_ROLE(CONFLICT, "정회원이 아닌 회원은 이벤트에 신청할 수 없습니다."),
    EVENT_NOT_APPLICABLE_APPLICATION_PERIOD_INVALID(CONFLICT, "이벤트 신청 기간이 아닙니다."),
    EVENT_NOT_APPLICABLE_AFTER_PARTY_NONE(CONFLICT, "뒤풀이가 활성화된 이벤트는 뒤풀이 신청 여부를 NONE으로 설정할 수 없습니다."),
    EVENT_NOT_APPLICABLE_AFTER_PARTY_DISABLED(CONFLICT, "뒤풀이가 비활성화된 이벤트에 뒤풀이 신청을 할 수 없습니다."),
    PARTICIPATION_NOT_READABLE_AFTER_PARTY_DISABLED(CONFLICT, "뒤풀이가 비활성화된 이벤트의 참여정보는 조회할 수 없습니다."),
    PARTICIPATION_NOT_DELETABLE_INVALID_IDS(BAD_REQUEST, "존재하지 않거나 중복된 참여 정보 ID가 포함되어 있습니다."),
    PARTICIPANT_NOT_CREATABLE_INFO_NOT_SATISFIED(
            INTERNAL_SERVER_ERROR, "기본 정보를 입력하지 않은 멤버로 참여자 정보 생성을 시도했습니다. 관리자에게 문의 바랍니다."),
    PARTICIPANT_NOT_CREATABLE_FIELD_NOT_NULL(BAD_REQUEST, "참여자 정보의 이름, 학번, 전화번호는 null이 될 수 없습니다."),
    PARTICIPANT_NOT_CREATABLE_STUDENT_ID_INVALID(BAD_REQUEST, "참여자 정보의 학번 형식이 올바르지 않습니다."),
    PARTICIPANT_NOT_CREATABLE_PHONE_INVALID(BAD_REQUEST, "참여자 정보의 전화번호 형식이 올바르지 않습니다."),
    PARTICIPANT_ROLE_NOT_CREATABLE_BOTH_EXISTENCE_MISMATCH(
            INTERNAL_SERVER_ERROR, "이벤트 참여정보의 멤버 ID와 멤버 인자는 둘 다 null이거나 not null이어야 합니다."),
    PARTICIPANT_ROLE_NOT_CREATABLE_BOTH_ID_MISMATCH(INTERNAL_SERVER_ERROR, "이벤트 참여정보의 멤버 ID와 인자의 멤버 ID가 일치하지 않습니다."),
    ;

    private final HttpStatus status;
    private final String message;
}

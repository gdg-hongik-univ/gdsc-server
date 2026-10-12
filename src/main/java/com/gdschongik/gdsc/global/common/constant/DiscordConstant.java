package com.gdschongik.gdsc.global.common.constant;

public class DiscordConstant {

    private DiscordConstant() {}

    // 공통 상수
    public static final String DISCORD_BOT_STATUS_CONTENT = "정상영업";
    public static final String MEMBER_ROLE_NAME = "커뮤니티-멤버";
    public static final String ADMIN_ROLE_NAME = "운영진";

    // 인증코드 발급 커맨드
    public static final String COMMAND_NAME_ISSUING_CODE = "인증코드";
    public static final String COMMAND_DESCRIPTION_ISSUING_CODE = "디스코드 연동을 위한 인증코드를 발급받습니다.";
    public static final String DEFER_MESSAGE_ISSUING_CODE = "인증코드를 발급받는 중입니다...";
    public static final String REPLY_MESSAGE_ISSUING_CODE = "인증코드는 %d 입니다. 인증코드는 %d분 동안 유효합니다.";

    // 디스코드 ID 저장 커맨드
    public static final String COMMAND_NAME_BATCH_DISCORD_ID = "디스코드id-저장하기";
    public static final String COMMAND_DESCRIPTION_BATCH_DISCORD_ID = "디스코드 인증이 완료된 멤버들의 디스코드 ID를 저장합니다.";
    public static final String DEFER_MESSAGE_BATCH_DISCORD_ID = "디스코드 ID 저장 배치 작업을 진행하는 중입니다...";
    public static final String REPLY_MESSAGE_BATCH_DISCORD_ID = "디스코드 ID 저장 배치 작업이 완료되었습니다.";

    // 정회원 승급 누락자 승급 커맨드
    public static final String COMMAND_NAME_ADVANCE_FAILED_MEMBER = "정회원-승급-누락자-승급하기";
    public static final String COMMAND_DESCRIPTION_ADVANCE_FAILED_MEMBER = "정회원 승급 누락자를 승급합니다.";
    public static final String DEFER_MESSAGE_ADVANCE_FAILED_MEMBER = "정회원 승급 누락자 승급 작업을 진행하는 중입니다...";
    public static final String REPLY_MESSAGE_ADVANCE_FAILED_MEMBER = "정회원 승급 누락자 승급 작업이 완료되었습니다.";

    // 어드민 권한 부여 커맨드
    public static final String COMMAND_NAME_ASSIGN_ADMIN_ROLE = "어드민-권한-부여";
    public static final String COMMAND_DESCRIPTION_ASSIGN_ADMIN_ROLE = "지정한 멤버에게 어드민 권한을 부여합니다.";
    public static final String DEFER_MESSAGE_ASSIGN_ADMIN_ROLE = "어드민 권한 부여 작업을 진행하는 중입니다...";
    public static final String REPLY_MESSAGE_ASSIGN_ADMIN_ROLE = "어드민 권한 부여 작업이 완료되었습니다.";
    public static final String OPTION_NAME_ASSIGN_ADMIN_ROLE = "학번";
    public static final String OPTION_DESCRIPTION_ASSIGN_ADMIN_ROLE = "어드민 권한을 부여할 멤버의 학번을 입력해주세요.";

    // 스터디 역할 부여 누락자 부여 커맨드
    public static final String COMMAND_NAME_ASSIGN_STUDY_ROLE = "스터디-역할-부여";
    public static final String COMMAND_DESCRIPTION_ASSIGN_STUDY_ROLE = "스터디 역할을 부여합니다.";
    public static final String DEFER_MESSAGE_ASSIGN_STUDY_ROLE = "스터디 역할 부여 작업을 진행하는 중입니다...";
    public static final String REPLY_MESSAGE_ASSIGN_STUDY_ROLE = "스터디 역할 부여 작업이 완료되었습니다.";
    public static final String OPTION_NAME_STUDY_TITLE = "스터디-이름";
    public static final String OPTION_DESCRIPTION_STUDY_TITLE = "스터디 이름을 입력해주세요.";
    public static final String OPTION_NAME_STUDY_YEAR = "스터디-연도";
    public static final String OPTION_DESCRIPTION_STUDY_YEAR = "연도를 입력해주세요. YYYY 형식으로 입력해주세요.";
    public static final String OPTION_NAME_STUDY_SEMESTER = "스터디-학기";
    public static final String OPTION_DESCRIPTION_STUDY_SEMESTER = "학기를 입력해주세요. FIRST 혹은 SECOND로 입력해주세요.";

    // 커뮤니티 통계 세미나 수 갱신 커맨드
    public static final String COMMAND_NAME_UPDATE_SEMINAR_COUNT = "커뮤니티-통계-세미나-수-갱신하기";
    public static final String COMMAND_DESCRIPTION_UPDATE_SEMINAR_COUNT = "커뮤니티 통계 세미나 수를 갱신합니다.";
    public static final String DEFER_MESSAGE_UPDATE_SEMINAR_COUNT = "커뮤니티 통계 세미나 수 갱신 작업을 진행하는 중입니다...";
    public static final String REPLY_MESSAGE_UPDATE_SEMINAR_COUNT = "커뮤니티 통계 세미나 수 갱신 작업이 완료되었습니다.";
    public static final String OPTION_NAME_SEMINAR_COUNT = "세미나-수";
    public static final String OPTION_DESCRIPTION_SEMINAR_COUNT = "세미나 수를 입력해주세요.";
}

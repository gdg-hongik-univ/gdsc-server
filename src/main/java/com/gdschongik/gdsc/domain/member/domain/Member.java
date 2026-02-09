package com.gdschongik.gdsc.domain.member.domain;

import static com.gdschongik.gdsc.domain.member.domain.MemberManageRole.*;
import static com.gdschongik.gdsc.domain.member.domain.MemberRole.*;
import static com.gdschongik.gdsc.domain.member.domain.MemberStudyRole.*;
import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.common.model.BaseEntity;
import com.gdschongik.gdsc.domain.member.domain.event.*;
import com.gdschongik.gdsc.global.exception.CustomException;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@SQLRestriction("status='NORMAL'")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    @Enumerated(EnumType.STRING)
    private MemberRole role;

    @Enumerated(EnumType.STRING)
    private MemberManageRole manageRole;

    @Enumerated(EnumType.STRING)
    private MemberStudyRole studyRole;

    private String name;

    private String studentId;

    @Enumerated(EnumType.STRING)
    private Department department;

    private String email;

    private String univEmail;

    private String phone;

    private String discordId;

    private String discordUsername;

    private String nickname;

    @Column(nullable = false)
    private String oauthId;

    private LocalDateTime lastLoginAt;

    @Embedded
    private AssociateRequirement associateRequirement;

    @Builder(access = AccessLevel.PRIVATE)
    private Member(
            MemberStatus status,
            MemberRole role,
            MemberManageRole manageRole,
            MemberStudyRole studyRole,
            String name,
            String studentId,
            Department department,
            String email,
            String univEmail,
            String phone,
            String discordUsername,
            String nickname,
            String oauthId,
            LocalDateTime lastLoginAt,
            AssociateRequirement associateRequirement) {
        this.status = status;
        this.role = role;
        this.manageRole = manageRole;
        this.studyRole = studyRole;
        this.name = name;
        this.studentId = studentId;
        this.department = department;
        this.email = email;
        this.univEmail = univEmail;
        this.phone = phone;
        this.discordUsername = discordUsername;
        this.nickname = nickname;
        this.oauthId = oauthId;
        this.lastLoginAt = lastLoginAt;
        this.associateRequirement = associateRequirement;
    }

    public static Member createGuest(String oauthId) {
        AssociateRequirement associateRequirement = AssociateRequirement.unsatisfied();
        return Member.builder()
                .status(MemberStatus.NORMAL)
                .role(GUEST)
                .manageRole(NONE)
                .studyRole(STUDENT)
                .oauthId(oauthId)
                .associateRequirement(associateRequirement)
                .build();
    }

    // 상태 검증 로직

    /**
     * 회원 상태를 변경할 수 있는지 검증합니다. 삭제되거나 차단된 회원은 상태를 변경할 수 없습니다.<br>
     * 대부분의 상태 변경 로직에서 사용됩니다.
     */
    private void validateStatusUpdatable() {
        if (status.isDeleted()) {
            throw new CustomException(MEMBER_DELETED);
        }
        if (status.isForbidden()) {
            throw new CustomException(MEMBER_FORBIDDEN);
        }
    }

    /**
     * 준회원 승급 가능 여부를 검증합니다.
     */
    private void validateAssociateAvailable() {
        if (isAssociate()) {
            throw new CustomException(MEMBER_ALREADY_ASSOCIATE);
        }

        if (!isGuest()) {
            throw new CustomException(MEMBER_NOT_GUEST);
        }

        associateRequirement.validateAllSatisfied();
    }

    /**
     * 정회원 승급 가능 여부를 검증합니다.
     */
    private void validateRegularAvailable() {
        if (isRegular()) {
            throw new CustomException(MEMBER_ALREADY_REGULAR);
        }

        if (!isAssociate()) {
            throw new CustomException(MEMBER_NOT_ASSOCIATE);
        }
    }

    // 준회원 승급 관련 로직

    /**
     * 기본 회원 정보를 작성합니다.
     * 기본정보 인증상태를 인증 처리합니다.
     */
    public void updateInfo(String studentId, String name, String phone, Department department, String email) {
        validateStatusUpdatable();

        this.studentId = studentId;
        this.name = name;
        this.phone = phone;
        this.department = department;
        this.email = email;

        associateRequirement.verifyInfo();

        registerEvent(new MemberAssociateRequirementUpdatedEvent(this.id));
    }

    /**
     * 재학생 이메일 인증을 진행합니다.
     * 재학생 이메일 인증상태를 인증 처리합니다.
     */
    public void completeUnivEmailVerification(String univEmail) {
        validateStatusUpdatable();

        // 이미 인증되어있으면 에러
        associateRequirement.checkVerifiableUniv();

        this.univEmail = univEmail;

        associateRequirement.verifyUniv();

        registerEvent(new MemberAssociateRequirementUpdatedEvent(this.id));
    }

    /**
     * 디스코드 서버와의 연동을 진행합니다.
     * 디스코드 인증상태를 인증 처리합니다.
     */
    public void verifyDiscord(String discordUsername, String nickname, String discordId) {
        validateStatusUpdatable();

        updateDiscordUsername(discordUsername);
        updateNickname(nickname);
        updateDiscordId(discordId);

        associateRequirement.verifyDiscord();

        registerEvent(new MemberAssociateRequirementUpdatedEvent(this.id));
    }

    /**
     * 디스코드 서버와의 재연동을 진행합니다.
     */
    public void changeDiscord(String discordUsername, String nickname, String discordId) {
        validateStatusUpdatable();

        String previousDiscordId = this.discordId;

        updateDiscordUsername(discordUsername);
        updateNickname(nickname);
        updateDiscordId(discordId);

        if (previousDiscordId != null && !previousDiscordId.equals(discordId)) {
            registerEvent(new MemberDiscordAccountChangedEvent(id, role, previousDiscordId, discordId));
        }
    }

    /**
     * 게스트에서 준회원으로 승급합니다.
     * 본 로직은 승급조건 충족 이벤트로 트리거됩니다. 다음 조건을 모두 충족하면 승급됩니다.
     * 조건 1 : 기본 회원정보 작성
     * 조건 2 : 재학생 인증
     * 조건 3 : 디스코드 인증
     * 조건 4 : 멤버가 게스트이어야 함
     */
    public void advanceToAssociate() {
        validateStatusUpdatable();

        validateAssociateAvailable();

        role = ASSOCIATE;
    }

    /**
     * 준회원에서 정회원으로 승급합니다.
     * 조건 1 : 멤버가 준회원이어야 함
     */
    public void advanceToRegular() {
        validateStatusUpdatable();

        validateRegularAvailable();

        role = REGULAR;

        registerEvent(new MemberAdvancedToRegularEvent(id, discordId));
    }

    /**
     * 정회원에서 준회원으로 강등합니다.
     */
    public void demoteToAssociate() {
        validateStatusUpdatable();

        role = ASSOCIATE;

        registerEvent(new MemberDemotedToAssociateEvent(id, discordId));
    }

    /**
     * 테스트 환경 구성을 위한 사용자 상태 변경 메소드
     * 1. 멤버 역할을 GUEST로 강등
     * 2. 준회원 가입 조건을 'PENDING'으로 변경
     */
    public void demoteToGuest() {
        role = GUEST;

        univEmail = null;
        name = null;
        department = null;
        studentId = null;
        phone = null;

        discordId = null;
        nickname = null;
        discordUsername = null;

        associateRequirement.demoteAssociateRequirement();
    }

    // 기타 역할 변경 로직

    public void assignToMentor() {
        validateStatusUpdatable();
        studyRole = MENTOR;
    }

    public void assignToAdmin() {
        validateStatusUpdatable();
        manageRole = ADMIN;
    }

    // 기타 상태 변경 로직

    public void updateLastLoginAt(LocalDateTime now) {
        this.lastLoginAt = now;
    }

    private void updateDiscordUsername(String discordUsername) {
        this.discordUsername = discordUsername;
    }

    private void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 디스코드 ID를 업데이트 합니다.
     */
    public void updateDiscordId(String discordId) {
        this.discordId = discordId;
    }

    /**
     * 해당 회원을 탈퇴 처리합니다.
     */
    public void withdraw() {
        if (status.isDeleted()) {
            throw new CustomException(MEMBER_DELETED);
        }
        status = MemberStatus.DELETED;
    }

    /**
     * 회원 정보를 수정합니다. 어드민만 사용할 수 있어야 합니다.
     */
    public void updateInfo(
            String studentId,
            String name,
            String phone,
            Department department,
            String email,
            String discordUsername,
            String nickname) {
        validateStatusUpdatable();

        this.studentId = studentId;
        this.name = name;
        this.phone = phone;
        this.department = department;
        this.email = email;
        this.discordUsername = discordUsername;
        this.nickname = nickname;
    }

    // 데이터 전달 로직
    public boolean isGuest() {
        return role == GUEST;
    }

    public boolean isAssociate() {
        return role == ASSOCIATE;
    }

    public boolean isRegular() {
        return role == REGULAR;
    }

    public boolean isAdmin() {
        return manageRole == ADMIN;
    }

    public boolean isMentor() {
        return studyRole == MENTOR;
    }

    public boolean isStudent() {
        return studyRole == STUDENT;
    }
}

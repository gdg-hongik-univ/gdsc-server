package com.gdschongik.gdsc.domain.study.domain;

import static com.gdschongik.gdsc.domain.study.domain.StudyHistoryStatus.*;

import com.gdschongik.gdsc.domain.common.model.BaseEntity;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.domain.study.domain.event.StudyApplyCanceledEvent;
import com.gdschongik.gdsc.domain.study.domain.event.StudyApplyCompletedEvent;
import com.gdschongik.gdsc.domain.study.domain.event.StudyHistoryCompletionWithdrawnEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "study_history_v2",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"member_id", "study_v2_id"})})
public class StudyHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_history_v2_id")
    private Long id;

    @Comment("수료 상태")
    @Enumerated(EnumType.STRING)
    private StudyHistoryStatus status;

    private String repositoryLink;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_v2_id")
    private Study study;

    @Builder(access = AccessLevel.PRIVATE)
    private StudyHistory(StudyHistoryStatus status, Member student, Study study) {
        this.status = status;
        this.student = student;
        this.study = study;
    }

    public static StudyHistory create(Member student, Study study) {
        return StudyHistory.builder().status(NONE).student(student).study(study).build();
    }

    @PostPersist
    private void postPersist() {
        registerEvent(new StudyApplyCompletedEvent(this.study.getDiscordRoleId(), this.student.getDiscordId()));
    }

    @PreRemove
    private void preRemove() {
        registerEvent(new StudyApplyCanceledEvent(
                this.study.getDiscordRoleId(), this.student.getDiscordId(), this.study.getId(), this.student.getId()));
    }

    /**
     * 레포지토리 링크를 업데이트합니다.
     */
    public void updateRepositoryLink(String repositoryLink) {
        this.repositoryLink = repositoryLink;
    }

    /**
     * 스터디 수료
     */
    public void complete() {
        this.status = COMPLETED;
    }

    /**
     * 스터디 수료 철회
     */
    public void withdrawCompletion() {
        this.status = NONE;
        registerEvent(new StudyHistoryCompletionWithdrawnEvent(this.id));
    }

    // 데이터 전달 로직
    public boolean isCompleted() {
        return this.status == COMPLETED;
    }
}

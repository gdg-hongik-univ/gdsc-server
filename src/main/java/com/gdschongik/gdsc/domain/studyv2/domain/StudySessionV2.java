package com.gdschongik.gdsc.domain.studyv2.domain;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.common.model.BaseEntity;
import com.gdschongik.gdsc.domain.common.vo.Period;
import com.gdschongik.gdsc.global.exception.CustomException;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Comment;

@Getter
@Entity
@Table(name = "study_session_v2")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class StudySessionV2 extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_session_v2_id")
    private Long id;

    @Comment("회차 순서")
    private Integer position;

    @Comment("회차 설명")
    private String description;

    // 수업 관련 필드

    @Comment("수업 제목")
    private String lessonTitle;

    @Comment("수업 출석 번호")
    private String lessonAttendanceNumber;

    @Embedded
    @AttributeOverride(name = "startDate", column = @Column(name = "lesson_start_at"))
    @AttributeOverride(name = "endDate", column = @Column(name = "lesson_end_at"))
    private Period lessonPeriod;

    // 과제 관련 필드

    @Comment("과제 제목")
    private String assignmentTitle;

    @Comment("과제 명세 링크")
    private String assignmentDescriptionLink;

    @Embedded
    @AttributeOverride(name = "startDate", column = @Column(name = "assignment_start_at"))
    @AttributeOverride(name = "endDate", column = @Column(name = "assignment_end_at"))
    private Period assignmentPeriod;

    // 참조 필드

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_v2_id")
    private StudyV2 study;

    /**
     * 모든 스터디회차는 인자로 전달되는 스터디 애그리거트 루트 엔티티에 종속됩니다.
     * 따라서 스터디회차 생성 팩토리 메서드 생성 시 자기 자신을 반환하지 않고,
     * 스터디 애그리거트 루트 엔티티의 스터디회차 컬렉션에 자기 자신을 추가하는 식으로 생성합니다.
     * 부모 클래스에서 위 로직을 수행하기 때문에, 생성 팩토리 메서드는 void 타입을 반환해야 합니다.
     */
    @Builder(access = AccessLevel.PRIVATE)
    private StudySessionV2(
            Integer position,
            String lessonTitle,
            String description,
            String lessonAttendanceNumber,
            Period lessonPeriod,
            String assignmentTitle,
            String assignmentDescriptionLink,
            Period assignmentPeriod,
            StudyV2 study) {
        this.position = position;
        this.lessonTitle = lessonTitle;
        this.description = description;
        this.lessonAttendanceNumber = lessonAttendanceNumber;
        this.lessonPeriod = lessonPeriod;
        this.assignmentTitle = assignmentTitle;
        this.assignmentDescriptionLink = assignmentDescriptionLink;
        this.assignmentPeriod = assignmentPeriod;
        this.study = study;
        study.getStudySessions().add(this);
    }

    public static void createEmptyForLive(Integer position, String lessonAttendanceNumber, StudyV2 study) {
        StudySessionV2.builder()
                .position(position)
                .lessonAttendanceNumber(lessonAttendanceNumber)
                .study(study)
                .build();
    }

    public static void createEmptyForAssignment(Integer position, StudyV2 study) {
        StudySessionV2.builder().position(position).study(study).build();
    }

    // 데이터 전달 로직

    public void validateAssignmentSubmittable(LocalDateTime now) {
        if (assignmentPeriod == null) {
            throw new CustomException(ASSIGNMENT_SUBMIT_NOT_PUBLISHED);
        }

        if (now.isBefore(assignmentPeriod.getStartDate())) {
            throw new CustomException(ASSIGNMENT_SUBMIT_NOT_STARTED);
        }

        if (now.isAfter(assignmentPeriod.getEndDate())) {
            throw new CustomException(ASSIGNMENT_SUBMIT_DEADLINE_PASSED);
        }
    }

    public boolean isAssignmentSubmittable(LocalDateTime now) {
        if (assignmentPeriod == null) {
            return false;
        }

        return assignmentPeriod.isWithin(now);
    }

    public boolean isAttendable(LocalDateTime now) {
        if (lessonPeriod == null) {
            return false;
        }

        return lessonPeriod.isWithin(now);
    }

    // 데이터 변경 로직

    public void update(StudyUpdateCommand.Session command) {
        validateLessonFieldNullWhenAssignmentStudy(command.lessonPeriod());
        this.lessonTitle = command.lessonTitle();
        this.description = command.description();
        this.lessonPeriod = command.lessonPeriod();
        this.assignmentTitle = command.assignmentTitle();
        this.assignmentDescriptionLink = command.assignmentDescriptionLink();
        this.assignmentPeriod = command.assignmentPeriod();
    }

    private void validateLessonFieldNullWhenAssignmentStudy(Period lessonPeriodToUpdate) {
        if (study.getType().isLive()) {
            return;
        }

        if (lessonPeriodToUpdate != null) {
            throw new CustomException(STUDY_NOT_UPDATABLE_LESSON_FIELD_NOT_NULL);
        }
    }
}

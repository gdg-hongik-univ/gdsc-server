package com.gdschongik.gdsc.domain.study.domain;

import static com.gdschongik.gdsc.global.exception.ErrorCode.*;

import com.gdschongik.gdsc.domain.common.model.BaseEntity;
import com.gdschongik.gdsc.domain.common.vo.Period;
import com.gdschongik.gdsc.domain.common.vo.Semester;
import com.gdschongik.gdsc.domain.member.domain.Member;
import com.gdschongik.gdsc.global.exception.CustomException;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

/**
 * 스터디 애그리거트 루트 엔티티입니다.
 * [메타] 태그가 있는 필드는 수강 신청 시 참고용으로만 사용되며, 스터디회차 엔티티 내부 상태 검증에 사용되지 않습니다.
 * 혼동이 예상되는 경우 코멘트에 해당 태그가 명시되어 있습니다.
 * 그 외 필드의 경우 스터디 애그리거트 내부 상태 검증에 사용될 수 있습니다. (ex: 총 회차 수)
 */
@Getter
@Entity
@Table(name = "study_v2")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Study extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "study_v2_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private StudyType type;

    @Comment("스터디 이름")
    private String title;

    @Comment("스터디 소개")
    private String description;

    @Comment("스터디 소개 노션 링크")
    @Column(columnDefinition = "TEXT")
    private String descriptionNotionLink;

    @Embedded
    private Semester semester;

    @Comment("총 회차 수")
    private Integer totalRound;

    @Comment("[메타] 스터디 요일")
    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    @Comment("[메타] 스터디 시작 시간")
    private LocalTime startTime;

    @Comment("[메타] 스터디 종료 시간")
    private LocalTime endTime;

    @Embedded
    @AttributeOverride(name = "startDate", column = @Column(name = "application_start_date"))
    @AttributeOverride(name = "endDate", column = @Column(name = "application_end_date"))
    private Period applicationPeriod;

    @Comment("디스코드 채널 ID")
    private String discordChannelId;

    @Comment("디스코드 역할 ID")
    private String discordRoleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member mentor;

    @Comment("과제 최소 글자수")
    private Integer minAssignmentLength;

    @OrderBy("position asc")
    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudySession> studySessions = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Study(
            StudyType type,
            String title,
            String description,
            String descriptionNotionLink,
            Semester semester,
            Integer totalRound,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            Period applicationPeriod,
            String discordChannelId,
            String discordRoleId,
            Member mentor,
            Integer minAssignmentLength) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.descriptionNotionLink = descriptionNotionLink;
        this.semester = semester;
        this.totalRound = totalRound;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.applicationPeriod = applicationPeriod;
        this.discordChannelId = discordChannelId;
        this.discordRoleId = discordRoleId;
        this.mentor = mentor;
        this.minAssignmentLength = minAssignmentLength;
    }

    /**
     * 라이브 스터디를 생성합니다.
     */
    public static Study createLive(
            StudyType type,
            String title,
            Semester semester,
            Integer totalRound,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            Period applicationPeriod,
            String discordChannelId,
            String discordRoleId,
            Member mentor,
            Integer minAssignmentLength) {
        validateLiveStudy(type);
        return Study.builder()
                .type(type)
                .title(title)
                .semester(semester)
                .totalRound(totalRound)
                .dayOfWeek(dayOfWeek)
                .startTime(startTime)
                .endTime(endTime)
                .applicationPeriod(applicationPeriod)
                .discordChannelId(discordChannelId)
                .discordRoleId(discordRoleId)
                .mentor(mentor)
                .minAssignmentLength(minAssignmentLength)
                .build();
    }

    private static void validateLiveStudy(StudyType type) {
        if (!type.isLive()) {
            throw new CustomException(STUDY_NOT_CREATABLE_NOT_LIVE);
        }
    }

    /**
     * 과제 스터디를 생성합니다.
     */
    public static Study createAssignment(
            String title,
            Semester semester,
            Integer totalRound,
            Period applicationPeriod,
            String discordChannelId,
            String discordRoleId,
            Member mentor,
            Integer minAssignmentLength) {
        return Study.builder()
                .type(StudyType.ASSIGNMENT)
                .title(title)
                .semester(semester)
                .totalRound(totalRound)
                .applicationPeriod(applicationPeriod)
                .discordChannelId(discordChannelId)
                .discordRoleId(discordRoleId)
                .mentor(mentor)
                .minAssignmentLength(minAssignmentLength)
                .build();
    }

    // 데이터 조회 로직

    public Optional<StudySession> getOptionalStudySession(Long studySessionId) {
        return studySessions.stream()
                .filter(session -> session.getId().equals(studySessionId))
                .findFirst();
    }

    public StudySession getStudySession(Long studySessionId) {
        return getOptionalStudySession(studySessionId).orElseThrow(() -> new CustomException(STUDY_SESSION_NOT_FOUND));
    }

    public boolean isApplicable(LocalDateTime now) {
        return applicationPeriod.isWithin(now);
    }

    public LocalDateTime getOpeningDate() {
        return studySessions.stream()
                .filter(studySession -> studySession.getPosition() == 1)
                .findFirst()
                .map(studySession -> type.isLive()
                        ? studySession.getLessonPeriod().getStartDate()
                        : studySession.getAssignmentPeriod().getStartDate())
                .orElse(null);
    }

    // 데이터 변경 로직

    public void update(StudyUpdateCommand command) {
        this.title = command.title();
        this.description = command.description();
        this.descriptionNotionLink = command.descriptionNotionLink();
        this.dayOfWeek = command.dayOfWeek();
        this.startTime = command.startTime();
        this.endTime = command.endTime();
        this.minAssignmentLength = command.minAssignmentLength();

        command.studySessions().forEach(sessionCommand -> {
            getStudySessionForUpdate(sessionCommand.studySessionId()).update(sessionCommand);
        });

        validateLessonTimeOrderMatchesPosition();
    }

    private StudySession getStudySessionForUpdate(Long studySessionId) {
        return getOptionalStudySession(studySessionId)
                .orElseThrow(() -> new CustomException(STUDY_NOT_UPDATABLE_SESSION_NOT_FOUND));
    }

    /**
     * 위치에 따라 정렬된 스터디회차의 수업 진행일들의 순차성을 검증합니다.
     * lessonPeriod가 null인 초기 StudySession은 검증에서 제외됩니다.
     */
    private void validateLessonTimeOrderMatchesPosition() {
        // position 순서로 정렬
        List<StudySession> sortedSessions = studySessions.stream()
                .sorted(Comparator.comparing(StudySession::getPosition))
                .toList();

        LocalDateTime previousStartDate = null;

        for (StudySession session : sortedSessions) {
            Period currentLessonPeriod = session.getLessonPeriod();

            // lessonPeriod가 null인 경우 검증 제외
            if (currentLessonPeriod == null) {
                continue;
            }

            LocalDateTime currentStartDate = currentLessonPeriod.getStartDate();

            // 이전 시작일이 존재하고, 현재 시작일이 이전 시작일보다 과거이거나 같은 경우 실패
            if (previousStartDate != null
                    && (currentStartDate.isBefore(previousStartDate) || currentStartDate.isEqual(previousStartDate))) {
                throw new CustomException(STUDY_NOT_UPDATABLE_LESSON_PERIOD_NOT_SEQUENTIAL);
            }

            previousStartDate = currentStartDate;
        }
    }
}

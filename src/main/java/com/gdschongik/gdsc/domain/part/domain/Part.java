package com.gdschongik.gdsc.domain.part.domain;

import com.gdschongik.gdsc.domain.common.model.BaseEntity;
import com.gdschongik.gdsc.domain.common.vo.Semester;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"academic_year", "semester_type", "part_type"})})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Part extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "part_id")
    private Long id;

    @Embedded
    private Semester semester;

    @Enumerated(EnumType.STRING)
    private PartType partType;

    // todo: 향후 Image 엔티티로 변경
    @Column(columnDefinition = "TEXT")
    private String thumbnailUrl;

    private String leadName;

    @ElementCollection
    @CollectionTable(name = "part_introduction", joinColumns = @JoinColumn(name = "part_id"))
    @Column(name = "content")
    @OrderColumn(name = "position")
    private List<String> introductions = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "part_recommendation", joinColumns = @JoinColumn(name = "part_id"))
    @Column(name = "content")
    @OrderColumn(name = "position")
    private List<String> recommendations = new ArrayList<>();

    @OrderBy("position asc")
    @OneToMany(mappedBy = "part", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PartSession> partSessions = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Part(
            Semester semester,
            PartType partType,
            String thumbnailUrl,
            String leadName,
            List<String> introductions,
            List<String> recommendations) {
        this.semester = semester;
        this.partType = partType;
        this.thumbnailUrl = thumbnailUrl;
        this.leadName = leadName;
        this.introductions = new ArrayList<>(introductions);
        this.recommendations = new ArrayList<>(recommendations);
    }

    public static Part create(
            Semester semester,
            PartType partType,
            String thumbnailUrl,
            String leadName,
            List<String> introductions,
            List<String> recommendations) {
        return Part.builder()
                .semester(semester)
                .partType(partType)
                .thumbnailUrl(thumbnailUrl)
                .leadName(leadName)
                .introductions(introductions)
                .recommendations(recommendations)
                .build();
    }
}

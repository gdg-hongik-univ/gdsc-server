package com.gdschongik.gdsc.domain.part.domain;

import com.gdschongik.gdsc.domain.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PartSession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "part_session_id")
    private Long id;

    private Integer position;

    private String title;

    private String description;

    private boolean isHighlighted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id")
    private Part part;

    @Builder(access = AccessLevel.PRIVATE)
    private PartSession(Integer position, String title, String description, boolean isHighlighted, Part part) {
        this.position = position;
        this.title = title;
        this.description = description;
        this.isHighlighted = isHighlighted;
        this.part = part;
        part.getPartSessions().add(this);
    }

    public static PartSession create(
            Integer position, String title, String description, boolean isHighlighted, Part part) {
        return PartSession.builder()
                .position(position)
                .title(title)
                .description(description)
                .isHighlighted(isHighlighted)
                .part(part)
                .build();
    }
}

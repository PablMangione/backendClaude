package com.acainfo.backendclaude.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "subjects",
        uniqueConstraints =
        @UniqueConstraint(
                name = "uq_subjects_name_major",
                columnNames = {"name", "major_id"}))
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "major_id", nullable = false)
    private Major major;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_year_id", nullable = false)
    private StudyYear studyYear;

    @NotNull
    @Min(value = 0, message = "Monthly price cannot be negative")
    @Column(name = "monthly_price", nullable = false)
    private Integer monthlyPrice;

    // CAMBIO: Removemos @Lob y especificamos un tamaño máximo
    @Size(max = 1000) // O el tamaño que necesites
    @Column(name = "description", length = 1000)
    private String description;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    private void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    @OneToMany(mappedBy = "subject")
    @Builder.Default
    private Set<SubjectsGroup> subjectsGroups = new LinkedHashSet<>();

}
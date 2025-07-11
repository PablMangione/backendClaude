package com.acainfo.backendclaude.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "subjects_groups",
        uniqueConstraints =
        @UniqueConstraint(
                name = "uq_subjects_groups_name_subject",
                columnNames = {"name", "subject_id"}))
public class SubjectsGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    // RELACIÓN FALTANTE CON SUBJECT
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "instructor_id")
    private Instructor instructor;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;


    @AssertTrue(message = "End date must be after start date")
    boolean isValidDates() {
        return endDate == null ||
                startDate == null ||
                endDate.isAfter(startDate);
    }

    @Min(value = 1, message = "Max capacity must be at least 1")
    @ColumnDefault("30")
    @Column(name = "max_capacity")
    private Integer maxCapacity;

    // USAR ENUM EN LUGAR DE STRING
    @Enumerated(EnumType.STRING)
    @ColumnDefault("'PLANNED'")
    @Column(name = "status")
    @Builder.Default
    private GroupStatus status = GroupStatus.PLANNED;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    private void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if(maxCapacity == null) {
            maxCapacity = 30;
        }
    }

    // RELACIONES BIDIRECCIONALES
    @OneToMany(mappedBy = "group")
    @Builder.Default
    private Set<Registration> registrations = new LinkedHashSet<>();

    @OneToMany(mappedBy = "subjectsGroup")
    @Builder.Default
    private Set<Session> sessions = new LinkedHashSet<>();

    public enum GroupStatus {
        PLANNED, ACTIVE, COMPLETED, CANCELLED
    }
}
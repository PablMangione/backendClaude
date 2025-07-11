package com.acainfo.backendclaude.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "sessions")
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "subjects_group_id", nullable = false)
    private SubjectsGroup subjectsGroup;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Builder.Default
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek = DayOfWeek.MONDAY;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull
    @Min(value = 60, message = "La duracion debe ser al menos una hora")
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @ColumnDefault("1")
    @Column(name = "is_active")
    private Boolean isActive;

    @OneToMany(mappedBy = "session")
    @Builder.Default
    private Set<SessionInstance> sessionInstances = new LinkedHashSet<>();

    @PrePersist
    public void prePersist() {
        isActive = true;
    }

    public enum DayOfWeek {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
    }

}
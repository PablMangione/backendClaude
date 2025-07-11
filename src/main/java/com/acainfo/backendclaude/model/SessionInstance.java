package com.acainfo.backendclaude.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "session_instances",
        uniqueConstraints =
        @UniqueConstraint(
                name = "uq_session_instances_session_date",
                columnNames = {"session_id", "session_date"}))
public class SessionInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @NotNull
    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "actual_start_time")
    private LocalTime actualStartTime;

    @Column(name = "actual_end_time")
    private LocalTime actualEndTime;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'SCHEDULED'")
    @Builder.Default
    @Column(name = "status")
    private SessionStatus status = SessionStatus.SCHEDULED;

    @Lob
    @Column(name = "notes")
    private String notes;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    private void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    @OneToMany(mappedBy = "sessionInstance")
    @Builder.Default
    private Set<Attendance> attendances = new LinkedHashSet<>();

    @OneToMany(mappedBy = "sessionInstance")
    @Builder.Default
    private Set<Incident> incidents = new LinkedHashSet<>();

    public enum SessionStatus {
        SCHEDULED, COMPLETED, CANCELLED
    }

}
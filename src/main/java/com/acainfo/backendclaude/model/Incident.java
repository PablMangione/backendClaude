package com.acainfo.backendclaude.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "incidents")
public class Incident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "session_instance_id", nullable = false)
    private SessionInstance sessionInstance;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "incident_type", nullable = false)
    private IncidentType incidentType = IncidentType.OTHER;

    // CAMBIO: Removemos @Lob y especificamos un tamaño máximo
    @Size(max = 1000) // O el tamaño que necesites
    @Column(name = "description", length = 1000,nullable = false)
    private String description;

    @Size(max = 255)
    @Column(name = "reported_by")
    private String reportedBy;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "reported_at")
    private Instant reportedAt;

    @PrePersist
    private void prePersist() {
        if (reportedAt == null) {
            reportedAt = Instant.now();
        }
    }

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    public enum IncidentType {
        CANCELLATION, DELAY, ROOM_CHANGE, INSTRUCTOR_CHANGE, OTHER
    }

}
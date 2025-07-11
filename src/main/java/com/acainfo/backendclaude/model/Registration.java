package com.acainfo.backendclaude.model;

import jakarta.persistence.*;
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
@Table(name = "registrations")
public class Registration {
    @EmbeddedId
    private RegistrationId id;

    @MapsId("studentId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @MapsId("groupId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "group_id", nullable = false)
    private SubjectsGroup group;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "registration_date")
    private Instant registrationDate;

    @PrePersist
    private void prePersist() {
        if (registrationDate == null) {
            registrationDate = Instant.now();
        }
    }


    @ColumnDefault("'active'")
    @Column(name = "status")
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private RegistrationStatus status = RegistrationStatus.ACTIVE;

    public enum RegistrationStatus {
        ACTIVE, DROPPED, COMPLETED
    }

}


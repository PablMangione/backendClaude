package com.acainfo.backendclaude.model;

import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Incident Entity Mapping Tests")
class IncidentMappingTests {

    @Autowired
    private EntityManager em;

    // ========================================
    // Helper Methods
    // ========================================

    private SessionInstance createAndPersistSessionInstance() {
        // Create Major
        Major major = Major.builder()
                .name("Computer Science")
                .build();
        em.persist(major);

        // Create StudyYear
        StudyYear year = StudyYear.builder()
                .name("2nd Year")
                .level(2)
                .build();
        em.persist(year);

        // Create Subject
        Subject subject = Subject.builder()
                .name("Data Structures")
                .major(major)
                .studyYear(year)
                .monthlyPrice(150)
                .build();
        em.persist(subject);

        // Create SubjectsGroup
        SubjectsGroup group = SubjectsGroup.builder()
                .name("Group A")
                .subject(subject)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(4))
                .maxCapacity(30)
                .build();
        em.persist(group);

        // Create Session
        Session session = Session.builder()
                .subjectsGroup(group)
                .dayOfWeek(Session.DayOfWeek.WEDNESDAY)
                .startTime(LocalTime.of(10, 0))
                .durationMinutes(120)
                .isActive(true)
                .build();
        em.persist(session);

        // Create SessionInstance
        SessionInstance instance = SessionInstance.builder()
                .session(session)
                .sessionDate(LocalDate.now())
                .status(SessionInstance.SessionStatus.SCHEDULED)
                .build();
        em.persist(instance);
        em.flush();

        return instance;
    }

    // ========================================
    // Basic CRUD Tests
    // ========================================

    @Test
    @Transactional
    @DisplayName("Should handle minimal valid incident")
    void shouldHandleMinimalValidIncident() {
        // Given
        SessionInstance sessionInstance = createAndPersistSessionInstance();

        Incident incident = Incident.builder()
                .sessionInstance(sessionInstance)
                .incidentType(Incident.IncidentType.OTHER)
                .description("Test incident description")
                .build();

        // When
        em.persist(incident);
        em.flush();

        // Then
        assertNotNull(incident.getId());
        assertEquals(sessionInstance, incident.getSessionInstance());
        assertEquals(Incident.IncidentType.OTHER, incident.getIncidentType());
        assertEquals("Test incident description", incident.getDescription());
        assertNull(incident.getReportedBy());
        assertNotNull(incident.getReportedAt());
        assertNull(incident.getResolvedAt());
    }

    @Test
    @Transactional
    @DisplayName("Should create incident with all fields")
    void shouldCreateIncidentWithAllFields() {
        // Given
        SessionInstance sessionInstance = createAndPersistSessionInstance();
        Instant reportedTime = Instant.now().minus(2, ChronoUnit.HOURS);
        Instant resolvedTime = Instant.now();

        Incident incident = Incident.builder()
                .sessionInstance(sessionInstance)
                .incidentType(Incident.IncidentType.ROOM_CHANGE)
                .description("Room changed from A101 to B203 due to maintenance")
                .reportedBy("John Doe")
                .reportedAt(reportedTime)
                .resolvedAt(resolvedTime)
                .build();

        // When
        em.persist(incident);
        em.flush();

        // Then
        assertNotNull(incident.getId());
        assertEquals(Incident.IncidentType.ROOM_CHANGE, incident.getIncidentType());
        assertEquals("Room changed from A101 to B203 due to maintenance", incident.getDescription());
        assertEquals("John Doe", incident.getReportedBy());
        assertEquals(reportedTime, incident.getReportedAt());
        assertEquals(resolvedTime, incident.getResolvedAt());
    }

    @Test
    @Transactional
    @DisplayName("Should persist and retrieve incident")
    void shouldPersistAndRetrieveIncident() {
        // Given
        SessionInstance sessionInstance = createAndPersistSessionInstance();

        Incident incident = Incident.builder()
                .sessionInstance(sessionInstance)
                .incidentType(Incident.IncidentType.CANCELLATION)
                .description("Class cancelled due to instructor illness")
                .reportedBy("Admin Office")
                .build();

        em.persist(incident);
        em.flush();
        em.clear();

        // When
        Incident retrieved = em.find(Incident.class, incident.getId());

        // Then
        assertNotNull(retrieved);
        assertEquals(Incident.IncidentType.CANCELLATION, retrieved.getIncidentType());
        assertEquals("Class cancelled due to instructor illness", retrieved.getDescription());
        assertEquals("Admin Office", retrieved.getReportedBy());
        assertEquals(sessionInstance.getId(), retrieved.getSessionInstance().getId());
    }

    // ========================================
    // Validation Tests
    // ========================================

    @Test
    @DisplayName("Should reject null session instance")
    void shouldRejectNullSessionInstance() {
        // Given
        Incident incident = Incident.builder()
                .sessionInstance(null)
                .incidentType(Incident.IncidentType.DELAY)
                .description("Session delayed")
                .build();

        // When & Then
        assertThrows(ConstraintViolationException.class, () -> {
            em.persist(incident);
            em.flush();
        });
    }

    @Test
    @DisplayName("Should reject null incident type")
    void shouldRejectNullIncidentType() {
        // Given
        SessionInstance sessionInstance = createAndPersistSessionInstance();

        Incident incident = Incident.builder()
                .sessionInstance(sessionInstance)
                .incidentType(null)
                .description("Some incident")
                .build();

        // When & Then
        assertThrows(ConstraintViolationException.class, () -> {
            em.persist(incident);
            em.flush();
        });
    }

    @Test
    @DisplayName("Should reject null description")
    void shouldRejectNullDescription() {
        // Given
        SessionInstance sessionInstance = createAndPersistSessionInstance();

        Incident incident = Incident.builder()
                .sessionInstance(sessionInstance)
                .incidentType(Incident.IncidentType.OTHER)
                .description(null)
                .build();

        // When & Then
        assertThrows(org.hibernate.exception.ConstraintViolationException.class, () -> {
            em.persist(incident);
            em.flush();
        });
    }

    @Test
    @DisplayName("Should reject too long reported by")
    void shouldRejectTooLongReportedBy() {
        // Given
        SessionInstance sessionInstance = createAndPersistSessionInstance();
        String longReportedBy = "R".repeat(256);

        Incident incident = Incident.builder()
                .sessionInstance(sessionInstance)
                .incidentType(Incident.IncidentType.OTHER)
                .description("Test incident")
                .reportedBy(longReportedBy)
                .build();

        // When & Then
        assertThrows(ConstraintViolationException.class, () -> {
            em.persist(incident);
            em.flush();
        });
    }

    // ========================================
    // IncidentType Enum Tests
    // ========================================

    @Test
    @DisplayName("Should default incident type to OTHER")
    void shouldDefaultIncidentTypeToOther() {
        // Given
        SessionInstance sessionInstance = createAndPersistSessionInstance();

        Incident incident = Incident.builder()
                .sessionInstance(sessionInstance)
                .description("General incident")
                // No incident type specified - will use default
                .build();

        // When
        em.persist(incident);
        em.flush();

        // Then
        assertEquals(Incident.IncidentType.OTHER, incident.getIncidentType());
    }

    @Test
    @DisplayName("Should handle all incident types")
    void shouldHandleAllIncidentTypes() {
        // Given
        SessionInstance sessionInstance = createAndPersistSessionInstance();

        for (Incident.IncidentType type : Incident.IncidentType.values()) {
            Incident incident = Incident.builder()
                    .sessionInstance(sessionInstance)
                    .incidentType(type)
                    .description("Test incident of type " + type)
                    .build();

            // When
            em.persist(incident);
            em.flush();

            // Then
            assertEquals(type, incident.getIncidentType());
            em.clear();
        }
    }

    // ========================================
    // Optional Fields Tests
    // ========================================

    @Test
    @DisplayName("Should allow null reported by")
    void shouldAllowNullReportedBy() {
        // Given
        SessionInstance sessionInstance = createAndPersistSessionInstance();

        Incident incident = Incident.builder()
                .sessionInstance(sessionInstance)
                .incidentType(Incident.IncidentType.DELAY)
                .description("15 minutes delay")
                .reportedBy(null)
                .build();

        // When
        em.persist(incident);
        em.flush();

        // Then
        assertNotNull(incident.getId());
        assertNull(incident.getReportedBy());
    }

    @Test
    @DisplayName("Should allow null resolved at")
    void shouldAllowNullResolvedAt() {
        // Given
        SessionInstance sessionInstance = createAndPersistSessionInstance();

        Incident incident = Incident.builder()
                .sessionInstance(sessionInstance)
                .incidentType(Incident.IncidentType.INSTRUCTOR_CHANGE)
                .description("Substitute instructor assigned")
                .resolvedAt(null)
                .build();

        // When
        em.persist(incident);
        em.flush();

        // Then
        assertNotNull(incident.getId());
        assertNull(incident.getResolvedAt());
    }

    @Test
    @DisplayName("Should handle long description")
    void shouldHandleLongDescription() {
        // Given
        SessionInstance sessionInstance = createAndPersistSessionInstance();
        String longDescription = "This is a very detailed incident report. ".repeat(20);

        Incident incident = Incident.builder()
                .sessionInstance(sessionInstance)
                .incidentType(Incident.IncidentType.OTHER)
                .description(longDescription)
                .build();

        // When
        em.persist(incident);
        em.flush();
        em.clear();

        // Then
        Incident retrieved = em.find(Incident.class, incident.getId());
        assertNotNull(retrieved);
        assertEquals(longDescription, retrieved.getDescription());
    }

    // ========================================
    // Timestamp Tests
    // ========================================

    @Test
    @DisplayName("Should set reported at automatically")
    void shouldSetReportedAtAutomatically() {
        // Given
        SessionInstance sessionInstance = createAndPersistSessionInstance();
        Instant before = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        Incident incident = Incident.builder()
                .sessionInstance(sessionInstance)
                .incidentType(Incident.IncidentType.DELAY)
                .description("10 minutes delay")
                // No reportedAt specified
                .build();

        // When
        em.persist(incident);
        em.flush();

        // Then
        assertNotNull(incident.getReportedAt());
        Instant reportedAt = incident.getReportedAt().truncatedTo(ChronoUnit.SECONDS);
        assertTrue(reportedAt.equals(before) || reportedAt.isAfter(before));
    }

    @Test
    @DisplayName("Should respect manually set reported at")
    void shouldRespectManuallySetReportedAt() {
        // Given
        SessionInstance sessionInstance = createAndPersistSessionInstance();
        Instant customTime = Instant.now().minus(1, ChronoUnit.DAYS);

        Incident incident = Incident.builder()
                .sessionInstance(sessionInstance)
                .incidentType(Incident.IncidentType.CANCELLATION)
                .description("Class cancelled yesterday")
                .reportedAt(customTime)
                .build();

        // When
        em.persist(incident);
        em.flush();

        // Then
        assertEquals(customTime, incident.getReportedAt());
    }

    // ========================================
    // Relationship Tests
    // ========================================

    @Test
    @Transactional
    @DisplayName("Should cascade delete when session instance is deleted")
    void shouldCascadeDeleteWhenSessionInstanceDeleted() {
        // Given
        SessionInstance sessionInstance = createAndPersistSessionInstance();

        Incident incident = Incident.builder()
                .sessionInstance(sessionInstance)
                .incidentType(Incident.IncidentType.ROOM_CHANGE)
                .description("Room changed")
                .build();
        em.persist(incident);
        em.flush();

        Integer instanceId = sessionInstance.getId();
        Integer incidentId = incident.getId();
        em.clear();

        // When - Delete session instance
        SessionInstance toDelete = em.find(SessionInstance.class, instanceId);
        em.remove(toDelete);
        em.flush();
        em.clear();

        // Then
        assertNull(em.find(SessionInstance.class, instanceId));
        assertNull(em.find(Incident.class, incidentId)); // Should be cascade deleted
    }

    @Test
    @Transactional
    @DisplayName("Should handle multiple incidents for same session instance")
    void shouldHandleMultipleIncidentsForSameSessionInstance() {
        // Given
        SessionInstance sessionInstance = createAndPersistSessionInstance();

        Incident incident1 = Incident.builder()
                .sessionInstance(sessionInstance)
                .incidentType(Incident.IncidentType.DELAY)
                .description("Started 15 minutes late")
                .reportedBy("Student Representative")
                .build();

        Incident incident2 = Incident.builder()
                .sessionInstance(sessionInstance)
                .incidentType(Incident.IncidentType.ROOM_CHANGE)
                .description("Moved to larger room")
                .reportedBy("Admin")
                .build();

        // When
        em.persist(incident1);
        em.persist(incident2);
        em.flush();
        em.clear();

        // Then
        SessionInstance loaded = em.find(SessionInstance.class, sessionInstance.getId());
        assertNotNull(loaded);
        assertEquals(2, loaded.getIncidents().size());
    }

    // ========================================
    // Business Logic Tests
    // ========================================

    @Test
    @DisplayName("Should handle incident resolution workflow")
    void shouldHandleIncidentResolutionWorkflow() {
        // Given
        SessionInstance sessionInstance = createAndPersistSessionInstance();

        Incident incident = Incident.builder()
                .sessionInstance(sessionInstance)
                .incidentType(Incident.IncidentType.INSTRUCTOR_CHANGE)
                .description("Original instructor unavailable")
                .reportedBy("Department Head")
                .build();

        em.persist(incident);
        em.flush();

        // When - Resolve the incident
        incident.setResolvedAt(Instant.now());
        em.flush();
        em.clear();

        // Then
        Incident resolved = em.find(Incident.class, incident.getId());
        assertNotNull(resolved.getResolvedAt());
        assertTrue(resolved.getResolvedAt().isAfter(resolved.getReportedAt()));
    }

    @Test
    @DisplayName("Should handle special characters in fields")
    void shouldHandleSpecialCharactersInFields() {
        // Given
        SessionInstance sessionInstance = createAndPersistSessionInstance();

        Incident incident = Incident.builder()
                .sessionInstance(sessionInstance)
                .incidentType(Incident.IncidentType.OTHER)
                .description("Incident with special chars: áéíóú ñ 中文 😊 @#$%")
                .reportedBy("José García-O'Brien")
                .build();

        // When
        em.persist(incident);
        em.flush();
        em.clear();

        // Then
        Incident retrieved = em.find(Incident.class, incident.getId());
        assertNotNull(retrieved);
        assertEquals("Incident with special chars: áéíóú ñ 中文 😊 @#$%", retrieved.getDescription());
        assertEquals("José García-O'Brien", retrieved.getReportedBy());
    }

    @Test
    @DisplayName("Should generate id automatically")
    void shouldGenerateIdAutomatically() {
        // Given
        SessionInstance sessionInstance = createAndPersistSessionInstance();

        Incident incident = Incident.builder()
                .sessionInstance(sessionInstance)
                .incidentType(Incident.IncidentType.DELAY)
                .description("Auto ID test")
                .build();

        // When
        assertNull(incident.getId());
        em.persist(incident);
        em.flush();

        // Then
        assertNotNull(incident.getId());
        assertTrue(incident.getId() > 0);
    }

    @Test
    @DisplayName("Should handle maximum length reported by")
    void shouldHandleMaximumLengthReportedBy() {
        // Given
        SessionInstance sessionInstance = createAndPersistSessionInstance();
        String maxReportedBy = "R".repeat(255);

        Incident incident = Incident.builder()
                .sessionInstance(sessionInstance)
                .incidentType(Incident.IncidentType.OTHER)
                .description("Test with max length reporter")
                .reportedBy(maxReportedBy)
                .build();

        // When
        em.persist(incident);
        em.flush();

        // Then
        assertNotNull(incident.getId());
        assertEquals(maxReportedBy, incident.getReportedBy());
    }
}
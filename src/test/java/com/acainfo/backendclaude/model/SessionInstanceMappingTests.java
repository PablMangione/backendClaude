package com.acainfo.backendclaude.model;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
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
@DisplayName("SessionInstance Entity Mapping Tests")
class SessionInstanceMappingTests {

    @Autowired
    private EntityManager em;

    // ========================================
    // Helper Methods
    // ========================================

    private Session createAndPersistSession(String groupName, Session.DayOfWeek dayOfWeek) {
        // Create Major
        Major major = Major.builder()
                .name("Engineering")
                .build();
        em.persist(major);

        // Create StudyYear
        StudyYear year = StudyYear.builder()
                .name("3rd Year")
                .level(3)
                .build();
        em.persist(year);

        // Create Subject
        Subject subject = Subject.builder()
                .name("Advanced Programming")
                .major(major)
                .studyYear(year)
                .monthlyPrice(200)
                .build();
        em.persist(subject);

        // Create SubjectsGroup
        SubjectsGroup group = SubjectsGroup.builder()
                .name(groupName)
                .subject(subject)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(4))
                .maxCapacity(25)
                .build();
        em.persist(group);

        // Create Session
        Session session = Session.builder()
                .subjectsGroup(group)
                .dayOfWeek(dayOfWeek)
                .startTime(LocalTime.of(10, 0))
                .durationMinutes(120)
                .isActive(true)
                .build();
        em.persist(session);
        em.flush();

        return session;
    }

    // ========================================
    // Basic CRUD Tests
    // ========================================

    @Test
    @Transactional
    @DisplayName("Should handle minimal valid session instance")
    void shouldHandleMinimalValidSessionInstance() {
        // Given
        Session session = createAndPersistSession("Monday Group", Session.DayOfWeek.MONDAY);

        SessionInstance instance = SessionInstance.builder()
                .session(session)
                .sessionDate(LocalDate.now().plusDays(7))
                .build();

        // When
        em.persist(instance);
        em.flush();

        // Then
        assertNotNull(instance.getId());
        assertEquals(session, instance.getSession());
        assertEquals(LocalDate.now().plusDays(7), instance.getSessionDate());
        assertEquals(SessionInstance.SessionStatus.SCHEDULED, instance.getStatus()); // Default
        assertNull(instance.getActualStartTime());
        assertNull(instance.getActualEndTime());
        assertNull(instance.getNotes());
        assertNotNull(instance.getCreatedAt());
        assertNotNull(instance.getAttendances());
        assertTrue(instance.getAttendances().isEmpty());
        assertNotNull(instance.getIncidents());
        assertTrue(instance.getIncidents().isEmpty());
    }

    @Test
    @Transactional
    @DisplayName("Should create session instance with all fields")
    void shouldCreateSessionInstanceWithAllFields() {
        // Given
        Session session = createAndPersistSession("Tuesday Group", Session.DayOfWeek.TUESDAY);
        LocalDate sessionDate = LocalDate.now();
        LocalTime actualStart = LocalTime.of(10, 5);
        LocalTime actualEnd = LocalTime.of(12, 10);

        // When
        SessionInstance instance = SessionInstance.builder()
                .session(session)
                .sessionDate(sessionDate)
                .status(SessionInstance.SessionStatus.COMPLETED)
                .actualStartTime(actualStart)
                .actualEndTime(actualEnd)
                .notes("Session completed successfully with 5 minutes delay")
                .build();

        em.persist(instance);
        em.flush();

        // Then
        assertNotNull(instance.getId());
        assertEquals(SessionInstance.SessionStatus.COMPLETED, instance.getStatus());
        assertEquals(actualStart, instance.getActualStartTime());
        assertEquals(actualEnd, instance.getActualEndTime());
        assertEquals("Session completed successfully with 5 minutes delay", instance.getNotes());
    }

    @Test
    @Transactional
    @DisplayName("Should persist and retrieve session instance")
    void shouldPersistAndRetrieveSessionInstance() {
        // Given
        Session session = createAndPersistSession("Wednesday Group", Session.DayOfWeek.WEDNESDAY);

        SessionInstance instance = SessionInstance.builder()
                .session(session)
                .sessionDate(LocalDate.now().plusDays(14))
                .status(SessionInstance.SessionStatus.SCHEDULED)
                .notes("Mid-term exam session")
                .build();

        em.persist(instance);
        em.flush();
        em.clear();

        // When
        SessionInstance retrieved = em.find(SessionInstance.class, instance.getId());

        // Then
        assertNotNull(retrieved);
        assertEquals(LocalDate.now().plusDays(14), retrieved.getSessionDate());
        assertEquals(SessionInstance.SessionStatus.SCHEDULED, retrieved.getStatus());
        assertEquals("Mid-term exam session", retrieved.getNotes());
        assertEquals(session.getId(), retrieved.getSession().getId());
    }

    // ========================================
    // Validation Tests
    // ========================================

    @Test
    @DisplayName("Should reject null session")
    void shouldRejectNullSession() {
        // Given
        SessionInstance instance = SessionInstance.builder()
                .session(null)
                .sessionDate(LocalDate.now())
                .build();

        // When & Then
        assertThrows(ConstraintViolationException.class, () -> {
            em.persist(instance);
            em.flush();
        });
    }

    @Test
    @DisplayName("Should reject null session date")
    void shouldRejectNullSessionDate() {
        // Given
        Session session = createAndPersistSession("Test Group", Session.DayOfWeek.THURSDAY);

        SessionInstance instance = SessionInstance.builder()
                .session(session)
                .sessionDate(null)
                .build();

        // When & Then
        assertThrows(ConstraintViolationException.class, () -> {
            em.persist(instance);
            em.flush();
        });
    }

    // ========================================
    // Unique Constraint Tests
    // ========================================

    @Test
    @DisplayName("Should reject duplicate session date for same session")
    void shouldRejectDuplicateSessionDateForSameSession() {
        // Given
        Session session = createAndPersistSession("Friday Group", Session.DayOfWeek.FRIDAY);
        LocalDate sessionDate = LocalDate.now().plusDays(7);

        SessionInstance first = SessionInstance.builder()
                .session(session)
                .sessionDate(sessionDate)
                .build();
        em.persist(first);
        em.flush();

        SessionInstance duplicate = SessionInstance.builder()
                .session(session)
                .sessionDate(sessionDate) // Same date
                .build();

        // When & Then
        assertThrows(PersistenceException.class, () -> {
            em.persist(duplicate);
            em.flush();
        });
    }

    @Test
    @DisplayName("Should allow same date for different sessions")
    void shouldAllowSameDateForDifferentSessions() {
        // Given
        Session session1 = createAndPersistSession("Morning Session", Session.DayOfWeek.MONDAY);
        Session session2 = createAndPersistSession("Afternoon Session", Session.DayOfWeek.MONDAY);
        LocalDate sessionDate = LocalDate.now().plusDays(7);

        SessionInstance instance1 = SessionInstance.builder()
                .session(session1)
                .sessionDate(sessionDate)
                .build();

        SessionInstance instance2 = SessionInstance.builder()
                .session(session2)
                .sessionDate(sessionDate) // Same date, different session
                .build();

        // When
        em.persist(instance1);
        em.persist(instance2);
        em.flush();

        // Then
        assertNotNull(instance1.getId());
        assertNotNull(instance2.getId());
        assertNotEquals(instance1.getId(), instance2.getId());
    }

    // ========================================
    // Status Enum Tests
    // ========================================

    @Test
    @DisplayName("Should default status to SCHEDULED")
    void shouldDefaultStatusToScheduled() {
        // Given
        Session session = createAndPersistSession("Default Status Group", Session.DayOfWeek.SATURDAY);

        SessionInstance instance = SessionInstance.builder()
                .session(session)
                .sessionDate(LocalDate.now().plusDays(21))
                // No status specified
                .build();

        // When
        em.persist(instance);
        em.flush();

        // Then
        assertEquals(SessionInstance.SessionStatus.SCHEDULED, instance.getStatus());
    }

    @Test
    @DisplayName("Should handle all status values")
    void shouldHandleAllStatusValues() {
        // Given
        Session session = createAndPersistSession("Status Test Group", Session.DayOfWeek.SUNDAY);

        for (SessionInstance.SessionStatus status : SessionInstance.SessionStatus.values()) {
            SessionInstance instance = SessionInstance.builder()
                    .session(session)
                    .sessionDate(LocalDate.now().plusDays(status.ordinal() + 1))
                    .status(status)
                    .build();

            // When
            em.persist(instance);
            em.flush();

            // Then
            assertEquals(status, instance.getStatus());

            em.clear();
        }
    }

    // ========================================
    // Optional Fields Tests
    // ========================================

    @Test
    @DisplayName("Should allow null actual times")
    void shouldAllowNullActualTimes() {
        // Given
        Session session = createAndPersistSession("Null Times Group", Session.DayOfWeek.MONDAY);

        SessionInstance instance = SessionInstance.builder()
                .session(session)
                .sessionDate(LocalDate.now())
                .actualStartTime(null)
                .actualEndTime(null)
                .build();

        // When
        em.persist(instance);
        em.flush();

        // Then
        assertNotNull(instance.getId());
        assertNull(instance.getActualStartTime());
        assertNull(instance.getActualEndTime());
    }

    @Test
    @DisplayName("Should allow null notes")
    void shouldAllowNullNotes() {
        // Given
        Session session = createAndPersistSession("No Notes Group", Session.DayOfWeek.TUESDAY);

        SessionInstance instance = SessionInstance.builder()
                .session(session)
                .sessionDate(LocalDate.now())
                .notes(null)
                .build();

        // When
        em.persist(instance);
        em.flush();

        // Then
        assertNotNull(instance.getId());
        assertNull(instance.getNotes());
    }

    @Test
    @DisplayName("Should handle long notes")
    void shouldHandleLongNotes() {
        // Given
        Session session = createAndPersistSession("Long Notes Group", Session.DayOfWeek.WEDNESDAY);
        String longNotes = "This is a very detailed session instance note. ".repeat(100);

        SessionInstance instance = SessionInstance.builder()
                .session(session)
                .sessionDate(LocalDate.now())
                .notes(longNotes)
                .build();

        // When
        em.persist(instance);
        em.flush();
        em.clear();

        // Then
        SessionInstance retrieved = em.find(SessionInstance.class, instance.getId());
        assertNotNull(retrieved);
        assertEquals(longNotes, retrieved.getNotes());
    }

    // ========================================
    // Timestamp Tests
    // ========================================

    @Test
    @DisplayName("Should set created at automatically")
    void shouldSetCreatedAtAutomatically() {
        // Given
        Session session = createAndPersistSession("Timestamp Group", Session.DayOfWeek.THURSDAY);
        Instant before = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        SessionInstance instance = SessionInstance.builder()
                .session(session)
                .sessionDate(LocalDate.now())
                .build();

        // When
        em.persist(instance);
        em.flush();

        // Then
        assertNotNull(instance.getCreatedAt());
        Instant createdAt = instance.getCreatedAt().truncatedTo(ChronoUnit.SECONDS);
        assertTrue(createdAt.equals(before) || createdAt.isAfter(before));
    }

    // ========================================
    // Relationship Tests
    // ========================================

    @Test
    @Transactional
    @DisplayName("Should cascade delete when session is deleted")
    void shouldCascadeDeleteWhenSessionDeleted() {
        // Given
        Session session = createAndPersistSession("To Delete Session", Session.DayOfWeek.FRIDAY);

        SessionInstance instance = SessionInstance.builder()
                .session(session)
                .sessionDate(LocalDate.now())
                .status(SessionInstance.SessionStatus.SCHEDULED)
                .build();
        em.persist(instance);
        em.flush();

        Integer sessionId = session.getId();
        Integer instanceId = instance.getId();
        em.clear();

        // When - Delete session
        Session toDelete = em.find(Session.class, sessionId);
        em.remove(toDelete);
        em.flush();
        em.clear();

        // Then
        assertNull(em.find(Session.class, sessionId));
        assertNull(em.find(SessionInstance.class, instanceId)); // Should be cascade deleted
    }

    @Test
    @Transactional
    @DisplayName("Should load session instance with attendances")
    void shouldLoadSessionInstanceWithAttendances() {
        // Given
        Session session = createAndPersistSession("Attendance Test", Session.DayOfWeek.MONDAY);
        SessionInstance instance = SessionInstance.builder()
                .session(session)
                .sessionDate(LocalDate.now())
                .status(SessionInstance.SessionStatus.COMPLETED)
                .build();
        em.persist(instance);

        // Create students and attendances
        Major major = session.getSubjectsGroup().getSubject().getMajor();
        StudyYear year = session.getSubjectsGroup().getSubject().getStudyYear();

        for (int i = 0; i < 3; i++) {
            Student student = Student.builder()
                    .email("student" + i + "@test.com")
                    .name("Student " + i)
                    .major(major)
                    .studyYear(year)
                    .build();
            em.persist(student);

            Attendance attendance = Attendance.builder()
                    .student(student)
                    .sessionInstance(instance)
                    .status(Attendance.AttendanceStatus.PRESENT)
                    .build();
            em.persist(attendance);
        }
        em.flush();
        em.clear();

        // When
        SessionInstance loaded = em.find(SessionInstance.class, instance.getId());

        // Then
        assertNotNull(loaded);
        assertEquals(3, loaded.getAttendances().size());
        assertTrue(loaded.getAttendances().stream()
                .allMatch(a -> a.getStatus() == Attendance.AttendanceStatus.PRESENT));
    }

    @Test
    @Transactional
    @DisplayName("Should load session instance with incidents")
    void shouldLoadSessionInstanceWithIncidents() {
        // Given
        Session session = createAndPersistSession("Incident Test", Session.DayOfWeek.TUESDAY);
        SessionInstance instance = SessionInstance.builder()
                .session(session)
                .sessionDate(LocalDate.now())
                .build();
        em.persist(instance);

        // Create incidents
        Incident incident1 = Incident.builder()
                .sessionInstance(instance)
                .incidentType(Incident.IncidentType.DELAY)
                .description("Professor arrived 15 minutes late")
                .reportedBy("Class Representative")
                .build();
        em.persist(incident1);

        Incident incident2 = Incident.builder()
                .sessionInstance(instance)
                .incidentType(Incident.IncidentType.ROOM_CHANGE)
                .description("Moved to Room 203 due to projector issues")
                .reportedBy("Admin Staff")
                .build();
        em.persist(incident2);

        em.flush();
        em.clear();

        // When
        SessionInstance loaded = em.find(SessionInstance.class, instance.getId());

        // Then
        assertNotNull(loaded);
        assertEquals(2, loaded.getIncidents().size());
    }


}

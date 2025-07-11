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

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Session Entity Mapping Tests")
class SessionMappingTests {

    @Autowired
    private EntityManager em;

    // ========================================
    // Helper Methods
    // ========================================

    private SubjectsGroup createAndPersistSubjectsGroup(String groupName) {
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
                .name("Programming II")
                .major(major)
                .studyYear(year)
                .monthlyPrice(150)
                .build();
        em.persist(subject);

        // Create SubjectsGroup
        SubjectsGroup group = SubjectsGroup.builder()
                .name(groupName)
                .subject(subject)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(4))
                .maxCapacity(30)
                .build();
        em.persist(group);
        em.flush();

        return group;
    }

    // ========================================
    // Basic CRUD Tests
    // ========================================

    @Test
    @Transactional
    @DisplayName("Should handle minimal valid session")
    void shouldHandleMinimalValidSession() {
        // Given
        SubjectsGroup group = createAndPersistSubjectsGroup("Morning Group");

        Session session = Session.builder()
                .subjectsGroup(group)
                .dayOfWeek(Session.DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .durationMinutes(120)
                .build();

        // When
        em.persist(session);
        em.flush();

        // Then
        assertNotNull(session.getId());
        assertEquals(group, session.getSubjectsGroup());
        assertEquals(Session.DayOfWeek.MONDAY, session.getDayOfWeek());
        assertEquals(LocalTime.of(9, 0), session.getStartTime());
        assertEquals(120, session.getDurationMinutes());
        assertTrue(session.getIsActive()); // Default value
        assertNotNull(session.getSessionInstances());
        assertTrue(session.getSessionInstances().isEmpty());
    }

    @Test
    @Transactional
    @DisplayName("Should create session using builder with all fields")
    void shouldCreateSessionUsingBuilder() {
        // Given
        SubjectsGroup group = createAndPersistSubjectsGroup("Afternoon Group");

        // When
        Session session = Session.builder()
                .subjectsGroup(group)
                .dayOfWeek(Session.DayOfWeek.WEDNESDAY)
                .startTime(LocalTime.of(14, 30))
                .durationMinutes(90)
                .build();

        em.persist(session);
        em.flush();

        // Then
        assertNotNull(session.getId());
        assertEquals(Session.DayOfWeek.WEDNESDAY, session.getDayOfWeek());
        assertEquals(LocalTime.of(14, 30), session.getStartTime());
        assertEquals(90, session.getDurationMinutes());
        assertTrue(session.getIsActive());
    }

    @Test
    @Transactional
    @DisplayName("Should persist and retrieve session")
    void shouldPersistAndRetrieveSession() {
        // Given
        SubjectsGroup group = createAndPersistSubjectsGroup("Lab Group");

        Session session = Session.builder()
                .subjectsGroup(group)
                .dayOfWeek(Session.DayOfWeek.TUESDAY)
                .startTime(LocalTime.of(10, 15))
                .durationMinutes(180)
                .isActive(true)
                .build();

        em.persist(session);
        em.flush();
        em.clear();

        // When
        Session retrieved = em.find(Session.class, session.getId());

        // Then
        assertNotNull(retrieved);
        assertEquals(Session.DayOfWeek.TUESDAY, retrieved.getDayOfWeek());
        assertEquals(LocalTime.of(10, 15), retrieved.getStartTime());
        assertEquals(180, retrieved.getDurationMinutes());
        assertTrue(retrieved.getIsActive());
        assertEquals(group.getId(), retrieved.getSubjectsGroup().getId());
    }

    // ========================================
    // Validation Tests
    // ========================================

    @Test
    @DisplayName("Should reject null subjects group")
    void shouldRejectNullSubjectsGroup() {
        // Given
        Session session = Session.builder()
                .subjectsGroup(null)
                .dayOfWeek(Session.DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .durationMinutes(120)
                .build();

        // When & Then
        assertThrows(ConstraintViolationException.class, () -> {
            em.persist(session);
            em.flush();
        });
    }

    @Test
    @DisplayName("Should reject null day of week")
    void shouldRejectNullDayOfWeek() {
        // Given
        SubjectsGroup group = createAndPersistSubjectsGroup("Test Group");

        Session session = Session.builder()
                .subjectsGroup(group)
                .dayOfWeek(null)
                .startTime(LocalTime.of(9, 0))
                .durationMinutes(120)
                .build();

        // When & Then
        assertThrows(ConstraintViolationException.class, () -> {
            em.persist(session);
            em.flush();
        });
    }

    @Test
    @DisplayName("Should reject null start time")
    void shouldRejectNullStartTime() {
        // Given
        SubjectsGroup group = createAndPersistSubjectsGroup("Test Group");

        Session session = Session.builder()
                .subjectsGroup(group)
                .dayOfWeek(Session.DayOfWeek.THURSDAY)
                .startTime(null)
                .durationMinutes(120)
                .build();

        // When & Then
        assertThrows(ConstraintViolationException.class, () -> {
            em.persist(session);
            em.flush();
        });
    }

    @Test
    @DisplayName("Should reject null duration minutes")
    void shouldRejectNullDurationMinutes() {
        // Given
        SubjectsGroup group = createAndPersistSubjectsGroup("Test Group");

        Session session = Session.builder()
                .subjectsGroup(group)
                .dayOfWeek(Session.DayOfWeek.FRIDAY)
                .startTime(LocalTime.of(16, 0))
                .durationMinutes(null)
                .build();

        // When & Then
        assertThrows(ConstraintViolationException.class, () -> {
            em.persist(session);
            em.flush();
        });
    }

    @Test
    @DisplayName("Should reject zero duration minutes")
    void shouldRejectZeroDurationMinutes() {
        // Given
        SubjectsGroup group = createAndPersistSubjectsGroup("Test Group");

        Session session = Session.builder()
                .subjectsGroup(group)
                .dayOfWeek(Session.DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .durationMinutes(0)
                .build();

        // When & Then
        assertThrows(ConstraintViolationException.class, () -> {
            em.persist(session);
            em.flush();
        });
    }

    @Test
    @DisplayName("Should reject negative duration minutes")
    void shouldRejectNegativeDurationMinutes() {
        // Given
        SubjectsGroup group = createAndPersistSubjectsGroup("Test Group");

        Session session = Session.builder()
                .subjectsGroup(group)
                .dayOfWeek(Session.DayOfWeek.TUESDAY)
                .startTime(LocalTime.of(11, 0))
                .durationMinutes(-60)
                .build();

        // When & Then
        assertThrows(ConstraintViolationException.class, () -> {
            em.persist(session);
            em.flush();
        });
    }

    // ========================================
    // DayOfWeek Enum Tests
    // ========================================

    @Test
    @DisplayName("Should handle all days of week")
    void shouldHandleAllDaysOfWeek() {
        // Given
        SubjectsGroup group = createAndPersistSubjectsGroup("Weekly Group");

        for (Session.DayOfWeek day : Session.DayOfWeek.values()) {
            Session session = Session.builder()
                    .subjectsGroup(group)
                    .dayOfWeek(day)
                    .startTime(LocalTime.of(10, 0))
                    .durationMinutes(60)
                    .build();

            // When
            em.persist(session);
            em.flush();

            // Then
            assertNotNull(session.getId());
            assertEquals(day, session.getDayOfWeek());

            em.clear();
        }
    }

    // ========================================
    // Time Tests
    // ========================================

    @Test
    @DisplayName("Should handle various start times")
    void shouldHandleVariousStartTimes() {
        // Given
        SubjectsGroup group = createAndPersistSubjectsGroup("Time Test Group");

        LocalTime[] testTimes = {
                LocalTime.of(0, 0),      // Midnight
                LocalTime.of(6, 30),     // Early morning
                LocalTime.of(12, 0),     // Noon
                LocalTime.of(18, 45),    // Evening
                LocalTime.of(23, 59)     // Just before midnight
        };

        for (LocalTime time : testTimes) {
            Session session = Session.builder()
                    .subjectsGroup(group)
                    .dayOfWeek(Session.DayOfWeek.MONDAY)
                    .startTime(time)
                    .durationMinutes(60)
                    .build();

            // When
            em.persist(session);
            em.flush();

            // Then
            assertEquals(time, session.getStartTime());

            em.clear();
        }
    }

    @Test
    @DisplayName("Should handle various duration minutes")
    void shouldHandleVariousDurationMinutes() {
        // Given
        SubjectsGroup group = createAndPersistSubjectsGroup("Duration Test Group");

        Integer[] durations = {60, 90, 120, 180, 240, 480}; // From 1 minute to 8 hours

        for (Integer duration : durations) {
            Session session = Session.builder()
                    .subjectsGroup(group)
                    .dayOfWeek(Session.DayOfWeek.WEDNESDAY)
                    .startTime(LocalTime.of(9, 0))
                    .durationMinutes(duration)
                    .build();

            // When
            em.persist(session);
            em.flush();

            // Then
            assertEquals(duration, session.getDurationMinutes());

            em.clear();
        }
    }

    // ========================================
    // Default Values Tests
    // ========================================

    @Test
    @DisplayName("Should default is active to true")
    void shouldDefaultIsActiveToTrue() {
        // Given
        SubjectsGroup group = createAndPersistSubjectsGroup("Default Test Group");

        Session session = Session.builder()
                .subjectsGroup(group)
                .dayOfWeek(Session.DayOfWeek.THURSDAY)
                .startTime(LocalTime.of(15, 0))
                .durationMinutes(120)
                // isActive not specified
                .build();

        // When
        em.persist(session);
        em.flush();

        // Then
        assertTrue(session.getIsActive());
    }

    // ========================================
    // Relationship Tests
    // ========================================

    @Test
    @Transactional
    @DisplayName("Should cascade delete when subjects group is deleted")
    void shouldCascadeDeleteWhenSubjectsGroupDeleted() {
        // Given
        SubjectsGroup group = createAndPersistSubjectsGroup("To Delete Group");

        Session session = Session.builder()
                .subjectsGroup(group)
                .dayOfWeek(Session.DayOfWeek.FRIDAY)
                .startTime(LocalTime.of(17, 0))
                .durationMinutes(90)
                .build();
        em.persist(session);
        em.flush();

        Integer groupId = group.getId();
        Integer sessionId = session.getId();
        em.clear();

        // When - Delete subjects group
        SubjectsGroup toDelete = em.find(SubjectsGroup.class, groupId);
        em.remove(toDelete);
        em.flush();
        em.clear();

        // Then
        assertNull(em.find(SubjectsGroup.class, groupId));
        assertNull(em.find(Session.class, sessionId)); // Should be cascade deleted
    }

    @Test
    @Transactional
    @DisplayName("Should load session with session instances")
    void shouldLoadSessionWithSessionInstances() {
        // Given
        SubjectsGroup group = createAndPersistSubjectsGroup("Instance Test Group");

        Session session = Session.builder()
                .subjectsGroup(group)
                .dayOfWeek(Session.DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .durationMinutes(120)
                .build();
        em.persist(session);

        // Create session instances
        for (int i = 0; i < 3; i++) {
            SessionInstance instance = SessionInstance.builder()
                    .session(session)
                    .sessionDate(LocalDate.now().plusDays(i * 7)) // Weekly sessions
                    .status(SessionInstance.SessionStatus.SCHEDULED)
                    .build();
            em.persist(instance);
        }
        em.flush();
        em.clear();

        // When
        Session loaded = em.find(Session.class, session.getId());

        // Then
        assertNotNull(loaded);
        assertEquals(3, loaded.getSessionInstances().size());
        assertTrue(loaded.getSessionInstances().stream()
                .allMatch(si -> si.getStatus() == SessionInstance.SessionStatus.SCHEDULED));
    }


    // ========================================
    // Edge Cases
    // ========================================

    @Test
    @DisplayName("Should generate id automatically")
    void shouldGenerateIdAutomatically() {
        // Given
        SubjectsGroup group = createAndPersistSubjectsGroup("Auto ID Group");

        Session session = Session.builder()
                .subjectsGroup(group)
                .dayOfWeek(Session.DayOfWeek.SATURDAY)
                .startTime(LocalTime.of(9, 0))
                .durationMinutes(180)
                .build();

        // When
        assertNull(session.getId());
        em.persist(session);
        em.flush();

        // Then
        assertNotNull(session.getId());
        assertTrue(session.getId() > 0);
    }

    @Test
    @DisplayName("Should handle session updates")
    void shouldHandleSessionUpdates() {
        // Given
        SubjectsGroup group = createAndPersistSubjectsGroup("Update Test Group");

        Session session = Session.builder()
                .subjectsGroup(group)
                .dayOfWeek(Session.DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .durationMinutes(120)
                .isActive(true)
                .build();

        em.persist(session);
        em.flush();

        // When - Update various fields
        session.setStartTime(LocalTime.of(10, 30));
        session.setDurationMinutes(90);
        session.setIsActive(false);
        em.flush();
        em.clear();

        // Then
        Session updated = em.find(Session.class, session.getId());
        assertEquals(LocalTime.of(10, 30), updated.getStartTime());
        assertEquals(90, updated.getDurationMinutes());
        assertFalse(updated.getIsActive());
        // Day of week should remain unchanged
        assertEquals(Session.DayOfWeek.MONDAY, updated.getDayOfWeek());
    }

    @Test
    @DisplayName("Should handle maximum duration")
    void shouldHandleMaximumDuration() {
        // Given
        SubjectsGroup group = createAndPersistSubjectsGroup("Marathon Group");

        Session session = Session.builder()
                .subjectsGroup(group)
                .dayOfWeek(Session.DayOfWeek.SUNDAY)
                .startTime(LocalTime.of(0, 0))
                .durationMinutes(1440) // 24 hours
                .build();

        // When
        em.persist(session);
        em.flush();

        // Then
        assertEquals(1440, session.getDurationMinutes());
    }
}
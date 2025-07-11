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
@DisplayName("Attendance Entity Mapping Tests")
class AttendanceMappingTests {

    @Autowired
    private EntityManager em;

    // ========================================
    // Helper Methods
    // ========================================

    private Student createAndPersistStudent(String email, String name) {
        Major major = Major.builder()
                .name("Engineering")
                .build();
        em.persist(major);

        StudyYear year = StudyYear.builder()
                .name("3rd Year")
                .level(3)
                .build();
        em.persist(year);

        Student student = Student.builder()
                .email(email)
                .name(name)
                .major(major)
                .studyYear(year)
                .build();
        em.persist(student);
        em.flush();

        return student;
    }

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
                .name("Algorithms")
                .major(major)
                .studyYear(year)
                .monthlyPrice(120)
                .build();
        em.persist(subject);

        // Create SubjectsGroup
        SubjectsGroup group = SubjectsGroup.builder()
                .name("Morning Group")
                .subject(subject)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(4))
                .maxCapacity(25)
                .build();
        em.persist(group);

        // Create Session
        Session session = Session.builder()
                .subjectsGroup(group)
                .dayOfWeek(Session.DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .durationMinutes(90)
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
    @DisplayName("Should handle minimal valid attendance")
    void shouldHandleMinimalValidAttendance() {
        // Given
        Student student = createAndPersistStudent("alice@example.com", "Alice Johnson");
        SessionInstance sessionInstance = createAndPersistSessionInstance();

        Attendance attendance = Attendance.builder()
                .student(student)
                .sessionInstance(sessionInstance)
                .build();

        // When
        em.persist(attendance);
        em.flush();

        // Then
        assertNotNull(attendance.getId());
        assertEquals(student, attendance.getStudent());
        assertEquals(sessionInstance, attendance.getSessionInstance());
        assertEquals(Attendance.AttendanceStatus.ABSENT, attendance.getStatus()); // Default
        assertNull(attendance.getNotes());
        assertNotNull(attendance.getMarkedAt());
    }

    @Test
    @Transactional
    @DisplayName("Should create attendance with all fields")
    void shouldCreateAttendanceWithAllFields() {
        // Given
        Student student = createAndPersistStudent("bob@example.com", "Bob Smith");
        SessionInstance sessionInstance = createAndPersistSessionInstance();
        Instant markedTime = Instant.now().minus(30, ChronoUnit.MINUTES);

        Attendance attendance = Attendance.builder()
                .student(student)
                .sessionInstance(sessionInstance)
                .status(Attendance.AttendanceStatus.LATE)
                .notes("Arrived 10 minutes late due to traffic")
                .markedAt(markedTime)
                .build();

        // When
        em.persist(attendance);
        em.flush();

        // Then
        assertNotNull(attendance.getId());
        assertEquals(Attendance.AttendanceStatus.LATE, attendance.getStatus());
        assertEquals("Arrived 10 minutes late due to traffic", attendance.getNotes());
        assertEquals(markedTime, attendance.getMarkedAt());
    }

    @Test
    @Transactional
    @DisplayName("Should persist and retrieve attendance")
    void shouldPersistAndRetrieveAttendance() {
        // Given
        Student student = createAndPersistStudent("charlie@example.com", "Charlie Brown");
        SessionInstance sessionInstance = createAndPersistSessionInstance();

        Attendance attendance = Attendance.builder()
                .student(student)
                .sessionInstance(sessionInstance)
                .status(Attendance.AttendanceStatus.PRESENT)
                .notes("On time")
                .build();

        em.persist(attendance);
        em.flush();
        em.clear();

        // When
        Attendance retrieved = em.find(Attendance.class, attendance.getId());

        // Then
        assertNotNull(retrieved);
        assertEquals(Attendance.AttendanceStatus.PRESENT, retrieved.getStatus());
        assertEquals("On time", retrieved.getNotes());
        assertEquals(student.getId(), retrieved.getStudent().getId());
        assertEquals(sessionInstance.getId(), retrieved.getSessionInstance().getId());
    }

    // ========================================
    // Validation Tests
    // ========================================

    @Test
    @DisplayName("Should reject null student")
    void shouldRejectNullStudent() {
        // Given
        SessionInstance sessionInstance = createAndPersistSessionInstance();

        Attendance attendance = Attendance.builder()
                .student(null)
                .sessionInstance(sessionInstance)
                .status(Attendance.AttendanceStatus.PRESENT)
                .build();

        // When & Then
        assertThrows(ConstraintViolationException.class, () -> {
            em.persist(attendance);
            em.flush();
        });
    }

    @Test
    @DisplayName("Should reject null session instance")
    void shouldRejectNullSessionInstance() {
        // Given
        Student student = createAndPersistStudent("david@example.com", "David Wilson");

        Attendance attendance = Attendance.builder()
                .student(student)
                .sessionInstance(null)
                .status(Attendance.AttendanceStatus.PRESENT)
                .build();

        // When & Then
        assertThrows(ConstraintViolationException.class, () -> {
            em.persist(attendance);
            em.flush();
        });
    }

    @Test
    @DisplayName("Should reject null status")
    void shouldRejectNullStatus() {
        // Given
        Student student = createAndPersistStudent("eve@example.com", "Eve Davis");
        SessionInstance sessionInstance = createAndPersistSessionInstance();

        Attendance attendance = Attendance.builder()
                .student(student)
                .sessionInstance(sessionInstance)
                .status(null)
                .build();

        // When & Then
        assertThrows(ConstraintViolationException.class, () -> {
            em.persist(attendance);
            em.flush();
        });
    }

    // ========================================
    // Unique Constraint Tests
    // ========================================

    @Test
    @DisplayName("Should reject duplicate student-session combination")
    void shouldRejectDuplicateStudentSessionCombination() {
        // Given
        Student student = createAndPersistStudent("frank@example.com", "Frank Miller");
        SessionInstance sessionInstance = createAndPersistSessionInstance();

        Attendance first = Attendance.builder()
                .student(student)
                .sessionInstance(sessionInstance)
                .status(Attendance.AttendanceStatus.PRESENT)
                .build();
        em.persist(first);
        em.flush();

        Attendance duplicate = Attendance.builder()
                .student(student)
                .sessionInstance(sessionInstance)
                .status(Attendance.AttendanceStatus.ABSENT)
                .build();

        // When & Then
        assertThrows(PersistenceException.class, () -> {
            em.persist(duplicate);
            em.flush();
        });
    }

    @Test
    @DisplayName("Should allow same student in different sessions")
    void shouldAllowSameStudentInDifferentSessions() {
        // Given
        Student student = createAndPersistStudent("grace@example.com", "Grace Lee");
        SessionInstance instance1 = createAndPersistSessionInstance();

        // Create another session instance
        SessionInstance instance2 = SessionInstance.builder()
                .session(instance1.getSession())
                .sessionDate(LocalDate.now().plusDays(7))
                .status(SessionInstance.SessionStatus.SCHEDULED)
                .build();
        em.persist(instance2);
        em.flush();

        Attendance attendance1 = Attendance.builder()
                .student(student)
                .sessionInstance(instance1)
                .status(Attendance.AttendanceStatus.PRESENT)
                .build();

        Attendance attendance2 = Attendance.builder()
                .student(student)
                .sessionInstance(instance2)
                .status(Attendance.AttendanceStatus.PRESENT)
                .build();

        // When
        em.persist(attendance1);
        em.persist(attendance2);
        em.flush();

        // Then
        assertNotNull(attendance1.getId());
        assertNotNull(attendance2.getId());
        assertNotEquals(attendance1.getId(), attendance2.getId());
    }

    @Test
    @DisplayName("Should allow different students in same session")
    void shouldAllowDifferentStudentsInSameSession() {
        // Given
        Student student1 = createAndPersistStudent("henry@example.com", "Henry Taylor");
        Student student2 = createAndPersistStudent("iris@example.com", "Iris Martinez");
        SessionInstance sessionInstance = createAndPersistSessionInstance();

        Attendance attendance1 = Attendance.builder()
                .student(student1)
                .sessionInstance(sessionInstance)
                .status(Attendance.AttendanceStatus.PRESENT)
                .build();

        Attendance attendance2 = Attendance.builder()
                .student(student2)
                .sessionInstance(sessionInstance)
                .status(Attendance.AttendanceStatus.LATE)
                .build();

        // When
        em.persist(attendance1);
        em.persist(attendance2);
        em.flush();

        // Then
        assertNotNull(attendance1.getId());
        assertNotNull(attendance2.getId());
        assertNotEquals(attendance1.getId(), attendance2.getId());
    }

    // ========================================
    // AttendanceStatus Enum Tests
    // ========================================

    @Test
    @DisplayName("Should default status to ABSENT")
    void shouldDefaultStatusToAbsent() {
        // Given
        Student student = createAndPersistStudent("jack@example.com", "Jack Robinson");
        SessionInstance sessionInstance = createAndPersistSessionInstance();

        Attendance attendance = Attendance.builder()
                .student(student)
                .sessionInstance(sessionInstance)
                // No status specified - will use default
                .build();

        // When
        em.persist(attendance);
        em.flush();

        // Then
        assertEquals(Attendance.AttendanceStatus.ABSENT, attendance.getStatus());
    }

    @Test
    @DisplayName("Should handle all attendance status values")
    void shouldHandleAllAttendanceStatusValues() {
        // Given
        Student student = createAndPersistStudent("karen@example.com", "Karen White");
        SessionInstance sessionInstance = createAndPersistSessionInstance();

        for (Attendance.AttendanceStatus status : Attendance.AttendanceStatus.values()) {
            // Create new session instance for each test to avoid unique constraint
            SessionInstance newInstance = SessionInstance.builder()
                    .session(sessionInstance.getSession())
                    .sessionDate(LocalDate.now().plusDays(status.ordinal()))
                    .status(SessionInstance.SessionStatus.SCHEDULED)
                    .build();
            em.persist(newInstance);

            Attendance attendance = Attendance.builder()
                    .student(student)
                    .sessionInstance(newInstance)
                    .status(status)
                    .build();

            // When
            em.persist(attendance);
            em.flush();

            // Then
            assertEquals(status, attendance.getStatus());
            em.clear();
        }
    }

    // ========================================
    // Optional Fields Tests
    // ========================================

    @Test
    @DisplayName("Should allow null notes")
    void shouldAllowNullNotes() {
        // Given
        Student student = createAndPersistStudent("leo@example.com", "Leo Anderson");
        SessionInstance sessionInstance = createAndPersistSessionInstance();

        Attendance attendance = Attendance.builder()
                .student(student)
                .sessionInstance(sessionInstance)
                .status(Attendance.AttendanceStatus.PRESENT)
                .notes(null)
                .build();

        // When
        em.persist(attendance);
        em.flush();

        // Then
        assertNotNull(attendance.getId());
        assertNull(attendance.getNotes());
    }

    @Test
    @DisplayName("Should handle long notes")
    void shouldHandleLongNotes() {
        // Given
        Student student = createAndPersistStudent("mary@example.com", "Mary Thompson");
        SessionInstance sessionInstance = createAndPersistSessionInstance();
        String longNotes = "This is a very detailed attendance note. ".repeat(100);

        Attendance attendance = Attendance.builder()
                .student(student)
                .sessionInstance(sessionInstance)
                .status(Attendance.AttendanceStatus.EXCUSED)
                .notes(longNotes)
                .build();

        // When
        em.persist(attendance);
        em.flush();
        em.clear();

        // Then
        Attendance retrieved = em.find(Attendance.class, attendance.getId());
        assertNotNull(retrieved);
        assertEquals(longNotes, retrieved.getNotes());
    }

    // ========================================
    // Timestamp Tests
    // ========================================

    @Test
    @DisplayName("Should set marked at automatically")
    void shouldSetMarkedAtAutomatically() {
        // Given
        Student student = createAndPersistStudent("nathan@example.com", "Nathan Green");
        SessionInstance sessionInstance = createAndPersistSessionInstance();
        Instant before = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        Attendance attendance = Attendance.builder()
                .student(student)
                .sessionInstance(sessionInstance)
                .status(Attendance.AttendanceStatus.PRESENT)
                // No markedAt specified
                .build();

        // When
        em.persist(attendance);
        em.flush();

        // Then
        assertNotNull(attendance.getMarkedAt());
        Instant markedAt = attendance.getMarkedAt().truncatedTo(ChronoUnit.SECONDS);
        assertTrue(markedAt.equals(before) || markedAt.isAfter(before));
    }

    @Test
    @DisplayName("Should respect manually set marked at")
    void shouldRespectManuallySetMarkedAt() {
        // Given
        Student student = createAndPersistStudent("olivia@example.com", "Olivia Clark");
        SessionInstance sessionInstance = createAndPersistSessionInstance();
        Instant customTime = Instant.now().minus(2, ChronoUnit.HOURS);

        Attendance attendance = Attendance.builder()
                .student(student)
                .sessionInstance(sessionInstance)
                .status(Attendance.AttendanceStatus.LATE)
                .markedAt(customTime)
                .build();

        // When
        em.persist(attendance);
        em.flush();

        // Then
        assertEquals(customTime, attendance.getMarkedAt());
    }

    // ========================================
    // Relationship Tests
    // ========================================

    @Test
    @Transactional
    @DisplayName("Should cascade delete when student is deleted")
    void shouldCascadeDeleteWhenStudentDeleted() {
        // Given
        Student student = createAndPersistStudent("peter@example.com", "Peter Harris");
        SessionInstance sessionInstance = createAndPersistSessionInstance();

        Attendance attendance = Attendance.builder()
                .student(student)
                .sessionInstance(sessionInstance)
                .status(Attendance.AttendanceStatus.PRESENT)
                .build();
        em.persist(attendance);
        em.flush();

        Integer studentId = student.getId();
        Integer attendanceId = attendance.getId();
        Integer sessionInstanceId = sessionInstance.getId();
        em.clear();

        // When - Delete student
        Student toDelete = em.find(Student.class, studentId);
        em.remove(toDelete);
        em.flush();
        em.clear();

        // Then
        assertNull(em.find(Student.class, studentId));
        assertNull(em.find(Attendance.class, attendanceId)); // Should be cascade deleted
        assertNotNull(em.find(SessionInstance.class, sessionInstanceId)); // Session instance should remain
    }

    @Test
    @Transactional
    @DisplayName("Should cascade delete when session instance is deleted")
    void shouldCascadeDeleteWhenSessionInstanceDeleted() {
        // Given
        Student student = createAndPersistStudent("quinn@example.com", "Quinn Lopez");
        SessionInstance sessionInstance = createAndPersistSessionInstance();

        Attendance attendance = Attendance.builder()
                .student(student)
                .sessionInstance(sessionInstance)
                .status(Attendance.AttendanceStatus.EXCUSED)
                .build();
        em.persist(attendance);
        em.flush();

        Integer studentId = student.getId();
        Integer attendanceId = attendance.getId();
        Integer sessionInstanceId = sessionInstance.getId();
        em.clear();

        // When - Delete session instance
        SessionInstance toDelete = em.find(SessionInstance.class, sessionInstanceId);
        em.remove(toDelete);
        em.flush();
        em.clear();

        // Then
        assertNull(em.find(SessionInstance.class, sessionInstanceId));
        assertNull(em.find(Attendance.class, attendanceId)); // Should be cascade deleted
        assertNotNull(em.find(Student.class, studentId)); // Student should remain
    }

    @Test
    @Transactional
    @DisplayName("Should handle multiple attendances for same student")
    void shouldHandleMultipleAttendancesForSameStudent() {
        // Given
        Student student = createAndPersistStudent("rachel@example.com", "Rachel Brown");
        SessionInstance instance1 = createAndPersistSessionInstance();

        // Create more session instances for the same session
        Session session = instance1.getSession();
        SessionInstance instance2 = SessionInstance.builder()
                .session(session)
                .sessionDate(LocalDate.now().plusDays(7))
                .status(SessionInstance.SessionStatus.COMPLETED)
                .build();
        em.persist(instance2);

        SessionInstance instance3 = SessionInstance.builder()
                .session(session)
                .sessionDate(LocalDate.now().plusDays(14))
                .status(SessionInstance.SessionStatus.SCHEDULED)
                .build();
        em.persist(instance3);

        // Create attendances
        Attendance attendance1 = Attendance.builder()
                .student(student)
                .sessionInstance(instance1)
                .status(Attendance.AttendanceStatus.PRESENT)
                .build();

        Attendance attendance2 = Attendance.builder()
                .student(student)
                .sessionInstance(instance2)
                .status(Attendance.AttendanceStatus.LATE)
                .build();

        Attendance attendance3 = Attendance.builder()
                .student(student)
                .sessionInstance(instance3)
                .status(Attendance.AttendanceStatus.ABSENT)
                .build();

        // When
        em.persist(attendance1);
        em.persist(attendance2);
        em.persist(attendance3);
        em.flush();
        em.clear();

        // Then
        Student loaded = em.find(Student.class, student.getId());
        assertNotNull(loaded);
        assertEquals(3, loaded.getAttendances().size());
    }

    // ========================================
    // Business Logic Tests
    // ========================================

    @Test
    @DisplayName("Should update attendance status")
    void shouldUpdateAttendanceStatus() {
        // Given
        Student student = createAndPersistStudent("sam@example.com", "Sam Wilson");
        SessionInstance sessionInstance = createAndPersistSessionInstance();

        Attendance attendance = Attendance.builder()
                .student(student)
                .sessionInstance(sessionInstance)
                .status(Attendance.AttendanceStatus.ABSENT)
                .build();

        em.persist(attendance);
        em.flush();

        // When - Update status from ABSENT to LATE
        attendance.setStatus(Attendance.AttendanceStatus.LATE);
        attendance.setNotes("Arrived late but attended the class");
        em.flush();
        em.clear();

        // Then
        Attendance updated = em.find(Attendance.class, attendance.getId());
        assertEquals(Attendance.AttendanceStatus.LATE, updated.getStatus());
        assertEquals("Arrived late but attended the class", updated.getNotes());
    }

    @Test
    @DisplayName("Should handle attendance for different session types")
    void shouldHandleAttendanceForDifferentSessionTypes() {
        // Given
        Student student = createAndPersistStudent("tina@example.com", "Tina Garcia");

        // Create different types of sessions
        SessionInstance regularSession = createAndPersistSessionInstance();
        regularSession.setNotes("Regular class session");

        SessionInstance examSession = SessionInstance.builder()
                .session(regularSession.getSession())
                .sessionDate(LocalDate.now().plusDays(30))
                .status(SessionInstance.SessionStatus.SCHEDULED)
                .notes("Midterm exam")
                .build();
        em.persist(examSession);

        // Create attendances
        Attendance regularAttendance = Attendance.builder()
                .student(student)
                .sessionInstance(regularSession)
                .status(Attendance.AttendanceStatus.PRESENT)
                .notes("Regular attendance")
                .build();

        Attendance examAttendance = Attendance.builder()
                .student(student)
                .sessionInstance(examSession)
                .status(Attendance.AttendanceStatus.PRESENT)
                .notes("Took the exam")
                .build();

        // When
        em.persist(regularAttendance);
        em.persist(examAttendance);
        em.flush();

        // Then
        assertNotNull(regularAttendance.getId());
        assertNotNull(examAttendance.getId());
        assertEquals("Regular attendance", regularAttendance.getNotes());
        assertEquals("Took the exam", examAttendance.getNotes());
    }

    @Test
    @DisplayName("Should handle special characters in notes")
    void shouldHandleSpecialCharactersInNotes() {
        // Given
        Student student = createAndPersistStudent("victor@example.com", "Victor Chen");
        SessionInstance sessionInstance = createAndPersistSessionInstance();

        Attendance attendance = Attendance.builder()
                .student(student)
                .sessionInstance(sessionInstance)
                .status(Attendance.AttendanceStatus.EXCUSED)
                .notes("Special chars: áéíóú ñ 中文 日本語 😊 @#$% & symbols")
                .build();

        // When
        em.persist(attendance);
        em.flush();
        em.clear();

        // Then
        Attendance retrieved = em.find(Attendance.class, attendance.getId());
        assertNotNull(retrieved);
        assertEquals("Special chars: áéíóú ñ 中文 日本語 😊 @#$% & symbols", retrieved.getNotes());
    }

    @Test
    @DisplayName("Should generate id automatically")
    void shouldGenerateIdAutomatically() {
        // Given
        Student student = createAndPersistStudent("wendy@example.com", "Wendy Parker");
        SessionInstance sessionInstance = createAndPersistSessionInstance();

        Attendance attendance = Attendance.builder()
                .student(student)
                .sessionInstance(sessionInstance)
                .status(Attendance.AttendanceStatus.PRESENT)
                .build();

        // When
        assertNull(attendance.getId());
        em.persist(attendance);
        em.flush();

        // Then
        assertNotNull(attendance.getId());
        assertTrue(attendance.getId() > 0);
    }

    @Test
    @DisplayName("Should handle attendance statistics scenario")
    void shouldHandleAttendanceStatisticsScenario() {
        // Given - Create a session with multiple instances and students
        SessionInstance instance = createAndPersistSessionInstance();

        // Create multiple students
        Student student1 = createAndPersistStudent("stat1@example.com", "Student 1");
        Student student2 = createAndPersistStudent("stat2@example.com", "Student 2");
        Student student3 = createAndPersistStudent("stat3@example.com", "Student 3");

        // Create various attendance records
        em.persist(Attendance.builder()
                .student(student1)
                .sessionInstance(instance)
                .status(Attendance.AttendanceStatus.PRESENT)
                .build());

        em.persist(Attendance.builder()
                .student(student2)
                .sessionInstance(instance)
                .status(Attendance.AttendanceStatus.LATE)
                .notes("10 minutes late")
                .build());

        em.persist(Attendance.builder()
                .student(student3)
                .sessionInstance(instance)
                .status(Attendance.AttendanceStatus.ABSENT)
                .build());

        em.flush();
        em.clear();

        // When
        SessionInstance loaded = em.find(SessionInstance.class, instance.getId());

        // Then
        assertNotNull(loaded);
        assertEquals(3, loaded.getAttendances().size());

        // Verify attendance distribution
        long presentCount = loaded.getAttendances().stream()
                .filter(a -> a.getStatus() == Attendance.AttendanceStatus.PRESENT)
                .count();
        long lateCount = loaded.getAttendances().stream()
                .filter(a -> a.getStatus() == Attendance.AttendanceStatus.LATE)
                .count();
        long absentCount = loaded.getAttendances().stream()
                .filter(a -> a.getStatus() == Attendance.AttendanceStatus.ABSENT)
                .count();

        assertEquals(1, presentCount);
        assertEquals(1, lateCount);
        assertEquals(1, absentCount);
    }
}

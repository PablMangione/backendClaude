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
@DisplayName("Student Entity Mapping Tests")
class StudentMappingTests {

    @Autowired
    private EntityManager em;

    // ========================================
    // Basic CRUD & Defaults
    // ========================================

    @Test
    @Transactional
    @DisplayName("Should handle minimal valid student")
    void shouldHandleMinimalValidStudent() {
        // Given - crear dependencias
        Major major = Major.builder().name("Engineering").build();
        em.persist(major);

        StudyYear year = StudyYear.builder().name("1st Year").level(1).build();
        em.persist(year);
        em.flush();

        Student student = Student.builder()
                .email("alice@example.com")
                .name("Alice")
                .major(major)
                .studyYear(year)
                .build();

        // When
        em.persist(student);
        em.flush();

        // Then
        assertNotNull(student.getId());
        assertTrue(student.getId() > 0);
        assertEquals("alice@example.com", student.getEmail());
        assertEquals("Alice", student.getName());
        assertTrue(student.getIsActive(), "isActive debe ser true por defecto");
        assertNull(student.getPhone(), "phone debe ser null cuando no se especifica");
        assertNotNull(student.getRegistrations());
        assertTrue(student.getRegistrations().isEmpty());
        assertNotNull(student.getAttendances());
        assertTrue(student.getAttendances().isEmpty());
    }

    @Test
    @Transactional
    @DisplayName("Should create student using builder with optional fields")
    void shouldCreateStudentUsingBuilder() {
        // Given
        Major major = Major.builder().name("Arts").build();
        em.persist(major);

        StudyYear year = StudyYear.builder().name("2nd Year").level(2).build();
        em.persist(year);
        em.flush();

        // When
        Student student = Student.builder()
                .email("bob@example.com")
                .name("Bob")
                .phone("+34123456789")
                .major(major)
                .studyYear(year)
                .isActive(false)
                .build();
        em.persist(student);
        em.flush();

        // Then
        assertNotNull(student.getId());
        assertEquals("bob@example.com", student.getEmail());
        assertEquals("Bob", student.getName());
        assertEquals("+34123456789", student.getPhone());
        assertFalse(student.getIsActive(), "isActive debe reflejar el valor seteado");
    }

    @Test
    @Transactional
    @DisplayName("Should persist and retrieve student")
    void shouldPersistAndRetrieveStudent() {
        // Given
        Major major = Major.builder().name("Computer Science").build();
        StudyYear year = StudyYear.builder().name("3rd Year").level(3).build();
        em.persist(major);
        em.persist(year);

        Student student = Student.builder()
                .email("charlie@example.com")
                .name("Charlie Brown")
                .phone("555-1234")
                .major(major)
                .studyYear(year)
                .isActive(true)
                .build();

        em.persist(student);
        em.flush();
        em.clear();

        // When
        Student retrieved = em.find(Student.class, student.getId());

        // Then
        assertNotNull(retrieved);
        assertEquals("charlie@example.com", retrieved.getEmail());
        assertEquals("Charlie Brown", retrieved.getName());
        assertEquals("555-1234", retrieved.getPhone());
        assertTrue(retrieved.getIsActive());
        assertEquals(major.getId(), retrieved.getMajor().getId());
        assertEquals(year.getId(), retrieved.getStudyYear().getId());
    }

    @Test
    @Transactional
    @DisplayName("Should set createdAt automatically")
    void shouldSetCreatedAtAutomatically() {
        // Given
        Major major = Major.builder().name("Medicine").build();
        em.persist(major);
        StudyYear year = StudyYear.builder().name("3rd Year").level(3).build();
        em.persist(year);
        em.flush();

        Instant before = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        Student student = Student.builder()
                .email("carol@example.com")
                .name("Carol")
                .major(major)
                .studyYear(year)
                .build();

        // When
        em.persist(student);
        em.flush();

        // Then
        assertNotNull(student.getCreatedAt());
        Instant createdAt = student.getCreatedAt().truncatedTo(ChronoUnit.SECONDS);
        assertTrue(createdAt.equals(before) || createdAt.isAfter(before));
    }

    // ========================================
    // Constraint Validations
    // ========================================

    @Test
    @Transactional
    @DisplayName("Should reject null email")
    void shouldRejectNullEmail() {
        Major major = Major.builder().name("Law").build();
        em.persist(major);
        StudyYear year = StudyYear.builder().name("4th Year").level(4).build();
        em.persist(year);
        em.flush();

        Student student = Student.builder()
                .email(null)
                .name("Dave")
                .major(major)
                .studyYear(year)
                .build();

        assertThrows(ConstraintViolationException.class, () -> {
            em.persist(student);
            em.flush();
        });
    }

    @Test
    @Transactional
    @DisplayName("Should reject invalid email format")
    void shouldRejectInvalidEmail() {
        Major major = Major.builder().name("Business").build();
        em.persist(major);
        StudyYear year = StudyYear.builder().name("5th Year").level(5).build();
        em.persist(year);
        em.flush();

        Student student = Student.builder()
                .email("not-an-email")
                .name("Eve")
                .major(major)
                .studyYear(year)
                .build();

        assertThrows(ConstraintViolationException.class, () -> {
            em.persist(student);
            em.flush();
        });
    }

    @Test
    @Transactional
    @DisplayName("Should reject null name")
    void shouldRejectNullName() {
        Major major = Major.builder().name("Philosophy").build();
        em.persist(major);
        StudyYear year = StudyYear.builder().name("6th Year").level(6).build();
        em.persist(year);
        em.flush();

        Student student = Student.builder()
                .email("frank@example.com")
                .name(null)
                .major(major)
                .studyYear(year)
                .build();

        assertThrows(ConstraintViolationException.class, () -> {
            em.persist(student);
            em.flush();
        });
    }

    @Test
    @Transactional
    @DisplayName("Should reject null major")
    void shouldRejectNullMajor() {
        StudyYear year = StudyYear.builder().name("7th Year").level(7).build();
        em.persist(year);
        em.flush();

        Student student = Student.builder()
                .email("gina@example.com")
                .name("Gina")
                .major(null)
                .studyYear(year)
                .build();

        assertThrows(ConstraintViolationException.class, () -> {
            em.persist(student);
            em.flush();
        });
    }

    @Test
    @Transactional
    @DisplayName("Should reject null studyYear")
    void shouldRejectNullStudyYear() {
        Major major = Major.builder().name("Science").build();
        em.persist(major);
        em.flush();

        Student student = Student.builder()
                .email("henry@example.com")
                .name("Henry")
                .major(major)
                .studyYear(null)
                .build();

        assertThrows(ConstraintViolationException.class, () -> {
            em.persist(student);
            em.flush();
        });
    }

    @Test
    @Transactional
    @DisplayName("Should reject duplicate email")
    void shouldRejectDuplicateEmail() {
        Major major = Major.builder().name("Music").build();
        em.persist(major);
        StudyYear year = StudyYear.builder().name("8th Year").level(8).build();
        em.persist(year);
        em.flush();

        Student first = Student.builder()
                .email("ivy@example.com")
                .name("Ivy")
                .major(major)
                .studyYear(year)
                .build();
        em.persist(first);
        em.flush();

        Student second = Student.builder()
                .email("ivy@example.com")
                .name("IvyClone")
                .major(major)
                .studyYear(year)
                .build();

        assertThrows(PersistenceException.class, () -> {
            em.persist(second);
            em.flush();
        });
    }

    @Test
    @Transactional
    @DisplayName("Should reject too long name")
    void shouldRejectTooLongName() {
        Major major = Major.builder().name("Design").build();
        em.persist(major);
        StudyYear year = StudyYear.builder().name("9th Year").level(9).build();
        em.persist(year);
        em.flush();

        String longName = "N".repeat(256);

        Student student = Student.builder()
                .email("toolong@example.com")
                .name(longName)
                .major(major)
                .studyYear(year)
                .build();

        assertThrows(ConstraintViolationException.class, () -> {
            em.persist(student);
            em.flush();
        });
    }

    @Test
    @Transactional
    @DisplayName("Should reject too long email")
    void shouldRejectTooLongEmail() {
        Major major = Major.builder().name("Art").build();
        em.persist(major);
        StudyYear year = StudyYear.builder().name("1st").level(1).build();
        em.persist(year);
        em.flush();

        String longEmail = "e".repeat(250) + "@example.com";

        Student student = Student.builder()
                .email(longEmail)
                .name("Test")
                .major(major)
                .studyYear(year)
                .build();

        assertThrows(ConstraintViolationException.class, () -> {
            em.persist(student);
            em.flush();
        });
    }

    @Test
    @Transactional
    @DisplayName("Should reject too long phone")
    void shouldRejectTooLongPhone() {
        Major major = Major.builder().name("Physics").build();
        em.persist(major);
        StudyYear year = StudyYear.builder().name("2nd").level(2).build();
        em.persist(year);
        em.flush();

        String longPhone = "1".repeat(21);

        Student student = Student.builder()
                .email("phone@example.com")
                .name("Phone Test")
                .phone(longPhone)
                .major(major)
                .studyYear(year)
                .build();

        assertThrows(ConstraintViolationException.class, () -> {
            em.persist(student);
            em.flush();
        });
    }

    // ========================================
    // Optional Fields & Edge Cases
    // ========================================

    @Test
    @Transactional
    @DisplayName("Should allow null phone")
    void shouldAllowNullPhone() {
        // Given
        Major major = Major.builder().name("Chemistry").build();
        StudyYear year = StudyYear.builder().name("1st").level(1).build();
        em.persist(major);
        em.persist(year);

        Student student = Student.builder()
                .email("nophone@example.com")
                .name("No Phone")
                .phone(null)
                .major(major)
                .studyYear(year)
                .build();

        // When
        em.persist(student);
        em.flush();

        // Then
        assertNotNull(student.getId());
        assertNull(student.getPhone());
    }

    @Test
    @Transactional
    @DisplayName("Should handle maximum length fields")
    void shouldHandleMaximumLengthFields() {
        // Given
        Major major = Major.builder().name("Test").build();
        StudyYear year = StudyYear.builder().name("1st").level(1).build();
        em.persist(major);
        em.persist(year);

        String maxName = "N".repeat(255);
        String maxPhone = "1".repeat(20);

        Student student = Student.builder()
                .email("test@test.com")
                .name(maxName)
                .phone(maxPhone)
                .major(major)
                .studyYear(year)
                .build();

        // When
        em.persist(student);
        em.flush();

        // Then
        assertNotNull(student.getId());
        assertEquals(maxName, student.getName());
        assertEquals(maxPhone, student.getPhone());
    }

    @Test
    @Transactional
    @DisplayName("Should handle special characters in fields")
    void shouldHandleSpecialCharactersInFields() {
        // Given
        Major major = Major.builder().name("International").build();
        StudyYear year = StudyYear.builder().name("1st").level(1).build();
        em.persist(major);
        em.persist(year);

        Student student = Student.builder()
                .email("josé.garcía@example.com")
                .name("José García-O'Brien")
                .phone("+34 (91) 123-4567")
                .major(major)
                .studyYear(year)
                .build();

        // When
        em.persist(student);
        em.flush();
        em.clear();

        // Then
        Student retrieved = em.find(Student.class, student.getId());
        assertNotNull(retrieved);
        assertEquals("José García-O'Brien", retrieved.getName());
        assertEquals("josé.garcía@example.com", retrieved.getEmail());
        assertEquals("+34 (91) 123-4567", retrieved.getPhone());
    }

    @Test
    @Transactional
    @DisplayName("Should generate id automatically")
    void shouldGenerateIdAutomatically() {
        // Given
        Major major = Major.builder().name("Math").build();
        StudyYear year = StudyYear.builder().name("1st").level(1).build();
        em.persist(major);
        em.persist(year);

        Student student = Student.builder()
                .email("autoid@example.com")
                .name("Auto ID")
                .major(major)
                .studyYear(year)
                .build();

        // When
        assertNull(student.getId());
        em.persist(student);
        em.flush();

        // Then
        assertNotNull(student.getId());
        assertTrue(student.getId() > 0);
    }

    // ========================================
    // Relationship Tests
    // ========================================

    @Test
    @Transactional
    @DisplayName("Should load student with registrations")
    void shouldLoadStudentWithRegistrations() {
        // Given
        Major major = Major.builder().name("History").build();
        StudyYear year = StudyYear.builder().name("2nd").level(2).build();
        em.persist(major);
        em.persist(year);

        Student student = Student.builder()
                .email("registrations@example.com")
                .name("Student With Registrations")
                .major(major)
                .studyYear(year)
                .build();
        em.persist(student);

        // Create subject and group
        Subject subject = Subject.builder()
                .name("World History")
                .major(major)
                .studyYear(year)
                .monthlyPrice(100)
                .build();
        em.persist(subject);

        SubjectsGroup group = SubjectsGroup.builder()
                .name("Group A")
                .subject(subject)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(4))
                .maxCapacity(30)
                .build();
        em.persist(group);

        // Create registration
        RegistrationId regId = RegistrationId.builder()
                .studentId(student.getId())
                .groupId(group.getId())
                .build();

        Registration registration = Registration.builder()
                .id(regId)
                .student(student)
                .group(group)
                .status(Registration.RegistrationStatus.ACTIVE)
                .build();
        em.persist(registration);
        em.flush();
        em.clear();

        // When
        Student loaded = em.find(Student.class, student.getId());

        // Then
        assertNotNull(loaded);
        assertEquals(1, loaded.getRegistrations().size());
        Registration loadedReg = loaded.getRegistrations().iterator().next();
        assertEquals(Registration.RegistrationStatus.ACTIVE, loadedReg.getStatus());
    }

    @Test
    @Transactional
    @DisplayName("Should cascade delete registrations when student is deleted")
    void shouldCascadeDeleteRegistrations() {
        // Given
        Major major = Major.builder().name("Geography").build();
        StudyYear year = StudyYear.builder().name("1st").level(1).build();
        em.persist(major);
        em.persist(year);

        Student student = Student.builder()
                .email("cascade@example.com")
                .name("Cascade Test")
                .major(major)
                .studyYear(year)
                .build();
        em.persist(student);

        Subject subject = Subject.builder()
                .name("Physical Geography")
                .major(major)
                .studyYear(year)
                .monthlyPrice(90)
                .build();
        em.persist(subject);

        SubjectsGroup group = SubjectsGroup.builder()
                .name("Group B")
                .subject(subject)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(3))
                .build();
        em.persist(group);

        RegistrationId regId = RegistrationId.builder()
                .studentId(student.getId())
                .groupId(group.getId())
                .build();

        Registration registration = Registration.builder()
                .id(regId)
                .student(student)
                .group(group)
                .build();
        em.persist(registration);
        em.flush();

        Integer studentId = student.getId();
        Integer groupId = group.getId();
        em.clear();

        // When
        Student toDelete = em.find(Student.class, studentId);
        em.remove(toDelete);
        em.flush();
        em.clear();

        // Then
        assertNull(em.find(Student.class, studentId));
        // Registration should be deleted due to cascade
        assertNull(em.find(Registration.class, regId));
        // But group should still exist
        assertNotNull(em.find(SubjectsGroup.class, groupId));
    }

    @Test
    @Transactional
    @DisplayName("Should load student with attendances")
    void shouldLoadStudentWithAttendances() {
        // Given
        Major major = Major.builder().name("Biology").build();
        StudyYear year = StudyYear.builder().name("3rd").level(3).build();
        em.persist(major);
        em.persist(year);

        Student student = Student.builder()
                .email("attendance@example.com")
                .name("Attendance Student")
                .major(major)
                .studyYear(year)
                .build();
        em.persist(student);

        Subject subject = Subject.builder()
                .name("Molecular Biology")
                .major(major)
                .studyYear(year)
                .monthlyPrice(120)
                .build();
        em.persist(subject);

        SubjectsGroup group = SubjectsGroup.builder()
                .name("Lab Group")
                .subject(subject)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(4))
                .build();
        em.persist(group);

        Session session = Session.builder()
                .subjectsGroup(group)
                .dayOfWeek(Session.DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .durationMinutes(120)
                .isActive(true)
                .build();
        em.persist(session);

        SessionInstance instance = SessionInstance.builder()
                .session(session)
                .sessionDate(LocalDate.now())
                .status(SessionInstance.SessionStatus.SCHEDULED)
                .build();
        em.persist(instance);

        Attendance attendance = Attendance.builder()
                .student(student)
                .sessionInstance(instance)
                .status(Attendance.AttendanceStatus.PRESENT)
                .build();
        em.persist(attendance);
        em.flush();
        em.clear();

        // When
        Student loaded = em.find(Student.class, student.getId());

        // Then
        assertNotNull(loaded);
        assertEquals(1, loaded.getAttendances().size());
        Attendance loadedAttendance = loaded.getAttendances().iterator().next();
        assertEquals(Attendance.AttendanceStatus.PRESENT, loadedAttendance.getStatus());
    }

    // ========================================
    // PrePersist Test
    // ========================================

    @Test
    @Transactional
    @DisplayName("Should use PrePersist to set createdAt if null")
    void shouldUsePrePersistForCreatedAt() {
        // Given
        Major major = Major.builder().name("Economics").build();
        StudyYear year = StudyYear.builder().name("4th").level(4).build();
        em.persist(major);
        em.persist(year);

        Student student = Student.builder()
                .email("prepersist@example.com")
                .name("PrePersist Test")
                .major(major)
                .studyYear(year)
                .createdAt(null) // Explicitly set to null
                .build();

        // When
        em.persist(student);
        em.flush();

        // Then
        assertNotNull(student.getCreatedAt());
    }

    @Test
    @Transactional
    @DisplayName("Should respect manually set createdAt")
    void shouldRespectManuallySetCreatedAt() {
        // Given
        Major major = Major.builder().name("Politics").build();
        StudyYear year = StudyYear.builder().name("5th").level(5).build();
        em.persist(major);
        em.persist(year);

        Instant customTime = Instant.now().minus(30, ChronoUnit.DAYS);

        Student student = Student.builder()
                .email("manual@example.com")
                .name("Manual Time")
                .major(major)
                .studyYear(year)
                .createdAt(customTime)
                .build();

        // When
        em.persist(student);
        em.flush();

        // Then
        assertEquals(customTime, student.getCreatedAt());
    }

    // ========================================
    // IsActive Field Tests
    // ========================================

    @Test
    @Transactional
    @DisplayName("Should default isActive to true")
    void shouldDefaultIsActiveToTrue() {
        // Given
        Major major = Major.builder().name("Sociology").build();
        StudyYear year = StudyYear.builder().name("1st").level(1).build();
        em.persist(major);
        em.persist(year);

        Student student = Student.builder()
                .email("default@example.com")
                .name("Default Active")
                .major(major)
                .studyYear(year)
                // No especificamos isActive
                .build();

        // When
        em.persist(student);
        em.flush();

        // Then
        assertTrue(student.getIsActive());
    }

    @Test
    @Transactional
    @DisplayName("Should handle isActive updates")
    void shouldHandleIsActiveUpdates() {
        // Given
        Major major = Major.builder().name("Psychology").build();
        StudyYear year = StudyYear.builder().name("2nd").level(2).build();
        em.persist(major);
        em.persist(year);

        Student student = Student.builder()
                .email("toggle@example.com")
                .name("Toggle Active")
                .major(major)
                .studyYear(year)
                .isActive(true)
                .build();

        em.persist(student);
        em.flush();

        // When
        student.setIsActive(false);
        em.flush();
        em.clear();

        // Then
        Student updated = em.find(Student.class, student.getId());
        assertFalse(updated.getIsActive());
    }
}
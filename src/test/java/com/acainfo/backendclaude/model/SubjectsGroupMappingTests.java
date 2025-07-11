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
@DisplayName("SubjectsGroup Entity Mapping Tests")
class SubjectsGroupMappingTests {

    @Autowired
    private EntityManager em;

    // ========================================
    // Helper Methods
    // ========================================

    private Subject createAndPersistSubject(String name) {
        Major major = Major.builder()
                .name("Test Major for " + name)
                .build();
        em.persist(major);

        StudyYear year = StudyYear.builder()
                .name("1st Year")
                .level(1)
                .build();
        em.persist(year);

        Subject subject = Subject.builder()
                .name(name)
                .major(major)
                .studyYear(year)
                .monthlyPrice(100)
                .build();
        em.persist(subject);
        em.flush();

        return subject;
    }

    private Instructor createAndPersistInstructor(String email, String name) {
        Instructor instructor = Instructor.builder()
                .email(email)
                .name(name)
                .build();
        em.persist(instructor);
        em.flush();

        return instructor;
    }

    // ========================================
    // Basic CRUD Tests
    // ========================================

    @Test
    @Transactional
    @DisplayName("Should handle minimal valid subjects group")
    void shouldHandleMinimalValidSubjectsGroup() {
        // Given
        Subject subject = createAndPersistSubject("Mathematics I");

        SubjectsGroup group = SubjectsGroup.builder()
                .name("Group A")
                .subject(subject)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(4))
                .build();

        // When
        em.persist(group);
        em.flush();

        // Then
        assertNotNull(group.getId());
        assertEquals("Group A", group.getName());
        assertEquals(subject, group.getSubject());
        assertNull(group.getInstructor());
        assertEquals(30, group.getMaxCapacity()); // Default value
        assertEquals(SubjectsGroup.GroupStatus.PLANNED, group.getStatus()); // Default
        assertNotNull(group.getRegistrations());
        assertTrue(group.getRegistrations().isEmpty());
        assertNotNull(group.getSessions());
        assertTrue(group.getSessions().isEmpty());
    }

    @Test
    @Transactional
    @DisplayName("Should create subjects group using builder with all fields")
    void shouldCreateSubjectsGroupUsingBuilder() {
        // Given
        Subject subject = createAndPersistSubject("Physics II");
        Instructor instructor = createAndPersistInstructor("prof@example.com", "Professor Smith");

        LocalDate startDate = LocalDate.of(2025, 9, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 20);

        // When
        SubjectsGroup group = SubjectsGroup.builder()
                .name("Advanced Physics Group")
                .subject(subject)
                .instructor(instructor)
                .startDate(startDate)
                .endDate(endDate)
                .maxCapacity(25)
                .status(SubjectsGroup.GroupStatus.ACTIVE)
                .build();

        em.persist(group);
        em.flush();

        // Then
        assertNotNull(group.getId());
        assertEquals("Advanced Physics Group", group.getName());
        assertEquals(subject, group.getSubject());
        assertEquals(instructor, group.getInstructor());
        assertEquals(startDate, group.getStartDate());
        assertEquals(endDate, group.getEndDate());
        assertEquals(25, group.getMaxCapacity());
        assertEquals(SubjectsGroup.GroupStatus.ACTIVE, group.getStatus());
    }

    @Test
    @Transactional
    @DisplayName("Should persist and retrieve subjects group")
    void shouldPersistAndRetrieveSubjectsGroup() {
        // Given
        Subject subject = createAndPersistSubject("Chemistry");
        Instructor instructor = createAndPersistInstructor("chem@example.com", "Dr. Brown");

        SubjectsGroup group = SubjectsGroup.builder()
                .name("Lab Group 1")
                .subject(subject)
                .instructor(instructor)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(3))
                .maxCapacity(15)
                .status(SubjectsGroup.GroupStatus.PLANNED)
                .build();

        em.persist(group);
        em.flush();
        em.clear();

        // When
        SubjectsGroup retrieved = em.find(SubjectsGroup.class, group.getId());

        // Then
        assertNotNull(retrieved);
        assertEquals("Lab Group 1", retrieved.getName());
        assertEquals(subject.getId(), retrieved.getSubject().getId());
        assertEquals(instructor.getId(), retrieved.getInstructor().getId());
        assertEquals(15, retrieved.getMaxCapacity());
        assertEquals(SubjectsGroup.GroupStatus.PLANNED, retrieved.getStatus());
    }

    // ========================================
    // Validation Tests
    // ========================================

    @Test
    @DisplayName("Should reject null name")
    void shouldRejectNullName() {
        // Given
        Subject subject = createAndPersistSubject("Biology");

        SubjectsGroup group = SubjectsGroup.builder()
                .name(null)
                .subject(subject)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(4))
                .build();

        // When & Then
        assertThrows(ConstraintViolationException.class, () -> {
            em.persist(group);
            em.flush();
        });
    }

    @Test
    @DisplayName("Should reject null subject")
    void shouldRejectNullSubject() {
        // Given
        SubjectsGroup group = SubjectsGroup.builder()
                .name("Invalid Group")
                .subject(null)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(4))
                .build();

        // When & Then
        assertThrows(ConstraintViolationException.class, () -> {
            em.persist(group);
            em.flush();
        });
    }

    @Test
    @DisplayName("Should reject null start date")
    void shouldRejectNullStartDate() {
        // Given
        Subject subject = createAndPersistSubject("History");

        SubjectsGroup group = SubjectsGroup.builder()
                .name("History Group")
                .subject(subject)
                .startDate(null)
                .endDate(LocalDate.now().plusMonths(4))
                .build();

        // When & Then
        assertThrows(ConstraintViolationException.class, () -> {
            em.persist(group);
            em.flush();
        });
    }

    @Test
    @DisplayName("Should reject null end date")
    void shouldRejectNullEndDate() {
        // Given
        Subject subject = createAndPersistSubject("Geography");

        SubjectsGroup group = SubjectsGroup.builder()
                .name("Geography Group")
                .subject(subject)
                .startDate(LocalDate.now())
                .endDate(null)
                .build();

        // When & Then
        assertThrows(ConstraintViolationException.class, () -> {
            em.persist(group);
            em.flush();
        });
    }

    @Test
    @DisplayName("Should reject end date before start date")
    void shouldRejectEndDateBeforeStartDate() {
        // Given
        Subject subject = createAndPersistSubject("Music");

        SubjectsGroup group = SubjectsGroup.builder()
                .name("Music Group")
                .subject(subject)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().minusDays(1)) // End before start
                .build();

        // When & Then
        assertThrows(ConstraintViolationException.class, () -> {
            em.persist(group);
            em.flush();
        });
    }

    @Test
    @DisplayName("Should reject zero max capacity")
    void shouldRejectZeroMaxCapacity() {
        // Given
        Subject subject = createAndPersistSubject("Art");

        SubjectsGroup group = SubjectsGroup.builder()
                .name("Art Group")
                .subject(subject)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(2))
                .maxCapacity(0)
                .build();

        // When & Then
        assertThrows(ConstraintViolationException.class, () -> {
            em.persist(group);
            em.flush();
        });
    }

    @Test
    @DisplayName("Should reject negative max capacity")
    void shouldRejectNegativeMaxCapacity() {
        // Given
        Subject subject = createAndPersistSubject("Sports");

        SubjectsGroup group = SubjectsGroup.builder()
                .name("Sports Group")
                .subject(subject)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(2))
                .maxCapacity(-10)
                .build();

        // When & Then
        assertThrows(ConstraintViolationException.class, () -> {
            em.persist(group);
            em.flush();
        });
    }

    @Test
    @DisplayName("Should reject too long name")
    void shouldRejectTooLongName() {
        // Given
        Subject subject = createAndPersistSubject("Literature");
        String longName = "N".repeat(256);

        SubjectsGroup group = SubjectsGroup.builder()
                .name(longName)
                .subject(subject)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(4))
                .build();

        // When & Then
        assertThrows(ConstraintViolationException.class, () -> {
            em.persist(group);
            em.flush();
        });
    }

    // ========================================
    // Unique Constraint Tests
    // ========================================

    @Test
    @DisplayName("Should reject duplicate name within same subject")
    void shouldRejectDuplicateNameWithinSameSubject() {
        // Given
        Subject subject = createAndPersistSubject("Economics");

        SubjectsGroup group1 = SubjectsGroup.builder()
                .name("Morning Group")
                .subject(subject)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(4))
                .build();
        em.persist(group1);
        em.flush();

        SubjectsGroup group2 = SubjectsGroup.builder()
                .name("Morning Group") // Same name
                .subject(subject)      // Same subject
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now().plusMonths(5))
                .build();

        // When & Then
        assertThrows(PersistenceException.class, () -> {
            em.persist(group2);
            em.flush();
        });
    }

    @Test
    @DisplayName("Should allow same name in different subjects")
    void shouldAllowSameNameInDifferentSubjects() {
        // Given
        Subject subject1 = createAndPersistSubject("Calculus I");
        Subject subject2 = createAndPersistSubject("Calculus II");

        SubjectsGroup group1 = SubjectsGroup.builder()
                .name("Group A")
                .subject(subject1)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(4))
                .build();
        em.persist(group1);
        em.flush();

        SubjectsGroup group2 = SubjectsGroup.builder()
                .name("Group A")  // Same name
                .subject(subject2) // Different subject
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(4))
                .build();

        // When
        em.persist(group2);
        em.flush();

        // Then
        assertNotNull(group2.getId());
        assertNotEquals(group1.getId(), group2.getId());
    }

    // ========================================
    // Optional Fields Tests
    // ========================================

    @Test
    @DisplayName("Should allow null instructor")
    void shouldAllowNullInstructor() {
        // Given
        Subject subject = createAndPersistSubject("Philosophy");

        SubjectsGroup group = SubjectsGroup.builder()
                .name("Self-Study Group")
                .subject(subject)
                .instructor(null)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(3))
                .build();

        // When
        em.persist(group);
        em.flush();

        // Then
        assertNotNull(group.getId());
        assertNull(group.getInstructor());
    }

    @Test
    @DisplayName("Should set created at automatically")
    void shouldSetCreatedAtAutomatically() {
        // Given
        Subject subject = createAndPersistSubject("Computer Science");
        Instant before = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        SubjectsGroup group = SubjectsGroup.builder()
                .name("Programming Lab")
                .subject(subject)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(4))
                .build();

        // When
        em.persist(group);
        em.flush();

        // Then
        assertNotNull(group.getCreatedAt());
        Instant createdAt = group.getCreatedAt().truncatedTo(ChronoUnit.SECONDS);
        assertTrue(createdAt.equals(before) || createdAt.isAfter(before));
    }

    // ========================================
    // Status Enum Tests
    // ========================================

    @Test
    @DisplayName("Should default status to PLANNED")
    void shouldDefaultStatusToPlanned() {
        // Given
        Subject subject = createAndPersistSubject("Languages");

        SubjectsGroup group = SubjectsGroup.builder()
                .name("Spanish Group")
                .subject(subject)
                .startDate(LocalDate.now().plusMonths(1))
                .endDate(LocalDate.now().plusMonths(5))
                // No status specified
                .build();

        // When
        em.persist(group);
        em.flush();

        // Then
        assertEquals(SubjectsGroup.GroupStatus.PLANNED, group.getStatus());
    }

    @Test
    @DisplayName("Should handle all status values")
    void shouldHandleAllStatusValues() {
        // Given
        Subject subject = createAndPersistSubject("Engineering");

        for (SubjectsGroup.GroupStatus status : SubjectsGroup.GroupStatus.values()) {
            SubjectsGroup group = SubjectsGroup.builder()
                    .name("Group " + status)
                    .subject(subject)
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusMonths(4))
                    .status(status)
                    .build();

            // When
            em.persist(group);
            em.flush();

            // Then
            assertEquals(status, group.getStatus());
            em.clear();
        }
    }

    // ========================================
    // Relationship Tests
    // ========================================

    @Test
    @Transactional
    @DisplayName("Should handle instructor deletion with SET_NULL")
    void shouldHandleInstructorDeletionWithSetNull() {
        // Given
        Subject subject = createAndPersistSubject("Psychology");
        Instructor instructor = createAndPersistInstructor("psych@example.com", "Dr. Mind");

        SubjectsGroup group = SubjectsGroup.builder()
                .name("Therapy Group")
                .subject(subject)
                .instructor(instructor)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(6))
                .build();
        em.persist(group);
        em.flush();

        Integer groupId = group.getId();
        Integer instructorId = instructor.getId();
        em.clear();

        // When - Delete instructor
        Instructor toDelete = em.find(Instructor.class, instructorId);
        em.remove(toDelete);
        em.flush();
        em.clear();

        // Then
        SubjectsGroup remainingGroup = em.find(SubjectsGroup.class, groupId);
        assertNotNull(remainingGroup);
        assertNull(remainingGroup.getInstructor()); // Should be SET NULL
        assertEquals("Therapy Group", remainingGroup.getName());
    }

    @Test
    @Transactional
    @DisplayName("Should cascade delete when subject is deleted")
    void shouldCascadeDeleteWhenSubjectDeleted() {
        // Given
        Subject subject = createAndPersistSubject("Temporary Subject");

        SubjectsGroup group = SubjectsGroup.builder()
                .name("Temporary Group")
                .subject(subject)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(2))
                .build();
        em.persist(group);
        em.flush();

        Integer subjectId = subject.getId();
        Integer groupId = group.getId();
        em.clear();

        // When - Delete subject
        Subject toDelete = em.find(Subject.class, subjectId);
        em.remove(toDelete);
        em.flush();
        em.clear();

        // Then
        assertNull(em.find(Subject.class, subjectId));
        assertNull(em.find(SubjectsGroup.class, groupId)); // Should be CASCADE deleted
    }

    @Test
    @Transactional
    @DisplayName("Should load subjects group with registrations")
    void shouldLoadSubjectsGroupWithRegistrations() {
        // Given
        Subject subject = createAndPersistSubject("Database Systems");
        SubjectsGroup group = SubjectsGroup.builder()
                .name("DB Lab Group")
                .subject(subject)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(4))
                .maxCapacity(20)
                .build();
        em.persist(group);

        // Create students and registrations
        Major major = subject.getMajor();
        StudyYear year = subject.getStudyYear();

        for (int i = 0; i < 3; i++) {
            Student student = Student.builder()
                    .email("student" + i + "@example.com")
                    .name("Student " + i)
                    .major(major)
                    .studyYear(year)
                    .build();
            em.persist(student);

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
        }
        em.flush();
        em.clear();

        // When
        SubjectsGroup loaded = em.find(SubjectsGroup.class, group.getId());

        // Then
        assertNotNull(loaded);
        assertEquals(3, loaded.getRegistrations().size());
        assertTrue(loaded.getRegistrations().stream()
                .allMatch(r -> r.getStatus() == Registration.RegistrationStatus.ACTIVE));
    }

    @Test
    @Transactional
    @DisplayName("Should load subjects group with sessions")
    void shouldLoadSubjectsGroupWithSessions() {
        // Given
        Subject subject = createAndPersistSubject("Web Development");
        SubjectsGroup group = SubjectsGroup.builder()
                .name("Web Dev Group")
                .subject(subject)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(4))
                .build();
        em.persist(group);

        // Create sessions
        Session monday = Session.builder()
                .subjectsGroup(group)
                .dayOfWeek(Session.DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0))
                .durationMinutes(120)
                .isActive(true)
                .build();
        em.persist(monday);

        Session wednesday = Session.builder()
                .subjectsGroup(group)
                .dayOfWeek(Session.DayOfWeek.WEDNESDAY)
                .startTime(LocalTime.of(14, 0))
                .durationMinutes(90)
                .isActive(true)
                .build();
        em.persist(wednesday);

        em.flush();
        em.clear();

        // When
        SubjectsGroup loaded = em.find(SubjectsGroup.class, group.getId());

        // Then
        assertNotNull(loaded);
        assertEquals(2, loaded.getSessions().size());
        assertTrue(loaded.getSessions().stream().allMatch(Session::getIsActive));
    }

    // ========================================
    // Edge Cases & Special Tests
    // ========================================

    @Test
    @DisplayName("Should handle maximum length fields")
    void shouldHandleMaximumLengthFields() {
        // Given
        Subject subject = createAndPersistSubject("Test Subject");
        String maxName = "N".repeat(255);

        SubjectsGroup group = SubjectsGroup.builder()
                .name(maxName)
                .subject(subject)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(10))
                .maxCapacity(Integer.MAX_VALUE)
                .build();

        // When
        em.persist(group);
        em.flush();

        // Then
        assertNotNull(group.getId());
        assertEquals(maxName, group.getName());
        assertEquals(Integer.MAX_VALUE, group.getMaxCapacity());
    }

    @Test
    @DisplayName("Should handle special characters in name")
    void shouldHandleSpecialCharactersInName() {
        // Given
        Subject subject = createAndPersistSubject("International Studies");

        SubjectsGroup group = SubjectsGroup.builder()
                .name("Grupo Español/English - 特別クラス")
                .subject(subject)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(4))
                .build();

        // When
        em.persist(group);
        em.flush();
        em.clear();

        // Then
        SubjectsGroup retrieved = em.find(SubjectsGroup.class, group.getId());
        assertNotNull(retrieved);
        assertEquals("Grupo Español/English - 特別クラス", retrieved.getName());
    }

    @Test
    @DisplayName("Should generate id automatically")
    void shouldGenerateIdAutomatically() {
        // Given
        Subject subject = createAndPersistSubject("Auto ID Test");

        SubjectsGroup group = SubjectsGroup.builder()
                .name("Auto ID Group")
                .subject(subject)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(3))
                .build();

        // When
        assertNull(group.getId());
        em.persist(group);
        em.flush();

        // Then
        assertNotNull(group.getId());
        assertTrue(group.getId() > 0);
    }

    @Test
    @DisplayName("Should validate dates correctly with isValidDates method")
    void shouldValidateDatesCorrectlyWithIsValidDatesMethod() {
        // Given
        Subject subject = createAndPersistSubject("Date Validation Test");

        // Test 1: Valid dates (end after start)
        SubjectsGroup validGroup = SubjectsGroup.builder()
                .name("Valid Dates Group")
                .subject(subject)
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 6, 30))
                .build();

        assertTrue(validGroup.isValidDates());

        // Test 2: Invalid dates (end before start)
        SubjectsGroup invalidGroup = SubjectsGroup.builder()
                .name("Invalid Dates Group")
                .subject(subject)
                .startDate(LocalDate.of(2025, 6, 1))
                .endDate(LocalDate.of(2025, 1, 1))
                .build();

        assertFalse(invalidGroup.isValidDates());

        // Test 3: Same dates (should be valid)
        SubjectsGroup sameDatesGroup = SubjectsGroup.builder()
                .name("Same Dates Group")
                .subject(subject)
                .startDate(LocalDate.of(2025, 3, 15))
                .endDate(LocalDate.of(2025, 3, 17))
                .build();

        assertTrue(sameDatesGroup.isValidDates());
    }
}

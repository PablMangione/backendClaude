package com.acainfo.backendclaude.model;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Subject Entity Mapping Tests")
class SubjectMappingTests {

    @Autowired
    private EntityManager em;

    // ========================================
    // Basic CRUD Tests
    // ========================================

    @Test
    @Transactional
    @DisplayName("Should handle minimal valid subject")
    void shouldHandleMinimalValidSubject() {
        // Given - Create required entities
        Major major = Major.builder()
                .name("Computer Science")
                .build();
        em.persist(major);

        StudyYear studyYear = StudyYear.builder()
                .name("1st Year")
                .level(1)
                .build();
        em.persist(studyYear);
        em.flush();

        Subject subject = Subject.builder()
                .name("Programming I")
                .major(major)
                .studyYear(studyYear)
                .monthlyPrice(100)
                .build();

        // When
        em.persist(subject);
        em.flush();

        // Then
        assertNotNull(subject.getId());
        assertEquals("Programming I", subject.getName());
        assertEquals(100, subject.getMonthlyPrice());
        assertNull(subject.getDescription());
    }

    @Test
    @Transactional
    @DisplayName("Should create subject using builder")
    void shouldCreateSubjectUsingBuilder() {
        // Given
        Major major = Major.builder()
                .name("Mathematics")
                .description("Mathematics Department")
                .build();
        em.persist(major);

        StudyYear studyYear = StudyYear.builder()
                .name("2nd Year")
                .level(2)
                .description("Second year courses")
                .build();
        em.persist(studyYear);
        em.flush();

        // When
        Subject subject = Subject.builder()
                .name("Calculus II")
                .major(major)
                .studyYear(studyYear)
                .monthlyPrice(150)
                .description("Advanced calculus course")
                .build();

        em.persist(subject);
        em.flush();

        // Then
        assertNotNull(subject.getId());
        assertEquals("Calculus II", subject.getName());
        assertEquals(major, subject.getMajor());
        assertEquals(studyYear, subject.getStudyYear());
        assertEquals(150, subject.getMonthlyPrice());
        assertEquals("Advanced calculus course", subject.getDescription());
        assertNotNull(subject.getSubjectsGroups());
        assertTrue(subject.getSubjectsGroups().isEmpty());
    }

    @Test
    @Transactional
    @DisplayName("Should persist and retrieve subject")
    void shouldPersistAndRetrieveSubject() {
        // Given
        Major major = Major.builder().name("Physics").build();
        StudyYear year = StudyYear.builder().name("3rd Year").level(3).build();
        em.persist(major);
        em.persist(year);

        Subject subject = Subject.builder()
                .name("Quantum Mechanics")
                .major(major)
                .studyYear(year)
                .monthlyPrice(200)
                .description("Introduction to quantum mechanics")
                .build();

        em.persist(subject);
        em.flush();
        em.clear();

        // When
        Subject retrieved = em.find(Subject.class, subject.getId());

        // Then
        assertNotNull(retrieved);
        assertEquals("Quantum Mechanics", retrieved.getName());
        assertEquals(200, retrieved.getMonthlyPrice());
        assertEquals("Introduction to quantum mechanics", retrieved.getDescription());
        assertEquals(major.getId(), retrieved.getMajor().getId());
        assertEquals(year.getId(), retrieved.getStudyYear().getId());
    }

    // ========================================
    // Validation Tests
    // ========================================

    @Test
    @DisplayName("Should reject null name")
    void shouldRejectNullName() {
        // Given
        Major major = Major.builder().name("Test Major").build();
        StudyYear year = StudyYear.builder().name("1st").level(1).build();
        em.persist(major);
        em.persist(year);

        Subject subject = Subject.builder()
                .name(null)
                .major(major)
                .studyYear(year)
                .monthlyPrice(100)
                .build();

        // When & Then
        assertThrows(ConstraintViolationException.class, () -> {
            em.persist(subject);
            em.flush();
        });
    }

    @Test
    @DisplayName("Should reject null major")
    void shouldRejectNullMajor() {
        // Given
        StudyYear year = StudyYear.builder().name("1st").level(1).build();
        em.persist(year);

        Subject subject = Subject.builder()
                .name("Test Subject")
                .major(null)
                .studyYear(year)
                .monthlyPrice(100)
                .build();

        // When & Then
        assertThrows(ConstraintViolationException.class, () -> {
            em.persist(subject);
            em.flush();
        });
    }

    @Test
    @DisplayName("Should reject null study year")
    void shouldRejectNullStudyYear() {
        // Given
        Major major = Major.builder().name("Test Major").build();
        em.persist(major);

        Subject subject = Subject.builder()
                .name("Test Subject")
                .major(major)
                .studyYear(null)
                .monthlyPrice(100)
                .build();

        // When & Then
        assertThrows(ConstraintViolationException.class, () -> {
            em.persist(subject);
            em.flush();
        });
    }

    @Test
    @DisplayName("Should reject null monthly price")
    void shouldRejectNullMonthlyPrice() {
        // Given
        Major major = Major.builder().name("Test Major").build();
        StudyYear year = StudyYear.builder().name("1st").level(1).build();
        em.persist(major);
        em.persist(year);

        Subject subject = Subject.builder()
                .name("Test Subject")
                .major(major)
                .studyYear(year)
                .monthlyPrice(null)
                .build();

        // When & Then
        assertThrows(ConstraintViolationException.class, () -> {
            em.persist(subject);
            em.flush();
        });
    }

    @Test
    @DisplayName("Should reject negative monthly price")
    void shouldRejectNegativeMonthlyPrice() {
        // Given
        Major major = Major.builder().name("Test Major").build();
        StudyYear year = StudyYear.builder().name("1st").level(1).build();
        em.persist(major);
        em.persist(year);

        Subject subject = Subject.builder()
                .name("Test Subject")
                .major(major)
                .studyYear(year)
                .monthlyPrice(-10)
                .build();

        // When & Then
        assertThrows(ConstraintViolationException.class, () -> {
            em.persist(subject);
            em.flush();
        });
    }

    @Test
    @DisplayName("Should accept zero monthly price")
    void shouldAcceptZeroMonthlyPrice() {
        // Given
        Major major = Major.builder().name("Test Major").build();
        StudyYear year = StudyYear.builder().name("1st").level(1).build();
        em.persist(major);
        em.persist(year);

        Subject subject = Subject.builder()
                .name("Free Course")
                .major(major)
                .studyYear(year)
                .monthlyPrice(0)
                .build();

        // When
        em.persist(subject);
        em.flush();

        // Then
        assertNotNull(subject.getId());
        assertEquals(0, subject.getMonthlyPrice());
    }

    @Test
    @DisplayName("Should reject too long name")
    void shouldRejectTooLongName() {
        // Given
        Major major = Major.builder().name("Test Major").build();
        StudyYear year = StudyYear.builder().name("1st").level(1).build();
        em.persist(major);
        em.persist(year);

        Subject subject = Subject.builder()
                .name("N".repeat(256))
                .major(major)
                .studyYear(year)
                .monthlyPrice(100)
                .build();

        // When & Then
        assertThrows(ConstraintViolationException.class, () -> {
            em.persist(subject);
            em.flush();
        });
    }

    // ========================================
    // Unique Constraint Tests
    // ========================================

    @Test
    @DisplayName("Should reject duplicate name within same major")
    void shouldRejectDuplicateNameWithinSameMajor() {
        // Given
        Major major = Major.builder().name("Computer Science").build();
        StudyYear year1 = StudyYear.builder().name("1st").level(1).build();
        StudyYear year2 = StudyYear.builder().name("2nd").level(2).build();
        em.persist(major);
        em.persist(year1);
        em.persist(year2);

        Subject subject1 = Subject.builder()
                .name("Programming")
                .major(major)
                .studyYear(year1)
                .monthlyPrice(100)
                .build();
        em.persist(subject1);
        em.flush();

        Subject subject2 = Subject.builder()
                .name("Programming")  // Same name
                .major(major)         // Same major
                .studyYear(year2)     // Different year
                .monthlyPrice(150)
                .build();

        // When & Then
        assertThrows(PersistenceException.class, () -> {
            em.persist(subject2);
            em.flush();
        });
    }

    @Test
    @DisplayName("Should allow same name in different majors")
    void shouldAllowSameNameInDifferentMajors() {
        // Given
        Major major1 = Major.builder().name("Computer Science").build();
        Major major2 = Major.builder().name("Mathematics").build();
        StudyYear year = StudyYear.builder().name("1st").level(1).build();
        em.persist(major1);
        em.persist(major2);
        em.persist(year);

        Subject subject1 = Subject.builder()
                .name("Introduction to Programming")
                .major(major1)
                .studyYear(year)
                .monthlyPrice(100)
                .build();
        em.persist(subject1);
        em.flush();

        Subject subject2 = Subject.builder()
                .name("Introduction to Programming")  // Same name
                .major(major2)                       // Different major
                .studyYear(year)
                .monthlyPrice(150)
                .build();

        // When
        em.persist(subject2);
        em.flush();

        // Then
        assertNotNull(subject2.getId());
        assertNotEquals(subject1.getId(), subject2.getId());
    }

    // ========================================
    // Relationship Tests
    // ========================================

    @Test
    @Transactional
    @DisplayName("Should load subject with subjects groups")
    void shouldLoadSubjectWithSubjectsGroups() {
        // Given
        Major major = Major.builder().name("Engineering").build();
        StudyYear year = StudyYear.builder().name("1st").level(1).build();
        em.persist(major);
        em.persist(year);

        Subject subject = Subject.builder()
                .name("Engineering Math")
                .major(major)
                .studyYear(year)
                .monthlyPrice(120)
                .build();
        em.persist(subject);

        // Create groups for the subject
        SubjectsGroup group1 = SubjectsGroup.builder()
                .name("Group A")
                .subject(subject)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(4))
                .maxCapacity(30)
                .build();

        SubjectsGroup group2 = SubjectsGroup.builder()
                .name("Group B")
                .subject(subject)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(4))
                .maxCapacity(25)
                .build();

        em.persist(group1);
        em.persist(group2);
        em.flush();
        em.clear();

        // When
        Subject loaded = em.find(Subject.class, subject.getId());

        // Then
        assertNotNull(loaded);
        assertEquals(2, loaded.getSubjectsGroups().size());
    }

    @Test
    @Transactional
    @DisplayName("Should handle subject deletion with groups")
    void shouldHandleSubjectDeletionWithGroups() {
        // Given
        Major major = Major.builder().name("Chemistry").build();
        StudyYear year = StudyYear.builder().name("2nd").level(2).build();
        em.persist(major);
        em.persist(year);

        Subject subject = Subject.builder()
                .name("Organic Chemistry")
                .major(major)
                .studyYear(year)
                .monthlyPrice(180)
                .build();
        em.persist(subject);

        SubjectsGroup group = SubjectsGroup.builder()
                .name("Lab Group")
                .subject(subject)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(3))
                .maxCapacity(15)
                .build();
        em.persist(group);
        em.flush();

        Integer subjectId = subject.getId();
        Integer groupId = group.getId();
        em.clear();

        // When - Delete subject
        Subject subjectToDelete = em.find(Subject.class, subjectId);
        em.remove(subjectToDelete);
        em.flush();
        em.clear();

        // Then - Subject and group should be deleted (CASCADE)
        assertNull(em.find(Subject.class, subjectId));
        assertNull(em.find(SubjectsGroup.class, groupId),
                "Group should be deleted due to CASCADE on subject relationship");
    }

    // ========================================
    // Special Field Tests
    // ========================================

    @Test
    @DisplayName("Should allow null description")
    void shouldAllowNullDescription() {
        // Given
        Major major = Major.builder().name("Biology").build();
        StudyYear year = StudyYear.builder().name("1st").level(1).build();
        em.persist(major);
        em.persist(year);

        Subject subject = Subject.builder()
                .name("Cell Biology")
                .major(major)
                .studyYear(year)
                .monthlyPrice(130)
                .description(null)
                .build();

        // When
        em.persist(subject);
        em.flush();

        // Then
        assertNotNull(subject.getId());
        assertNull(subject.getDescription());
    }

    @Test
    @DisplayName("Should handle long description")
    void shouldHandleLongDescription() {
        // Given
        Major major = Major.builder().name("Literature").build();
        StudyYear year = StudyYear.builder().name("3rd").level(3).build();
        em.persist(major);
        em.persist(year);

        String longDescription = "This is a comprehensive course that covers ".repeat(20);

        Subject subject = Subject.builder()
                .name("World Literature")
                .major(major)
                .studyYear(year)
                .monthlyPrice(110)
                .description(longDescription)
                .build();

        // When
        em.persist(subject);
        em.flush();
        em.clear();

        // Then
        Subject retrieved = em.find(Subject.class, subject.getId());
        assertNotNull(retrieved);
        assertEquals(longDescription, retrieved.getDescription());
    }

    @Test
    @DisplayName("Should set created at automatically")
    void shouldSetCreatedAtAutomatically() {
        // Given
        Major major = Major.builder().name("History").build();
        StudyYear year = StudyYear.builder().name("1st").level(1).build();
        em.persist(major);
        em.persist(year);

        Instant before = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        Subject subject = Subject.builder()
                .name("Ancient History")
                .major(major)
                .studyYear(year)
                .monthlyPrice(95)
                .build();

        // When
        em.persist(subject);
        em.flush();

        // Then
        assertNotNull(subject.getCreatedAt());
        // Truncate to seconds to avoid nanosecond precision issues
        Instant createdAt = subject.getCreatedAt().truncatedTo(ChronoUnit.SECONDS);
        assertTrue(createdAt.equals(before) || createdAt.isAfter(before));
    }

    @Test
    @DisplayName("Should handle maximum length fields")
    void shouldHandleMaximumLengthFields() {
        // Given
        Major major = Major.builder().name("Test Major").build();
        StudyYear year = StudyYear.builder().name("1st").level(1).build();
        em.persist(major);
        em.persist(year);

        String maxName = "N".repeat(255);

        Subject subject = Subject.builder()
                .name(maxName)
                .major(major)
                .studyYear(year)
                .monthlyPrice(Integer.MAX_VALUE)
                .build();

        // When
        em.persist(subject);
        em.flush();

        // Then
        assertNotNull(subject.getId());
        assertEquals(maxName, subject.getName());
        assertEquals(Integer.MAX_VALUE, subject.getMonthlyPrice());
    }

    @Test
    @DisplayName("Should handle special characters in fields")
    void shouldHandleSpecialCharactersInFields() {
        // Given
        Major major = Major.builder().name("Languages").build();
        StudyYear year = StudyYear.builder().name("2nd").level(2).build();
        em.persist(major);
        em.persist(year);

        Subject subject = Subject.builder()
                .name("日本語 & 中文 - Special Characters!")
                .major(major)
                .studyYear(year)
                .monthlyPrice(140)
                .description("Description with émojis 😊 and symbols @#$%")
                .build();

        // When
        em.persist(subject);
        em.flush();
        em.clear();

        // Then
        Subject retrieved = em.find(Subject.class, subject.getId());
        assertNotNull(retrieved);
        assertEquals("日本語 & 中文 - Special Characters!", retrieved.getName());
        assertEquals("Description with émojis 😊 and symbols @#$%", retrieved.getDescription());
    }

    // ========================================
    // Edge Case Tests
    // ========================================

    @Test
    @DisplayName("Should generate id automatically")
    void shouldGenerateIdAutomatically() {
        // Given
        Major major = Major.builder().name("Music").build();
        StudyYear year = StudyYear.builder().name("1st").level(1).build();
        em.persist(major);
        em.persist(year);

        Subject subject = Subject.builder()
                .name("Music Theory")
                .major(major)
                .studyYear(year)
                .monthlyPrice(125)
                .build();

        // When
        assertNull(subject.getId());
        em.persist(subject);
        em.flush();

        // Then
        assertNotNull(subject.getId());
        assertTrue(subject.getId() > 0);
    }
}
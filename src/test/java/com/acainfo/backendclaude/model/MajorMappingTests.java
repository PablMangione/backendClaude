package com.acainfo.backendclaude.model;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.ConstraintViolationException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Major Entity Mapping Tests")
public class MajorMappingTests {

    @PersistenceContext
    private EntityManager em;

    // ---------------------------------------------------------------------
    // BASIC CRUD OPERATIONS
    // ---------------------------------------------------------------------

    @Test
    @Transactional
    @DisplayName("Should persist and retrieve major successfully")
    void shouldPersistAndRetrieveMajor() {
        // Given
        Major major = TestEntityFactory.createMajor("Computer Science");

        // When
        em.persist(major);
        em.flush();
        em.clear();

        // Then
        Major retrieved = em.find(Major.class, major.getId());
        assertNotNull(retrieved);
        assertEquals("Computer Science", retrieved.getName());
        assertEquals("Test major: Computer Science", retrieved.getDescription());
        assertNotNull(retrieved.getCreatedAt());

        // Verify collections are initialized
        assertNotNull(retrieved.getStudents());
        assertNotNull(retrieved.getSubjects());
        assertTrue(retrieved.getStudents().isEmpty());
        assertTrue(retrieved.getSubjects().isEmpty());
    }

    @Test
    @Transactional
    @DisplayName("Should generate ID automatically")
    void shouldGenerateIdAutomatically() {
        // Given
        Major major = TestEntityFactory.createMajor("Engineering");

        // When
        assertNull(major.getId()); // Before persist
        em.persist(major);
        em.flush();

        // Then
        assertNotNull(major.getId());
        assertTrue(major.getId() > 0);
    }

    @Test
    @Transactional
    @DisplayName("Should set createdAt automatically")
    void shouldSetCreatedAtAutomatically() {
        // Given
        Instant beforeCreation = Instant.now().minusSeconds(1);
        Major major = TestEntityFactory.createMajor("Biology");

        // When
        em.persist(major);
        em.flush();

        // Then
        assertNotNull(major.getCreatedAt());
        assertTrue(major.getCreatedAt().isAfter(beforeCreation));
    }

    // ---------------------------------------------------------------------
    // VALIDATION TESTS
    // ---------------------------------------------------------------------

    @Test
    @Transactional
    @DisplayName("Should reject null name")
    void shouldRejectNullName() {
        // Given
        Major major = Major.builder()
                .name(null)  // Invalid
                .description("Test description")
                .createdAt(Instant.now())
                .build();

        // When/Then
        assertThrows(Exception.class, () -> {
            em.persist(major);
            em.flush();
        });
    }

    @Test
    @Transactional
    @DisplayName("Should reject empty name")
    void shouldRejectEmptyName() {
        // Given
        Major major = Major.builder()
                .name("")  // Invalid
                .description("Test description")
                .createdAt(Instant.now())
                .build();

        // When/Then
        assertThrows(ConstraintViolationException.class, () -> {
            em.persist(major);
            em.flush();
        });
    }

    @Test
    @Transactional
    @DisplayName("Should reject name longer than 255 characters")
    void shouldRejectTooLongName() {
        // Given
        String longName = "A".repeat(256); // Too long
        Major major = Major.builder()
                .name(longName)
                .description("Test description")
                .createdAt(Instant.now())
                .build();

        // When/Then
        assertThrows(Exception.class, () -> {
            em.persist(major);
            em.flush();
        });
    }

    @Test
    @Transactional
    @DisplayName("Should allow null description")
    void shouldAllowNullDescription() {
        // Given
        Major major = Major.builder()
                .name("Valid Major")
                .description(null)  // Should be allowed
                .createdAt(Instant.now())
                .build();

        // When
        em.persist(major);
        em.flush();
        em.clear();

        // Then
        Major retrieved = em.find(Major.class, major.getId());
        assertNotNull(retrieved);
        assertEquals("Valid Major", retrieved.getName());
        assertNull(retrieved.getDescription());
    }

    // ---------------------------------------------------------------------
    // RELATIONSHIP TESTS
    // ---------------------------------------------------------------------

    @Test
    @Transactional
    @DisplayName("Should load major with students (1:M)")
    void shouldLoadMajorWithStudents() {
        // Given
        Major major = TestEntityFactory.createMajor("Mathematics");
        StudyYear studyYear = TestEntityFactory.createStudyYear("1st Year", 1);
        em.persist(major);
        em.persist(studyYear);

        Student student1 = TestEntityFactory.createStudent("student1@example.com", "Student One", major, studyYear);
        Student student2 = TestEntityFactory.createStudent("student2@example.com", "Student Two", major, studyYear);
        em.persist(student1);
        em.persist(student2);
        em.flush();
        em.clear();

        // When
        Major retrieved = em.find(Major.class, major.getId());

        // Then
        assertNotNull(retrieved.getStudents());
        assertEquals(2, retrieved.getStudents().size());

        assertThat(retrieved.getStudents())
                .extracting(Student::getEmail)
                .containsExactlyInAnyOrder("student1@example.com", "student2@example.com");
    }

    @Test
    @Transactional
    @DisplayName("Should load major with subjects (1:M)")
    void shouldLoadMajorWithSubjects() {
        // Given
        Major major = TestEntityFactory.createMajor("Physics");
        StudyYear studyYear1 = TestEntityFactory.createStudyYear("1st Year", 1);
        StudyYear studyYear2 = TestEntityFactory.createStudyYear("2nd Year", 2);
        em.persist(major);
        em.persist(studyYear1);
        em.persist(studyYear2);

        Subject subject1 = TestEntityFactory.createSubject("Physics I", major, studyYear1);
        Subject subject2 = TestEntityFactory.createSubject("Physics II", major, studyYear2);
        em.persist(subject1);
        em.persist(subject2);
        em.flush();
        em.clear();

        // When
        Major retrieved = em.find(Major.class, major.getId());

        // Then
        assertNotNull(retrieved.getSubjects());
        assertEquals(2, retrieved.getSubjects().size());

        assertThat(retrieved.getSubjects())
                .extracting(Subject::getName)
                .containsExactlyInAnyOrder("Physics I", "Physics II");
    }

    @Test
    @Transactional
    @DisplayName("Should maintain referential integrity when major has dependent entities")
    void shouldMaintainReferentialIntegrityWithDependents() {
        // Given
        Major major = TestEntityFactory.createMajor("Chemistry");
        StudyYear studyYear = TestEntityFactory.createStudyYear("1st Year", 1);
        em.persist(major);
        em.persist(studyYear);

        Student student = TestEntityFactory.createStudent("chemistry.student@example.com", "Chemistry Student", major, studyYear);
        Subject subject = TestEntityFactory.createSubject("Chemistry I", major, studyYear);
        em.persist(student);
        em.persist(subject);
        em.flush();

        // When/Then - Should not be able to delete major with dependent entities
        assertThrows(Exception.class, () -> {
            em.remove(major);
            em.flush();
        });
    }

    // ---------------------------------------------------------------------
    // BUILDER PATTERN TESTS
    // ---------------------------------------------------------------------

    @Test
    @Transactional
    @DisplayName("Should create major using builder pattern")
    void shouldCreateMajorUsingBuilder() {
        // Given
        Instant now = Instant.now();
        Major major = Major.builder()
                .name("Art")
                .description("Fine Arts and Visual Arts")
                .createdAt(now)
                .build();

        // When
        em.persist(major);
        em.flush();
        em.clear();

        // Then
        Major retrieved = em.find(Major.class, major.getId());
        assertNotNull(retrieved);
        assertEquals("Art", retrieved.getName());
        assertEquals("Fine Arts and Visual Arts", retrieved.getDescription());
        assertEquals(now.getEpochSecond(), retrieved.getCreatedAt().getEpochSecond());

        // Collections should be initialized by @Builder.Default
        assertNotNull(retrieved.getStudents());
        assertNotNull(retrieved.getSubjects());
        assertEquals(0, retrieved.getStudents().size());
        assertEquals(0, retrieved.getSubjects().size());
    }

    // ---------------------------------------------------------------------
    // EDGE CASES AND BOUNDARY CONDITIONS
    // ---------------------------------------------------------------------

    @Test
    @Transactional
    @DisplayName("Should handle maximum length name")
    void shouldHandleMaximumLengthName() {
        // Given
        String maxLengthName = "A".repeat(255); // Exactly 255 characters
        Major major = Major.builder()
                .name(maxLengthName)
                .description("Test description")
                .createdAt(Instant.now())
                .build();

        // When
        em.persist(major);
        em.flush();
        em.clear();

        // Then
        Major retrieved = em.find(Major.class, major.getId());
        assertNotNull(retrieved);
        assertEquals(maxLengthName, retrieved.getName());
        assertEquals(255, retrieved.getName().length());
    }

    @Test
    @Transactional
    @DisplayName("Should handle special characters in name")
    void shouldHandleSpecialCharactersInName() {
        // Given
        String specialName = "Computer Science & Engineering (AI/ML)";
        Major major = TestEntityFactory.createMajor(specialName);

        // When
        em.persist(major);
        em.flush();
        em.clear();

        // Then
        Major retrieved = em.find(Major.class, major.getId());
        assertNotNull(retrieved);
        assertEquals(specialName, retrieved.getName());
    }

    @Test
    @Transactional
    @DisplayName("Should handle very long description")
    void shouldHandleVeryLongDescription() {
        // Given
        String longDescription = "This is a very long description. ".repeat(20); // Very long text
        Major major = Major.builder()
                .name("Test Major")
                .description(longDescription)
                .createdAt(Instant.now())
                .build();

        // When
        em.persist(major);
        em.flush();
        em.clear();

        // Then
        Major retrieved = em.find(Major.class, major.getId());
        assertNotNull(retrieved);
        assertEquals("Test Major", retrieved.getName());
        assertEquals(longDescription, retrieved.getDescription());
    }
}

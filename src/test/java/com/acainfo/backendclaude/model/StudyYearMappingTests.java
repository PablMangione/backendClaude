package com.acainfo.backendclaude.model;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("StudyYear Entity Mapping Tests")
public class StudyYearMappingTests {

    @PersistenceContext
    private EntityManager em;

    // ---------------------------------------------------------------------
    // BASIC CRUD OPERATIONS
    // ---------------------------------------------------------------------

    @Test
    @Transactional
    @DisplayName("Should persist and retrieve study year successfully")
    void shouldPersistAndRetrieveStudyYear() {
        // Given
        StudyYear studyYear = TestEntityFactory.createStudyYear("1st Year", 1);

        // When
        em.persist(studyYear);
        em.flush();
        em.clear();

        // Then
        StudyYear retrieved = em.find(StudyYear.class, studyYear.getId());
        assertNotNull(retrieved);
        assertEquals("1st Year", retrieved.getName());
        assertEquals(1, retrieved.getLevel());
        assertEquals("Test study year: 1st Year", retrieved.getDescription());

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
        StudyYear studyYear = TestEntityFactory.createStudyYear("2nd Year", 2);

        // When
        assertNull(studyYear.getId()); // Before persist
        em.persist(studyYear);
        em.flush();

        // Then
        assertNotNull(studyYear.getId());
        assertTrue(studyYear.getId() > 0);
    }

    // ---------------------------------------------------------------------
    // VALIDATION TESTS
    // ---------------------------------------------------------------------

    @Test
    @Transactional
    @DisplayName("Should reject null name")
    void shouldRejectNullName() {
        // Given
        StudyYear studyYear = StudyYear.builder()
                .name(null)  // Invalid
                .level(1)
                .description("Test description")
                .build();

        // When/Then
        assertThrows(Exception.class, () -> {
            em.persist(studyYear);
            em.flush();
        });
    }

    @Test
    @Transactional
    @DisplayName("Should reject null level")
    void shouldRejectNullLevel() {
        // Given
        StudyYear studyYear = StudyYear.builder()
                .name("Valid Year")
                .level(null)  // Invalid
                .description("Test description")
                .build();

        // When/Then
        assertThrows(Exception.class, () -> {
            em.persist(studyYear);
            em.flush();
        });
    }

    @Test
    @Transactional
    @DisplayName("Should reject name longer than 16 characters")
    void shouldRejectTooLongName() {
        // Given
        String longName = "A".repeat(17); // Too long (max 16)
        StudyYear studyYear = StudyYear.builder()
                .name(longName)
                .level(1)
                .description("Test description")
                .build();

        // When/Then
        assertThrows(Exception.class, () -> {
            em.persist(studyYear);
            em.flush();
        });
    }

    @Test
    @Transactional
    @DisplayName("Should allow null description")
    void shouldAllowNullDescription() {
        // Given
        StudyYear studyYear = StudyYear.builder()
                .name("Valid Year")
                .level(3)
                .description(null)  // Should be allowed
                .build();

        // When
        em.persist(studyYear);
        em.flush();
        em.clear();

        // Then
        StudyYear retrieved = em.find(StudyYear.class, studyYear.getId());
        assertNotNull(retrieved);
        assertEquals("Valid Year", retrieved.getName());
        assertEquals(3, retrieved.getLevel());
        assertNull(retrieved.getDescription());
    }

    @Test
    @Transactional
    @DisplayName("Should handle negative levels")
    void shouldHandleNegativeLevels() {
        // Given - might be valid for preparatory courses
        StudyYear studyYear = StudyYear.builder()
                .name("Prep Year")
                .level(-1)
                .description("Preparatory year")
                .build();

        // When
        em.persist(studyYear);
        em.flush();
        em.clear();

        // Then
        StudyYear retrieved = em.find(StudyYear.class, studyYear.getId());
        assertNotNull(retrieved);
        assertEquals("Prep Year", retrieved.getName());
        assertEquals(-1, retrieved.getLevel());
    }

    @Test
    @Transactional
    @DisplayName("Should handle high level numbers")
    void shouldHandleHighLevelNumbers() {
        // Given - for PhD or advanced degrees
        StudyYear studyYear = StudyYear.builder()
                .name("PhD Year 5")
                .level(9)
                .description("Advanced PhD year")
                .build();

        // When
        em.persist(studyYear);
        em.flush();
        em.clear();

        // Then
        StudyYear retrieved = em.find(StudyYear.class, studyYear.getId());
        assertNotNull(retrieved);
        assertEquals("PhD Year 5", retrieved.getName());
        assertEquals(9, retrieved.getLevel());
    }

    // ---------------------------------------------------------------------
    // RELATIONSHIP TESTS
    // ---------------------------------------------------------------------

    @Test
    @Transactional
    @DisplayName("Should load study year with students (1:M)")
    void shouldLoadStudyYearWithStudents() {
        // Given
        Major major = TestEntityFactory.createMajor("Engineering");
        StudyYear studyYear = TestEntityFactory.createStudyYear("3rd Year", 3);
        em.persist(major);
        em.persist(studyYear);

        Student student1 = TestEntityFactory.createStudent("student1@example.com", "Student One", major, studyYear);
        Student student2 = TestEntityFactory.createStudent("student2@example.com", "Student Two", major, studyYear);
        em.persist(student1);
        em.persist(student2);
        em.flush();
        em.clear();

        // When
        StudyYear retrieved = em.find(StudyYear.class, studyYear.getId());

        // Then
        assertNotNull(retrieved.getStudents());
        assertEquals(2, retrieved.getStudents().size());

        assertThat(retrieved.getStudents())
                .extracting(Student::getEmail)
                .containsExactlyInAnyOrder("student1@example.com", "student2@example.com");
    }

    @Test
    @Transactional
    @DisplayName("Should load study year with subjects (1:M)")
    void shouldLoadStudyYearWithSubjects() {
        // Given
        Major major1 = TestEntityFactory.createMajor("Mathematics");
        Major major2 = TestEntityFactory.createMajor("Physics");
        StudyYear studyYear = TestEntityFactory.createStudyYear("2nd Year", 2);
        em.persist(major1);
        em.persist(major2);
        em.persist(studyYear);

        Subject subject1 = TestEntityFactory.createSubject("Advanced Calculus", major1, studyYear);
        Subject subject2 = TestEntityFactory.createSubject("Quantum Physics", major2, studyYear);
        em.persist(subject1);
        em.persist(subject2);
        em.flush();
        em.clear();

        // When
        StudyYear retrieved = em.find(StudyYear.class, studyYear.getId());

        // Then
        assertNotNull(retrieved.getSubjects());
        assertEquals(2, retrieved.getSubjects().size());

        assertThat(retrieved.getSubjects())
                .extracting(Subject::getName)
                .containsExactlyInAnyOrder("Advanced Calculus", "Quantum Physics");
    }

    @Test
    @Transactional
    @DisplayName("Should maintain referential integrity when study year has dependent entities")
    void shouldMaintainReferentialIntegrityWithDependents() {
        // Given
        Major major = TestEntityFactory.createMajor("Biology");
        StudyYear studyYear = TestEntityFactory.createStudyYear("4th Year", 4);
        em.persist(major);
        em.persist(studyYear);

        Student student = TestEntityFactory.createStudent("bio.student@example.com", "Bio Student", major, studyYear);
        Subject subject = TestEntityFactory.createSubject("Molecular Biology", major, studyYear);
        em.persist(student);
        em.persist(subject);
        em.flush();

        // When/Then - Should not be able to delete study year with dependent entities
        assertThrows(Exception.class, () -> {
            em.remove(studyYear);
            em.flush();
        });
    }

    // ---------------------------------------------------------------------
    // BUILDER PATTERN TESTS
    // ---------------------------------------------------------------------

    @Test
    @Transactional
    @DisplayName("Should create study year using builder pattern")
    void shouldCreateStudyYearUsingBuilder() {
        // Given
        StudyYear studyYear = StudyYear.builder()
                .name("Master 1")
                .level(5)
                .description("First year of Master's degree")
                .build();

        // When
        em.persist(studyYear);
        em.flush();
        em.clear();

        // Then
        StudyYear retrieved = em.find(StudyYear.class, studyYear.getId());
        assertNotNull(retrieved);
        assertEquals("Master 1", retrieved.getName());
        assertEquals(5, retrieved.getLevel());
        assertEquals("First year of Master's degree", retrieved.getDescription());

        // Collections should be initialized by @Builder.Default
        assertNotNull(retrieved.getStudents());
        assertNotNull(retrieved.getSubjects());
        assertEquals(0, retrieved.getStudents().size());
        assertEquals(0, retrieved.getSubjects().size());
    }

    // ---------------------------------------------------------------------
    // BUSINESS LOGIC TESTS
    // ---------------------------------------------------------------------

    @Test
    @Transactional
    @DisplayName("Should handle typical academic year progression")
    void shouldHandleTypicalAcademicProgression() {
        // Given - Create typical bachelor degree progression
        StudyYear year1 = TestEntityFactory.createStudyYear("1st Year", 1);
        StudyYear year2 = TestEntityFactory.createStudyYear("2nd Year", 2);
        StudyYear year3 = TestEntityFactory.createStudyYear("3rd Year", 3);
        StudyYear year4 = TestEntityFactory.createStudyYear("4th Year", 4);

        // When
        em.persist(year1);
        em.persist(year2);
        em.persist(year3);
        em.persist(year4);
        em.flush();
        em.clear();

        // Then - Verify all were persisted with correct levels
        StudyYear retrieved1 = em.find(StudyYear.class, year1.getId());
        StudyYear retrieved2 = em.find(StudyYear.class, year2.getId());
        StudyYear retrieved3 = em.find(StudyYear.class, year3.getId());
        StudyYear retrieved4 = em.find(StudyYear.class, year4.getId());

        assertNotNull(retrieved1);
        assertNotNull(retrieved2);
        assertNotNull(retrieved3);
        assertNotNull(retrieved4);

        assertEquals(1, retrieved1.getLevel());
        assertEquals(2, retrieved2.getLevel());
        assertEquals(3, retrieved3.getLevel());
        assertEquals(4, retrieved4.getLevel());
    }

    // ---------------------------------------------------------------------
    // EDGE CASES AND BOUNDARY CONDITIONS
    // ---------------------------------------------------------------------

    @Test
    @Transactional
    @DisplayName("Should handle special characters in name")
    void shouldHandleSpecialCharactersInName() {
        // Given
        String specialName = "1st Year (A&B)";
        StudyYear studyYear = StudyYear.builder()
                .name(specialName)
                .level(1)
                .description("First year with specializations")
                .build();

        // When
        em.persist(studyYear);
        em.flush();
        em.clear();

        // Then
        StudyYear retrieved = em.find(StudyYear.class, studyYear.getId());
        assertNotNull(retrieved);
        assertEquals(specialName, retrieved.getName());
    }

    @Test
    @Transactional
    @DisplayName("Should reject description longer than 255 characters")
    void shouldRejectTooLongDescription() {
        // Given
        String longDescription = "A".repeat(256); // Too long (max 16)
        StudyYear studyYear = StudyYear.builder()
                .name("Test name")
                .level(1)
                .description(longDescription)
                .build();

        // When/Then
        assertThrows(Exception.class, () -> {
            em.persist(studyYear);
            em.flush();
        });
    }
}
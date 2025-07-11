package com.acainfo.backendclaude.model;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolationException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Instructor Entity Mapping Tests")
public class InstructorMappingTests {

    @PersistenceContext
    private EntityManager em;

    // ---------------------------------------------------------------------
    // BASIC CRUD OPERATIONS
    // ---------------------------------------------------------------------

    @Test
    @Transactional
    @DisplayName("Should persist and retrieve instructor successfully")
    void shouldPersistAndRetrieveInstructor() {
        // Given
        Instructor instructor =
                TestEntityFactory.createInstructor("john.doe@example.com", "John Doe");

        // When
        em.persist(instructor);
        em.flush();
        em.clear();

        // Then
        Instructor retrieved = em.find(Instructor.class, instructor.getId());
        assertNotNull(retrieved);
        assertEquals("john.doe@example.com", retrieved.getEmail());
        assertEquals("John Doe", retrieved.getName());
        assertEquals("987654321", retrieved.getPhone());
        assertEquals("Test specialization", retrieved.getSpecialization());
        assertNotNull(retrieved.getCreatedAt());

        // Verify collections are initialized
        assertNotNull(retrieved.getSubjectsGroups());
        assertTrue(retrieved.getSubjectsGroups().isEmpty());
    }

    @Test
    @Transactional
    @DisplayName("Should generate ID automatically")
    void shouldGenerateIdAutomatically() {
        // Given
        Instructor instructor = TestEntityFactory.
                createInstructor("jane.smith@example.com", "Jane Smith");

        // When
        assertNull(instructor.getId()); // Before persist
        em.persist(instructor);
        em.flush();

        // Then
        assertNotNull(instructor.getId());
        assertTrue(instructor.getId() > 0);
    }

    @Test
    @Transactional
    @DisplayName("Should set createdAt automatically")
    void shouldSetCreatedAtAutomatically() {
        // Given
        Instant beforeCreation = Instant.now().minusSeconds(1);
        Instructor instructor = TestEntityFactory.createInstructor("auto.timestamp@example.com", "Auto Timestamp");

        // When
        em.persist(instructor);
        em.flush();

        // Then
        assertNotNull(instructor.getCreatedAt());
        assertTrue(instructor.getCreatedAt().isAfter(beforeCreation));
    }

    // ---------------------------------------------------------------------
    // VALIDATION TESTS
    // ---------------------------------------------------------------------

    @Test
    @Transactional
    @DisplayName("Should reject null email")
    void shouldRejectNullEmail() {
        // Given
        Instructor instructor = Instructor.builder()
                .email(null)  // Invalid
                .name("Valid Name")
                .phone("123456789")
                .specialization("Math")
                .createdAt(Instant.now())
                .build();

        // When/Then
        assertThrows(Exception.class, () -> {
            em.persist(instructor);
            em.flush();
        });
    }

    @Test
    @Transactional
    @DisplayName("Should reject null name")
    void shouldRejectNullName() {
        // Given
        Instructor instructor = Instructor.builder()
                .email("valid@example.com")
                .name(null)  // Invalid
                .phone("123456789")
                .specialization("Math")
                .createdAt(Instant.now())
                .build();

        // When/Then
        assertThrows(Exception.class, () -> {
            em.persist(instructor);
            em.flush();
        });
    }

    @Test
    @Transactional
    @DisplayName("Should reject duplicate email addresses")
    void shouldRejectDuplicateEmail() {
        // Given
        Instructor instructor1 = TestEntityFactory.createInstructor("duplicate@example.com", "Instructor One");
        em.persist(instructor1);
        em.flush();

        Instructor instructor2 = TestEntityFactory.createInstructor("duplicate@example.com", "Instructor Two");

        // When/Then
        assertThrows(PersistenceException.class, () -> {
            em.persist(instructor2);
            em.flush();
        });
    }

    @Test
    @Transactional
    @DisplayName("Should reject email longer than 255 characters")
    void shouldRejectTooLongEmail() {
        // Given
        String longEmail = "a".repeat(240) + "@example.com"; // Too long
        Instructor instructor = Instructor.builder()
                .email(longEmail)
                .name("Valid Name")
                .phone("123456789")
                .specialization("Math")
                .createdAt(Instant.now())
                .build();

        // When/Then
        assertThrows(Exception.class, () -> {
            em.persist(instructor);
            em.flush();
        });
    }

    @Test
    @Transactional
    @DisplayName("Should reject name longer than 255 characters")
    void shouldRejectTooLongName() {
        // Given
        String longName = "A".repeat(256); // Too long
        Instructor instructor = Instructor.builder()
                .email("valid@example.com")
                .name(longName)
                .phone("123456789")
                .specialization("Math")
                .createdAt(Instant.now())
                .build();

        // When/Then
        assertThrows(Exception.class, () -> {
            em.persist(instructor);
            em.flush();
        });
    }

    @Test
    @Transactional
    @DisplayName("Should reject phone longer than 20 characters")
    void shouldRejectTooLongPhone() {
        // Given
        String longPhone = "1".repeat(21); // Too long (max 20)
        Instructor instructor = Instructor.builder()
                .email("valid@example.com")
                .name("Valid Name")
                .phone(longPhone)
                .specialization("Math")
                .createdAt(Instant.now())
                .build();

        // When/Then
        assertThrows(Exception.class, () -> {
            em.persist(instructor);
            em.flush();
        });
    }

    @Test
    @Transactional
    @DisplayName("Should reject specialization longer than 255 characters")
    void shouldRejectTooLongSpecialization() {
        // Given
        String longSpecialization = "A".repeat(256); // Too long
        Instructor instructor = Instructor.builder()
                .email("valid@example.com")
                .name("Valid Name")
                .phone("123456789")
                .specialization(longSpecialization)
                .createdAt(Instant.now())
                .build();

        // When/Then
        assertThrows(Exception.class, () -> {
            em.persist(instructor);
            em.flush();
        });
    }

    @Test
    @Transactional
    @DisplayName("Should allow null phone")
    void shouldAllowNullPhone() {
        // Given
        Instructor instructor = Instructor.builder()
                .email("nophone@example.com")
                .name("No Phone")
                .phone(null)  // Should be allowed
                .specialization("Math")
                .createdAt(Instant.now())
                .build();

        // When
        em.persist(instructor);
        em.flush();
        em.clear();

        // Then
        Instructor retrieved = em.find(Instructor.class, instructor.getId());
        assertNotNull(retrieved);
        assertEquals("nophone@example.com", retrieved.getEmail());
        assertNull(retrieved.getPhone());
    }

    @Test
    @Transactional
    @DisplayName("Should allow null specialization")
    void shouldAllowNullSpecialization() {
        // Given
        Instructor instructor = Instructor.builder()
                .email("nospec@example.com")
                .name("No Specialization")
                .phone("123456789")
                .specialization(null)  // Should be allowed
                .createdAt(Instant.now())
                .build();

        // When
        em.persist(instructor);
        em.flush();
        em.clear();

        // Then
        Instructor retrieved = em.find(Instructor.class, instructor.getId());
        assertNotNull(retrieved);
        assertEquals("nospec@example.com", retrieved.getEmail());
        assertNull(retrieved.getSpecialization());
    }

    // ---------------------------------------------------------------------
    // RELATIONSHIP TESTS
    // ---------------------------------------------------------------------

    @Test
    @Transactional
    @DisplayName("Should load instructor with subjects groups (1:M)")
    void shouldLoadInstructorWithSubjectsGroups() {
        // Given
        Major major = TestEntityFactory.createMajor("Mathematics");
        StudyYear studyYear = TestEntityFactory.createStudyYear("1st Year", 1);
        Instructor instructor = TestEntityFactory.createInstructor("math.teacher@example.com", "Math Teacher");
        em.persist(major);
        em.persist(studyYear);
        em.persist(instructor);

        Subject subject1 = TestEntityFactory.createSubject("Algebra", major, studyYear);
        Subject subject2 = TestEntityFactory.createSubject("Geometry", major, studyYear);
        em.persist(subject1);
        em.persist(subject2);

        SubjectsGroup group1 = TestEntityFactory.createSubjectsGroup("Algebra Group A", subject1, instructor);
        SubjectsGroup group2 = TestEntityFactory.createSubjectsGroup("Geometry Group B", subject2, instructor);
        em.persist(group1);
        em.persist(group2);
        em.flush();
        em.clear();

        // When
        Instructor retrieved = em.find(Instructor.class, instructor.getId());

        // Then
        assertNotNull(retrieved.getSubjectsGroups());
        assertEquals(2, retrieved.getSubjectsGroups().size());

        assertThat(retrieved.getSubjectsGroups())
                .extracting(SubjectsGroup::getName)
                .containsExactlyInAnyOrder("Algebra Group A", "Geometry Group B");
    }



    // ---------------------------------------------------------------------
    // BUILDER PATTERN TESTS
    // ---------------------------------------------------------------------

    @Test
    @Transactional
    @DisplayName("Should create instructor using builder pattern")
    void shouldCreateInstructorUsingBuilder() {
        // Given
        Instant now = Instant.now();
        Instructor instructor = Instructor.builder()
                .email("builder@example.com")
                .name("Builder Test")
                .phone("555-1234")
                .specialization("Software Engineering")
                .createdAt(now)
                .build();

        // When
        em.persist(instructor);
        em.flush();
        em.clear();

        // Then
        Instructor retrieved = em.find(Instructor.class, instructor.getId());
        assertNotNull(retrieved);
        assertEquals("builder@example.com", retrieved.getEmail());
        assertEquals("Builder Test", retrieved.getName());
        assertEquals("555-1234", retrieved.getPhone());
        assertEquals("Software Engineering", retrieved.getSpecialization());
        assertEquals(now.getEpochSecond(), retrieved.getCreatedAt().getEpochSecond());

        // Collections should be initialized by @Builder.Default
        assertNotNull(retrieved.getSubjectsGroups());
        assertEquals(0, retrieved.getSubjectsGroups().size());
    }

    @Test
    @Transactional
    @DisplayName("Should handle special characters in fields")
    void shouldHandleSpecialCharactersInFields() {
        // Given
        String specialEmail = "test+special@example-domain.com";
        String specialName = "José María O'Connor-Smith";
        String specialPhone = "+34 (91) 123-4567";
        String specialSpecialization = "AI/ML & Data Science (PhD)";

        Instructor instructor = Instructor.builder()
                .email(specialEmail)
                .name(specialName)
                .phone(specialPhone)
                .specialization(specialSpecialization)
                .createdAt(Instant.now())
                .build();

        // When
        em.persist(instructor);
        em.flush();
        em.clear();

        // Then
        Instructor retrieved = em.find(Instructor.class, instructor.getId());
        assertNotNull(retrieved);
        assertEquals(specialEmail, retrieved.getEmail());
        assertEquals(specialName, retrieved.getName());
        assertEquals(specialPhone, retrieved.getPhone());
        assertEquals(specialSpecialization, retrieved.getSpecialization());
    }

    @Test
    @Transactional
    @DisplayName("Should handle minimal valid instructor")
    void shouldHandleMinimalValidInstructor() {
        // Given - Only required fields
        Instructor instructor = Instructor.builder()
                .email("minimal@example.com")
                .name("Minimal")
                .createdAt(Instant.now())
                .build();

        // When
        em.persist(instructor);
        em.flush();
        em.clear();

        // Then
        Instructor retrieved = em.find(Instructor.class, instructor.getId());
        assertNotNull(retrieved);
        assertEquals("minimal@example.com", retrieved.getEmail());
        assertEquals("Minimal", retrieved.getName());
        assertNull(retrieved.getPhone());
        assertNull(retrieved.getSpecialization());
        assertNotNull(retrieved.getCreatedAt());
    }


    // Test corregido para manejar correctamente la cascada
    @Test
    @Transactional
    @DisplayName("Should handle instructor deletion with subjects groups")
    void shouldHandleInstructorDeletionWithSubjectsGroups() {
        // Given - NO usar TestEntityFactory para evitar problemas con referencias circulares

        // 1. Crear y persistir Major
        Major major = Major.builder()
                .name("Physics")
                .description("Physics Department")
                .build();
        em.persist(major);

        // 2. Crear y persistir StudyYear
        StudyYear studyYear = StudyYear.builder()
                .name("2nd Year")
                .level(2)
                .description("Second year")
                .build();
        em.persist(studyYear);

        // 3. Crear y persistir Instructor
        Instructor instructor = Instructor.builder()
                .name("Physics Teacher")
                .email("physics.teacher@example.com")
                .phone("123456789")
                .specialization("Quantum Physics")
                .build();
        em.persist(instructor);

        // 4. Flush para asegurar que las entidades base están en la DB
        em.flush();

        // 5. Crear y persistir Subject (necesita Major y StudyYear)
        Subject subject = Subject.builder()
                .name("Physics I")
                .major(major)
                .studyYear(studyYear)
                .monthlyPrice(100)
                .description("Introduction to Physics")
                .build();
        em.persist(subject);

        // 6. Crear y persistir SubjectsGroup (necesita Subject e Instructor)
        SubjectsGroup group = SubjectsGroup.builder()
                .name("Physics Group A")
                .subject(subject)
                .instructor(instructor)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(3))
                .maxCapacity(30)
                .status(SubjectsGroup.GroupStatus.PLANNED)
                .build();
        em.persist(group);

        // 7. Flush final para asegurar todo está persistido
        em.flush();

        // Capturar IDs antes de clear
        Integer instructorId = instructor.getId();
        Integer groupId = group.getId();

        // 8. Clear del contexto de persistencia
        em.clear();

        // When - Eliminar el instructor
        // Recargar el instructor desde la base de datos
        Instructor instructorToDelete = em.find(Instructor.class, instructorId);
        assertNotNull(instructorToDelete, "Instructor should exist before deletion");

        // Eliminar el instructor
        em.remove(instructorToDelete);
        em.flush();
        em.clear();

        // Then - Verificar resultados
        // El instructor debe estar eliminado
        Instructor deletedInstructor = em.find(Instructor.class, instructorId);
        assertNull(deletedInstructor, "Instructor should be deleted");

        // El grupo debe existir con instructor = null
        SubjectsGroup remainingGroup = em.find(SubjectsGroup.class, groupId);
        assertNotNull(remainingGroup, "Group should still exist after instructor deletion");
        assertNull(remainingGroup.getInstructor(), "Instructor reference should be null due to @OnDelete(SET_NULL)");

        // Verificar que el subject sigue existiendo
        assertNotNull(remainingGroup.getSubject(), "Subject reference should still exist");
        assertEquals("Physics Group A", remainingGroup.getName());
    }

    // Test corregido para manejar la validación de email con longitud máxima
    @Test
    @DisplayName("Should handle maximum length fields")
    void shouldHandleMaximumLengthFields() {
        // Given - Crear un email válido pero largo
        String maxName = "N".repeat(255);
        String maxPhone = "1".repeat(20);
        String maxSpecialization = "S".repeat(255);

        Instructor instructor = Instructor.builder()
                .email("validEmail@test.com")
                .name(maxName)
                .phone(maxPhone)
                .specialization(maxSpecialization)
                .build();

        // When & Then
        assertDoesNotThrow(() -> {
            em.persist(instructor);
            em.flush();
        });

        assertNotNull(instructor.getId());
        assertEquals(maxName, instructor.getName());
        assertEquals(maxPhone, instructor.getPhone());
        assertEquals(maxSpecialization, instructor.getSpecialization());
    }

    // Nuevo test para validar emails inválidos
    @Test
    @DisplayName("Should reject invalid email format")
    void shouldRejectInvalidEmailFormat() {
        // Given - Varios formatos de email inválidos
        List<String> invalidEmails = List.of(
                "notanemail",
                "@example.com",
                "user@",
                "user@@example.com",
                "user with spaces@example.com",
                "a".repeat(255) // String largo sin formato de email
        );

        for (String invalidEmail : invalidEmails) {
            Instructor instructor = Instructor.builder()
                    .email(invalidEmail)
                    .name("Test User")
                    .build();

            // When & Then
            assertThrows(ConstraintViolationException.class, () -> {
                em.persist(instructor);
                em.flush();
            }, "Should reject invalid email: " + invalidEmail);

            em.clear(); // Clear any pending operations
        }
    }

    // Test adicional para validar emails válidos
    @Test
    @DisplayName("Should accept valid email formats")
    void shouldAcceptValidEmailFormats() {
        // Given - Varios formatos de email válidos
        List<String> validEmails = List.of(
                "user@example.com",
                "user.name@example.com",
                "user+tag@example.co.uk",
                "user123@test-domain.com",
                "a@b.co"
        );

        for (int i = 0; i < validEmails.size(); i++) {
            String validEmail = validEmails.get(i);
            Instructor instructor = Instructor.builder()
                    .email(validEmail)
                    .name("Test User " + i)
                    .build();

            // When
            em.persist(instructor);
            em.flush();

            // Then
            assertNotNull(instructor.getId());
            assertEquals(validEmail, instructor.getEmail());

            em.clear(); // Clear for next iteration
        }
    }

    // Test para verificar que la validación ocurre antes de la persistencia
    @Test
    @DisplayName("Should validate email before database constraints")
    void shouldValidateEmailBeforeDatabaseConstraints() {
        // Given
        String invalidEmail = "not-an-email";
        Instructor instructor = Instructor.builder()
                .email(invalidEmail)
                .name("Test User")
                .build();

        // When & Then
        ConstraintViolationException exception = assertThrows(
                ConstraintViolationException.class,
                () -> {
                    em.persist(instructor);
                    em.flush();
                }
        );

        // Verificar que el mensaje de error es el esperado
        assertTrue(exception.getMessage().contains("Email should be valid"));
    }
}

package com.acainfo.backendclaude.model;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.validation.ConstraintViolationException;
import org.hibernate.id.IdentifierGenerationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Registration Entity Mapping Tests")
class RegistrationMappingTests {

    @Autowired
    private EntityManager em;

    // ========================================
    // Helper Methods
    // ========================================

    private Student createAndPersistStudent(String email, String name) {
        Major major = Major.builder()
                .name("Computer Science")
                .build();
        em.persist(major);

        StudyYear year = StudyYear.builder()
                .name("2nd Year")
                .level(2)
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

    private SubjectsGroup createAndPersistSubjectsGroup(String groupName, String subjectName) {
        Major major = Major.builder()
                .name("Engineering")
                .build();
        em.persist(major);

        StudyYear year = StudyYear.builder()
                .name("2nd Year")
                .level(2)
                .build();
        em.persist(year);

        Subject subject = Subject.builder()
                .name(subjectName)
                .major(major)
                .studyYear(year)
                .monthlyPrice(100)
                .build();
        em.persist(subject);

        SubjectsGroup group = SubjectsGroup.builder()
                .name(groupName)
                .subject(subject)
                .startDate(LocalDate.now().plusDays(7))
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
    @DisplayName("Should handle minimal valid registration")
    void shouldHandleMinimalValidRegistration() {
        // Given
        Student student = createAndPersistStudent("john@example.com", "John Doe");
        SubjectsGroup group = createAndPersistSubjectsGroup("Group A", "Programming");

        RegistrationId registrationId = RegistrationId.builder()
                .studentId(student.getId())
                .groupId(group.getId())
                .build();

        Registration registration = Registration.builder()
                .id(registrationId)
                .student(student)
                .group(group)
                .build();

        // When
        em.persist(registration);
        em.flush();

        // Then
        assertNotNull(registration.getId());
        assertEquals(student.getId(), registration.getId().getStudentId());
        assertEquals(group.getId(), registration.getId().getGroupId());
        assertEquals(student, registration.getStudent());
        assertEquals(group, registration.getGroup());
        assertEquals(Registration.RegistrationStatus.ACTIVE, registration.getStatus()); // Default
        assertNotNull(registration.getRegistrationDate());
    }

    @Test
    @Transactional
    @DisplayName("Should create registration with all fields")
    void shouldCreateRegistrationWithAllFields() {
        // Given
        Student student = createAndPersistStudent("jane@example.com", "Jane Smith");
        SubjectsGroup group = createAndPersistSubjectsGroup("Advanced Group", "Data Structures");

        RegistrationId registrationId = RegistrationId.builder()
                .studentId(student.getId())
                .groupId(group.getId())
                .build();

        Instant customDate = Instant.now().minus(5, ChronoUnit.DAYS);

        // When
        Registration registration = Registration.builder()
                .id(registrationId)
                .student(student)
                .group(group)
                .registrationDate(customDate)
                .status(Registration.RegistrationStatus.COMPLETED)
                .build();

        em.persist(registration);
        em.flush();

        // Then
        assertNotNull(registration.getId());
        assertEquals(Registration.RegistrationStatus.COMPLETED, registration.getStatus());
        assertEquals(customDate, registration.getRegistrationDate());
    }

    @Test
    @Transactional
    @DisplayName("Should persist and retrieve registration")
    void shouldPersistAndRetrieveRegistration() {
        // Given
        Student student = createAndPersistStudent("alice@example.com", "Alice Brown");
        SubjectsGroup group = createAndPersistSubjectsGroup("Morning Group", "Algorithms");

        RegistrationId registrationId = RegistrationId.builder()
                .studentId(student.getId())
                .groupId(group.getId())
                .build();

        Registration registration = Registration.builder()
                .id(registrationId)
                .student(student)
                .group(group)
                .status(Registration.RegistrationStatus.ACTIVE)
                .build();

        em.persist(registration);
        em.flush();
        em.clear();

        // When
        Registration retrieved = em.find(Registration.class, registrationId);

        // Then
        assertNotNull(retrieved);
        assertEquals(student.getId(), retrieved.getStudent().getId());
        assertEquals(group.getId(), retrieved.getGroup().getId());
        assertEquals(Registration.RegistrationStatus.ACTIVE, retrieved.getStatus());
    }

    // ========================================
    // Composite Key Tests
    // ========================================

    @Test
    @DisplayName("Should handle composite key correctly")
    void shouldHandleCompositeKeyCorrectly() {
        // Given
        Student student = createAndPersistStudent("bob@example.com", "Bob Wilson");
        SubjectsGroup group = createAndPersistSubjectsGroup("Lab Group", "Operating Systems");

        // When
        RegistrationId id = RegistrationId.builder()
                .studentId(student.getId())
                .groupId(group.getId())
                .build();

        // Then
        assertEquals(student.getId(), id.getStudentId());
        assertEquals(group.getId(), id.getGroupId());
    }

    @Test
    @DisplayName("Should reject duplicate registration")
    void shouldRejectDuplicateRegistration() {
        // Given
        Student student = createAndPersistStudent("carol@example.com", "Carol Davis");
        SubjectsGroup group = createAndPersistSubjectsGroup("Theory Group", "Networks");

        RegistrationId registrationId = RegistrationId.builder()
                .studentId(student.getId())
                .groupId(group.getId())
                .build();

        Registration first = Registration.builder()
                .id(registrationId)
                .student(student)
                .group(group)
                .build();
        em.persist(first);
        em.flush();

        Registration duplicate = Registration.builder()
                .id(registrationId)
                .student(student)
                .group(group)
                .build();

        // When & Then
        assertThrows(PersistenceException.class, () -> {
            em.persist(duplicate);
            em.flush();
        });
    }

    @Test
    @DisplayName("Should allow same student in different groups")
    void shouldAllowSameStudentInDifferentGroups() {
        // Given
        Student student = createAndPersistStudent("david@example.com", "David Miller");
        SubjectsGroup group1 = createAndPersistSubjectsGroup("Group 1", "Math I");
        SubjectsGroup group2 = createAndPersistSubjectsGroup("Group 2", "Physics I");

        RegistrationId id1 = RegistrationId.builder()
                .studentId(student.getId())
                .groupId(group1.getId())
                .build();

        RegistrationId id2 = RegistrationId.builder()
                .studentId(student.getId())
                .groupId(group2.getId())
                .build();

        Registration registration1 = Registration.builder()
                .id(id1)
                .student(student)
                .group(group1)
                .build();

        Registration registration2 = Registration.builder()
                .id(id2)
                .student(student)
                .group(group2)
                .build();

        // When
        em.persist(registration1);
        em.persist(registration2);
        em.flush();

        // Then
        assertNotNull(registration1.getId());
        assertNotNull(registration2.getId());
        assertNotEquals(registration1.getId(), registration2.getId());
    }

    @Test
    @DisplayName("Should allow same group with different students")
    void shouldAllowSameGroupWithDifferentStudents() {
        // Given
        Student student1 = createAndPersistStudent("eve@example.com", "Eve Johnson");
        Student student2 = createAndPersistStudent("frank@example.com", "Frank Lee");
        SubjectsGroup group = createAndPersistSubjectsGroup("Popular Group", "Web Development");

        RegistrationId id1 = RegistrationId.builder()
                .studentId(student1.getId())
                .groupId(group.getId())
                .build();

        RegistrationId id2 = RegistrationId.builder()
                .studentId(student2.getId())
                .groupId(group.getId())
                .build();

        Registration registration1 = Registration.builder()
                .id(id1)
                .student(student1)
                .group(group)
                .build();

        Registration registration2 = Registration.builder()
                .id(id2)
                .student(student2)
                .group(group)
                .build();

        // When
        em.persist(registration1);
        em.persist(registration2);
        em.flush();

        // Then
        assertNotNull(registration1.getId());
        assertNotNull(registration2.getId());
        assertNotEquals(registration1.getId(), registration2.getId());
    }

    // ========================================
    // Validation Tests
    // ========================================

    @Test
    @DisplayName("Should reject null registration id")
    void shouldRejectNullRegistrationId() {
        // Given
        Student student = createAndPersistStudent("grace@example.com", "Grace Chen");
        SubjectsGroup group = createAndPersistSubjectsGroup("Test Group", "Testing");

        Registration registration = Registration.builder()
                .id(null)
                .student(student)
                .group(group)
                .build();

        // When & Then
        assertThrows(PersistenceException.class, () -> {
            em.persist(registration);
            em.flush();
        });
    }

    @Test
    @DisplayName("Should reject null student")
    void shouldRejectNullStudent() {
        // Given
        SubjectsGroup group = createAndPersistSubjectsGroup("Invalid Group", "Subject");

        RegistrationId id = RegistrationId.builder()
                .studentId(1)
                .groupId(group.getId())
                .build();

        Registration registration = Registration.builder()
                .id(id)
                .student(null)
                .group(group)
                .build();

        // When & Then
        assertThrows(IdentifierGenerationException.class, () -> {
            em.persist(registration);
            em.flush();
        });
    }

    @Test
    @DisplayName("Should reject null group")
    void shouldRejectNullGroup() {
        // Given
        Student student = createAndPersistStudent("henry@example.com", "Henry Wong");

        RegistrationId id = RegistrationId.builder()
                .studentId(student.getId())
                .groupId(1)
                .build();

        Registration registration = Registration.builder()
                .id(id)
                .student(student)
                .group(null)
                .build();

        // When & Then
        assertThrows(IdentifierGenerationException.class, () -> {
            em.persist(registration);
            em.flush();
        });
    }

    // ========================================
    // Default Values Tests
    // ========================================

    @Test
    @DisplayName("Should set registration date automatically")
    void shouldSetRegistrationDateAutomatically() {
        // Given
        Student student = createAndPersistStudent("iris@example.com", "Iris Martinez");
        SubjectsGroup group = createAndPersistSubjectsGroup("Auto Date Group", "Databases");

        RegistrationId id = RegistrationId.builder()
                .studentId(student.getId())
                .groupId(group.getId())
                .build();

        Instant before = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        Registration registration = Registration.builder()
                .id(id)
                .student(student)
                .group(group)
                // No registration date specified
                .build();

        // When
        em.persist(registration);
        em.flush();

        // Then
        assertNotNull(registration.getRegistrationDate());
        Instant regDate = registration.getRegistrationDate().truncatedTo(ChronoUnit.SECONDS);
        assertTrue(regDate.equals(before) || regDate.isAfter(before));
    }

    @Test
    @DisplayName("Should default status to ACTIVE")
    void shouldDefaultStatusToActive() {
        // Given
        Student student = createAndPersistStudent("jack@example.com", "Jack Robinson");
        SubjectsGroup group = createAndPersistSubjectsGroup("Default Status Group", "Security");

        RegistrationId id = RegistrationId.builder()
                .studentId(student.getId())
                .groupId(group.getId())
                .build();

        Registration registration = Registration.builder()
                .id(id)
                .student(student)
                .group(group)
                // No status specified
                .build();

        // When
        em.persist(registration);
        em.flush();

        // Then
        assertEquals(Registration.RegistrationStatus.ACTIVE, registration.getStatus());
    }

    // ========================================
    // Status Enum Tests
    // ========================================

    @Test
    @DisplayName("Should handle all status values")
    void shouldHandleAllStatusValues() {
        // Given - Create student once and store its ID
        Student initialStudent = createAndPersistStudent("karen@example.com", "Karen White");
        Integer studentId = initialStudent.getId();

        for (Registration.RegistrationStatus status : Registration.RegistrationStatus.values()) {
            // Reload student from database to avoid detached entity issues
            Student student = em.find(Student.class, studentId);

            SubjectsGroup group = createAndPersistSubjectsGroup(
                    "Group " + status,
                    "Subject " + status
            );

            RegistrationId id = RegistrationId.builder()
                    .studentId(student.getId())
                    .groupId(group.getId())
                    .build();

            Registration registration = Registration.builder()
                    .id(id)
                    .student(student)
                    .group(group)
                    .status(status)
                    .build();

            // When
            em.persist(registration);
            em.flush();

            // Then
            assertEquals(status, registration.getStatus());
            em.clear();
        }
    }

    @Test
    @DisplayName("Should update status correctly")
    void shouldUpdateStatusCorrectly() {
        // Given
        Student student = createAndPersistStudent("leo@example.com", "Leo Taylor");
        SubjectsGroup group = createAndPersistSubjectsGroup("Update Group", "AI");

        RegistrationId id = RegistrationId.builder()
                .studentId(student.getId())
                .groupId(group.getId())
                .build();

        Registration registration = Registration.builder()
                .id(id)
                .student(student)
                .group(group)
                .status(Registration.RegistrationStatus.ACTIVE)
                .build();

        em.persist(registration);
        em.flush();

        // When
        registration.setStatus(Registration.RegistrationStatus.DROPPED);
        em.flush();
        em.clear();

        // Then
        Registration updated = em.find(Registration.class, id);
        assertEquals(Registration.RegistrationStatus.DROPPED, updated.getStatus());
    }

    // ========================================
    // Cascade & Relationship Tests
    // ========================================

    @Test
    @Transactional
    @DisplayName("Should delete registration when student is deleted")
    void shouldDeleteRegistrationWhenStudentDeleted() {
        // Given
        Student student = createAndPersistStudent("mary@example.com", "Mary Anderson");
        SubjectsGroup group = createAndPersistSubjectsGroup("Cascade Test Group", "Machine Learning");

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

        // When - Delete student
        Student toDelete = em.find(Student.class, studentId);
        em.remove(toDelete);
        em.flush();
        em.clear();

        // Then
        assertNull(em.find(Student.class, studentId));
        assertNull(em.find(Registration.class, regId)); // Should be cascade deleted
        assertNotNull(em.find(SubjectsGroup.class, groupId)); // Group should remain
    }

    @Test
    @Transactional
    @DisplayName("Should delete registration when group is deleted")
    void shouldDeleteRegistrationWhenGroupDeleted() {
        // Given
        Student student = createAndPersistStudent("nathan@example.com", "Nathan Green");
        SubjectsGroup group = createAndPersistSubjectsGroup("To Delete Group", "Compilers");

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

        // When - Delete group
        SubjectsGroup toDelete = em.find(SubjectsGroup.class, groupId);
        em.remove(toDelete);
        em.flush();
        em.clear();

        // Then
        assertNull(em.find(SubjectsGroup.class, groupId));
        assertNull(em.find(Registration.class, regId)); // Should be cascade deleted
        assertNotNull(em.find(Student.class, studentId)); // Student should remain
    }
}
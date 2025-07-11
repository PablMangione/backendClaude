package com.acainfo.backendclaude.model;

import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@UtilityClass
public class TestEntityFactory {

    // ---------------------------------------------------------------------
    // CORE ENTITIES
    // ---------------------------------------------------------------------

    public static Major createMajor(String name) {
        return Major.builder()
                .name(name)
                .description("Test major: " + name)
                .createdAt(Instant.now())
                .build();
    }

    public static StudyYear createStudyYear(String name, Integer level) {
        return StudyYear.builder()
                .name(name)
                .level(level)
                .description("Test study year: " + name)
                .build();
    }

    public static Student createStudent(String email, String name, Major major, StudyYear studyYear) {
        return Student.builder()
                .email(email)
                .name(name)
                .phone("123456789")
                .major(major)
                .studyYear(studyYear)
                .isActive(true)
                .createdAt(Instant.now())
                .build();
    }

    public static Instructor createInstructor(String email, String name) {
        return Instructor.builder()
                .email(email)
                .name(name)
                .phone("987654321")
                .specialization("Test specialization")
                .createdAt(Instant.now())
                .build();
    }

    public static Subject createSubject(String name, Major major, StudyYear studyYear) {
        return Subject.builder()
                .name(name)
                .major(major)
                .studyYear(studyYear)
                .monthlyPrice(100)
                .description("Test subject: " + name)
                .createdAt(Instant.now())
                .build();
    }

    public static SubjectsGroup createSubjectsGroup(String name, Subject subject) {
        return SubjectsGroup.builder()
                .name(name)
                .subject(subject)
                .startDate(LocalDate.of(2025, 9, 1))
                .endDate(LocalDate.of(2025, 12, 31))
                .maxCapacity(30)
                .status(SubjectsGroup.GroupStatus.PLANNED)
                .createdAt(Instant.now())
                .build();
    }

    // Con instructor
    public static SubjectsGroup createSubjectsGroup(String name, Subject subject, Instructor instructor) {
        return SubjectsGroup.builder()
                .name(name)
                .subject(subject)
                .instructor(instructor)
                .startDate(LocalDate.of(2025, 9, 1))
                .endDate(LocalDate.of(2025, 12, 31))
                .maxCapacity(30)
                .status(SubjectsGroup.GroupStatus.PLANNED)
                .createdAt(Instant.now())
                .build();
    }

    // ---------------------------------------------------------------------
    // RELATIONSHIP ENTITIES
    // ---------------------------------------------------------------------

    public static Registration createRegistration(Student student, SubjectsGroup group) {
        RegistrationId registrationId = RegistrationId.builder()
                .studentId(student.getId())
                .groupId(group.getId())
                .build();

        return Registration.builder()
                .id(registrationId)
                .student(student)
                .group(group)
                .registrationDate(Instant.now())
                .status(Registration.RegistrationStatus.ACTIVE)
                .build();
    }

    public static Session createSession(SubjectsGroup group, Session.DayOfWeek dayOfWeek, LocalTime startTime) {
        return Session.builder()
                .subjectsGroup(group)
                .dayOfWeek(dayOfWeek)
                .startTime(startTime)
                .durationMinutes(120)
                .isActive(true)
                .build();
    }

    public static SessionInstance createSessionInstance(Session session, LocalDate sessionDate) {
        return SessionInstance.builder()
                .session(session)
                .sessionDate(sessionDate)
                .actualStartTime(session.getStartTime())
                .actualEndTime(session.getStartTime().plusMinutes(session.getDurationMinutes()))
                .status(SessionInstance.SessionStatus.SCHEDULED)
                .createdAt(Instant.now())
                .build();
    }

    public static Attendance createAttendance(Student student, SessionInstance sessionInstance) {
        return Attendance.builder()
                .student(student)
                .sessionInstance(sessionInstance)
                .status(Attendance.AttendanceStatus.PRESENT)
                .markedAt(Instant.now())
                .build();
    }

    public static Incident createIncident(SessionInstance sessionInstance, Incident.IncidentType type, String description) {
        return Incident.builder()
                .sessionInstance(sessionInstance)
                .incidentType(type)
                .description(description)
                .reportedBy("Test Reporter")
                .reportedAt(Instant.now())
                .build();
    }

    // ---------------------------------------------------------------------
    // BUILDER VARIANTS WITH CUSTOM PARAMETERS
    // ---------------------------------------------------------------------

    public static Student createStudentWithCustomPhone(String email, String name, String phone, Major major, StudyYear studyYear) {
        return Student.builder()
                .email(email)
                .name(name)
                .phone(phone)
                .major(major)
                .studyYear(studyYear)
                .isActive(true)
                .createdAt(Instant.now())
                .build();
    }

    public static Subject createSubjectWithPrice(String name, Major major, StudyYear studyYear, Integer price) {
        return Subject.builder()
                .name(name)
                .major(major)
                .studyYear(studyYear)
                .monthlyPrice(price)
                .description("Test subject: " + name)
                .createdAt(Instant.now())
                .build();
    }

    public static SubjectsGroup createSubjectsGroupWithDates(String name, Subject subject, LocalDate startDate, LocalDate endDate) {
        return SubjectsGroup.builder()
                .name(name)
                .subject(subject)
                .startDate(startDate)
                .endDate(endDate)
                .maxCapacity(30)
                .status(SubjectsGroup.GroupStatus.PLANNED)
                .createdAt(Instant.now())
                .build();
    }
}
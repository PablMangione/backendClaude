package com.acainfo.backendclaude.mapper;

import com.acainfo.backendclaude.dto.major.*;
import com.acainfo.backendclaude.model.Major;
import com.acainfo.backendclaude.model.Student;
import com.acainfo.backendclaude.model.Subject;
import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper utility class para convertir entre entidades Major y DTOs
 */
@UtilityClass
public class MajorMapper {

    // ========================================
    // ENTITY TO DTO MAPPINGS
    // ========================================

    /**
     * Convierte Major entity a MajorDto básico
     */
    public static MajorDto toDto(Major major) {
        if (major == null) {
            return null;
        }

        return MajorDto.builder()
                .id(major.getId())
                .name(major.getName())
                .description(major.getDescription())
                .createdAt(major.getCreatedAt())
                .build();
    }

    /**
     * Convierte lista de Major entities a lista de MajorDto
     */
    public static List<MajorDto> toDtoList(List<Major> majors) {
        if (majors == null) {
            return List.of();
        }

        return majors.stream()
                .map(MajorMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Convierte Major entity a MajorDetailDto con entidades relacionadas
     */
    public static MajorDetailDto toDetailDto(Major major) {
        if (major == null) {
            return null;
        }

        return MajorDetailDto.builder()
                .id(major.getId())
                .name(major.getName())
                .description(major.getDescription())
                .createdAt(major.getCreatedAt())
                .students(mapStudentsToBasicDto(major.getStudents()))
                .subjects(mapSubjectsToBasicDto(major.getSubjects()))
                .build();
    }

    /**
     * Convierte Major entity a MajorSummaryDto (para casos donde no usamos query personalizada)
     */
    public static MajorSummaryDto toSummaryDto(Major major) {
        if (major == null) {
            return null;
        }

        long totalStudents = major.getStudents() != null ? major.getStudents().size() : 0;
        long activeStudents = major.getStudents() != null ?
                major.getStudents().stream()
                        .filter(Student::getIsActive)
                        .count() : 0;
        long totalSubjects = major.getSubjects() != null ? major.getSubjects().size() : 0;

        return MajorSummaryDto.builder()
                .id(major.getId())
                .name(major.getName())
                .description(major.getDescription())
                .createdAt(major.getCreatedAt())
                .totalStudents(totalStudents)
                .totalSubjects(totalSubjects)
                .activeStudents(activeStudents)
                .build();
    }

    // ========================================
    // DTO TO ENTITY MAPPINGS
    // ========================================

    /**
     * Convierte MajorCreateDto a Major entity
     */
    public static Major toEntity(MajorCreateDto createDto) {
        if (createDto == null) {
            return null;
        }

        return Major.builder()
                .name(createDto.getName())
                .description(createDto.getDescription())
                .createdAt(Instant.now())
                .build();
    }

    /**
     * Actualiza Major entity con datos de MajorUpdateDto
     */
    public static void updateEntity(Major major, MajorUpdateDto updateDto) {
        if (major == null || updateDto == null) {
            return;
        }

        major.setName(updateDto.getName());
        major.setDescription(updateDto.getDescription());
    }

    /**
     * Crea un nuevo Major entity a partir de MajorUpdateDto
     */
    public static Major toEntity(MajorUpdateDto updateDto) {
        if (updateDto == null) {
            return null;
        }

        return Major.builder()
                .name(updateDto.getName())
                .description(updateDto.getDescription())
                .build();
    }

    // ========================================
    // HELPER METHODS
    // ========================================

    /**
     * Mapea collection de Students a lista de StudentBasicDto
     */
    private static List<MajorDetailDto.StudentBasicDto> mapStudentsToBasicDto(
            java.util.Set<Student> students) {
        if (students == null) {
            return List.of();
        }

        return students.stream()
                .map(student -> MajorDetailDto.StudentBasicDto.builder()
                        .id(student.getId())
                        .name(student.getName())
                        .email(student.getEmail())
                        .isActive(student.getIsActive())
                        .studyYearName(student.getStudyYear() != null ?
                                student.getStudyYear().getName() : null)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Mapea collection de Subjects a lista de SubjectBasicDto
     */
    private static List<MajorDetailDto.SubjectBasicDto> mapSubjectsToBasicDto(
            java.util.Set<Subject> subjects) {
        if (subjects == null) {
            return List.of();
        }

        return subjects.stream()
                .map(subject -> MajorDetailDto.SubjectBasicDto.builder()
                        .id(subject.getId())
                        .name(subject.getName())
                        .monthlyPrice(subject.getMonthlyPrice())
                        .studyYearName(subject.getStudyYear() != null ?
                                subject.getStudyYear().getName() : null)
                        .build())
                .collect(Collectors.toList());
    }

    // ========================================
    // VALIDATION HELPERS
    // ========================================

    /**
     * Valida si un MajorCreateDto tiene datos válidos
     */
    public static boolean isValidCreateDto(MajorCreateDto createDto) {
        return createDto != null &&
                createDto.getName() != null &&
                !createDto.getName().trim().isEmpty();
    }

    /**
     * Valida si un MajorUpdateDto tiene datos válidos
     */
    public static boolean isValidUpdateDto(MajorUpdateDto updateDto) {
        return updateDto != null &&
                updateDto.getName() != null &&
                !updateDto.getName().trim().isEmpty();
    }

    // ========================================
    // UTILITY METHODS
    // ========================================

    /**
     * Crea un MajorDto mínimo con solo ID y nombre
     */
    public static MajorDto createMinimalDto(Integer id, String name) {
        return MajorDto.builder()
                .id(id)
                .name(name)
                .build();
    }

    /**
     * Verifica si dos Major entities son equivalentes (por nombre)
     */
    public static boolean areEquivalent(Major major1, Major major2) {
        if (major1 == null && major2 == null) {
            return true;
        }
        if (major1 == null || major2 == null) {
            return false;
        }

        return major1.getName() != null &&
                major1.getName().equalsIgnoreCase(major2.getName());
    }
}

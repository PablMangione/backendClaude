package com.acainfo.backendclaude.mapper;

import com.acainfo.backendclaude.dto.studyyear.*;
import com.acainfo.backendclaude.model.Student;
import com.acainfo.backendclaude.model.StudyYear;
import com.acainfo.backendclaude.model.Subject;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper utility class para convertir entre entidades StudyYear y DTOs
 */
@UtilityClass
public class StudyYearMapper {

    // ========================================
    // ENTITY TO DTO MAPPINGS
    // ========================================

    /**
     * Convierte StudyYear entity a StudyYearDto básico
     */
    public static StudyYearDto toDto(StudyYear studyYear) {
        if (studyYear == null) {
            return null;
        }

        return StudyYearDto.builder()
                .id(studyYear.getId())
                .name(studyYear.getName())
                .level(studyYear.getLevel())
                .description(studyYear.getDescription())
                .build();
    }

    /**
     * Convierte lista de StudyYear entities a lista de StudyYearDto
     */
    public static List<StudyYearDto> toDtoList(List<StudyYear> studyYears) {
        if (studyYears == null) {
            return List.of();
        }

        return studyYears.stream()
                .map(StudyYearMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Convierte StudyYear entity a StudyYearDetailDto con entidades relacionadas
     */
    public static StudyYearDetailDto toDetailDto(StudyYear studyYear) {
        if (studyYear == null) {
            return null;
        }

        return StudyYearDetailDto.builder()
                .id(studyYear.getId())
                .name(studyYear.getName())
                .level(studyYear.getLevel())
                .description(studyYear.getDescription())
                .students(mapStudentsToBasicDto(studyYear.getStudents()))
                .subjects(mapSubjectsToBasicDto(studyYear.getSubjects()))
                .build();
    }

    /**
     * Convierte StudyYear entity a StudyYearSummaryDto (para casos donde no usamos query personalizada)
     */
    public static StudyYearSummaryDto toSummaryDto(StudyYear studyYear) {
        if (studyYear == null) {
            return null;
        }

        long totalStudents = studyYear.getStudents() != null ? studyYear.getStudents().size() : 0;
        long activeStudents = studyYear.getStudents() != null ?
                studyYear.getStudents().stream()
                        .filter(Student::getIsActive)
                        .count() : 0;
        long totalSubjects = studyYear.getSubjects() != null ? studyYear.getSubjects().size() : 0;
        long totalMajorsWithStudents = studyYear.getStudents() != null ?
                studyYear.getStudents().stream()
                        .map(student -> student.getMajor().getId())
                        .distinct()
                        .count() : 0;
        double averageSubjectPrice = studyYear.getSubjects() != null ?
                studyYear.getSubjects().stream()
                        .mapToInt(Subject::getMonthlyPrice)
                        .average()
                        .orElse(0.0) : 0.0;

        return StudyYearSummaryDto.builder()
                .id(studyYear.getId())
                .name(studyYear.getName())
                .level(studyYear.getLevel())
                .description(studyYear.getDescription())
                .totalStudents(totalStudents)
                .totalSubjects(totalSubjects)
                .activeStudents(activeStudents)
                .totalMajorsWithStudents(totalMajorsWithStudents)
                .averageSubjectPrice(averageSubjectPrice)
                .build();
    }

    /**
     * Mapea Object[] de consulta nativa a StudyYearSummaryDto
     */
    public static StudyYearSummaryDto mapNativeResultToSummaryDto(Object[] row) {
        if (row == null || row.length < 9) {
            return null;
        }

        Integer id = (Integer) row[0];
        String name = (String) row[1];
        Integer level = (Integer) row[2];
        String description = (String) row[3];
        Long totalStudents = row[4] != null ? ((BigInteger) row[4]).longValue() : 0L;
        Long totalSubjects = row[5] != null ? ((BigInteger) row[5]).longValue() : 0L;
        Long activeStudents = row[6] != null ? ((BigInteger) row[6]).longValue() : 0L;
        Long totalMajorsWithStudents = row[7] != null ? ((BigInteger) row[7]).longValue() : 0L;
        Double averageSubjectPrice = row[8] != null ? ((BigDecimal) row[8]).doubleValue() : 0.0;

        return new StudyYearSummaryDto(id, name, level, description,
                totalStudents, totalSubjects, activeStudents,
                totalMajorsWithStudents, averageSubjectPrice);
    }

    // ========================================
    // DTO TO ENTITY MAPPINGS
    // ========================================

    /**
     * Convierte StudyYearCreateDto a StudyYear entity
     */
    public static StudyYear toEntity(StudyYearCreateDto createDto) {
        if (createDto == null) {
            return null;
        }

        return StudyYear.builder()
                .name(createDto.getName())
                .level(createDto.getLevel())
                .description(createDto.getDescription())
                .build();
    }

    /**
     * Actualiza StudyYear entity con datos de StudyYearUpdateDto
     */
    public static void updateEntity(StudyYear studyYear, StudyYearUpdateDto updateDto) {
        if (studyYear == null || updateDto == null) {
            return;
        }

        studyYear.setName(updateDto.getName());
        studyYear.setLevel(updateDto.getLevel());
        studyYear.setDescription(updateDto.getDescription());
    }

    /**
     * Crea un nuevo StudyYear entity a partir de StudyYearUpdateDto
     */
    public static StudyYear toEntity(StudyYearUpdateDto updateDto) {
        if (updateDto == null) {
            return null;
        }

        return StudyYear.builder()
                .name(updateDto.getName())
                .level(updateDto.getLevel())
                .description(updateDto.getDescription())
                .build();
    }

    // ========================================
    // HELPER METHODS
    // ========================================

    /**
     * Mapea collection de Students a lista de StudentBasicDto
     */
    private static List<StudyYearDetailDto.StudentBasicDto> mapStudentsToBasicDto(
            java.util.Set<Student> students) {
        if (students == null) {
            return List.of();
        }

        return students.stream()
                .map(student -> StudyYearDetailDto.StudentBasicDto.builder()
                        .id(student.getId())
                        .name(student.getName())
                        .email(student.getEmail())
                        .isActive(student.getIsActive())
                        .majorName(student.getMajor() != null ?
                                student.getMajor().getName() : null)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Mapea collection de Subjects a lista de SubjectBasicDto
     */
    private static List<StudyYearDetailDto.SubjectBasicDto> mapSubjectsToBasicDto(
            java.util.Set<Subject> subjects) {
        if (subjects == null) {
            return List.of();
        }

        return subjects.stream()
                .map(subject -> StudyYearDetailDto.SubjectBasicDto.builder()
                        .id(subject.getId())
                        .name(subject.getName())
                        .monthlyPrice(subject.getMonthlyPrice())
                        .majorName(subject.getMajor() != null ?
                                subject.getMajor().getName() : null)
                        .build())
                .collect(Collectors.toList());
    }

    // ========================================
    // VALIDATION HELPERS
    // ========================================

    /**
     * Valida si un StudyYearCreateDto tiene datos válidos
     */
    public static boolean isValidCreateDto(StudyYearCreateDto createDto) {
        return createDto != null &&
                createDto.getName() != null &&
                !createDto.getName().trim().isEmpty() &&
                createDto.getLevel() != null;
    }

    /**
     * Valida si un StudyYearUpdateDto tiene datos válidos
     */
    public static boolean isValidUpdateDto(StudyYearUpdateDto updateDto) {
        return updateDto != null &&
                updateDto.getName() != null &&
                !updateDto.getName().trim().isEmpty() &&
                updateDto.getLevel() != null;
    }

    // ========================================
    // UTILITY METHODS
    // ========================================

    /**
     * Crea un StudyYearDto mínimo con solo ID, nombre y nivel
     */
    public static StudyYearDto createMinimalDto(Integer id, String name, Integer level) {
        return StudyYearDto.builder()
                .id(id)
                .name(name)
                .level(level)
                .build();
    }

    /**
     * Verifica si dos StudyYear entities son equivalentes (por nombre y nivel)
     */
    public static boolean areEquivalent(StudyYear studyYear1, StudyYear studyYear2) {
        if (studyYear1 == null && studyYear2 == null) {
            return true;
        }
        if (studyYear1 == null || studyYear2 == null) {
            return false;
        }

        return studyYear1.getName() != null &&
                studyYear1.getName().equalsIgnoreCase(studyYear2.getName()) &&
                studyYear1.getLevel() != null &&
                studyYear1.getLevel().equals(studyYear2.getLevel());
    }

    /**
     * Compara StudyYears por nivel (útil para ordenamientos)
     */
    public static int compareByLevel(StudyYear sy1, StudyYear sy2) {
        if (sy1 == null && sy2 == null) return 0;
        if (sy1 == null) return -1;
        if (sy2 == null) return 1;
        if (sy1.getLevel() == null && sy2.getLevel() == null) return 0;
        if (sy1.getLevel() == null) return -1;
        if (sy2.getLevel() == null) return 1;

        return Integer.compare(sy1.getLevel(), sy2.getLevel());
    }

    /**
     * Obtiene una descripción formateada del StudyYear
     */
    public static String getFormattedDescription(StudyYear studyYear) {
        if (studyYear == null) {
            return "";
        }

        String name = studyYear.getName() != null ? studyYear.getName() : "Unknown";
        String level = studyYear.getLevel() != null ? " (Level " + studyYear.getLevel() + ")" : "";

        return name + level;
    }

    // ========================================
    // STATISTICS MAPPING HELPERS
    // ========================================

    /**
     * Mapea Object[] de estadísticas generales a DTO
     */
    public static StudyYearStatisticsDto mapStatisticsToDto(Object[] stats) {
        if (stats == null || stats.length < 8) {
            return StudyYearStatisticsDto.builder().build();
        }

        return StudyYearStatisticsDto.builder()
                .totalStudyYears(stats[0] != null ? ((BigInteger) stats[0]).longValue() : 0L)
                .totalStudents(stats[1] != null ? ((BigInteger) stats[1]).longValue() : 0L)
                .activeStudents(stats[2] != null ? ((BigInteger) stats[2]).longValue() : 0L)
                .totalSubjects(stats[3] != null ? ((BigInteger) stats[3]).longValue() : 0L)
                .averageSubjectPrice(stats[4] != null ? ((BigDecimal) stats[4]).doubleValue() : 0.0)
                .minLevel(stats[5] != null ? (Integer) stats[5] : 0)
                .maxLevel(stats[6] != null ? (Integer) stats[6] : 0)
                .averageLevel(stats[7] != null ? ((BigDecimal) stats[7]).doubleValue() : 0.0)
                .build();
    }

    /**
     * DTO para estadísticas de StudyYear
     */
    @lombok.Value
    @lombok.Builder
    public static class StudyYearStatisticsDto {
        Long totalStudyYears;
        Long totalStudents;
        Long activeStudents;
        Long totalSubjects;
        Double averageSubjectPrice;
        Integer minLevel;
        Integer maxLevel;
        Double averageLevel;
    }

    /**
     * DTO para distribución por nivel
     */
    @lombok.Value
    @lombok.Builder
    public static class LevelDistributionDto {
        Integer level;
        String name;
        Long totalStudents;
        Long activeStudents;
        Long totalSubjects;
    }

    /**
     * Mapea Object[] de distribución por nivel a DTO
     */
    public static LevelDistributionDto mapLevelDistributionToDto(Object[] row) {
        if (row == null || row.length < 5) {
            return null;
        }

        return LevelDistributionDto.builder()
                .level((Integer) row[0])
                .name((String) row[1])
                .totalStudents(row[2] != null ? ((BigInteger) row[2]).longValue() : 0L)
                .activeStudents(row[3] != null ? ((BigInteger) row[3]).longValue() : 0L)
                .totalSubjects(row[4] != null ? ((BigInteger) row[4]).longValue() : 0L)
                .build();
    }

    /**
     * DTO para rangos de nivel
     */
    @lombok.Value
    @lombok.Builder
    public static class LevelRangeDistributionDto {
        String levelRange;
        Long studyYearCount;
        Long totalStudents;
        Long activeStudents;
    }

    /**
     * Mapea Object[] de distribución por rango de nivel a DTO
     */
    public static LevelRangeDistributionDto mapLevelRangeDistributionToDto(Object[] row) {
        if (row == null || row.length < 4) {
            return null;
        }

        return LevelRangeDistributionDto.builder()
                .levelRange((String) row[0])
                .studyYearCount(row[1] != null ? ((BigInteger) row[1]).longValue() : 0L)
                .totalStudents(row[2] != null ? ((BigInteger) row[2]).longValue() : 0L)
                .activeStudents(row[3] != null ? ((BigInteger) row[3]).longValue() : 0L)
                .build();
    }
}
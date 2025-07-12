package com.acainfo.backendclaude.dto.studyyear;

/**
 * DTO que representa un StudyYear con estadísticas de materias y estudiantes
 */
public record StudyYearWithSubjectStatsDto(
        Integer id,
        String name,
        Integer level,
        String description,
        Long totalSubjects,
        Long activeStudentCount
) {
    /**
     * Verifica si tiene materias
     */
    public boolean hasSubjects() {
        return totalSubjects != null && totalSubjects > 0;
    }

    /**
     * Verifica si tiene estudiantes activos
     */
    public boolean hasActiveStudents() {
        return activeStudentCount != null && activeStudentCount > 0;
    }

    /**
     * Calcula ratio estudiantes/materias
     */
    public Double getStudentToSubjectRatio() {
        if (totalSubjects == null || totalSubjects == 0) return 0.0;
        return activeStudentCount.doubleValue() / totalSubjects.doubleValue();
    }

    /**
     * Verifica si es un año académico viable (tiene materias Y estudiantes)
     */
    public boolean isViableAcademicYear() {
        return hasSubjects() && hasActiveStudents();
    }
}

package com.acainfo.backendclaude.dto.studyyear;

/**
 * DTO que representa la distribución de estudiantes por StudyYear
 */
public record StudyYearDistributionDto(
        Integer id,
        String name,
        Integer level,
        Long activeStudentCount,
        Double percentage
) {
    /**
     * Verifica si tiene estudiantes
     */
    public boolean hasStudents() {
        return activeStudentCount != null && activeStudentCount > 0;
    }

    /**
     * Verifica si es un nivel mayoritario (>= 20% de estudiantes)
     */
    public boolean isMajorityLevel() {
        return percentage != null && percentage >= 20.0;
    }

    /**
     * Verifica si es un nivel minoritario (< 10% de estudiantes)
     */
    public boolean isMinorityLevel() {
        return percentage != null && percentage < 10.0;
    }

    /**
     * Obtiene descripción del porcentaje formateada
     */
    public String getFormattedPercentage() {
        if (percentage == null) return "0.00%";
        return String.format("%.2f%%", percentage);
    }

    /**
     * Categoriza el nivel según su población
     */
    public String getPopulationCategory() {
        if (percentage == null || percentage == 0.0) return "EMPTY";
        if (percentage < 10.0) return "LOW";
        if (percentage < 25.0) return "MEDIUM";
        if (percentage < 50.0) return "HIGH";
        return "VERY_HIGH";
    }
}

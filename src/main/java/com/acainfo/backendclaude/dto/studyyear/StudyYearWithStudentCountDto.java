package com.acainfo.backendclaude.dto.studyyear;

/**
 * DTO que representa un StudyYear con el conteo de estudiantes activos
 * Usamos Record (Java 14+) para inmutabilidad y menos boilerplate
 */
public record StudyYearWithStudentCountDto(
        Integer id,
        String name,
        Integer level,
        String description,
        Long activeStudentCount
) {
    /**
     * Constructor de conveniencia para casos donde no hay estudiantes
     */
    public StudyYearWithStudentCountDto(Integer id, String name, Integer level, String description) {
        this(id, name, level, description, 0L);
    }

    /**
     * Método de utilidad para verificar si tiene estudiantes activos
     */
    public boolean hasActiveStudents() {
        return activeStudentCount != null && activeStudentCount > 0;
    }

    /**
     * Método de utilidad para obtener descripción del nivel
     */
    public String getLevelDescription() {
        if (level == null) return "Sin nivel";

        return switch (level) {
            case 1 -> "Primer año";
            case 2 -> "Segundo año";
            case 3 -> "Tercer año";
            case 4 -> "Cuarto año";
            case 5 -> "Quinto año";
            default -> level + "º año";
        };
    }

    /**
     * Verifica si es un nivel básico (1º o 2º año)
     */
    public boolean isBasicLevel() {
        return level != null && level <= 2;
    }

    /**
     * Verifica si es un nivel avanzado (4º año o superior)
     */
    public boolean isAdvancedLevel() {
        return level != null && level >= 4;
    }
}
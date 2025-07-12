package com.acainfo.backendclaude.dto.major;

/**
 * DTO que representa un Major con el conteo de estudiantes activos
 * Usamos Record (Java 14+) para inmutabilidad y menos boilerplate
 */
public record MajorWithStudentCountDto(
        Integer id,
        String name,
        String description,
        Long activeStudentCount
) {
    /**
     * Constructor de conveniencia para casos donde no hay estudiantes
     */
    public MajorWithStudentCountDto(Integer id, String name, String description) {
        this(id, name, description, 0L);
    }

    /**
     * Método de utilidad para verificar si tiene estudiantes
     */
    public boolean hasActiveStudents() {
        return activeStudentCount != null && activeStudentCount > 0;
    }
}
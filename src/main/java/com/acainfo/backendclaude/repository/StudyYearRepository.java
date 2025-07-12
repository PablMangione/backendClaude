package com.acainfo.backendclaude.repository;

import com.acainfo.backendclaude.dto.studyyear.StudyYearDistributionDto;
import com.acainfo.backendclaude.dto.studyyear.StudyYearWithStudentCountDto;
import com.acainfo.backendclaude.dto.studyyear.StudyYearWithSubjectStatsDto;
import com.acainfo.backendclaude.model.StudyYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudyYearRepository extends JpaRepository<StudyYear, Integer> {
    // 1. Búsqueda por nombre
    List<StudyYear> findByNameContainingIgnoreCase(String name);

    // 2. Verificar existencia por nivel
    boolean existsByLevel(Integer level);

    // 3. Encontrar por nivel exacto
    Optional<StudyYear> findByLevel(Integer level);

    // 4. Ordenar por nivel ascendente
    List<StudyYear> findAllByOrderByLevelAsc();

    // 5. Contar estudiantes activos por StudyYear
    @Query("SELECT COUNT(s) FROM Student s WHERE s.studyYear.id = :studyYearId AND s.isActive = true")
    Long countActiveStudentsByStudyYearId(@Param("studyYearId") Integer studyYearId);

    // 6. Contar materias por StudyYear
    @Query("SELECT COUNT(s) FROM Subject s WHERE s.studyYear.id = :studyYearId")
    Long countSubjectsByStudyYearId(@Param("studyYearId") Integer studyYearId);

    // 7. StudyYears con conteo de estudiantes activos
    @Query("""
    SELECT new com.acainfo.backendclaude.dto.studyyear.StudyYearWithStudentCountDto(
        sy.id, sy.name, sy.level, sy.description,
        CAST(COUNT(s.id) AS long)
    )
    FROM StudyYear sy
    LEFT JOIN sy.students s ON s.isActive = true
    GROUP BY sy.id, sy.name, sy.level, sy.description
    ORDER BY sy.level ASC
    """)
    List<StudyYearWithStudentCountDto> findAllStudyYearsWithActiveStudentCount();

    /** 8.
     * Obtiene study years sin estudiantes (activos o inactivos)
     * Útil para identificar años académicos sin uso
     */
    @Query("""
    SELECT sy
    FROM StudyYear sy
    LEFT JOIN sy.students s
    WHERE s IS NULL
    ORDER BY sy.level ASC
    """)
    List<StudyYear> findEmptyStudyYears();

    /** 9
     * Obtiene study years que tienen al menos un estudiante activo
     * Útil para filtrar años académicos "vivos" o activos
     */
    @Query("""
    SELECT DISTINCT sy
    FROM StudyYear sy
    JOIN sy.students s
    WHERE s.isActive = true
    ORDER BY sy.level ASC
    """)
    List<StudyYear> findStudyYearsWithActiveStudents();

    /** 10
     * Obtiene el study year con más estudiantes activos
     * Útil para identificar niveles de mayor demanda
     */
    @Query("""
    SELECT sy
    FROM StudyYear sy
    LEFT JOIN sy.students s ON s.isActive = true
    GROUP BY sy
    ORDER BY COUNT(s) DESC
    LIMIT 1
    """)
    Optional<StudyYear> findMostPopularStudyYear();

    /** 11
     * Años dentro de un rango de niveles
     * Útil para filtros administrativos (ej: "cursos básicos 1-2", "cursos avanzados 4-5")
     */
    @Query("""
    SELECT sy
    FROM StudyYear sy
    WHERE sy.level >= :minLevel AND sy.level <= :maxLevel
    ORDER BY sy.level ASC
    """)
    List<StudyYear> findByLevelRange(@Param("minLevel") Integer minLevel, @Param("maxLevel") Integer maxLevel);

    /** 11
     * Cuenta cuántos study years tienen al menos N estudiantes activos
     * Útil para métricas de viabilidad académica
     */
    @Query("""
    SELECT COUNT(DISTINCT sy.id)
    FROM StudyYear sy
    WHERE sy.id IN (
        SELECT sy2.id
        FROM StudyYear sy2
        JOIN sy2.students s2
        WHERE s2.isActive = true
        GROUP BY sy2.id
        HAVING COUNT(s2) >= :minStudents
    )
    """)
    Long countStudyYearsWithMinimumActiveStudents(@Param("minStudents") Long minStudents);

    /**
     * Obtiene todos los study years con estadísticas de materias
     * Útil para análisis académico y planificación curricular
     */
    @Query("""
    SELECT new com.acainfo.backendclaude.dto.studyyear.StudyYearWithSubjectStatsDto(
        sy.id,
        sy.name,
        sy.level,
        sy.description,
        CAST(COUNT(DISTINCT s.id) AS long),
        CAST(COUNT(DISTINCT st.id) AS long)
    )
    FROM StudyYear sy
    LEFT JOIN sy.students st ON st.isActive = true
    LEFT JOIN sy.subjects s
    GROUP BY sy.id, sy.name, sy.level, sy.description
    ORDER BY sy.level ASC
    """)
    List<StudyYearWithSubjectStatsDto> findAllStudyYearsWithSubjectStats();




}
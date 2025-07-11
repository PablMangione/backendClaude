package com.acainfo.backendclaude.repository;

import com.acainfo.backendclaude.dto.studyyear.*;
import com.acainfo.backendclaude.model.StudyYear;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyYearRepository extends JpaRepository<StudyYear, Integer> {

    // ========================================
    // BASIC CRUD WITH ENTITY GRAPHS
    // ========================================

    /**
     * Encuentra un StudyYear por ID con estudiantes y materias cargados
     */
    @EntityGraph(attributePaths = {"students", "subjects"})
    @Query("SELECT sy FROM StudyYear sy WHERE sy.id = :id")
    Optional<StudyYear> findByIdWithDetails(@Param("id") Integer id);

    /**
     * Encuentra un StudyYear por ID solo con estudiantes
     */
    @EntityGraph(attributePaths = {"students", "students.major"})
    @Query("SELECT sy FROM StudyYear sy WHERE sy.id = :id")
    Optional<StudyYear> findByIdWithStudents(@Param("id") Integer id);

    /**
     * Encuentra un StudyYear por ID solo con materias
     */
    @EntityGraph(attributePaths = {"subjects", "subjects.major"})
    @Query("SELECT sy FROM StudyYear sy WHERE sy.id = :id")
    Optional<StudyYear> findByIdWithSubjects(@Param("id") Integer id);

    // ========================================
    // DERIVED QUERIES
    // ========================================

    /**
     * Busca study years por nombre (case-insensitive)
     */
    List<StudyYear> findByNameContainingIgnoreCase(String name);

    /**
     * Busca study year por nombre exacto (case-insensitive)
     */
    Optional<StudyYear> findByNameIgnoreCase(String name);

    /**
     * Verifica si existe un study year con el nombre dado
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Encuentra study years por nivel
     */
    List<StudyYear> findByLevel(Integer level);

    /**
     * Encuentra study years por rango de niveles
     */
    List<StudyYear> findByLevelBetween(Integer minLevel, Integer maxLevel);

    /**
     * Encuentra study years con nivel mayor que
     */
    List<StudyYear> findByLevelGreaterThan(Integer level);

    /**
     * Encuentra study years con nivel menor que
     */
    List<StudyYear> findByLevelLessThan(Integer level);

    /**
     * Encuentra study years ordenados por nivel
     */
    List<StudyYear> findAllByOrderByLevelAsc();

    /**
     * Encuentra study years ordenados por nivel descendente
     */
    List<StudyYear> findAllByOrderByLevelDesc();

    // ========================================
    // PAGINATED QUERIES
    // ========================================

    /**
     * Busca study years por nombre con paginación
     */
    Page<StudyYear> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Obtiene todos los study years ordenados por nivel
     */
    Page<StudyYear> findAllByOrderByLevelAsc(Pageable pageable);

    /**
     * Busca study years por nivel con paginación
     */
    Page<StudyYear> findByLevel(Integer level, Pageable pageable);

    // ========================================
    // CUSTOM JPQL QUERIES
    // ========================================

    /**
     * Obtiene información de resumen de todos los study years - Versión JPQL con subconsultas
     */
    @Query(
            value = """
        SELECT
            sy.id,
            sy.name,
            sy.level,
            sy.description,
            (SELECT COUNT(DISTINCT s1.id)
             FROM students s1
             WHERE s1.study_year_id = sy.id)                           AS totalStudents,
            (SELECT COUNT(DISTINCT sub1.id)
             FROM subjects sub1
             WHERE sub1.study_year_id = sy.id)                         AS totalSubjects,
            (SELECT COUNT(DISTINCT s2.id)
             FROM students s2
             WHERE s2.study_year_id = sy.id
               AND s2.is_active = TRUE)                                AS activeStudents,
            (SELECT COUNT(DISTINCT s3.major_id)
             FROM students s3
             WHERE s3.study_year_id = sy.id)                           AS distinctMajors,
            (SELECT AVG(sub2.monthly_price)
             FROM subjects sub2
             WHERE sub2.study_year_id = sy.id)                         AS avgSubjectPrice
        FROM study_years sy
        ORDER BY sy.level
        """,
            nativeQuery = true
    )
    List<StudyYearSummaryDto> findAllWithSummary();

    /**
     * Obtiene información de resumen de un study year específico
     */
    @Query(
            value = """
        SELECT
            sy.id,
            sy.name,
            sy.level,
            sy.description,
            (SELECT COUNT(DISTINCT s1.id)
             FROM students s1
             WHERE s1.study_year_id = sy.id)                             AS totalStudents,
            (SELECT COUNT(DISTINCT sub1.id)
             FROM subjects sub1
             WHERE sub1.study_year_id = sy.id)                           AS totalSubjects,
            (SELECT COUNT(DISTINCT s2.id)
             FROM students s2
             WHERE s2.study_year_id = sy.id
               AND s2.is_active = TRUE)                                  AS activeStudents,
            (SELECT COUNT(DISTINCT s3.major_id)
             FROM students s3
             WHERE s3.study_year_id = sy.id)                             AS distinctMajors,
            (SELECT AVG(sub2.monthly_price)
             FROM subjects sub2
             WHERE sub2.study_year_id = sy.id)                           AS avgSubjectPrice
        FROM study_years sy
        WHERE sy.id = :studyYearId
        """,
            nativeQuery = true
    )
    Optional<StudyYearSummaryDto> findSummaryById(
            @Param("studyYearId") Integer studyYearId);

    // ========================================
    // NATIVE QUERIES FOR COMPLEX AGGREGATIONS
    // ========================================

    /**
     * Obtiene información de resumen de todos los study years - Versión nativa optimizada
     */
    @Query(value = """
        SELECT
            sy.id,
            sy.name,
            sy.level,
            sy.description,
            COUNT(DISTINCT s.id)                                            AS total_students,
            COUNT(DISTINCT sub.id)                                          AS total_subjects,
            COUNT(DISTINCT CASE WHEN s.is_active = TRUE THEN s.id END)      AS active_students,
            COUNT(DISTINCT s.major_id)                                      AS total_majors_with_students,
            AVG(sub.monthly_price)                                          AS average_subject_price
        FROM study_years sy
        LEFT JOIN students s   ON s.study_year_id = sy.id
        LEFT JOIN subjects sub ON sub.study_year_id = sy.id
        GROUP BY sy.id, sy.name, sy.level, sy.description
        ORDER BY sy.level
        """, nativeQuery = true)
    List<Object[]> findAllWithSummaryNative();

    /**
     * Obtiene resumen de un study year específico - Versión nativa
     */
    @Query(value = """
        SELECT
            sy.id,
            sy.name,
            sy.level,
            sy.description,
            COUNT(DISTINCT s.id)                                            AS total_students,
            COUNT(DISTINCT sub.id)                                          AS total_subjects,
            COUNT(DISTINCT CASE WHEN s.is_active = TRUE THEN s.id END)      AS active_students,
            COUNT(DISTINCT s.major_id)                                      AS total_majors_with_students,
            AVG(sub.monthly_price)                                          AS average_subject_price
        FROM study_years sy
        LEFT JOIN students s   ON s.study_year_id = sy.id
        LEFT JOIN subjects sub ON sub.study_year_id = sy.id
        WHERE sy.id = :studyYearId
        GROUP BY sy.id, sy.name, sy.level, sy.description
        """, nativeQuery = true)
    Optional<Object[]> findSummaryByIdNative(@Param("studyYearId") Integer studyYearId);

    // ========================================
    // BUSINESS LOGIC QUERIES
    // ========================================

    /**
     * Encuentra study years que tienen estudiantes activos
     */
    @Query("""
        SELECT DISTINCT sy FROM StudyYear sy
        JOIN sy.students s
        WHERE s.isActive = true
        ORDER BY sy.level ASC
        """)
    List<StudyYear> findStudyYearsWithActiveStudents();

    /**
     * Encuentra study years que NO tienen estudiantes
     */
    @Query("""
        SELECT sy FROM StudyYear sy
        WHERE NOT EXISTS (
            SELECT 1 FROM Student s WHERE s.studyYear = sy
        )
        ORDER BY sy.level ASC
        """)
    List<StudyYear> findStudyYearsWithoutStudents();

    /**
     * Encuentra study years que NO tienen materias
     */
    @Query("""
        SELECT sy FROM StudyYear sy
        WHERE NOT EXISTS (
            SELECT 1 FROM Subject sub WHERE sub.studyYear = sy
        )
        ORDER BY sy.level ASC
        """)
    List<StudyYear> findStudyYearsWithoutSubjects();

    /**
     * Encuentra study years con más de X estudiantes activos
     */
    @Query("""
        SELECT sy FROM StudyYear sy
        JOIN sy.students s
        WHERE s.isActive = true
        GROUP BY sy
        HAVING COUNT(s) > :minStudents
        ORDER BY COUNT(s) DESC, sy.level ASC
        """)
    List<StudyYear> findStudyYearsWithMoreThanXActiveStudents(@Param("minStudents") Long minStudents);

    /**
     * Cuenta estudiantes activos por study year
     */
    @Query("""
        SELECT sy.name, sy.level, COUNT(s.id)
        FROM StudyYear sy
        LEFT JOIN sy.students s ON s.isActive = true
        GROUP BY sy.id, sy.name, sy.level
        ORDER BY COUNT(s.id) DESC, sy.level ASC
        """)
    List<Object[]> countActiveStudentsByStudyYear();

    /**
     * Obtiene el top N de study years por número de estudiantes
     */
    @Query("""
        SELECT sy FROM StudyYear sy
        LEFT JOIN sy.students s
        GROUP BY sy
        ORDER BY COUNT(s) DESC, sy.level ASC
        """)
    List<StudyYear> findTopStudyYearsByStudentCount(Pageable pageable);

    // ========================================
    // LEVEL-SPECIFIC QUERIES
    // ========================================

    /**
     * Encuentra el study year con nivel más alto
     */
    @Query("SELECT sy FROM StudyYear sy WHERE sy.level = (SELECT MAX(sy2.level) FROM StudyYear sy2)")
    List<StudyYear> findHighestLevel();

    /**
     * Encuentra el study year con nivel más bajo
     */
    @Query("SELECT sy FROM StudyYear sy WHERE sy.level = (SELECT MIN(sy2.level) FROM StudyYear sy2)")
    List<StudyYear> findLowestLevel();

    /**
     * Encuentra study years con niveles únicos (sin duplicados de nivel)
     */
    @Query("""
        SELECT sy FROM StudyYear sy
        WHERE sy.level IN (
            SELECT sy2.level FROM StudyYear sy2
            GROUP BY sy2.level
            HAVING COUNT(sy2) = 1
        )
        ORDER BY sy.level ASC
        """)
    List<StudyYear> findStudyYearsWithUniqueLevels();

    // ========================================
    // SEARCH AND FILTER QUERIES
    // ========================================

    /**
     * Búsqueda avanzada de study years
     */
    @Query("""
        SELECT DISTINCT sy FROM StudyYear sy
        LEFT JOIN sy.students s
        LEFT JOIN sy.subjects sub
        WHERE (:name IS NULL OR LOWER(sy.name) LIKE LOWER(CONCAT('%', :name, '%')))
        AND (:description IS NULL OR LOWER(sy.description) LIKE LOWER(CONCAT('%', :description, '%')))
        AND (:minLevel IS NULL OR sy.level >= :minLevel)
        AND (:maxLevel IS NULL OR sy.level <= :maxLevel)
        AND (:hasStudents IS NULL OR
             (:hasStudents = true AND EXISTS (SELECT 1 FROM Student st WHERE st.studyYear = sy)) OR
             (:hasStudents = false AND NOT EXISTS (SELECT 1 FROM Student st WHERE st.studyYear = sy)))
        AND (:hasSubjects IS NULL OR
             (:hasSubjects = true AND EXISTS (SELECT 1 FROM Subject subj WHERE subj.studyYear = sy)) OR
             (:hasSubjects = false AND NOT EXISTS (SELECT 1 FROM Subject subj WHERE subj.studyYear = sy)))
        ORDER BY sy.level ASC
        """)
    List<StudyYear> findStudyYearsWithFilters(
            @Param("name") String name,
            @Param("description") String description,
            @Param("minLevel") Integer minLevel,
            @Param("maxLevel") Integer maxLevel,
            @Param("hasStudents") Boolean hasStudents,
            @Param("hasSubjects") Boolean hasSubjects
    );

    /**
     * Búsqueda avanzada paginada
     */
    @Query("""
        SELECT DISTINCT sy FROM StudyYear sy
        WHERE (:name IS NULL OR LOWER(sy.name) LIKE LOWER(CONCAT('%', :name, '%')))
        AND (:description IS NULL OR LOWER(sy.description) LIKE LOWER(CONCAT('%', :description, '%')))
        AND (:minLevel IS NULL OR sy.level >= :minLevel)
        AND (:maxLevel IS NULL OR sy.level <= :maxLevel)
        """)
    Page<StudyYear> findStudyYearsWithFilters(
            @Param("name") String name,
            @Param("description") String description,
            @Param("minLevel") Integer minLevel,
            @Param("maxLevel") Integer maxLevel,
            Pageable pageable
    );

    // ========================================
    // STATISTICS QUERIES (NATIVE)
    // ========================================

    /**
     * Obtiene estadísticas básicas de study years
     */
    @Query(value = """
        SELECT
            COUNT(DISTINCT sy.id)                                           AS total_study_years,
            COUNT(DISTINCT s.id)                                            AS total_students,
            COUNT(DISTINCT CASE WHEN s.is_active = TRUE THEN s.id END)      AS active_students,
            COUNT(DISTINCT sub.id)                                          AS total_subjects,
            AVG(sub.monthly_price)                                          AS avg_subject_price,
            MIN(sy.level)                                                   AS min_level,
            MAX(sy.level)                                                   AS max_level,
            AVG(sy.level)                                                   AS avg_level
        FROM study_years sy
        LEFT JOIN students s   ON s.study_year_id = sy.id
        LEFT JOIN subjects sub ON sub.study_year_id = sy.id
        """, nativeQuery = true)
    Object[] getStudyYearStatistics();

    /**
     * Distribución de estudiantes por nivel de study year
     */
    @Query(value = """
        SELECT
            sy.level,
            sy.name,
            COUNT(DISTINCT s.id)                                            AS total_students,
            COUNT(DISTINCT CASE WHEN s.is_active = TRUE THEN s.id END)      AS active_students,
            COUNT(DISTINCT sub.id)                                          AS total_subjects
        FROM study_years sy
        LEFT JOIN students s   ON s.study_year_id = sy.id
        LEFT JOIN subjects sub ON sub.study_year_id = sy.id
        GROUP BY sy.level, sy.name, sy.id
        ORDER BY sy.level
        """, nativeQuery = true)
    List<Object[]> getStudentDistributionByLevel();

    /**
     * Rangos de niveles con conteo de study years
     */
    @Query(value = """
        SELECT
            CASE
                WHEN sy.level <= 0 THEN 'Preparatory (≤0)'
                WHEN sy.level BETWEEN 1 AND 2 THEN 'Lower Years (1-2)'
                WHEN sy.level BETWEEN 3 AND 4 THEN 'Upper Years (3-4)'
                WHEN sy.level BETWEEN 5 AND 6 THEN 'Graduate (5-6)'
                ELSE 'Advanced (7+)'
            END                                                             AS level_range,
            COUNT(DISTINCT sy.id)                                           AS study_year_count,
            COUNT(DISTINCT s.id)                                            AS total_students,
            COUNT(DISTINCT CASE WHEN s.is_active = TRUE THEN s.id END)      AS active_students
        FROM study_years sy
        LEFT JOIN students s ON s.study_year_id = sy.id
        GROUP BY level_range
        ORDER BY MIN(sy.level)
        """, nativeQuery = true)
    List<Object[]> getStudyYearDistributionByLevelRange();
}
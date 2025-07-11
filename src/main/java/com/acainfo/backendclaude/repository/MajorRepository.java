package com.acainfo.backendclaude.repository;

import com.acainfo.backendclaude.dto.MajorSummaryDto;
import com.acainfo.backendclaude.model.Major;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface MajorRepository extends JpaRepository<Major, Integer> {

    // ========================================
    // BASIC CRUD WITH ENTITY GRAPHS
    // ========================================

    /**
     * Encuentra un Major por ID con estudiantes y materias cargados
     */
    @EntityGraph(attributePaths = {"students", "subjects"})
    @Query("SELECT m FROM Major m WHERE m.id = :id")
    Optional<Major> findByIdWithDetails(@Param("id") Integer id);

    /**
     * Encuentra un Major por ID solo con estudiantes
     */
    @EntityGraph(attributePaths = {"students", "students.studyYear"})
    @Query("SELECT m FROM Major m WHERE m.id = :id")
    Optional<Major> findByIdWithStudents(@Param("id") Integer id);

    /**
     * Encuentra un Major por ID solo con materias
     */
    @EntityGraph(attributePaths = {"subjects", "subjects.studyYear"})
    @Query("SELECT m FROM Major m WHERE m.id = :id")
    Optional<Major> findByIdWithSubjects(@Param("id") Integer id);

    // ========================================
    // DERIVED QUERIES
    // ========================================

    /**
     * Busca majors por nombre (case-insensitive)
     */
    List<Major> findByNameContainingIgnoreCase(String name);

    /**
     * Busca majors por nombre exacto (case-insensitive)
     */
    Optional<Major> findByNameIgnoreCase(String name);

    /**
     * Verifica si existe un major con el nombre dado
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Encuentra majors creados después de una fecha
     */
    List<Major> findByCreatedAtAfter(Instant date);

    /**
     * Encuentra majors creados entre dos fechas
     */
    List<Major> findByCreatedAtBetween(Instant startDate, Instant endDate);

    // ========================================
    // PAGINATED QUERIES
    // ========================================

    /**
     * Busca majors por nombre con paginación
     */
    Page<Major> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Obtiene todos los majors ordenados por nombre
     */
    Page<Major> findAllByOrderByNameAsc(Pageable pageable);

    // ========================================
    // CUSTOM QUERIES FOR SUMMARY DATA
    // ========================================

    /**
     * Obtiene información de resumen de todos los majors
     */
    @Query(value = """
        SELECT
            m.id,
            m.name,
            m.description,
            m.created_at,
            COUNT(DISTINCT s.id) as totalStudents,
            COUNT(DISTINCT sub.id) as totalSubjects,
            COUNT(DISTINCT CASE WHEN s.is_active = true THEN s.id END) as activeStudents
        FROM majors m
        LEFT JOIN students s ON s.major_id = m.id
        LEFT JOIN subjects sub ON sub.major_id = m.id
        GROUP BY m.id, m.name, m.description, m.created_at
        ORDER BY m.name
        """, nativeQuery = true)
    List<Object[]> findAllWithSummaryNative();

    /**
     * Obtiene información de resumen de un major específico
    */
    @Query(value = """
    SELECT
        m.id,                                               -- ❶ id
        m.name,                                             -- ❷ name
        m.description,                                      -- ❸ description
        m.created_at,                                       -- ❹ createdAt
        COUNT(DISTINCT s.id)                                                AS total_students,   -- ❺
        COUNT(DISTINCT sub.id)                                               AS total_subjects,  -- ❻
        COUNT(DISTINCT CASE WHEN s.is_active = TRUE THEN s.id END)           AS active_students  -- ❼
    FROM majors m
    LEFT JOIN students  s   ON s.major_id  = m.id
    LEFT JOIN subjects  sub ON sub.major_id = m.id
    WHERE m.id = :majorId
    GROUP BY m.id, m.name, m.description, m.created_at
    """,
            nativeQuery = true)
    Optional<MajorSummaryDto> findSummaryById(@Param("majorId") Integer majorId);


    /**
     * Obtiene majors con resumen paginado
    */
     @Query(
     value = """
     SELECT
     m.id AS id,
     m.name AS name,
     m.description AS description,
     m.created_at AS createdAt,
     COUNT(DISTINCT s.id) AS totalStudents,
     COUNT(DISTINCT sub.id) AS totalSubjects,
     COUNT(DISTINCT CASE WHEN s.is_active = TRUE THEN s.id END)  AS activeStudents
     FROM majors m
     LEFT JOIN students s   ON s.major_id   = m.id
     LEFT JOIN subjects sub ON sub.major_id = m.id
     GROUP BY m.id, m.name, m.description, m.created_at
     """,
     countQuery = """
     SELECT COUNT(DISTINCT m.id)
     FROM majors m
     """,
     nativeQuery = true
     )
     Page<MajorSummaryDto> findAllWithSummary(Pageable pageable);

    // ========================================
    // BUSINESS LOGIC QUERIES
    // ========================================

    /**
     * Encuentra majors que tienen estudiantes activos
     */
    @Query("""
        SELECT DISTINCT m FROM Major m
        JOIN m.students s
        WHERE s.isActive = true
        ORDER BY m.name ASC
        """)
    List<Major> findMajorsWithActiveStudents();

    /**
     * Encuentra majors que NO tienen estudiantes
     */
    @Query("""
        SELECT m FROM Major m
        WHERE NOT EXISTS (
            SELECT 1 FROM Student s WHERE s.major = m
        )
        ORDER BY m.name ASC
        """)
    List<Major> findMajorsWithoutStudents();

    /**
     * Encuentra majors que NO tienen materias
     */
    @Query("""
        SELECT m FROM Major m
        WHERE NOT EXISTS (
            SELECT 1 FROM Subject sub WHERE sub.major = m
        )
        ORDER BY m.name ASC
        """)
    List<Major> findMajorsWithoutSubjects();

    /**
     * Encuentra majors con más de X estudiantes activos
     */
    @Query("""
        SELECT m FROM Major m
        JOIN m.students s
        WHERE s.isActive = true
        GROUP BY m
        HAVING COUNT(s) > :minStudents
        ORDER BY COUNT(s) DESC, m.name ASC
        """)
    List<Major> findMajorsWithMoreThanXActiveStudents(@Param("minStudents") Long minStudents);

    /**
     * Cuenta estudiantes activos por major
     */
    @Query("""
        SELECT m.name, COUNT(s.id)
        FROM Major m
        LEFT JOIN m.students s ON s.isActive = true
        GROUP BY m.id, m.name
        ORDER BY COUNT(s.id) DESC, m.name ASC
        """)
    List<Object[]> countActiveStudentsByMajor();

    /**
     * Obtiene el top N de majors por número de estudiantes
     */
    @Query("""
        SELECT m FROM Major m
        LEFT JOIN m.students s
        GROUP BY m
        ORDER BY COUNT(s) DESC, m.name ASC
        """)
    List<Major> findTopMajorsByStudentCount(Pageable pageable);

    // ========================================
    // SEARCH AND FILTER QUERIES
    // ========================================

    /**
     * Búsqueda avanzada de majors
     */
    @Query("""
        SELECT DISTINCT m FROM Major m
        LEFT JOIN m.students s
        LEFT JOIN m.subjects sub
        WHERE (:name IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%')))
        AND (:description IS NULL OR LOWER(m.description) LIKE LOWER(CONCAT('%', :description, '%')))
        AND (:hasStudents IS NULL OR
             (:hasStudents = true AND EXISTS (SELECT 1 FROM Student st WHERE st.major = m)) OR
             (:hasStudents = false AND NOT EXISTS (SELECT 1 FROM Student st WHERE st.major = m)))
        AND (:hasSubjects IS NULL OR
             (:hasSubjects = true AND EXISTS (SELECT 1 FROM Subject subj WHERE subj.major = m)) OR
             (:hasSubjects = false AND NOT EXISTS (SELECT 1 FROM Subject subj WHERE subj.major = m)))
        ORDER BY m.name ASC
        """)
    List<Major> findMajorsWithFilters(
            @Param("name") String name,
            @Param("description") String description,
            @Param("hasStudents") Boolean hasStudents,
            @Param("hasSubjects") Boolean hasSubjects
    );

    /**
     * Búsqueda avanzada paginada
     */
    @Query("""
        SELECT DISTINCT m FROM Major m
        WHERE (:name IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%')))
        AND (:description IS NULL OR LOWER(m.description) LIKE LOWER(CONCAT('%', :description, '%')))
        """)
    Page<Major> findMajorsWithFilters(
            @Param("name") String name,
            @Param("description") String description,
            Pageable pageable
    );

    // ========================================
    // STATISTICS QUERIES
    // ========================================

    /**
     * Obtiene estadísticas básicas de majors*/

    @Query(value = """
    SELECT
        COUNT(DISTINCT m.id)                                                AS total_majors,
        COUNT(DISTINCT s.id)                                                AS total_students,
        COUNT(DISTINCT CASE WHEN s.is_active = TRUE THEN s.id END)          AS active_students,
        COUNT(DISTINCT sub.id)                                              AS total_subjects,
        AVG(sub.monthly_price)                                              AS avg_subject_price     -- o AVG(DISTINCT sub.monthly_price)
    FROM majors m
    LEFT JOIN students  s   ON s.major_id  = m.id
    LEFT JOIN subjects  sub ON sub.major_id = m.id
    """, nativeQuery = true)
    Object[] getMajorStatistics();

    /**
     * Cuenta majors por rango de estudiantes
     */
    @Query(value = """
        SELECT
            CASE
                WHEN student_count = 0 THEN '0 students'
                WHEN student_count BETWEEN 1 AND 10 THEN '1-10 students'
                WHEN student_count BETWEEN 11 AND 50 THEN '11-50 students'
                WHEN student_count BETWEEN 51 AND 100 THEN '51-100 students'
                ELSE '100+ students'
            END as range_label,
            COUNT(*) as major_count
        FROM (
            SELECT m.id, COUNT(s.id) as student_count
            FROM majors m
            LEFT JOIN students s ON s.major_id = m.id AND s.is_active = true
            GROUP BY m.id
        ) as counts
        GROUP BY range_label
        ORDER BY
            CASE range_label
                WHEN '0 students' THEN 1
                WHEN '1-10 students' THEN 2
                WHEN '11-50 students' THEN 3
                WHEN '51-100 students' THEN 4
                ELSE 5
            END
        """, nativeQuery = true)
    List<Object[]> getMajorDistributionByStudentCount();
}
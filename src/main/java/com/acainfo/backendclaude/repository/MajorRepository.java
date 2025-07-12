
package com.acainfo.backendclaude.repository;

import com.acainfo.backendclaude.dto.major.MajorRevenueDto;
import com.acainfo.backendclaude.dto.major.MajorWithStudentCountDto;
import com.acainfo.backendclaude.model.Major;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MajorRepository extends JpaRepository<Major, Integer> {

    /**
     * Busca majors por nombre usando LIKE (case-insensitive)
     * Derived query - Spring genera automáticamente la implementación
     *
     * @param name parte del nombre a buscar
     * @return lista de majors que contienen el texto en su nombre
     */
    List<Major> findByNameContainingIgnoreCase(String name);

    /**
     * Verifica si existe un major con el nombre exacto (case-insensitive)
     * Útil para validaciones antes de crear un nuevo major
     *
     * @param name nombre exacto del major
     * @return true si existe, false si no
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Encuentra un major por nombre exacto (case-insensitive)
     * Útil para búsquedas exactas
     *
     * @param name nombre exacto del major
     * @return Optional con el major si existe
     */
    Optional<Major> findByNameIgnoreCase(String name);

    /**
     * Obtiene todos los majors con el conteo de estudiantes activos
     * JPQL con LEFT JOIN para incluir majors sin estudiantes
     * Constructor expression para mapear directamente al DTO
     *
     * @return lista de DTOs con major y conteo de estudiantes activos
     */
    @Query("""
        SELECT new com.acainfo.backendclaude.dto.major.MajorWithStudentCountDto(m.id, m.name, m.description,
                CAST(COALESCE(COUNT(s.id), 0L) AS long))
        FROM Major m
        LEFT JOIN m.students s ON s.isActive = true
        GROUP BY m.id, m.name, m.description
        ORDER BY COUNT(s.id) DESC, m.name ASC
        """)
    List<MajorWithStudentCountDto> findAllMajorsWithActiveStudentCount();

    /**
     * Obtiene el conteo de estudiantes activos para un major específico
     * Útil para estadísticas rápidas de un solo major
     *
     * @param majorId ID del major
     * @return número de estudiantes activos
     */
    @Query("""
        SELECT COUNT(s)
        FROM Student s
        WHERE s.major.id = :majorId
        AND s.isActive = true
        """)
    Long countActiveStudentsByMajorId(@Param("majorId") Integer majorId);

    /**
     * Obtiene majors que tienen al menos un estudiante activo
     * Útil para filtrar carreras "vivas" o activas
     *
     * @return lista de majors con estudiantes activos
     */
    @Query("""
        SELECT DISTINCT m
        FROM Major m
        JOIN m.students s
        WHERE s.isActive = true
        ORDER BY m.name
        """)
    List<Major> findMajorsWithActiveStudents();

    /**
     * Obtiene majors sin estudiantes (activos o inactivos)
     * Útil para identificar carreras sin uso
     *
     * @return lista de majors sin estudiantes
     */
    @Query("""
        SELECT m
        FROM Major m
        LEFT JOIN m.students s
        WHERE s IS NULL
        ORDER BY m.name
        """)
    List<Major> findMajorsWithoutStudents();

    /**
     * Cuenta el número total de materias de un major
     * Útil para estadísticas del plan de estudios
     *
     * @param majorId ID del major
     * @return número de materias
     */
    @Query("""
        SELECT COUNT(s)
        FROM Subject s
        WHERE s.major.id = :majorId
        """)
    Long countSubjectsByMajorId(@Param("majorId") Integer majorId);

    /**
     * Obtiene majors ordenados por fecha de creación (más recientes primero)
     * Útil para dashboards o listados administrativos
     *
     * @return lista de majors ordenados por fecha descendente
     */
    List<Major> findAllByOrderByCreatedAtDesc();

    /**
     * Busca majors creados después de una fecha específica
     * Útil para reportes o filtros temporales
     *
     * @param date fecha de corte
     * @return lista de majors creados después de la fecha
     */
    @Query("""
        SELECT m
        FROM Major m
        WHERE m.createdAt > :date
        ORDER BY m.createdAt DESC
        """)
    List<Major> findMajorsCreatedAfter(@Param("date") java.time.Instant date);

    /**
     * Obtiene el major con más estudiantes activos
     * Útil para estadísticas y rankings
     *
     * @return Optional con el major más popular
     */
    @Query("""
        SELECT m
        FROM Major m
        LEFT JOIN m.students s ON s.isActive = true
        GROUP BY m
        ORDER BY COUNT(s) DESC
        LIMIT 1
        """)
    Optional<Major> findMostPopularMajor();

    /**
     * Cuenta cuántos majors tienen al menos N estudiantes activos
     * Útil para métricas de viabilidad
     *
     * @param minStudents número mínimo de estudiantes
     * @return cantidad de majors que cumplen el criterio
     */
    @Query("""
        SELECT COUNT(DISTINCT m.id)
        FROM Major m
        WHERE m.id IN (
            SELECT m2.id
            FROM Major m2
            JOIN m2.students s2
            WHERE s2.isActive = true
            GROUP BY m2.id
            HAVING COUNT(s2) >= :minStudents
        )
        """)
    Long countMajorsWithMinimumActiveStudents(@Param("minStudents") Long minStudents);

    /**
     * Obtiene majors con información de ingresos estimados mensuales
     *
     * NOTA: Esta consulta es simplificada. Para un cálculo exacto del revenue,
     * considera usar una consulta nativa o calcular en el servicio.
     */
    @Query("""
        SELECT new com.acainfo.backendclaude.dto.major.MajorRevenueDto(
            m.id,
            m.name,
            CAST((SELECT COUNT(DISTINCT s.id) FROM Student s WHERE s.major = m AND s.isActive = true) AS long),
            CAST((SELECT COUNT(DISTINCT subj.id) FROM Subject subj WHERE subj.major = m) AS int),
            CAST(COALESCE((SELECT SUM(subj2.monthlyPrice) 
                      FROM Subject subj2 
                      WHERE subj2.major = m 
                      AND EXISTS (SELECT 1 FROM subj2.subjectsGroups sg2 
                                  WHERE sg2.status = com.acainfo.backendclaude.model.SubjectsGroup.GroupStatus.ACTIVE)), 0L) AS long)
        )
        FROM Major m
        ORDER BY m.name ASC
        """)
    List<MajorRevenueDto> findAllMajorsWithSubjectStats();
}

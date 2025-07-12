
package com.acainfo.backendclaude.repository;

import com.acainfo.backendclaude.dto.major.MajorRevenueDto;
import com.acainfo.backendclaude.dto.major.MajorWithStudentCountDto;
import com.acainfo.backendclaude.model.Major;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

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
     * Obtiene majors con información de ingresos estimados mensuales
     *
     * CONSULTA COMPLEJA que involucra múltiples JOINs:
     * Major → Subject → SubjectsGroup → Registration → Student
     *
     * Lógica del cálculo:
     * 1. Para cada materia (Subject) del major
     * 2. Encuentra todos los grupos activos de esa materia
     * 3. Cuenta estudiantes activos registrados en esos grupos
     * 4. Multiplica: precio_materia * estudiantes_registrados
     * 5. Suma todos los ingresos de todas las materias del major
     *
     * @return lista de DTOs con información de ingresos por major
     */
    @Query("""
        SELECT new com.acainfo.backendclaude.dto.major.MajorRevenueDto(
            m.id,
            m.name,
            CAST(COALESCE(COUNT(DISTINCT stud.id), 0) AS long),
            CAST(COALESCE(COUNT(DISTINCT subj.id), 0) AS int),
            CAST(COALESCE(SUM(subj.monthlyPrice * regCount.studentCount), 0) AS long)
        )
        FROM Major m
        LEFT JOIN m.subjects subj
        LEFT JOIN subj.subjectsGroups sg ON sg.status = com.acainfo.backendclaude.model.SubjectsGroup.GroupStatus.ACTIVE
        LEFT JOIN sg.registrations reg ON reg.status = com.acainfo.backendclaude.model.Registration.RegistrationStatus.ACTIVE
        LEFT JOIN reg.student stud ON stud.isActive = true
        LEFT JOIN (
            SELECT sg2.id as groupId, COUNT(r2.student.id) as studentCount
            FROM SubjectsGroup sg2
            LEFT JOIN sg2.registrations r2 ON r2.status = com.acainfo.backendclaude.model.Registration.RegistrationStatus.ACTIVE
            LEFT JOIN r2.student s2 ON s2.isActive = true
            WHERE sg2.status = com.acainfo.backendclaude.model.SubjectsGroup.GroupStatus.ACTIVE
            GROUP BY sg2.id
        ) regCount ON regCount.groupId = sg.id
        GROUP BY m.id, m.name
        ORDER BY SUM(subj.monthlyPrice * regCount.studentCount) DESC NULLS LAST, m.name ASC
        """)
    List<MajorRevenueDto> findAllMajorsWithSubjectStats();
}

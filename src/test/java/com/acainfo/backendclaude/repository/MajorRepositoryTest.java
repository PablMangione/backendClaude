package com.acainfo.backendclaude.repository;

import com.acainfo.backendclaude.dto.major.MajorRevenueDto;
import com.acainfo.backendclaude.dto.major.MajorWithStudentCountDto;
import com.acainfo.backendclaude.model.Major;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(excludeAutoConfiguration = FlywayAutoConfiguration.class)
@ActiveProfiles("test")
@Sql(scripts = "/test-data.sql") // Carga los datos automáticamente
class MajorRepositoryTest {

    @Autowired
    private MajorRepository majorRepository;

    // ========== TESTS PARA findByNameContainingIgnoreCase ==========

    @Test
    @DisplayName("Debe encontrar majors por nombre parcial (case-insensitive)")
    void testFindByNameContainingIgnoreCase() {
        // When: Buscamos por "info" (parcial de "Informática")
        List<Major> result = majorRepository.findByNameContainingIgnoreCase("info");

        // Then: Debe encontrar 1 resultado
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("Ingeniería Informática");
    }

    @Test
    @DisplayName("Debe encontrar majors independientemente de mayúsculas/minúsculas")
    void testFindByNameContainingIgnoreCase_CaseInsensitive() {
        // When: Buscamos con diferentes casos
        List<Major> resultLower = majorRepository.findByNameContainingIgnoreCase("medicina");
        List<Major> resultUpper = majorRepository.findByNameContainingIgnoreCase("MEDICINA");
        List<Major> resultMixed = majorRepository.findByNameContainingIgnoreCase("MeDiCiNa");

        // Then: Todos deben encontrar el mismo resultado
        assertThat(resultLower).hasSize(1);
        assertThat(resultUpper).hasSize(1);
        assertThat(resultMixed).hasSize(1);
        assertThat(resultLower.getFirst().getName()).isEqualTo("Medicina");
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no encuentra coincidencias")
    void testFindByNameContainingIgnoreCase_NoResults() {
        // When: Buscamos algo que no existe
        List<Major> result = majorRepository.findByNameContainingIgnoreCase("inexistente");

        // Then: Lista vacía
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Debe encontrar múltiples majors con coincidencias parciales")
    void testFindByNameContainingIgnoreCase_MultipleResults() {
        // When: Buscamos por "ing" (debe encontrar las 2 ingenierías)
        List<Major> result = majorRepository.findByNameContainingIgnoreCase("ing");

        // Then: Debe encontrar 2 resultados (Informática e Industrial)
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Major::getName)
                .containsExactlyInAnyOrder("Ingeniería Informática", "Ingeniería Industrial");
    }

    // ========== TESTS PARA findAllMajorsWithActiveStudentCount ==========

    @Test
    @DisplayName("Debe obtener todos los majors con conteo correcto ordenados por popularidad")
    void testFindAllMajorsWithActiveStudentCount() {
        // When: Obtenemos majors con conteo
        List<MajorWithStudentCountDto> result = majorRepository.findAllMajorsWithActiveStudentCount();

        // Then: Debe retornar los 5 majors
        assertThat(result).hasSize(5);

        // Verificamos el orden: Industrial (4), Informática (3), Medicina (2), Derecho (1), Psicología (0)
        assertThat(result.get(0).name()).isEqualTo("Ingeniería Industrial");
        assertThat(result.get(0).activeStudentCount()).isEqualTo(4L);

        assertThat(result.get(1).name()).isEqualTo("Ingeniería Informática");
        assertThat(result.get(1).activeStudentCount()).isEqualTo(3L); // 3 activos, 1 inactivo no cuenta

        assertThat(result.get(2).name()).isEqualTo("Medicina");
        assertThat(result.get(2).activeStudentCount()).isEqualTo(2L);

        assertThat(result.get(3).name()).isEqualTo("Derecho");
        assertThat(result.get(3).activeStudentCount()).isEqualTo(1L);

        assertThat(result.get(4).name()).isEqualTo("Psicología");
        assertThat(result.get(4).activeStudentCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Debe incluir majors sin estudiantes con count = 0")
    void testFindAllMajorsWithActiveStudentCount_IncludesEmptyMajors() {
        // When: Obtenemos majors con conteo
        List<MajorWithStudentCountDto> result = majorRepository.findAllMajorsWithActiveStudentCount();

        // Then: Psicología debe aparecer con 0 estudiantes
        MajorWithStudentCountDto psicologia = result.stream()
                .filter(dto -> dto.name().equals("Psicología"))
                .findFirst()
                .orElseThrow();

        assertThat(psicologia.activeStudentCount()).isEqualTo(0L);
        assertThat(psicologia.hasActiveStudents()).isFalse();
    }

    @Test
    @DisplayName("Debe contar solo estudiantes activos, ignorando inactivos")
    void testFindAllMajorsWithActiveStudentCount_OnlyActiveStudents() {
        // When: Obtenemos majors con conteo
        List<MajorWithStudentCountDto> result = majorRepository.findAllMajorsWithActiveStudentCount();

        // Then: Informática debe tener 3 estudiantes (no 4, porque uno está inactivo)
        MajorWithStudentCountDto informatica = result.stream()
                .filter(dto -> dto.name().equals("Ingeniería Informática"))
                .findFirst()
                .orElseThrow();

        assertThat(informatica.activeStudentCount()).isEqualTo(3L);
        assertThat(informatica.hasActiveStudents()).isTrue();
    }

    @Test
    @DisplayName("Debe mantener información completa del major en el DTO")
    void testFindAllMajorsWithActiveStudentCount_CompleteInfo() {
        // When: Obtenemos majors con conteo
        List<MajorWithStudentCountDto> result = majorRepository.findAllMajorsWithActiveStudentCount();

        // Then: Verificamos que el DTO tiene toda la información
        MajorWithStudentCountDto informatica = result.stream()
                .filter(dto -> dto.name().equals("Ingeniería Informática"))
                .findFirst()
                .orElseThrow();

        assertThat(informatica.id()).isEqualTo(1);
        assertThat(informatica.name()).isEqualTo("Ingeniería Informática");
        assertThat(informatica.description()).isEqualTo("Carrera enfocada en desarrollo de software y sistemas");
        assertThat(informatica.activeStudentCount()).isEqualTo(3L);
    }

    @Test
    @DisplayName("Debe calcular correctamente los ingresos mensuales estimados por major")
    void testFindAllMajorsWithSubjectStats_PriceSum() {
        // When: Obtenemos majors con estadísticas de materias
        List<MajorRevenueDto> result = majorRepository.findAllMajorsWithSubjectStats();

        // Then: Verificamos los cálculos esperados
        assertThat(result).hasSize(5);

    }

}
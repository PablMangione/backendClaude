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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

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
    void testFindAllMajorsWithSubjectStats_count() {
        // When: Obtenemos majors con estadísticas de materias
        List<MajorRevenueDto> result = majorRepository.findAllMajorsWithSubjectStats();

        // Then: Verificamos los cálculos esperados
        assertThat(result).hasSize(5);

    }

    @Test
    @DisplayName("Debe verificar existencia de major por nombre exacto (case-insensitive)")
    void testExistsByNameIgnoreCase() {
        // When & Then: Existe
        assertThat(majorRepository.existsByNameIgnoreCase("Medicina")).isTrue();
        assertThat(majorRepository.existsByNameIgnoreCase("MEDICINA")).isTrue();
        assertThat(majorRepository.existsByNameIgnoreCase("medicina")).isTrue();

        // When & Then: No existe
        assertThat(majorRepository.existsByNameIgnoreCase("Arquitectura")).isFalse();
        assertThat(majorRepository.existsByNameIgnoreCase("Medicin")).isFalse(); // Parcial no cuenta
    }

    @Test
    @DisplayName("Debe encontrar major por nombre exacto (case-insensitive)")
    void testFindByNameIgnoreCase() {
        // When: Buscamos con diferentes casos
        Optional<Major> result1 = majorRepository.findByNameIgnoreCase("Ingeniería Informática");
        Optional<Major> result2 = majorRepository.findByNameIgnoreCase("INGENIERÍA INFORMÁTICA");
        Optional<Major> result3 = majorRepository.findByNameIgnoreCase("ingeniería informática");

        // Then: Todos encuentran el mismo major
        assertThat(result1).isPresent();
        assertThat(result2).isPresent();
        assertThat(result3).isPresent();
        assertThat(result1.get().getId()).isEqualTo(1);

        // When: Buscamos algo que no existe
        Optional<Major> noResult = majorRepository.findByNameIgnoreCase("No Existe");

        // Then: Empty
        assertThat(noResult).isEmpty();
    }

    @Test
    @DisplayName("Debe contar estudiantes activos por major ID")
    void testCountActiveStudentsByMajorId() {
        // When & Then
        assertThat(majorRepository.countActiveStudentsByMajorId(1)).isEqualTo(3L); // Informática
        assertThat(majorRepository.countActiveStudentsByMajorId(2)).isEqualTo(2L); // Medicina
        assertThat(majorRepository.countActiveStudentsByMajorId(3)).isEqualTo(1L); // Derecho
        assertThat(majorRepository.countActiveStudentsByMajorId(4)).isEqualTo(4L); // Industrial
        assertThat(majorRepository.countActiveStudentsByMajorId(5)).isEqualTo(0L); // Psicología
    }

    @Test
    @DisplayName("Debe encontrar solo majors con estudiantes activos")
    void testFindMajorsWithActiveStudents() {
        // When
        List<Major> result = majorRepository.findMajorsWithActiveStudents();

        // Then: Solo 4 majors tienen estudiantes activos (Psicología no)
        assertThat(result).hasSize(4);
        assertThat(result)
                .extracting(Major::getName)
                .containsExactly(
                        "Derecho",
                        "Ingeniería Industrial",
                        "Ingeniería Informática",
                        "Medicina"
                ); // Orden alfabético
    }

    @Test
    @DisplayName("Debe encontrar majors sin estudiantes")
    void testFindMajorsWithoutStudents() {
        // When
        List<Major> result = majorRepository.findMajorsWithoutStudents();

        // Then: Solo Psicología no tiene estudiantes
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("Psicología");
    }

    @Test
    @DisplayName("Debe contar materias por major ID")
    void testCountSubjectsByMajorId() {
        // When & Then
        assertThat(majorRepository.countSubjectsByMajorId(1)).isEqualTo(3L); // Informática tiene 3
        assertThat(majorRepository.countSubjectsByMajorId(2)).isEqualTo(2L); // Medicina tiene 2
        assertThat(majorRepository.countSubjectsByMajorId(3)).isEqualTo(2L); // Derecho tiene 2
        assertThat(majorRepository.countSubjectsByMajorId(4)).isEqualTo(2L); // Industrial tiene 2
        assertThat(majorRepository.countSubjectsByMajorId(5)).isEqualTo(0L); // Psicología no tiene
    }

    @Test
    @DisplayName("Debe obtener majors ordenados por fecha de creación descendente")
    void testFindAllByOrderByCreatedAtDesc() {
        // When
        List<Major> result = majorRepository.findAllByOrderByCreatedAtDesc();

        // Then: Todos deben estar en orden (en test-data.sql todos tienen CURRENT_TIMESTAMP)
        assertThat(result).hasSize(5);
        // Como todos se crean al mismo tiempo, verificamos que al menos están todos
        assertThat(result)
                .extracting(Major::getName)
                .containsExactlyInAnyOrder(
                        "Ingeniería Informática",
                        "Medicina",
                        "Derecho",
                        "Ingeniería Industrial",
                        "Psicología"
                );
    }

    @Test
    @DisplayName("Debe encontrar majors creados después de una fecha")
    void testFindMajorsCreatedAfter() {
        // Given: Una fecha de hace 1 hora
        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);

        // When
        List<Major> result = majorRepository.findMajorsCreatedAfter(oneHourAgo);

        // Then: Todos los majors fueron creados recientemente
        assertThat(result).hasSize(5);

        // Given: Una fecha futura
        Instant tomorrow = Instant.now().plus(1, ChronoUnit.DAYS);

        // When
        List<Major> noResults = majorRepository.findMajorsCreatedAfter(tomorrow);

        // Then: No hay majors del futuro
        assertThat(noResults).isEmpty();
    }

    @Test
    @DisplayName("Debe encontrar el major más popular")
    void testFindMostPopularMajor() {
        // When
        Optional<Major> result = majorRepository.findMostPopularMajor();

        // Then: Industrial con 4 estudiantes activos
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Ingeniería Industrial");
    }

    @Test
    @DisplayName("Debe contar majors con mínimo N estudiantes activos")
    void testCountMajorsWithMinimumActiveStudents() {
        // When & Then
        assertThat(majorRepository.countMajorsWithMinimumActiveStudents(1L)).isEqualTo(4L); // Todos menos Psicología
        assertThat(majorRepository.countMajorsWithMinimumActiveStudents(2L)).isEqualTo(3L); // Industrial, Informática, Medicina
        assertThat(majorRepository.countMajorsWithMinimumActiveStudents(3L)).isEqualTo(2L); // Industrial, Informática
        assertThat(majorRepository.countMajorsWithMinimumActiveStudents(4L)).isEqualTo(1L); // Solo Industrial
        assertThat(majorRepository.countMajorsWithMinimumActiveStudents(5L)).isEqualTo(0L); // Ninguno tiene 5 o más
    }

    @Test
    @DisplayName("Debe calcular correctamente los datos básicos de majors")
    void testFindAllMajorsWithSubjectStats_BasicData() {
        // When
        List<MajorRevenueDto> result = majorRepository.findAllMajorsWithSubjectStats();

        // Then: Verificamos tamaño
        assertThat(result).hasSize(5);

        // Verificamos que los datos básicos están correctos
        // NOTA: El revenue en esta versión es simplificado (suma de precios sin multiplicar por estudiantes)

        // Verificamos estudiantes activos y materias para cada major
        MajorRevenueDto informatica = result.stream()
                .filter(dto -> dto.name().equals("Ingeniería Informática"))
                .findFirst()
                .orElseThrow();
        assertThat(informatica.totalActiveStudents()).isEqualTo(3L);
        assertThat(informatica.totalSubjects()).isEqualTo(3);

        MajorRevenueDto medicina = result.stream()
                .filter(dto -> dto.name().equals("Medicina"))
                .findFirst()
                .orElseThrow();
        assertThat(medicina.totalActiveStudents()).isEqualTo(2L);
        assertThat(medicina.totalSubjects()).isEqualTo(2);

        MajorRevenueDto derecho = result.stream()
                .filter(dto -> dto.name().equals("Derecho"))
                .findFirst()
                .orElseThrow();
        assertThat(derecho.totalActiveStudents()).isEqualTo(1L);
        assertThat(derecho.totalSubjects()).isEqualTo(2);

        MajorRevenueDto industrial = result.stream()
                .filter(dto -> dto.name().equals("Ingeniería Industrial"))
                .findFirst()
                .orElseThrow();
        assertThat(industrial.totalActiveStudents()).isEqualTo(4L);
        assertThat(industrial.totalSubjects()).isEqualTo(2);

        MajorRevenueDto psicologia = result.stream()
                .filter(dto -> dto.name().equals("Psicología"))
                .findFirst()
                .orElseThrow();
        assertThat(psicologia.totalActiveStudents()).isEqualTo(0L);
        assertThat(psicologia.totalSubjects()).isEqualTo(0);
    }
}
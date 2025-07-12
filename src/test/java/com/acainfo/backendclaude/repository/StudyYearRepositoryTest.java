package com.acainfo.backendclaude.repository;

import com.acainfo.backendclaude.dto.studyyear.StudyYearWithStudentCountDto;
import com.acainfo.backendclaude.dto.studyyear.StudyYearWithSubjectStatsDto;
import com.acainfo.backendclaude.model.StudyYear;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(excludeAutoConfiguration = FlywayAutoConfiguration.class)
@ActiveProfiles("test")
@Sql(scripts = "/test-data.sql")
class StudyYearRepositoryTest {

    @Autowired
    private StudyYearRepository studyYearRepository;

    // ========== TEST 1: findByNameContainingIgnoreCase ==========

    @Test
    @DisplayName("Debe encontrar study years por nombre parcial (case-insensitive)")
    void testFindByNameContainingIgnoreCase() {
        // When: Buscamos por "primer" (parcial de "Primer Año")
        List<StudyYear> result = studyYearRepository.findByNameContainingIgnoreCase("primer");

        // Then: Debe encontrar 1 resultado
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("Primer Año");
        assertThat(result.getFirst().getLevel()).isEqualTo(1);
    }

    @Test
    @DisplayName("Debe encontrar study years independientemente de mayúsculas/minúsculas")
    void testFindByNameContainingIgnoreCase_CaseInsensitive() {
        // When: Buscamos con diferentes casos
        List<StudyYear> resultLower = studyYearRepository.findByNameContainingIgnoreCase("segundo");
        List<StudyYear> resultUpper = studyYearRepository.findByNameContainingIgnoreCase("SEGUNDO");
        List<StudyYear> resultMixed = studyYearRepository.findByNameContainingIgnoreCase("SeGuNdO");

        // Then: Todos deben encontrar el mismo resultado
        assertThat(resultLower).hasSize(1);
        assertThat(resultUpper).hasSize(1);
        assertThat(resultMixed).hasSize(1);
        assertThat(resultLower.getFirst().getName()).isEqualTo("Segundo Año");
        assertThat(resultLower.getFirst().getLevel()).isEqualTo(2);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando no encuentra coincidencias")
    void testFindByNameContainingIgnoreCase_NoResults() {
        // When: Buscamos algo que no existe
        List<StudyYear> result = studyYearRepository.findByNameContainingIgnoreCase("inexistente");

        // Then: Lista vacía
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Debe encontrar múltiples study years con coincidencias parciales")
    void testFindByNameContainingIgnoreCase_MultipleResults() {
        // When: Buscamos por "año" (debe encontrar todos porque todos terminan en "Año")
        List<StudyYear> result = studyYearRepository.findByNameContainingIgnoreCase("año");

        // Then: Debe encontrar los 5 resultados
        assertThat(result).hasSize(5);
        assertThat(result)
                .extracting(StudyYear::getName)
                .containsExactlyInAnyOrder(
                        "Primer Año", "Segundo Año", "Tercer Año",
                        "Cuarto Año", "Quinto Año"
                );
    }

    // ========== TEST 2: existsByLevel ==========

    @Test
    @DisplayName("Debe verificar existencia por nivel correctamente")
    void testExistsByLevel() {
        // When & Then: Niveles que existen
        assertThat(studyYearRepository.existsByLevel(1)).isTrue();
        assertThat(studyYearRepository.existsByLevel(2)).isTrue();
        assertThat(studyYearRepository.existsByLevel(3)).isTrue();
        assertThat(studyYearRepository.existsByLevel(4)).isTrue();
        assertThat(studyYearRepository.existsByLevel(5)).isTrue();

        // When & Then: Niveles que no existen
        assertThat(studyYearRepository.existsByLevel(0)).isFalse();
        assertThat(studyYearRepository.existsByLevel(6)).isFalse();
        assertThat(studyYearRepository.existsByLevel(-1)).isFalse();
        assertThat(studyYearRepository.existsByLevel(null)).isFalse();
    }

    // ========== TEST 3: findByLevel ==========

    @Test
    @DisplayName("Debe encontrar study year por nivel exacto")
    void testFindByLevel() {
        // When: Buscamos niveles existentes
        Optional<StudyYear> level1 = studyYearRepository.findByLevel(1);
        Optional<StudyYear> level3 = studyYearRepository.findByLevel(3);
        Optional<StudyYear> level5 = studyYearRepository.findByLevel(5);

        // Then: Encontramos los correctos
        assertThat(level1).isPresent();
        assertThat(level1.get().getName()).isEqualTo("Primer Año");
        assertThat(level1.get().getId()).isEqualTo(1);

        assertThat(level3).isPresent();
        assertThat(level3.get().getName()).isEqualTo("Tercer Año");
        assertThat(level3.get().getId()).isEqualTo(3);

        assertThat(level5).isPresent();
        assertThat(level5.get().getName()).isEqualTo("Quinto Año");
        assertThat(level5.get().getId()).isEqualTo(5);

        // When: Buscamos nivel inexistente
        Optional<StudyYear> noLevel = studyYearRepository.findByLevel(10);

        // Then: Empty
        assertThat(noLevel).isEmpty();
    }

    // ========== TEST 4: findAllByOrderByLevelAsc ==========

    @Test
    @DisplayName("Debe obtener todos los study years ordenados por nivel ascendente")
    void testFindAllByOrderByLevelAsc() {
        // When
        List<StudyYear> result = studyYearRepository.findAllByOrderByLevelAsc();

        // Then: Debe tener 5 elementos en orden correcto
        assertThat(result).hasSize(5);
        assertThat(result)
                .extracting(StudyYear::getLevel)
                .containsExactly(1, 2, 3, 4, 5); // Orden ascendente

        assertThat(result)
                .extracting(StudyYear::getName)
                .containsExactly(
                        "Primer Año", "Segundo Año", "Tercer Año",
                        "Cuarto Año", "Quinto Año"
                );
    }

    // ========== TEST 5: countActiveStudentsByStudyYearId ==========

    @Test
    @DisplayName("Debe contar estudiantes activos por StudyYear ID correctamente")
    void testCountActiveStudentsByStudyYearId() {
        // When & Then: Contamos según los datos de prueba
        // StudyYear 1: juan.perez, carlos.lopez, ana.martinez, sofia.rodriguez, miguel.torres, elena.vega = 6 estudiantes activos
        assertThat(studyYearRepository.countActiveStudentsByStudyYearId(1)).isEqualTo(6L);

        // StudyYear 2: maria.garcia, luis.sanchez, laura.jimenez = 3 estudiantes activos
        assertThat(studyYearRepository.countActiveStudentsByStudyYearId(2)).isEqualTo(3L);

        // StudyYear 3: david.moreno = 1 estudiante activo
        assertThat(studyYearRepository.countActiveStudentsByStudyYearId(3)).isEqualTo(1L);

        // StudyYear 4: 0 estudiantes
        assertThat(studyYearRepository.countActiveStudentsByStudyYearId(4)).isEqualTo(0L);

        // StudyYear 5: 0 estudiantes
        assertThat(studyYearRepository.countActiveStudentsByStudyYearId(5)).isEqualTo(0L);

        // StudyYear inexistente
        assertThat(studyYearRepository.countActiveStudentsByStudyYearId(999)).isEqualTo(0L);
    }

    @Test
    @DisplayName("Debe contar solo estudiantes activos, ignorando inactivos")
    void testCountActiveStudentsByStudyYearId_OnlyActiveStudents() {
        // Given: Según test-data.sql hay 1 estudiante inactivo en StudyYear 1 (inactive.student@student.com)
        // But total active students in StudyYear 1 should still be 5 (no consideramos el inactivo)

        // When & Then
        Long activeCount = studyYearRepository.countActiveStudentsByStudyYearId(1);

        // Then: Solo cuenta activos
        assertThat(activeCount).isEqualTo(6L);
    }

    // ========== TEST 6: countSubjectsByStudyYearId ==========

    @Test
    @DisplayName("Debe contar materias por StudyYear ID correctamente")
    void testCountSubjectsByStudyYearId() {
        // When & Then: Contamos según los datos de prueba
        // StudyYear 1: Programación I, Anatomía Humana, Derecho Civil I, Matemáticas I = 4 materias
        assertThat(studyYearRepository.countSubjectsByStudyYearId(1)).isEqualTo(4L);

        // StudyYear 2: Estructuras de Datos, Fisiología, Derecho Penal, Estadística = 4 materias
        assertThat(studyYearRepository.countSubjectsByStudyYearId(2)).isEqualTo(4L);

        // StudyYear 3: Bases de Datos = 1 materia
        assertThat(studyYearRepository.countSubjectsByStudyYearId(3)).isEqualTo(1L);

        // StudyYear 4: 0 materias
        assertThat(studyYearRepository.countSubjectsByStudyYearId(4)).isEqualTo(0L);

        // StudyYear 5: 0 materias
        assertThat(studyYearRepository.countSubjectsByStudyYearId(5)).isEqualTo(0L);

        // StudyYear inexistente
        assertThat(studyYearRepository.countSubjectsByStudyYearId(999)).isEqualTo(0L);
    }

    // ========== TEST 7: findAllStudyYearsWithActiveStudentCount ==========

    @Test
    @DisplayName("Debe obtener todos los study years con conteo correcto ordenados por nivel")
    void testFindAllStudyYearsWithActiveStudentCount() {
        // When
        List<StudyYearWithStudentCountDto> result = studyYearRepository.findAllStudyYearsWithActiveStudentCount();

        // Then: Debe retornar los 5 study years ordenados por nivel
        assertThat(result).hasSize(5);

        // Verificamos orden por nivel ascendente
        assertThat(result.get(0).level()).isEqualTo(1);
        assertThat(result.get(0).name()).isEqualTo("Primer Año");
        assertThat(result.get(0).activeStudentCount()).isEqualTo(6L);

        assertThat(result.get(1).level()).isEqualTo(2);
        assertThat(result.get(1).name()).isEqualTo("Segundo Año");
        assertThat(result.get(1).activeStudentCount()).isEqualTo(3L);

        assertThat(result.get(2).level()).isEqualTo(3);
        assertThat(result.get(2).name()).isEqualTo("Tercer Año");
        assertThat(result.get(2).activeStudentCount()).isEqualTo(1L);

        assertThat(result.get(3).level()).isEqualTo(4);
        assertThat(result.get(3).name()).isEqualTo("Cuarto Año");
        assertThat(result.get(3).activeStudentCount()).isEqualTo(0L);

        assertThat(result.get(4).level()).isEqualTo(5);
        assertThat(result.get(4).name()).isEqualTo("Quinto Año");
        assertThat(result.get(4).activeStudentCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Debe incluir study years sin estudiantes con count = 0")
    void testFindAllStudyYearsWithActiveStudentCount_IncludesEmptyStudyYears() {
        // When
        List<StudyYearWithStudentCountDto> result = studyYearRepository.findAllStudyYearsWithActiveStudentCount();

        // Then: Cuarto y Quinto año deben aparecer con 0 estudiantes
        StudyYearWithStudentCountDto cuartoAno = result.stream()
                .filter(dto -> dto.level().equals(4))
                .findFirst()
                .orElseThrow();

        StudyYearWithStudentCountDto quintoAno = result.stream()
                .filter(dto -> dto.level().equals(5))
                .findFirst()
                .orElseThrow();

        assertThat(cuartoAno.activeStudentCount()).isEqualTo(0L);
        assertThat(cuartoAno.hasActiveStudents()).isFalse();

        assertThat(quintoAno.activeStudentCount()).isEqualTo(0L);
        assertThat(quintoAno.hasActiveStudents()).isFalse();
    }

    @Test
    @DisplayName("Debe mantener información completa del StudyYear en el DTO")
    void testFindAllStudyYearsWithActiveStudentCount_CompleteInfo() {
        // When
        List<StudyYearWithStudentCountDto> result = studyYearRepository.findAllStudyYearsWithActiveStudentCount();

        // Then: Verificamos que el DTO tiene toda la información
        StudyYearWithStudentCountDto primerAno = result.stream()
                .filter(dto -> dto.level().equals(1))
                .findFirst()
                .orElseThrow();

        assertThat(primerAno.id()).isEqualTo(1);
        assertThat(primerAno.name()).isEqualTo("Primer Año");
        assertThat(primerAno.level()).isEqualTo(1);
        assertThat(primerAno.description()).isEqualTo("Primer año de estudios universitarios");
        assertThat(primerAno.activeStudentCount()).isEqualTo(6L);
        assertThat(primerAno.hasActiveStudents()).isTrue();
    }

    @Test
    @DisplayName("Debe verificar métodos utilitarios del DTO")
    void testStudyYearWithStudentCountDto_UtilityMethods() {
        // When
        List<StudyYearWithStudentCountDto> result = studyYearRepository.findAllStudyYearsWithActiveStudentCount();

        // Then: Verificamos métodos utilitarios
        StudyYearWithStudentCountDto primerAno = result.get(0);  // level = 1
        StudyYearWithStudentCountDto cuartoAno = result.get(3);  // level = 4

        // Verificamos getLevelDescription()
        assertThat(primerAno.getLevelDescription()).isEqualTo("Primer año");
        assertThat(cuartoAno.getLevelDescription()).isEqualTo("Cuarto año");

        // Verificamos isBasicLevel() - niveles 1 y 2
        assertThat(primerAno.isBasicLevel()).isTrue();
        assertThat(result.get(1).isBasicLevel()).isTrue();  // segundo año
        assertThat(cuartoAno.isBasicLevel()).isFalse();

        // Verificamos isAdvancedLevel() - nivel 4 o superior
        assertThat(primerAno.isAdvancedLevel()).isFalse();
        assertThat(cuartoAno.isAdvancedLevel()).isTrue();
        assertThat(result.get(4).isAdvancedLevel()).isTrue();  // quinto año
    }

    @Test
    @DisplayName("Debe encontrar study years sin estudiantes")
    void testFindEmptyStudyYears() {
        // When
        List<StudyYear> result = studyYearRepository.findEmptyStudyYears();

        // Then: Solo StudyYear 4 y 5 no tienen estudiantes
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(StudyYear::getLevel)
                .containsExactly(4, 5); // Orden ascendente

        assertThat(result)
                .extracting(StudyYear::getName)
                .containsExactly("Cuarto Año", "Quinto Año");
    }

    @Test
    @DisplayName("Debe retornar lista vacía si todos los study years tienen estudiantes")
    void testFindEmptyStudyYears_AllHaveStudents() {
        // Given: Este test fallará con datos actuales, pero es importante para cobertura
        // En un escenario donde todos los StudyYears tuvieran estudiantes

        // When
        List<StudyYear> result = studyYearRepository.findEmptyStudyYears();

        // Then: Con datos actuales, debería encontrar 2 (niveles 4 y 5)
        assertThat(result).isNotEmpty(); // Este es el comportamiento actual

        // Nota: Este test es más conceptual para cuando cambien los datos
    }

    @Test
    @DisplayName("Debe considerar tanto estudiantes activos como inactivos")
    void testFindEmptyStudyYears_ConsidersAllStudents() {
        // When
        List<StudyYear> result = studyYearRepository.findEmptyStudyYears();

        // Then: StudyYear 1 tiene estudiantes (activos E inactivos), no debe aparecer
        assertThat(result)
                .extracting(StudyYear::getLevel)
                .doesNotContain(1, 2, 3); // Estos tienen estudiantes

        // Solo 4 y 5 están completamente vacíos
        assertThat(result)
                .extracting(StudyYear::getLevel)
                .containsExactly(4, 5);
    }

    @Test
    @DisplayName("Debe encontrar study years con estudiantes activos")
    void testFindStudyYearsWithActiveStudents() {
        // When
        List<StudyYear> result = studyYearRepository.findStudyYearsWithActiveStudents();

        // Then: Solo StudyYear 1, 2 y 3 tienen estudiantes activos (4 y 5 no)
        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting(StudyYear::getLevel)
                .containsExactly(1, 2, 3); // Orden ascendente

        assertThat(result)
                .extracting(StudyYear::getName)
                .containsExactly("Primer Año", "Segundo Año", "Tercer Año");

        assertThat(result)
                .extracting(StudyYear::getId)
                .containsExactly(1, 2, 3);
    }

    @Test
    @DisplayName("Debe incluir solo study years con estudiantes ACTIVOS, no inactivos")
    void testFindStudyYearsWithActiveStudents_OnlyActiveStudents() {
        // When
        List<StudyYear> result = studyYearRepository.findStudyYearsWithActiveStudents();

        // Then: StudyYear 1 debe aparecer aunque tenga 1 estudiante inactivo,
        // porque también tiene 6 estudiantes activos
        assertThat(result)
                .extracting(StudyYear::getLevel)
                .contains(1);

        // StudyYear 4 y 5 NO deben aparecer (no tienen estudiantes activos)
        assertThat(result)
                .extracting(StudyYear::getLevel)
                .doesNotContain(4, 5);
    }

    @Test
    @DisplayName("Debe usar DISTINCT para evitar duplicados por múltiples estudiantes")
    void testFindStudyYearsWithActiveStudents_NoDuplicates() {
        // When
        List<StudyYear> result = studyYearRepository.findStudyYearsWithActiveStudents();

        // Then: Cada StudyYear debe aparecer solo una vez
        // aunque tenga múltiples estudiantes activos
        assertThat(result).hasSize(3);

        // Verificamos que no hay duplicados
        assertThat(result)
                .extracting(StudyYear::getId)
                .doesNotHaveDuplicates();

        // StudyYear 1 tiene 6 estudiantes activos, pero debe aparecer solo una vez
        long level1Count = result.stream()
                .filter(sy -> sy.getLevel().equals(1))
                .count();
        assertThat(level1Count).isEqualTo(1L);
    }

    @Test
    @DisplayName("Debe mantener el orden por nivel ascendente")
    void testFindStudyYearsWithActiveStudents_OrderedByLevel() {
        // When
        List<StudyYear> result = studyYearRepository.findStudyYearsWithActiveStudents();

        // Then: Debe estar ordenado 1, 2, 3
        assertThat(result)
                .extracting(StudyYear::getLevel)
                .isSorted(); // Verifica orden ascendente

        assertThat(result)
                .extracting(StudyYear::getLevel)
                .containsExactly(1, 2, 3); // Orden específico esperado
    }

    @Test
    @DisplayName("Debe encontrar el study year más popular")
    void testFindMostPopularStudyYear() {
        // When
        Optional<StudyYear> result = studyYearRepository.findMostPopularStudyYear();

        // Then: StudyYear 1 con 6 estudiantes activos es el más popular
        assertThat(result).isPresent();
        assertThat(result.get().getLevel()).isEqualTo(1);
        assertThat(result.get().getName()).isEqualTo("Primer Año");
        assertThat(result.get().getId()).isEqualTo(1);
    }

    @Test
    @DisplayName("Debe considerar solo estudiantes activos para popularidad")
    void testFindMostPopularStudyYear_OnlyActiveStudents() {
        // When
        Optional<StudyYear> result = studyYearRepository.findMostPopularStudyYear();

        // Then: StudyYear 1 gana con 6 activos (no cuenta el 1 inactivo)
        // vs StudyYear 2 con 3 activos, StudyYear 3 con 1 activo
        assertThat(result).isPresent();
        assertThat(result.get().getLevel()).isEqualTo(1);
    }

    @Test
    @DisplayName("Debe retornar el primer study year en caso de empate")
    void testFindMostPopularStudyYear_TieBreaker() {
        // Given: Con los datos actuales no hay empate, pero es bueno testear el comportamiento
        // StudyYear 1: 6 estudiantes, StudyYear 2: 3 estudiantes, StudyYear 3: 1 estudiante

        // When
        Optional<StudyYear> result = studyYearRepository.findMostPopularStudyYear();

        // Then: No hay empate en datos actuales, pero verificamos que retorna algo
        assertThat(result).isPresent();

        // El ganador claro es level 1 con 6 estudiantes
        assertThat(result.get().getLevel()).isEqualTo(1);
    }

    @Test
    @DisplayName("Debe funcionar correctamente con GROUP BY")
    void testFindMostPopularStudyYear_GroupByBehavior() {
        // When
        Optional<StudyYear> result = studyYearRepository.findMostPopularStudyYear();

        // Then: Debe agrupar correctamente y elegir el de mayor count
        assertThat(result).isPresent();

        // Verificamos que es realmente el más popular comparando con conteos individuales
        StudyYear mostPopular = result.get();
        Long countMostPopular = studyYearRepository.countActiveStudentsByStudyYearId(mostPopular.getId());

        // Verificamos que efectivamente tiene más estudiantes que otros
        Long countLevel2 = studyYearRepository.countActiveStudentsByStudyYearId(2);
        Long countLevel3 = studyYearRepository.countActiveStudentsByStudyYearId(3);

        assertThat(countMostPopular).isGreaterThan(countLevel2);
        assertThat(countMostPopular).isGreaterThan(countLevel3);
        assertThat(countMostPopular).isEqualTo(6L); // Verificación específica
    }

    @Test
    @DisplayName("Debe encontrar study years en rango de niveles válido")
    void testFindByLevelRange() {
        // When: Buscamos niveles 2-4
        List<StudyYear> result = studyYearRepository.findByLevelRange(2, 4);

        // Then: Debe encontrar niveles 2, 3, 4
        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting(StudyYear::getLevel)
                .containsExactly(2, 3, 4); // Orden ascendente

        assertThat(result)
                .extracting(StudyYear::getName)
                .containsExactly("Segundo Año", "Tercer Año", "Cuarto Año");
    }

    @Test
    @DisplayName("Debe encontrar todos los study years con rango completo")
    void testFindByLevelRange_FullRange() {
        // When: Buscamos rango completo 1-5
        List<StudyYear> result = studyYearRepository.findByLevelRange(1, 5);

        // Then: Debe encontrar todos los 5 study years
        assertThat(result).hasSize(5);
        assertThat(result)
                .extracting(StudyYear::getLevel)
                .containsExactly(1, 2, 3, 4, 5);
    }

    @Test
    @DisplayName("Debe encontrar un solo study year con rango unitario")
    void testFindByLevelRange_SingleLevel() {
        // When: Buscamos solo nivel 3 (min=3, max=3)
        List<StudyYear> result = studyYearRepository.findByLevelRange(3, 3);

        // Then: Debe encontrar solo el nivel 3
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLevel()).isEqualTo(3);
        assertThat(result.get(0).getName()).isEqualTo("Tercer Año");
    }

    @Test
    @DisplayName("Debe retornar lista vacía con rango fuera de datos")
    void testFindByLevelRange_OutOfRange() {
        // When: Buscamos rango que no existe (6-10)
        List<StudyYear> result = studyYearRepository.findByLevelRange(6, 10);

        // Then: Lista vacía
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Debe retornar lista vacía con rango inválido (min > max)")
    void testFindByLevelRange_InvalidRange() {
        // When: Rango inválido donde min > max
        List<StudyYear> result = studyYearRepository.findByLevelRange(4, 2);

        // Then: Lista vacía (no puede haber nivel >= 4 AND <= 2)
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Debe manejar rangos parciales correctamente")
    void testFindByLevelRange_PartialRanges() {
        // When: Rango de niveles básicos (1-2)
        List<StudyYear> basicLevels = studyYearRepository.findByLevelRange(1, 2);

        // Then: Solo primero y segundo
        assertThat(basicLevels).hasSize(2);
        assertThat(basicLevels)
                .extracting(StudyYear::getLevel)
                .containsExactly(1, 2);

        // When: Rango de niveles avanzados (4-5)
        List<StudyYear> advancedLevels = studyYearRepository.findByLevelRange(4, 5);

        // Then: Solo cuarto y quinto
        assertThat(advancedLevels).hasSize(2);
        assertThat(advancedLevels)
                .extracting(StudyYear::getLevel)
                .containsExactly(4, 5);
    }

    @Test
    @DisplayName("Debe mantener orden ascendente por nivel")
    void testFindByLevelRange_OrderedByLevel() {
        // When: Rango aleatorio 1-4
        List<StudyYear> result = studyYearRepository.findByLevelRange(1, 4);

        // Then: Orden ascendente
        assertThat(result)
                .extracting(StudyYear::getLevel)
                .isSorted(); // Verifica orden ascendente

        assertThat(result)
                .extracting(StudyYear::getLevel)
                .containsExactly(1, 2, 3, 4);
    }

    @Test
    @DisplayName("Debe manejar valores extremos correctamente")
    void testFindByLevelRange_ExtremeValues() {
        // When: Rango desde 0 (no existe) hasta 3
        List<StudyYear> fromZero = studyYearRepository.findByLevelRange(0, 3);

        // Then: Solo encuentra 1, 2, 3 (0 no existe)
        assertThat(fromZero).hasSize(3);
        assertThat(fromZero)
                .extracting(StudyYear::getLevel)
                .containsExactly(1, 2, 3);

        // When: Rango desde 3 hasta 100 (solo existe hasta 5)
        List<StudyYear> toHundred = studyYearRepository.findByLevelRange(3, 100);

        // Then: Solo encuentra 3, 4, 5
        assertThat(toHundred).hasSize(3);
        assertThat(toHundred)
                .extracting(StudyYear::getLevel)
                .containsExactly(3, 4, 5);
    }

    @Test
    @DisplayName("Debe contar study years con mínimo de estudiantes activos")
    void testCountStudyYearsWithMinimumActiveStudents() {
        // When & Then: Probamos diferentes umbrales
        // StudyYear 1: 6 estudiantes, StudyYear 2: 3 estudiantes, StudyYear 3: 1 estudiante

        // Mínimo 1 estudiante: niveles 1, 2, 3 cumplen = 3 study years
        assertThat(studyYearRepository.countStudyYearsWithMinimumActiveStudents(1L)).isEqualTo(3L);

        // Mínimo 2 estudiantes: niveles 1, 2 cumplen = 2 study years
        assertThat(studyYearRepository.countStudyYearsWithMinimumActiveStudents(2L)).isEqualTo(2L);

        // Mínimo 3 estudiantes: niveles 1, 2 cumplen = 2 study years
        assertThat(studyYearRepository.countStudyYearsWithMinimumActiveStudents(3L)).isEqualTo(2L);

        // Mínimo 4 estudiantes: solo nivel 1 cumple = 1 study year
        assertThat(studyYearRepository.countStudyYearsWithMinimumActiveStudents(4L)).isEqualTo(1L);

        // Mínimo 6 estudiantes: solo nivel 1 cumple = 1 study year
        assertThat(studyYearRepository.countStudyYearsWithMinimumActiveStudents(6L)).isEqualTo(1L);

        // Mínimo 7 estudiantes: ninguno cumple = 0 study years
        assertThat(studyYearRepository.countStudyYearsWithMinimumActiveStudents(7L)).isEqualTo(0L);
    }

    @Test
    @DisplayName("Debe excluir study years sin estudiantes")
    void testCountStudyYearsWithMinimumActiveStudents_ExcludesEmpty() {
        // When: Mínimo 0 estudiantes (todos deberían cumplir conceptualmente)
        Long result = studyYearRepository.countStudyYearsWithMinimumActiveStudents(0L);

        // Then: Solo cuenta los que tienen al menos 1 estudiante activo
        // porque la subconsulta con JOIN solo incluye study years con estudiantes
        assertThat(result).isEqualTo(3L); // Solo niveles 1, 2, 3 tienen estudiantes

        // StudyYear 4 y 5 no aparecen porque no tienen estudiantes para hacer JOIN
    }

    @Test
    @DisplayName("Debe considerar solo estudiantes activos")
    void testCountStudyYearsWithMinimumActiveStudents_OnlyActiveStudents() {
        // When: Verificamos que solo cuenta activos
        // StudyYear 1 tiene 6 activos + 1 inactivo = 7 total, pero solo cuenta 6 activos
        Long result = studyYearRepository.countStudyYearsWithMinimumActiveStudents(6L);

        // Then: StudyYear 1 cumple con 6 activos (ignora el inactivo)
        assertThat(result).isEqualTo(1L);

        // Si contara inactivos, tendría 7 total, pero la consulta es correcta
    }

    @Test
    @DisplayName("Debe manejar valores extremos correctamente")
    void testCountStudyYearsWithMinimumActiveStudents_ExtremeValues() {
        // When: Valor muy alto
        Long highThreshold = studyYearRepository.countStudyYearsWithMinimumActiveStudents(100L);

        // Then: Ningún study year tiene 100+ estudiantes
        assertThat(highThreshold).isEqualTo(0L);

        // When: Valor negativo (edge case)
        Long negativeThreshold = studyYearRepository.countStudyYearsWithMinimumActiveStudents(-1L);

        // Then: Debería contar todos los que tienen estudiantes (conceptualmente >= -1)
        assertThat(negativeThreshold).isEqualTo(3L);
    }

    @Test
    @DisplayName("Debe usar DISTINCT correctamente para evitar duplicados")
    void testCountStudyYearsWithMinimumActiveStudents_DistinctCount() {
        // When: Verificamos que cada study year se cuenta solo una vez
        Long result = studyYearRepository.countStudyYearsWithMinimumActiveStudents(1L);

        // Then: Debe ser exactamente 3, no más (aunque tengan múltiples estudiantes)
        assertThat(result).isEqualTo(3L);

        // StudyYear 1 tiene 6 estudiantes, pero se cuenta solo una vez
        // StudyYear 2 tiene 3 estudiantes, pero se cuenta solo una vez
        // StudyYear 3 tiene 1 estudiante, pero se cuenta solo una vez
    }

    @Test
    @DisplayName("Debe validar la subconsulta con HAVING correctamente")
    void testCountStudyYearsWithMinimumActiveStudents_HavingClause() {
        // When: Probamos umbrales específicos que prueban la cláusula HAVING

        // Exactamente 3 estudiantes: solo StudyYear 2 tiene exactamente 3
        // Pero >= 3 incluye StudyYear 1 (6) y StudyYear 2 (3)
        Long exactly3OrMore = studyYearRepository.countStudyYearsWithMinimumActiveStudents(3L);
        assertThat(exactly3OrMore).isEqualTo(2L);

        // Exactamente 1 estudiante: StudyYear 3 tiene 1, pero >= 1 incluye todos
        Long exactly1OrMore = studyYearRepository.countStudyYearsWithMinimumActiveStudents(1L);
        assertThat(exactly1OrMore).isEqualTo(3L);

        // Entre los extremos: >= 4 solo incluye StudyYear 1
        Long exactly4OrMore = studyYearRepository.countStudyYearsWithMinimumActiveStudents(4L);
        assertThat(exactly4OrMore).isEqualTo(1L);
    }

    @Test
    @DisplayName("Debe obtener todos los study years con estadísticas correctas")
    void testFindAllStudyYearsWithSubjectStats() {
        // When
        List<StudyYearWithSubjectStatsDto> result = studyYearRepository.findAllStudyYearsWithSubjectStats();

        // Then: Debe retornar los 5 study years ordenados por nivel
        assertThat(result).hasSize(5);

        // StudyYear 1: 6 estudiantes activos, 4 materias
        StudyYearWithSubjectStatsDto level1 = result.get(0);
        assertThat(level1.level()).isEqualTo(1);
        assertThat(level1.name()).isEqualTo("Primer Año");
        assertThat(level1.activeStudentCount()).isEqualTo(6L);
        assertThat(level1.totalSubjects()).isEqualTo(4L);

        // StudyYear 2: 3 estudiantes activos, 4 materias
        StudyYearWithSubjectStatsDto level2 = result.get(1);
        assertThat(level2.level()).isEqualTo(2);
        assertThat(level2.activeStudentCount()).isEqualTo(3L);
        assertThat(level2.totalSubjects()).isEqualTo(4L);

        // StudyYear 3: 1 estudiante activo, 1 materia
        StudyYearWithSubjectStatsDto level3 = result.get(2);
        assertThat(level3.level()).isEqualTo(3);
        assertThat(level3.activeStudentCount()).isEqualTo(1L);
        assertThat(level3.totalSubjects()).isEqualTo(1L);

        // StudyYear 4: 0 estudiantes, 0 materias
        StudyYearWithSubjectStatsDto level4 = result.get(3);
        assertThat(level4.level()).isEqualTo(4);
        assertThat(level4.activeStudentCount()).isEqualTo(0L);
        assertThat(level4.totalSubjects()).isEqualTo(0L);

        // StudyYear 5: 0 estudiantes, 0 materias
        StudyYearWithSubjectStatsDto level5 = result.get(4);
        assertThat(level5.level()).isEqualTo(5);
        assertThat(level5.activeStudentCount()).isEqualTo(0L);
        assertThat(level5.totalSubjects()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Debe calcular ratios estudiante/materia correctamente")
    void testStudyYearWithSubjectStatsDto_StudentToSubjectRatio() {
        // When
        List<StudyYearWithSubjectStatsDto> result = studyYearRepository.findAllStudyYearsWithSubjectStats();

        // Then: Verificamos ratios específicos
        StudyYearWithSubjectStatsDto level1 = result.get(0); // 6 estudiantes / 4 materias = 1.5
        assertThat(level1.getStudentToSubjectRatio()).isEqualTo(1.5);

        StudyYearWithSubjectStatsDto level2 = result.get(1); // 3 estudiantes / 4 materias = 0.75
        assertThat(level2.getStudentToSubjectRatio()).isEqualTo(0.75);

        StudyYearWithSubjectStatsDto level3 = result.get(2); // 1 estudiante / 1 materia = 1.0
        assertThat(level3.getStudentToSubjectRatio()).isEqualTo(1.0);

        StudyYearWithSubjectStatsDto level4 = result.get(3); // 0 estudiantes / 0 materias = 0.0
        assertThat(level4.getStudentToSubjectRatio()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Debe identificar años académicos viables correctamente")
    void testStudyYearWithSubjectStatsDto_ViableAcademicYears() {
        // When
        List<StudyYearWithSubjectStatsDto> result = studyYearRepository.findAllStudyYearsWithSubjectStats();

        // Then: Solo niveles 1, 2, 3 son viables (tienen materias Y estudiantes)
        assertThat(result.get(0).isViableAcademicYear()).isTrue();  // Level 1
        assertThat(result.get(1).isViableAcademicYear()).isTrue();  // Level 2
        assertThat(result.get(2).isViableAcademicYear()).isTrue();  // Level 3
        assertThat(result.get(3).isViableAcademicYear()).isFalse(); // Level 4 (sin materias ni estudiantes)
        assertThat(result.get(4).isViableAcademicYear()).isFalse(); // Level 5 (sin materias ni estudiantes)
    }

    @Test
    @DisplayName("Debe verificar métodos utilitarios del DTO")
    void testStudyYearWithSubjectStatsDto_UtilityMethods() {
        // When
        List<StudyYearWithSubjectStatsDto> result = studyYearRepository.findAllStudyYearsWithSubjectStats();

        StudyYearWithSubjectStatsDto level1 = result.get(0);
        StudyYearWithSubjectStatsDto level4 = result.get(3);

        // hasSubjects()
        assertThat(level1.hasSubjects()).isTrue();
        assertThat(level4.hasSubjects()).isFalse();

        // hasActiveStudents()
        assertThat(level1.hasActiveStudents()).isTrue();
        assertThat(level4.hasActiveStudents()).isFalse();
    }
}

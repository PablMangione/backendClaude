package com.acainfo.backendclaude.dto.major;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * DTO que representa un Major con información de ingresos estimados
 *
 * Cálculo: Para cada materia del major, multiplicamos:
 * (precio_mensual_materia * número_estudiantes_activos_registrados)
 *
 * Ejemplo:
 * - Programación I: 150€/mes * 3 estudiantes = 450€/mes
 * - Bases de Datos: 200€/mes * 2 estudiantes = 400€/mes
 * - Total Informática: 850€/mes
 */
public record MajorRevenueDto(
        Integer id,
        String name,
        Long totalActiveStudents,
        Integer totalSubjects,
        Long estimatedMonthlyRevenue
) {
    /**
     * Calcula el ingreso promedio por estudiante
     */
    public BigDecimal averageRevenuePerStudent() {
        if (totalActiveStudents == null || totalActiveStudents == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(estimatedMonthlyRevenue).divide(
                BigDecimal.valueOf(totalActiveStudents),
                RoundingMode.CEILING
        );
    }

    /**
     * Verifica si el major genera ingresos
     */
    public boolean hasRevenue() {
        return estimatedMonthlyRevenue != null &&
                estimatedMonthlyRevenue.compareTo(0L) > 0;
    }
}

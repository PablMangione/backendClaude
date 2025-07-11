package com.acainfo.backendclaude.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * DTO con información de resumen para Major - incluye contadores de entidades relacionadas
 */
@Value
@Builder
public class MajorSummaryDto {
    Integer id;
    String name;
    String description;
    Instant createdAt;
    Long totalStudents;
    Long totalSubjects;
    Long activeStudents;

    /**
     * Constructor para usar en queries nativas/JPQL
     */
    public MajorSummaryDto(Integer id, String name, String description, Instant createdAt,
                           Long totalStudents, Long totalSubjects, Long activeStudents) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.totalStudents = totalStudents != null ? totalStudents : 0L;
        this.totalSubjects = totalSubjects != null ? totalSubjects : 0L;
        this.activeStudents = activeStudents != null ? activeStudents : 0L;
    }
}
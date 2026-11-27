package org.example.lfmnacional.dto;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    /** Tope de {@code size} en los listados paginados: evita traer la tabla entera de un saque. */
    public static final int MAX_SIZE = 100;
}

package org.example.lfmnacional.dto.usuario;

import jakarta.validation.constraints.Min;

public record RatingRequest(
        @Min(0) Integer elo,
        @Min(0) Integer safetyRating
) {
}

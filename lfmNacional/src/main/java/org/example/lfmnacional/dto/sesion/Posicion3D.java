package org.example.lfmnacional.dto.sesion;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Posicion3D(
        @JsonProperty("X") Double x,
        @JsonProperty("Y") Double y,
        @JsonProperty("Z") Double z
) {
}

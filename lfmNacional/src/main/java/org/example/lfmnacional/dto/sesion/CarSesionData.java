package org.example.lfmnacional.dto.sesion;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CarSesionData(
        @JsonProperty("CarId") Integer carId,
        @JsonProperty("Driver") DriverSesionData driver,
        @JsonProperty("Model") String model,
        @JsonProperty("Skin") String skin,
        @JsonProperty("BallastKG") Integer ballastKG,
        @JsonProperty("Restrictor") Integer restrictor
) {
}

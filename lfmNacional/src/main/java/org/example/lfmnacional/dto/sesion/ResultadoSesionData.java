package org.example.lfmnacional.dto.sesion;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ResultadoSesionData(
        @JsonProperty("DriverName") String driverName,
        @JsonProperty("DriverGuid") String driverGuid,
        @JsonProperty("CarId") Integer carId,
        @JsonProperty("CarModel") String carModel,
        @JsonProperty("BestLap") Long bestLap,
        @JsonProperty("TotalTime") Long totalTime,
        @JsonProperty("BallastKG") Integer ballastKG,
        @JsonProperty("Restrictor") Integer restrictor
) {
}

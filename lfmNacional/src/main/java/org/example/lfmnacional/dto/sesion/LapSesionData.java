package org.example.lfmnacional.dto.sesion;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record LapSesionData(
        @JsonProperty("DriverName") String driverName,
        @JsonProperty("DriverGuid") String driverGuid,
        @JsonProperty("CarId") Integer carId,
        @JsonProperty("CarModel") String carModel,
        @JsonProperty("Timestamp") Long timestamp,
        @JsonProperty("LapTime") Long lapTime,
        @JsonProperty("Sectors") List<Integer> sectors,
        @JsonProperty("Cuts") Integer cuts,
        @JsonProperty("BallastKG") Integer ballastKG,
        @JsonProperty("Tyre") String tyre,
        @JsonProperty("Restrictor") Integer restrictor
) {
}

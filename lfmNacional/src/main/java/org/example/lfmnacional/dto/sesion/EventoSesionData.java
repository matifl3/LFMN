package org.example.lfmnacional.dto.sesion;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EventoSesionData(
        @JsonProperty("Type") String type,
        @JsonProperty("CarId") Integer carId,
        @JsonProperty("Driver") DriverSesionData driver,
        @JsonProperty("OtherCarId") Integer otherCarId,
        @JsonProperty("OtherDriver") DriverSesionData otherDriver,
        @JsonProperty("ImpactSpeed") Double impactSpeed,
        @JsonProperty("WorldPosition") Posicion3D worldPosition,
        @JsonProperty("RelPosition") Posicion3D relPosition
) {
}

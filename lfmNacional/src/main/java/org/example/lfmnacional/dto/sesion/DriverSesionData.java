package org.example.lfmnacional.dto.sesion;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DriverSesionData(
        @JsonProperty("Name") String name,
        @JsonProperty("Team") String team,
        @JsonProperty("Nation") String nation,
        @JsonProperty("Guid") String guid,
        @JsonProperty("GuidsList") List<String> guidsList
) {
}

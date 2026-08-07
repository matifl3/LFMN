package org.example.lfmnacional.dto.sesion;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record SesionServerData(
        @JsonProperty("TrackName") String trackName,
        @JsonProperty("TrackConfig") String trackConfig,
        @JsonProperty("Type") String type,
        @JsonProperty("DurationSecs") Integer durationSecs,
        @JsonProperty("RaceLaps") Integer raceLaps,
        @JsonProperty("Cars") List<CarSesionData> cars,
        @JsonProperty("Result") List<ResultadoSesionData> result,
        @JsonProperty("Laps") List<LapSesionData> laps,
        @JsonProperty("Events") List<EventoSesionData> events
) {
}

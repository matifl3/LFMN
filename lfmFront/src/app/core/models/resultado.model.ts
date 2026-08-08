export interface ResultadoCarreraResponse {
  id: number;
  carreraId: number;
  usuarioId: number;
  posicionFinal: number;
  tiempoTotal: number | null;
  vueltaRapida: number | null;
  poles: boolean | null;
  finalizo: boolean | null;
  eloGanado: number | null;
  srGanado: number | null;
}

export interface ResultadoCarreraRequest {
  carreraId: number;
  usuarioId: number;
  posicionFinal: number;
  tiempoTotal?: number | null;
  vueltaRapida?: number | null;
  poles?: boolean | null;
  finalizo?: boolean | null;
  eloGanado?: number | null;
  srGanado?: number | null;
}

export interface CargarResultadosRequest {
  carreraId: number;
  resultados: ResultadoCarreraRequest[];
}

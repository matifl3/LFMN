export enum EstadoCampeonato {
  ACTIVO = 'ACTIVO',
  CERRADO = 'CERRADO',
}

export interface CampeonatoResponse {
  id: number;
  nombre: string;
  temporada: string | null;
  sistemaPuntos: string | null;
  estado: EstadoCampeonato;
  categoriaId: number;
  categoriaNombre: string;
}

export interface CampeonatoRequest {
  nombre: string;
  temporada?: string | null;
  sistemaPuntos?: string | null;
  categoriaId: number;
}

export interface TablaPosicionResponse {
  usuarioId: number;
  nombrePiloto: string;
  puntos: number;
  posicion: number;
}

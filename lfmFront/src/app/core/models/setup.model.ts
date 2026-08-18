export interface SetupResponse {
  id: number;
  titulo: string;
  descripcion: string;
  circuito: string;
  vehiculo: string;
  archivo: string;
  autorId: number;
  categoriaId: number;
  fechaPublicacion: string;
  promedioCalificacion: number;
}

export interface SetupRequest {
  titulo: string;
  descripcion: string;
  circuito: string;
  vehiculo: string;
  archivo: string;
  categoriaId: number;
}

export interface SetupComentarioRequest {
  usuarioId: number;
  texto: string;
}

export interface SetupComentarioResponse {
  id: number;
  setupId: number;
  usuarioId: number;
  texto: string;
  fecha: string;
}

export interface SetupCalificacionRequest {
  usuarioId: number;
  puntaje: number;
}

export interface SetupCalificacionResponse {
  id: number;
  setupId: number;
  usuarioId: number;
  puntaje: number;
}

export enum EstadoInscripcion {
  INSCRIPTO = 'INSCRIPTO',
  LISTA_ESPERA = 'LISTA_ESPERA',
  CANCELADA = 'CANCELADA',
}

export interface InscripcionResponse {
  id: number;
  carreraId: number;
  usuarioId: number;
  estado: EstadoInscripcion;
  fechaInscripcion: string;
}

export interface InscripcionRequest {
  carreraId: number;
  usuarioId: number;
}

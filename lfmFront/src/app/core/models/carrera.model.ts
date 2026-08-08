export enum EstadoCarrera {
  PROGRAMADA = 'PROGRAMADA',
  INSCRIPCIONES_ABIERTAS = 'INSCRIPCIONES_ABIERTAS',
  INSCRIPCIONES_CERRADAS = 'INSCRIPCIONES_CERRADAS',
  EN_CURSO = 'EN_CURSO',
  FINALIZADA = 'FINALIZADA',
  CANCELADA = 'CANCELADA',
}

export interface CarreraResponse {
  id: number;
  nombre: string;
  fecha: string;
  circuito: string;
  categoriaId: number;
  categoriaNombre: string;
  estado: EstadoCarrera;
  cupoMaximo: number | null;
  servidor: string | null;
  contrasenaServidor: string | null;
  archivoId: number | null;
  archivoNombre: string | null;
}

export interface CarreraRequest {
  nombre: string;
  fecha: string;
  circuito: string;
  categoriaId: number;
  estado?: EstadoCarrera | null;
  cupoMaximo?: number | null;
  servidor?: string | null;
  contrasenaServidor?: string | null;
  archivoId?: number | null;
}

export interface VincularArchivoRequest {
  archivoId: number;
}

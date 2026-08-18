export enum TipoCondicionLogro {
  VICTORIAS = 'VICTORIAS',
  PODIOS = 'PODIOS',
  CARRERAS = 'CARRERAS',
  POLES = 'POLES',
  VUELTAS_RAPIDAS = 'VUELTAS_RAPIDAS',
  CARRERAS_COMPLETADAS = 'CARRERAS_COMPLETADAS',
  ELO = 'ELO',
}

export enum TipoRecompensa {
  VIRTUAL = 'VIRTUAL',
  FISICA = 'FISICA',
  DESCUENTO = 'DESCUENTO',
  OTRA = 'OTRA',
}

export interface RecompensaResponse {
  id: number;
  logroId: number;
  descripcion: string;
  tipo: TipoRecompensa;
}

export interface RecompensaRequest {
  descripcion: string;
  tipo: TipoRecompensa;
}

export interface LogroResponse {
  id: number;
  nombre: string;
  descripcion: string;
  tipoCondicion: TipoCondicionLogro;
  valorCondicion: number;
  icono: string;
  recompensas: RecompensaResponse[];
}

export interface LogroRequest {
  nombre: string;
  descripcion: string;
  tipoCondicion: TipoCondicionLogro;
  valorCondicion: number;
  icono: string;
}

export interface UsuarioLogroResponse {
  logroId: number;
  nombre: string;
  descripcion: string;
  tipoCondicion: TipoCondicionLogro;
  valorCondicion: number;
  progreso: number;
  obtenido: boolean;
  fechaObtencion: string | null;
}

export interface UsuarioRecompensaResponse {
  recompensaId: number;
  descripcion: string;
  tipo: TipoRecompensa;
  reclamada: boolean;
}

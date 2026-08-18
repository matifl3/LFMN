export enum TipoNotificacion {
  CARRERA_INICIO = 'CARRERA_INICIO',
  PENALIZACION = 'PENALIZACION',
  LOGRO = 'LOGRO',
  RECOMPENSA = 'RECOMPENSA',
  ANUNCIO = 'ANUNCIO',
  INCIDENTE = 'INCIDENTE',
  APELACION = 'APELACION',
}

export interface NotificacionResponse {
  id: number;
  usuarioId: number;
  tipo: TipoNotificacion;
  mensaje: string;
  leida: boolean;
  fecha: string;
  link: string | null;
}

import { Pipe, PipeTransform } from '@angular/core';

export interface ChipInfo {
  clase: string;
  label: string;
}

export const CARRERA_CHIPS: Record<string, ChipInfo> = {
  PROGRAMADA: { clase: 'chip-upcoming', label: 'Programada' },
  INSCRIPCIONES_ABIERTAS: { clase: 'chip-upcoming', label: 'Inscripciones abiertas' },
  INSCRIPCIONES_CERRADAS: { clase: 'chip-closed', label: 'Cerrada' },
  EN_CURSO: { clase: 'chip-review', label: 'En curso' },
  FINALIZADA: { clase: 'chip-resolved', label: 'Finalizada' },
  CANCELADA: { clase: 'chip-rejected', label: 'Cancelada' },
};

export const INCIDENTE_CHIPS: Record<string, ChipInfo> = {
  PENDIENTE: { clase: 'chip-pending', label: 'Pendiente' },
  EN_ANALISIS: { clase: 'chip-review', label: 'En análisis' },
  RESUELTO: { clase: 'chip-resolved', label: 'Resuelto' },
};

export const APELACION_CHIPS: Record<string, ChipInfo> = {
  PENDIENTE: { clase: 'chip-pending', label: 'Pendiente' },
  APROBADA: { clase: 'chip-approved', label: 'Aprobada' },
  RECHAZADA: { clase: 'chip-rejected', label: 'Rechazada' },
};

export const VOTO_CHIPS: Record<string, ChipInfo> = {
  A_FAVOR: { clase: 'chip-resolved', label: 'A favor' },
  EN_CONTRA: { clase: 'chip-rejected', label: 'En contra' },
  ABSTENCION: { clase: 'chip-closed', label: 'Abstención' },
};

export const INSCRIPCION_CHIPS: Record<string, ChipInfo> = {
  LISTA_ESPERA: { clase: 'chip-pending', label: 'En espera' },
  CONFIRMADA: { clase: 'chip-confirmed', label: 'Confirmado' },
};

const MAPAS: Record<string, Record<string, ChipInfo>> = {
  carrera: CARRERA_CHIPS,
  incidente: INCIDENTE_CHIPS,
  apelacion: APELACION_CHIPS,
  voto: VOTO_CHIPS,
  inscripcion: INSCRIPCION_CHIPS,
};

@Pipe({ name: 'estadoChip', standalone: true })
export class EstadoChipPipe implements PipeTransform {
  transform(estado: string | null | undefined, tipo = 'generic'): ChipInfo {
    if (!estado) return { clase: 'chip-pending', label: '—' };
    const mapa = MAPAS[tipo];
    if (mapa && mapa[estado]) return mapa[estado];
    return { clase: 'chip-pending', label: estado };
  }
}
import { Pipe, PipeTransform } from '@angular/core';
import { t } from '../i18n/t';

export interface ChipInfo {
  clase: string;
  /** En los mapas de abajo es una clave de traducción; `transform` la devuelve traducida. */
  label: string;
}

export const CARRERA_CHIPS: Record<string, ChipInfo> = {
  PROGRAMADA: { clase: 'chip-upcoming', label: 'comun.chip.carrera.PROGRAMADA' },
  INSCRIPCIONES_ABIERTAS: {
    clase: 'chip-upcoming',
    label: 'comun.chip.carrera.INSCRIPCIONES_ABIERTAS',
  },
  INSCRIPCIONES_CERRADAS: {
    clase: 'chip-closed',
    label: 'comun.chip.carrera.INSCRIPCIONES_CERRADAS',
  },
  EN_CURSO: { clase: 'chip-review', label: 'comun.chip.carrera.EN_CURSO' },
  FINALIZADA: { clase: 'chip-resolved', label: 'comun.chip.carrera.FINALIZADA' },
  CANCELADA: { clase: 'chip-rejected', label: 'comun.chip.carrera.CANCELADA' },
};

export const INCIDENTE_CHIPS: Record<string, ChipInfo> = {
  PENDIENTE: { clase: 'chip-pending', label: 'comun.chip.incidente.PENDIENTE' },
  EN_ANALISIS: { clase: 'chip-review', label: 'comun.chip.incidente.EN_ANALISIS' },
  RESUELTO: { clase: 'chip-resolved', label: 'comun.chip.incidente.RESUELTO' },
};

export const APELACION_CHIPS: Record<string, ChipInfo> = {
  PENDIENTE: { clase: 'chip-pending', label: 'comun.chip.apelacion.PENDIENTE' },
  APROBADA: { clase: 'chip-approved', label: 'comun.chip.apelacion.APROBADA' },
  RECHAZADA: { clase: 'chip-rejected', label: 'comun.chip.apelacion.RECHAZADA' },
};

export const VOTO_CHIPS: Record<string, ChipInfo> = {
  A_FAVOR: { clase: 'chip-resolved', label: 'comun.chip.voto.A_FAVOR' },
  EN_CONTRA: { clase: 'chip-rejected', label: 'comun.chip.voto.EN_CONTRA' },
  ABSTENCION: { clase: 'chip-closed', label: 'comun.chip.voto.ABSTENCION' },
};

export const INSCRIPCION_CHIPS: Record<string, ChipInfo> = {
  LISTA_ESPERA: { clase: 'chip-pending', label: 'comun.chip.inscripcion.LISTA_ESPERA' },
  CONFIRMADA: { clase: 'chip-confirmed', label: 'comun.chip.inscripcion.CONFIRMADA' },
};

const MAPAS: Record<string, Record<string, ChipInfo>> = {
  carrera: CARRERA_CHIPS,
  incidente: INCIDENTE_CHIPS,
  apelacion: APELACION_CHIPS,
  voto: VOTO_CHIPS,
  inscripcion: INSCRIPCION_CHIPS,
};

@Pipe({ name: 'estadoChip', standalone: true, pure: false })
export class EstadoChipPipe implements PipeTransform {
  transform(estado: string | null | undefined, tipo = 'generic'): ChipInfo {
    if (!estado) return { clase: 'chip-pending', label: '—' };
    const mapa = MAPAS[tipo];
    if (mapa && mapa[estado]) return { clase: mapa[estado].clase, label: t(mapa[estado].label) };
    return { clase: 'chip-pending', label: estado };
  }
}

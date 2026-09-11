import { Pipe, PipeTransform } from '@angular/core';
import { fmtFecha, fmtFechaHora, fmtHora } from '../utils/formato';

@Pipe({ name: 'fmtFecha', standalone: true })
export class FmtFechaPipe implements PipeTransform {
  transform(iso: string | null | undefined): string {
    return fmtFecha(iso);
  }
}

@Pipe({ name: 'fmtFechaHora', standalone: true })
export class FmtFechaHoraPipe implements PipeTransform {
  transform(iso: string | null | undefined): string {
    return fmtFechaHora(iso);
  }
}

@Pipe({ name: 'fmtHora', standalone: true })
export class FmtHoraPipe implements PipeTransform {
  transform(iso: string | null | undefined): string {
    return fmtHora(iso);
  }
}
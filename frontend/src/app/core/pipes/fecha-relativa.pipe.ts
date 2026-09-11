import { Pipe, PipeTransform } from '@angular/core';
import { fmtRel } from '../utils/formato';

@Pipe({ name: 'fechaRelativa', standalone: true })
export class FechaRelativaPipe implements PipeTransform {
  transform(iso: string | null | undefined): string {
    return fmtRel(iso);
  }
}
import { Pipe, PipeTransform } from '@angular/core';
import { fmtDif } from '../utils/formato';

@Pipe({ name: 'fmtDif', standalone: true })
export class FmtDifPipe implements PipeTransform {
  transform(ms: number | null | undefined): string {
    return fmtDif(ms);
  }
}
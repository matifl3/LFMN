import { Pipe, PipeTransform } from '@angular/core';
import { fmtLap } from '../utils/formato';

@Pipe({ name: 'fmtLap', standalone: true })
export class FmtLapPipe implements PipeTransform {
  transform(ms: number | null | undefined): string {
    return fmtLap(ms);
  }
}
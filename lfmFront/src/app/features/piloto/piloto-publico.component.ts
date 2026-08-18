import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { firstValueFrom } from 'rxjs';

import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatChipsModule } from '@angular/material/chips';

import { UsuarioService } from '../../core/api/usuario.service';
import { LogroService } from '../../core/api/logro.service';
import { ResultadoService } from '../../core/api/resultado.service';
import { ErrorHandlerService } from '../../core/error/error-handler.service';

import { UsuarioResponse } from '../../core/models/usuario.model';
import { StatsResponse } from '../../core/models/usuario.model';
import { EloHistorialResponse } from '../../core/models/usuario.model';
import { SafetyRatingHistorialResponse } from '../../core/models/usuario.model';
import { UsuarioLogroResponse } from '../../core/models/logro.model';
import { ResultadoCarreraResponse } from '../../core/models/resultado.model';

@Component({
  selector: 'app-piloto-publico',
  imports: [
    DatePipe,
    MatCardModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatTableModule,
    MatTabsModule,
    MatChipsModule,
  ],
  templateUrl: './piloto-publico.component.html',
  styleUrl: './piloto-publico.component.scss',
})
export class PilotoPublicoComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly usuarioService = inject(UsuarioService);
  private readonly logroService = inject(LogroService);
  private readonly resultadoService = inject(ResultadoService);
  private readonly errorHandler = inject(ErrorHandlerService);

  readonly cargando = signal(true);
  readonly usuario = signal<UsuarioResponse | null>(null);
  readonly stats = signal<StatsResponse | null>(null);
  readonly historialElo = signal<EloHistorialResponse[]>([]);
  readonly historialSR = signal<SafetyRatingHistorialResponse[]>([]);
  readonly logros = signal<UsuarioLogroResponse[]>([]);
  readonly resultados = signal<ResultadoCarreraResponse[]>([]);

  readonly columnasElo = ['fecha', 'cambio', 'motivo'];
  readonly columnasResultados = ['posicion', 'tiempo', 'elo', 'sr'];

  private usuarioId = 0;

  constructor() {
    this.usuarioId = Number(this.route.snapshot.paramMap.get('id'));
    void this.cargar();
  }

  private async cargar() {
    this.cargando.set(true);
    try {
      const [usuario, stats, historialElo, historialSR, logros, resultados] = await Promise.all([
        firstValueFrom(this.usuarioService.obtener(this.usuarioId)),
        firstValueFrom(this.usuarioService.stats(this.usuarioId)),
        firstValueFrom(this.usuarioService.historialElo(this.usuarioId)),
        firstValueFrom(this.usuarioService.historialSafetyRating(this.usuarioId)),
        firstValueFrom(this.logroService.progresoPorUsuario(this.usuarioId)),
        firstValueFrom(this.resultadoService.deUsuario(this.usuarioId)),
      ]);
      this.usuario.set(usuario);
      this.stats.set(stats);
      this.historialElo.set(historialElo);
      this.historialSR.set(historialSR);
      this.logros.set(logros);
      this.resultados.set(resultados);
    } catch (error) {
      this.errorHandler.handle(error);
    } finally {
      this.cargando.set(false);
    }
  }

  formatTiempo(milisegundos: number | null): string {
    if (!milisegundos) {
      return 'DNF';
    }
    const ms = milisegundos % 1000;
    const s = Math.floor(milisegundos / 1000) % 60;
    const m = Math.floor(milisegundos / 60000) % 60;
    return `${m}:${s.toString().padStart(2, '0')}.${ms.toString().padStart(3, '0')}`;
  }
}

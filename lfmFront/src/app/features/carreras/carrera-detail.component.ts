import { Component, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';

import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatTabsModule } from '@angular/material/tabs';

import { CarreraService } from '../../core/api/carrera.service';
import { InscripcionService } from '../../core/api/inscripcion.service';
import { ResultadoService } from '../../core/api/resultado.service';
import { UsuarioService } from '../../core/api/usuario.service';
import { VueltaService } from '../../core/api/vuelta.service';
import { ClasificacionService } from '../../core/api/clasificacion.service';
import { IncidenteService } from '../../core/api/incidente.service';
import { AuthService } from '../../core/auth/auth.service';
import { ErrorHandlerService } from '../../core/error/error-handler.service';

import { CarreraResponse } from '../../core/models/carrera.model';
import { EstadoCarrera } from '../../core/models/carrera.model';
import { EstadoInscripcion, InscripcionResponse } from '../../core/models/inscripcion.model';
import { ResultadoCarreraResponse } from '../../core/models/resultado.model';
import { VueltaResponse } from '../../core/models/vuelta.model';
import { SesionClasificacionResponse } from '../../core/models/clasificacion.model';
import { IncidenteResponse } from '../../core/models/incidente.model';

@Component({
  selector: 'app-carrera-detail',
  imports: [
    RouterLink,
    DatePipe,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatTableModule,
    MatChipsModule,
    MatTabsModule,
  ],
  templateUrl: './carrera-detail.component.html',
  styleUrl: './carrera-detail.component.scss',
})
export class CarreraDetailComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly auth = inject(AuthService);
  private readonly carreraService = inject(CarreraService);
  private readonly inscripcionService = inject(InscripcionService);
  private readonly resultadoService = inject(ResultadoService);
  private readonly usuarioService = inject(UsuarioService);
  private readonly vueltaService = inject(VueltaService);
  private readonly clasificacionService = inject(ClasificacionService);
  private readonly incidenteService = inject(IncidenteService);
  private readonly errorHandler = inject(ErrorHandlerService);

  readonly cargando = signal(true);
  readonly cargandoAccion = signal(false);
  readonly carrera = signal<CarreraResponse | null>(null);
  readonly inscriptos = signal(0);
  readonly miInscripcion = signal<InscripcionResponse | null>(null);
  readonly inscripciones = signal<InscripcionResponse[]>([]);
  readonly resultados = signal<ResultadoCarreraResponse[]>([]);
  readonly clasificaciones = signal<SesionClasificacionResponse[]>([]);
  readonly vueltas = signal<VueltaResponse[]>([]);
  readonly incidentes = signal<IncidenteResponse[]>([]);
  readonly usuarios = signal<Record<number, string>>({});

  readonly estaLogueado = this.auth.isLoggedIn;

  readonly esFinalizada = computed(
    () => this.carrera()?.estado === EstadoCarrera.FINALIZADA,
  );
  readonly abierta = computed(
    () => this.carrera()?.estado === EstadoCarrera.INSCRIPCIONES_ABIERTAS,
  );

  readonly vueltasAgrupadas = computed(() => {
    const map = new Map<number, VueltaResponse[]>();
    for (const v of this.vueltas()) {
      const arr = map.get(v.usuarioId) ?? [];
      arr.push(v);
      map.set(v.usuarioId, arr);
    }
    return map;
  });

  readonly columnasResultados = ['posicion', 'piloto', 'tiempo', 'vuelta', 'elo', 'sr'];
  readonly columnasClasificacion = ['posicion', 'piloto', 'tiempo', 'diferencia', 'auto'];
  readonly columnasVueltas = ['vuelta', 'tiempo', 's1', 's2', 's3', 'neumatico', 'cortes'];

  private carreraId = 0;

  constructor() {
    this.carreraId = Number(this.route.snapshot.paramMap.get('id'));
    void this.cargar();
  }

  private async cargar() {
    this.cargando.set(true);
    try {
      const [carrera, count, inscripciones, usuarios] = await Promise.all([
        firstValueFrom(this.carreraService.obtener(this.carreraId)),
        firstValueFrom(this.inscripcionService.contar(this.carreraId)),
        firstValueFrom(this.inscripcionService.deCarrera(this.carreraId)),
        firstValueFrom(this.usuarioService.listar()),
      ]);
      this.carrera.set(carrera);
      this.inscriptos.set(count.inscriptos);
      this.inscripciones.set(inscripciones);
      this.usuarios.set(Object.fromEntries(usuarios.map((u) => [u.id, u.nombrePiloto ?? u.email])));

      const miId = this.auth.usuario()?.id;
      this.miInscripcion.set(
        miId ? inscripciones.find((i) => i.usuarioId === miId) ?? null : null,
      );

      if (carrera.estado === EstadoCarrera.FINALIZADA) {
        const [resultados, clasificaciones, vueltas, incidentes] = await Promise.all([
          firstValueFrom(this.resultadoService.deCarrera(this.carreraId)),
          firstValueFrom(this.clasificacionService.deCarrera(this.carreraId)).catch(() => []),
          firstValueFrom(this.vueltaService.deCarrera(this.carreraId)).catch(() => []),
          firstValueFrom(this.incidenteService.deCarrera(this.carreraId)).catch(() => []),
        ]);
        this.resultados.set(resultados);
        this.clasificaciones.set(clasificaciones);
        this.vueltas.set(vueltas);
        this.incidentes.set(incidentes);
      }
    } catch (error) {
      this.errorHandler.handle(error);
    } finally {
      this.cargando.set(false);
    }
  }

  nombrePiloto(usuarioId: number): string {
    return this.usuarios()[usuarioId] ?? `Piloto #${usuarioId}`;
  }

  private async reload() {
    this.miInscripcion.set(null);
    await this.cargar();
  }

  async inscribirse() {
    const usuario = this.auth.usuario();
    if (!usuario) {
      return;
    }
    this.cargandoAccion.set(true);
    try {
      await firstValueFrom(
        this.inscripcionService.inscribirse({ carreraId: this.carreraId, usuarioId: usuario.id }),
      );
      await this.reload();
    } catch (error) {
      this.errorHandler.handle(error);
    } finally {
      this.cargandoAccion.set(false);
    }
  }

  async bajarse() {
    const usuario = this.auth.usuario();
    if (!usuario) {
      return;
    }
    this.cargandoAccion.set(true);
    try {
      await firstValueFrom(
        this.inscripcionService.cancelarPorCarreraYUsuario(this.carreraId, usuario.id),
      );
      await this.reload();
    } catch (error) {
      this.errorHandler.handle(error);
    } finally {
      this.cargandoAccion.set(false);
    }
  }

  formatTiempo(milisegundos: number | null): string {
    if (milisegundos === null || milisegundos === undefined || milisegundos === 0) {
      return 'DNF';
    }
    const ms = milisegundos % 1000;
    const s = Math.floor(milisegundos / 1000) % 60;
    const m = Math.floor(milisegundos / 60000) % 60;
    return `${m}:${s.toString().padStart(2, '0')}.${ms.toString().padStart(3, '0')}`;
  }

  readonly ESTADO_INSCRIPTO = EstadoInscripcion.INSCRIPTO;
}

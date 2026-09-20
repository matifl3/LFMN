import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from '../../../core/services/auth.service';
import { ApiService, apiError } from '../../../core/services/api.service';
import { ToastService } from '../../../core/services/toast.service';
import { Avatar } from '../../../shared/components/avatar/avatar';
import { Chip } from '../../../shared/components/chip/chip';
import { RankBadge } from '../../../shared/components/rank-badge/rank-badge';
import { FmtFechaHoraPipe } from '../../../core/pipes/fmt-fecha.pipe';
import { FmtLapPipe } from '../../../core/pipes/fmt-lap.pipe';
import { FmtDifPipe } from '../../../core/pipes/fmt-dif.pipe';
import {
  Carrera, CarreraAcceso, EloEstimado, Inscripcion, ResultadoCarrera,
  SesionClasificacion, Vuelta, VueltaAnalisis, VueltaResumen,
} from '../../../core/models/models';

const ESTADOS_INSCRIPCION = ['PROGRAMADA', 'INSCRIPCIONES_ABIERTAS'] as const;

@Component({
  selector: 'app-race-detail',
  standalone: true,
  imports: [RouterLink, Chip, Avatar, RankBadge, FmtFechaHoraPipe, FmtLapPipe, FmtDifPipe],
  templateUrl: './race-detail.html',
})
export class RaceDetailComponent implements OnInit {
  readonly auth = inject(AuthService);
  private readonly api = inject(ApiService);
  private readonly toast = inject(ToastService);
  private readonly route = inject(ActivatedRoute);
  private readonly sanitizer = inject(DomSanitizer);

  readonly carrera = signal<Carrera | null>(null);
  readonly inscriptos = signal<Inscripcion[]>([]);
  readonly resultados = signal<ResultadoCarrera[]>([]);
  readonly clasificaciones = signal<SesionClasificacion[]>([]);
  readonly vueltas = signal<Vuelta[]>([]);
  readonly analisis = signal<VueltaAnalisis[]>([]);
  readonly resumen = signal<VueltaResumen | null>(null);
  readonly acceso = signal<CarreraAcceso | null>(null);
  readonly eloEstimado = signal<EloEstimado | null>(null);
  readonly cargando = signal(true);
  readonly mostrarPassword = signal(false);
  readonly panel = signal<'info' | 'archivos' | 'inscriptos' | 'resultados' | 'clasificacion' | 'analisis'>('info');
  readonly inscribiendo = signal(false);

  private id: string | null = null;

  readonly inscriptosActivos = computed(() => this.inscriptos().filter((i) => i.estado !== 'CANCELADA').length);

  readonly cupoLleno = computed(() => {
    const c = this.carrera();
    return !!c && c.cupoMaximo !== undefined && c.cupoMaximo > 0 && this.inscriptosActivos() >= c.cupoMaximo;
  });

  readonly cupoPct = computed(() => {
    const c = this.carrera();
    return c?.cupoMaximo ? Math.round((this.inscriptosActivos() / c.cupoMaximo) * 100) : 0;
  });

  readonly yaInscripto = computed(() => {
    const u = this.auth.user();
    if (!u) return false;
    return this.inscriptos().some((i) => i.usuarioId === u.id && i.estado !== 'CANCELADA');
  });

  readonly botonDisabled = computed(() => {
    if (!this.auth.user()) return true;
    if (this.inscribiendo()) return true;
    const c = this.carrera();
    return !c || !ESTADOS_INSCRIPCION.includes(c.estado as (typeof ESTADOS_INSCRIPCION)[number]);
  });

  readonly botonLabel = computed(() => {
    if (!this.auth.user()) return 'Ingresar para inscribirme';
    const c = this.carrera();
    if (c && !ESTADOS_INSCRIPCION.includes(c.estado as (typeof ESTADOS_INSCRIPCION)[number])) return 'Inscripción cerrada';
    return this.yaInscripto() ? 'Darme de baja' : 'Inscribirme';
  });

  readonly resultadosSorted = computed(() =>
    [...this.resultados()].sort((a, b) => (a.posicionFinal ?? 999) - (b.posicionFinal ?? 999))
  );

  readonly clasificacionesSorted = computed(() =>
    [...this.clasificaciones()].sort((a, b) => a.tiempo - b.tiempo)
  );

  readonly accesoVisible = computed(() => !!this.acceso()?.contrasenaServidor || !!this.acceso()?.servidor);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.cargando.set(false);
      return;
    }
    this.id = id;
    const user = this.auth.user();
    forkJoin({
      carrera: this.api.get<Carrera>('/carreras/' + id).pipe(catchError(() => of(null))),
      inscriptos: this.api.list<Inscripcion>('/inscripciones/carrera/' + id).pipe(catchError(() => of([]))),
      resultados: this.api.list<ResultadoCarrera>('/resultados/carrera/' + id).pipe(catchError(() => of([]))),
      clasificaciones: this.api.list<SesionClasificacion>('/clasificaciones/carrera/' + id).pipe(catchError(() => of([]))),
      vueltas: user
        ? this.api.list<Vuelta>('/vueltas/carrera/' + id + '/usuario/' + user.id).pipe(catchError(() => of([])))
        : of([]),
      analisis: user
        ? this.api.list<VueltaAnalisis>('/vueltas/carrera/' + id + '/usuario/' + user.id + '/analisis').pipe(catchError(() => of([])))
        : of([]),
      resumen: user
        ? this.api.get<VueltaResumen>('/vueltas/carrera/' + id + '/usuario/' + user.id + '/analisis/resumen').pipe(catchError(() => of(null)))
        : of(null),
      acceso: user
        ? this.api.get<CarreraAcceso>('/carreras/' + id + '/acceso-servidor').pipe(catchError(() => of(null)))
        : of(null),
      eloEstimado: user
        ? this.api.get<EloEstimado>('/carreras/' + id + '/elo-estimado').pipe(catchError(() => of(null)))
        : of(null),
    }).subscribe({
      next: (r) => {
        this.carrera.set(r.carrera);
        this.inscriptos.set(r.inscriptos);
        this.resultados.set(r.resultados);
        this.clasificaciones.set(r.clasificaciones);
        this.vueltas.set(r.vueltas);
        this.analisis.set(r.analisis);
        this.resumen.set(r.resumen);
        this.acceso.set(r.acceso);
        this.eloEstimado.set(r.eloEstimado);
        this.cargando.set(false);
      },
      error: () => this.cargando.set(false),
    });
  }

  toggleInscripcion(): void {
    if (this.botonDisabled() || !this.id) return;
    const user = this.auth.user();
    if (!user) return;
    this.inscribiendo.set(true);
    const darDeBaja = this.yaInscripto();
    (darDeBaja
      ? this.api.del('/inscripciones/carrera/' + this.id + '/usuario/' + user.id)
      : this.api.post('/inscripciones', { carreraId: Number(this.id) })
    ).subscribe({
      next: () => {
        this.inscribiendo.set(false);
        this.toast.success(darDeBaja ? 'Te diste de baja de la carrera.' : '¡Inscripción confirmada!');
        this.recargarInscriptos();
      },
      error: (err) => {
        this.inscribiendo.set(false);
        this.toast.error(apiError(err));
      },
    });
  }

  private recargarInscriptos(): void {
    if (!this.id) return;
    this.api.list<Inscripcion>('/inscripciones/carrera/' + this.id).subscribe({
      next: (l) => this.inscriptos.set(l),
      error: () => this.toast.error('No se pudo actualizar la lista de inscriptos.'),
    });
  }

  eloTxt(v: number | null | undefined): string {
    if (v === null || v === undefined) return '—';
    return (v > 0 ? '+' : '') + v;
  }

  eloColor(v: number | null | undefined): string {
    if (v === null || v === undefined) return '';
    return v >= 0 ? 'var(--status-positivo)' : 'var(--lbm-rojo-hi)';
  }

  /** Diferencia de dos tiempos en ms; null si falta alguno */
  difLap(a: number | null | undefined, b: number | null | undefined): number | null {
    if (a === null || a === undefined || b === null || b === undefined) return null;
    return a - b;
  }

  /** Porcentaje con total; '—' si no hay base */
  pct(n: number | null | undefined, total: number | null | undefined): string {
    if (n === null || n === undefined || total === null || total === undefined || total <= 0) return '—';
    return Math.round((n / total) * 100) + '%';
  }

  /** "+3" si ganó posiciones, "−2" si las perdió */
  posGanadasTxt(v: number | null | undefined): string {
    if (v === null || v === undefined) return '—';
    return v > 0 ? '+' + v : String(v);
  }

  posTxt(v: number | null | undefined): string {
    if (v === null || v === undefined) return '—';
    return 'P' + v;
  }

  posicionesChart(): SafeHtml {
    const analisis = this.analisis();
    if (analisis.length === 0) return this.sanitizer.bypassSecurityTrustHtml('');
    return this.sanitizer.bypassSecurityTrustHtml(
      this.chartSvg(analisis.map((a) => 1 / (a.posicionEnVuelta ?? 1)), 'var(--lbm-rojo)')
    );
  }

  deltasChart(): SafeHtml {
    const analisis = this.analisis();
    if (analisis.length === 0) return this.sanitizer.bypassSecurityTrustHtml('');
    const deltas = analisis.map((a) => (a.deltaLiderMs ?? 0) / 1000);
    return this.sanitizer.bypassSecurityTrustHtml(
      this.chartSvg(deltas, 'var(--lbm-rojo-hi)')
    );
  }

  private chartSvg(values: number[], color: string): string {
    const W = 720, H = 200, PAD = 24;
    const min = Math.min(...values);
    const max = Math.max(...values);
    const rango = max - min || 1;
    const step = (W - PAD * 2) / (values.length - 1);
    const line = values.map((v, i) => {
      const x = PAD + i * step;
      const y = PAD + (H - PAD * 2) * (1 - (v - min) / rango);
      return x.toFixed(2) + ',' + y.toFixed(2);
    });
    const area = line.join(' ') + ' ' + W + ',' + H + ' 0,' + H;
    return (
      '<svg viewBox="0 0 ' + W + ' ' + H + '" width="100%" height="200" role="img">' +
      '<polygon points="' + area + '" fill="' + color + '" opacity="0.15"/>' +
      '<polyline points="' + line.join(' ') + '" fill="none" stroke="' + color + '" stroke-width="2.5" stroke-linejoin="round" stroke-linecap="round"/>' +
      '</svg>'
    );
  }
}
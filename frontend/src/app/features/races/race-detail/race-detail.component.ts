import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { HttpErrorResponse } from '@angular/common/http';
import { forkJoin, of } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { ApiService, apiError } from '../../../core/services/api.service';
import { ErrorCargaService } from '../../../core/services/error-carga.service';
import { ToastService } from '../../../core/services/toast.service';
import { Avatar } from '../../../shared/components/avatar/avatar';
import { Chip } from '../../../shared/components/chip/chip';
import { RankBadge } from '../../../shared/components/rank-badge/rank-badge';
import { ConfirmDialog } from '../../../shared/components/confirm-dialog/confirm-dialog';
import { FmtFechaHoraPipe } from '../../../core/pipes/fmt-fecha.pipe';
import { FmtLapPipe } from '../../../core/pipes/fmt-lap.pipe';
import { FmtDifPipe } from '../../../core/pipes/fmt-dif.pipe';
import { normalizeUrl } from '../../../core/utils/formato';
import {
  Carrera,
  CarreraAcceso,
  EloEstimado,
  Inscripcion,
  ResultadoCarrera,
  SesionClasificacion,
  VueltaAnalisis,
  VueltaResumen,
} from '../../../core/models/models';

const ESTADOS_INSCRIPCION = ['PROGRAMADA', 'INSCRIPCIONES_ABIERTAS'] as const;

const PANELES = [
  'info',
  'archivos',
  'inscriptos',
  'resultados',
  'clasificacion',
  'analisis',
] as const;
type Panel = (typeof PANELES)[number];

/** Paneles cuyos datos se piden recién la primera vez que se abren. */
type PanelPerezoso = 'resultados' | 'clasificacion' | 'analisis';

@Component({
  selector: 'app-race-detail',
  standalone: true,
  imports: [
    RouterLink,
    Chip,
    Avatar,
    RankBadge,
    ConfirmDialog,
    FmtFechaHoraPipe,
    FmtLapPipe,
    FmtDifPipe,
  ],
  templateUrl: './race-detail.html',
  styleUrl: './race-detail.scss',
})
export class RaceDetailComponent implements OnInit {
  readonly auth = inject(AuthService);
  private readonly api = inject(ApiService);
  private readonly carga = inject(ErrorCargaService);
  private readonly toast = inject(ToastService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly sanitizer = inject(DomSanitizer);

  readonly carrera = signal<Carrera | null>(null);
  readonly inscriptos = signal<Inscripcion[]>([]);
  readonly resultados = signal<ResultadoCarrera[]>([]);
  readonly clasificaciones = signal<SesionClasificacion[]>([]);
  readonly analisis = signal<VueltaAnalisis[]>([]);
  readonly resumen = signal<VueltaResumen | null>(null);
  readonly acceso = signal<CarreraAcceso | null>(null);
  readonly eloEstimado = signal<EloEstimado | null>(null);
  /** "Ronda 3 de 8" dentro del campeonato; null si no se pudo calcular. */
  readonly ronda = signal<{ numero: number; total: number } | null>(null);
  readonly cargando = signal(true);
  /** La carrera es de un campeonato privado del que el visor no es miembro. */
  readonly bloqueado = signal(false);
  /** Falló la carga por algo que no es "no existe" ni "es privada". */
  readonly errorCarga = signal(false);
  readonly mostrarPassword = signal(false);
  readonly panel = signal<Panel>('info');
  readonly inscribiendo = signal(false);
  readonly confirmandoBaja = signal(false);

  /** Paneles perezosos ya pedidos (o en vuelo), para no repetir la petición. */
  private readonly pedidos = new Set<PanelPerezoso>();
  readonly cargandoPanel = signal<PanelPerezoso | null>(null);

  private id: string | null = null;

  readonly inscriptosVisibles = computed(() =>
    this.inscriptos().filter((i) => i.estado !== 'CANCELADA'),
  );

  readonly inscriptosActivos = computed(
    () => this.inscriptosVisibles().filter((i) => i.estado !== 'LISTA_ESPERA').length,
  );

  readonly enEspera = computed(
    () => this.inscriptosVisibles().filter((i) => i.estado === 'LISTA_ESPERA').length,
  );

  readonly cupoLleno = computed(() => {
    const c = this.carrera();
    return !!c?.cupoMaximo && this.inscriptosActivos() >= c.cupoMaximo;
  });

  readonly cupoPct = computed(() => {
    const c = this.carrera();
    return c?.cupoMaximo
      ? Math.min(100, Math.round((this.inscriptosActivos() / c.cupoMaximo) * 100))
      : 0;
  });

  readonly yaInscripto = computed(() => {
    const u = this.auth.user();
    if (!u) return false;
    return this.inscriptosVisibles().some((i) => i.usuarioId === u.id);
  });

  private readonly inscripcionAbierta = computed(() => {
    const c = this.carrera();
    return !!c && ESTADOS_INSCRIPCION.includes(c.estado as (typeof ESTADOS_INSCRIPCION)[number]);
  });

  readonly botonDisabled = computed(
    () => !this.auth.user() || this.inscribiendo() || !this.inscripcionAbierta(),
  );

  readonly botonLabel = computed(() => {
    if (!this.auth.user()) return 'Ingresar para inscribirme';
    if (!this.inscripcionAbierta()) return 'Inscripción cerrada';
    if (this.yaInscripto()) return 'Darme de baja';
    return this.cupoLleno() ? 'Anotarme en lista de espera' : 'Inscribirme';
  });

  readonly mensajeBaja = computed(() => {
    const c = this.carrera();
    const nombre = c?.nombre ? '"' + c.nombre + '"' : 'esta carrera';
    const aviso = this.cupoLleno()
      ? ' El cupo está lleno: si te arrepentís, volvés a entrar por la lista de espera.'
      : '';
    return '¿Seguro que querés darte de baja de ' + nombre + '?' + aviso;
  });

  /** Finalizados primero por posición; los DNF siempre al final. */
  readonly resultadosSorted = computed(() =>
    [...this.resultados()].sort((a, b) => {
      const dnfA = a.finalizo === false ? 1 : 0;
      const dnfB = b.finalizo === false ? 1 : 0;
      if (dnfA !== dnfB) return dnfA - dnfB;
      return (a.posicionFinal ?? 999) - (b.posicionFinal ?? 999);
    }),
  );

  readonly clasificacionesSorted = computed(() =>
    [...this.clasificaciones()].sort((a, b) => a.tiempo - b.tiempo),
  );

  readonly accesoVisible = computed(() => !!this.acceso()?.contrasenaServidor);

  /** El campo `servidor` se carga a mano, asi que puede venir una URL o un host. */
  readonly servidorUrl = computed(() => normalizeUrl(this.carrera()?.servidor));
  readonly linkPista = computed(() => normalizeUrl(this.carrera()?.linkPista));
  readonly linkAuto = computed(() => normalizeUrl(this.carrera()?.linkAuto));

  /** Etiqueta corta para el boton: el host, no la URL entera. */
  readonly servidorLabel = computed(() => {
    const url = this.servidorUrl();
    if (!url) return null;
    try {
      return new URL(url).host;
    } catch {
      return url;
    }
  });

  /**
   * Posición por vuelta, en escala lineal y con P1 arriba (se grafica -pos).
   * null si hay menos de dos vueltas: con una sola no hay línea que dibujar.
   */
  readonly posicionesChart = computed<SafeHtml | null>(() => {
    const valores = this.analisis()
      .filter((a) => a.posicionEnVuelta != null)
      .map((a) => -(a.posicionEnVuelta as number));
    return this.chart(valores, 'var(--lbm-rojo)');
  });

  /** Mejor y peor posición del gráfico, para rotular el eje. */
  readonly rangoPosiciones = computed(() => {
    const pos = this.analisis()
      .map((a) => a.posicionEnVuelta)
      .filter((p): p is number => p != null);
    return pos.length ? { mejor: Math.min(...pos), peor: Math.max(...pos) } : null;
  });

  readonly deltasChart = computed<SafeHtml | null>(() => {
    const valores = this.analisis().map((a) => (a.deltaLiderMs ?? 0) / 1000);
    return this.chart(valores, 'var(--lbm-rojo-hi)');
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.cargando.set(false);
      return;
    }
    this.id = id;
    this.cargar();
  }

  /** Pide la carrera y lo que necesita la vista inicial; el resto, por panel. */
  cargar(): void {
    if (!this.id) return;
    const id = this.id;
    this.cargando.set(true);
    this.errorCarga.set(false);
    this.bloqueado.set(false);
    this.api.get<Carrera>('/carreras/' + id).subscribe({
      next: (carrera) => this.cargarResto(carrera),
      error: (err: HttpErrorResponse) => {
        // 400 de BusinessException = campeonato privado (CampeonatoAccesoService);
        // 404 = no existe, y lo muestra el "Carrera no encontrada" del template.
        if (err.status === 400 && /privado/i.test(apiError(err))) this.bloqueado.set(true);
        else if (err.status !== 404) this.errorCarga.set(true);
        this.cargando.set(false);
      },
    });
  }

  private cargarResto(carrera: Carrera): void {
    const id = String(carrera.id);
    const user = this.auth.user();
    const abierta = ESTADOS_INSCRIPCION.includes(
      carrera.estado as (typeof ESTADOS_INSCRIPCION)[number],
    );
    forkJoin({
      inscriptos: this.api
        .list<Inscripcion>('/inscripciones/carrera/' + id)
        .pipe(this.carga.siFalla([] as Inscripcion[])),
      acceso: user
        ? this.api
            .get<CarreraAcceso>('/carreras/' + id + '/acceso-servidor')
            .pipe(this.carga.siFalla(null))
        : of(null),
      // El Elo estimado solo tiene sentido antes de correrla.
      eloEstimado:
        user && abierta
          ? this.api
              .get<EloEstimado>('/carreras/' + id + '/elo-estimado')
              .pipe(this.carga.siFalla(null))
          : of(null),
      // /carreras/campeonato/{id} exige sesión: sin login no se muestra la ronda.
      delCampeonato:
        user && carrera.campeonatoId
          ? this.api
              .list<Carrera>('/carreras/campeonato/' + carrera.campeonatoId)
              .pipe(this.carga.siFalla([] as Carrera[]))
          : of([] as Carrera[]),
    }).subscribe((r) => {
      this.carrera.set(carrera);
      this.inscriptos.set(r.inscriptos);
      this.acceso.set(r.acceso);
      this.eloEstimado.set(r.eloEstimado);
      this.ronda.set(this.calcularRonda(carrera, r.delCampeonato));
      this.cargando.set(false);
      this.abrirPanel(this.panelInicial(carrera), false);
    });
  }

  /** ?tab= si es válido; si no, Resultados para una carrera ya corrida. */
  private panelInicial(c: Carrera): Panel {
    const tab = this.route.snapshot.queryParamMap.get('tab');
    if (tab && (PANELES as readonly string[]).includes(tab)) return tab as Panel;
    return c.estado === 'FINALIZADA' ? 'resultados' : 'info';
  }

  private calcularRonda(c: Carrera, todas: Carrera[]): { numero: number; total: number } | null {
    if (c.estado === 'CANCELADA') return null;
    const rondas = todas
      .filter((x) => x.estado !== 'CANCELADA')
      .sort((a, b) => (a.fecha ?? '').localeCompare(b.fecha ?? ''));
    const idx = rondas.findIndex((x) => x.id === c.id);
    return idx < 0 ? null : { numero: idx + 1, total: rondas.length };
  }

  abrirPanel(p: Panel, actualizarUrl = true): void {
    this.panel.set(p);
    if (actualizarUrl) {
      this.router.navigate([], {
        relativeTo: this.route,
        queryParams: { tab: p === 'info' ? null : p },
        queryParamsHandling: 'merge',
        replaceUrl: true,
      });
    }
    if (p === 'resultados' || p === 'clasificacion' || p === 'analisis') this.cargarPanel(p);
  }

  private cargarPanel(p: PanelPerezoso): void {
    if (!this.id || this.pedidos.has(p)) return;
    const id = this.id;
    const user = this.auth.user();
    if (p === 'analisis' && !user) return;
    this.pedidos.add(p);
    this.cargandoPanel.set(p);
    const listo = () => {
      if (this.cargandoPanel() === p) this.cargandoPanel.set(null);
    };
    if (p === 'resultados') {
      this.api
        .list<ResultadoCarrera>('/resultados/carrera/' + id)
        .pipe(this.carga.siFalla([] as ResultadoCarrera[]))
        .subscribe((l) => {
          this.resultados.set(l);
          listo();
        });
    } else if (p === 'clasificacion') {
      this.api
        .list<SesionClasificacion>('/clasificaciones/carrera/' + id)
        .pipe(this.carga.siFalla([] as SesionClasificacion[]))
        .subscribe((l) => {
          this.clasificaciones.set(l);
          listo();
        });
    } else {
      const base = '/vueltas/carrera/' + id + '/usuario/' + user!.id + '/analisis';
      forkJoin({
        analisis: this.api
          .list<VueltaAnalisis>(base)
          .pipe(this.carga.siFalla([] as VueltaAnalisis[])),
        resumen: this.api.get<VueltaResumen>(base + '/resumen').pipe(this.carga.siFalla(null)),
      }).subscribe((r) => {
        this.analisis.set(r.analisis);
        this.resumen.set(r.resumen);
        listo();
      });
    }
  }

  toggleInscripcion(): void {
    if (this.botonDisabled()) return;
    if (this.yaInscripto()) {
      this.confirmandoBaja.set(true);
      return;
    }
    this.ejecutarInscripcion(false);
  }

  confirmarBaja(): void {
    this.confirmandoBaja.set(false);
    this.ejecutarInscripcion(true);
  }

  private ejecutarInscripcion(darDeBaja: boolean): void {
    const user = this.auth.user();
    if (!user || !this.id) return;
    this.inscribiendo.set(true);
    (darDeBaja
      ? this.api.del('/inscripciones/carrera/' + this.id + '/usuario/' + user.id)
      : this.api.post('/inscripciones', { carreraId: Number(this.id) })
    ).subscribe({
      next: () => {
        this.inscribiendo.set(false);
        this.recargarInscriptos(darDeBaja);
      },
      error: (err) => {
        this.inscribiendo.set(false);
        this.toast.error(apiError(err));
      },
    });
  }

  private recargarInscriptos(darDeBaja: boolean): void {
    if (!this.id) return;
    this.api.list<Inscripcion>('/inscripciones/carrera/' + this.id).subscribe({
      next: (l) => {
        this.inscriptos.set(l);
        if (darDeBaja) {
          this.toast.success('Te diste de baja de la carrera.');
          return;
        }
        // El backend decide si entra al cupo o a la lista de espera.
        const uid = this.auth.user()?.id;
        const propia = l.find((i) => i.usuarioId === uid && i.estado !== 'CANCELADA');
        this.toast.success(
          propia?.estado === 'LISTA_ESPERA'
            ? 'Quedaste en la lista de espera. Te avisamos si se libera un lugar.'
            : '¡Inscripción confirmada!',
        );
      },
      error: () => this.toast.error('No se pudo actualizar la lista de inscriptos.'),
    });
  }

  eloTxt(v: number | null | undefined): string {
    if (v === null || v === undefined) return '—';
    return (v > 0 ? '+' : '') + v;
  }

  eloColor(v: number | null | undefined): string | null {
    if (v === null || v === undefined) return null;
    return v >= 0 ? 'var(--status-positivo)' : 'var(--lbm-rojo-hi)';
  }

  /** Diferencia de dos tiempos en ms; null si falta alguno */
  difLap(a: number | null | undefined, b: number | null | undefined): number | null {
    if (a === null || a === undefined || b === null || b === undefined) return null;
    return a - b;
  }

  /** Porcentaje con total; '—' si no hay base */
  pct(n: number | null | undefined, total: number | null | undefined): string {
    if (n === null || n === undefined || total === null || total === undefined || total <= 0)
      return '—';
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

  private chart(valores: number[], color: string): SafeHtml | null {
    if (valores.length < 2) return null;
    return this.sanitizer.bypassSecurityTrustHtml(this.chartSvg(valores, color));
  }

  /** Solo recibe números (nunca texto del usuario), por eso el bypass es seguro. */
  private chartSvg(values: number[], color: string): string {
    const W = 720,
      H = 200,
      PAD = 24;
    const min = Math.min(...values);
    const max = Math.max(...values);
    const rango = max - min || 1;
    const step = (W - PAD * 2) / (values.length - 1);
    const line = values.map((v, i) => {
      const x = PAD + i * step;
      const y = PAD + (H - PAD * 2) * (1 - (v - min) / rango);
      return x.toFixed(2) + ',' + y.toFixed(2);
    });
    const area = line.join(' ') + ' ' + (W - PAD) + ',' + H + ' ' + PAD + ',' + H;
    return (
      '<svg viewBox="0 0 ' +
      W +
      ' ' +
      H +
      '" width="100%" height="200" role="img" aria-hidden="true">' +
      '<polygon points="' +
      area +
      '" fill="' +
      color +
      '" opacity="0.15"/>' +
      '<polyline points="' +
      line.join(' ') +
      '" fill="none" stroke="' +
      color +
      '" stroke-width="2.5" stroke-linejoin="round" stroke-linecap="round"/>' +
      '</svg>'
    );
  }
}

import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Observable, forkJoin } from 'rxjs';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { ErrorCargaService } from '../../core/services/error-carga.service';
import { RelojService } from '../../core/services/reloj.service';
import { RankBadge } from '../../shared/components/rank-badge/rank-badge';
import { Chip } from '../../shared/components/chip/chip';
import { FmtFechaHoraPipe } from '../../core/pipes/fmt-fecha.pipe';
import {
  Campeonato,
  Carrera,
  ProgresoCampeonato,
  ResultadoCarrera,
  TablaPosicion,
} from '../../core/models/models';
import { fmtCuentaRegresiva, fmtDia, fmtMes } from '../../core/utils/formato';

/** Una fila del calendario: la carrera y su número de ronda (las canceladas no cuentan). */
interface Ronda {
  carrera: Carrera;
  numero: number | null;
}

type EstadoTabla = 'cargando' | 'ok' | 'vacia' | 'error' | 'login' | 'privado';

const ESTADO_CAMPEONATO: Record<string, string> = {
  ACTIVO: 'En curso',
  CERRADO: 'Finalizado',
};

@Component({
  selector: 'app-championship',
  standalone: true,
  imports: [RouterLink, RankBadge, Chip, FmtFechaHoraPipe],
  styleUrl: './championship.scss',
  templateUrl: './championship.html',
})
export class ChampionshipComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly carga = inject(ErrorCargaService);
  private readonly reloj = inject(RelojService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  readonly auth = inject(AuthService);

  readonly champs = signal<Campeonato[]>([]);
  /** Todas las carreras públicas (próximas + pasadas); se filtran por campeonato. */
  private readonly carreras = signal<Carrera[]>([]);
  readonly cargando = signal(true);
  readonly errorCarga = signal(false);

  readonly selectedId = signal<number | null>(null);
  readonly tabla = signal<TablaPosicion[]>([]);
  readonly estadoTabla = signal<EstadoTabla>('cargando');
  readonly progreso = signal<ProgresoCampeonato | null>(null);
  /** Top 3 de cada carrera finalizada, por id de carrera. */
  readonly podios = signal<Record<number, ResultadoCarrera[]>>({});

  readonly champ = computed(() => this.champs().find((c) => c.id === this.selectedId()) ?? null);

  readonly bloqueado = computed(() => {
    const c = this.champ();
    return !!c && c.visibilidad === 'PRIVADO' && !c.soyMiembro;
  });

  readonly estadoLabel = computed(() => {
    const e = this.champ()?.estado;
    return e ? (ESTADO_CAMPEONATO[e] ?? e) : '';
  });

  /** Campeonatos con al menos una carrera primero, para que el selector no abra en uno vacío. */
  private readonly idsConCarreras = computed(
    () =>
      new Set(
        this.carreras()
          .map((c) => c.campeonatoId)
          .filter((id): id is number => !!id),
      ),
  );
  readonly champsConActividad = computed(() =>
    this.champs().filter((c) => this.idsConCarreras().has(c.id)),
  );
  readonly champsSinCarreras = computed(() =>
    this.champs().filter((c) => !this.idsConCarreras().has(c.id)),
  );

  readonly calendario = computed<Ronda[]>(() => {
    const id = this.selectedId();
    let n = 0;
    return this.carreras()
      .filter((c) => c.campeonatoId === id)
      .sort((a, b) => (a.fecha ?? '').localeCompare(b.fecha ?? ''))
      .map((carrera) => ({
        carrera,
        numero: carrera.estado === 'CANCELADA' ? null : ++n,
      }));
  });

  /** La próxima carrera sin correr del campeonato (la que destaca el encabezado). */
  readonly proxima = computed(() => {
    const ahora = this.reloj.ahora();
    return (
      this.calendario().find(
        (r) =>
          r.carrera.estado !== 'CANCELADA' &&
          r.carrera.estado !== 'FINALIZADA' &&
          (r.carrera.estado === 'EN_CURSO' || new Date(r.carrera.fecha).getTime() > ahora),
      ) ?? null
    );
  });

  readonly progresoTxt = computed(() => {
    const p = this.progreso();
    if (!p || p.rondasTotales === 0) return null;
    if (p.rondasCorridas >= p.rondasTotales)
      return `${p.rondasTotales} rondas · Temporada completa`;
    return `${p.rondasCorridas} de ${p.rondasTotales} rondas corridas`;
  });

  readonly progresoPct = computed(() => {
    const p = this.progreso();
    return p && p.rondasTotales > 0 ? Math.round((p.rondasCorridas / p.rondasTotales) * 100) : 0;
  });

  readonly puntosLider = computed(() => this.tabla()[0]?.puntos ?? 0);

  readonly miFila = computed(() => {
    const uid = this.auth.user()?.id;
    return uid ? (this.tabla().find((t) => t.usuarioId === uid) ?? null) : null;
  });

  ngOnInit(): void {
    forkJoin({
      champs: this.api.list<Campeonato>('/campeonatos'),
      proximas: this.api
        .list<Carrera>('/carreras/proximas')
        .pipe(this.carga.siFalla([] as Carrera[])),
      pasadas: this.api
        .list<Carrera>('/carreras/pasadas')
        .pipe(this.carga.siFalla([] as Carrera[])),
    }).subscribe({
      next: (r) => {
        const porId = new Map<number, Carrera>();
        [...r.pasadas, ...r.proximas].forEach((c) => porId.set(c.id, c));
        this.champs.set(r.champs);
        this.carreras.set([...porId.values()]);
        this.cargando.set(false);
        const inicial = this.campeonatoInicial(r.champs, r.proximas, r.pasadas);
        if (inicial !== null) this.seleccionar(inicial, false);
      },
      error: () => {
        this.cargando.set(false);
        this.errorCarga.set(true);
      },
    });
  }

  /**
   * ?id= si es válido. Si no, el de la próxima carrera de la liga; si no hay,
   * el que más carreras corrió; y como último recurso el primer activo.
   */
  private campeonatoInicial(
    champs: Campeonato[],
    proximas: Carrera[],
    pasadas: Carrera[],
  ): number | null {
    if (champs.length === 0) return null;
    const existe = (id: number | null | undefined) => !!id && champs.some((c) => c.id === id);
    const pedido = Number(this.route.snapshot.queryParamMap.get('id'));
    if (existe(pedido)) return pedido;
    const prox = proximas.find((c) => c.estado !== 'CANCELADA' && existe(c.campeonatoId));
    if (prox?.campeonatoId) return prox.campeonatoId;
    const corridas = new Map<number, number>();
    pasadas
      .filter((c) => c.estado === 'FINALIZADA' && c.campeonatoId)
      .forEach((c) => corridas.set(c.campeonatoId!, (corridas.get(c.campeonatoId!) ?? 0) + 1));
    const masCorrido = [...corridas.entries()].sort((a, b) => b[1] - a[1])[0]?.[0];
    if (existe(masCorrido)) return masCorrido!;
    return (champs.find((c) => c.estado === 'ACTIVO') ?? champs[0]).id;
  }

  onChange(e: Event): void {
    this.seleccionar(Number((e.target as HTMLSelectElement).value), true);
  }

  private seleccionar(id: number, actualizarUrl: boolean): void {
    this.selectedId.set(id);
    if (actualizarUrl) {
      this.router.navigate([], {
        relativeTo: this.route,
        queryParams: { id },
        queryParamsHandling: 'merge',
        replaceUrl: true,
      });
    }
    this.progreso.set(null);
    this.api
      .get<ProgresoCampeonato>('/campeonatos/' + id + '/progreso')
      .pipe(this.carga.siFalla(null))
      .subscribe((p) => {
        if (this.selectedId() === id) this.progreso.set(p);
      });
    this.cargarTabla(id);
    this.cargarPodios();
  }

  private cargarTabla(id: number): void {
    this.tabla.set([]);
    if (this.bloqueado()) {
      this.estadoTabla.set('privado');
      return;
    }
    // La tabla exige sesión (SecurityConfig): sin login ni se pide, para no
    // provocar un 401 que solo ensucia la consola.
    if (!this.auth.autenticado()) {
      this.estadoTabla.set('login');
      return;
    }
    this.estadoTabla.set('cargando');
    this.api.get<TablaPosicion[]>('/campeonatos/' + id + '/tabla').subscribe({
      next: (list) => {
        if (this.selectedId() !== id) return;
        const ordenada = [...(list ?? [])].sort(
          (a, b) => (a.posicion ?? 999) - (b.posicion ?? 999) || b.puntos - a.puntos,
        );
        this.tabla.set(ordenada);
        this.estadoTabla.set(ordenada.length ? 'ok' : 'vacia');
      },
      error: () => {
        if (this.selectedId() === id) this.estadoTabla.set('error');
      },
    });
  }

  /** Podio de las carreras finalizadas que todavía no se pidieron. */
  private cargarPodios(): void {
    if (this.bloqueado()) return;
    const yaPedidas = this.podios();
    const faltan = this.calendario()
      .map((r) => r.carrera)
      .filter((c) => c.estado === 'FINALIZADA' && !(c.id in yaPedidas));
    if (faltan.length === 0) return;
    const pedidos: Record<string, Observable<ResultadoCarrera[]>> = {};
    faltan.forEach((c) => (pedidos[c.id] = this.podioDe(c.id)));
    forkJoin(pedidos).subscribe((r) => {
      const nuevos: Record<number, ResultadoCarrera[]> = {};
      Object.entries(r).forEach(([id, lista]) => (nuevos[Number(id)] = lista));
      this.podios.update((p) => ({ ...p, ...nuevos }));
    });
  }

  private podioDe(carreraId: number): Observable<ResultadoCarrera[]> {
    return this.api
      .list<ResultadoCarrera>('/resultados/carrera/' + carreraId)
      .pipe(this.carga.siFalla([] as ResultadoCarrera[]));
  }

  podio(carreraId: number): ResultadoCarrera[] {
    return (this.podios()[carreraId] ?? [])
      .filter((r) => r.finalizo !== false && r.posicionFinal != null)
      .sort((a, b) => (a.posicionFinal ?? 999) - (b.posicionFinal ?? 999))
      .slice(0, 3);
  }

  diferencia(t: TablaPosicion): string {
    const d = this.puntosLider() - t.puntos;
    return d === 0 ? '—' : '−' + d;
  }

  esMiFila(t: TablaPosicion): boolean {
    return t.usuarioId === this.auth.user()?.id;
  }

  irAMiFila(): void {
    document.getElementById('mi-fila')?.scrollIntoView({ behavior: 'smooth', block: 'center' });
  }

  dia(c: Carrera): string {
    return fmtDia(c.fecha);
  }

  mes(c: Carrera): string {
    return fmtMes(c.fecha);
  }

  cuentaRegresiva(c: Carrera): string {
    return fmtCuentaRegresiva(c.fecha, this.reloj.ahora(), c.estado);
  }
}

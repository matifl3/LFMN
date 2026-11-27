import { TranslocoPipe } from '@jsverse/transloco';
import { t } from '../../core/i18n/t';
import {
  Component,
  ElementRef,
  HostListener,
  computed,
  inject,
  signal,
  OnInit,
} from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { ApiService, apiError } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { ErrorCargaService } from '../../core/services/error-carga.service';
import { ToastService } from '../../core/services/toast.service';
import { RelojService } from '../../core/services/reloj.service';
import { Modal } from '../../shared/components/modal/modal';
import { ConfirmDialog } from '../../shared/components/confirm-dialog/confirm-dialog';
import { RankBadge } from '../../shared/components/rank-badge/rank-badge';
import { MiPosicion, PanelPilotoComponent } from './panel-piloto/panel-piloto.component';
import {
  Campeonato,
  Carrera,
  TablaPosicion,
  Anuncio,
  Inscripcion,
  ProgresoCampeonato,
  ResumenLiga,
} from '../../core/models/models';
import { fmtCuentaRegresiva, fmtDia, fmtFecha, fmtHora, fmtMes } from '../../core/utils/formato';
import { FechaRelativaPipe } from '../../core/pipes/fecha-relativa.pipe';
import { AssetUrlPipe } from '../../core/pipes/asset-url.pipe';

/** Estados en los que el backend acepta inscripciones (InscripcionesAbiertasValidador). */
const ESTADOS_INSCRIBIBLES = ['PROGRAMADA', 'INSCRIPCIONES_ABIERTAS'];

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    TranslocoPipe,
    RouterLink,
    Modal,
    ConfirmDialog,
    RankBadge,
    PanelPilotoComponent,
    FechaRelativaPipe,
    AssetUrlPipe,
  ],
  styleUrl: './home.scss',
  templateUrl: './home.html',
})
export class HomeComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly carga = inject(ErrorCargaService);
  private readonly toast = inject(ToastService);
  private readonly reloj = inject(RelojService);
  private readonly el = inject(ElementRef<HTMLElement>);

  readonly resumen = signal<ResumenLiga | null>(null);
  readonly proximas = signal<Carrera[]>([]);
  /** Carreras EN_CURSO: si hay, la sección muestra el banner "en vivo". */
  readonly enCurso = signal<Carrera[]>([]);
  readonly cargando = signal(true);
  readonly campeonato = signal<Campeonato | null>(null);
  readonly progreso = signal<ProgresoCampeonato | null>(null);
  readonly top3 = signal<TablaPosicion[]>([]);
  readonly cargandoTabla = signal(false);
  /** Todos los anuncios, del más nuevo al más viejo (GET /anuncios). */
  readonly anuncios = signal<Anuncio[]>([]);
  /** Anuncio abierto en el modal. */
  readonly anuncioSeleccionado = signal<Anuncio | null>(null);
  readonly ctaAbierto = signal(false);

  /** carreraId → estado de inscripción del usuario (INSCRIPTO / LISTA_ESPERA; sin canceladas). */
  readonly misInscripciones = signal<ReadonlyMap<number, string>>(new Map());
  /** Posición del usuario en el campeonato destacado (null si no suma puntos ahí). */
  readonly miPosicion = signal<MiPosicion | null>(null);
  readonly aConfirmar = signal<Carrera | null>(null);
  readonly inscribiendo = signal(false);

  readonly autenticado = this.auth.autenticado;
  readonly nombrePiloto = computed(() => this.auth.user()?.nombrePiloto ?? 'piloto');

  readonly inscribibles = computed(() =>
    this.proximas().filter((c) => ESTADOS_INSCRIBIBLES.includes(c.estado)),
  );

  /**
   * Anuncio principal: el destacado más nuevo o, si no hay, el más nuevo (mismo
   * criterio que GET /anuncios/ultimo).
   */
  readonly anuncioPrincipal = computed(
    () => this.anuncios().find((a) => a.destacado) ?? this.anuncios()[0] ?? null,
  );

  /** Los dos siguientes, en lista compacta debajo del principal. */
  readonly otrosAnuncios = computed(() => {
    const principal = this.anuncioPrincipal();
    return this.anuncios()
      .filter((a) => a !== principal)
      .slice(0, 2);
  });

  /** Carreras abiertas donde el usuario todavía no se anotó. */
  readonly otrasInscribibles = computed(() =>
    this.inscribibles().filter((c) => !this.misInscripciones().has(c.id)),
  );

  /**
   * Próxima carrera del usuario: primero una EN_CURSO donde esté anotado, después
   * la más cercana del calendario (`proximas` viene ordenada por fecha asc).
   */
  readonly miProximaCarrera = computed<Carrera | null>(() => {
    const mias = this.misInscripciones();
    const candidatas = [...this.enCurso(), ...this.proximas()].filter(
      (c) => mias.has(c.id) && c.estado !== 'CANCELADA' && c.estado !== 'FINALIZADA',
    );
    return candidatas[0] ?? null;
  });

  readonly miEstadoInscripcion = computed(() => {
    const c = this.miProximaCarrera();
    return c ? (this.misInscripciones().get(c.id) ?? null) : null;
  });

  /** Qué botón principal muestra el hero (punto 24). */
  readonly modoHero = computed<'visitante' | 'inscripto' | 'libre'>(() => {
    if (!this.autenticado()) return 'visitante';
    return this.miProximaCarrera() ? 'inscripto' : 'libre';
  });

  /** "Temporada 2026 · Ronda 8 de 16", calculado del campeonato destacado. */
  readonly eyebrowTemporada = computed(() => {
    const champ = this.campeonato();
    if (!champ) return '';
    const temporada = champ.temporada
      ? t('home.temporada', { anio: champ.temporada })
      : champ.nombre;
    const p = this.progreso();
    if (!p || p.rondasTotales === 0) return temporada;
    if (p.rondasCorridas >= p.rondasTotales) return t('home.finalizada', { temporada });
    return t('home.ronda', { temporada, n: p.rondasCorridas + 1, total: p.rondasTotales });
  });

  ngOnInit(): void {
    this.cargar();
  }

  private cargar(): void {
    const user = this.auth.user();
    forkJoin({
      resumen: this.api.get<ResumenLiga>('/estadisticas/resumen').pipe(this.carga.siFalla(null)),
      proximas: this.api.list<Carrera>('/carreras/proximas').pipe(this.carga.siFalla([])),
      enCurso: this.api.list<Carrera>('/carreras/en-curso').pipe(this.carga.siFalla([])),
      campeonatos: this.api.list<Campeonato>('/campeonatos').pipe(this.carga.siFalla([])),
      anuncios: this.api.list<Anuncio>('/anuncios').pipe(this.carga.siFalla([] as Anuncio[])),
      inscripciones: user
        ? this.api
            .list<Inscripcion>('/inscripciones/usuario/' + user.id)
            .pipe(this.carga.siFalla([]))
        : of([] as Inscripcion[]),
    }).subscribe({
      next: (r) => {
        this.resumen.set(r.resumen);
        this.proximas.set(r.proximas);
        this.enCurso.set(r.enCurso);
        this.anuncios.set(r.anuncios);
        this.misInscripciones.set(
          new Map(
            r.inscripciones
              .filter((i) => i.estado !== 'CANCELADA')
              .map((i) => [i.carreraId, i.estado] as const),
          ),
        );
        const champ = this.elegirCampeonato(r.campeonatos, r.proximas);
        this.campeonato.set(champ);
        if (champ) this.cargarCampeonato(champ);
        this.cargando.set(false);
      },
      error: (err) => {
        this.cargando.set(false);
        this.toast.error(apiError(err));
      },
    });
  }

  /**
   * Campeonato que destaca el home: activo y visible para quien mira (público o
   * del que es miembro). Entre esos, el de la próxima carrera del calendario;
   * si ninguno tiene carreras próximas, el primero activo.
   */
  private elegirCampeonato(campeonatos: Campeonato[], proximas: Carrera[]): Campeonato | null {
    const candidatos = campeonatos.filter(
      (c) => c.estado === 'ACTIVO' && (c.visibilidad === 'PUBLICO' || c.soyMiembro),
    );
    if (candidatos.length === 0) return null;
    for (const carrera of proximas) {
      const match = candidatos.find((c) => c.id === carrera.campeonatoId);
      if (match) return match;
    }
    return candidatos[0];
  }

  private cargarCampeonato(champ: Campeonato): void {
    this.api
      .get<ProgresoCampeonato>('/campeonatos/' + champ.id + '/progreso')
      .pipe(this.carga.siFalla(null))
      .subscribe((p) => this.progreso.set(p));

    // La tabla exige sesión (SecurityConfig): sin login ni se pide, para no
    // disparar un 401 con toast de error al visitante.
    if (!this.autenticado()) return;
    this.cargandoTabla.set(true);
    this.api.get<TablaPosicion[]>('/campeonatos/' + champ.id + '/tabla').subscribe({
      next: (tabla) => {
        this.top3.set(tabla.slice(0, 3));
        const uid = this.auth.user()?.id;
        const idx = tabla.findIndex((t) => t.usuarioId === uid);
        this.miPosicion.set(
          idx < 0
            ? null
            : {
                campeonato: champ.nombre,
                posicion: tabla[idx].posicion || idx + 1,
                total: tabla.length,
                puntos: tabla[idx].puntos,
              },
        );
        this.cargandoTabla.set(false);
      },
      error: () => {
        this.cargandoTabla.set(false);
        this.toast.error(t('home.errorPosiciones'));
      },
    });
  }

  raceDay(c: Carrera): string {
    return fmtDia(c.fecha);
  }
  raceMon(c: Carrera): string {
    return fmtMes(c.fecha);
  }
  raceHora(c: Carrera): string {
    return fmtHora(c.fecha);
  }

  statTxt(n: number | undefined): string {
    return n == null ? '—' : n.toLocaleString('es-AR');
  }

  cuentaRegresiva(c: Carrera): string {
    return fmtCuentaRegresiva(c.fecha, this.reloj.ahora(), c.estado);
  }

  /** % del cupo ocupado (0–100) para la barra; null si la carrera no tiene cupo. */
  cupoPct(c: Carrera): number | null {
    if (!c.cupoMaximo) return null;
    return Math.min(100, Math.round(((c.cuposInscritos ?? 0) / c.cupoMaximo) * 100));
  }

  cupoTxt(c: Carrera): string {
    if (!c.cupoMaximo) return '';
    return `${c.cuposInscritos ?? 0}/${c.cupoMaximo}`;
  }

  yaInscripto(c: Carrera): boolean {
    return this.misInscripciones().has(c.id);
  }

  alternarCta(): void {
    if (this.inscribibles().length === 0) return;
    if (this.ctaAbierto()) {
      this.ctaAbierto.set(false);
    } else {
      this.abrirCta('primero');
    }
  }

  // ---- Teclado del desplegable (patrón WAI-ARIA "menu button") ----------------

  private abrirCta(foco: 'primero' | 'ultimo'): void {
    if (this.inscribibles().length === 0) return;
    this.ctaAbierto.set(true);
    // El panel se renderiza en el próximo ciclo: recién ahí existen los ítems.
    setTimeout(() => this.enfocarItem(foco === 'primero' ? 0 : -1));
  }

  private itemsCta(): HTMLElement[] {
    const host = this.el.nativeElement as HTMLElement;
    return Array.from(host.querySelectorAll<HTMLElement>('#cta-panel [role="menuitem"]'));
  }

  /** Enfoca el ítem `i` (negativo cuenta desde el final). */
  private enfocarItem(i: number): void {
    const items = this.itemsCta();
    if (items.length === 0) return;
    items[(i + items.length) % items.length].focus();
  }

  private cerrarCtaYVolver(): void {
    this.ctaAbierto.set(false);
    (this.el.nativeElement as HTMLElement).querySelector<HTMLElement>('.cta-trigger')?.focus();
  }

  /** Flechas sobre el botón: abren el menú y enfocan el primer/último ítem. */
  teclaTrigger(ev: KeyboardEvent): void {
    if (ev.key === 'ArrowDown' || ev.key === 'ArrowUp') {
      ev.preventDefault();
      this.abrirCta(ev.key === 'ArrowDown' ? 'primero' : 'ultimo');
    }
  }

  /** Flechas/Home/End recorren los ítems; Escape vuelve al botón; Tab cierra. */
  teclaMenu(ev: KeyboardEvent): void {
    const items = this.itemsCta();
    const actual = items.indexOf(document.activeElement as HTMLElement);
    switch (ev.key) {
      case 'ArrowDown':
        ev.preventDefault();
        this.enfocarItem(actual + 1);
        break;
      case 'ArrowUp':
        ev.preventDefault();
        this.enfocarItem(actual < 0 ? -1 : actual - 1);
        break;
      case 'Home':
        ev.preventDefault();
        this.enfocarItem(0);
        break;
      case 'End':
        ev.preventDefault();
        this.enfocarItem(-1);
        break;
      case 'Escape':
        ev.preventDefault();
        ev.stopPropagation();
        this.cerrarCtaYVolver();
        break;
      case 'Tab':
        this.ctaAbierto.set(false);
        break;
    }
  }

  /** Click en una carrera del desplegable: inscribe, pide login o abre el detalle. */
  elegirCarrera(c: Carrera): void {
    this.ctaAbierto.set(false);
    if (!this.autenticado()) {
      this.router.navigate(['/auth'], { queryParams: { next: '/carreras/' + c.id } });
      return;
    }
    if (this.yaInscripto(c)) {
      this.router.navigate(['/carreras', c.id]);
      return;
    }
    this.aConfirmar.set(c);
  }

  mensajeConfirmacion(): string {
    const c = this.aConfirmar();
    if (!c) return '';
    const partes = [
      c.categoriaNombre,
      c.circuito,
      c.fecha ? fmtFecha(c.fecha) + ' ' + fmtHora(c.fecha) : '',
    ]
      .filter(Boolean)
      .join(' · ');
    const lleno = c.cupoMaximo != null && (c.cuposInscritos ?? 0) >= c.cupoMaximo;
    return (
      t('home.confirmarMensaje', { nombre: c.nombre, detalle: partes ? ' (' + partes + ')' : '' }) +
      (lleno ? t('home.confirmarLleno') : '')
    );
  }

  confirmarInscripcion(): void {
    const c = this.aConfirmar();
    if (!c || this.inscribiendo()) return;
    this.inscribiendo.set(true);
    this.api.post<Inscripcion>('/inscripciones', { carreraId: c.id }).subscribe({
      next: (ins) => {
        this.inscribiendo.set(false);
        this.aConfirmar.set(null);
        this.misInscripciones.update((m) => new Map(m).set(c.id, ins?.estado ?? 'INSCRIPTO'));
        if (ins?.estado === 'LISTA_ESPERA') {
          this.toast.success(t('home.quedasteEspera', { nombre: c.nombre }));
        } else {
          this.toast.success(t('home.inscripcionConfirmada', { nombre: c.nombre }));
          this.proximas.update((l) =>
            l.map((x) =>
              x.id === c.id ? { ...x, cuposInscritos: (x.cuposInscritos ?? 0) + 1 } : x,
            ),
          );
        }
      },
      error: (err) => {
        this.inscribiendo.set(false);
        this.aConfirmar.set(null);
        this.toast.error(apiError(err));
      },
    });
  }

  @HostListener('document:click', ['$event'])
  cerrarCtaAfuera(ev: MouseEvent): void {
    if (!this.ctaAbierto()) return;
    const dropdown = (this.el.nativeElement as HTMLElement).querySelector('.cta-dropdown');
    if (dropdown && !dropdown.contains(ev.target as Node)) this.ctaAbierto.set(false);
  }

  @HostListener('document:keydown.escape')
  cerrarCtaEscape(): void {
    if (this.ctaAbierto()) this.cerrarCtaYVolver();
  }
}

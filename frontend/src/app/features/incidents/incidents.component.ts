import { TranslocoPipe } from '@jsverse/transloco';
import { t } from '../../core/i18n/t';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { ApiService, apiError } from '../../core/services/api.service';
import { ErrorCargaService } from '../../core/services/error-carga.service';
import { ToastService } from '../../core/services/toast.service';
import { AuthService } from '../../core/services/auth.service';
import {
  Apelacion,
  Carrera,
  DecisionComisario,
  Incidente,
  IncidentePiloto,
  Resolucion,
  Sancion,
  UsuarioBasico,
  Voto,
} from '../../core/models/models';
import { Chip } from '../../shared/components/chip/chip';
import { EmptyState } from '../../shared/components/empty-state/empty-state';
import { LockedState } from '../../shared/components/locked-state/locked-state';
import { Modal } from '../../shared/components/modal/modal';
import { FechaRelativaPipe } from '../../core/pipes/fecha-relativa.pipe';

type Vista = 'incidentes' | 'sanciones' | 'apelaciones' | 'decisiones';
type FiltroIncidente = 'TODOS' | 'PENDIENTES' | 'RESUELTOS';

/** Sanción que se arma en el panel de resolución y viaja con ella (no se crea sola). */
type SancionBorrador = Pick<Sancion, 'usuarioId' | 'tipo' | 'valor' | 'motivo'>;

/** Clave de traducción de cada decisión (las mismas etiquetas que el chip de voto). */
const DECISION_LABEL: Record<string, string> = {
  A_FAVOR: 'comun.chip.voto.A_FAVOR',
  EN_CONTRA: 'comun.chip.voto.EN_CONTRA',
  ABSTENCION: 'comun.chip.voto.ABSTENCION',
};

@Component({
  selector: 'app-incidents',
  standalone: true,
  imports: [TranslocoPipe, FormsModule, Chip, EmptyState, LockedState, Modal, FechaRelativaPipe],
  styleUrl: './incidents.component.scss',
  templateUrl: './incidents.component.html',
})
export class IncidentsComponent implements OnInit {
  readonly api = inject(ApiService);
  private readonly carga = inject(ErrorCargaService);
  readonly toast = inject(ToastService);
  readonly auth = inject(AuthService);

  readonly vista = signal<Vista>('incidentes');
  readonly cargando = signal(true);

  readonly incidentes = signal<Incidente[]>([]);
  readonly carreras = signal<Carrera[]>([]);
  readonly apelaciones = signal<Apelacion[]>([]);
  readonly sanciones = signal<Sancion[]>([]);
  readonly allSanciones = signal<Sancion[]>([]);
  readonly usuariosBase = signal<UsuarioBasico[]>([]);
  readonly decisiones = signal<DecisionComisario[]>([]);
  readonly filtroEstado = signal<FiltroIncidente>('TODOS');
  readonly decisionLabel = DECISION_LABEL;

  readonly modalDetalle = signal(false);
  readonly detalleIncidente = signal<Incidente | null>(null);
  readonly pilotosIncidente = signal<IncidentePiloto[]>([]);
  readonly votosIncidente = signal<Voto[]>([]);
  readonly resolucionIncidente = signal<Resolucion | null>(null);

  readonly modalApelar = signal(false);
  readonly apelarSancion = signal<Sancion | null>(null);

  readonly modalResolverApelacion = signal(false);
  readonly apelacionResolver = signal<Apelacion | null>(null);
  readonly resolverEstado = signal<string | null>(null);
  readonly resolverRespuesta = signal('');

  readonly modalNuevaSancion = signal(false);
  /** true: el modal de sanción agrega un borrador a la resolución en vez de crearla. */
  readonly sancionParaResolucion = signal(false);

  readonly reportCarreraId = signal('');
  readonly reportVuelta = signal<number | null>(null);
  readonly reportDesc = signal('');
  readonly reportVideo = signal('');

  readonly asignarPilotoId = signal('');
  readonly asignarRol = signal('CAUSANTE');

  readonly votoComentario = signal('');
  readonly resolucionExplicacion = signal('');
  readonly apelarMotivo = signal('');

  readonly sanPilotoId = signal('');
  readonly sanCarreraId = signal('');
  readonly sanTipo = signal('ELO');
  readonly sanValor = signal<number | null>(null);
  readonly sanMotivo = signal('');

  readonly enviando = signal(false);
  readonly resolviendo = signal(false);
  readonly votando = signal(false);

  readonly miVoto = computed(
    () =>
      this.votosIncidente().find((v) => v.comisarioId === this.auth.user()?.id)?.decision ?? null,
  );

  readonly incidentesFiltrados = computed(() => {
    const f = this.filtroEstado();
    return this.incidentes().filter((i) => {
      if (f === 'PENDIENTES') return i.estado !== 'RESUELTO';
      if (f === 'RESUELTOS') return i.estado === 'RESUELTO';
      return true;
    });
  });
  readonly sancionesResolver = signal<SancionBorrador[]>([]);

  ngOnInit(): void {
    if (!this.auth.autenticado()) {
      this.cargando.set(false);
      return;
    }
    this.cargar();
  }

  private cargar(): void {
    const userId = this.auth.user()?.id;
    const sanciones$ = userId
      ? this.api.list<Sancion>('/sanciones/usuario/' + userId).pipe(this.carga.siFalla([]))
      : of<Sancion[]>([]);
    const allSanciones$ = this.auth.esModerador()
      ? this.api.list<Sancion>('/sanciones').pipe(this.carga.siFalla([]))
      : of<Sancion[]>([]);
    const decisiones$ =
      this.auth.esModerador() && userId
        ? this.api
            .list<DecisionComisario>('/incidentes/comisario/' + userId + '/decisiones')
            .pipe(this.carga.siFalla([]))
        : of<DecisionComisario[]>([]);
    forkJoin({
      incidentes: this.api.list<Incidente>('/incidentes').pipe(this.carga.siFalla([])),
      carreras: this.api.list<Carrera>('/carreras').pipe(this.carga.siFalla([])),
      apelaciones: this.api.list<Apelacion>('/apelaciones').pipe(this.carga.siFalla([])),
      usuariosBase: this.api.list<UsuarioBasico>('/usuarios/basico').pipe(this.carga.siFalla([])),
      sanciones: sanciones$,
      allSanciones: allSanciones$,
      decisiones: decisiones$,
    }).subscribe({
      next: (r) => {
        this.incidentes.set(r.incidentes);
        this.carreras.set(r.carreras);
        this.apelaciones.set(r.apelaciones);
        this.usuariosBase.set(r.usuariosBase);
        this.sanciones.set(r.sanciones);
        this.allSanciones.set(r.allSanciones);
        this.decisiones.set(r.decisiones);
        this.cargando.set(false);
      },
      error: (err) => {
        this.cargando.set(false);
        this.toast.error(apiError(err));
      },
    });
  }

  setVista(v: Vista): void {
    this.vista.set(v);
  }

  toNumber(v: unknown, fallback: number | null = null): number | null {
    return v === '' || v === null || v === undefined ? fallback : Number(v);
  }

  abrirDetalle(id: number): void {
    if (this.detalleIncidente()?.id !== id) {
      this.detalleIncidente.set(null);
      this.pilotosIncidente.set([]);
      this.votosIncidente.set([]);
      this.resolucionIncidente.set(null);
      this.sancionesResolver.set([]);
      this.resolucionExplicacion.set('');
    }
    this.modalDetalle.set(true);
    forkJoin({
      incidente: this.api.get<Incidente>('/incidentes/' + id).pipe(this.carga.siFalla(null)),
      pilotos: this.api
        .list<IncidentePiloto>('/incidentes/' + id + '/pilotos')
        .pipe(this.carga.siFalla([])),
      votos: this.api.list<Voto>('/votos/incidente/' + id).pipe(this.carga.siFalla([])),
      resolucion: this.api
        .get<Resolucion>('/incidentes/' + id + '/resolucion')
        .pipe(this.carga.siFalla(null)),
    }).subscribe({
      next: (r) => {
        this.detalleIncidente.set(r.incidente);
        this.pilotosIncidente.set(r.pilotos);
        this.votosIncidente.set(r.votos);
        this.resolucionIncidente.set(r.resolucion);
        this.asignarPilotoId.set('');
      },
    });
  }

  asignarPiloto(): void {
    const id = this.detalleIncidente()?.id;
    const usuarioId = Number(this.asignarPilotoId());
    if (!id || !usuarioId) {
      this.toast.error(t('incidentes.toast.elegiPiloto'));
      return;
    }
    this.api
      .post('/incidentes/' + id + '/pilotos', [{ usuarioId, rol: this.asignarRol() }])
      .subscribe({
        next: () => {
          this.toast.success(t('incidentes.toast.pilotoAsignado'));
          this.abrirDetalle(id);
        },
        error: (err) => this.toast.error(apiError(err)),
      });
  }

  votar(decision: 'A_FAVOR' | 'EN_CONTRA' | 'ABSTENCION'): void {
    const id = this.detalleIncidente()?.id;
    if (!id || this.votando()) return;
    this.votando.set(true);
    this.api
      .post('/incidentes/' + id + '/votos', {
        decision,
        comentario: this.votoComentario()?.trim() || null,
      })
      .subscribe({
        next: () => {
          this.votando.set(false);
          this.toast.success(t('incidentes.toast.votoRegistrado'));
          this.votoComentario.set('');
          this.abrirDetalle(id);
          this.cargar();
        },
        error: (err) => {
          this.votando.set(false);
          this.toast.error(apiError(err));
        },
      });
  }

  resolverIncidente(): void {
    const id = this.detalleIncidente()?.id;
    if (!id) return;
    const explicacion = this.resolucionExplicacion()?.trim();
    if (!explicacion) {
      this.toast.error(t('incidentes.toast.explicaResolucion'));
      return;
    }
    this.resolviendo.set(true);
    const sanciones = this.sancionesResolver();
    this.api
      .post('/incidentes/' + id + '/resolucion', {
        explicacion,
        sanciones: sanciones.length ? sanciones.map((s) => ({ ...s, origen: 'COMISARIO' })) : null,
      })
      .subscribe({
        next: () => {
          this.resolviendo.set(false);
          this.toast.success(t('incidentes.toast.incidenteResuelto'));
          this.modalDetalle.set(false);
          this.cargar();
        },
        error: (err) => {
          this.resolviendo.set(false);
          this.toast.error(apiError(err));
        },
      });
  }

  abrirApelar(s: Sancion): void {
    this.apelarSancion.set(s);
    this.apelarMotivo.set('');
    this.modalApelar.set(true);
  }

  enviarApelacion(): void {
    const sancion = this.apelarSancion();
    const motivo = this.apelarMotivo()?.trim();
    if (!sancion || !motivo) {
      this.toast.error(t('incidentes.toast.motivoApelacion'));
      return;
    }
    this.enviando.set(true);
    this.api.post<Apelacion>('/apelaciones', { sancionId: sancion.id, motivo }).subscribe({
      next: () => {
        this.enviando.set(false);
        this.toast.success(t('incidentes.toast.apelacionEnviada'));
        this.modalApelar.set(false);
        this.cargarApelacionesYSanciones();
      },
      error: (err) => {
        this.enviando.set(false);
        this.toast.error(apiError(err));
      },
    });
  }

  abrirResolverApelacion(a: Apelacion): void {
    this.apelacionResolver.set(a);
    this.resolverEstado.set(null);
    this.resolverRespuesta.set('');
    this.modalResolverApelacion.set(true);
  }

  confirmarResolverApelacion(): void {
    const a = this.apelacionResolver();
    const estado = this.resolverEstado();
    const respuesta = this.resolverRespuesta()?.trim();
    if (!a || !estado || !respuesta) {
      this.toast.error(t('incidentes.toast.estadoYRespuesta'));
      return;
    }
    this.api
      .put<Apelacion>('/apelaciones/' + a.id + '/resolucion', { estado, respuestaAdmin: respuesta })
      .subscribe({
        next: () => {
          this.toast.success(t('incidentes.toast.apelacionResuelta'));
          this.modalResolverApelacion.set(false);
          this.cargar();
        },
        error: (err) => this.toast.error(apiError(err)),
      });
  }

  abrirNuevaSancion(paraResolucion: boolean): void {
    this.sancionParaResolucion.set(paraResolucion);
    this.limpiarFormSancion();
    // En la resolución lo habitual es sancionar al causante: lo preselecciona.
    if (paraResolucion) {
      const causante = this.pilotosIncidente().find((p) => p.rol === 'CAUSANTE');
      if (causante) this.sanPilotoId.set(String(causante.usuarioId));
    }
    this.modalNuevaSancion.set(true);
  }

  private limpiarFormSancion(): void {
    this.sanPilotoId.set('');
    this.sanCarreraId.set('');
    this.sanTipo.set('ELO');
    this.sanValor.set(null);
    this.sanMotivo.set('');
  }

  crearSancion(): void {
    const usuarioId = Number(this.sanPilotoId());
    const motivo = this.sanMotivo()?.trim();
    if (!usuarioId || !motivo) {
      this.toast.error(t('incidentes.toast.pilotoYMotivo'));
      return;
    }
    if (this.sancionParaResolucion()) {
      // Se aplica recién al resolver, junto con la resolución y en su misma carrera.
      this.sancionesResolver.update((arr) => [
        ...arr,
        { usuarioId, tipo: this.sanTipo(), valor: this.sanValor() ?? undefined, motivo },
      ]);
      this.modalNuevaSancion.set(false);
      return;
    }
    this.enviando.set(true);
    this.api
      .post<Sancion>('/sanciones', {
        usuarioId,
        carreraId: this.sanCarreraId() ? Number(this.sanCarreraId()) : null,
        tipo: this.sanTipo(),
        valor: this.sanValor() ?? null,
        motivo,
        origen: 'ADMIN',
      })
      .subscribe({
        next: () => {
          this.enviando.set(false);
          this.toast.success(t('incidentes.toast.sancionCreada'));
          this.modalNuevaSancion.set(false);
          this.limpiarFormSancion();
          this.cargar();
        },
        error: (err) => {
          this.enviando.set(false);
          this.toast.error(apiError(err));
        },
      });
  }

  enviarReporte(): void {
    const carreraId = Number(this.reportCarreraId());
    const descripcion = this.reportDesc()?.trim();
    if (!carreraId || !descripcion) {
      this.toast.error(t('incidentes.toast.carreraYDescripcion'));
      return;
    }
    this.enviando.set(true);
    this.api
      .post<Incidente>('/incidentes', {
        carreraId,
        vuelta: this.reportVuelta() ?? null,
        descripcion,
        videoUrl: this.reportVideo()?.trim() || null,
      })
      .subscribe({
        next: () => {
          this.enviando.set(false);
          this.toast.success(t('incidentes.toast.incidenteReportado'));
          this.reportCarreraId.set('');
          this.reportVuelta.set(null);
          this.reportDesc.set('');
          this.reportVideo.set('');
          this.cargar();
        },
        error: (err) => {
          this.enviando.set(false);
          this.toast.error(apiError(err));
        },
      });
  }

  usuarioNombre(id: number): string {
    return (
      this.usuariosBase().find((u) => u.id === id)?.nombrePiloto ||
      t('incidentes.comisarioN', { id })
    );
  }

  sancionDetalle(s: Pick<Sancion, 'tipo' | 'valor'>): string {
    if (s.valor === null || s.valor === undefined) {
      return s.tipo
        ? s.tipo.toLowerCase().replace(/_/g, ' ')
        : t('incidentes.detalleSancion.generica');
    }
    const val = s.valor > 0 ? '+' + s.valor : String(s.valor);
    switch (s.tipo) {
      case 'ELO':
        return t('incidentes.detalleSancion.elo', { v: val });
      case 'SAFETY_RATING':
        return t('incidentes.detalleSancion.sr', { v: val });
      case 'PUESTOS':
        return t('incidentes.detalleSancion.puestos', { v: val });
      case 'SEGUNDOS':
        return t('incidentes.detalleSancion.segundos', { v: val });
      default:
        return val + ' ' + (s.tipo || '').toLowerCase().replace(/_/g, ' ');
    }
  }

  apelacionExiste(sancionId: number): boolean {
    const userId = this.auth.user()?.id;
    if (!userId) return false;
    return this.apelaciones().some((a) => a.sancionId === sancionId && a.usuarioId === userId);
  }

  quitarSancionResolver(i: number): void {
    this.sancionesResolver.update((arr) => arr.filter((_, idx) => idx !== i));
  }

  private cargarApelacionesYSanciones(): void {
    const userId = this.auth.user()?.id;
    forkJoin({
      apelaciones: this.api.list<Apelacion>('/apelaciones').pipe(this.carga.siFalla([])),
      sanciones: userId
        ? this.api.list<Sancion>('/sanciones/usuario/' + userId).pipe(this.carga.siFalla([]))
        : of([]),
    }).subscribe({
      next: (r) => {
        this.apelaciones.set(r.apelaciones);
        this.sanciones.set(r.sanciones);
      },
    });
  }
}

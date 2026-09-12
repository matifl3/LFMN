import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ApiService, apiError } from '../../core/services/api.service';
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
import { Modal } from '../../shared/components/modal/modal';
import { FechaRelativaPipe } from '../../core/pipes/fecha-relativa.pipe';

type Vista = 'incidentes' | 'sanciones' | 'apelaciones' | 'decisiones';
type FiltroIncidente = 'TODOS' | 'PENDIENTES' | 'RESUELTOS';

const DECISION_LABEL: Record<string, string> = {
  A_FAVOR: 'A favor',
  EN_CONTRA: 'En contra',
  ABSTENCION: 'Abstención',
};

@Component({
  selector: 'app-incidents',
  standalone: true,
  imports: [FormsModule, Chip, EmptyState, Modal, FechaRelativaPipe],
  styleUrl: './incidents.component.scss',
  templateUrl: './incidents.component.html',
})
export class IncidentsComponent implements OnInit {
  readonly api = inject(ApiService);
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

  readonly incidentesFiltrados = computed(() => {
    const f = this.filtroEstado();
    return this.incidentes().filter((i) => {
      if (f === 'PENDIENTES') return i.estado !== 'RESUELTO';
      if (f === 'RESUELTOS') return i.estado === 'RESUELTO';
      return true;
    });
  });
  readonly sancionesResolver = signal<Sancion[]>([]);

  ngOnInit(): void {
    this.cargar();
  }

  private cargar(): void {
    const userId = this.auth.user()?.id;
    const sanciones$ = userId
      ? this.api.list<Sancion>('/sanciones/usuario/' + userId).pipe(catchError(() => of([])))
      : of<Sancion[]>([]);
    const allSanciones$ = this.auth.esModerador()
      ? this.api.list<Sancion>('/sanciones').pipe(catchError(() => of([])))
      : of<Sancion[]>([]);
    const decisiones$ = this.auth.esModerador() && userId
      ? this.api.list<DecisionComisario>('/incidentes/comisario/' + userId + '/decisiones').pipe(catchError(() => of([])))
      : of<DecisionComisario[]>([]);
    forkJoin({
      incidentes: this.api.list<Incidente>('/incidentes').pipe(catchError(() => of([]))),
      carreras: this.api.list<Carrera>('/carreras').pipe(catchError(() => of([]))),
      apelaciones: this.api.list<Apelacion>('/apelaciones').pipe(catchError(() => of([]))),
      usuariosBase: this.api.list<UsuarioBasico>('/usuarios/basico').pipe(catchError(() => of([]))),
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
    this.modalDetalle.set(true);
    forkJoin({
      incidente: this.api.get<Incidente>('/incidentes/' + id).pipe(catchError(() => of(null))),
      pilotos: this.api.list<IncidentePiloto>('/incidentes/' + id + '/pilotos').pipe(catchError(() => of([]))),
      votos: this.api.list<Voto>('/votos/incidente/' + id).pipe(catchError(() => of([]))),
      resolucion: this.api.get<Resolucion>('/incidentes/' + id + '/resolucion').pipe(catchError(() => of(null))),
    }).subscribe({
      next: (r) => {
        this.detalleIncidente.set(r.incidente);
        this.pilotosIncidente.set(r.pilotos);
        this.votosIncidente.set(r.votos);
        this.resolucionIncidente.set(r.resolucion);
        this.sancionesResolver.set([]);
        this.asignarPilotoId.set('');
        this.resolucionExplicacion.set('');
      },
    });
  }

  asignarPiloto(): void {
    const id = this.detalleIncidente()?.id;
    const usuarioId = Number(this.asignarPilotoId());
    if (!id || !usuarioId) {
      this.toast.error('Seleccioná un piloto.');
      return;
    }
    this.api.post('/incidentes/' + id + '/pilotos', [{ usuarioId, rol: this.asignarRol() }]).subscribe({
      next: () => {
        this.toast.success('Piloto asignado.');
        this.abrirDetalle(id);
      },
      error: (err) => this.toast.error(apiError(err)),
    });
  }

  votar(decision: 'A_FAVOR' | 'EN_CONTRA' | 'ABSTENCION'): void {
    const id = this.detalleIncidente()?.id;
    if (!id) return;
    this.api.post('/incidentes/' + id + '/votos', {
      comisarioId: this.auth.user()?.id,
      decision,
      comentario: this.votoComentario()?.trim() || null,
    }).subscribe({
      next: () => {
        this.toast.success('Voto registrado.');
        this.votoComentario.set('');
        this.abrirDetalle(id);
      },
      error: (err) => this.toast.error(apiError(err)),
    });
  }

  resolverIncidente(): void {
    const id = this.detalleIncidente()?.id;
    if (!id) return;
    this.resolviendo.set(true);
    const sanciones = this.sancionesResolver();
    this.api.post('/incidentes/' + id + '/resolucion', {
      comisarioId: this.auth.user()?.id,
      explicacion: this.resolucionExplicacion(),
      sanciones: sanciones.length
        ? sanciones.map((s) => ({ usuarioId: s.usuarioId, tipo: s.tipo, valor: s.valor, motivo: s.motivo, origen: 'COMISARIO' }))
        : null,
    }).subscribe({
      next: () => {
        this.resolviendo.set(false);
        this.toast.success('Incidente resuelto.');
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
      this.toast.error('Escribí el motivo de la apelación.');
      return;
    }
    this.enviando.set(true);
    this.api.post<Apelacion>('/apelaciones', { sancionId: sancion.id, motivo }).subscribe({
      next: () => {
        this.enviando.set(false);
        this.toast.success('Apelación enviada.');
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
      this.toast.error('Elegí un estado y escribí una respuesta.');
      return;
    }
    this.api.put<Apelacion>('/apelaciones/' + a.id + '/resolucion', { estado, respuestaAdmin: respuesta }).subscribe({
      next: () => {
        this.toast.success('Apelación resuelta.');
        this.modalResolverApelacion.set(false);
        this.cargar();
      },
      error: (err) => this.toast.error(apiError(err)),
    });
  }

  crearSancion(): void {
    const usuarioId = Number(this.sanPilotoId());
    const motivo = this.sanMotivo()?.trim();
    if (!usuarioId || !motivo) {
      this.toast.error('Seleccioná un piloto y escribí un motivo.');
      return;
    }
    this.enviando.set(true);
    this.api.post<Sancion>('/sanciones', {
      usuarioId,
      carreraId: this.sanCarreraId() ? Number(this.sanCarreraId()) : null,
      tipo: this.sanTipo(),
      valor: this.sanValor() ?? null,
      motivo,
      origen: 'ADMIN',
    }).subscribe({
      next: () => {
        this.enviando.set(false);
        this.toast.success('Sanción creada.');
        this.modalNuevaSancion.set(false);
        this.sanPilotoId.set('');
        this.sanCarreraId.set('');
        this.sanTipo.set('ELO');
        this.sanValor.set(null);
        this.sanMotivo.set('');
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
      this.toast.error('Seleccioná una carrera y describí el incidente.');
      return;
    }
    this.enviando.set(true);
    this.api.post<Incidente>('/incidentes', {
      carreraId,
      vuelta: this.reportVuelta() ?? null,
      descripcion,
      videoUrl: this.reportVideo()?.trim() || null,
    }).subscribe({
      next: () => {
        this.enviando.set(false);
        this.toast.success('Incidente reportado.');
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
    return this.usuariosBase().find((u) => u.id === id)?.nombrePiloto || 'Comisario #' + id;
  }

  sancionDetalle(s: Sancion): string {
    if (s.valor === null || s.valor === undefined) {
      return (s.tipo || 'sanción').toLowerCase().replace(/_/g, ' ');
    }
    const val = s.valor > 0 ? '+' + s.valor : String(s.valor);
    switch (s.tipo) {
      case 'ELO':
        return val + ' elo';
      case 'SAFETY_RATING':
        return val + ' SR';
      case 'PUESTOS':
        return val + ' puestos';
      case 'SEGUNDOS':
        return val + ' segundos';
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
      apelaciones: this.api.list<Apelacion>('/apelaciones').pipe(catchError(() => of([]))),
      sanciones: userId ? this.api.list<Sancion>('/sanciones/usuario/' + userId).pipe(catchError(() => of([]))) : of([]),
    }).subscribe({
      next: (r) => {
        this.apelaciones.set(r.apelaciones);
        this.sanciones.set(r.sanciones);
      },
    });
  }
}
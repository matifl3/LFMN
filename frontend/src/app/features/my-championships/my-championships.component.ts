import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { DecimalPipe, SlicePipe } from '@angular/common';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ApiService, apiError } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';
import { AuthService } from '../../core/services/auth.service';
import {
  Campeonato,
  Carrera,
  EstadisticasCampeonato,
  MiembroCampeonato,
  TablaPosicion,
} from '../../core/models/models';
import { Chip } from '../../shared/components/chip/chip';
import { Avatar } from '../../shared/components/avatar/avatar';
import { RankBadge } from '../../shared/components/rank-badge/rank-badge';
import { fmtLap, fmtFecha } from '../../core/utils/formato';

const ESTADO_CARRERA_LABEL: Record<string, string> = {
  PROGRAMADA: 'Programada',
  INSCRIPCIONES_ABIERTAS: 'Inscripciones abiertas',
  INSCRIPCIONES_CERRADAS: 'Inscripciones cerradas',
  EN_CURSO: 'En curso',
  FINALIZADA: 'Finalizada',
  CANCELADA: 'Cancelada',
};

const ESTADO_CAMPEONATO_LABEL: Record<string, string> = {
  ACTIVO: 'Activo',
  CERRADO: 'Cerrado',
};

type Panel = 'pilotos' | 'carreras' | 'tabla' | 'estadisticas' | 'importar';

@Component({
  selector: 'app-mis-campeonatos',
  standalone: true,
  imports: [FormsModule, RouterLink, SlicePipe, DecimalPipe, Chip, Avatar, RankBadge],
  templateUrl: './my-championships.component.html',
  styleUrl: './my-championships.component.scss',
})
export class MisCampeonatosComponent implements OnInit {
  readonly auth = inject(AuthService);
  private readonly api = inject(ApiService);
  private readonly toast = inject(ToastService);

  readonly cargando = signal(true);
  readonly campeonatos = signal<Campeonato[]>([]);
  readonly selectedId = signal<number | null>(null);
  readonly panel = signal<Panel>('pilotos');

  readonly miembros = signal<MiembroCampeonato[]>([]);
  readonly carreras = signal<Carrera[]>([]);
  readonly tabla = signal<TablaPosicion[]>([]);
  readonly stats = signal<EstadisticasCampeonato | null>(null);

  /** El alta/edicion de campeonato vive en el panel de admin, no aca. */
  readonly esAdminGlobal = computed(() => this.auth.esAdmin());

  // roster
  readonly busquedaPiloto = signal('');

  // carrera
  readonly editingCarreraId = signal<number | null>(null);
  readonly cNombre = signal('');
  readonly cCircuito = signal('');
  readonly cFecha = signal('');
  readonly cHora = signal('');
  readonly cCupo = signal(24);
  readonly cServidor = signal('');
  readonly cLinkPista = signal('');

  // importacion
  readonly importCarreraId = signal('');
  readonly importJson = signal('');
  readonly importando = signal(false);

  readonly ESTADO_CARRERA_LABEL = ESTADO_CARRERA_LABEL;
  readonly ESTADO_CAMPEONATO_LABEL = ESTADO_CAMPEONATO_LABEL;
  readonly estadosCarrera = Object.keys(ESTADO_CARRERA_LABEL);
  readonly fmtLap = fmtLap;
  readonly fmtFecha = fmtFecha;

  /** Con signo, para los deltas de Elo y Safety Rating. */
  delta(v: number): string {
    if (v === 0) return '0';
    return v > 0 ? '+' + v : String(v);
  }

  deltaColor(v: number): string | null {
    if (v > 0) return 'var(--status-positivo-hi)';
    if (v < 0) return 'var(--status-peligro)';
    return null;
  }

  readonly seleccionado = computed<Campeonato | null>(() => {
    const id = this.selectedId();
    return this.campeonatos().find((c) => c.id === id) ?? null;
  });

  /** Piloto con mas puntos, para el dato destacado de la pestana. */
  readonly lider = computed(() => {
    const pilotos = this.stats()?.pilotos ?? [];
    return pilotos.length > 0 ? pilotos[0] : null;
  });

  /** Ancho de la barra de asistencia de una ronda, en porcentaje del cupo. */
  readonly asistenciaPct = (inscriptos: number, cupo: number): number => {
    if (!cupo || cupo <= 0) return 0;
    return Math.min(100, Math.round((inscriptos / cupo) * 100));
  };

  /** Ancho de la barra de participacion de un piloto, contra el maximo del campeonato. */
  readonly participacionPct = (carreras: number): number => {
    const max = Math.max(...(this.stats()?.pilotos ?? []).map((p) => p.carrerasDisputadas), 0);
    if (max === 0) return 0;
    return Math.round((carreras / max) * 100);
  };

  ngOnInit(): void {
    this.cargar();
  }

  private cargar(): void {
    this.cargando.set(true);
    forkJoin({
      mios: this.api.list<Campeonato>('/campeonatos/mios').pipe(catchError(() => of([]))),
    }).subscribe({
      next: (r) => {
        this.campeonatos.set(r.mios);
        this.cargando.set(false);
        const primero = r.mios[0];
        if (primero && this.selectedId() === null) this.seleccionar(primero.id);
      },
      error: (err) => {
        this.cargando.set(false);
        this.toast.error(apiError(err));
      },
    });
  }

  seleccionar(id: number): void {
    this.selectedId.set(id);
    this.panel.set('pilotos');
    this.cargarDetalle();
  }

  private cargarDetalle(): void {
    const id = this.selectedId();
    if (!id) return;
    forkJoin({
      miembros: this.api
        .list<MiembroCampeonato>(`/campeonatos/${id}/miembros`)
        .pipe(catchError(() => of([] as MiembroCampeonato[]))),
      carreras: this.api
        .list<Carrera>(`/carreras/campeonato/${id}`)
        .pipe(catchError(() => of([] as Carrera[]))),
      tabla: this.api
        .list<TablaPosicion>(`/campeonatos/${id}/tabla`)
        .pipe(catchError(() => of([] as TablaPosicion[]))),
      // Solo el organizador del campeonato (o el ADMIN global) puede leerlo. Si
      // el backend responde 403, la pestana muestra su estado vacio en vez de
      // romper la carga del resto del detalle.
      stats: this.api
        .get<EstadisticasCampeonato>(`/campeonatos/${id}/estadisticas`)
        .pipe(catchError(() => of(null))),
    }).subscribe({
      next: (r) => {
        this.miembros.set(r.miembros);
        this.carreras.set(r.carreras);
        this.tabla.set(r.tabla);
        this.stats.set(r.stats);
      },
      error: () => this.toast.error('No se pudo cargar el detalle del campeonato.'),
    });
  }

  setPanel(p: Panel): void {
    this.panel.set(p);
  }

  cerrarCampeonato(c: Campeonato): void {
    this.api.put(`/campeonatos/${c.id}/cerrar`).subscribe({
      next: () => {
        this.toast.success('Campeonato cerrado.');
        this.cargar();
      },
      error: (err) => this.toast.error(apiError(err)),
    });
  }

  eliminarCampeonato(c: Campeonato): void {
    if (!confirm(`¿Eliminar el campeonato "${c.nombre}"?`)) return;
    this.api.del(`/campeonatos/${c.id}`).subscribe({
      next: () => {
        this.toast.success('Campeonato eliminado.');
        this.selectedId.set(null);
        this.cargar();
      },
      error: (err) => this.toast.error(apiError(err)),
    });
  }

  // ---------------------------------------------------------------- roster

  agregarMiembro(): void {
    const id = this.selectedId();
    const busqueda = this.busquedaPiloto().trim();
    if (!id || !busqueda) {
      this.toast.error('Escribí el nombre de piloto o el email.');
      return;
    }
    const esEmail = busqueda.includes('@');
    const body = esEmail ? { email: busqueda } : { nombrePiloto: busqueda };
    this.api.post<MiembroCampeonato>(`/campeonatos/${id}/miembros`, body).subscribe({
      next: () => {
        this.toast.success('Piloto sumado al campeonato.');
        this.busquedaPiloto.set('');
        this.cargarDetalle();
      },
      error: (err) => this.toast.error(apiError(err)),
    });
  }

  quitarMiembro(m: MiembroCampeonato): void {
    const id = this.selectedId();
    if (!id) return;
    if (!confirm(`¿Sacar a ${m.nombrePiloto} del campeonato?`)) return;
    this.api.del(`/campeonatos/${id}/miembros/${m.usuarioId}`).subscribe({
      next: () => {
        this.toast.success('Piloto quitado.');
        this.cargarDetalle();
      },
      error: (err) => this.toast.error(apiError(err)),
    });
  }

  // --------------------------------------------------------------- carreras

  guardarCarrera(): void {
    const id = this.selectedId();
    const editando = this.editingCarreraId();
    // El alta de carreras es solo del ADMIN global (mismo criterio que el
    // campeonato). El backend lo rechaza igual, pero conviene cortarlo aca para no
    // dejar un formulario a medio llenar que va a fallar seguro.
    if (!editando && !this.esAdminGlobal()) {
      this.toast.error('Solo el administrador global puede crear carreras.');
      return;
    }
    const nombre = this.cNombre().trim();
    if (!id || !nombre) {
      this.toast.error('El nombre de la carrera es obligatorio.');
      return;
    }
    const fecha = this.cFecha() && this.cHora() ? `${this.cFecha()}T${this.cHora()}:00` : undefined;
    const body: Record<string, unknown> = {
      nombre,
      campeonatoId: id,
      circuito: this.cCircuito().trim() || undefined,
      fecha,
      cupoMaximo: this.cCupo() || 24,
      servidor: this.cServidor().trim() || undefined,
      linkPista: this.cLinkPista().trim() || undefined,
    };
    const req = editando
      ? this.api.put<Carrera>(`/carreras/${editando}`, body)
      : this.api.post<Carrera>('/carreras', body);
    req.subscribe({
      next: () => {
        this.toast.success(editando ? 'Carrera actualizada.' : 'Carrera creada.');
        this.limpiarFormCarrera();
        this.cargarDetalle();
      },
      error: (err) => this.toast.error(apiError(err)),
    });
  }

  editarCarrera(c: Carrera): void {
    this.editingCarreraId.set(c.id);
    this.cNombre.set(c.nombre);
    this.cCircuito.set(c.circuito || '');
    this.cCupo.set(c.cupoMaximo || 24);
    this.cServidor.set(c.servidor || '');
    this.cLinkPista.set(c.linkPista || '');
    if (c.fecha) {
      const d = new Date(c.fecha);
      this.cFecha.set(this.isoDate(d));
      this.cHora.set(this.hhmm(d));
    }
  }

  limpiarFormCarrera(): void {
    this.editingCarreraId.set(null);
    this.cNombre.set('');
    this.cCircuito.set('');
    this.cFecha.set('');
    this.cHora.set('');
    this.cCupo.set(24);
    this.cServidor.set('');
    this.cLinkPista.set('');
  }

  cambiarEstadoCarrera(c: Carrera, estado: string): void {
    this.api.put(`/carreras/${c.id}/estado?estado=${encodeURIComponent(estado)}`).subscribe({
      next: () => {
        this.toast.success('Estado actualizado.');
        this.cargarDetalle();
      },
      error: (err) => this.toast.error(apiError(err)),
    });
  }

  eliminarCarrera(c: Carrera): void {
    if (!confirm(`¿Eliminar la carrera "${c.nombre}"?`)) return;
    this.api.del(`/carreras/${c.id}`).subscribe({
      next: () => {
        this.toast.success('Carrera eliminada.');
        this.cargarDetalle();
      },
      error: (err) => this.toast.error(apiError(err)),
    });
  }

  // ----------------------------------------------------------- importacion

  onImportJsonFile(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = () => this.importJson.set(String(reader.result || ''));
    reader.readAsText(file);
    input.value = '';
  }

  importarSesion(): void {
    const carreraId = this.importCarreraId();
    if (!carreraId) {
      this.toast.error('Elegí la carrera a la que pertenece la sesión.');
      return;
    }
    const texto = this.importJson().trim();
    if (!texto) {
      this.toast.error('Pegá o cargá el JSON de la sesión.');
      return;
    }
    let sesion: unknown;
    try {
      sesion = JSON.parse(texto);
    } catch {
      this.toast.error('El JSON no es válido.');
      return;
    }
    const tipo = (sesion as { Type?: string })?.Type;
    if (!tipo) {
      this.toast.error('El JSON no tiene el campo Type (QUALIFY / RACE / PRACTICE).');
      return;
    }
    this.importando.set(true);
    this.api
      .post<{ tipo?: string }>(`/sesiones/importar?carreraId=${carreraId}`, sesion)
      .subscribe({
        next: (res) => {
          this.importando.set(false);
          const tipoProcesado = res?.tipo || tipo;
          this.toast.success(
            tipoProcesado === 'PRACTICE'
              ? 'Sesión PRACTICE procesada (no importa datos de carrera).'
              : `Sesión ${tipoProcesado} importada.`,
          );
          this.importJson.set('');
          this.cargarDetalle();
        },
        error: (err) => {
          this.importando.set(false);
          this.toast.error(apiError(err));
        },
      });
  }

  // ----------------------------------------------------------------- utils

  private isoDate(d: Date): string {
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  }

  private hhmm(d: Date): string {
    return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
  }
}

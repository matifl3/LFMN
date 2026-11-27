import { TranslocoPipe } from '@jsverse/transloco';
import { t } from '../../../core/i18n/t';
import { Component, OnInit, computed, effect, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { ErrorCargaService } from '../../../core/services/error-carga.service';
import { ToastService } from '../../../core/services/toast.service';
import {
  Carrera,
  CarreraAcceso,
  HistorialRating,
  StatsResponse,
  Usuario,
} from '../../../core/models/models';
import {
  fmtCuentaRegresiva,
  fmtDia,
  fmtHora,
  fmtMes,
  normalizeUrl,
} from '../../../core/utils/formato';
import { RelojService } from '../../../core/services/reloj.service';

/** Posición del usuario en el campeonato destacado del home. */
export interface MiPosicion {
  campeonato: string;
  posicion: number;
  total: number;
  puntos: number;
}

/**
 * Panel del piloto logueado en el home: su próxima carrera (con cuenta regresiva
 * y acceso al servidor) y su resumen (Elo, SR, posición, estadísticas).
 */
@Component({
  selector: 'app-panel-piloto',
  standalone: true,
  imports: [TranslocoPipe, RouterLink],
  templateUrl: './panel-piloto.html',
  styleUrl: './panel-piloto.scss',
})
export class PanelPilotoComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly auth = inject(AuthService);
  private readonly carga = inject(ErrorCargaService);
  private readonly toast = inject(ToastService);

  /** Próxima carrera del usuario (EN_CURSO primero, después la más cercana). */
  readonly carrera = input<Carrera | null>(null);
  /** Estado de su inscripción a `carrera`: INSCRIPTO o LISTA_ESPERA. */
  readonly estadoInscripcion = input<string | null>(null);
  readonly posicion = input<MiPosicion | null>(null);
  /** El home todavía está cargando calendario/inscripciones. */
  readonly cargandoHome = input(false);

  readonly yo = signal<Usuario | null>(this.auth.user());
  readonly stats = signal<StatsResponse | null>(null);
  readonly deltaElo = signal<number | null>(null);
  readonly deltaSr = signal<number | null>(null);
  readonly cargando = signal(true);

  readonly acceso = signal<CarreraAcceso | null>(null);
  readonly mostrarPassword = signal(false);

  private readonly reloj = inject(RelojService);

  readonly servidorUrl = computed(() => normalizeUrl(this.acceso()?.servidor));

  readonly cuentaRegresiva = computed(() => {
    const c = this.carrera();
    return c ? fmtCuentaRegresiva(c.fecha, this.reloj.ahora(), c.estado) : '';
  });

  constructor() {
    // El acceso al servidor solo lo ve un INSCRIPTO (no lista de espera): pedirlo
    // en otro estado devolvería un 400 de negocio.
    effect(() => {
      const c = this.carrera();
      this.acceso.set(null);
      this.mostrarPassword.set(false);
      if (!c || this.estadoInscripcion() !== 'INSCRIPTO') return;
      this.api
        .get<CarreraAcceso>('/carreras/' + c.id + '/acceso-servidor')
        .pipe(this.carga.siFalla(null))
        .subscribe((a) => this.acceso.set(a));
    });
  }

  ngOnInit(): void {
    const user = this.auth.user();
    if (!user) return;
    forkJoin({
      // Elo/SR frescos: el usuario de la sesión se guardó al loguear y puede estar viejo.
      yo: this.api.get<Usuario>('/usuarios/me').pipe(this.carga.siFalla(null)),
      stats: this.api
        .get<StatsResponse>('/usuarios/' + user.id + '/stats')
        .pipe(this.carga.siFalla(null)),
      elo: this.api
        .list<HistorialRating>('/usuarios/' + user.id + '/historial-elo')
        .pipe(this.carga.siFalla([] as HistorialRating[])),
      sr: this.api
        .list<HistorialRating>('/usuarios/' + user.id + '/historial-safety-rating')
        .pipe(this.carga.siFalla([] as HistorialRating[])),
    }).subscribe((r) => {
      if (r.yo) {
        this.yo.set(r.yo);
        this.auth.updateUser(r.yo);
      }
      this.stats.set(r.stats);
      this.deltaElo.set(this.ultimoCambio(r.elo));
      this.deltaSr.set(this.ultimoCambio(r.sr));
      this.cargando.set(false);
    });
  }

  /** Suma de los cambios de la última carrera (el historial viene por fecha desc). */
  private ultimoCambio(historial: HistorialRating[]): number | null {
    const ultima = historial.find((h) => h.carreraId != null)?.carreraId;
    if (ultima == null) return null;
    return historial
      .filter((h) => h.carreraId === ultima)
      .reduce((acc, h) => acc + (h.cambio ?? 0), 0);
  }

  deltaTxt(v: number | null): string {
    if (v == null) return '';
    return (v > 0 ? '+' : v < 0 ? '−' : '±') + Math.abs(v);
  }

  dia(c: Carrera): string {
    return fmtDia(c.fecha);
  }
  mes(c: Carrera): string {
    return fmtMes(c.fecha);
  }
  hora(c: Carrera): string {
    return fmtHora(c.fecha);
  }

  copiarPassword(): void {
    const pass = this.acceso()?.contrasenaServidor;
    if (!pass) return;
    navigator.clipboard.writeText(pass).then(
      () => this.toast.success(t('home.panel.copiada')),
      () => this.toast.error(t('home.panel.noCopiada')),
    );
  }
}

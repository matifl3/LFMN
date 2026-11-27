import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Observable, map } from 'rxjs';
import { ApiService } from '../../core/services/api.service';
import { ErrorCargaService } from '../../core/services/error-carga.service';
import { AuthService } from '../../core/services/auth.service';
import { FechaRelativaPipe } from '../../core/pipes/fecha-relativa.pipe';
import { EmptyState } from '../../shared/components/empty-state/empty-state';
import { Logro, UsuarioLogro } from '../../core/models/models';

type LogroEstado = Omit<UsuarioLogro, 'progreso' | 'obtenido'> & {
  progreso?: number;
  obtenido?: boolean;
};

@Component({
  selector: 'app-achievements',
  standalone: true,
  imports: [EmptyState, RouterLink, FechaRelativaPipe],
  styleUrl: './achievements.component.scss',
  templateUrl: './achievements.component.html',
})
export class AchievementsComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly carga = inject(ErrorCargaService);
  readonly auth = inject(AuthService);

  readonly logros = signal<LogroEstado[]>([]);
  readonly cargando = signal(true);

  readonly obtenidos = computed(() => this.logros().filter((lg) => lg.obtenido).length);
  readonly total = computed(() => this.logros().length);
  readonly progressPct = computed(() =>
    this.total() ? Math.round((this.obtenidos() / this.total()) * 100) : 0,
  );
  readonly eyebrow = computed(() =>
    this.auth.user()
      ? this.obtenidos() + ' DE ' + this.total() + ' DESBLOQUEADOS'
      : this.total() + ' LOGROS PARA DESBLOQUEAR',
  );

  /**
   * Primero los obtenidos (el más reciente arriba) y después los bloqueados, del
   * más cercano a completarse al más lejano: lo que conviene mirar queda arriba.
   */
  readonly ordenados = computed(() =>
    [...this.logros()].sort((a, b) => {
      if (!!a.obtenido !== !!b.obtenido) return a.obtenido ? -1 : 1;
      if (a.obtenido) return (b.fechaObtencion ?? '').localeCompare(a.fechaObtencion ?? '');
      return this.progresoPct(b) - this.progresoPct(a) || a.logroId - b.logroId;
    }),
  );

  ngOnInit(): void {
    this.cargar();
  }

  private cargar(): void {
    const userId = this.auth.user()?.id;
    // Sin sesión se muestra igual el catálogo (el endpoint es público), sin progreso.
    const obs: Observable<LogroEstado[]> = userId
      ? this.api.list<LogroEstado>(`/logros/usuario/${userId}`)
      : this.api.list<Logro>('/logros').pipe(
          map((ls) =>
            ls.map((l) => ({
              logroId: l.id,
              nombre: l.nombre,
              descripcion: l.descripcion,
              tipoCondicion: l.tipoCondicion,
              valorCondicion: l.valorCondicion,
              icono: l.icono,
            })),
          ),
        );
    obs.pipe(this.carga.siFalla([] as LogroEstado[])).subscribe((ls) => {
      this.logros.set(ls);
      this.cargando.set(false);
    });
  }

  progresoPct(lg: LogroEstado): number {
    return lg.valorCondicion ? Math.round(((lg.progreso ?? 0) / lg.valorCondicion) * 100) : 0;
  }
}

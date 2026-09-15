import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { map } from 'rxjs';
import { Observable } from 'rxjs';
import { ApiService, apiError } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';
import { AuthService } from '../../core/services/auth.service';
import { EmptyState } from '../../shared/components/empty-state/empty-state';
import { LockedState } from '../../shared/components/locked-state/locked-state';
import { Logro, UsuarioLogro } from '../../core/models/models';

type LogroEstado = Omit<UsuarioLogro, 'progreso' | 'obtenido'> & {
  icono?: string;
  progreso?: number;
  obtenido?: boolean;
};

@Component({
  selector: 'app-achievements',
  standalone: true,
  imports: [EmptyState, LockedState],
  styleUrl: './achievements.component.scss',
  templateUrl: './achievements.component.html',
})
export class AchievementsComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly toast = inject(ToastService);
  readonly auth = inject(AuthService);

  readonly logros = signal<LogroEstado[]>([]);
  readonly cargando = signal(true);

  readonly obtenidos = computed(() => this.logros().filter((lg) => lg.obtenido).length);
  readonly total = computed(() => this.logros().length);
  readonly progressPct = computed(() => (this.total() ? Math.round((this.obtenidos() / this.total()) * 100) : 0));
  readonly eyebrow = computed(() =>
    this.auth.user() ? this.obtenidos() + ' DE ' + this.total() + ' DESBLOQUEADOS' : 'INICIÁ SESIÓN PARA VER TU PROGRESO'
  );

  ngOnInit(): void {
    this.cargar();
  }

  private cargar(): void {
    const userId = this.auth.user()?.id;
    const obs: Observable<LogroEstado[]> = userId
      ? this.api.list<LogroEstado>(`/logros/usuario/${userId}`)
      : this.api
          .list<Logro>('/logros')
          .pipe(
            map((ls) =>
              ls.map((l) => ({
                logroId: l.id,
                nombre: l.nombre,
                descripcion: l.descripcion,
                tipoCondicion: l.tipoCondicion,
                valorCondicion: l.valorCondicion,
                icono: l.icono,
                fechaObtencion: undefined,
              }))
            )
          );
    obs.subscribe({
      next: (ls) => {
        this.logros.set(ls);
        this.cargando.set(false);
      },
      error: (err) => {
        this.cargando.set(false);
        this.toast.error(apiError(err));
      },
    });
  }

  progresoPct(lg: LogroEstado): number {
    return lg.valorCondicion ? Math.round(((lg.progreso ?? 0) / lg.valorCondicion) * 100) : 0;
  }
}
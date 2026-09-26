import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ApiService, apiError } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';
import { RankBadge } from '../../shared/components/rank-badge/rank-badge';
import { Campeonato, TablaPosicion } from '../../core/models/models';

@Component({
  selector: 'app-championship',
  standalone: true,
  imports: [RouterLink, RankBadge],
  styleUrl: './championship.scss',
  templateUrl: './championship.html',
})
export class ChampionshipComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly toast = inject(ToastService);

  readonly champs = signal<Campeonato[]>([]);
  readonly tabla = signal<TablaPosicion[]>([]);
  readonly selectedId = signal<number | null>(null);
  readonly tablaLoading = signal(false);
  readonly tablaMsg = signal<string | null>(null);
  readonly cargando = signal(true);

  readonly champInfo = computed(() => {
    const c = this.champs().find((x) => x.id === this.selectedId());
    if (!c) return '';
    return (
      (c.temporada || 'Temporada 2026') +
      ' · ' +
      (c.sistemaPuntos || '—') +
      ' · ' +
      (c.estado || '—')
    );
  });

  /** El campeonato y su calendario son publicos; la tabla no. */
  readonly bloqueado = computed(() => {
    const c = this.champs().find((x) => x.id === this.selectedId());
    return !!c && c.visibilidad === 'PRIVADO' && !c.soyMiembro;
  });

  ngOnInit(): void {
    this.api.list<Campeonato>('/campeonatos').subscribe({
      next: (list) => {
        this.champs.set(list);
        this.cargando.set(false);
        if (list.length) {
          this.selectedId.set(list[0].id);
          this.loadTabla(list[0].id);
        }
      },
      error: (err) => {
        this.cargando.set(false);
        this.toast.error(apiError(err));
      },
    });
  }

  onChange(e: Event): void {
    const v = Number((e.target as HTMLSelectElement).value);
    this.selectedId.set(v);
    this.loadTabla(v);
  }

  private loadTabla(id: number): void {
    this.tablaLoading.set(true);
    this.tablaMsg.set(null);
    const c = this.champs().find((x) => x.id === id);
    if (c && c.visibilidad === 'PRIVADO' && !c.soyMiembro) {
      this.tablaLoading.set(false);
      this.tabla.set([]);
      this.tablaMsg.set(null);
      return;
    }
    this.api.get<TablaPosicion[]>('/campeonatos/' + id + '/tabla').subscribe({
      next: (list) => {
        this.tablaLoading.set(false);
        if (!list || list.length === 0) {
          this.tabla.set([]);
          this.tablaMsg.set('Todavía no hay posiciones registradas.');
        } else {
          this.tabla.set(list);
        }
      },
      error: () => {
        this.tablaLoading.set(false);
        this.tabla.set([]);
        this.tablaMsg.set('Error al cargar posiciones.');
      },
    });
  }
}

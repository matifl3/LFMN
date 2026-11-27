import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { ErrorCargaService } from '../../../core/services/error-carga.service';
import { Chip } from '../../../shared/components/chip/chip';
import { EmptyState } from '../../../shared/components/empty-state/empty-state';
import { Carrera, Categoria } from '../../../core/models/models';
import { fmtDia, fmtFecha, fmtHora, fmtMes } from '../../../core/utils/formato';

@Component({
  selector: 'app-races-list',
  standalone: true,
  imports: [RouterLink, Chip, EmptyState],
  templateUrl: './races-list.html',
})
export class RacesListComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly carga = inject(ErrorCargaService);
  private readonly route = inject(ActivatedRoute);

  readonly cats = signal<Categoria[]>([]);
  readonly proximasRaw = signal<Carrera[]>([]);
  readonly pasadasRaw = signal<Carrera[]>([]);
  readonly cargando = signal(true);
  readonly view = signal<'proximas' | 'pasadas'>('proximas');
  readonly filtroCat = signal<string | null>(null);

  /**
   * El backend separa próximas/pasadas solo por fecha, así que una carrera EN_CURSO
   * (ya arrancó) llega en /pasadas. La subimos a Próximas, primera, para que no
   * quede enterrada entre las corridas.
   */
  private readonly enCurso = computed(() =>
    this.pasadasRaw().filter((c) => c.estado === 'EN_CURSO'),
  );

  readonly proximasFiltradas = computed(() =>
    [...this.enCurso(), ...this.proximasRaw()].filter((c) => this.pasaFiltro(c)),
  );

  readonly pasadasFiltradas = computed(() =>
    this.pasadasRaw().filter((c) => c.estado !== 'EN_CURSO' && this.pasaFiltro(c)),
  );

  readonly listaActual = computed(() =>
    this.view() === 'proximas' ? this.proximasFiltradas() : this.pasadasFiltradas(),
  );

  ngOnInit(): void {
    const cat = this.route.snapshot.queryParamMap.get('cat');
    if (cat) this.filtroCat.set(cat);
    forkJoin({
      categorias: this.api.list<Categoria>('/categorias').pipe(this.carga.siFalla([])),
      proximas: this.api.list<Carrera>('/carreras/proximas').pipe(this.carga.siFalla([])),
      pasadas: this.api.list<Carrera>('/carreras/pasadas').pipe(this.carga.siFalla([])),
    }).subscribe({
      next: (r) => {
        this.cats.set(r.categorias);
        this.proximasRaw.set(r.proximas);
        this.pasadasRaw.set(r.pasadas);
        this.cargando.set(false);
      },
      error: () => this.cargando.set(false),
    });
  }

  setView(v: 'proximas' | 'pasadas'): void {
    this.view.set(v);
  }

  onChangeCategoria(e: Event): void {
    this.filtroCat.set((e.target as HTMLSelectElement).value || null);
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

  racePracticaHora(c: Carrera): string {
    if (!c.practicaFecha) return '';
    return fmtFecha(c.practicaFecha) + ' ' + fmtHora(c.practicaFecha);
  }

  cupoN(c: Carrera): number {
    return c.cuposInscritos || 0;
  }

  cupoPct(c: Carrera): number {
    return c.cupoMaximo ? Math.round((this.cupoN(c) / c.cupoMaximo) * 100) : 0;
  }

  isFull(c: Carrera): boolean {
    return typeof c.cupoMaximo === 'number' && c.cupoMaximo > 0 && this.cupoN(c) >= c.cupoMaximo;
  }

  /**
   * Chip de la fila. Respeta el estado real (antes PROGRAMADA salía como
   * "Inscripciones abiertas") y marca las que ya pasaron de fecha sin cerrarse.
   */
  chipDe(c: Carrera): { clase: string; label: string } | null {
    if (c.estado === 'EN_CURSO') return { clase: 'chip-live', label: 'En vivo' };
    const abierta = c.estado === 'PROGRAMADA' || c.estado === 'INSCRIPCIONES_ABIERTAS';
    if (abierta && this.view() === 'pasadas') {
      return { clase: 'chip-pending', label: 'Sin resultados' };
    }
    if (c.estado === 'INSCRIPCIONES_ABIERTAS' && this.isFull(c)) {
      return { clase: 'chip-closed', label: 'Cupo lleno' };
    }
    return null; // el resto lo resuelve <app-chip> con el mapa de estados
  }

  private pasaFiltro(c: Carrera): boolean {
    return !this.filtroCat() || String(c.categoriaId) === this.filtroCat();
  }
}

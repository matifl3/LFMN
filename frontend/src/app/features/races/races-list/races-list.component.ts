import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ApiService } from '../../../core/services/api.service';
import { Chip } from '../../../shared/components/chip/chip';
import { EmptyState } from '../../../shared/components/empty-state/empty-state';
import { Carrera, Categoria } from '../../../core/models/models';
import { fmtFecha, fmtHora } from '../../../core/utils/formato';

@Component({
  selector: 'app-races-list',
  standalone: true,
  imports: [RouterLink, Chip, EmptyState],
  templateUrl: './races-list.html',
})
export class RacesListComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly route = inject(ActivatedRoute);

  readonly cats = signal<Categoria[]>([]);
  readonly proximasRaw = signal<Carrera[]>([]);
  readonly pasadasRaw = signal<Carrera[]>([]);
  readonly cargando = signal(true);
  readonly view = signal<'proximas' | 'pasadas'>('proximas');
  readonly filtroCat = signal<string | null>(null);

  readonly proximasFiltradas = computed(() =>
    this.proximasRaw().filter((c) => !this.filtroCat() || String(c.categoriaId) === this.filtroCat())
  );

  readonly pasadasFiltradas = computed(() =>
    this.pasadasRaw().filter((c) => !this.filtroCat() || String(c.categoriaId) === this.filtroCat())
  );

  readonly listaActual = computed(() => (this.view() === 'proximas' ? this.proximasFiltradas() : this.pasadasFiltradas()));

  ngOnInit(): void {
    const cat = this.route.snapshot.queryParamMap.get('cat');
    if (cat) this.filtroCat.set(cat);
    forkJoin({
      categorias: this.api.list<Categoria>('/categorias').pipe(catchError(() => of([]))),
      proximas: this.api.list<Carrera>('/carreras/proximas').pipe(catchError(() => of([]))),
      pasadas: this.api.list<Carrera>('/carreras/pasadas').pipe(catchError(() => of([]))),
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
    return fmtFecha(c.fecha).split(' ')[0];
  }

  raceMon(c: Carrera): string {
    return fmtFecha(c.fecha).split(' ')[1] || '';
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
}
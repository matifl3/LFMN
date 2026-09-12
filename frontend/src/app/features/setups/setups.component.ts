import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ApiService, apiError } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';
import { AuthService } from '../../core/services/auth.service';
import { Avatar } from '../../shared/components/avatar/avatar';
import { EmptyState } from '../../shared/components/empty-state/empty-state';
import { StarRating } from '../../shared/components/star-rating/star-rating';
import { FechaRelativaPipe } from '../../core/pipes/fecha-relativa.pipe';
import { Categoria, Setup, SetupComentario } from '../../core/models/models';

@Component({
  selector: 'app-setups',
  standalone: true,
  imports: [Avatar, EmptyState, StarRating, FechaRelativaPipe],
  styleUrl: './setups.scss',
  templateUrl: './setups.html',
})
export class SetupsComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly toast = inject(ToastService);
  private readonly auth = inject(AuthService);

  readonly setups = signal<Setup[]>([]);
  readonly cats = signal<Categoria[]>([]);
  readonly selectedId = signal<number | null>(null);
  readonly detailCargando = signal(false);
  readonly uploadVisible = signal(false);
  readonly comentarios = signal<SetupComentario[]>([]);
  readonly searchText = signal('');
  readonly filterCircuito = signal('');
  readonly filterVehiculo = signal('');
  readonly filterOrder = signal('rating');
  readonly cargando = signal(true);
  readonly archivo = signal<File | null>(null);
  readonly myRating = signal(4);
  readonly fTitulo = signal('');
  readonly fDescripcion = signal('');
  readonly fCircuito = signal('');
  readonly fVehiculo = signal('');
  readonly fCategoria = signal<number | null>(null);
  readonly fComentario = signal('');
  readonly editingId = signal<number | null>(null);
  readonly eTitulo = signal('');
  readonly eDescripcion = signal('');
  readonly eCircuito = signal('');
  readonly eVehiculo = signal('');
  readonly eCategoria = signal<number | null>(null);

  readonly archivoNombre = computed(() => this.archivo()?.name ?? '');
  readonly autenticado = computed(() => this.auth.autenticado());
  readonly esAdmin = computed(() => this.auth.esAdmin());
  readonly detail = computed(() => this.setups().find((s) => s.id === this.selectedId()) ?? null);
  readonly catsById = computed(() => new Map(this.cats().map((c) => [c.id, c] as const)));
  readonly fCategoriaFija = computed(() => {
    const cat = this.catsById().get(this.fCategoria() ?? -1);
    return cat?.setupFijo === true;
  });
  readonly puedePublicar = computed(() => this.esAdmin() || !this.fCategoriaFija());

  esSetupFijo(s: Setup): boolean {
    return this.catsById().get(s.categoriaId ?? -1)?.setupFijo === true;
  }

  puedeEditar(s: Setup): boolean {
    return this.esAdmin() || s.autorId === this.auth.user()?.id;
  }

  readonly circuitos = computed(() => Array.from(new Set(this.setups().map((s) => s.circuito))).sort());
  readonly vehiculos = computed(() => Array.from(new Set(this.setups().map((s) => s.vehiculo))).sort());

  readonly filtered = computed(() => {
    const q = this.searchText().toLowerCase().trim();
    const c = this.filterCircuito();
    const v = this.filterVehiculo();
    const order = this.filterOrder();
    const list = this.setups().filter((s) => {
      if (c && s.circuito !== c) return false;
      if (v && s.vehiculo !== v) return false;
      if (
        q &&
        !(
          (s.titulo || '').toLowerCase().includes(q) ||
          (s.vehiculo || '').toLowerCase().includes(q) ||
          (s.circuito || '').toLowerCase().includes(q) ||
          (s.descripcion || '').toLowerCase().includes(q)
        )
      ) {
        return false;
      }
      return true;
    });
    return list.sort((a, b) => {
      if (order === 'recientes') return new Date(b.fechaPublicacion).getTime() - new Date(a.fechaPublicacion).getTime();
      return (b.promedioCalificacion || 0) - (a.promedioCalificacion || 0);
    });
  });

  ngOnInit(): void {
    forkJoin({
      setups: this.api.list<Setup>('/setups').pipe(catchError(() => of([]))),
      cats: this.api.list<Categoria>('/categorias').pipe(catchError(() => of([]))),
    }).subscribe({
      next: (r) => {
        this.cats.set(r.cats);
        this.setups.set(r.setups);
        this.cargando.set(false);
        if (r.setups.length) this.select(r.setups[0].id);
      },
      error: (err) => {
        this.cargando.set(false);
        this.toast.error(apiError(err));
      },
    });
  }

  inputVal(e: Event): string {
    return (e.target as HTMLInputElement | HTMLTextAreaElement).value;
  }

  selectStr(e: Event): string {
    return (e.target as HTMLSelectElement).value;
  }

  selectNum(e: Event): number | null {
    const v = (e.target as HTMLSelectElement).value;
    return v ? Number(v) : null;
  }

  toggleUpload(): void {
    this.uploadVisible.update((v) => !v);
  }

  onFileSelect(e: Event): void {
    this.archivo.set((e.target as HTMLInputElement).files?.[0] ?? null);
  }

  select(id: number): void {
    this.selectedId.set(id);
    this.loadDetail(id);
  }

  private loadDetail(id: number): void {
    this.detailCargando.set(true);
    this.comentarios.set([]);
    this.api.get<Setup>('/setups/' + id).subscribe({
      next: (s) => {
        this.setups.update((l) => l.map((x) => (x.id === id ? s : x)));
        this.myRating.set(4);
        this.detailCargando.set(false);
      },
      error: (err) => {
        this.detailCargando.set(false);
        this.toast.error(apiError(err));
      },
    });
    this.cargarComentarios(id);
  }

  private cargarComentarios(id: number): void {
    this.api.list<SetupComentario>('/setups/' + id + '/comentarios').subscribe({
      next: (list) => this.comentarios.set(list),
      error: () => this.comentarios.set([]),
    });
  }

  private reloadSetup(id: number): void {
    this.api.get<Setup>('/setups/' + id).subscribe({
      next: (s) => this.setups.update((l) => l.map((x) => (x.id === id ? s : x))),
      error: () => undefined,
    });
  }

  promedio(s: Setup): string {
    return s.promedioCalificacion != null ? s.promedioCalificacion.toFixed(1) : '—';
  }

  calificar(e: Event, id: number): void {
    if (!this.auth.autenticado()) return;
    const puntaje = Number((e.target as HTMLInputElement).value);
    this.api.post('/setups/' + id + '/calificaciones', { puntaje }).subscribe({
      next: () => {
        this.toast.success('Calificación guardada');
        this.reloadSetup(id);
      },
      error: (err) => this.toast.error(apiError(err)),
    });
  }

  publicar(): void {
    const user = this.auth.user();
    if (!user) {
      this.toast.error('Ingresá para subir un setup.');
      return;
    }
    const titulo = this.fTitulo().trim();
    const circuito = this.fCircuito().trim();
    const vehiculo = this.fVehiculo().trim();
    if (!titulo || !circuito || !vehiculo) {
      this.toast.error('Completá título, circuito y vehículo.');
      return;
    }
    if (this.fCategoriaFija() && !this.esAdmin()) {
      this.toast.error('Esta categoría tiene un setup fijo. Solo el comité puede publicarlo.');
      return;
    }
    this.api
      .post<Setup>('/setups', {
        titulo,
        descripcion: this.fDescripcion().trim(),
        circuito,
        vehiculo,
        autorId: user.id,
        categoriaId: this.fCategoria(),
      })
      .subscribe({
        next: (nuevo) => {
          const archivo = this.archivo();
          if (archivo) {
            const fd = new FormData();
            fd.append('archivo', archivo);
            this.api.post('/setups/' + nuevo.id + '/archivo', fd).subscribe({
              error: (err) => this.toast.error(apiError(err)),
            });
          }
          this.setups.update((l) => [nuevo, ...l]);
          this.uploadVisible.set(false);
          this.fTitulo.set('');
          this.fDescripcion.set('');
          this.fCircuito.set('');
          this.fVehiculo.set('');
          this.fCategoria.set(null);
          this.archivo.set(null);
          this.select(nuevo.id);
          this.toast.success('Setup publicado');
        },
        error: (err) => this.toast.error(apiError(err)),
      });
  }

  publicarComentario(id: number): void {
    const texto = this.fComentario().trim();
    if (!texto) {
      this.toast.error('Escribí un comentario primero.');
      return;
    }
    this.api.post('/setups/' + id + '/comentarios', { texto }).subscribe({
      next: () => {
        this.toast.success('Comentario publicado');
        this.fComentario.set('');
        this.cargarComentarios(id);
      },
      error: (err) => this.toast.error(apiError(err)),
    });
  }

  eliminarSetup(e: Event, id: number): void {
    e.stopPropagation();
    if (!confirm('¿Eliminar este setup y todos sus comentarios?')) return;
    this.api.del('/setups/' + id).subscribe({
      next: () => {
        this.toast.success('Setup eliminado');
        this.setups.update((l) => l.filter((s) => s.id !== id));
        if (this.selectedId() === id) {
          this.selectedId.set(null);
          this.comentarios.set([]);
        }
      },
      error: (err) => this.toast.error(apiError(err)),
    });
  }

  eliminarComentario(e: Event, setupId: number, comentarioId: number): void {
    e.stopPropagation();
    if (!confirm('¿Eliminar este comentario?')) return;
    this.api.del(`/setups/${setupId}/comentarios/${comentarioId}`).subscribe({
      next: () => {
        this.toast.success('Comentario eliminado');
        this.comentarios.update((l) => l.filter((c) => c.id !== comentarioId));
      },
      error: (err) => this.toast.error(apiError(err)),
    });
  }

  editarSetup(s: Setup): void {
    this.editingId.set(s.id);
    this.eTitulo.set(s.titulo);
    this.eDescripcion.set(s.descripcion || '');
    this.eCircuito.set(s.circuito || '');
    this.eVehiculo.set(s.vehiculo || '');
    this.eCategoria.set(s.categoriaId ?? null);
  }

  cancelarEdit(): void {
    this.editingId.set(null);
  }

  guardarEdit(): void {
    const id = this.editingId();
    if (id === null) return;
    const titulo = this.eTitulo().trim();
    const circuito = this.eCircuito().trim();
    const vehiculo = this.eVehiculo().trim();
    if (!titulo || !circuito || !vehiculo) {
      this.toast.error('Completá título, circuito y vehículo.');
      return;
    }
    this.api.put('/setups/' + id, {
      titulo,
      descripcion: this.eDescripcion().trim(),
      circuito,
      vehiculo,
      categoriaId: this.eCategoria(),
    }).subscribe({
      next: () => {
        this.toast.success('Setup actualizado');
        this.editingId.set(null);
        this.reloadSetup(id);
      },
      error: (err) => this.toast.error(apiError(err)),
    });
  }
}
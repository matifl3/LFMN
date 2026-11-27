import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { ApiService, apiError } from '../../core/services/api.service';
import { ErrorCargaService } from '../../core/services/error-carga.service';
import { ToastService } from '../../core/services/toast.service';
import { AuthService } from '../../core/services/auth.service';
import { Avatar } from '../../shared/components/avatar/avatar';
import { EmptyState } from '../../shared/components/empty-state/empty-state';
import { StarRating } from '../../shared/components/star-rating/star-rating';
import { Modal } from '../../shared/components/modal/modal';
import { ConfirmDialog } from '../../shared/components/confirm-dialog/confirm-dialog';
import { FechaRelativaPipe } from '../../core/pipes/fecha-relativa.pipe';
import { Categoria, Setup, SetupCalificacion, SetupComentario } from '../../core/models/models';

/** Las mismas que acepta SetupService.guardarArchivo en el backend. */
const EXTENSIONES = ['.ini', '.acd', '.json', '.rar', '.zip'];
/** spring.servlet.multipart.max-file-size */
const MAX_BYTES = 10 * 1024 * 1024;

type Confirmacion =
  | { tipo: 'setup'; id: number; titulo: string }
  | { tipo: 'comentario'; setupId: number; id: number };

@Component({
  selector: 'app-setups',
  standalone: true,
  imports: [RouterLink, Avatar, EmptyState, StarRating, Modal, ConfirmDialog, FechaRelativaPipe],
  styleUrl: './setups.scss',
  templateUrl: './setups.html',
})
export class SetupsComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly carga = inject(ErrorCargaService);
  private readonly toast = inject(ToastService);
  private readonly auth = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly extensionesTxt = EXTENSIONES.join(', ');
  readonly estrellas = [1, 2, 3, 4, 5];

  readonly setups = signal<Setup[]>([]);
  readonly cats = signal<Categoria[]>([]);
  readonly selectedId = signal<number | null>(null);
  readonly detailCargando = signal(false);
  readonly comentarios = signal<SetupComentario[]>([]);
  readonly calificaciones = signal<SetupCalificacion[]>([]);
  readonly cargando = signal(true);

  // Filtros
  readonly searchText = signal('');
  readonly filterCircuito = signal('');
  readonly filterVehiculo = signal('');
  readonly filterCategoria = signal<number | null>(null);
  readonly filterOrder = signal<'rating' | 'recientes'>('rating');

  // Formulario de alta
  readonly uploadVisible = signal(false);
  readonly publicando = signal(false);
  readonly archivo = signal<File | null>(null);
  readonly arrastrando = signal(false);
  readonly fTitulo = signal('');
  readonly fDescripcion = signal('');
  readonly fCircuito = signal('');
  readonly fVehiculo = signal('');
  readonly fCategoria = signal<number | null>(null);
  readonly intentoPublicar = signal(false);

  // Edición
  readonly editingId = signal<number | null>(null);
  readonly eTitulo = signal('');
  readonly eDescripcion = signal('');
  readonly eCircuito = signal('');
  readonly eVehiculo = signal('');
  readonly eCategoria = signal<number | null>(null);

  readonly fComentario = signal('');
  readonly descargando = signal(false);
  readonly subiendoArchivo = signal(false);
  readonly aConfirmar = signal<Confirmacion | null>(null);

  readonly autenticado = computed(() => this.auth.autenticado());
  readonly esAdmin = computed(() => this.auth.esAdmin());
  private readonly miId = computed(() => this.auth.user()?.id ?? null);

  readonly detail = computed(() => this.setups().find((s) => s.id === this.selectedId()) ?? null);
  readonly catsById = computed(() => new Map(this.cats().map((c) => [c.id, c] as const)));

  readonly fCategoriaInfo = computed(() => this.catsById().get(this.fCategoria() ?? -1) ?? null);
  readonly fCategoriaFija = computed(() => this.fCategoriaInfo()?.setupFijo === true);
  readonly puedePublicar = computed(() => this.esAdmin() || !this.fCategoriaFija());

  readonly errorArchivo = computed(() => this.validarArchivo(this.archivo()));
  readonly faltanCampos = computed(() => ({
    titulo: !this.fTitulo().trim(),
    circuito: !this.fCircuito().trim(),
    vehiculo: !this.fVehiculo().trim(),
  }));

  /** Mi puntaje actual del setup abierto (null si todavía no lo califiqué). */
  readonly miPuntaje = computed(() => {
    const id = this.miId();
    return this.calificaciones().find((c) => c.usuarioId === id)?.puntaje ?? null;
  });
  readonly puntajeHover = signal<number | null>(null);

  readonly circuitos = computed(() => this.unicos(this.setups().map((s) => s.circuito)));
  readonly vehiculos = computed(() => this.unicos(this.setups().map((s) => s.vehiculo)));
  /** Categorías que tienen al menos un setup, para el filtro. */
  readonly catsConSetups = computed(() => {
    const ids = new Set(
      this.setups()
        .map((s) => s.categoriaId)
        .filter((x) => x != null),
    );
    return this.cats().filter((c) => ids.has(c.id));
  });

  readonly filtered = computed(() => {
    const q = this.searchText().toLowerCase().trim();
    const c = this.filterCircuito().toLowerCase();
    const v = this.filterVehiculo().toLowerCase();
    const cat = this.filterCategoria();
    const order = this.filterOrder();
    const list = this.setups().filter((s) => {
      if (c && (s.circuito || '').trim().toLowerCase() !== c) return false;
      if (v && (s.vehiculo || '').trim().toLowerCase() !== v) return false;
      if (cat !== null && s.categoriaId !== cat) return false;
      if (!q) return true;
      return [s.titulo, s.vehiculo, s.circuito, s.descripcion, s.categoriaNombre].some((t) =>
        (t || '').toLowerCase().includes(q),
      );
    });
    return list.sort((a, b) => {
      if (order === 'recientes') {
        return new Date(b.fechaPublicacion).getTime() - new Date(a.fechaPublicacion).getTime();
      }
      return (b.promedioCalificacion || 0) - (a.promedioCalificacion || 0);
    });
  });

  readonly mensajeConfirmacion = computed(() => {
    const c = this.aConfirmar();
    if (!c) return '';
    return c.tipo === 'setup'
      ? `¿Eliminar "${c.titulo}" con su archivo, calificaciones y comentarios?`
      : '¿Eliminar este comentario?';
  });

  ngOnInit(): void {
    // El listado y el detalle son públicos (SecurityConfig): los visitantes
    // pueden mirar; descargar, calificar y comentar piden sesión.
    forkJoin({
      setups: this.api.list<Setup>('/setups').pipe(this.carga.siFalla([] as Setup[])),
      cats: this.api.list<Categoria>('/categorias').pipe(this.carga.siFalla([] as Categoria[])),
    }).subscribe((r) => {
      this.cats.set(r.cats);
      this.setups.set(r.setups);
      this.cargando.set(false);
      const q = this.route.snapshot.queryParamMap;
      const cat = Number(q.get('cat'));
      if (cat) this.filterCategoria.set(cat);
      const pedido = Number(q.get('id'));
      const inicial = r.setups.some((s) => s.id === pedido) ? pedido : this.filtered()[0]?.id;
      if (inicial) this.select(inicial, false);
    });
  }

  // ---- helpers de template ----

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

  esSetupFijo(s: Setup): boolean {
    return this.catsById().get(s.categoriaId ?? -1)?.setupFijo === true;
  }

  esMio(s: Setup): boolean {
    return s.autorId === this.miId();
  }

  puedeEditar(s: Setup): boolean {
    return this.esAdmin() || this.esMio(s);
  }

  puedeBorrarComentario(c: SetupComentario): boolean {
    return this.esAdmin() || c.usuarioId === this.miId();
  }

  promedio(s: Setup): string {
    return s.promedioCalificacion != null ? s.promedioCalificacion.toFixed(1) : '—';
  }

  setupLabel(c: Categoria | null): string | null {
    if (!c) return null;
    if (c.setupAbierto) return 'Setup abierto: cualquier piloto puede compartir el suyo.';
    if (c.setupFijo) return 'Setup fijo: todos corren con el setup oficial de la liga.';
    return 'Setup libre: cada piloto arma el suyo.';
  }

  private unicos(valores: (string | undefined)[]): string[] {
    // Agrupa "Monza" y "monza " en una sola opción, conservando la primera grafía.
    const porClave = new Map<string, string>();
    for (const v of valores) {
      const limpio = (v || '').trim();
      if (limpio && !porClave.has(limpio.toLowerCase())) porClave.set(limpio.toLowerCase(), limpio);
    }
    return [...porClave.values()].sort((a, b) => a.localeCompare(b));
  }

  // ---- selección y detalle ----

  select(id: number, actualizarUrl = true): void {
    this.selectedId.set(id);
    this.editingId.set(null);
    if (actualizarUrl) {
      this.router.navigate([], {
        relativeTo: this.route,
        queryParams: { id },
        queryParamsHandling: 'merge',
        replaceUrl: true,
      });
    }
    this.loadDetail(id);
  }

  private loadDetail(id: number): void {
    this.detailCargando.set(true);
    this.comentarios.set([]);
    this.calificaciones.set([]);
    this.api.get<Setup>('/setups/' + id).subscribe({
      next: (s) => {
        this.setups.update((l) => l.map((x) => (x.id === id ? s : x)));
        this.detailCargando.set(false);
      },
      error: (err) => {
        this.detailCargando.set(false);
        this.toast.error(apiError(err));
      },
    });
    // Comentarios y calificaciones exigen sesión: sin login ni se piden.
    if (this.autenticado()) {
      this.cargarComentarios(id);
      this.cargarCalificaciones(id);
    }
  }

  private cargarComentarios(id: number): void {
    this.api
      .list<SetupComentario>('/setups/' + id + '/comentarios')
      .pipe(this.carga.siFalla([] as SetupComentario[]))
      .subscribe((list) => {
        if (this.selectedId() === id) this.comentarios.set(list);
      });
  }

  private cargarCalificaciones(id: number): void {
    this.api
      .list<SetupCalificacion>('/setups/' + id + '/calificaciones')
      .pipe(this.carga.siFalla([] as SetupCalificacion[]))
      .subscribe((list) => {
        if (this.selectedId() === id) this.calificaciones.set(list);
      });
  }

  private reloadSetup(id: number): void {
    this.api.get<Setup>('/setups/' + id).subscribe({
      next: (s) => this.setups.update((l) => l.map((x) => (x.id === id ? s : x))),
      error: () => undefined,
    });
  }

  // ---- descarga ----

  descargar(s: Setup): void {
    if (!this.autenticado()) return;
    this.descargando.set(true);
    this.api.blob('/setups/' + s.id + '/descargar').subscribe({
      next: (blob) => {
        this.descargando.set(false);
        const punto = (s.archivo || '').lastIndexOf('.');
        const ext = punto >= 0 ? s.archivo!.slice(punto) : '';
        const nombre = (s.titulo || 'setup').replace(/[\\/:*?"<>|]+/g, '_') + ext;
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = nombre;
        document.body.appendChild(a);
        a.click();
        a.remove();
        setTimeout(() => URL.revokeObjectURL(url), 1000);
      },
      error: (err) => {
        this.descargando.set(false);
        this.toast.error(
          err?.status === 404
            ? 'El archivo de este setup ya no está en el servidor. Pedile al autor que lo vuelva a subir.'
            : apiError(err),
        );
      },
    });
  }

  // ---- calificación ----

  calificar(s: Setup, puntaje: number): void {
    if (!this.autenticado() || this.esMio(s)) return;
    this.api.post('/setups/' + s.id + '/calificaciones', { puntaje }).subscribe({
      next: () => {
        this.toast.success('Calificación guardada');
        this.reloadSetup(s.id);
        this.cargarCalificaciones(s.id);
      },
      error: (err) => this.toast.error(apiError(err)),
    });
  }

  // ---- archivo: selección, arrastre y validación ----

  private validarArchivo(f: File | null): string | null {
    if (!f) return null;
    const punto = f.name.lastIndexOf('.');
    const ext = punto >= 0 ? f.name.slice(punto).toLowerCase() : '';
    if (!EXTENSIONES.includes(ext))
      return `Formato no permitido. Aceptados: ${this.extensionesTxt}.`;
    if (f.size > MAX_BYTES) return 'El archivo supera los 10 MB.';
    return null;
  }

  onFileSelect(e: Event): void {
    this.archivo.set((e.target as HTMLInputElement).files?.[0] ?? null);
  }

  onDragOver(e: DragEvent): void {
    e.preventDefault();
    this.arrastrando.set(true);
  }

  onDragLeave(): void {
    this.arrastrando.set(false);
  }

  onDrop(e: DragEvent): void {
    e.preventDefault();
    this.arrastrando.set(false);
    const f = e.dataTransfer?.files?.[0];
    if (f) this.archivo.set(f);
  }

  /** Sube o reemplaza el archivo de un setup existente (autor o admin). */
  subirArchivo(s: Setup, e: Event): void {
    const input = e.target as HTMLInputElement;
    const f = input.files?.[0] ?? null;
    input.value = '';
    const error = this.validarArchivo(f);
    if (!f || error) {
      if (error) this.toast.error(error);
      return;
    }
    this.subiendoArchivo.set(true);
    const fd = new FormData();
    fd.append('archivo', f);
    this.api.post('/setups/' + s.id + '/archivo', fd).subscribe({
      next: () => {
        this.subiendoArchivo.set(false);
        this.toast.success(s.archivo ? 'Archivo reemplazado' : 'Archivo subido');
        this.reloadSetup(s.id);
      },
      error: (err) => {
        this.subiendoArchivo.set(false);
        this.toast.error(apiError(err));
      },
    });
  }

  // ---- alta ----

  abrirUpload(): void {
    this.intentoPublicar.set(false);
    this.uploadVisible.set(true);
  }

  cerrarUpload(): void {
    this.uploadVisible.set(false);
  }

  publicar(): void {
    this.intentoPublicar.set(true);
    if (!this.auth.user()) return;
    const faltan = this.faltanCampos();
    if (faltan.titulo || faltan.circuito || faltan.vehiculo) return;
    if (this.errorArchivo()) return;
    if (!this.puedePublicar()) return;
    this.publicando.set(true);
    this.api
      .post<Setup>('/setups', {
        titulo: this.fTitulo().trim(),
        descripcion: this.fDescripcion().trim(),
        circuito: this.fCircuito().trim(),
        vehiculo: this.fVehiculo().trim(),
        categoriaId: this.fCategoria(),
      })
      .subscribe({
        next: (nuevo) => {
          this.setups.update((l) => [nuevo, ...l]);
          const archivo = this.archivo();
          if (!archivo) {
            this.terminarAlta(nuevo, 'Setup publicado');
            return;
          }
          const fd = new FormData();
          fd.append('archivo', archivo);
          this.api.post('/setups/' + nuevo.id + '/archivo', fd).subscribe({
            next: () => {
              this.reloadSetup(nuevo.id);
              this.terminarAlta(nuevo, 'Setup publicado');
            },
            // El setup ya existe: no se pierde, y el autor ve "Subir archivo" en el detalle.
            error: (err) =>
              this.terminarAlta(
                nuevo,
                null,
                'El setup se publicó pero el archivo no se pudo subir: ' +
                  apiError(err) +
                  ' Podés subirlo desde el detalle.',
              ),
          });
        },
        error: (err) => {
          this.publicando.set(false);
          this.toast.error(apiError(err));
        },
      });
  }

  private terminarAlta(nuevo: Setup, ok: string | null, error?: string): void {
    this.publicando.set(false);
    this.uploadVisible.set(false);
    this.fTitulo.set('');
    this.fDescripcion.set('');
    this.fCircuito.set('');
    this.fVehiculo.set('');
    this.fCategoria.set(null);
    this.archivo.set(null);
    this.intentoPublicar.set(false);
    this.select(nuevo.id);
    if (ok) this.toast.success(ok);
    if (error) this.toast.error(error);
  }

  // ---- comentarios ----

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

  // ---- borrado (con ConfirmDialog en vez de confirm()) ----

  pedirBorrarSetup(e: Event, s: Setup): void {
    e.stopPropagation();
    this.aConfirmar.set({ tipo: 'setup', id: s.id, titulo: s.titulo });
  }

  pedirBorrarComentario(setupId: number, c: SetupComentario): void {
    this.aConfirmar.set({ tipo: 'comentario', setupId, id: c.id });
  }

  confirmarBorrado(): void {
    const c = this.aConfirmar();
    this.aConfirmar.set(null);
    if (!c) return;
    if (c.tipo === 'setup') {
      this.api.del('/setups/' + c.id).subscribe({
        next: () => {
          this.toast.success('Setup eliminado');
          this.setups.update((l) => l.filter((s) => s.id !== c.id));
          if (this.selectedId() === c.id) {
            const siguiente = this.filtered()[0]?.id;
            if (siguiente) this.select(siguiente);
            else this.selectedId.set(null);
          }
        },
        error: (err) => this.toast.error(apiError(err)),
      });
    } else {
      this.api.del(`/setups/${c.setupId}/comentarios/${c.id}`).subscribe({
        next: () => {
          this.toast.success('Comentario eliminado');
          this.comentarios.update((l) => l.filter((x) => x.id !== c.id));
        },
        error: (err) => this.toast.error(apiError(err)),
      });
    }
  }

  // ---- edición ----

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
    this.api
      .put('/setups/' + id, {
        titulo,
        descripcion: this.eDescripcion().trim(),
        circuito,
        vehiculo,
        categoriaId: this.eCategoria(),
      })
      .subscribe({
        next: () => {
          this.toast.success('Setup actualizado');
          this.editingId.set(null);
          this.reloadSetup(id);
        },
        error: (err) => this.toast.error(apiError(err)),
      });
  }

  setFiltroCategoria(e: Event): void {
    const v = this.selectNum(e);
    this.filterCategoria.set(v);
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { cat: v },
      queryParamsHandling: 'merge',
      replaceUrl: true,
    });
  }
}

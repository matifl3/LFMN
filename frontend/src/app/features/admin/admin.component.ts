import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ApiService, apiError } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';
import { AuthService } from '../../core/services/auth.service';
import { Anuncio, Campeonato, Carrera, Categoria, Estadisticas, Usuario } from '../../core/models/models';
import { Chip } from '../../shared/components/chip/chip';
import { Avatar } from '../../shared/components/avatar/avatar';
import { FmtFechaHoraPipe } from '../../core/pipes/fmt-fecha.pipe';

const ESTADO_LABEL: Record<string, string> = {
  PROGRAMADA: 'Programada',
  INSCRIPCIONES_ABIERTAS: 'Inscripciones abiertas',
  INSCRIPCIONES_CERRADAS: 'Inscripciones cerradas',
  EN_CURSO: 'En curso',
  FINALIZADA: 'Finalizada',
  CANCELADA: 'Cancelada',
};

type Tab = 'estadisticas' | 'carreras' | 'campeonatos' | 'categorias' | 'pilotos' | 'anuncios';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [FormsModule, RouterLink, Chip, Avatar, FmtFechaHoraPipe],
  styleUrl: './admin.component.scss',
  templateUrl: './admin.component.html',
})
export class AdminComponent implements OnInit {
  readonly auth = inject(AuthService);
  private readonly api = inject(ApiService);
  private readonly toast = inject(ToastService);

  readonly tab = signal<Tab>('carreras');
  readonly isAdmin = computed(() => this.auth.esAdmin());
  readonly isComisario = computed(() => this.auth.esComisario());
  readonly cargando = signal(true);

  readonly carreras = signal<Carrera[]>([]);
  readonly campeonatos = signal<Campeonato[]>([]);
  readonly categorias = signal<Categoria[]>([]);
  readonly usuarios = signal<Usuario[]>([]);
  readonly anuncios = signal<Anuncio[]>([]);
  readonly stats = signal<Estadisticas | null>(null);

  readonly carrerasCats = signal<Categoria[]>([]);
  readonly campeonatosForm = signal<Campeonato[]>([]);

  readonly ESTADO_LABEL = ESTADO_LABEL;
  readonly objectKeys = Object.keys;

  readonly editingCarreraId = signal<number | null>(null);
  readonly editingCampeonatoId = signal<number | null>(null);
  readonly editingCategoriaId = signal<number | null>(null);
  readonly editingAnuncioId = signal<number | null>(null);

  readonly cNombre = signal('');
  readonly cFecha = signal('');
  readonly cHora = signal('');
  readonly cCircuito = signal('');
  readonly cCategoriaId = signal('');
  readonly cCampeonatoId = signal('');
  readonly cCupo = signal(32);
  readonly cServidor = signal('');
  readonly cLinkPista = signal('');
  readonly cLinkAuto = signal('');

  readonly chNombre = signal('');
  readonly chTemporada = signal('');
  readonly chCategoriaId = signal('');
  readonly chSistemaPuntos = signal('default');

  readonly catNombre = signal('');
  readonly catDescripcion = signal('');
  readonly catEloMinimo = signal<number | null>(null);
  readonly catEloMaximo = signal<number | null>(null);
  readonly catSetup = signal('ABIERTO');

  readonly aTitulo = signal('');
  readonly aContenido = signal('');
  readonly aImagenFile = signal<File | null>(null);
  readonly aImagenUrl = signal('');

  ngOnInit(): void {
    this.cargar();
  }

  private cargar(): void {
    forkJoin({
      carreras: this.api.list<Carrera>('/carreras').pipe(catchError(() => of([]))),
      campeonatos: this.api.list<Campeonato>('/campeonatos').pipe(catchError(() => of([]))),
      categorias: this.api.list<Categoria>('/categorias').pipe(catchError(() => of([]))),
      usuarios: this.api.list<Usuario>('/usuarios').pipe(catchError(() => of([]))),
      anuncios: this.api.list<Anuncio>('/anuncios').pipe(catchError(() => of([]))),
      stats: this.api.get<Estadisticas>('/estadisticas').pipe(catchError(() => of(null))),
    }).subscribe({
      next: (r) => {
        this.carreras.set(r.carreras);
        this.campeonatos.set(r.campeonatos);
        this.categorias.set(r.categorias);
        this.usuarios.set(r.usuarios);
        this.anuncios.set(r.anuncios);
        this.stats.set(r.stats);
        this.carrerasCats.set(r.categorias);
        this.cargando.set(false);
        this.cargarSelectoresCategorias();
      },
      error: (err) => {
        this.cargando.set(false);
        this.toast.error(apiError(err));
      },
    });
  }

  private cargarSelectoresCategorias(): void {
    this.api.list<Categoria>('/categorias').subscribe({
      next: (cats) => this.carrerasCats.set(cats),
      error: () => this.carrerasCats.set([]),
    });
  }

  setTab(t: Tab): void {
    this.tab.set(t);
  }

  toNumber(v: unknown, fallback: number | null = null): number | null {
    return v === '' || v === null || v === undefined ? fallback : Number(v);
  }

  onCategoriaChange(categoriaId: string): void {
    this.cCategoriaId.set(categoriaId);
    this.cCampeonatoId.set('');
    if (categoriaId) {
      this.api.list<Campeonato>('/campeonatos/categoria/' + categoriaId).subscribe({
        next: (camps) => this.campeonatosForm.set(camps),
        error: () => this.campeonatosForm.set([]),
      });
    } else {
      this.campeonatosForm.set([]);
    }
  }

  crearOActualizarCarrera(): void {
    const nombre = this.cNombre()?.trim();
    const categoriaId = Number(this.cCategoriaId());
    if (!nombre || !categoriaId) {
      this.toast.error('Nombre y categoría son obligatorios.');
      return;
    }
    const fecha = this.cFecha() && this.cHora() ? this.cFecha() + 'T' + this.cHora() + ':00' : undefined;
    const body: Record<string, unknown> = {
      nombre,
      fecha,
      circuito: this.cCircuito()?.trim() || undefined,
      campeonatoId: this.cCampeonatoId() ? Number(this.cCampeonatoId()) : undefined,
      cupoMaximo: this.cCupo() || 32,
      servidor: this.cServidor()?.trim() || undefined,
      linkPista: this.cLinkPista()?.trim() || undefined,
      linkAuto: this.cLinkAuto()?.trim() || undefined,
    };
    const editing = this.editingCarreraId();
    const req = editing ? this.api.put('/carreras/' + editing, body) : this.api.post('/carreras', body);
    req.subscribe({
      next: () => {
        this.toast.success(editing ? 'Carrera actualizada.' : 'Carrera creada.');
        this.limpiarFormCarrera();
        this.cargar();
      },
      error: (err) => this.toast.error(apiError(err)),
    });
  }

  editarCarrera(c: Carrera): void {
    this.editingCarreraId.set(c.id);
    this.cNombre.set(c.nombre);
    this.cCircuito.set(c.circuito || '');
    this.cCupo.set(c.cupoMaximo || 32);
    this.cServidor.set(c.servidor || '');
    this.cLinkPista.set(c.linkPista || '');
    this.cLinkAuto.set(c.linkAuto || '');
    if (c.fecha) {
      const d = new Date(c.fecha);
      this.cFecha.set(d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0'));
      this.cHora.set(String(d.getHours()).padStart(2, '0') + ':' + String(d.getMinutes()).padStart(2, '0'));
    }
    this.cCategoriaId.set(String(c.categoriaId));
    this.campeonatosForm.set([]);
    this.cCampeonatoId.set('');
    if (c.categoriaId) {
      this.api.list<Campeonato>('/campeonatos/categoria/' + c.categoriaId).subscribe({
        next: (camps) => {
          this.campeonatosForm.set(camps);
          if (c.campeonatoId) this.cCampeonatoId.set(String(c.campeonatoId));
        },
        error: () => this.campeonatosForm.set([]),
      });
    }
  }

  limpiarFormCarrera(): void {
    this.editingCarreraId.set(null);
    this.cNombre.set('');
    this.cFecha.set('');
    this.cHora.set('');
    this.cCircuito.set('');
    this.cCategoriaId.set('');
    this.cCampeonatoId.set('');
    this.cCupo.set(32);
    this.cServidor.set('');
    this.cLinkPista.set('');
    this.cLinkAuto.set('');
    this.campeonatosForm.set([]);
  }

  cambiarEstadoCarrera(id: number, estado: string): void {
    this.api.put('/carreras/' + id + '/estado?estado=' + encodeURIComponent(estado)).subscribe({
      next: () => {
        this.toast.success('Estado actualizado.');
        this.cargar();
      },
      error: (err) => this.toast.error(apiError(err)),
    });
  }

  eliminarCarrera(id: number): void {
    if (!confirm('¿Eliminar esta carrera?')) return;
    this.api.del('/carreras/' + id).subscribe({
      next: () => {
        this.toast.success('Carrera eliminada.');
        this.cargar();
      },
      error: (err) => this.toast.error(apiError(err)),
    });
  }

  crearOActualizarCampeonato(): void {
    const nombre = this.chNombre()?.trim();
    const categoriaId = Number(this.chCategoriaId());
    if (!nombre || !categoriaId) {
      this.toast.error('Nombre y categoría son obligatorios.');
      return;
    }
    const body: Record<string, unknown> = {
      nombre,
      temporada: this.chTemporada()?.trim() || undefined,
      categoriaId,
      sistemaPuntos: this.chSistemaPuntos() || undefined,
    };
    const editing = this.editingCampeonatoId();
    const req = editing ? this.api.put('/campeonatos/' + editing, body) : this.api.post('/campeonatos', body);
    req.subscribe({
      next: () => {
        this.toast.success(editing ? 'Campeonato actualizado.' : 'Campeonato creado.');
        this.limpiarFormCampeonato();
        this.cargar();
      },
      error: (err) => this.toast.error(apiError(err)),
    });
  }

  editarCampeonato(c: Campeonato): void {
    this.editingCampeonatoId.set(c.id);
    this.chNombre.set(c.nombre);
    this.chTemporada.set(c.temporada || '');
    this.chCategoriaId.set(String(c.categoriaId));
    this.chSistemaPuntos.set(c.sistemaPuntos || 'default');
  }

  limpiarFormCampeonato(): void {
    this.editingCampeonatoId.set(null);
    this.chNombre.set('');
    this.chTemporada.set('');
    this.chCategoriaId.set('');
    this.chSistemaPuntos.set('default');
  }

  toggleCerrarCampeonato(c: Campeonato): void {
    this.api.put('/campeonatos/' + c.id + '/cerrar').subscribe({
      next: () => {
        this.toast.success('Campeonato actualizado.');
        this.cargar();
      },
      error: (err) => this.toast.error(apiError(err)),
    });
  }

  eliminarCampeonato(id: number): void {
    if (!confirm('¿Eliminar este campeonato?')) return;
    this.api.del('/campeonatos/' + id).subscribe({
      next: () => {
        this.toast.success('Campeonato eliminado.');
        this.cargar();
      },
      error: (err) => this.toast.error(apiError(err)),
    });
  }

  crearOActualizarCategoria(): void {
    const nombre = this.catNombre()?.trim();
    if (!nombre) {
      this.toast.error('El nombre es obligatorio.');
      return;
    }
    const setup = this.catSetup();
    const body: Record<string, unknown> = {
      nombre,
      descripcion: this.catDescripcion()?.trim() || undefined,
      eloMinimo: this.catEloMinimo(),
      eloMaximo: this.catEloMaximo(),
      setupAbierto: setup === 'ABIERTO',
      setupFijo: setup === 'FIJO',
    };
    const editing = this.editingCategoriaId();
    const req = editing ? this.api.put('/categorias/' + editing, body) : this.api.post('/categorias', body);
    req.subscribe({
      next: () => {
        this.toast.success(editing ? 'Categoría actualizada.' : 'Categoría creada.');
        this.limpiarFormCategoria();
        this.cargar();
      },
      error: (err) => this.toast.error(apiError(err)),
    });
  }

  editarCategoria(c: Categoria): void {
    this.editingCategoriaId.set(c.id);
    this.catNombre.set(c.nombre);
    this.catDescripcion.set(c.descripcion || '');
    this.catEloMinimo.set(c.eloMinimo ?? null);
    this.catEloMaximo.set(c.eloMaximo ?? null);
    if (c.setupAbierto) this.catSetup.set('ABIERTO');
    else if (c.setupFijo) this.catSetup.set('FIJO');
    else this.catSetup.set('LIBRE');
  }

  limpiarFormCategoria(): void {
    this.editingCategoriaId.set(null);
    this.catNombre.set('');
    this.catDescripcion.set('');
    this.catEloMinimo.set(null);
    this.catEloMaximo.set(null);
    this.catSetup.set('ABIERTO');
  }

  eliminarCategoria(id: number): void {
    if (!confirm('¿Eliminar esta categoría?')) return;
    this.api.del('/categorias/' + id).subscribe({
      next: () => {
        this.toast.success('Categoría eliminada.');
        this.cargar();
      },
      error: (err) => this.toast.error(apiError(err)),
    });
  }

  updateRating(u: Usuario, campo: 'elo' | 'safetyRating', event: Event): void {
    const input = event.target as HTMLInputElement;
    const valor = Number(input.value);
    this.api.put('/usuarios/' + u.id + '/rating', { [campo]: valor }).subscribe({
      next: () => {
        this.toast.success(campo === 'elo' ? 'Elo actualizado.' : 'Safety Rating actualizado.');
        this.cargar();
      },
      error: (err) => this.toast.error(apiError(err)),
    });
  }

  updateRol(u: Usuario, rol: string): void {
    this.api.put('/usuarios/' + u.id + '/rol?rol=' + encodeURIComponent(rol)).subscribe({
      next: () => {
        this.toast.success('Rol actualizado.');
        this.cargar();
      },
      error: (err) => this.toast.error(apiError(err)),
    });
  }

  eliminarUsuario(u: Usuario): void {
    if (u.id === this.auth.user()?.id) {
      this.toast.error('No podés eliminarte a vos mismo.');
      return;
    }
    if (!confirm('¿Eliminar a ' + u.nombrePiloto + '?')) return;
    this.api.del('/usuarios/' + u.id).subscribe({
      next: () => {
        this.toast.success('Usuario eliminado.');
        this.cargar();
      },
      error: (err) => this.toast.error(apiError(err)),
    });
  }

  toggleDestacado(a: Anuncio): void {
    this.api.patch('/anuncios/' + a.id + '/destacar').subscribe({
      next: () => {
        this.toast.success('Anuncio actualizado.');
        this.cargar();
      },
      error: (err) => this.toast.error(apiError(err)),
    });
  }

  onAnuncioFile(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.aImagenFile.set(input.files?.[0] || null);
  }

  crearAnuncio(): void {
    const titulo = this.aTitulo()?.trim();
    if (!titulo) {
      this.toast.error('El título es obligatorio.');
      return;
    }
    const file = this.aImagenFile();
    if (file) {
      const fd = new FormData();
      fd.append('file', file);
      this.api.post<{ url: string }>('/imagenes', fd).subscribe({
        next: (res) => this.publicarAnuncio(res?.url || ''),
        error: (err) => this.toast.error(apiError(err)),
      });
    } else {
      this.publicarAnuncio(this.aImagenUrl() || '');
    }
  }

  private publicarAnuncio(urlImagen: string): void {
    const body: Record<string, unknown> = {
      titulo: this.aTitulo(),
      contenido: this.aContenido()?.trim() || undefined,
    };
    if (urlImagen) body['urlImagen'] = urlImagen;
    const editing = this.editingAnuncioId();
    const req = editing ? this.api.put('/anuncios/' + editing, body) : this.api.post('/anuncios', body);
    req.subscribe({
      next: () => {
        this.toast.success(editing ? 'Anuncio actualizado.' : 'Anuncio creado.');
        this.limpiarFormAnuncio();
        this.cargar();
      },
      error: (err) => this.toast.error(apiError(err)),
    });
  }

  editarAnuncio(a: Anuncio): void {
    this.editingAnuncioId.set(a.id);
    this.aTitulo.set(a.titulo);
    this.aContenido.set(a.contenido || '');
    this.aImagenUrl.set(a.urlImagen || '');
    this.aImagenFile.set(null);
  }

  limpiarFormAnuncio(): void {
    this.editingAnuncioId.set(null);
    this.aTitulo.set('');
    this.aContenido.set('');
    this.aImagenFile.set(null);
    this.aImagenUrl.set('');
  }

  eliminarAnuncio(id: number): void {
    if (!confirm('¿Eliminar este anuncio?')) return;
    this.api.del('/anuncios/' + id).subscribe({
      next: () => {
        this.toast.success('Anuncio eliminado.');
        this.cargar();
      },
      error: (err) => this.toast.error(apiError(err)),
    });
  }
}
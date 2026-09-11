import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from '../../../core/services/auth.service';
import { ApiService, apiError } from '../../../core/services/api.service';
import { ToastService } from '../../../core/services/toast.service';
import { Avatar } from '../../../shared/components/avatar/avatar';
import { Chip } from '../../../shared/components/chip/chip';
import { RankBadge } from '../../../shared/components/rank-badge/rank-badge';
import { FmtFechaHoraPipe } from '../../../core/pipes/fmt-fecha.pipe';
import { FmtLapPipe } from '../../../core/pipes/fmt-lap.pipe';
import { Carrera, Inscripcion, ResultadoCarrera, SesionClasificacion, Vuelta } from '../../../core/models/models';

const ESTADOS_INSCRIPCION = ['PROGRAMADA', 'INSCRIPCIONES_ABIERTAS'] as const;

@Component({
  selector: 'app-race-detail',
  standalone: true,
  imports: [RouterLink, Chip, Avatar, RankBadge, FmtFechaHoraPipe, FmtLapPipe],
  templateUrl: './race-detail.html',
})
export class RaceDetailComponent implements OnInit {
  readonly auth = inject(AuthService);
  private readonly api = inject(ApiService);
  private readonly toast = inject(ToastService);
  private readonly route = inject(ActivatedRoute);

  readonly carrera = signal<Carrera | null>(null);
  readonly inscriptos = signal<Inscripcion[]>([]);
  readonly resultados = signal<ResultadoCarrera[]>([]);
  readonly clasificaciones = signal<SesionClasificacion[]>([]);
  readonly vueltas = signal<Vuelta[]>([]);
  readonly cargando = signal(true);
  readonly panel = signal<'info' | 'archivos' | 'inscriptos' | 'resultados' | 'clasificacion' | 'analisis'>('info');
  readonly inscribiendo = signal(false);

  private id: string | null = null;

  readonly inscriptosActivos = computed(() => this.inscriptos().filter((i) => i.estado !== 'CANCELADA').length);

  readonly cupoLleno = computed(() => {
    const c = this.carrera();
    return !!c && c.cupoMaximo !== undefined && c.cupoMaximo > 0 && this.inscriptosActivos() >= c.cupoMaximo;
  });

  readonly cupoPct = computed(() => {
    const c = this.carrera();
    return c?.cupoMaximo ? Math.round((this.inscriptosActivos() / c.cupoMaximo) * 100) : 0;
  });

  readonly yaInscripto = computed(() => {
    const u = this.auth.user();
    if (!u) return false;
    return this.inscriptos().some((i) => i.usuarioId === u.id && i.estado !== 'CANCELADA');
  });

  readonly botonDisabled = computed(() => {
    if (!this.auth.user()) return true;
    if (this.inscribiendo()) return true;
    const c = this.carrera();
    return !c || !ESTADOS_INSCRIPCION.includes(c.estado as (typeof ESTADOS_INSCRIPCION)[number]);
  });

  readonly botonLabel = computed(() => {
    if (!this.auth.user()) return 'Ingresar para inscribirme';
    const c = this.carrera();
    if (c && !ESTADOS_INSCRIPCION.includes(c.estado as (typeof ESTADOS_INSCRIPCION)[number])) return 'Inscripción cerrada';
    return this.yaInscripto() ? 'Darme de baja' : 'Inscribirme';
  });

  readonly resultadosSorted = computed(() =>
    [...this.resultados()].sort((a, b) => (a.posicionFinal ?? 999) - (b.posicionFinal ?? 999))
  );

  readonly clasificacionesSorted = computed(() =>
    [...this.clasificaciones()].sort((a, b) => a.tiempo - b.tiempo)
  );

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.cargando.set(false);
      return;
    }
    this.id = id;
    const user = this.auth.user();
    forkJoin({
      carrera: this.api.get<Carrera>('/carreras/' + id).pipe(catchError(() => of(null))),
      inscriptos: this.api.list<Inscripcion>('/inscripciones/carrera/' + id).pipe(catchError(() => of([]))),
      resultados: this.api.list<ResultadoCarrera>('/carreras/' + id + '/resultados').pipe(catchError(() => of([]))),
      clasificaciones: this.api.list<SesionClasificacion>('/carreras/' + id + '/clasificacion').pipe(catchError(() => of([]))),
      vueltas: user
        ? this.api.list<Vuelta>('/vueltas/carrera/' + id + '/usuario/' + user.id).pipe(catchError(() => of([])))
        : of([]),
    }).subscribe({
      next: (r) => {
        this.carrera.set(r.carrera);
        this.inscriptos.set(r.inscriptos);
        this.resultados.set(r.resultados);
        this.clasificaciones.set(r.clasificaciones);
        this.vueltas.set(r.vueltas);
        this.cargando.set(false);
      },
      error: () => this.cargando.set(false),
    });
  }

  toggleInscripcion(): void {
    if (this.botonDisabled() || !this.id) return;
    const user = this.auth.user();
    if (!user) return;
    this.inscribiendo.set(true);
    const darDeBaja = this.yaInscripto();
    (darDeBaja
      ? this.api.del('/inscripciones/carrera/' + this.id + '/usuario/' + user.id)
      : this.api.post('/inscripciones', { carreraId: Number(this.id) })
    ).subscribe({
      next: () => {
        this.inscribiendo.set(false);
        this.toast.success(darDeBaja ? 'Te diste de baja de la carrera.' : '¡Inscripción confirmada!');
        this.recargarInscriptos();
      },
      error: (err) => {
        this.inscribiendo.set(false);
        this.toast.error(apiError(err));
      },
    });
  }

  private recargarInscriptos(): void {
    if (!this.id) return;
    this.api.list<Inscripcion>('/inscripciones/carrera/' + this.id).subscribe({
      next: (l) => this.inscriptos.set(l),
      error: () => this.toast.error('No se pudo actualizar la lista de inscriptos.'),
    });
  }
}
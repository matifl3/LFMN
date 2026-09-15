import { Component, inject, signal, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ApiService, apiError } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';
import { Modal } from '../../shared/components/modal/modal';
import { RankBadge } from '../../shared/components/rank-badge/rank-badge';
import { Categoria, Campeonato, Carrera, TablaPosicion, Anuncio, UsuarioBasico } from '../../core/models/models';
import { fmtFecha, fmtHora } from '../../core/utils/formato';
import { FechaRelativaPipe } from '../../core/pipes/fecha-relativa.pipe';
import { AssetUrlPipe } from '../../core/pipes/asset-url.pipe';

const CTA_ESTADOS = ['PROGRAMADA', 'INSCRIPCIONES_ABIERTAS', 'INSCRIPCIONES_CERRADAS', 'EN_CURSO'];

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink, Modal, RankBadge, FechaRelativaPipe, AssetUrlPipe],
  styleUrl: './home.scss',
  templateUrl: './home.html',
})
export class HomeComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly toast = inject(ToastService);

  readonly stats = signal({ pilotos: 0, carreras: 0, categorias: 0 });
  readonly proximas = signal<Carrera[]>([]);
  readonly cargando = signal(true);
  readonly campeonato = signal<Campeonato | null>(null);
  readonly top3 = signal<TablaPosicion[]>([]);
  readonly anuncio = signal<Anuncio | null>(null);
  readonly ctaAbierto = signal(false);
  readonly anuncioAbierto = signal(false);

  ngOnInit(): void {
    this.cargar();
  }

  private cargar(): void {
    forkJoin({
      usuarios: this.api.list<UsuarioBasico>('/usuarios/basico').pipe(catchError(() => of([]))),
      pasadas: this.api.list<Carrera>('/carreras/pasadas').pipe(catchError(() => of([]))),
      categorias: this.api.list<Categoria>('/categorias').pipe(catchError(() => of([]))),
      proximas: this.api.list<Carrera>('/carreras/proximas').pipe(catchError(() => of([]))),
      campeonatos: this.api.list<Campeonato>('/campeonatos').pipe(catchError(() => of([]))),
      anuncio: this.api.get<Anuncio>('/anuncios/ultimo').pipe(catchError(() => of(null))),
    }).subscribe({
      next: (r) => {
        this.stats.set({ pilotos: r.usuarios.length, carreras: r.pasadas.length, categorias: r.categorias.length });
        this.proximas.set(r.proximas);
        const champ = r.campeonatos[0];
        if (champ) {
          this.campeonato.set(champ);
          this.api.get<TablaPosicion[]>('/campeonatos/' + champ.id + '/tabla').subscribe({
            next: (tabla) => this.top3.set(tabla.slice(0, 3)),
            error: () => this.toast.error('Error al cargar posiciones.'),
          });
        }
        this.anuncio.set(r.anuncio);
        this.cargando.set(false);
      },
      error: (err) => {
        this.cargando.set(false);
        this.toast.error(apiError(err));
      },
    });
  }

  ctaItems(): Carrera[] {
    return this.proximas().filter((c) => CTA_ESTADOS.includes(c.estado));
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

  alternarCta(): void {
    if (this.ctaItems().length === 0) return;
    this.ctaAbierto.update((v) => !v);
  }
}
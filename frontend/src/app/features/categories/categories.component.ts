import { TranslocoPipe } from '@jsverse/transloco';
import { t } from '../../core/i18n/t';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { NgTemplateOutlet } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { ErrorCargaService } from '../../core/services/error-carga.service';
import { EmptyState } from '../../shared/components/empty-state/empty-state';
import { Campeonato, Carrera, Categoria, Usuario } from '../../core/models/models';
import { fmtFechaHora } from '../../core/utils/formato';

/** Dónde queda el Elo del piloto respecto del rango de la categoría. */
type Situacion = 'puede' | 'falta' | 'excede';

interface Tarjeta {
  cat: Categoria;
  sigla: string;
  tipo: 'formula' | 'turismo' | 'abierta';
  rangoTxt: string;
  setup: { label: string; ayuda: string };
  situacion: Situacion | null;
  /** Elo que le falta (si no llega) o que le sobra (si se pasó). */
  diferencia: number;
  /** Posición del Elo del piloto dentro del rango, 0..100, para la barra. */
  posicionPct: number | null;
  proximas: Carrera[];
  campeonato: Campeonato | null;
}

@Component({
  selector: 'app-categories',
  standalone: true,
  imports: [TranslocoPipe, RouterLink, EmptyState, NgTemplateOutlet],
  styleUrl: './categories.scss',
  templateUrl: './categories.html',
})
export class CategoriesComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly carga = inject(ErrorCargaService);
  readonly auth = inject(AuthService);

  readonly cats = signal<Categoria[]>([]);
  private readonly proximas = signal<Carrera[]>([]);
  private readonly champs = signal<Campeonato[]>([]);
  /** Elo actualizado de /usuarios/me: el de la sesión guardada queda viejo tras cada carrera. */
  private readonly eloFresco = signal<number | null>(null);
  readonly cargando = signal(true);
  readonly soloMias = signal(false);

  readonly miElo = computed(() => this.eloFresco() ?? this.auth.user()?.elo ?? null);

  readonly tarjetas = computed<Tarjeta[]>(() => {
    const elo = this.miElo();
    return this.cats()
      .map((cat) => this.armarTarjeta(cat, elo))
      .sort(
        (a, b) =>
          (a.cat.eloMinimo ?? 0) - (b.cat.eloMinimo ?? 0) ||
          (a.cat.eloMaximo ?? Infinity) - (b.cat.eloMaximo ?? Infinity) ||
          a.cat.nombre.localeCompare(b.cat.nombre),
      );
  });

  private readonly visibles = computed(() =>
    this.soloMias() ? this.tarjetas().filter((t) => t.situacion === 'puede') : this.tarjetas(),
  );

  readonly conCarreras = computed(() => this.visibles().filter((t) => t.proximas.length > 0));
  readonly sinCarreras = computed(() => this.visibles().filter((t) => t.proximas.length === 0));

  readonly cuantasPuedo = computed(
    () => this.tarjetas().filter((t) => t.situacion === 'puede').length,
  );

  ngOnInit(): void {
    forkJoin({
      cats: this.api.list<Categoria>('/categorias').pipe(this.carga.siFalla([] as Categoria[])),
      proximas: this.api
        .list<Carrera>('/carreras/proximas')
        .pipe(this.carga.siFalla([] as Carrera[])),
      champs: this.api
        .list<Campeonato>('/campeonatos')
        .pipe(this.carga.siFalla([] as Campeonato[])),
      yo: this.auth.autenticado()
        ? this.api.get<Usuario>('/usuarios/me').pipe(this.carga.siFalla(null))
        : of(null),
    }).subscribe((r) => {
      this.cats.set(r.cats);
      this.proximas.set(r.proximas.filter((c) => c.estado !== 'CANCELADA'));
      this.champs.set(r.champs);
      this.eloFresco.set(r.yo?.elo ?? null);
      this.cargando.set(false);
    });
  }

  private armarTarjeta(cat: Categoria, elo: number | null): Tarjeta {
    const min = cat.eloMinimo ?? null;
    const max = cat.eloMaximo ?? null;
    let situacion: Situacion | null = null;
    let diferencia = 0;
    let posicionPct: number | null = null;
    if (elo != null) {
      if (min != null && elo < min) {
        situacion = 'falta';
        diferencia = min - elo;
      } else if (max != null && elo > max) {
        situacion = 'excede';
        diferencia = elo - max;
      } else {
        situacion = 'puede';
      }
      if (min != null && max != null && max > min) {
        posicionPct = Math.min(100, Math.max(0, Math.round(((elo - min) / (max - min)) * 100)));
      }
    }
    const champsCat = this.champs().filter((c) => c.categoriaId === cat.id);
    return {
      cat,
      sigla: this.sigla(cat.nombre),
      tipo: this.tipo(cat),
      rangoTxt: this.rango(min, max),
      setup: this.setup(cat),
      situacion,
      diferencia,
      posicionPct,
      proximas: this.proximas()
        .filter((c) => c.categoriaId === cat.id)
        .sort((a, b) => (a.fecha ?? '').localeCompare(b.fecha ?? '')),
      campeonato: champsCat.find((c) => c.estado === 'ACTIVO') ?? champsCat[0] ?? null,
    };
  }

  /** Un límite null es "sin límite", igual que RequisitosEloValidador en el backend. */
  private rango(min: number | null, max: number | null): string {
    if (min == null && max == null) return t('categorias.rango.sinRequisito');
    if (min == null) return t('categorias.rango.hasta', { max });
    if (max == null) return t('categorias.rango.desde', { min });
    return t('categorias.rango.entre', { min, max });
  }

  /** Mismas etiquetas que el panel de admin. */
  private setup(cat: Categoria): { label: string; ayuda: string } {
    if (cat.setupAbierto) {
      return {
        label: t('categorias.setup.abierto'),
        ayuda: t('categorias.setup.abiertoAyuda'),
      };
    }
    if (cat.setupFijo) {
      return {
        label: t('categorias.setup.fijo'),
        ayuda: t('categorias.setup.fijoAyuda'),
      };
    }
    return { label: t('categorias.setup.libre'), ayuda: t('categorias.setup.libreAyuda') };
  }

  private sigla(nombre: string): string {
    const limpio = nombre.replace(/^LFM\s+/i, '').trim();
    return limpio.length <= 4 ? limpio.toUpperCase() : limpio.slice(0, 3).toUpperCase();
  }

  /** Solo para el color de la sigla: no hay un campo "tipo" en la categoría. */
  private tipo(cat: Categoria): Tarjeta['tipo'] {
    if (/open/i.test(cat.nombre)) return 'abierta';
    if (/^F\d|f[oó]rmula/i.test(cat.nombre + ' ' + (cat.descripcion ?? ''))) return 'formula';
    return 'turismo';
  }

  fechaProxima(c: Carrera): string {
    return fmtFechaHora(c.fecha);
  }

  toggleSoloMias(): void {
    this.soloMias.update((v) => !v);
  }
}

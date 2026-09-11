import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ApiService, apiError } from '../../../core/services/api.service';
import { ToastService } from '../../../core/services/toast.service';
import { Avatar } from '../../../shared/components/avatar/avatar';
import { RankBadge } from '../../../shared/components/rank-badge/rank-badge';
import { EmptyState } from '../../../shared/components/empty-state/empty-state';
import { FmtFechaPipe } from '../../../core/pipes/fmt-fecha.pipe';
import { Categoria, HistorialRating, ResultadoCarrera, StatsResponse, Usuario, UsuarioLogro } from '../../../core/models/models';
import { fmtFecha } from '../../../core/utils/formato';

@Component({
  selector: 'app-driver-profile',
  standalone: true,
  imports: [DecimalPipe, RouterLink, Avatar, RankBadge, EmptyState, FmtFechaPipe],
  styleUrl: './driver-profile.component.scss',
  templateUrl: './driver-profile.component.html',
})
export class DriverProfileComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly toast = inject(ToastService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly sanitizer = inject(DomSanitizer);

  readonly usuario = signal<Usuario | null>(null);
  readonly stats = signal<StatsResponse | null>(null);
  readonly historialElo = signal<HistorialRating[]>([]);
  readonly historialSr = signal<HistorialRating[]>([]);
  readonly resultados = signal<ResultadoCarrera[]>([]);
  readonly logros = signal<UsuarioLogro[]>([]);
  readonly categorias = signal<Categoria[]>([]);
  readonly tab = signal<'elo' | 'sr' | 'resultados' | 'logros'>('elo');

  readonly eloCum = computed(() => this.cumulativo(this.historialElo(), this.usuario()?.elo ?? 0));
  readonly srCum = computed(() => this.cumulativo(this.historialSr(), this.usuario()?.safetyRating ?? 0));

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      void this.router.navigate(['/carreras']);
      return;
    }
    this.cargarPerfil(id);
  }

  private cargarPerfil(id: number): void {
    forkJoin({
      usuario: this.api.get<Usuario>(`/usuarios/${id}`).pipe(catchError(() => of(null))),
      stats: this.api.get<StatsResponse>(`/usuarios/${id}/stats`).pipe(catchError(() => of(null))),
      elo: this.api.list<HistorialRating>(`/usuarios/${id}/historial-elo`).pipe(catchError(() => of([]))),
      sr: this.api.list<HistorialRating>(`/usuarios/${id}/historial-safety-rating`).pipe(catchError(() => of([]))),
      resultados: this.api.list<ResultadoCarrera>(`/resultados/usuario/${id}`).pipe(catchError(() => of([]))),
      logros: this.api.list<UsuarioLogro>(`/usuarios/${id}/logros/obtenidos`).pipe(catchError(() => of([]))),
      categorias: this.api.list<Categoria>('/categorias').pipe(catchError(() => of([]))),
    }).subscribe({
      next: (r) => {
        this.usuario.set(r.usuario);
        this.stats.set(r.stats);
        this.historialElo.set(r.elo);
        this.historialSr.set(r.sr);
        this.resultados.set(r.resultados);
        this.logros.set(r.logros);
        this.categorias.set(r.categorias);
      },
      error: (err) => this.toast.error(apiError(err)),
    });
  }

  categoria(u: Usuario): string {
    const elo = u.elo;
    if (elo === null || elo === undefined) return '';
    const cat = this.categorias().find((c) => {
      if (c.eloMinimo != null && elo < c.eloMinimo) return false;
      if (c.eloMaximo != null && elo > c.eloMaximo) return false;
      return true;
    });
    return cat?.nombre ?? '';
  }

  tagline(u: Usuario): string {
    const parts: string[] = [];
    if (u.email) parts.push(u.email);
    if (u.guidSteam) parts.push('Steam: ' + u.guidSteam);
    if (u.fechaRegistro) parts.push('miembro desde ' + fmtFecha(u.fechaRegistro));
    return parts.join(' · ');
  }

  cumulativo(cambios: HistorialRating[], actual: number): number[] {
    const serie: number[] = [];
    let val = actual - cambios.reduce((s, c) => s + (c.cambio || 0), 0);
    serie.push(val);
    for (const c of cambios) {
      val += c.cambio || 0;
      serie.push(val);
    }
    return serie;
  }

  chartSvg(values: number[], color: string): SafeHtml {
    if (values.length < 2) return this.sanitizer.bypassSecurityTrustHtml('');
    const W = 720, H = 200, PAD = 24;
    const min = Math.min(...values);
    const max = Math.max(...values);
    const rango = max - min || 1;
    const step = (W - PAD * 2) / (values.length - 1);
    const line = values.map((v, i) => {
      const x = PAD + i * step;
      const y = PAD + (H - PAD * 2) * (1 - (v - min) / rango);
      return x.toFixed(2) + ',' + y.toFixed(2);
    });
    const area = line.join(' ') + ' ' + W + ',' + H + ' 0,' + H;
    const ultimo = line[line.length - 1].split(',');
    return this.sanitizer.bypassSecurityTrustHtml(
      '<svg viewBox="0 0 ' + W + ' ' + H + '" width="100%" height="200" role="img">' +
        '<polygon points="' + area + '" fill="' + color + '" opacity="0.15"/>' +
        '<polyline points="' + line.join(' ') + '" fill="none" stroke="' + color + '" stroke-width="2.5" stroke-linejoin="round" stroke-linecap="round"/>' +
        '<circle cx="' + ultimo[0] + '" cy="' + ultimo[1] + '" r="4.5" fill="' + color + '" stroke="var(--bg-paneles)" stroke-width="2"/>' +
        '</svg>'
    );
  }

  ratTxt(v: number | null | undefined): string {
    if (v === null || v === undefined) return '—';
    return (v > 0 ? '+' : '') + v;
  }

  ratColor(v: number | null | undefined): string {
    if (v === null || v === undefined) return '';
    return v >= 0 ? 'var(--status-positivo)' : 'var(--lbm-rojo-hi)';
  }
}
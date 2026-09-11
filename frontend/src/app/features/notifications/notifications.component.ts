import { Component, OnInit, inject, signal } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { ApiService, apiError } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';
import { EmptyState } from '../../shared/components/empty-state/empty-state';
import { FechaRelativaPipe } from '../../core/pipes/fecha-relativa.pipe';
import { Notificacion } from '../../core/models/models';
import { sanitizeUrl } from '../../core/utils/formato';

interface IconoNotif {
  color: string;
  svg: string;
}

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [EmptyState, FechaRelativaPipe],
  styleUrl: './notifications.component.scss',
  templateUrl: './notifications.component.html',
})
export class NotificationsComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly toast = inject(ToastService);
  private readonly sanitizer = inject(DomSanitizer);

  readonly notificaciones = signal<Notificacion[]>([]);
  readonly cargando = signal(true);

  private readonly iconos: Record<string, IconoNotif> = {
    CARRERA_INICIO: {
      color: 'var(--lbm-rojo-hi)',
      svg: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 17V7l8-4 8 4v10l-8 4-8-4Z"/></svg>',
    },
    PENALIZACION: {
      color: 'var(--lbm-rojo)',
      svg: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0Z"/><path d="M12 9v4M12 17h.01"/></svg>',
    },
    LOGRO: {
      color: 'var(--status-alerta)',
      svg: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m12 2 3 7h7l-5.5 4.5L18 21l-6-4-6 4 1.5-7.5L2 9h7z"/></svg>',
    },
    RECOMPENSA: {
      color: 'var(--status-alerta)',
      svg: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m12 2 3 7h7l-5.5 4.5L18 21l-6-4-6 4 1.5-7.5L2 9h7z"/></svg>',
    },
    __default: {
      color: 'var(--lbm-rojo-hi)',
      svg: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="9"/></svg>',
    },
  };

  ngOnInit(): void {
    this.cargar();
  }

  private cargar(): void {
    this.cargando.set(true);
    this.api.list<Notificacion>('/notificaciones/me').subscribe({
      next: (l) => {
        this.notificaciones.set(l);
        this.cargando.set(false);
      },
      error: (err) => {
        this.cargando.set(false);
        this.toast.error(apiError(err));
      },
    });
  }

  toggleLeida(n: Notificacion): void {
    this.api.put<void>(`/notificaciones/${n.id}/leida`).subscribe({
      next: () => this.cargar(),
      error: (err) => this.toast.error(apiError(err)),
    });
  }

  eliminar(n: Notificacion): void {
    this.api.del<void>(`/notificaciones/${n.id}`).subscribe({
      next: () => this.cargar(),
      error: (err) => this.toast.error(apiError(err)),
    });
  }

  marcarTodas(): void {
    this.api.put<void>('/notificaciones/me/leidas').subscribe({
      next: () => {
        this.toast.success('Todas marcadas como leídas');
        this.cargar();
      },
      error: (err) => this.toast.error(apiError(err)),
    });
  }

  noLeidasCount(): number {
    return this.notificaciones().filter((n) => !n.leida).length;
  }

  tituloTipo(tipo: string): string {
    return tipo
      .split('_')
      .map((w) => w.charAt(0).toUpperCase() + w.slice(1).toLowerCase())
      .join(' ');
  }

  iconoColor(tipo: string): string {
    return (this.iconos[tipo] || this.iconos['__default']).color;
  }

  iconoSeguro(tipo: string): SafeHtml {
    const cfg = this.iconos[tipo] || this.iconos['__default'];
    return this.sanitizer.bypassSecurityTrustHtml(cfg.svg);
  }

  linkSeguro(link: string | undefined): string {
    return sanitizeUrl(link);
  }
}
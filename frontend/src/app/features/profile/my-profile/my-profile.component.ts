import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ApiService, apiError } from '../../../core/services/api.service';
import { ToastService } from '../../../core/services/toast.service';
import { AuthService } from '../../../core/services/auth.service';
import { Chip } from '../../../shared/components/chip/chip';
import { EmptyState } from '../../../shared/components/empty-state/empty-state';
import { Inscripcion, Usuario } from '../../../core/models/models';
import { fmtFecha } from '../../../core/utils/formato';

interface RecompensaDisponible {
  id?: number;
  recompensaId?: number;
  descripcion?: string;
}

@Component({
  selector: 'app-my-profile',
  standalone: true,
  imports: [RouterLink, Chip, EmptyState],
  styleUrl: './my-profile.component.scss',
  templateUrl: './my-profile.component.html',
})
export class MyProfileComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly toast = inject(ToastService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly user = signal<Usuario | null>(this.auth.user());
  readonly nombre = signal('');
  readonly email = signal('');
  readonly foto = signal('');
  readonly passwordActual = signal('');
  readonly passwordNueva = signal('');
  readonly passwordActualEdit = signal(false);
  readonly inscripciones = signal<Inscripcion[]>([]);
  readonly recompensas = signal<RecompensaDisponible[]>([]);
  readonly tab = signal<'datos' | 'password' | 'steam'>('datos');
  readonly steamOk = signal<string | null>(null);
  readonly cargando = signal(false);

  ngOnInit(): void {
    this.procesarQueries();
    this.refrescarUsuario();
    this.cargarInscripciones();
    this.cargarRecompensas();
  }

  private procesarQueries(): void {
    const steam = this.route.snapshot.queryParamMap.get('steam');
    if (!steam) return;
    this.steamOk.set(steam);
    history.replaceState({}, '', location.pathname);
    if (steam === 'ok') {
      this.toast.success('Cuenta de Steam vinculada');
    } else if (steam === 'ocupado') {
      this.toast.error('Esa cuenta de Steam ya está vinculada a otro usuario');
    } else if (steam === 'expirado') {
      this.toast.error('El enlace de vinculación expiró. Intentá de nuevo.');
    } else {
      this.toast.error('No se pudo vincular la cuenta de Steam.');
    }
  }

  private refrescarUsuario(): void {
    const id = this.auth.user()?.id;
    if (!id) return;
    this.api.get<Usuario>(`/usuarios/${id}`).subscribe({
      next: (u) => {
        this.user.set(u);
        this.auth.updateUser(u);
        this.nombre.set(u.nombrePiloto || '');
        this.email.set(u.email || '');
        this.foto.set(u.fotoPerfil || '');
        this.passwordActualEdit.set(!!u.passwordEstablecida);
      },
      error: (err) => this.toast.error(apiError(err)),
    });
  }

  private cargarInscripciones(): void {
    const id = this.auth.user()?.id;
    if (!id) return;
    this.api.list<Inscripcion>(`/inscripciones/usuario/${id}`).pipe(catchError(() => of([]))).subscribe({
      next: (l) => this.inscripciones.set(l.filter((i) => i.estado !== 'CANCELADA')),
    });
  }

  private cargarRecompensas(): void {
    const id = this.auth.user()?.id;
    if (!id) return;
    this.api.list<RecompensaDisponible>(`/usuarios/${id}/recompensas/no-reclamadas`).pipe(catchError(() => of([]))).subscribe({
      next: (l) => this.recompensas.set(l),
    });
  }

  fechaDia(ins: Inscripcion): string {
    return fmtFecha(ins.carreraFecha).split(' ')[0];
  }

  fechaMes(ins: Inscripcion): string {
    return fmtFecha(ins.carreraFecha).split(' ')[1] || '';
  }

  guardar(): void {
    const id = this.auth.user()?.id;
    if (!id) return;
    this.cargando.set(true);
    this.api.put<Usuario>(`/usuarios/${id}/perfil`, {
      email: this.email().trim(),
      nombrePiloto: this.nombre().trim(),
      fotoPerfil: this.foto().trim() || null,
    }).subscribe({
      next: (u) => {
        this.cargando.set(false);
        this.user.set(u);
        this.auth.updateUser(u);
        this.passwordActualEdit.set(!!u.passwordEstablecida);
        this.toast.success('Perfil actualizado');
      },
      error: (err) => {
        this.cargando.set(false);
        this.toast.error(apiError(err));
      },
    });
  }

  cambiarPassword(): void {
    const id = this.auth.user()?.id;
    if (!id) return;
    const nueva = this.passwordNueva().trim();
    if (!nueva) {
      this.toast.error('Escribí la nueva contraseña.');
      return;
    }
    if (nueva.length < 6) {
      this.toast.error('Usá al menos 6 caracteres.');
      return;
    }
    if (this.passwordActualEdit() && !this.passwordActual().trim()) {
      this.toast.error('Escribí tu contraseña actual.');
      return;
    }
    this.cargando.set(true);
    const body: { nuevaPassword: string; passwordActual?: string } = { nuevaPassword: nueva };
    if (this.passwordActualEdit()) body.passwordActual = this.passwordActual().trim();
    this.api.put<void>(`/usuarios/${id}/password`, body).subscribe({
      next: () => {
        this.cargando.set(false);
        this.auth.clearSesion();
        this.toast.success('Contraseña actualizada. Ingresá con tu nueva contraseña.');
        void this.router.navigate(['/auth']);
      },
      error: (err) => {
        this.cargando.set(false);
        this.toast.error(apiError(err));
      },
    });
  }

  vincularSteam(): void {
    this.api.get<{ url: string }>('/steam/vincular-url').subscribe({
      next: (data) => {
        if (data?.url) {
          window.location.href = data.url;
        } else {
          this.toast.error('No se pudo iniciar la vinculación con Steam.');
        }
      },
      error: (err) => this.toast.error(apiError(err)),
    });
  }

  desvincularSteam(): void {
    const id = this.auth.user()?.id;
    if (!id) return;
    this.api.del<void>(`/usuarios/${id}/steam`).subscribe({
      next: () => {
        this.toast.success('Cuenta de Steam desvinculada');
        this.refrescarUsuario();
      },
      error: (err) => this.toast.error(apiError(err)),
    });
  }

  cancelarInscripcion(ins: Inscripcion): void {
    const id = this.auth.user()?.id;
    if (!id) return;
    this.api.del<void>(`/inscripciones/carrera/${ins.carreraId}/usuario/${id}`).subscribe({
      next: () => {
        this.toast.success('Inscripción cancelada');
        this.cargarInscripciones();
      },
      error: (err) => this.toast.error(apiError(err)),
    });
  }

  reclamar(r: RecompensaDisponible): void {
    const id = this.auth.user()?.id;
    if (!id || !r.recompensaId) return;
    this.api.post<void>(`/recompensas/usuario/${id}/recompensas/${r.recompensaId}/reclamar`).subscribe({
      next: () => {
        this.toast.success('¡Recompensa reclamada!');
        this.cargarRecompensas();
      },
      error: (err) => this.toast.error(apiError(err)),
    });
  }
}
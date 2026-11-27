import { TranslocoPipe } from '@jsverse/transloco';
import { t, t as translate } from '../../../core/i18n/t';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApiService, apiError } from '../../../core/services/api.service';
import { ErrorCargaService } from '../../../core/services/error-carga.service';
import { ToastService } from '../../../core/services/toast.service';
import { AuthService } from '../../../core/services/auth.service';
import { RelojService } from '../../../core/services/reloj.service';
import { Chip } from '../../../shared/components/chip/chip';
import { EmptyState } from '../../../shared/components/empty-state/empty-state';
import { ConfirmDialog } from '../../../shared/components/confirm-dialog/confirm-dialog';
import { PasswordInput } from '../../../shared/components/password-input/password-input';
import { Inscripcion, Usuario, UsuarioRecompensa } from '../../../core/models/models';
import { fmtDia, fmtMes, parseIso } from '../../../core/utils/formato';

type Tab = 'datos' | 'password' | 'steam';
const TABS: readonly Tab[] = ['datos', 'password', 'steam'];

/** InscripcionesAbiertasValidador.MINUTOS_CIERRE_PREVIO en el backend. */
const MINUTOS_CIERRE_PREVIO = 5;
const MIN_PASSWORD = 6;
const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

type Confirmacion = { tipo: 'steam' } | { tipo: 'inscripcion'; ins: Inscripcion };

@Component({
  selector: 'app-my-profile',
  standalone: true,
  imports: [TranslocoPipe, RouterLink, FormsModule, Chip, EmptyState, ConfirmDialog, PasswordInput],
  styleUrl: './my-profile.component.scss',
  templateUrl: './my-profile.component.html',
})
export class MyProfileComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly carga = inject(ErrorCargaService);
  private readonly toast = inject(ToastService);
  private readonly auth = inject(AuthService);
  private readonly reloj = inject(RelojService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly tabs = TABS;
  readonly minPassword = MIN_PASSWORD;

  readonly user = signal<Usuario | null>(this.auth.user());
  readonly tab = signal<Tab>('datos');

  // ---- datos personales ----
  readonly nombre = signal('');
  readonly email = signal('');
  readonly guardandoDatos = signal(false);
  readonly emailValido = computed(() => EMAIL_RE.test(this.email().trim()));
  readonly nombreValido = computed(() => this.nombre().trim().length > 0);
  readonly cambiaEmail = computed(() => this.email().trim() !== (this.user()?.email ?? ''));
  /** Cambiar el email exige la contraseña actual (si la cuenta tiene una). */
  readonly passwordEmail = signal('');
  readonly pidePasswordEmail = computed(
    () => this.cambiaEmail() && !!this.user()?.passwordEstablecida,
  );
  readonly datosModificados = computed(() => {
    const u = this.user();
    return this.cambiaEmail() || this.nombre().trim() !== (u?.nombrePiloto ?? '');
  });
  readonly puedeGuardarDatos = computed(
    () =>
      this.datosModificados() &&
      this.emailValido() &&
      (!this.pidePasswordEmail() || this.passwordEmail().length > 0) &&
      this.nombreValido() &&
      !this.guardandoDatos(),
  );

  // ---- contraseña (sin trim: los espacios son parte de la contraseña) ----
  readonly passwordActual = signal('');
  readonly passwordNueva = signal('');
  readonly passwordRepetida = signal('');
  readonly cambiandoPassword = signal(false);
  readonly requiereActual = computed(() => !!this.user()?.passwordEstablecida);
  readonly passwordCorta = computed(
    () => this.passwordNueva().length > 0 && this.passwordNueva().length < MIN_PASSWORD,
  );
  readonly passwordsDistintas = computed(
    () => this.passwordRepetida().length > 0 && this.passwordNueva() !== this.passwordRepetida(),
  );
  readonly puedeCambiarPassword = computed(
    () =>
      this.passwordNueva().length >= MIN_PASSWORD &&
      this.passwordNueva() === this.passwordRepetida() &&
      (!this.requiereActual() || this.passwordActual().length > 0) &&
      !this.cambiandoPassword(),
  );

  // ---- steam ----
  readonly procesandoSteam = signal(false);
  readonly steamUrl = computed(() => {
    const guid = this.user()?.guidSteam;
    return guid ? `https://steamcommunity.com/profiles/${encodeURIComponent(guid)}` : null;
  });

  // ---- inscripciones / recompensas ----
  private readonly inscripcionesTodas = signal<Inscripcion[]>([]);
  /** Solo carreras que todavía no empezaron, de la más cercana a la más lejana. */
  readonly inscripciones = computed(() => {
    const ahora = this.reloj.ahora();
    return this.inscripcionesTodas()
      .filter((i) => i.estado !== 'CANCELADA')
      .filter((i) => (parseIso(i.carreraFecha)?.getTime() ?? 0) > ahora)
      .sort(
        (a, b) =>
          (parseIso(a.carreraFecha)?.getTime() ?? 0) - (parseIso(b.carreraFecha)?.getTime() ?? 0),
      );
  });
  readonly cancelando = signal<number | null>(null);
  readonly recompensas = signal<UsuarioRecompensa[]>([]);
  readonly reclamando = signal<number | null>(null);

  readonly aConfirmar = signal<Confirmacion | null>(null);
  readonly confirmacion = computed(() => {
    const c = this.aConfirmar();
    if (!c) return null;
    if (c.tipo === 'steam') {
      return {
        titulo: t('miPerfil.confirmar.steamTitulo'),
        mensaje: t('miPerfil.confirmar.steamMensaje'),
        boton: t('miPerfil.steam.desvincular'),
      };
    }
    return {
      titulo: t('miPerfil.confirmar.cancelarTitulo'),
      mensaje: t('miPerfil.confirmar.cancelarMensaje', { nombre: c.ins.carreraNombre }),
      boton: t('miPerfil.confirmar.cancelarTitulo'),
    };
  });

  ngOnInit(): void {
    this.leerTab();
    this.procesarSteam();
    this.refrescarUsuario();
    this.cargarInscripciones();
    this.cargarRecompensas();
  }

  // ---- pestañas (persistidas en ?tab=) ----

  private leerTab(): void {
    const t = this.route.snapshot.queryParamMap.get('tab') as Tab | null;
    if (t && TABS.includes(t)) this.tab.set(t);
  }

  elegirTab(t: Tab): void {
    this.tab.set(t);
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { tab: t === 'datos' ? null : t },
      replaceUrl: true,
    });
  }

  /** Flechas izquierda/derecha entre pestañas (patrón ARIA tabs). */
  moverTab(e: KeyboardEvent): void {
    if (e.key !== 'ArrowRight' && e.key !== 'ArrowLeft') return;
    e.preventDefault();
    const i = TABS.indexOf(this.tab());
    const sig = TABS[(i + (e.key === 'ArrowRight' ? 1 : TABS.length - 1)) % TABS.length];
    this.elegirTab(sig);
    document.getElementById(`mp-tab-${sig}`)?.focus();
  }

  // El parámetro se llama `t` como la pestaña, así que acá se usa `translate` (mismo efecto).
  tabLabel(t: Tab): string {
    return translate(
      t === 'datos'
        ? 'miPerfil.tab.datos'
        : t === 'password'
          ? 'miPerfil.tab.password'
          : 'miPerfil.tab.steam',
    );
  }

  /** Vuelta del OpenID de Steam: /mi-perfil?steam=ok|ocupado|expirado|error */
  private procesarSteam(): void {
    const steam = this.route.snapshot.queryParamMap.get('steam');
    if (!steam) return;
    this.tab.set('steam');
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { tab: 'steam' },
      replaceUrl: true,
    });
    if (steam === 'ok') {
      this.toast.success(t('miPerfil.toast.steamVinculada'));
    } else if (steam === 'ocupado') {
      this.toast.error(t('miPerfil.toast.steamOcupada'));
    } else if (steam === 'expirado') {
      this.toast.error(t('miPerfil.toast.steamExpirado'));
    } else {
      this.toast.error(t('miPerfil.toast.steamError'));
    }
  }

  // ---- carga ----

  private refrescarUsuario(): void {
    const id = this.auth.user()?.id;
    if (!id) return;
    this.api.get<Usuario>(`/usuarios/${id}`).subscribe({
      next: (u) => this.aplicarUsuario(u),
      error: (err) => this.toast.error(apiError(err)),
    });
  }

  private aplicarUsuario(u: Usuario): void {
    this.user.set(u);
    this.auth.updateUser(u);
    this.nombre.set(u.nombrePiloto || '');
    this.email.set(u.email || '');
  }

  private cargarInscripciones(): void {
    const id = this.auth.user()?.id;
    if (!id) return;
    this.api
      .list<Inscripcion>(`/inscripciones/usuario/${id}`)
      .pipe(this.carga.siFalla([]))
      .subscribe({ next: (l) => this.inscripcionesTodas.set(l) });
  }

  private cargarRecompensas(): void {
    const id = this.auth.user()?.id;
    if (!id) return;
    this.api
      .list<UsuarioRecompensa>(`/usuarios/${id}/recompensas/no-reclamadas`)
      .pipe(this.carga.siFalla([]))
      .subscribe({ next: (l) => this.recompensas.set(l) });
  }

  fechaDia(ins: Inscripcion): string {
    return fmtDia(ins.carreraFecha);
  }

  fechaMes(ins: Inscripcion): string {
    return fmtMes(ins.carreraFecha);
  }

  /** El backend rechaza la baja a menos de MINUTOS_CIERRE_PREVIO de la largada. */
  puedeCancelar(ins: Inscripcion): boolean {
    const t = parseIso(ins.carreraFecha)?.getTime();
    return t != null && t - this.reloj.ahora() > MINUTOS_CIERRE_PREVIO * 60_000;
  }

  // ---- acciones ----

  descartarDatos(): void {
    const u = this.user();
    this.nombre.set(u?.nombrePiloto ?? '');
    this.email.set(u?.email ?? '');
    this.passwordEmail.set('');
  }

  guardar(): void {
    const id = this.auth.user()?.id;
    if (!id || !this.puedeGuardarDatos()) return;
    const email = this.email().trim();
    // El JWT usa el email como subject: si cambia, el token actual deja de ser valido.
    const cambiaEmail = this.cambiaEmail();
    this.guardandoDatos.set(true);
    this.api
      .put<Usuario>(`/usuarios/${id}/perfil`, {
        email,
        nombrePiloto: this.nombre().trim(),
        password: this.pidePasswordEmail() ? this.passwordEmail() : undefined,
      })
      .subscribe({
        next: (u) => {
          this.guardandoDatos.set(false);
          if (cambiaEmail) {
            this.auth.clearSesion();
            this.toast.success(t('miPerfil.toast.emailActualizado'));
            void this.router.navigate(['/auth']);
            return;
          }
          this.aplicarUsuario(u);
          this.toast.success(t('miPerfil.toast.perfilActualizado'));
        },
        error: (err) => {
          this.guardandoDatos.set(false);
          this.toast.error(apiError(err));
        },
      });
  }

  cambiarPassword(): void {
    const id = this.auth.user()?.id;
    if (!id || !this.puedeCambiarPassword()) return;
    const body: { nuevaPassword: string; passwordActual?: string } = {
      nuevaPassword: this.passwordNueva(),
    };
    if (this.requiereActual()) body.passwordActual = this.passwordActual();
    this.cambiandoPassword.set(true);
    this.api.put<void>(`/usuarios/${id}/password`, body).subscribe({
      next: () => {
        this.cambiandoPassword.set(false);
        this.auth.clearSesion();
        this.toast.success(t('miPerfil.toast.passwordActualizada'));
        void this.router.navigate(['/auth']);
      },
      error: (err) => {
        this.cambiandoPassword.set(false);
        this.toast.error(apiError(err));
      },
    });
  }

  vincularSteam(): void {
    this.procesandoSteam.set(true);
    this.api.get<{ url: string }>('/steam/vincular-url').subscribe({
      next: (data) => {
        if (data?.url) {
          window.location.href = data.url;
        } else {
          this.procesandoSteam.set(false);
          this.toast.error(t('miPerfil.toast.steamNoIniciado'));
        }
      },
      error: (err) => {
        this.procesandoSteam.set(false);
        this.toast.error(apiError(err));
      },
    });
  }

  pedirDesvincularSteam(): void {
    this.aConfirmar.set({ tipo: 'steam' });
  }

  pedirCancelarInscripcion(ins: Inscripcion): void {
    this.aConfirmar.set({ tipo: 'inscripcion', ins });
  }

  confirmar(): void {
    const c = this.aConfirmar();
    this.aConfirmar.set(null);
    if (!c) return;
    if (c.tipo === 'steam') this.desvincularSteam();
    else this.cancelarInscripcion(c.ins);
  }

  private desvincularSteam(): void {
    const id = this.auth.user()?.id;
    if (!id) return;
    this.procesandoSteam.set(true);
    this.api.del<Usuario>(`/usuarios/${id}/steam`).subscribe({
      next: () => {
        this.procesandoSteam.set(false);
        this.toast.success(t('miPerfil.toast.steamDesvinculada'));
        this.refrescarUsuario();
      },
      error: (err) => {
        this.procesandoSteam.set(false);
        this.toast.error(apiError(err));
      },
    });
  }

  private cancelarInscripcion(ins: Inscripcion): void {
    const id = this.auth.user()?.id;
    if (!id) return;
    this.cancelando.set(ins.id);
    this.api.del<void>(`/inscripciones/carrera/${ins.carreraId}/usuario/${id}`).subscribe({
      next: () => {
        this.cancelando.set(null);
        this.toast.success(t('miPerfil.toast.inscripcionCancelada'));
        this.cargarInscripciones();
      },
      error: (err) => {
        this.cancelando.set(null);
        this.toast.error(apiError(err));
      },
    });
  }

  reclamar(r: UsuarioRecompensa): void {
    const id = this.auth.user()?.id;
    if (!id || !r.recompensaId) return;
    this.reclamando.set(r.recompensaId);
    this.api
      .post<void>(`/recompensas/usuario/${id}/recompensas/${r.recompensaId}/reclamar`)
      .subscribe({
        next: () => {
          this.reclamando.set(null);
          this.toast.success(t('miPerfil.toast.recompensaReclamada'));
          this.cargarRecompensas();
        },
        error: (err) => {
          this.reclamando.set(null);
          this.toast.error(apiError(err));
        },
      });
  }
}

import { Component, inject, signal, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { ApiService, apiError } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';
import { Usuario } from '../../core/models/models';
import { TranslocoPipe, TranslocoService } from '@jsverse/transloco';
import { PasswordInput } from '../../shared/components/password-input/password-input';
import { SelectorIdioma } from '../../shared/components/selector-idioma/selector-idioma';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, PasswordInput, TranslocoPipe, SelectorIdioma],
  styleUrl: './auth.scss',
  templateUrl: './auth.html',
})
export class AuthComponent implements OnInit {
  readonly auth = inject(AuthService);
  private readonly api = inject(ApiService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder);
  private readonly transloco = inject(TranslocoService);

  readonly tab = signal<'login' | 'register'>('login');
  readonly modoSteamSetup = signal(false);
  readonly cargando = signal(false);

  readonly loginForm = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
  });

  readonly steamForm = this.fb.nonNullable.group({
    // JWT firmado por el backend tras el OpenID de Steam (no el GUID suelto).
    tokenRegistro: ['', Validators.required],
    nombrePiloto: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
  });

  readonly registerForm = this.fb.nonNullable.group({
    nombrePiloto: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  ngOnInit(): void {
    // "Crear cuenta" del home llega con ?tab=registro.
    if (this.route.snapshot.queryParamMap.get('tab') === 'registro') this.tab.set('register');
    if (sessionStorage.getItem('lfm_msg_pass_changed')) {
      sessionStorage.removeItem('lfm_msg_pass_changed');
      this.toast.success(this.t('auth.toast.passwordActualizada'));
    }
    setTimeout(() => this.procesarSteam(), 0);
  }

  setTab(t: 'login' | 'register'): void {
    this.tab.set(t);
  }

  errores(form: FormGroup, campo: string): string[] {
    const c = form.controls[campo];
    if (!c || c.valid || (!c.touched && !c.dirty) || c.disabled) return [];
    const out: string[] = [];
    if (c.errors?.['required']) {
      const claves: Record<string, string> = {
        email: 'auth.errores.emailRequerido',
        password: 'auth.errores.passwordRequerida',
        nombrePiloto: 'auth.errores.nombreRequerido',
      };
      out.push(this.t(claves[campo] ?? 'auth.errores.obligatorio'));
    }
    if (c.errors?.['email']) out.push(this.t('auth.errores.emailInvalido'));
    if (c.errors?.['minlength'])
      out.push(this.t('auth.errores.minimo', { n: c.errors['minlength'].requiredLength }));
    return out;
  }

  invalido(form: FormGroup, campo: string): boolean {
    const c = form.controls[campo];
    return !!c && c.invalid && (c.touched || c.dirty);
  }

  marcarTocados(form: FormGroup): void {
    Object.values(form.controls).forEach((c) => c.markAsTouched({ onlySelf: true }));
  }

  private nextValor(): string {
    const n = this.route.snapshot.queryParamMap.get('next') || '';
    if (
      n.startsWith('/') &&
      !n.startsWith('//') &&
      !/^https?:\/\//i.test(n) &&
      !n.toLowerCase().startsWith('javascript:')
    ) {
      return n;
    }
    return '';
  }

  private redirigir(): void {
    const n = this.nextValor();
    if (n) this.auth.guardarNext(n);
    this.auth.redirectPostLogin();
  }

  login(): void {
    if (this.loginForm.invalid) {
      this.marcarTocados(this.loginForm);
      this.toast.error(this.t('auth.toast.completarLogin'));
      return;
    }
    this.cargando.set(true);
    const { email, password } = this.loginForm.getRawValue();
    this.auth.login(email.trim(), password).subscribe({
      next: (data) => {
        this.cargando.set(false);
        this.auth.setSesion(data.token, data.usuario);
        this.toast.success(this.t('auth.toast.bienvenido', { nombre: this.nombre(data.usuario) }));
        this.avisarSteamVinculacion(data.usuario);
        this.redirigir();
      },
      error: (err) => {
        this.cargando.set(false);
        this.toast.error(apiError(err));
      },
    });
  }

  iniciarSteam(): void {
    if (this.cargando()) return;
    this.cargando.set(true);
    this.api.get<{ url: string }>('/steam/auth-url').subscribe({
      next: (data) => {
        this.cargando.set(false);
        if (data?.url) {
          window.location.href = data.url;
        } else {
          this.toast.error(this.t('auth.toast.steamNoIniciado'));
        }
      },
      error: (err) => {
        this.cargando.set(false);
        this.toast.error(apiError(err));
      },
    });
  }

  crearCuentaSteam(): void {
    if (this.steamForm.invalid) {
      this.marcarTocados(this.steamForm);
      this.toast.error(this.t('auth.toast.completarSteam'));
      return;
    }
    this.cargando.set(true);
    const v = this.steamForm.getRawValue();
    this.api
      .post<LoginResponseLFM>('/usuarios/registro-steam', {
        email: v.email.trim(),
        nombrePiloto: v.nombrePiloto.trim(),
        tokenRegistro: v.tokenRegistro,
      })
      .subscribe({
        next: (data) => {
          this.cargando.set(false);
          this.auth.setSesion(data.token, data.usuario);
          this.toast.success(this.t('auth.toast.cuentaCreada'));
          this.redirigir();
        },
        error: (err) => {
          this.cargando.set(false);
          this.toast.error(apiError(err));
        },
      });
  }

  registrar(): void {
    if (this.registerForm.invalid) {
      this.marcarTocados(this.registerForm);
      this.toast.error(this.t('auth.toast.completarRegistro'));
      return;
    }
    this.cargando.set(true);
    const v = this.registerForm.getRawValue();
    this.api
      .post<LoginResponseLFM>('/usuarios', {
        email: v.email.trim(),
        nombrePiloto: v.nombrePiloto.trim(),
        password: v.password,
      })
      .subscribe({
        next: (data) => {
          this.cargando.set(false);
          this.auth.setSesion(data.token, data.usuario);
          this.toast.success(
            this.t('auth.toast.cuentaCreadaBienvenido', { nombre: this.nombre(data.usuario) }),
          );
          this.avisarSteamVinculacion(data.usuario);
          this.redirigir();
        },
        error: (err) => {
          this.cargando.set(false);
          this.toast.error(apiError(err));
        },
      });
  }

  private t(clave: string, params?: Record<string, unknown>): string {
    return this.transloco.translate(clave, params);
  }

  private nombre(usuario: Usuario | null | undefined): string {
    return usuario?.nombrePiloto || this.t('auth.toast.piloto');
  }

  private avisarSteamVinculacion(usuario: Usuario): void {
    if (!usuario.guidSteam) {
      this.toast.mostrar(this.t('auth.toast.sinSteam'), '', 6000);
    }
  }

  private procesarSteam(): void {
    const q = this.route.snapshot.queryParamMap;
    const steam = q.get('steam');
    if (!steam) return;
    const codigo = q.get('codigo');
    const registro = q.get('registro');
    this.router.navigate([], { replaceUrl: true, relativeTo: this.route });

    if (steam === 'ok' && codigo) {
      this.api.post<LoginResponseLFM>('/steam/completar', { codigo }).subscribe({
        next: (data) => {
          this.auth.setSesion(data.token, null);
          this.api.get<Usuario>('/usuarios/me').subscribe({
            next: (usuario) => {
              this.auth.updateUser(usuario);
              this.toast.success(this.t('auth.toast.bienvenido', { nombre: this.nombre(usuario) }));
              this.redirigir();
            },
            error: (err) => {
              // Sin usuario la sesion queda a medias (token guardado, autenticado() false).
              this.auth.clearSesion();
              this.toast.error(apiError(err));
            },
          });
        },
        error: (err) => this.toast.error(apiError(err)),
      });
    } else if (steam === 'nuevo' && registro) {
      this.steamForm.patchValue({ tokenRegistro: registro });
      this.modoSteamSetup.set(true);
    } else {
      this.toast.error(
        steam === 'expirado' ? this.t('auth.toast.steamExpirado') : this.t('auth.toast.steamError'),
      );
    }
  }
}

interface LoginResponseLFM {
  token: string;
  usuario: Usuario;
}

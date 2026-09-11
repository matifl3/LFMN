import { Component, inject, signal, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { ApiService, apiError } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';
import { Usuario } from '../../core/models/models';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [ReactiveFormsModule],
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

  readonly tab = signal<'login' | 'register'>('login');
  readonly modoSteamSetup = signal(false);
  readonly cargando = signal(false);

  readonly loginForm = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
  });

  readonly steamForm = this.fb.nonNullable.group({
    guidSteam: ['', Validators.required],
    nombrePiloto: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
  });

  readonly registerForm = this.fb.nonNullable.group({
    nombrePiloto: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  ngOnInit(): void {
    if (sessionStorage.getItem('lfm_msg_pass_changed')) {
      sessionStorage.removeItem('lfm_msg_pass_changed');
      this.toast.success('Contraseña actualizada. Ingresá con tu nueva contraseña.');
    }
    setTimeout(() => this.procesarSteam(), 0);
  }

  setTab(t: 'login' | 'register'): void {
    this.tab.set(t);
  }

  private nextValor(): string {
    const n = this.route.snapshot.queryParamMap.get('next') || '';
    if (n.startsWith('/') && !n.startsWith('//') && !/^https?:\/\//i.test(n) && !n.toLowerCase().startsWith('javascript:')) {
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
      this.toast.error('Completá tu email y contraseña.');
      return;
    }
    this.cargando.set(true);
    const { email, password } = this.loginForm.getRawValue();
    this.auth.login(email.trim(), password).subscribe({
      next: (data) => {
        this.cargando.set(false);
        this.auth.setSesion(data.token, data.usuario);
        this.toast.success(`¡Bienvenido, ${data.usuario.nombrePiloto || 'piloto'}!`);
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
          this.toast.error('No se pudo iniciar el ingreso con Steam.');
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
      this.toast.error('Completá tu nombre de piloto y email.');
      return;
    }
    this.cargando.set(true);
    const v = this.steamForm.getRawValue();
    this.api.post<LoginResponseLFM>('/usuarios/registro-steam', {
      email: v.email.trim(),
      nombrePiloto: v.nombrePiloto.trim(),
      guidSteam: v.guidSteam,
    }).subscribe({
      next: (data) => {
        this.cargando.set(false);
        this.auth.setSesion(data.token, data.usuario);
        this.toast.success('Cuenta creada correctamente');
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
      this.toast.error('Completá nombre de piloto, email y contraseña (mínimo 6 caracteres).');
      return;
    }
    this.cargando.set(true);
    const v = this.registerForm.getRawValue();
    this.api.post<LoginResponseLFM>('/usuarios', {
      email: v.email.trim(),
      nombrePiloto: v.nombrePiloto.trim(),
      password: v.password,
    }).subscribe({
      next: (data) => {
        this.cargando.set(false);
        this.auth.setSesion(data.token, data.usuario);
        this.toast.success('Cuenta creada correctamente. ¡Bienvenido, ' + (data.usuario.nombrePiloto || 'piloto') + '!');
        this.redirigir();
      },
      error: (err) => {
        this.cargando.set(false);
        this.toast.error(apiError(err));
      },
    });
  }

  private procesarSteam(): void {
    const q = this.route.snapshot.queryParamMap;
    const steam = q.get('steam');
    if (!steam) return;
    const codigo = q.get('codigo');
    const guid = q.get('guid');
    this.router.navigate([], { replaceUrl: true, relativeTo: this.route });

    if (steam === 'ok' && codigo) {
      this.api.post<LoginResponseLFM>('/steam/completar', { codigo }).subscribe({
        next: (data) => {
          this.auth.setSesion(data.token, null);
          this.api.get<Usuario>('/usuarios/me').subscribe({
            next: (usuario) => {
              this.auth.updateUser(usuario);
              this.toast.success(`¡Bienvenido, ${usuario.nombrePiloto || 'piloto'}!`);
              this.redirigir();
            },
            error: (err) => this.toast.error(apiError(err)),
          });
        },
        error: (err) => this.toast.error(apiError(err)),
      });
    } else if (steam === 'nuevo' && guid) {
      this.steamForm.patchValue({ guidSteam: guid });
      this.modoSteamSetup.set(true);
    } else {
      this.toast.error(
        steam === 'expirado'
          ? 'El enlace de ingreso con Steam expiró. Intentá de nuevo.'
          : 'No se pudo ingresar con Steam.'
      );
    }
  }
}

interface LoginResponseLFM {
  token: string;
  usuario: Usuario;
}
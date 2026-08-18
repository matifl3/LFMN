import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { firstValueFrom } from 'rxjs';

import { AuthService } from '../../core/auth/auth.service';
import { ErrorHandlerService } from '../../core/error/error-handler.service';

@Component({
  selector: 'app-login',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly errorHandler = inject(ErrorHandlerService);

  readonly cargando = signal(false);
  ocultarPassword = true;

  readonly form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
  });

  async submit() {
    if (this.form.invalid) {
      return;
    }
    this.cargando.set(true);
    const { email, password } = this.form.value;
    try {
      const respuesta = await firstValueFrom(
        this.auth.login({ email: email ?? '', password: password ?? '' }),
      );
      if (respuesta) {
        this.auth.guardarSesion(respuesta.token, respuesta.usuario);
        await this.router.navigate(['/']);
      }
    } catch (error) {
      this.errorHandler.handle(error, 'Credenciales invalidas');
    } finally {
      this.cargando.set(false);
    }
  }
}

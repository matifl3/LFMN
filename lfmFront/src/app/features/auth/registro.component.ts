import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { firstValueFrom } from 'rxjs';

import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { AuthService } from '../../core/auth/auth.service';
import { ErrorHandlerService } from '../../core/error/error-handler.service';

@Component({
  selector: 'app-registro',
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
  templateUrl: './registro.component.html',
  styleUrl: './registro.component.scss',
})
export class RegistroComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly errorHandler = inject(ErrorHandlerService);

  readonly cargando = signal(false);
  ocultarPassword = true;

  readonly form = this.fb.group({
    nombrePiloto: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    guidSteam: [''],
  });

  async submit() {
    if (this.form.invalid) {
      return;
    }
    this.cargando.set(true);
    const { nombrePiloto, email, password, guidSteam } = this.form.value;
    try {
      await firstValueFrom(
        this.auth.registrar({
          email: email ?? '',
          password: password ?? '',
          nombrePiloto: nombrePiloto ?? null,
          guidSteam: guidSteam || null,
        }),
      );
      await this.router.navigate(['/login']);
    } catch (error) {
      this.errorHandler.handle(error, 'No se pudo registrar el usuario');
    } finally {
      this.cargando.set(false);
    }
  }
}

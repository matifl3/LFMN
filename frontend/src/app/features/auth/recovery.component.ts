import { Component, inject, signal, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApiService, apiError } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';
import { PasswordInput } from '../../shared/components/password-input/password-input';

@Component({
  selector: 'app-recovery',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, PasswordInput],
  styleUrl: './recovery.scss',
  templateUrl: './recovery.html',
})
export class RecoveryComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder);

  readonly token = signal('');
  readonly enviado = signal(false);
  readonly cargando = signal(false);

  readonly emailForm = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
  });

  readonly resetForm = this.fb.nonNullable.group(
    {
      nuevaPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmar: ['', Validators.required],
    },
    { validators: (g) => (g.get('nuevaPassword')?.value === g.get('confirmar')?.value ? null : { contrasenaDistinta: true }) }
  );

  ngOnInit(): void {
    this.token.set(this.route.snapshot.queryParamMap.get('token') || '');
  }

  solicitar(): void {
    if (this.emailForm.invalid) {
      this.toast.error('Ingresá un email válido.');
      return;
    }
    this.cargando.set(true);
    const email = this.emailForm.getRawValue().email.trim();
    this.api.post('/recuperar/solicitar', { email }).subscribe({
      next: () => {
        this.cargando.set(false);
        this.enviado.set(true);
      },
      error: (err) => {
        this.cargando.set(false);
        this.toast.error(apiError(err));
      },
    });
  }

  restablecer(): void {
    if (this.resetForm.invalid) {
      this.toast.error('La contraseña debe tener al menos 6 caracteres y coincidir.');
      return;
    }
    this.cargando.set(true);
    this.api.post('/recuperar/restablecer', {
      token: this.token(),
      nuevaPassword: this.resetForm.getRawValue().nuevaPassword,
    }).subscribe({
      next: () => {
        this.cargando.set(false);
        sessionStorage.setItem('lfm_msg_pass_changed', '1');
        this.router.navigate(['/auth'], { replaceUrl: true });
      },
      error: (err) => {
        this.cargando.set(false);
        this.toast.error(apiError(err));
      },
    });
  }
}
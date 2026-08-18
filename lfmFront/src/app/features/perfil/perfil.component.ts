import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { firstValueFrom } from 'rxjs';

import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBarModule } from '@angular/material/snack-bar';

import { AuthService } from '../../core/auth/auth.service';
import { UsuarioService } from '../../core/api/usuario.service';
import { CarreraService } from '../../core/api/carrera.service';
import { InscripcionService } from '../../core/api/inscripcion.service';
import { LogroService } from '../../core/api/logro.service';
import { ErrorHandlerService } from '../../core/error/error-handler.service';

import { UsuarioResponse } from '../../core/models/usuario.model';
import { InscripcionResponse } from '../../core/models/inscripcion.model';
import { UsuarioLogroResponse, UsuarioRecompensaResponse } from '../../core/models/logro.model';
import { CarreraResponse } from '../../core/models/carrera.model';

@Component({
  selector: 'app-perfil',
  imports: [
    DatePipe,
    FormsModule,
    ReactiveFormsModule,
    MatCardModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatTabsModule,
    MatTableModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSnackBarModule,
  ],
  templateUrl: './perfil.component.html',
  styleUrl: './perfil.component.scss',
})
export class PerfilComponent {
  private readonly auth = inject(AuthService);
  private readonly fb = inject(FormBuilder);
  private readonly usuarioService = inject(UsuarioService);
  private readonly carreraService = inject(CarreraService);
  private readonly inscripcionService = inject(InscripcionService);
  private readonly logroService = inject(LogroService);
  private readonly errorHandler = inject(ErrorHandlerService);

  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly usuario = signal<UsuarioResponse | null>(null);
  readonly inscripciones = signal<InscripcionResponse[]>([]);
  readonly recompensas = signal<UsuarioRecompensaResponse[]>([]);
  readonly logros = signal<UsuarioLogroResponse[]>([]);
  readonly carreras = signal<Record<number, string>>({});

  readonly columnasInscripciones = ['carrera', 'fecha', 'estado'];
  readonly columnasRecompensas = ['descripcion', 'tipo', 'estado', 'accion'];

  readonly formPerfil = this.fb.nonNullable.group({
    nombrePiloto: ['', Validators.required],
    fotoPerfil: [''],
  });

  readonly formPassword = this.fb.nonNullable.group({
    passwordActual: ['', Validators.required],
    nuevaPassword: ['', [Validators.required, Validators.minLength(6)]],
  });

  readonly formSteam = this.fb.nonNullable.group({
    guidSteam: ['', Validators.required],
  });

  private usuarioId = 0;

  constructor() {
    const usuario = this.auth.usuario();
    if (!usuario) {
      return;
    }
    this.usuarioId = usuario.id;
    void this.cargar();
  }

  private async cargar() {
    this.cargando.set(true);
    try {
      const [usuario, inscripciones, recompensas, carreras, logros] = await Promise.all([
        firstValueFrom(this.usuarioService.obtener(this.usuarioId)),
        firstValueFrom(this.inscripcionService.deUsuario(this.usuarioId)),
        firstValueFrom(this.logroService.recompensasPorUsuario(this.usuarioId)),
        firstValueFrom(this.carreraService.listar()),
        firstValueFrom(this.logroService.progresoPorUsuario(this.usuarioId)),
      ]);
      this.usuario.set(usuario);
      this.inscripciones.set(inscripciones);
      this.recompensas.set(recompensas);
      this.logros.set(logros);
      this.carreras.set(Object.fromEntries(carreras.map((c: CarreraResponse) => [c.id, c.nombre])));
      this.formPerfil.patchValue({
        nombrePiloto: usuario.nombrePiloto ?? '',
        fotoPerfil: usuario.fotoPerfil ?? '',
      });
    } catch (error) {
      this.errorHandler.handle(error);
    } finally {
      this.cargando.set(false);
    }
  }

  nombreCarrera(carreraId: number): string {
    return this.carreras()[carreraId] ?? `Carrera #${carreraId}`;
  }

  async guardarPerfil() {
    if (this.formPerfil.invalid) {
      return;
    }
    this.guardando.set(true);
    try {
      const usuario = await firstValueFrom(
        this.usuarioService.actualizarPerfil(this.usuarioId, this.formPerfil.value),
      );
      this.usuario.set(usuario);
      this.auth.guardarSesion(this.auth.token()!, usuario);
      this.errorHandler.exito('Perfil actualizado');
    } catch (error) {
      this.errorHandler.handle(error);
    } finally {
      this.guardando.set(false);
    }
  }

  async cambiarPassword() {
    if (this.formPassword.invalid) {
      return;
    }
    this.guardando.set(true);
    try {
      await firstValueFrom(
        this.usuarioService.cambiarPassword(this.usuarioId, this.formPassword.getRawValue()),
      );
      this.formPassword.reset();
      this.errorHandler.exito('Contrasena actualizada');
    } catch (error) {
      this.errorHandler.handle(error);
    } finally {
      this.guardando.set(false);
    }
  }

  async vincularSteam() {
    if (this.formSteam.invalid) {
      return;
    }
    this.guardando.set(true);
    try {
      const usuario = await firstValueFrom(
        this.usuarioService.vincularSteam(this.usuarioId, this.formSteam.getRawValue()),
      );
      this.usuario.set(usuario);
      this.auth.guardarSesion(this.auth.token()!, usuario);
      this.formSteam.reset();
      this.errorHandler.exito('Cuenta de Steam vinculada');
    } catch (error) {
      this.errorHandler.handle(error);
    } finally {
      this.guardando.set(false);
    }
  }

  async desvincularSteam() {
    this.guardando.set(true);
    try {
      const usuario = await firstValueFrom(this.usuarioService.desvincularSteam(this.usuarioId));
      this.usuario.set(usuario);
      this.auth.guardarSesion(this.auth.token()!, usuario);
      this.errorHandler.exito('Cuenta de Steam desvinculada');
    } catch (error) {
      this.errorHandler.handle(error);
    } finally {
      this.guardando.set(false);
    }
  }

  async reclamar(recompensa: UsuarioRecompensaResponse) {
    this.guardando.set(true);
    try {
      await firstValueFrom(this.logroService.reclamarRecompensa(this.usuarioId, recompensa.recompensaId));
      this.recompensas.update((list) =>
        list.map((r) =>
          r.recompensaId === recompensa.recompensaId ? { ...r, reclamada: true } : r,
        ),
      );
      this.errorHandler.exito('Recompensa reclamada');
    } catch (error) {
      this.errorHandler.handle(error);
    } finally {
      this.guardando.set(false);
    }
  }
}

import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { firstValueFrom } from 'rxjs';

import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatChipsModule } from '@angular/material/chips';
import { MatSliderModule } from '@angular/material/slider';

import { SetupService } from '../../core/api/setup.service';
import { UsuarioService } from '../../core/api/usuario.service';
import { AuthService } from '../../core/auth/auth.service';
import { ErrorHandlerService } from '../../core/error/error-handler.service';

import { SetupComentarioResponse, SetupResponse } from '../../core/models/setup.model';

@Component({
  selector: 'app-setup-detail',
  imports: [
    DatePipe,
    ReactiveFormsModule,
    FormsModule,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatFormFieldModule,
    MatInputModule,
    MatChipsModule,
    MatSliderModule,
  ],
  templateUrl: './setup-detail.component.html',
  styleUrl: './setup-detail.component.scss',
})
export class SetupDetailComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly setupService = inject(SetupService);
  private readonly usuarioService = inject(UsuarioService);
  private readonly errorHandler = inject(ErrorHandlerService);

  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly setup = signal<SetupResponse | null>(null);
  readonly comentarios = signal<SetupComentarioResponse[]>([]);
  readonly autores = signal<Record<number, string>>({});

  readonly estaLogueado = this.auth.isLoggedIn;

  readonly formComentario = this.fb.nonNullable.group({
    texto: ['', Validators.required],
  });

  readonly puntaje = signal(3);

  private setupId = 0;

  constructor() {
    this.setupId = Number(this.route.snapshot.paramMap.get('id'));
    void this.cargar();
  }

  private async cargar() {
    this.cargando.set(true);
    try {
      const [setup, comentarios, usuarios] = await Promise.all([
        firstValueFrom(this.setupService.obtener(this.setupId)),
        firstValueFrom(this.setupService.comentarios(this.setupId)),
        firstValueFrom(this.usuarioService.listar()),
      ]);
      this.setup.set(setup);
      this.comentarios.set(comentarios);
      this.autores.set(Object.fromEntries(usuarios.map((u) => [u.id, u.nombrePiloto ?? u.email])));
    } catch (error) {
      this.errorHandler.handle(error);
    } finally {
      this.cargando.set(false);
    }
  }

  autor(usuarioId: number): string {
    return this.autores()[usuarioId] ?? `Piloto #${usuarioId}`;
  }

  async comentar() {
    const usuario = this.auth.usuario();
    if (!usuario || this.formComentario.invalid) {
      return;
    }
    this.guardando.set(true);
    try {
      const comentario = await firstValueFrom(
        this.setupService.agregarComentario(this.setupId, {
          usuarioId: usuario.id,
          texto: this.formComentario.value.texto!,
        }),
      );
      this.comentarios.update((list) => [...list, comentario]);
      this.formComentario.reset();
    } catch (error) {
      this.errorHandler.handle(error);
    } finally {
      this.guardando.set(false);
    }
  }

  async calificar() {
    const usuario = this.auth.usuario();
    if (!usuario) {
      return;
    }
    this.guardando.set(true);
    try {
      await firstValueFrom(
        this.setupService.calificar(this.setupId, {
          usuarioId: usuario.id,
          puntaje: this.puntaje(),
        }),
      );
      this.errorHandler.exito('Calificacion guardada');
      await this.cargar();
    } catch (error) {
      this.errorHandler.handle(error);
    } finally {
      this.guardando.set(false);
    }
  }
}

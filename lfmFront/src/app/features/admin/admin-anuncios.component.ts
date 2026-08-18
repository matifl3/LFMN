import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { firstValueFrom } from 'rxjs';

import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

import { AnuncioService } from '../../core/api/anuncio.service';
import { ErrorHandlerService } from '../../core/error/error-handler.service';
import { AnuncioResponse } from '../../core/models/anuncio.model';

@Component({
  selector: 'app-admin-anuncios',
  imports: [
    ReactiveFormsModule,
    DatePipe,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  templateUrl: './admin-anuncios.component.html',
  styleUrl: './admin.component.scss',
})
export class AdminAnunciosComponent {
  private readonly fb = inject(FormBuilder);
  private readonly anuncioService = inject(AnuncioService);
  private readonly errorHandler = inject(ErrorHandlerService);

  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly anuncios = signal<AnuncioResponse[]>([]);

  readonly form = this.fb.nonNullable.group({
    titulo: ['', Validators.required],
    contenido: ['', Validators.required],
    urlImagen: [''],
  });

  constructor() {
    void this.cargar();
  }

  private async cargar() {
    this.cargando.set(true);
    try {
      this.anuncios.set(await firstValueFrom(this.anuncioService.listar()));
    } catch (error) {
      this.errorHandler.handle(error);
    } finally {
      this.cargando.set(false);
    }
  }

  async publicar() {
    if (this.form.invalid) {
      return;
    }
    this.guardando.set(true);
    try {
      const value = this.form.getRawValue();
      const anuncio = await firstValueFrom(
        this.anuncioService.crear({
          titulo: value.titulo,
          contenido: value.contenido,
          urlImagen: value.urlImagen || null,
        }),
      );
      this.anuncios.update((list) => [anuncio, ...list]);
      this.form.reset();
      this.errorHandler.exito('Anuncio publicado');
    } catch (error) {
      this.errorHandler.handle(error);
    } finally {
      this.guardando.set(false);
    }
  }

  async eliminar(anuncio: AnuncioResponse) {
    if (!window.confirm(`Seguro que queres eliminar el anuncio "${anuncio.titulo}"?`)) {
      return;
    }
    try {
      await firstValueFrom(this.anuncioService.eliminar(anuncio.id));
      this.anuncios.update((list) => list.filter((a) => a.id !== anuncio.id));
      this.errorHandler.exito('Anuncio eliminado');
    } catch (error) {
      this.errorHandler.handle(error);
    }
  }
}

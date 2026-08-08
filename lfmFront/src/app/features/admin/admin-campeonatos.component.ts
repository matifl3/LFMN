import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { firstValueFrom } from 'rxjs';

import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';

import { CampeonatoService } from '../../core/api/campeonato.service';
import { CategoriaService } from '../../core/api/categoria.service';
import { ErrorHandlerService } from '../../core/error/error-handler.service';

import { CampeonatoResponse, EstadoCampeonato } from '../../core/models/campeonato.model';
import { CategoriaResponse } from '../../core/models/categoria.model';

@Component({
  selector: 'app-admin-campeonatos',
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
  ],
  templateUrl: './admin-campeonatos.component.html',
  styleUrl: './admin.component.scss',
})
export class AdminCampeonatosComponent {
  private readonly fb = inject(FormBuilder);
  private readonly campeonatoService = inject(CampeonatoService);
  private readonly categoriaService = inject(CategoriaService);
  private readonly errorHandler = inject(ErrorHandlerService);

  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly campeonatos = signal<CampeonatoResponse[]>([]);
  readonly categorias = signal<CategoriaResponse[]>([]);
  readonly editandoId = signal<number | null>(null);

  readonly estados = [EstadoCampeonato.ACTIVO, EstadoCampeonato.CERRADO];

  readonly ESTADO_CERRADO = EstadoCampeonato.CERRADO;

  readonly columnas = ['nombre', 'temporada', 'categoria', 'estado', 'acciones'];

  readonly form = this.fb.nonNullable.group({
    nombre: ['', Validators.required],
    temporada: [''],
    categoriaId: [0, Validators.required],
    estado: [EstadoCampeonato.ACTIVO],
    sistemaPuntos: [''],
  });

  constructor() {
    void this.cargar();
  }

  private async cargar() {
    this.cargando.set(true);
    try {
      const [campeonatos, categorias] = await Promise.all([
        firstValueFrom(this.campeonatoService.listar()),
        firstValueFrom(this.categoriaService.listar()),
      ]);
      this.campeonatos.set(campeonatos);
      this.categorias.set(categorias);
    } catch (error) {
      this.errorHandler.handle(error);
    } finally {
      this.cargando.set(false);
    }
  }

  nueva() {
    this.editandoId.set(null);
    this.form.reset({ estado: EstadoCampeonato.ACTIVO });
  }

  editar(campeonato: CampeonatoResponse) {
    this.editandoId.set(campeonato.id);
    this.form.patchValue({
      nombre: campeonato.nombre,
      temporada: campeonato.temporada ?? '',
      categoriaId: campeonato.categoriaId,
      estado: campeonato.estado,
      sistemaPuntos: campeonato.sistemaPuntos ?? '',
    });
  }

  async guardar() {
    if (this.form.invalid) {
      return;
    }
    this.guardando.set(true);
    try {
      const value = this.form.getRawValue();
      if (this.editandoId() !== null) {
        await firstValueFrom(this.campeonatoService.actualizar(this.editandoId()!, value));
      } else {
        await firstValueFrom(this.campeonatoService.crear(value));
      }
      this.errorHandler.exito('Campeonato guardado');
      this.form.reset({ estado: EstadoCampeonato.ACTIVO });
      this.editandoId.set(null);
      await this.cargar();
    } catch (error) {
      this.errorHandler.handle(error);
    } finally {
      this.guardando.set(false);
    }
  }

  async cerrar(campeonato: CampeonatoResponse) {
    if (!window.confirm(`Seguro que queres cerrar el campeonato "${campeonato.nombre}"?`)) {
      return;
    }
    try {
      await firstValueFrom(this.campeonatoService.cerrar(campeonato.id));
      await this.cargar();
      this.errorHandler.exito('Campeonato cerrado');
    } catch (error) {
      this.errorHandler.handle(error);
    }
  }

  async eliminar(campeonato: CampeonatoResponse) {
    if (!window.confirm(`Seguro que queres eliminar el campeonato "${campeonato.nombre}"?`)) {
      return;
    }
    try {
      await firstValueFrom(this.campeonatoService.eliminar(campeonato.id));
      this.campeonatos.update((list) => list.filter((c) => c.id !== campeonato.id));
      this.errorHandler.exito('Campeonato eliminado');
    } catch (error) {
      this.errorHandler.handle(error);
    }
  }

  cancelarEdicion() {
    this.editandoId.set(null);
    this.form.reset();
  }
}

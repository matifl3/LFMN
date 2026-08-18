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
import { MatCheckboxModule } from '@angular/material/checkbox';

import { CategoriaService } from '../../core/api/categoria.service';
import { ErrorHandlerService } from '../../core/error/error-handler.service';
import { CategoriaResponse } from '../../core/models/categoria.model';

@Component({
  selector: 'app-admin-categorias',
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatFormFieldModule,
    MatInputModule,
    MatCheckboxModule,
  ],
  templateUrl: './admin-categorias.component.html',
  styleUrl: './admin.component.scss',
})
export class AdminCategoriasComponent {
  private readonly fb = inject(FormBuilder);
  private readonly categoriaService = inject(CategoriaService);
  private readonly errorHandler = inject(ErrorHandlerService);

  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly categorias = signal<CategoriaResponse[]>([]);
  readonly editandoId = signal<number | null>(null);

  readonly columnas = ['nombre', 'descripcion', 'elo', 'setup', 'acciones'];

  readonly form = this.fb.nonNullable.group({
    nombre: ['', Validators.required],
    descripcion: [''],
    eloMinimo: [null as number | null],
    eloMaximo: [null as number | null],
    setupAbierto: [false],
    setupFijo: [false],
  });

  constructor() {
    void this.cargar();
  }

  private async cargar() {
    this.cargando.set(true);
    try {
      this.categorias.set(await firstValueFrom(this.categoriaService.listar()));
    } catch (error) {
      this.errorHandler.handle(error);
    } finally {
      this.cargando.set(false);
    }
  }

  nueva() {
    this.editandoId.set(null);
    this.form.reset({ setupAbierto: false, setupFijo: false });
  }

  editar(categoria: CategoriaResponse) {
    this.editandoId.set(categoria.id);
    this.form.patchValue({
      nombre: categoria.nombre,
      descripcion: categoria.descripcion ?? '',
      eloMinimo: categoria.eloMinimo,
      eloMaximo: categoria.eloMaximo,
      setupAbierto: categoria.setupAbierto ?? false,
      setupFijo: categoria.setupFijo ?? false,
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
        await firstValueFrom(this.categoriaService.actualizar(this.editandoId()!, value));
      } else {
        await firstValueFrom(this.categoriaService.crear(value));
      }
      this.errorHandler.exito('Categoria guardada');
      this.form.reset();
      this.editandoId.set(null);
      await this.cargar();
    } catch (error) {
      this.errorHandler.handle(error);
    } finally {
      this.guardando.set(false);
    }
  }

  async eliminar(categoria: CategoriaResponse) {
    if (!window.confirm(`Seguro que queres eliminar la categoria "${categoria.nombre}"?`)) {
      return;
    }
    try {
      await firstValueFrom(this.categoriaService.eliminar(categoria.id));
      this.categorias.update((list) => list.filter((c) => c.id !== categoria.id));
      this.errorHandler.exito('Categoria eliminada');
    } catch (error) {
      this.errorHandler.handle(error);
    }
  }

  cancelarEdicion() {
    this.editandoId.set(null);
    this.form.reset();
  }
}

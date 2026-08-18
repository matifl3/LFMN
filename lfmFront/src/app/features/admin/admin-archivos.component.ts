import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
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

import { ArchivoService } from '../../core/api/archivo.service';
import { ErrorHandlerService } from '../../core/error/error-handler.service';
import { ArchivoCarreraResponse, TipoArchivo } from '../../core/models/archivo.model';

@Component({
  selector: 'app-admin-archivos',
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
  templateUrl: './admin-archivos.component.html',
  styleUrl: './admin.component.scss',
})
export class AdminArchivosComponent {
  private readonly fb = inject(FormBuilder);
  private readonly archivoService = inject(ArchivoService);
  private readonly errorHandler = inject(ErrorHandlerService);

  readonly cargando = signal(true);
  readonly subiendo = signal(false);
  readonly archivos = signal<ArchivoCarreraResponse[]>([]);
  readonly tipos = Object.values(TipoArchivo);
  readonly archivoSeleccionado = signal<File | null>(null);

  readonly columnas = ['nombre', 'tipo', 'descarga', 'acciones'];

  readonly form = this.fb.nonNullable.group({
    nombre: ['', Validators.required],
    tipo: [TipoArchivo.OTRO, Validators.required],
  });

  constructor() {
    void this.cargar();
  }

  private async cargar() {
    this.cargando.set(true);
    try {
      this.archivos.set(await firstValueFrom(this.archivoService.listar()));
    } catch (error) {
      this.errorHandler.handle(error);
    } finally {
      this.cargando.set(false);
    }
  }

  onArchivoSeleccionado(event: Event) {
    const input = event.target as HTMLInputElement;
    this.archivoSeleccionado.set(input.files?.item(0) ?? null);
  }

  async subir() {
    const archivo = this.archivoSeleccionado();
    if (this.form.invalid || !archivo) {
      return;
    }
    this.subiendo.set(true);
    try {
      const value = this.form.getRawValue();
      await firstValueFrom(this.archivoService.subir(value.nombre, value.tipo, archivo));
      this.errorHandler.exito('Archivo subido');
      this.form.reset({ tipo: TipoArchivo.OTRO });
      this.archivoSeleccionado.set(null);
      await this.cargar();
    } catch (error) {
      this.errorHandler.handle(error);
    } finally {
      this.subiendo.set(false);
    }
  }

  async eliminar(archivo: ArchivoCarreraResponse) {
    if (!window.confirm(`Seguro que queres eliminar el archivo "${archivo.nombre}"?`)) {
      return;
    }
    try {
      await firstValueFrom(this.archivoService.eliminar(archivo.id));
      this.archivos.update((list) => list.filter((a) => a.id !== archivo.id));
      this.errorHandler.exito('Archivo eliminado');
    } catch (error) {
      this.errorHandler.handle(error);
    }
  }

  urlDescarga(archivo: ArchivoCarreraResponse): string {
    return this.archivoService.urlDescarga(archivo.id);
  }
}

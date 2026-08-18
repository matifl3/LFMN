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

import { CarreraService } from '../../core/api/carrera.service';
import { CategoriaService } from '../../core/api/categoria.service';
import { CampeonatoService } from '../../core/api/campeonato.service';
import { ArchivoService } from '../../core/api/archivo.service';
import { ErrorHandlerService } from '../../core/error/error-handler.service';

import { CarreraResponse, EstadoCarrera } from '../../core/models/carrera.model';
import { CategoriaResponse } from '../../core/models/categoria.model';
import { CampeonatoResponse } from '../../core/models/campeonato.model';
import { ArchivoCarreraResponse } from '../../core/models/archivo.model';

@Component({
  selector: 'app-admin-carreras',
  imports: [
    ReactiveFormsModule,
    DatePipe,
    MatCardModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
  ],
  templateUrl: './admin-carreras.component.html',
  styleUrl: './admin.component.scss',
})
export class AdminCarrerasComponent {
  private readonly fb = inject(FormBuilder);
  private readonly carreraService = inject(CarreraService);
  private readonly categoriaService = inject(CategoriaService);
  private readonly campeonatoService = inject(CampeonatoService);
  private readonly archivoService = inject(ArchivoService);
  private readonly errorHandler = inject(ErrorHandlerService);

  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly carreras = signal<CarreraResponse[]>([]);
  readonly categorias = signal<CategoriaResponse[]>([]);
  readonly campeonatos = signal<CampeonatoResponse[]>([]);
  readonly archivos = signal<ArchivoCarreraResponse[]>([]);
  readonly editandoId = signal<number | null>(null);

  readonly estados = [
    EstadoCarrera.PROGRAMADA,
    EstadoCarrera.INSCRIPCIONES_ABIERTAS,
    EstadoCarrera.INSCRIPCIONES_CERRADAS,
    EstadoCarrera.EN_CURSO,
    EstadoCarrera.FINALIZADA,
  ];

  readonly columnas = ['nombre', 'fecha', 'campeonato', 'estado', 'acciones'];

  readonly form = this.fb.nonNullable.group({
    nombre: ['', Validators.required],
    fecha: ['', Validators.required],
    circuito: ['', Validators.required],
    categoriaId: [0, Validators.required],
    campeonatoId: [0, Validators.required],
    estado: [EstadoCarrera.PROGRAMADA],
    cupoMaximo: [null as number | null],
    servidor: [''],
    contrasenaServidor: [''],
    archivoId: [null as number | null],
  });

  constructor() {
    void this.cargar();
  }

  private async cargar() {
    this.cargando.set(true);
    try {
      const [carreras, categorias, archivos] = await Promise.all([
        firstValueFrom(this.carreraService.listar()),
        firstValueFrom(this.categoriaService.listar()),
        firstValueFrom(this.archivoService.listar()),
      ]);
      this.carreras.set(carreras);
      this.categorias.set(categorias);
      this.archivos.set(archivos);
    } catch (error) {
      this.errorHandler.handle(error);
    } finally {
      this.cargando.set(false);
    }
  }

  nueva() {
    this.editandoId.set(null);
    this.campeonatos.set([]);
    this.form.reset({ categoriaId: 0, campeonatoId: 0, estado: EstadoCarrera.PROGRAMADA });
  }

  editar(carrera: CarreraResponse) {
    this.editandoId.set(carrera.id);
    this.form.patchValue({
      nombre: carrera.nombre,
      fecha: this.toLocalInput(carrera.fecha),
      circuito: carrera.circuito,
      categoriaId: carrera.categoriaId,
      campeonatoId: carrera.campeonatoId,
      estado: carrera.estado,
      cupoMaximo: carrera.cupoMaximo,
      servidor: carrera.servidor ?? '',
      contrasenaServidor: carrera.contrasenaServidor ?? '',
      archivoId: carrera.archivoId,
    });
    this.cargarCampeonatos(carrera.categoriaId);
  }

  private toLocalInput(iso: string): string {
    return iso.replace('T', ' ').slice(0, 16);
  }

  async onCategoriaChange(categoriaId: number) {
    this.form.patchValue({ campeonatoId: 0 });
    if (!categoriaId) {
      this.campeonatos.set([]);
      return;
    }
    try {
      const campeonatos = await firstValueFrom(this.campeonatoService.porCategoria(categoriaId));
      this.campeonatos.set(campeonatos);
    } catch (error) {
      this.errorHandler.handle(error);
    }
  }

  private async cargarCampeonatos(categoriaId: number) {
    if (!categoriaId) {
      this.campeonatos.set([]);
      return;
    }
    try {
      const campeonatos = await firstValueFrom(this.campeonatoService.porCategoria(categoriaId));
      this.campeonatos.set(campeonatos);
    } catch (error) {
      this.errorHandler.handle(error);
    }
  }

  async guardar() {
    if (this.form.invalid) {
      return;
    }
    this.guardando.set(true);
    try {
      const value = this.form.getRawValue();
      const request = {
        nombre: value.nombre,
        fecha: value.fecha.replace(' ', 'T'),
        circuito: value.circuito,
        campeonatoId: value.campeonatoId,
        estado: value.estado,
        cupoMaximo: value.cupoMaximo,
        servidor: value.servidor,
        contrasenaServidor: value.contrasenaServidor,
        archivoId: value.archivoId,
      };
      if (this.editandoId() !== null) {
        await firstValueFrom(this.carreraService.actualizar(this.editandoId()!, request));
      } else {
        await firstValueFrom(this.carreraService.crear(request));
      }
      this.errorHandler.exito('Carrera guardada');
      this.form.reset({ categoriaId: 0, campeonatoId: 0, estado: EstadoCarrera.PROGRAMADA });
      this.editandoId.set(null);
      this.campeonatos.set([]);
      await this.cargar();
    } catch (error) {
      this.errorHandler.handle(error);
    } finally {
      this.guardando.set(false);
    }
  }

  async cambiarEstado(carrera: CarreraResponse, estado: EstadoCarrera) {
    try {
      await firstValueFrom(this.carreraService.cambiarEstado(carrera.id, estado));
      this.carreras.update((list) =>
        list.map((c) => (c.id === carrera.id ? { ...c, estado } : c)),
      );
      this.errorHandler.exito('Estado actualizado');
    } catch (error) {
      this.errorHandler.handle(error);
    }
  }

  async cancelar(carrera: CarreraResponse) {
    if (!window.confirm(`Seguro que queres cancelar la carrera "${carrera.nombre}"?`)) {
      return;
    }
    try {
      await firstValueFrom(this.carreraService.cancelar(carrera.id));
      await this.cargar();
      this.errorHandler.exito('Carrera cancelada');
    } catch (error) {
      this.errorHandler.handle(error);
    }
  }

  async eliminar(carrera: CarreraResponse) {
    if (!window.confirm(`Seguro que queres eliminar la carrera "${carrera.nombre}"?`)) {
      return;
    }
    try {
      await firstValueFrom(this.carreraService.eliminar(carrera.id));
      this.carreras.update((list) => list.filter((c) => c.id !== carrera.id));
      this.errorHandler.exito('Carrera eliminada');
    } catch (error) {
      this.errorHandler.handle(error);
    }
  }

  cancelarEdicion() {
    this.editandoId.set(null);
    this.campeonatos.set([]);
    this.form.reset();
  }
}

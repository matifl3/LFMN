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
import { MatChipsModule } from '@angular/material/chips';

import { LogroService } from '../../core/api/logro.service';
import { ErrorHandlerService } from '../../core/error/error-handler.service';

import {
  LogroResponse,
  LogroRequest,
  RecompensaRequest,
  TipoCondicionLogro,
  TipoRecompensa,
} from '../../core/models/logro.model';

@Component({
  selector: 'app-admin-logros',
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
    MatChipsModule,
  ],
  templateUrl: './admin-logros.component.html',
  styleUrl: './admin.component.scss',
})
export class AdminLogrosComponent {
  private readonly fb = inject(FormBuilder);
  private readonly logroService = inject(LogroService);
  private readonly errorHandler = inject(ErrorHandlerService);

  readonly cargando = signal(true);
  readonly guardando = signal(false);
  readonly logros = signal<LogroResponse[]>([]);
  readonly editandoId = signal<number | null>(null);

  readonly tiposCondicion = Object.values(TipoCondicionLogro);
  readonly tiposRecompensa = Object.values(TipoRecompensa);

  readonly columnas = ['nombre', 'condicion', 'recompensas', 'acciones'];

  readonly form = this.fb.nonNullable.group({
    nombre: ['', Validators.required],
    descripcion: [''],
    tipoCondicion: [TipoCondicionLogro.CARRERAS, Validators.required],
    valorCondicion: [1, [Validators.required, Validators.min(1)]],
    icono: ['military_tech'],
  });

  readonly formRecompensa = this.fb.nonNullable.group({
    descripcion: ['', Validators.required],
    tipo: [TipoRecompensa.VIRTUAL, Validators.required],
  });

  constructor() {
    void this.cargar();
  }

  private async cargar() {
    this.cargando.set(true);
    try {
      this.logros.set(await firstValueFrom(this.logroService.listar()));
    } catch (error) {
      this.errorHandler.handle(error);
    } finally {
      this.cargando.set(false);
    }
  }

  nueva() {
    this.editandoId.set(null);
    this.form.reset({
      tipoCondicion: TipoCondicionLogro.CARRERAS,
      valorCondicion: 1,
      icono: 'military_tech',
    });
  }

  editar(logro: LogroResponse) {
    this.editandoId.set(logro.id);
    this.form.patchValue({
      nombre: logro.nombre,
      descripcion: logro.descripcion,
      tipoCondicion: logro.tipoCondicion,
      valorCondicion: logro.valorCondicion,
      icono: logro.icono,
    });
  }

  async guardar() {
    if (this.form.invalid) {
      return;
    }
    this.guardando.set(true);
    try {
      const value = this.form.getRawValue() as LogroRequest;
      if (this.editandoId() !== null) {
        await firstValueFrom(this.logroService.actualizar(this.editandoId()!, value));
      } else {
        await firstValueFrom(this.logroService.crear(value));
      }
      this.errorHandler.exito('Logro guardado');
      this.form.reset({ tipoCondicion: TipoCondicionLogro.CARRERAS, valorCondicion: 1, icono: 'military_tech' });
      this.editandoId.set(null);
      await this.cargar();
    } catch (error) {
      this.errorHandler.handle(error);
    } finally {
      this.guardando.set(false);
    }
  }

  async agregarRecompensa(logro: LogroResponse) {
    if (this.formRecompensa.invalid) {
      return;
    }
    this.guardando.set(true);
    try {
      const value = this.formRecompensa.getRawValue() as RecompensaRequest;
      const recompensa = await firstValueFrom(this.logroService.agregarRecompensa(logro.id, value));
      this.logros.update((list) =>
        list.map((l) =>
          l.id === logro.id ? { ...l, recompensas: [...l.recompensas, recompensa] } : l,
        ),
      );
      this.formRecompensa.reset({ tipo: TipoRecompensa.VIRTUAL });
    } catch (error) {
      this.errorHandler.handle(error);
    } finally {
      this.guardando.set(false);
    }
  }

  async quitarRecompensa(logro: LogroResponse, recompensaId: number) {
    try {
      await firstValueFrom(this.logroService.quitarRecompensa(logro.id, recompensaId));
      this.logros.update((list) =>
        list.map((l) =>
          l.id === logro.id
            ? { ...l, recompensas: l.recompensas.filter((r) => r.id !== recompensaId) }
            : l,
        ),
      );
    } catch (error) {
      this.errorHandler.handle(error);
    }
  }

  async eliminar(logro: LogroResponse) {
    if (!window.confirm(`Seguro que queres eliminar el logro "${logro.nombre}"?`)) {
      return;
    }
    try {
      await firstValueFrom(this.logroService.eliminar(logro.id));
      this.logros.update((list) => list.filter((l) => l.id !== logro.id));
      this.errorHandler.exito('Logro eliminado');
    } catch (error) {
      this.errorHandler.handle(error);
    }
  }

  cancelarEdicion() {
    this.editandoId.set(null);
    this.form.reset();
  }
}

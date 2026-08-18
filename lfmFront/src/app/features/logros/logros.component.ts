import { Component, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';

import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';

import { LogroService } from '../../core/api/logro.service';
import { ErrorHandlerService } from '../../core/error/error-handler.service';
import { LogroResponse } from '../../core/models/logro.model';

@Component({
  selector: 'app-logros',
  imports: [MatCardModule, MatIconModule, MatProgressSpinnerModule, MatChipsModule],
  templateUrl: './logros.component.html',
  styleUrl: './logros.component.scss',
})
export class LogrosComponent {
  private readonly logroService = inject(LogroService);
  private readonly errorHandler = inject(ErrorHandlerService);

  readonly cargando = signal(true);
  readonly logros = signal<LogroResponse[]>([]);

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
}

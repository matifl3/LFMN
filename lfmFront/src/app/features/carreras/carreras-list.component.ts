import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';

import { MatTabsModule } from '@angular/material/tabs';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { CarreraService } from '../../core/api/carrera.service';
import { CarreraResponse } from '../../core/models/carrera.model';
import { EstadoCarrera } from '../../core/models/carrera.model';

@Component({
  selector: 'app-carreras-list',
  imports: [
    RouterLink,
    DatePipe,
    MatTabsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './carreras-list.component.html',
  styleUrl: './carreras-list.component.scss',
})
export class CarrerasListComponent {
  private readonly carreraService = inject(CarreraService);

  readonly cargando = signal(true);
  readonly proximas = signal<CarreraResponse[]>([]);
  readonly pasadas = signal<CarreraResponse[]>([]);

  constructor() {
    void this.cargar();
  }

  private async cargar() {
    this.cargando.set(true);
    try {
      const [proximas, pasadas] = await Promise.all([
        firstValueFrom(this.carreraService.proximas()),
        firstValueFrom(this.carreraService.pasadas()),
      ]);
      this.proximas.set(proximas);
      this.pasadas.set(pasadas);
    } finally {
      this.cargando.set(false);
    }
  }

  abierta(carrera: CarreraResponse): boolean {
    return carrera.estado === EstadoCarrera.INSCRIPCIONES_ABIERTAS;
  }
}

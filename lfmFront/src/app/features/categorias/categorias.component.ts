import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';

import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { CategoriaService } from '../../core/api/categoria.service';
import { CategoriaResponse } from '../../core/models/categoria.model';

@Component({
  selector: 'app-categorias',
  imports: [RouterLink, MatCardModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './categorias.component.html',
  styleUrl: './categorias.component.scss',
})
export class CategoriasComponent {
  private readonly categoriaService = inject(CategoriaService);

  readonly cargando = signal(true);
  readonly categorias = signal<CategoriaResponse[]>([]);

  constructor() {
    void this.cargar();
  }

  private async cargar() {
    this.cargando.set(true);
    try {
      this.categorias.set(await firstValueFrom(this.categoriaService.listar()));
    } finally {
      this.cargando.set(false);
    }
  }
}

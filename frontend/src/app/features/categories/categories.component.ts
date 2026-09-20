import { Component, OnInit, inject, signal } from '@angular/core';
import { DecimalPipe, registerLocaleData } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import localeEsAr from '@angular/common/locales/es-AR';
import { ApiService } from '../../core/services/api.service';
import { EmptyState } from '../../shared/components/empty-state/empty-state';
import { Categoria, UsuarioBasico } from '../../core/models/models';

registerLocaleData(localeEsAr, 'es-AR');

@Component({
  selector: 'app-categories',
  standalone: true,
  imports: [RouterLink, EmptyState, DecimalPipe],
  styleUrl: './categories.scss',
  templateUrl: './categories.html',
})
export class CategoriesComponent implements OnInit {
  private readonly api = inject(ApiService);

  readonly cats = signal<Categoria[]>([]);
  readonly users = signal<UsuarioBasico[]>([]);
  readonly cargando = signal(true);

  ngOnInit(): void {
    forkJoin({
      cats: this.api.list<Categoria>('/categorias').pipe(catchError(() => of([]))),
      users: this.api.list<UsuarioBasico>('/usuarios/basico').pipe(catchError(() => of([]))),
    }).subscribe((r) => {
      this.cats.set(r.cats);
      this.users.set(r.users);
      this.cargando.set(false);
    });
  }

  countCat(c: Categoria): number {
    if (c.eloMinimo == null || c.eloMaximo == null) return 0;
    return this.users().filter(
      (u) => u.elo != null && u.elo >= c.eloMinimo! && u.elo <= c.eloMaximo!
    ).length;
  }
}
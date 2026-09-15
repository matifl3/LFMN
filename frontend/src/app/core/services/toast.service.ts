import { Injectable, signal } from '@angular/core';

export interface ToastItem {
  id: number;
  mensaje: string;
  tipo: 'success' | 'error' | '';
  duracion: number;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  readonly items = signal<ToastItem[]>([]);
  private nextId = 1;

  mostrar(mensaje: string, tipo: 'success' | 'error' | '' = '', duracion = 4000): void {
    const id = this.nextId++;
    const item: ToastItem = { id, mensaje, tipo, duracion };
    this.items.update((l) => [...l, item]);
    setTimeout(() => this.quitar(id), duracion);
  }

  success(mensaje: string): void {
    this.mostrar(mensaje, 'success');
  }

  error(mensaje: string): void {
    this.mostrar(mensaje, 'error');
  }

  quitar(id: number): void {
    this.items.update((l) => l.filter((t) => t.id !== id));
  }
}
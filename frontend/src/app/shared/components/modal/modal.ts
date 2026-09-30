import { Component, ElementRef, HostListener, effect, inject, input, output } from '@angular/core';

let modalUid = 0;

@Component({
  selector: 'app-modal',
  standalone: true,
  template: `
    @if (abierto()) {
      <div
        class="modal-overlay"
        role="dialog"
        aria-modal="true"
        [attr.aria-labelledby]="titulo() ? tituloId : null"
        (click)="cerrarConOverlay($event)"
      >
        <div class="modal-panel" tabindex="-1" (click)="$event.stopPropagation()">
          <div class="modal-header">
            @if (titulo()) {
              <h3 [id]="tituloId">{{ titulo() }}</h3>
            } @else {
              <ng-content select="[app-modal-titulo]" />
            }
            <button type="button" class="icon-btn modal-close" aria-label="Cerrar" (click)="cerrar()">
              ✕
            </button>
          </div>
          <ng-content />
        </div>
      </div>
    }
  `,
})
export class Modal {
  readonly abierto = input(false);
  readonly titulo = input<string | null>(null);
  readonly clickFueraCierra = input(true);
  readonly cerrado = output<void>();

  private readonly el = inject(ElementRef);
  readonly tituloId = `modal-titulo-${++modalUid}`;
  private focoPrevio: HTMLElement | null = null;

  constructor() {
    effect(() => {
      if (this.abierto()) {
        this.focoPrevio = document.activeElement as HTMLElement | null;
        requestAnimationFrame(() => this.focusInicial());
      } else {
        this.focoPrevio?.focus();
        this.focoPrevio = null;
      }
    });
  }

  private focusables(): HTMLElement[] {
    const panel = this.el.nativeElement.querySelector('.modal-panel') as HTMLElement | null;
    if (!panel) return [];
    return Array.from(
      panel.querySelectorAll<HTMLElement>(
        'a[href], button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])',
      ),
    );
  }

  private focusInicial(): void {
    const f = this.focusables();
    const panel = this.el.nativeElement.querySelector('.modal-panel') as HTMLElement | null;
    (f[0] ?? panel)?.focus({ preventScroll: true });
  }

  cerrar(): void {
    this.cerrado.emit();
  }

  cerrarConOverlay(e: MouseEvent): void {
    if (this.clickFueraCierra()) this.cerrado.emit();
  }

  @HostListener('document:keydown', ['$event'])
  onKey(e: KeyboardEvent): void {
    if (!this.abierto()) return;
    if (e.key === 'Escape') {
      e.stopPropagation();
      this.cerrado.emit();
      return;
    }
    if (e.key === 'Tab') {
      const f = this.focusables();
      if (f.length === 0) return;
      const active = document.activeElement as HTMLElement;
      const first = f[0];
      const last = f[f.length - 1];
      if (e.shiftKey && (active === first || active === this.el.nativeElement)) {
        e.preventDefault();
        last.focus();
      } else if (!e.shiftKey && active === last) {
        e.preventDefault();
        first.focus();
      }
    }
  }
}
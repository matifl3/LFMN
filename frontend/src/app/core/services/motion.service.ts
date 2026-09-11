import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class MotionService {
  private readonly duracionConteo = 900;
  private readonly selectorStagger =
    '.race-row, .feed-item, tbody tr, .entrant-row, .file-row, .comment, .vote-row';
  private readonly revealAuto =
    '.hero-content, .section-head, .empty-state, .detail-header, .profile-header, .info-grid > *, .stat-grid > *, .admin-layout > *';

  private io: IntersectionObserver | null = null;
  private mo: MutationObserver | null = null;

  iniciar(): void {
    if (typeof window === 'undefined') return;
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return;

    this.io = 'IntersectionObserver' in window
      ? new IntersectionObserver((entries) => this.alIntersectar(entries), { threshold: 0.12, rootMargin: '0px 0px -40px 0px' })
      : null;

    this.escaneo(document);

    if (document.body && 'MutationObserver' in window) {
      this.mo = new MutationObserver((mutations) => {
        for (const mutation of mutations) {
          for (const nodo of Array.from(mutation.addedNodes)) {
            if (nodo.nodeType === 1) this.escaneo(nodo as Element);
          }
        }
      });
      this.mo.observe(document.body, { childList: true, subtree: true });
    }
  }

  revelar(el: HTMLElement): void {
    el.classList.add('reveal');
    this.observar(el);
  }

  stagger(contenedor: HTMLElement): void {
    this.staggerLista(contenedor);
  }

  contar(el: HTMLElement): void {
    this.conteo(el);
  }

  private alIntersectar(entries: IntersectionObserverEntry[]): void {
    for (const entry of entries) {
      if (!entry.isIntersecting) continue;
      const el = entry.target as HTMLElement;
      if (el.classList.contains('reveal')) el.classList.add('is-in');
      if (el.classList.contains('count-up')) this.conteo(el);
      this.io?.unobserve(el);
    }
  }

  private conteo(el: HTMLElement): void {
    const objetivo = this.numero(el);
    if (Number.isNaN(objetivo)) return;
    if (el.classList.contains('counted')) return;
    el.classList.add('counted');
    const dec = (String(objetivo).split('.')[1] || '').length;
    const sufijo = el.getAttribute('data-suffix') || '';
    const prefijo = el.getAttribute('data-prefix') || '';
    const inicio = performance.now();
    const tick = (ahora: number) => {
      const p = Math.min(1, (ahora - inicio) / this.duracionConteo);
      const suavizado = 1 - Math.pow(1 - p, 3);
      el.textContent = prefijo + (objetivo * suavizado).toFixed(dec) + sufijo;
      if (p < 1) requestAnimationFrame(tick);
    };
    requestAnimationFrame(tick);
  }

  private numero(el: HTMLElement): number {
    const texto = (el.getAttribute('data-count') || el.textContent || '').replace(/\s/g, '').trim();
    return /^-?\d+(\.\d+)?$/.test(texto) ? parseFloat(texto) : NaN;
  }

  private observar(el: HTMLElement): void {
    if (this.io) {
      this.io.observe(el);
    } else {
      if (el.classList.contains('reveal')) el.classList.add('is-in');
      if (el.classList.contains('count-up')) this.conteo(el);
    }
  }

  private staggerLista(contenedor: HTMLElement): void {
    if (!contenedor || !contenedor.children) return;
    const items = Array.from(contenedor.children);
    for (let i = 0; i < items.length; i++) {
      const it = items[i] as HTMLElement;
      if (it.nodeType !== 1) continue;
      if (!contenedor.classList.contains('motion-list') && !it.matches(this.selectorStagger)) continue;
      if (it.classList.contains('motion-item')) continue;
      it.style.setProperty('--i', String(i));
      it.classList.add('motion-item');
    }
  }

  private escaneo(root: Element | Document): void {
    const q = root.querySelectorAll.bind(root) as (s: string) => NodeListOf<Element>;

    const revelables = Array.from(q('.reveal:not(.is-in)')) as HTMLElement[];
    revelables.forEach((el) => this.observar(el));

    const conteos = Array.from(q('.count-up:not(.counted)')) as HTMLElement[];
    conteos.forEach((el) => this.observar(el));

    if (root.nodeType === 1) {
      const r = root as HTMLElement;
      if (r.matches('.reveal:not(.is-in)')) this.observar(r);
      if (r.matches('.count-up:not(.counted)')) this.observar(r);
      if (r.matches(this.revealAuto) && !r.classList.contains('reveal') && !r.closest('.modal-overlay')) {
        r.classList.add('reveal', 'reveal-up');
        this.observar(r);
      }
      if (r.matches('[data-motion-list], .motion-list')) this.staggerLista(r);
      else if (r.matches(this.selectorStagger) && r.parentElement) this.staggerLista(r.parentElement);
    }

    const autos = Array.from(q(this.revealAuto + ':not(.reveal)')) as HTMLElement[];
    autos.forEach((el) => {
      if (!el.closest('.modal-overlay')) {
        el.classList.add('reveal', 'reveal-up');
        this.observar(el);
      }
    });

    const valores = Array.from(
      q('.stat-value:not(.count-up), .rating-value:not(.count-up), .hero-stat .stat-value:not(.count-up)')
    ) as HTMLElement[];
    valores.forEach((el) => {
      if (!Number.isNaN(this.numero(el))) {
        el.classList.add('count-up');
        this.observar(el);
      }
    });

    const listas = Array.from(q('[data-motion-list], .motion-list')) as HTMLElement[];
    listas.forEach((el) => this.staggerLista(el));
  }
}
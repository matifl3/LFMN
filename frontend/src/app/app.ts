import { Component, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { Header } from './shared/components/header/header';
import { Footer } from './shared/components/footer/footer';
import { ToastContainer } from './shared/components/toast-container/toast-container';
import { MotionService } from './core/services/motion.service';

@Component({
  imports: [RouterOutlet, Header, Footer, ToastContainer],
  selector: 'app-root',
  styleUrl: './app.scss',
  templateUrl: './app.html',
})
export class App {
  private readonly motion = inject(MotionService);
  private readonly router = inject(Router);
  protected readonly esAuth = signal(false);

  constructor() {
    this.motion.iniciar();
    const esRutaAuth = () => this.router.url.startsWith('/auth');
    this.esAuth.set(esRutaAuth());
    this.router.events.subscribe((e) => {
      if (e instanceof NavigationEnd) {
        this.esAuth.set(esRutaAuth());
      }
    });
  }
}
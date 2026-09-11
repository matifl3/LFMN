import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
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

  constructor() {
    this.motion.iniciar();
  }
}
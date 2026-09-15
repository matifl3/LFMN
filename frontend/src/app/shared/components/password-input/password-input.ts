import { Component, forwardRef, input, signal } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

@Component({
  selector: 'app-password-input',
  standalone: true,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => PasswordInput),
      multi: true,
    },
  ],
  styleUrl: './password-input.scss',
  template: `
    <div class="password-input">
      <input
        class="input password-input__campo"
        [id]="id() ?? undefined"
        [type]="oculto() ? 'password' : 'text'"
        [placeholder]="placeholder()"
        [attr.autocomplete]="autocomplete()"
        [attr.aria-label]="ariaLabel() ?? id() ?? undefined"
        [value]="valor() ?? ''"
        (input)="onInput($event)"
        (blur)="blur()"
      />
      <button
        type="button"
        class="password-input__toggle"
        [attr.aria-label]="oculto() ? 'Mostrar contraseña' : 'Ocultar contraseña'"
        (click)="toggle()"
        tabindex="-1"
      >
        @if (oculto()) {
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
            <circle cx="12" cy="12" r="3" />
          </svg>
        } @else {
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" />
            <line x1="1" y1="1" x2="23" y2="23" />
          </svg>
        }
      </button>
    </div>
  `,
})
export class PasswordInput implements ControlValueAccessor {
  readonly id = input<string | null>(null);
  readonly placeholder = input('');
  readonly autocomplete = input('current-password');
  readonly ariaLabel = input<string | null>(null);

  readonly oculto = signal(true);
  readonly valor = signal<string | null>(null);

  private onChange: (v: string) => void = () => {};
  private onTouched: () => void = () => {};

  toggle(): void {
    this.oculto.update((o) => !o);
  }

  onInput(event: Event): void {
    const v = (event.target as HTMLInputElement).value;
    this.valor.set(v);
    this.onChange(v);
  }

  blur(): void {
    this.onTouched();
  }

  writeValue(value: string | null): void {
    this.valor.set(value ?? null);
  }

  registerOnChange(fn: (v: string) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    if (isDisabled) {
      this.valor.set(null);
    }
  }
}
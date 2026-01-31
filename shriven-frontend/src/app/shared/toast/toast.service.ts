import { Injectable, signal } from '@angular/core';

export type ToastType = 'success' | 'info' | 'error';

export interface Toast {
  message: string;
  type: ToastType;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private readonly toastSignal = signal<Toast | null>(null);
  private hideTimeout: ReturnType<typeof setTimeout> | null = null;

  readonly toast = this.toastSignal.asReadonly();

  show(message: string, type: ToastType = 'success'): void {
    if (this.hideTimeout) {
      clearTimeout(this.hideTimeout);
      this.hideTimeout = null;
    }

    this.toastSignal.set({ message, type });

    this.hideTimeout = setTimeout(() => {
      this.toastSignal.set(null);
      this.hideTimeout = null;
    }, 3000);
  }

  hide(): void {
    if (this.hideTimeout) {
      clearTimeout(this.hideTimeout);
      this.hideTimeout = null;
    }
    this.toastSignal.set(null);
  }
}

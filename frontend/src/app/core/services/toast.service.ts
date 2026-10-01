import { Injectable, signal } from '@angular/core';

export interface ToastMessage {
  id: number;
  type: 'success' | 'error' | 'warning' | 'info';
  title?: string;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  toasts = signal<ToastMessage[]>([]);
  private counter = 0;

  show(type: 'success' | 'error' | 'warning' | 'info', message: string, title?: string, duration = 4000): void {
    const id = ++this.counter;
    const toast: ToastMessage = { id, type, title, message };
    this.toasts.update(current => [...current, toast]);

    setTimeout(() => {
      this.remove(id);
    }, duration);
  }

  success(message: string, title = 'Success'): void {
    this.show('success', message, title);
  }

  error(message: string, title = 'Error'): void {
    this.show('error', message, title, 6000);
  }

  warning(message: string, title = 'Warning'): void {
    this.show('warning', message, title, 5000);
  }

  info(message: string, title = 'Info'): void {
    this.show('info', message, title);
  }

  remove(id: number): void {
    this.toasts.update(current => current.filter(t => t.id !== id));
  }
}

import { Component, inject } from '@angular/core';
import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-toast',
  template: `
    <div class="toast-list" aria-live="polite" aria-atomic="false">
      @for (toast of toastService.toasts(); track toast.id) {
        <div class="toast" [class]="'toast toast-' + toast.type" role="alert">
          @if (toast.title) { <strong>{{ toast.title }}</strong> }
          <span>{{ toast.message }}</span>
          <button type="button" class="toast-close" aria-label="Dismiss" (click)="toastService.remove(toast.id)">×</button>
        </div>
      }
    </div>
  `,
  styles: [`
    .toast-list { position: fixed; z-index: 9999; bottom: 28px; right: 28px; display: flex; flex-direction: column; gap: 10px; max-width: 380px; pointer-events: none; }
    .toast { display: flex; align-items: flex-start; gap: 10px; padding: 13px 14px; border-radius: 7px; border-left: 4px solid #91afb8; background: #fff; box-shadow: 0 4px 24px #20313d26; font-size: 13px; pointer-events: all; }
    .toast strong { display: block; margin-bottom: 3px; font-weight: 700; }
    .toast-success { border-left-color: #2a8a5a; }
    .toast-success strong { color: #1b6240; }
    .toast-error { border-left-color: #c0392b; }
    .toast-error strong { color: #8a2922; }
    .toast-info { border-left-color: #147e8a; }
    .toast-info strong { color: #155f6b; }
    .toast-warning { border-left-color: #d4a017; }
    .toast-warning strong { color: #7a5b0a; }
    .toast-close { margin-left: auto; padding: 0 4px; border: 0; background: none; color: #82939a; font-size: 18px; line-height: 1; cursor: pointer; }
    .toast-close:hover { color: #20313d; }
  `]
})
export class ToastComponent {
  protected readonly toastService = inject(ToastService);
}

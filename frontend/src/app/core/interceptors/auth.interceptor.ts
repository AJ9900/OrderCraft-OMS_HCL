import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { ToastService } from '../services/toast.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const toastService = inject(ToastService);
  const token = authService.token();

  let authReq = req;
  if (token && !req.url.includes('/api/auth/login')) {
    authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !req.url.includes('/api/auth/login')) {
        toastService.error('Session expired. Please log in again.');
        authService.logout();
      } else if (error.status === 403) {
        toastService.error('You do not have permission to perform this action.');
      } else if (error.status === 400 && error.error?.message) {
        toastService.error(error.error.message, 'Bad Request');
      } else if (error.status === 500) {
        toastService.error('An unexpected server error occurred. Please try again.');
      }
      return throwError(() => error);
    })
  );
};

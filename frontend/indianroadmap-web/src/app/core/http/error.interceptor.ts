import { HttpErrorResponse, HttpHandlerFn, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { NotificationService } from '../services/notification.service';

function toMessage(error: HttpErrorResponse): string {
  const apiMessage = (error.error as { error?: { message?: string } } | null)?.error?.message;
  if (apiMessage) {
    return apiMessage;
  }
  if (error.status === 0) {
    return 'Unable to reach IndianRoadmap services right now.';
  }
  if (error.status === 401) {
    return 'Your session has expired. Please sign in again.';
  }
  if (error.status === 403) {
    return 'You do not have permission to perform this action.';
  }
  if (error.status === 404) {
    return 'We could not find the resource you requested.';
  }
  if (error.status >= 500) {
    return 'Something went wrong on our side. Please try again shortly.';
  }
  return 'Something went wrong. Please try again.';
}

export const errorInterceptor: HttpInterceptorFn = (req, next: HttpHandlerFn) => {
  const notifications = inject(NotificationService);
  return next(req).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse) {
        notifications.show('error', toMessage(error));
      }
      return throwError(() => error);
    })
  );
};

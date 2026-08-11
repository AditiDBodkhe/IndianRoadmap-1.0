import { HttpHandlerFn, HttpInterceptorFn } from '@angular/common/http';
import { v4 as uuidv4 } from 'uuid';
import { environment } from '../../../environments/environment';

export const correlationInterceptor: HttpInterceptorFn = (req, next: HttpHandlerFn) => {
  if (!req.url.startsWith(environment.apiBaseUrl)) {
    return next(req);
  }

  return next(
    req.clone({
      setHeaders: {
        'X-Correlation-Id': uuidv4()
      }
    })
  );
};

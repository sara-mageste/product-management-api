import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, catchError, filter, switchMap, take, throwError } from 'rxjs';

import { AuthService } from '../service/auth.service';

const AUTH_ENDPOINTS = [
  '/api/auth/login',
  '/api/auth/register',
  '/api/auth/refresh',
  '/api/auth/forgot-password',
  '/api/auth/reset-password'
];

let isRefreshing = false;
const refreshedToken$ = new BehaviorSubject<string | null>(null);

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const isAuthEndpoint = AUTH_ENDPOINTS.some(endpoint => req.url.includes(endpoint));
  const token = authService.getAccessToken();

  const authReq = (!isAuthEndpoint && token)
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status !== 401 || isAuthEndpoint) {
        return throwError(() => error);
      }

      if (!isRefreshing) {
        isRefreshing = true;
        refreshedToken$.next(null);

        return authService.refresh().pipe(
          switchMap((response) => {
            isRefreshing = false;
            refreshedToken$.next(response.accessToken);

            const retriedReq = req.clone({
              setHeaders: { Authorization: `Bearer ${response.accessToken}` }
            });
            return next(retriedReq);
          }),
          catchError((refreshError) => {
            isRefreshing = false;
            router.navigate(['/login']);
            return throwError(() => refreshError);
          })
        );
      }

      return refreshedToken$.pipe(
        filter((newToken) => newToken !== null),
        take(1),
        switchMap((newToken) => {
          const retriedReq = req.clone({
            setHeaders: { Authorization: `Bearer ${newToken}` }
          });
          return next(retriedReq);
        })
      );
    })
  );
};
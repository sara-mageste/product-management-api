import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, tap, throwError } from 'rxjs';

import { 
  AuthResponse, LoginRequest, 
  RefreshRequest, RegisterRequest, RegisterResponse,
  ForgotPasswordRequest, ResetPasswordRequest, MessageResponse
} from '../models/auth.model';

const ACCESS_TOKEN_KEY = 'access_token';
const REFRESH_TOKEN_KEY = 'refresh_token';
const USER_INFO_KEY = 'auth_user';

interface StoredUser {
  employeeCode: string;
  name: string;
  avatarId: number;
}

@Injectable({ providedIn: 'root' })
export class AuthService {

  private readonly apiUrl = 'http://localhost:8080/api/auth';

  isAuthenticated = signal<boolean>(this.hasValidTokens());
  currentUser = signal<StoredUser | null>(this.readStoredUser());

  constructor(private http: HttpClient) {}

  login(employeeCode: string, password: string): Observable<AuthResponse> {
    const payload: LoginRequest = { employeeCode, password };

    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, payload).pipe(
      tap((response) => this.storeSession(response))
    );
  }

  refresh(): Observable<AuthResponse> {
    const refreshToken = this.getRefreshToken();

    if (!refreshToken) {
      return throwError(() => new Error('No refresh token available'));
    }

    const payload: RefreshRequest = { refreshToken };

    return this.http.post<AuthResponse>(`${this.apiUrl}/refresh`, payload).pipe(
      tap((response) => this.storeSession(response)),
      catchError((err) => {
        this.clearSession();
        return throwError(() => err);
      })
    );
  }

  logout(): Observable<void> {
    const refreshToken = this.getRefreshToken();

    if (!refreshToken) {
      this.clearSession();
      return new Observable<void>((subscriber) => {
        subscriber.next();
        subscriber.complete();
      });
    }

    return this.http.post<void>(`${this.apiUrl}/logout`, { refreshToken }).pipe(
      tap(() => this.clearSession()),
      catchError(() => {
        //Even if the backend call fails (e.g., token already expired), the local session is cleared anyway.
        this.clearSession();
        return new Observable<void>((subscriber) => {
          subscriber.next();
          subscriber.complete();
        });
      })
    );
  }

  getAccessToken(): string | null {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  }

  private hasValidTokens(): boolean {
    return !!this.getAccessToken() && !!this.getRefreshToken();
  }

  private readStoredUser(): StoredUser | null {
    const raw = localStorage.getItem(USER_INFO_KEY);
    if (!raw) return null;

    try {
      return JSON.parse(raw) as StoredUser;
    } catch {
      return null;
    }
  }

  private storeSession(response: AuthResponse): void {
    localStorage.setItem(ACCESS_TOKEN_KEY, response.accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, response.refreshToken);

    const user: StoredUser = {
      employeeCode: response.employeeCode,
      name: response.name,
      avatarId: response.avatarId
    };

    localStorage.setItem(USER_INFO_KEY, JSON.stringify(user));

    this.isAuthenticated.set(true);
    this.currentUser.set(user);
  }

  private clearSession(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(USER_INFO_KEY);

    this.isAuthenticated.set(false);
    this.currentUser.set(null);
  }

  register(payload: RegisterRequest): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(`${this.apiUrl}/register`, payload);
  }

  forgotPassword(email: string): Observable<MessageResponse> {
    const payload: ForgotPasswordRequest = { email };
    return this.http.post<MessageResponse>(`${this.apiUrl}/forgot-password`, payload);
  }

  resetPassword(token: string, newPassword: string, confirmPassword: string): Observable<MessageResponse> {
    const payload: ResetPasswordRequest = { token, newPassword, confirmPassword };
    return this.http.post<MessageResponse>(`${this.apiUrl}/reset-password`, payload);
  }

}
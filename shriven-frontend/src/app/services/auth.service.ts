import { Injectable, PLATFORM_ID, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, catchError, of, tap } from 'rxjs';
import { environment } from '../../environments/environment';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  email: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));
  private readonly apiUrl = `${environment.apiUrl}/auth`;

  private readonly EMAIL_KEY = 'shriven_email';

  private readonly _accessToken = signal<string | null>(null);
  private initializeAuthPromise: Promise<void> | null = null;

  readonly isLoggedIn = signal<boolean>(false);
  readonly currentEmail = signal<string | null>(null);
  readonly isAuthInitialized = signal<boolean>(false);

  constructor() {
    if (this.isBrowser) {
      // Optimistically restore session state from localStorage so the UI
      // shows the correct state immediately without waiting for the refresh call.
      // initializeAuth() will confirm by getting a new access token, or will
      // call clearState() to log the user out if the refresh token has expired.
      const email = localStorage.getItem(this.EMAIL_KEY);
      if (email) {
        this.isLoggedIn.set(true);
        this.currentEmail.set(email);
      }
    }
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.apiUrl}/login`, request, { withCredentials: true })
      .pipe(tap(response => this.handleAuthResponse(response)));
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.apiUrl}/register`, request, { withCredentials: true })
      .pipe(tap(response => this.handleAuthResponse(response)));
  }

  refresh(): Observable<AuthResponse | null> {
    return this.http
      .post<AuthResponse>(`${this.apiUrl}/refresh`, {}, { withCredentials: true })
      .pipe(
        tap(response => this.handleAuthResponse(response)),
        catchError(() => {
          this.clearState();
          return of(null);
        })
      );
  }

  logout(): void {
    this.http
      .post<void>(`${this.apiUrl}/logout`, {}, { withCredentials: true })
      .subscribe({ error: () => {} });
    this.clearState();
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return this._accessToken();
  }

  initializeAuth(): Promise<void> {
    if (this.initializeAuthPromise) {
      return this.initializeAuthPromise;
    }

    if (!this.isBrowser) {
      this.initializeAuthPromise = Promise.resolve();
      return this.initializeAuthPromise;
    }

    this.initializeAuthPromise = new Promise(resolve => {
      this.refresh().subscribe({
        next: () => {
          this.isAuthInitialized.set(true);
          resolve();
        },
        error: () => {
          this.isAuthInitialized.set(true);
          resolve();
        }
      });
    });

    return this.initializeAuthPromise;
  }

  private handleAuthResponse(response: AuthResponse): void {
    this._accessToken.set(response.accessToken);
    this.isLoggedIn.set(true);
    this.currentEmail.set(response.email);
    if (this.isBrowser) {
      localStorage.setItem(this.EMAIL_KEY, response.email);
    }
  }

  private clearState(): void {
    this._accessToken.set(null);
    this.isLoggedIn.set(false);
    this.currentEmail.set(null);
    if (this.isBrowser) {
      localStorage.removeItem(this.EMAIL_KEY);
    }
  }
}

// path: src/app/services/auth.service.ts
import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { map, catchError } from 'rxjs/operators';
import { Observable, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthRequest, AuthResponse, RegisterRequest } from '../models/auth.model';

const TOKEN_KEY = 'taskflow_token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  #http = inject(HttpClient);
  #router = inject(Router);
  #base = `${environment.apiBaseUrl}/api/auth`;

  

  /** Login – returns AuthResponse from API */
  login(payload: AuthRequest): Observable<AuthResponse> {
    return this.#http.post<AuthResponse>(`${this.#base}/login`, payload);
  }

  /**
   * Register – API often returns 201/204 without body.
   * Expose Observable<void> to keep component simple.
   */
  register(payload: RegisterRequest): Observable<void> {
    return this.#http
      .post<void>(`${this.#base}/register`, payload, { observe: 'response' })
      .pipe(
        map((res: HttpResponse<void>) => {
          // normalize to void regardless of status code
          return;
        })
      );
  }

  /** Token helpers */
  setToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  clearToken(): void {
    localStorage.removeItem(TOKEN_KEY);
  }

  setLoginResult(res: AuthResponse): void {
    // store token + user info together
    this.setToken(res.token);
    localStorage.setItem(
      'user',
      JSON.stringify({ id: res.userId, email: res.email, fullName: res.fullName, admin: res.admin })
    );
  }


  /**
   * Auth check:
   * - If token looks like a JWT and has 'exp', validate expiry.
   * - Otherwise, fallback to presence check (truthy token).
   */
  isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token) return false;

    const payload = this.decodeJwt(token);
    if (payload && typeof payload.exp === 'number') {
      return !this.isTokenExpired(payload.exp);
    }
    // If not a JWT or no exp, consider authenticated when token exists
    return true;
  }

  /** Get current user ID from localStorage */
  getCurrentUserId(): number {
    const user = localStorage.getItem('user');
    if (user) {
      try {
        return JSON.parse(user).id;
      } catch {
        return 0;
      }
    }
    return 0;
  }

  isAdmin(): boolean {
    const user = localStorage.getItem('user');
    if (!user) return false;
    try {
      return !!JSON.parse(user).admin;
    } catch {
      return false;
    }
  }

  /** Logout + navigate to /login (idempotent) */
  logout(): void {
    this.clearToken();
    // ignore navigation errors (e.g., already on /login)
    this.#router.navigate(['/login']).catch(() => {});
  }

  // ----------------- Private helpers -----------------

  /** Decode JWT payload safely; returns null if not a valid JWT */
  private decodeJwt(token: string): any | null {
    const parts = token.split('.');
    if (parts.length !== 3) return null;
    try {
      const payload = atob(parts[1].replace(/-/g, '+').replace(/_/g, '/'));
      // decodeURIComponent isn’t needed here; base64url decoded already
      return JSON.parse(payload);
    } catch {
      return null;
    }
  }

  private isTokenExpired(expSeconds: number): boolean {
    const nowSeconds = Math.floor(Date.now() / 1000);
    return expSeconds <= nowSeconds;
  }
}
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { Role } from '../models/auth.model';

export type Theme = 'LIGHT' | 'DARK' | 'SYSTEM';

export interface MeResponse {
  id: number;
  fullName: string;
  email: string;
  role: Role;
  avatarColour: string;
  bio?: string | null;
}

export interface UpdateProfileRequest {
  fullName: string;
  email: string;
  currentPassword?: string;
  avatarColour?: string;
  bio?: string | null;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export interface UpdatePreferencesRequest {
  theme?: Theme;
  notifyAssigned?: boolean;
  notifyComment?: boolean;
  notifySubtask?: boolean;
  notifyOverdue?: boolean;
  notifyTeam?: boolean;
  avatarColour?: string;
  bio?: string | null;
}

export interface SessionResponse {
  jti: string;
  deviceHint?: string | null;
  loginTime: string;
  lastActive: string;
  expiresAt: string;
  current: boolean;
}

@Injectable({ providedIn: 'root' })
export class UserSettingsService {
  #http = inject(HttpClient);
  #base = `${environment.apiBaseUrl}/api/users/me`;

  me(): Observable<MeResponse> {
    return this.#http.get<MeResponse>(this.#base);
  }

  updateProfile(payload: UpdateProfileRequest): Observable<MeResponse> {
    return this.#http.patch<MeResponse>(`${this.#base}/profile`, payload);
  }

  changePassword(payload: ChangePasswordRequest): Observable<void> {
    return this.#http.patch<void>(`${this.#base}/password`, payload);
  }

  updatePreferences(payload: UpdatePreferencesRequest): Observable<void> {
    return this.#http.patch<void>(`${this.#base}/preferences`, payload);
  }

  deleteMe(confirmEmail: string): Observable<void> {
    return this.#http.delete<void>(this.#base, { body: { confirmEmail } });
  }

  listSessions(): Observable<SessionResponse[]> {
    return this.#http.get<SessionResponse[]>(`${this.#base}/sessions`);
  }

  revokeSession(jti: string): Observable<void> {
    return this.#http.delete<void>(`${this.#base}/sessions/${encodeURIComponent(jti)}`);
  }

  revokeAllOtherSessions(): Observable<void> {
    return this.#http.delete<void>(`${this.#base}/sessions`);
  }
}


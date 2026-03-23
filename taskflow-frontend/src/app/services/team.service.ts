import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { CreateTeamRequest, Team } from '../models/team.model';

@Injectable({ providedIn: 'root' })
export class TeamService {
  #http = inject(HttpClient);
  #base = `${environment.apiBaseUrl}/api/teams`;

  list(): Observable<Team[]> {
    return this.#http.get<Team[]>(this.#base);
  }

  get(id: number): Observable<Team> {
    return this.#http.get<Team>(`${this.#base}/${id}`);
  }

  create(payload: CreateTeamRequest): Observable<Team> {
    return this.#http.post<Team>(this.#base, payload);
  }

  addMember(teamId: number, userId: number): Observable<Team> {
    return this.#http.post<Team>(`${this.#base}/${teamId}/members`, { userId });
  }

  removeMember(teamId: number, userId: number): Observable<Team> {
    return this.#http.delete<Team>(`${this.#base}/${teamId}/members/${userId}`);
  }

  delete(teamId: number): Observable<void> {
    return this.#http.delete<void>(`${this.#base}/${teamId}`);
  }
}


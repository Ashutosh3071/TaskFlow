// path: src/app/services/task.service.ts
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import {
  ActivityLogResponse,
  Priority,
  TaskComment,
  TaskCommentRequest,
  TaskRequest,
  TaskResponse,
  TaskSummaryResponse,
  UserResponse
} from '../models/task.model';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class TaskService {
  #http = inject(HttpClient);
  #base = `${environment.apiBaseUrl}/api/tasks`;
  #commentBase = `${environment.apiBaseUrl}/api/comments`;

  list(priority?: Priority): Observable<TaskResponse[]> {
    const params = priority ? { priority } : undefined;
    return this.#http.get<TaskResponse[]>(this.#base, { params });
  }

  create(req: TaskRequest): Observable<TaskResponse> {
    return this.#http.post<TaskResponse>(this.#base, req);
  }

  get(id: number): Observable<TaskResponse> {
    return this.#http.get<TaskResponse>(`${this.#base}/${id}`);
  }

  update(id: number, req: TaskRequest): Observable<TaskResponse> {
    return this.#http.put<TaskResponse>(`${this.#base}/${id}`, req);
  }

  remove(id: number): Observable<void> {
    return this.#http.delete<void>(`${this.#base}/${id}`);
  }

  getComments(taskId: number): Observable<TaskComment[]> {
    return this.#http.get<TaskComment[]>(`${this.#base}/${taskId}/comments`);
  }

  createComment(taskId: number, req: TaskCommentRequest): Observable<TaskComment> {
    return this.#http.post<TaskComment>(`${this.#base}/${taskId}/comments`, req);
  }

  deleteComment(commentId: number): Observable<void> {
    return this.#http.delete<void>(`${this.#commentBase}/${commentId}`);
  }

  listUsers(): Observable<UserResponse[]> {
    return this.#http.get<UserResponse[]>(`${environment.apiBaseUrl}/api/users`);
  }

  summary(): Observable<TaskSummaryResponse> {
    return this.#http.get<TaskSummaryResponse>(`${this.#base}/summary`);
  }

  activity(): Observable<ActivityLogResponse[]> {
    return this.#http.get<ActivityLogResponse[]>(`${environment.apiBaseUrl}/api/activity`);
  }

  clearActivity(): Observable<void> {
    return this.#http.delete<void>(`${environment.apiBaseUrl}/api/activity`);
  }

  // -------- Attachments --------
  listAttachments(taskId: number): Observable<any[]> {
    return this.#http.get<any[]>(`${this.#base}/${taskId}/attachments`);
  }

  uploadAttachment(taskId: number, file: File): Observable<void> {
    const form = new FormData();
    form.append('file', file);
    return this.#http.post<void>(`${this.#base}/${taskId}/attachments`, form);
  }

  deleteAttachment(attachmentId: number): Observable<void> {
    return this.#http.delete<void>(`${environment.apiBaseUrl}/api/attachments/${attachmentId}`);
  }

  // -------- Subtasks --------
  listSubtasks(taskId: number): Observable<any[]> {
    return this.#http.get<any[]>(`${this.#base}/${taskId}/subtasks`);
  }

  createSubtask(taskId: number, title: string, assignedTo?: number | null): Observable<any> {
    return this.#http.post<any>(`${this.#base}/${taskId}/subtasks`, { title, assignedTo: assignedTo ?? null });
  }

  toggleSubtask(id: number): Observable<any> {
    return this.#http.patch<any>(`${environment.apiBaseUrl}/api/subtasks/${id}/toggle`, {});
  }

  deleteSubtask(id: number): Observable<void> {
    return this.#http.delete<void>(`${environment.apiBaseUrl}/api/subtasks/${id}`);
  }

  subtaskSummary(taskId: number): Observable<{ total: number; completed: number }> {
    return this.#http.get<{ total: number; completed: number }>(`${this.#base}/${taskId}/subtasks/summary`);
  }

  // -------- Time Tracking --------
  startTimer(taskId: number): Observable<void> {
    return this.#http.post<void>(`${this.#base}/${taskId}/timer/start`, {});
  }

  stopTimer(taskId: number): Observable<any> {
    return this.#http.post<any>(`${this.#base}/${taskId}/timer/stop`, {});
  }

  listTimeLogs(taskId: number): Observable<any[]> {
    return this.#http.get<any[]>(`${this.#base}/${taskId}/time-logs`);
  }

  totalTime(taskId: number): Observable<{ totalMinutes: number }> {
    return this.#http.get<{ totalMinutes: number }>(`${this.#base}/${taskId}/time-logs/total`);
  }

  addManualTime(taskId: number, durationMinutes: number, logDate: string, note?: string): Observable<any> {
    return this.#http.post<any>(`${this.#base}/${taskId}/time-logs`, { durationMinutes, logDate, note });
  }

  deleteTimeLog(id: number): Observable<void> {
    return this.#http.delete<void>(`${environment.apiBaseUrl}/api/time-logs/${id}`);
  }
}

import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { NavbarComponent } from '../shared/navbar/navbar.component';

interface AdminUser {
  id: number;
  fullName: string;
  email: string;
  role: 'ADMIN' | 'MANAGER' | 'MEMBER' | 'VIEWER';
  active: boolean;
  createdAt: string;
}

@Component({
  standalone: true,
  selector: 'app-admin',
  imports: [CommonModule, NavbarComponent],
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.css']
})
export class AdminComponent implements OnInit {
  #http = inject(HttpClient);
  #base = `${environment.apiBaseUrl}/api/admin/users`;

  users: AdminUser[] = [];
  loading = false;
  error = '';

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.error = '';
    this.#http.get<AdminUser[]>(this.#base).subscribe({
      next: (u) => {
        this.users = u;
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'Failed to load users';
        this.loading = false;
      }
    });
  }

  setStatus(id: number, active: boolean): void {
    this.error = '';
    this.#http.patch<void>(`${this.#base}/${id}/status`, { active }).subscribe({
      next: () => {
        this.users = this.users.map(u => u.id === id ? { ...u, active } : u);
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'Failed to update status';
      }
    });
  }

  setRole(id: number, role: AdminUser['role']): void {
    this.error = '';
    this.#http.patch<void>(`${this.#base}/${id}/role`, { role }).subscribe({
      next: () => {
        this.users = this.users.map(u => u.id === id ? { ...u, role } : u);
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'Failed to update role';
      }
    });
  }

  softDelete(id: number): void {
    if (!confirm('Are you sure you want to delete (deactivate) this user?')) return;
    this.error = '';
    this.#http.delete<void>(`${this.#base}/${id}`).subscribe({
      next: () => {
        this.users = this.users.map(u => u.id === id ? { ...u, active: false } : u);
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'Failed to delete user';
      }
    });
  }
}


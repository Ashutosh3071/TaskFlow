import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { NavbarComponent } from '../shared/navbar/navbar.component';

interface AdminUser {
  id: number;
  fullName: string;
  email: string;
  active: boolean;
  admin: boolean;
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

  activate(id: number): void {
    this.error = '';
    this.#http.put<void>(`${this.#base}/${id}/activate`, {}).subscribe({
      next: () => {
        this.users = this.users.map(u => u.id === id ? { ...u, active: true } : u);
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'Failed to activate user';
      }
    });
  }

  deactivate(id: number): void {
    this.error = '';
    this.#http.put<void>(`${this.#base}/${id}/deactivate`, {}).subscribe({
      next: () => {
        this.users = this.users.map(u => u.id === id ? { ...u, active: false } : u);
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'Failed to deactivate user';
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


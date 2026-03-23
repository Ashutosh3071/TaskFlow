import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { NavbarComponent } from '../shared/navbar/navbar.component';
import { TeamService } from '../services/team.service';
import { Team } from '../models/team.model';
import { TaskService } from '../services/task.service';
import { TaskResponse, UserResponse } from '../models/task.model';
import { HasRoleDirective } from '../shared/directives/has-role.directive';

@Component({
  standalone: true,
  selector: 'app-teams',
  imports: [CommonModule, FormsModule, ReactiveFormsModule, NavbarComponent, HasRoleDirective],
  templateUrl: './teams.component.html',
  styleUrls: ['./teams.component.css']
})
export class TeamsComponent implements OnInit {
  #teams = inject(TeamService);
  #tasks = inject(TaskService);
  #fb = inject(FormBuilder);

  loading = false;
  error = '';

  teams: Team[] = [];
  selected: Team | null = null;

  users: UserResponse[] = [];
  tasks: TaskResponse[] = [];

  // stats strip
  myTeamsCount = 0;
  totalMembers = 0;
  activeTasks = 0;

  showCreate = false;
  createError = '';
  createForm = this.#fb.nonNullable.group({
    name: this.#fb.nonNullable.control('', { validators: [Validators.required] }),
    description: this.#fb.nonNullable.control('')
  });

  // member add
  memberToAddId: number | null = null;

  ngOnInit(): void {
    this.reload();
    this.#tasks.listUsers().subscribe((u) => (this.users = u));
    this.#tasks.list().subscribe((t) => {
      this.tasks = t;
      this.recomputeStats();
    });
  }

  reload(): void {
    this.loading = true;
    this.error = '';
    this.#teams.list().subscribe({
      next: (list) => {
        this.teams = list;
        this.selected = list.length ? list[0] : null;
        this.loading = false;
        this.recomputeStats();
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'Failed to load teams';
        this.loading = false;
      }
    });
  }

  selectTeam(t: Team): void {
    this.selected = t;
  }

  openCreate(): void {
    this.createError = '';
    this.createForm.reset({ name: '', description: '' });
    this.showCreate = true;
  }

  closeCreate(): void {
    this.showCreate = false;
  }

  submitCreate(): void {
    if (this.createForm.invalid) {
      this.createForm.markAllAsTouched();
      return;
    }
    this.createError = '';
    const payload = this.createForm.getRawValue();
    this.#teams.create({ name: payload.name, description: payload.description }).subscribe({
      next: (created) => {
        this.teams = [created, ...this.teams];
        this.selected = created;
        this.showCreate = false;
        this.recomputeStats();
      },
      error: (err) => {
        this.createError = err?.error?.message ?? 'Failed to create team';
      }
    });
  }

  addMember(): void {
    if (!this.selected || !this.memberToAddId) return;
    const teamId = this.selected.id;
    const userId = this.memberToAddId;
    this.#teams.addMember(teamId, userId).subscribe({
      next: (updated) => {
        this.teams = this.teams.map((t) => (t.id === updated.id ? updated : t));
        this.selected = updated;
        this.memberToAddId = null;
        this.recomputeStats();
      },
      error: (err) => {
        alert(err?.error?.message ?? 'Failed to add member');
      }
    });
  }

  removeMember(userId: number): void {
    if (!this.selected) return;
    if (!confirm('Remove this member from the team?')) return;
    const teamId = this.selected.id;
    this.#teams.removeMember(teamId, userId).subscribe({
      next: (updated) => {
        this.teams = this.teams.map((t) => (t.id === updated.id ? updated : t));
        this.selected = updated;
        this.recomputeStats();
      },
      error: (err) => {
        alert(err?.error?.message ?? 'Failed to remove member');
      }
    });
  }

  deleteTeam(teamId: number): void {
    if (!confirm('Delete this team? This cannot be undone.')) return;
    this.#teams.delete(teamId).subscribe({
      next: () => {
        this.teams = this.teams.filter((t) => t.id !== teamId);
        if (this.selected?.id === teamId) {
          this.selected = this.teams.length ? this.teams[0] : null;
        }
        this.recomputeStats();
      },
      error: (err) => alert(err?.error?.message ?? 'Failed to delete team')
    });
  }

  roleBadgeClass(role: string): string {
    switch (role) {
      case 'ADMIN':
        return 'role role--admin';
      case 'MANAGER':
        return 'role role--manager';
      case 'MEMBER':
        return 'role role--member';
      default:
        return 'role role--viewer';
    }
  }

  private recomputeStats(): void {
    this.myTeamsCount = this.teams.length;
    this.totalMembers = this.teams.reduce((sum, t) => sum + (t.members?.length ?? 0), 0);
    const teamIds = new Set(this.teams.map((t) => t.id));
    this.activeTasks = this.tasks.filter((x) => x.teamId && teamIds.has(x.teamId) && x.status !== 'DONE').length;
  }
}


import { Component, Input, OnChanges, SimpleChanges, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TaskService } from '../../services/task.service';
import { AuthService } from '../../services/auth.service';

type Subtask = {
  id: number;
  taskId: number;
  title: string;
  complete: boolean;
  assignedToId?: number | null;
  assignedToName?: string | null;
  createdBy: number;
};

@Component({
  standalone: true,
  selector: 'app-subtasks',
  imports: [CommonModule, FormsModule],
  templateUrl: './subtasks.component.html',
  styleUrls: ['./subtasks.component.css']
})
export class SubtasksComponent implements OnChanges {
  @Input() taskId!: number;

  #api = inject(TaskService);
  #auth = inject(AuthService);

  list: Subtask[] = [];
  loading = false;
  error = '';
  newTitle = '';

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['taskId'] && this.taskId) this.reload();
  }

  canEdit(): boolean {
    const r = this.#auth.getCurrentRole();
    return r === 'ADMIN' || r === 'MANAGER' || r === 'MEMBER';
  }

  reload(): void {
    this.loading = true;
    this.error = '';
    this.#api.listSubtasks(this.taskId).subscribe({
      next: (res) => { this.list = res as any; this.loading = false; },
      error: (err) => { this.error = err?.error?.message ?? 'Failed to load subtasks'; this.loading = false; }
    });
  }

  add(): void {
    const title = this.newTitle.trim();
    if (!title) return;
    this.error = '';
    this.#api.createSubtask(this.taskId, title).subscribe({
      next: (s) => { this.list = [...this.list, s]; this.newTitle = ''; },
      error: (err) => this.error = err?.error?.message ?? 'Failed to add subtask'
    });
  }

  toggle(s: Subtask): void {
    if (!this.canEdit()) return;
    this.#api.toggleSubtask(s.id).subscribe({
      next: (updated) => { this.list = this.list.map(x => x.id === s.id ? updated : x); },
      error: (err) => this.error = err?.error?.message ?? 'Failed to update'
    });
  }

  canDelete(s: Subtask): boolean {
    const role = this.#auth.getCurrentRole();
    const me = this.#auth.getCurrentUserId();
    return role === 'ADMIN' || role === 'MANAGER' || s.createdBy === me;
  }

  remove(s: Subtask): void {
    if (!confirm('Delete this subtask?')) return;
    this.#api.deleteSubtask(s.id).subscribe({
      next: () => { this.list = this.list.filter(x => x.id !== s.id); },
      error: (err) => this.error = err?.error?.message ?? 'Failed to delete'
    });
  }

  doneLabel(): string {
    const total = this.list.length;
    const done = this.list.filter(x => x.complete).length;
    if (total === 0) return 'No subtasks';
    if (done === total) return 'All done!';
    return `${done} / ${total} done`;
  }

  donePct(): number {
    const total = this.list.length;
    if (!total) return 0;
    const done = this.list.filter(x => x.complete).length;
    return Math.round((done / total) * 100);
  }
}


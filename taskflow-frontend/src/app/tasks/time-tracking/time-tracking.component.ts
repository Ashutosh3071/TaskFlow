import { Component, Input, OnChanges, OnDestroy, SimpleChanges, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TaskService } from '../../services/task.service';
import { AuthService } from '../../services/auth.service';

type TimeLog = {
  id: number;
  taskId: number;
  loggedById: number;
  loggedByName: string;
  durationMinutes: number;
  logDate: string;
  note?: string | null;
  manual: boolean;
};

@Component({
  standalone: true,
  selector: 'app-time-tracking',
  imports: [CommonModule, FormsModule],
  templateUrl: './time-tracking.component.html',
  styleUrls: ['./time-tracking.component.css']
})
export class TimeTrackingComponent implements OnChanges, OnDestroy {
  @Input() taskId!: number;

  #api = inject(TaskService);
  #auth = inject(AuthService);

  logs: TimeLog[] = [];
  totalMinutes = 0;
  error = '';

  running = false;
  seconds = 0;
  private tick?: any;

  // manual log form
  manualOpen = false;
  hours = 0;
  minutes = 0;
  date = new Date().toISOString().slice(0, 10);
  note = '';

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['taskId'] && this.taskId) {
      this.load();
    }
  }

  ngOnDestroy(): void {
    if (this.tick) clearInterval(this.tick);
  }

  canEdit(): boolean {
    const r = this.#auth.getCurrentRole();
    return r === 'ADMIN' || r === 'MANAGER' || r === 'MEMBER';
  }

  load(): void {
    this.error = '';
    this.#api.listTimeLogs(this.taskId).subscribe({
      next: (l) => (this.logs = l as any),
      error: () => {}
    });
    this.#api.totalTime(this.taskId).subscribe({
      next: (t) => (this.totalMinutes = t.totalMinutes),
      error: () => {}
    });
  }

  start(): void {
    this.error = '';
    this.#api.startTimer(this.taskId).subscribe({
      next: () => {
        this.running = true;
        this.seconds = 0;
        if (this.tick) clearInterval(this.tick);
        this.tick = setInterval(() => (this.seconds += 1), 1000);
      },
      error: (err) => (this.error = err?.status === 409 ? 'A timer is already running.' : (err?.error?.message ?? 'Failed to start timer'))
    });
  }

  stop(): void {
    this.error = '';
    this.#api.stopTimer(this.taskId).subscribe({
      next: () => {
        this.running = false;
        if (this.tick) clearInterval(this.tick);
        this.tick = undefined;
        this.seconds = 0;
        this.load();
      },
      error: (err) => (this.error = err?.error?.message ?? 'Failed to stop timer')
    });
  }

  submitManual(): void {
    const dur = Math.max(0, (this.hours * 60) + this.minutes);
    if (dur <= 0) {
      this.error = 'Duration must be at least 1 minute.';
      return;
    }
    this.error = '';
    this.#api.addManualTime(this.taskId, dur, this.date, this.note || undefined).subscribe({
      next: () => {
        this.hours = 0;
        this.minutes = 0;
        this.note = '';
        this.manualOpen = false;
        this.load();
      },
      error: (err) => (this.error = err?.error?.message ?? 'Failed to log time')
    });
  }

  canDelete(l: TimeLog): boolean {
    if (!l.manual) return false;
    const role = this.#auth.getCurrentRole();
    const me = this.#auth.getCurrentUserId();
    return role === 'ADMIN' || role === 'MANAGER' || l.loggedById === me;
  }

  delete(l: TimeLog): void {
    if (!confirm('Delete this time entry?')) return;
    this.#api.deleteTimeLog(l.id).subscribe({
      next: () => this.load(),
      error: (err) => (this.error = err?.error?.message ?? 'Failed to delete')
    });
  }

  fmt(minutes: number): string {
    if (!minutes) return 'No time logged';
    const h = Math.floor(minutes / 60);
    const m = minutes % 60;
    if (h <= 0) return `${m}m`;
    return `${h}h ${m}m`;
  }

  hhmmss(): string {
    const s = this.seconds;
    const hh = `${Math.floor(s / 3600)}`.padStart(2, '0');
    const mm = `${Math.floor((s % 3600) / 60)}`.padStart(2, '0');
    const ss = `${s % 60}`.padStart(2, '0');
    return `${hh} : ${mm} : ${ss}`;
  }
}


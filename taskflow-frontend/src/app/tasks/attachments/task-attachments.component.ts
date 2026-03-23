import { Component, Input, OnChanges, SimpleChanges, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TaskService } from '../../services/task.service';
import { AuthService } from '../../services/auth.service';
import { environment } from '../../../environments/environment';

type Attachment = {
  id: number;
  taskId: number;
  uploaderId: number;
  originalName: string;
  mimeType: string;
  fileSizeBytes: number;
  uploadedAt: string;
};

@Component({
  standalone: true,
  selector: 'app-task-attachments',
  imports: [CommonModule],
  templateUrl: './task-attachments.component.html',
  styleUrls: ['./task-attachments.component.css']
})
export class TaskAttachmentsComponent implements OnChanges {
  @Input() taskId!: number;

  #api = inject(TaskService);
  #auth = inject(AuthService);

  list: Attachment[] = [];
  loading = false;
  error = '';
  uploading = false;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['taskId'] && this.taskId) this.reload();
  }

  reload(): void {
    this.loading = true;
    this.error = '';
    this.#api.listAttachments(this.taskId).subscribe({
      next: (res) => {
        this.list = res as Attachment[];
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'Failed to load attachments';
        this.loading = false;
      }
    });
  }

  canUpload(): boolean {
    const role = this.#auth.getCurrentRole();
    return role === 'ADMIN' || role === 'MANAGER' || role === 'MEMBER';
  }

  onFilePicked(ev: Event): void {
    const input = ev.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    input.value = '';
    this.upload(file);
  }

  upload(file: File): void {
    this.error = '';
    if (file.size > 5 * 1024 * 1024) {
      this.error = 'File exceeds 5 MB limit.';
      return;
    }
    if (this.list.length >= 5) {
      this.error = 'Maximum 5 files reached.';
      return;
    }
    this.uploading = true;
    this.#api.uploadAttachment(this.taskId, file).subscribe({
      next: () => {
        this.uploading = false;
        this.reload();
      },
      error: (err) => {
        this.uploading = false;
        this.error = err?.error?.message ?? 'Upload failed';
      }
    });
  }

  download(a: Attachment): void {
    window.open(`${environment.apiBaseUrl}/api/attachments/${a.id}/download`, '_blank');
  }

  canDelete(a: Attachment): boolean {
    const role = this.#auth.getCurrentRole();
    const me = this.#auth.getCurrentUserId();
    return role === 'ADMIN' || role === 'MANAGER' || a.uploaderId === me;
  }

  delete(a: Attachment): void {
    if (!confirm('Delete this attachment?')) return;
    this.#api.deleteAttachment(a.id).subscribe({
      next: () => this.reload(),
      error: (err) => (this.error = err?.error?.message ?? 'Delete failed')
    });
  }

  bytes(n: number): string {
    if (!n && n !== 0) return '-';
    const kb = 1024;
    const mb = kb * 1024;
    if (n >= mb) return `${(n / mb).toFixed(1)} MB`;
    if (n >= kb) return `${Math.round(n / kb)} KB`;
    return `${n} B`;
  }
}


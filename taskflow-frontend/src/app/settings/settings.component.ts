import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { debounceTime } from 'rxjs/operators';
import { NavbarComponent } from '../shared/navbar/navbar.component';
import { HasRoleDirective } from '../shared/directives/has-role.directive';
import { ThemeService } from '../services/theme.service';
import { Theme, UserSettingsService } from '../services/user-settings.service';
import { AuthService } from '../services/auth.service';

type Tab = 'profile' | 'security' | 'theme' | 'notifications' | 'team';

@Component({
  standalone: true,
  selector: 'app-settings',
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterLink, NavbarComponent, HasRoleDirective],
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.css']
})
export class SettingsComponent implements OnInit {
  #route = inject(ActivatedRoute);
  #router = inject(Router);
  #fb = inject(FormBuilder);
  #api = inject(UserSettingsService);
  #theme = inject(ThemeService);
  #auth = inject(AuthService);

  tab: Tab = 'profile';
  loading = false;
  error = '';
  success = '';

  me: any = null;
  sessions: any[] = [];

  profileForm = this.#fb.nonNullable.group({
    fullName: this.#fb.nonNullable.control('', { validators: [Validators.required] }),
    email: this.#fb.nonNullable.control('', { validators: [Validators.required, Validators.email] }),
    currentPassword: this.#fb.nonNullable.control(''),
    avatarColour: this.#fb.nonNullable.control('#2563EB'),
    bio: this.#fb.nonNullable.control('')
  });

  passwordForm = this.#fb.nonNullable.group({
    currentPassword: this.#fb.nonNullable.control('', { validators: [Validators.required] }),
    newPassword: this.#fb.nonNullable.control('', { validators: [Validators.required, Validators.minLength(8)] })
  });

  notifyForm = this.#fb.nonNullable.group({
    notifyAssigned: this.#fb.nonNullable.control(true),
    notifyComment: this.#fb.nonNullable.control(true),
    notifySubtask: this.#fb.nonNullable.control(true),
    notifyOverdue: this.#fb.nonNullable.control(true),
    notifyTeam: this.#fb.nonNullable.control(true)
  });

  themeChoice: Theme = 'LIGHT';
  deleteEmail = '';

  ngOnInit(): void {
    this.#route.queryParamMap.subscribe((qp) => {
      const t = (qp.get('tab') ?? 'profile') as Tab;
      this.tab = (['profile', 'security', 'theme', 'notifications', 'team'] as Tab[]).includes(t) ? t : 'profile';
    });

    this.themeChoice = this.#theme.getStoredTheme();
    this.#theme.applyTheme(this.themeChoice);

    this.loadMe();

    this.notifyForm.valueChanges.pipe(debounceTime(500)).subscribe((v) => {
      this.#api.updatePreferences({
        notifyAssigned: !!v.notifyAssigned,
        notifyComment: !!v.notifyComment,
        notifySubtask: !!v.notifySubtask,
        notifyOverdue: !!v.notifyOverdue,
        notifyTeam: !!v.notifyTeam
      }).subscribe({ error: () => {} });
    });
  }

  setTab(tab: Tab): void {
    this.#router.navigate([], { queryParams: { tab: tab === 'profile' ? null : tab }, queryParamsHandling: 'merge' });
  }

  loadMe(): void {
    this.loading = true;
    this.error = '';
    this.success = '';
    this.#api.me().subscribe({
      next: (me) => {
        this.me = me;
        this.profileForm.patchValue({
          fullName: me.fullName,
          email: me.email,
          avatarColour: me.avatarColour ?? '#2563EB',
          bio: me.bio ?? ''
        });
        // keep auth storage in sync for navbar
        const existing = this.#auth.getCurrentUser();
        if (existing) {
          localStorage.setItem('user', JSON.stringify({ ...existing, fullName: me.fullName, email: me.email, role: me.role, admin: me.role === 'ADMIN' }));
        }
        this.loading = false;
      },
      error: (err) => {
        this.error = err?.error?.message ?? 'Failed to load profile';
        this.loading = false;
      }
    });
  }

  saveProfile(): void {
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      return;
    }
    this.error = '';
    this.success = '';
    const v = this.profileForm.getRawValue();
    this.#api.updateProfile({
      fullName: v.fullName,
      email: v.email,
      currentPassword: v.currentPassword || undefined,
      avatarColour: v.avatarColour,
      bio: v.bio
    }).subscribe({
      next: (me) => {
        this.success = 'Profile updated.';
        this.me = me;
        const existing = this.#auth.getCurrentUser();
        if (existing) {
          localStorage.setItem('user', JSON.stringify({ ...existing, fullName: me.fullName, email: me.email, role: me.role, admin: me.role === 'ADMIN' }));
        }
        this.profileForm.patchValue({ currentPassword: '' });
      },
      error: (err) => this.error = err?.error?.message ?? 'Failed to update profile'
    });
  }

  changePassword(): void {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }
    this.error = '';
    this.success = '';
    const v = this.passwordForm.getRawValue();
    this.#api.changePassword(v).subscribe({
      next: () => {
        this.success = 'Password updated.';
        this.passwordForm.reset({ currentPassword: '', newPassword: '' });
      },
      error: (err) => this.error = err?.error?.message ?? 'Failed to change password'
    });
  }

  selectTheme(theme: Theme): void {
    this.themeChoice = theme;
    this.#theme.setTheme(theme);
    this.#api.updatePreferences({ theme }).subscribe({ error: () => {} });
  }

  loadSessions(): void {
    this.#api.listSessions().subscribe({
      next: (s) => this.sessions = s,
      error: () => {}
    });
  }

  revoke(jti: string): void {
    this.#api.revokeSession(jti).subscribe({
      next: () => this.loadSessions(),
      error: () => {}
    });
  }

  revokeOthers(): void {
    this.#api.revokeAllOtherSessions().subscribe({
      next: () => this.loadSessions(),
      error: () => {}
    });
  }

  deleteMe(): void {
    if (!this.me?.email) return;
    this.error = '';
    this.success = '';
    this.#api.deleteMe(this.deleteEmail).subscribe({
      next: () => {
        this.#auth.logout();
        this.#router.navigate(['/register']);
      },
      error: (err) => this.error = err?.error?.message ?? 'Failed to delete account'
    });
  }
}


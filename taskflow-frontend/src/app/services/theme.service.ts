import { Injectable } from '@angular/core';
import { Theme } from './user-settings.service';

const KEY = 'taskflow_theme';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  getStoredTheme(): Theme {
    const v = localStorage.getItem(KEY);
    if (v === 'DARK' || v === 'SYSTEM' || v === 'LIGHT') return v;
    return 'LIGHT';
  }

  setTheme(theme: Theme): void {
    localStorage.setItem(KEY, theme);
    this.applyTheme(theme);
  }

  applyTheme(theme: Theme): void {
    const root = document.documentElement;
    if (theme === 'SYSTEM') {
      const prefersDark = window.matchMedia?.('(prefers-color-scheme: dark)')?.matches;
      root.setAttribute('data-theme', prefersDark ? 'dark' : 'light');
      return;
    }
    root.setAttribute('data-theme', theme === 'DARK' ? 'dark' : 'light');
  }
}


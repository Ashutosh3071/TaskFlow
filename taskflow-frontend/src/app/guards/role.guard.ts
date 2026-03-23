import { Injectable, inject } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, Router, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Role } from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class RoleGuard implements CanActivate {
  #auth = inject(AuthService);
  #router = inject(Router);

  canActivate(route: ActivatedRouteSnapshot): boolean | UrlTree {
    const allowed: Role[] | undefined = route.data['roles'] as Role[] | undefined;
    if (!this.#auth.isAuthenticated()) {
      return this.#router.parseUrl('/login');
    }

    if (!allowed || allowed.length === 0) {
      return true;
    }

    const role = this.#auth.getCurrentRole();
    if (role && allowed.includes(role)) {
      return true;
    }

    // Redirect to dashboard per SRS with a simple fallback
    return this.#router.parseUrl('/dashboard');
  }
}


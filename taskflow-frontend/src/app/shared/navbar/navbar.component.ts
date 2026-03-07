import { Component, OnInit } from '@angular/core';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { NgIf } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { Subscription, filter } from 'rxjs';

@Component({
  standalone: true,
  selector: 'app-navbar',
  imports: [RouterLink, NgIf],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent {
  // constructor(public auth: AuthService) {}
 
 fullName?: string;
  private sub?: Subscription;

  constructor(public auth: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.readFromStorage(); // initial read (page refresh)
    // re-read after every navigation end (e.g., after login redirect)
    this.sub = this.router.events
      .pipe(filter((e): e is NavigationEnd => e instanceof NavigationEnd))
      .subscribe(() => this.readFromStorage());
  }

  private readFromStorage(): void {
    try {
      const raw = localStorage.getItem('user');
      this.fullName = raw ? (JSON.parse(raw)?.fullName ?? undefined) : undefined;
      // console.log('Navbar fullName:', this.fullName);
    } catch {
      this.fullName = undefined;
    }
  }

  onLogout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    this.fullName = undefined;
    this.router.navigate(['/login']); // optional
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }



}
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { CanActivate } from '@angular/router';

/**
 * Guards routes that require player authentication.
 *
 * Redirects unauthenticated users to the login page.
 */
@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(private router: Router) {}

  canActivate(): boolean {
    const token = localStorage.getItem('accessToken');
    if (token) {
      return true;
    }

    // Redirect to login
    this.router.navigate(['/']);
    return false;
  }
}

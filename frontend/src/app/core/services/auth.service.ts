import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { AuthResponse, Role, User } from '../models/models';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;
  private tokenKey = 'ordercraft_token';
  private userKey = 'ordercraft_user';

  currentUser = signal<User | null>(this.getStoredUser());
  token = signal<string | null>(localStorage.getItem(this.tokenKey));

  isAuthenticated = computed(() => !!this.token() && !!this.currentUser());
  currentRole = computed(() => this.currentUser()?.role || null);

  constructor(private http: HttpClient, private router: Router) {}

  login(credentials: { username: string; password: string }): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap(res => {
        localStorage.setItem(this.tokenKey, res.token);
        const user: User = {
          id: res.id,
          username: res.username,
          email: res.email,
          fullName: res.fullName,
          role: res.role,
          active: true
        };
        localStorage.setItem(this.userKey, JSON.stringify(user));
        this.token.set(res.token);
        this.currentUser.set(user);
      })
    );
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);
    this.token.set(null);
    this.currentUser.set(null);
    this.router.navigate(['/login']);
  }

  hasRole(role: Role): boolean {
    return this.currentUser()?.role === role;
  }

  hasAnyRole(roles: Role[]): boolean {
    const role = this.currentUser()?.role;
    if (!role) return false;
    if (role === 'ADMIN') return true; // Admin has full access across modules
    return roles.includes(role);
  }

  private getStoredUser(): User | null {
    const data = localStorage.getItem(this.userKey);
    if (!data) return null;
    try {
      return JSON.parse(data) as User;
    } catch {
      return null;
    }
  }
}

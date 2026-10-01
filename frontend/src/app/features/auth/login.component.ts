import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';

interface DemoAccount {
  role: string;
  username: string;
  password: string;
  color: string;
  description: string;
}

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, FormsModule],
  template: `
    <div class="page">
      <!-- ── Left panel ──────────────────────────────────────────────────── -->
      <aside class="left-panel">
        <div class="left-inner">
          <a class="brand" href="#" aria-label="OrderCraft home">
            <span class="brand-mark">O</span>
            <span class="brand-name">OrderCraft</span>
          </a>

          <div class="hero-copy">
            <p class="hero-eyebrow">Manufacturing OMS</p>
            <h1 class="hero-title">Run your factory.<br>Not spreadsheets.</h1>
            <p class="hero-sub">OrderCraft connects every step — from raw materials and suppliers to production, invoicing and cash.</p>
          </div>

          <ul class="feature-list" aria-label="Key features">
            <li><span class="feat-icon">📦</span><span>Real-time inventory &amp; BOM management</span></li>
            <li><span class="feat-icon">🔄</span><span>End-to-end order lifecycle tracking</span></li>
            <li><span class="feat-icon">📊</span><span>Shortage detection &amp; purchase automation</span></li>
            <li><span class="feat-icon">🧾</span><span>Invoicing, payments &amp; financial reports</span></li>
            <li><span class="feat-icon">🔒</span><span>Role-based access for your whole team</span></li>
          </ul>

          <footer class="left-foot">
            <span class="live-dot"></span>
            <span>System online &nbsp;·&nbsp; MySQL backend &nbsp;·&nbsp; JWT secured</span>
          </footer>
        </div>
      </aside>

      <!-- ── Right panel ─────────────────────────────────────────────────── -->
      <main class="right-panel">
        <div class="form-shell">

          <!-- Social login section -->
          @if (mode() === 'social') {
            <div class="section-heading">
              <p class="eyebrow">SOCIAL SIGN-IN</p>
              <h2>Sign in with</h2>
              <p class="section-sub">Choose your identity provider. Your account will be linked automatically.</p>
            </div>
            <div class="social-grid">
              <button class="social-btn" type="button" (click)="socialNotice('Google')">
                <svg width="20" height="20" viewBox="0 0 24 24" aria-hidden="true"><path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/><path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/><path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l3.66-2.84z"/><path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/></svg>
                Continue with Google
              </button>
              <button class="social-btn" type="button" (click)="socialNotice('Microsoft')">
                <svg width="20" height="20" viewBox="0 0 24 24" aria-hidden="true"><path fill="#F25022" d="M1 1h10v10H1z"/><path fill="#00A4EF" d="M13 1h10v10H13z"/><path fill="#7FBA00" d="M1 13h10v10H1z"/><path fill="#FFB900" d="M13 13h10v10H13z"/></svg>
                Continue with Microsoft
              </button>
              <button class="social-btn" type="button" (click)="socialNotice('GitHub')">
                <svg width="20" height="20" viewBox="0 0 24 24" aria-hidden="true" fill="currentColor"><path d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z"/></svg>
                Continue with GitHub
              </button>
              <button class="social-btn" type="button" (click)="mode.set('email')">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><rect x="2" y="4" width="20" height="16" rx="2"/><path d="m22 4-10 9L2 4"/></svg>
                Continue with Email / username
              </button>
            </div>
            <button class="back-link" type="button" (click)="mode.set('landing')">← Back</button>
          }

          <!-- Forgot password -->
          @else if (mode() === 'forgot') {
            <div class="section-heading">
              <p class="eyebrow">PASSWORD RECOVERY</p>
              <h2>Reset your password</h2>
              <p class="section-sub">Enter your email and we'll send a reset link if the account exists.</p>
            </div>
            <form class="form-body" (ngSubmit)="submitForgot()">
              <label>Work email address<input type="email" [(ngModel)]="forgotEmail" name="forgotEmail" placeholder="you@company.com" autocomplete="email"></label>
              <button class="primary-btn" type="submit">Send reset link</button>
              @if (forgotSent()) {
                <p class="success-note" role="status">
                  ✅ If that email is registered, a reset link has been sent. Check your inbox.
                </p>
              }
              <button class="back-link" type="button" (click)="mode.set('landing')">← Back to sign in</button>
            </form>
          }

          <!-- Email / username login -->
          @else if (mode() === 'email') {
            <div class="section-heading">
              <p class="eyebrow">SIGN IN</p>
              <h2>Welcome back</h2>
              <p class="section-sub">Enter your OrderCraft credentials to continue.</p>
            </div>

            @if (errorMsg()) {
              <div class="alert alert-error" role="alert">{{ errorMsg() }}</div>
            }

            <form class="form-body" [formGroup]="form" (ngSubmit)="submit()">
              <label>
                Username or email
                <input formControlName="username" type="text" autocomplete="username" autofocus placeholder="admin">
                @if (form.controls.username.touched && form.controls.username.invalid) {
                  <small>Username is required.</small>
                }
              </label>
              <label>
                Password
                <div class="password-wrap">
                  <input formControlName="password" [type]="showPass() ? 'text' : 'password'" autocomplete="current-password" placeholder="••••••••">
                  <button type="button" class="pass-toggle" (click)="showPass.update(v => !v)" [title]="showPass() ? 'Hide' : 'Show'">
                    {{ showPass() ? '🙈' : '👁️' }}
                  </button>
                </div>
                @if (form.controls.password.touched && form.controls.password.invalid) {
                  <small>Password is required.</small>
                }
              </label>
              <button class="primary-btn" type="submit" [disabled]="busy()">
                @if (busy()) { <span class="spinner"></span> Signing in… } @else { Sign in }
              </button>
            </form>

            <div class="divider"><span>or</span></div>
            <button class="secondary-btn" type="button" (click)="mode.set('social')">Sign in with social account</button>
            <div class="form-links">
              <button class="text-link" type="button" (click)="mode.set('forgot')">Forgot password?</button>
              <button class="text-link" type="button" (click)="mode.set('landing')">← All options</button>
            </div>
          }

          <!-- Landing / choose method -->
          @else {
            <div class="section-heading">
              <p class="eyebrow">ORDERCRAFT · MANUFACTURING OMS</p>
              <h2>Sign in to your workspace</h2>
              <p class="section-sub">Choose how you'd like to access your account.</p>
            </div>

            <div class="method-list">
              <button class="method-btn" type="button" (click)="mode.set('email')">
                <span class="method-icon">🔑</span>
                <span class="method-copy">
                  <strong>Email / Username &amp; password</strong>
                  <small>Use your OrderCraft account credentials</small>
                </span>
                <span class="method-arrow">›</span>
              </button>
              <button class="method-btn" type="button" (click)="mode.set('social')">
                <span class="method-icon">🌐</span>
                <span class="method-copy">
                  <strong>Social &amp; enterprise sign-in</strong>
                  <small>Google, Microsoft, GitHub</small>
                </span>
                <span class="method-arrow">›</span>
              </button>
            </div>

            <div class="divider-label">
              <span>Demo accounts — try without registration</span>
            </div>

            <div class="demo-grid">
              @for (account of demoAccounts; track account.username) {
                <button class="demo-card" type="button" [style.--accent]="account.color" (click)="quickLogin(account)">
                  <span class="demo-role">{{ account.role }}</span>
                  <span class="demo-user">{{ account.username }}</span>
                  <span class="demo-desc">{{ account.description }}</span>
                </button>
              }
            </div>

            <p class="security-note">🔒 All demo accounts use the same secure password. Data is shared across sessions.</p>
          }

        </div>
      </main>
    </div>
  `,
  styles: [`
    /* ── Base ─────────────────────────────────────────────────────────────── */
    :host { display: block; min-height: 100vh; }
    * { box-sizing: border-box; }

    /* ── Page layout ─────────────────────────────────────────────────────── */
    .page {
      display: grid;
      grid-template-columns: minmax(340px, .85fr) 1.15fr;
      min-height: 100vh;
    }

    /* ── Left panel ──────────────────────────────────────────────────────── */
    .left-panel {
      display: flex;
      flex-direction: column;
      background: linear-gradient(165deg, #203746 0%, #146d78 60%, #214c5a 100%);
      color: #edf7f8;
      overflow: hidden;
    }
    .left-inner {
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      flex: 1;
      padding: 36px clamp(24px, 6vw, 72px) 32px;
    }

    .brand {
      display: flex; align-items: center; gap: 10px;
      text-decoration: none; color: inherit;
    }
    .brand-mark {
      display: grid; place-items: center;
      width: 38px; aspect-ratio: 1; border-radius: 10px;
      background: #c6e8eb; color: #17313c;
      font-size: 18px; font-weight: 900;
    }
    .brand-name { font-size: 18px; font-weight: 750; letter-spacing: -.01em; }

    .hero-copy { margin: 60px 0 40px; }
    .hero-eyebrow {
      margin: 0 0 14px;
      color: #f0c47e; font-size: 11px; font-weight: 800;
      letter-spacing: .15em; text-transform: uppercase;
    }
    .hero-title {
      margin: 0 0 18px;
      font-size: clamp(36px, 4.5vw, 58px); font-weight: 720;
      line-height: 1.05; letter-spacing: -.02em;
    }
    .hero-sub { margin: 0; color: #c5e0e4; font-size: 15px; line-height: 1.65; max-width: 380px; }

    .feature-list {
      list-style: none; margin: 0; padding: 0;
      display: flex; flex-direction: column; gap: 14px;
    }
    .feature-list li {
      display: flex; align-items: flex-start; gap: 12px;
      font-size: 13.5px; color: #d4e8e9;
    }
    .feat-icon { font-size: 16px; flex-shrink: 0; }

    .left-foot {
      display: flex; align-items: center; gap: 9px;
      margin-top: 40px;
      color: #b4d4d8; font-size: 12px;
    }
    .live-dot {
      width: 8px; aspect-ratio: 1; border-radius: 50%;
      background: #efbd68;
      box-shadow: 0 0 0 4px #efbd6830;
      animation: pulse 2.4s ease infinite;
    }
    @keyframes pulse {
      0%,100% { box-shadow: 0 0 0 4px #efbd6830; }
      50%      { box-shadow: 0 0 0 8px #efbd6814; }
    }

    /* ── Right panel ─────────────────────────────────────────────────────── */
    .right-panel {
      display: grid; place-items: center;
      padding: 40px 24px;
      background: #f1f5f7;
      overflow-y: auto;
    }
    .form-shell {
      width: min(100%, 460px);
      display: flex; flex-direction: column; gap: 0;
    }

    /* ── Section heading ─────────────────────────────────────────────────── */
    .section-heading { margin-bottom: 28px; }
    .eyebrow {
      margin: 0 0 10px;
      color: #147e8a; font-size: 10px; font-weight: 800;
      letter-spacing: .13em; text-transform: uppercase;
    }
    h2 {
      margin: 0 0 8px;
      color: #20313d; font-size: 30px; font-weight: 720;
      letter-spacing: -.02em;
    }
    .section-sub { margin: 0; color: #71828a; font-size: 13.5px; line-height: 1.6; }

    /* ── Alert ───────────────────────────────────────────────────────────── */
    .alert {
      padding: 12px 14px; border-radius: 6px; margin-bottom: 16px;
      font-size: 13px;
    }
    .alert-error { background: #fff1f0; border: 1px solid #ffc5c5; color: #882626; }

    /* ── Form ────────────────────────────────────────────────────────────── */
    .form-body {
      display: flex; flex-direction: column; gap: 16px;
    }
    .form-body label {
      display: flex; flex-direction: column; gap: 6px;
      font-size: 13px; font-weight: 650; color: #334953;
    }
    .form-body input {
      height: 46px; padding: 0 13px;
      border: 1.5px solid #c8d7dc; border-radius: 7px;
      background: #fff; color: #203642;
      font: inherit; font-size: 14px;
      transition: border-color .15s;
    }
    .form-body input:focus {
      outline: none;
      border-color: #147e8a;
      box-shadow: 0 0 0 3px #147e8a20;
    }
    .form-body small { color: #b33; font-size: 12px; }
    .password-wrap { position: relative; }
    .password-wrap input { width: 100%; padding-right: 44px; }
    .pass-toggle {
      position: absolute; right: 0; top: 0; height: 100%; width: 44px;
      border: 0; background: none; cursor: pointer; font-size: 16px;
    }

    /* ── Buttons ─────────────────────────────────────────────────────────── */
    .primary-btn {
      display: flex; align-items: center; justify-content: center; gap: 8px;
      height: 48px; border: 0; border-radius: 7px;
      background: #157d87; color: #fff;
      font: inherit; font-size: 14px; font-weight: 700;
      cursor: pointer; transition: background .15s, transform .1s;
    }
    .primary-btn:hover:not(:disabled) { background: #0e6570; }
    .primary-btn:active:not(:disabled) { transform: scale(.98); }
    .primary-btn:disabled { opacity: .6; cursor: wait; }
    .spinner {
      width: 16px; height: 16px;
      border: 2.5px solid #ffffff50;
      border-top-color: #fff;
      border-radius: 50%;
      animation: spin .7s linear infinite;
    }
    @keyframes spin { to { transform: rotate(360deg); } }

    .secondary-btn {
      height: 44px; border: 1.5px solid #c3d4da; border-radius: 7px;
      background: #fff; color: #334953;
      font: inherit; font-size: 13px; font-weight: 650; cursor: pointer;
      transition: background .15s;
    }
    .secondary-btn:hover { background: #e8f3f5; }

    .back-link, .text-link {
      border: 0; background: none; padding: 0;
      color: #147e8a; font: inherit; font-size: 13px;
      font-weight: 650; text-decoration: underline;
      text-underline-offset: 3px; cursor: pointer;
    }
    .back-link { margin-top: 4px; align-self: flex-start; }

    /* ── Divider ─────────────────────────────────────────────────────────── */
    .divider {
      display: flex; align-items: center; gap: 12px;
      margin: 18px 0;
      color: #82979e; font-size: 12px;
    }
    .divider::before, .divider::after {
      content: ''; flex: 1; height: 1px; background: #dce7ea;
    }

    /* ── Form links ──────────────────────────────────────────────────────── */
    .form-links {
      display: flex; justify-content: space-between; align-items: center;
      margin-top: 8px;
    }

    /* ── Method list ─────────────────────────────────────────────────────── */
    .method-list { display: flex; flex-direction: column; gap: 10px; margin-bottom: 28px; }
    .method-btn {
      display: flex; align-items: center; gap: 14px;
      padding: 14px 16px; border: 1.5px solid #c8d7dc; border-radius: 9px;
      background: #fff; text-align: left; cursor: pointer;
      transition: border-color .15s, box-shadow .15s;
    }
    .method-btn:hover {
      border-color: #147e8a;
      box-shadow: 0 2px 12px #147e8a20;
    }
    .method-icon { font-size: 22px; flex-shrink: 0; }
    .method-copy { display: flex; flex-direction: column; gap: 4px; flex: 1; }
    .method-copy strong { color: #203642; font-size: 14px; font-weight: 700; }
    .method-copy small { color: #71848b; font-size: 12px; }
    .method-arrow { color: #82979e; font-size: 20px; flex-shrink: 0; }

    /* ── Social buttons ──────────────────────────────────────────────────── */
    .social-grid { display: flex; flex-direction: column; gap: 10px; margin-bottom: 20px; }
    .social-btn {
      display: flex; align-items: center; gap: 12px;
      height: 48px; padding: 0 16px;
      border: 1.5px solid #c8d7dc; border-radius: 7px;
      background: #fff; color: #203642;
      font: inherit; font-size: 14px; font-weight: 600; cursor: pointer;
      transition: background .15s, border-color .15s;
    }
    .social-btn:hover { background: #edf6f7; border-color: #9bc9ce; }

    /* ── Demo grid ───────────────────────────────────────────────────────── */
    .divider-label {
      display: flex; align-items: center; gap: 10px;
      margin: 6px 0 14px; color: #81969d; font-size: 11.5px;
    }
    .divider-label::before, .divider-label::after {
      content: ''; flex: 1; height: 1px; background: #dce7ea;
    }

    .demo-grid {
      display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px;
      margin-bottom: 14px;
    }
    .demo-card {
      display: flex; flex-direction: column; gap: 4px;
      padding: 12px 10px; border-radius: 8px;
      border: 1.5px solid #dce7ea;
      background: #fff; text-align: left; cursor: pointer;
      transition: border-color .15s, box-shadow .15s, transform .1s;
    }
    .demo-card:hover {
      border-color: var(--accent, #147e8a);
      box-shadow: 0 2px 12px color-mix(in srgb, var(--accent, #147e8a) 15%, transparent);
      transform: translateY(-1px);
    }
    .demo-role {
      font-size: 9px; font-weight: 800; text-transform: uppercase;
      letter-spacing: .1em; color: var(--accent, #147e8a);
    }
    .demo-user { font-size: 13px; font-weight: 750; color: #203642; }
    .demo-desc { font-size: 10px; color: #788d95; line-height: 1.4; }

    .security-note { margin: 0; color: #81969d; font-size: 11.5px; text-align: center; }

    .success-note {
      padding: 12px; border-radius: 6px;
      background: #f0fbf4; border: 1px solid #b7e4c7;
      color: #216b42; font-size: 13px; line-height: 1.5;
    }

    /* ── Responsive ──────────────────────────────────────────────────────── */
    @media (max-width: 820px) {
      .page { grid-template-columns: 1fr; }
      .left-panel {
        padding: 0;
        min-height: auto;
      }
      .left-inner { padding: 24px 22px 20px; }
      .hero-copy { margin: 32px 0 24px; }
      .hero-title { font-size: 34px; }
      .feature-list { display: none; }
      .left-foot { margin-top: 20px; }
      .demo-grid { grid-template-columns: repeat(2, 1fr); }
    }
    @media (max-width: 420px) {
      .demo-grid { grid-template-columns: 1fr; }
      h2 { font-size: 26px; }
    }
  `]
})
export class LoginComponent {
  private readonly fb     = inject(FormBuilder);
  private readonly auth   = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly busy        = signal(false);
  protected readonly errorMsg    = signal('');
  protected readonly showPass    = signal(false);
  protected readonly forgotSent  = signal(false);
  protected forgotEmail          = '';
  protected readonly mode        = signal<'landing' | 'email' | 'social' | 'forgot'>('landing');

  protected readonly form = this.fb.nonNullable.group({
    username: ['', Validators.required],
    password: ['', Validators.required]
  });

  protected readonly demoAccounts: DemoAccount[] = [
    { role: 'Admin',       username: 'admin',       password: 'Admin@123', color: '#157d87', description: 'Full system access' },
    { role: 'Sales',       username: 'sales',       password: 'Admin@123', color: '#2563eb', description: 'Orders & customers' },
    { role: 'Production',  username: 'production',  password: 'Admin@123', color: '#7c3aed', description: 'Production & BOM' },
    { role: 'Procurement', username: 'procurement', password: 'Admin@123', color: '#b45309', description: 'Purchase orders' },
    { role: 'Warehouse',   username: 'warehouse',   password: 'Admin@123', color: '#0e7490', description: 'Inventory & receipts' },
    { role: 'Finance',     username: 'finance',     password: 'Admin@123', color: '#be185d', description: 'Invoices & payments' }
  ];

  protected submit(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.busy()) return;
    this.errorMsg.set('');
    this.busy.set(true);
    this.auth.login(this.form.getRawValue())
      .pipe(finalize(() => this.busy.set(false)))
      .subscribe({
        next: () => this.router.navigate(['/dashboard']),
        error: err => this.errorMsg.set(err.error?.message || 'Invalid username or password.')
      });
  }

  protected quickLogin(account: DemoAccount): void {
    this.mode.set('email');
    this.form.setValue({ username: account.username, password: account.password });
    this.submit();
  }

  protected socialNotice(provider: string): void {
    this.errorMsg.set('');
    alert(`${provider} OAuth is not configured in this development environment.\n\nUse the demo accounts below or sign in with your OrderCraft username and password.\n\nTo enable social login, configure OAuth client IDs in the backend settings.`);
  }

  protected submitForgot(): void {
    this.forgotSent.set(true);
  }
}
import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { Role } from '../core/models/models';
import { AuthService } from '../core/services/auth.service';

interface NavigationItem {
  label: string;
  route: string;
  group: string;
  roles?: Role[];
}

@Component({
  selector: 'app-shell',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  template: `
    <div class="app-frame">
      <aside class="sidebar" [class.sidebar-open]="menuOpen">
        <a class="brand" routerLink="/dashboard"><span class="brand-mark">O</span><span>OrderCraft</span></a>
        <div class="workspace-label">MANUFACTURING OMS</div>
        <nav aria-label="Main navigation">
          @for (group of groups; track group) {
            <div class="nav-group">
              <p>{{ group }}</p>
              @for (item of navigation; track item.route) {
                @if (item.group === group && (!item.roles || auth.hasAnyRole(item.roles))) {
                  <a [routerLink]="item.route" routerLinkActive="nav-active" (click)="menuOpen = false">
                    <span class="nav-marker"></span>{{ item.label }}
                  </a>
                }
              }
            </div>
          }
        </nav>
        <div class="sidebar-user">
          <div class="user-avatar">{{ initials }}</div>
          <div class="user-copy"><strong>{{ auth.currentUser()?.fullName }}</strong><span>{{ roleLabel }}</span></div>
          <button class="logout-button" type="button" title="Sign out" aria-label="Sign out" (click)="auth.logout()">↗</button>
        </div>
      </aside>
      <div class="main-column">
        <header class="topbar">
          <button class="menu-button" type="button" aria-label="Toggle navigation" (click)="menuOpen = !menuOpen">☰</button>
          <div class="breadcrumb">OrderCraft <span>/</span> <strong>Operations</strong></div>
          <div class="topbar-user">{{ auth.currentUser()?.fullName }}</div>
        </header>
        <main class="workspace"><router-outlet /></main>
      </div>
    </div>
  `,
  styles: [`
    :host { display: block; min-height: 100vh; }
    .app-frame { min-height: 100vh; display: grid; grid-template-columns: 252px minmax(0, 1fr); background: #f3f6f4; color: #1a2922; }
    .sidebar { position: sticky; top: 0; display: flex; flex-direction: column; height: 100vh; box-sizing: border-box; padding: 24px 16px 14px; background: #14231f; color: #f5faf6; }
    .brand { display: flex; align-items: center; gap: 10px; padding: 0 10px; color: inherit; text-decoration: none; font-size: 17px; font-weight: 750; }
    .brand-mark { display: grid; place-items: center; width: 30px; aspect-ratio: 1; border-radius: 8px; background: #b8e2c7; color: #14231f; font-weight: 800; }
    .workspace-label { margin: 35px 10px 12px; color: #81968b; font-size: 10px; font-weight: 800; letter-spacing: .12em; }
    nav { flex: 1; overflow-y: auto; }
    .nav-group { margin-bottom: 21px; }
    .nav-group p { margin: 0 10px 8px; color: #83978c; font-size: 10px; font-weight: 700; text-transform: uppercase; letter-spacing: .1em; }
    .nav-group a { display: flex; align-items: center; gap: 11px; min-height: 38px; margin: 2px 0; padding: 0 10px; border-radius: 5px; color: #c0cdc5; text-decoration: none; font-size: 13px; }
    .nav-group a:hover { background: #ffffff12; color: white; }
    .nav-group a.nav-active { background: #264b3a; color: #d7f5df; }
    .nav-marker { width: 6px; height: 6px; border: 1px solid #71877a; border-radius: 2px; }
    .nav-active .nav-marker { border-color: #91d4a8; background: #91d4a8; }
    .sidebar-user { display: flex; align-items: center; gap: 10px; padding: 14px 7px 3px; border-top: 1px solid #ffffff20; }
    .user-avatar { display: grid; place-items: center; width: 34px; aspect-ratio: 1; border-radius: 50%; background: #315844; color: #d7f5df; font-size: 12px; font-weight: 700; }
    .user-copy { display: flex; min-width: 0; flex: 1; flex-direction: column; gap: 3px; }
    .user-copy strong { overflow: hidden; color: #f5faf6; font-size: 11px; text-overflow: ellipsis; white-space: nowrap; }
    .user-copy span { color: #9aada2; font-size: 10px; }
    .logout-button, .menu-button { border: 0; background: none; color: inherit; cursor: pointer; }
    .logout-button { color: #c0cdc5; font-size: 17px; }
    .main-column { min-width: 0; }
    .topbar { position: sticky; z-index: 2; top: 0; display: flex; align-items: center; justify-content: space-between; height: 62px; padding: 0 32px; border-bottom: 1px solid #e0e7e2; background: #fff; }
    .breadcrumb { color: #829087; font-size: 12px; }
    .breadcrumb span { margin: 0 9px; color: #c2cbc5; }
    .breadcrumb strong { color: #314239; font-weight: 650; }
    .topbar-user { color: #59685f; font-size: 12px; }
    .menu-button { display: none; color: #26362e; font-size: 18px; }
    .workspace { max-width: 1500px; margin: 0 auto; padding: 32px; }
    @media (max-width: 860px) { .app-frame { display: block; } .sidebar { position: fixed; z-index: 5; left: -270px; width: 252px; transition: left .2s ease; box-shadow: 12px 0 32px #0e1c1826; } .sidebar.sidebar-open { left: 0; } .menu-button { display: inline-flex; } .topbar { justify-content: flex-start; gap: 16px; padding: 0 18px; } .topbar-user { margin-left: auto; } .workspace { padding: 22px 18px; } }
  `]
})
export class AppShellComponent {
  protected readonly auth = inject(AuthService);
  protected menuOpen = false;
  protected readonly groups = ['Workspace', 'Supply chain', 'Finance & insight'];
  protected readonly navigation: NavigationItem[] = [
    { label: 'Dashboard', route: '/dashboard', group: 'Workspace' },
    { label: 'Customer orders', route: '/orders', group: 'Workspace', roles: ['ADMIN', 'SALES_MANAGER', 'PRODUCTION_MANAGER'] },
    { label: 'Customers', route: '/customers', group: 'Workspace', roles: ['ADMIN', 'SALES_MANAGER'] },
    { label: 'Products', route: '/products', group: 'Workspace' },
    { label: 'Bills of material', route: '/boms', group: 'Workspace', roles: ['ADMIN', 'PRODUCTION_MANAGER'] },
    { label: 'Production', route: '/production-orders', group: 'Workspace', roles: ['ADMIN', 'PRODUCTION_MANAGER'] },
    { label: 'Inventory', route: '/inventory', group: 'Supply chain', roles: ['ADMIN', 'WAREHOUSE_MANAGER', 'PRODUCTION_MANAGER', 'PROCUREMENT_MANAGER'] },
    { label: 'Material shortages', route: '/material-shortages', group: 'Supply chain', roles: ['ADMIN', 'PROCUREMENT_MANAGER', 'PRODUCTION_MANAGER'] },
    { label: 'Suppliers', route: '/suppliers', group: 'Supply chain', roles: ['ADMIN', 'PROCUREMENT_MANAGER'] },
    { label: 'Purchase orders', route: '/purchase-orders', group: 'Supply chain', roles: ['ADMIN', 'PROCUREMENT_MANAGER', 'WAREHOUSE_MANAGER'] },
    { label: 'Goods receipts', route: '/goods-receipts', group: 'Supply chain', roles: ['ADMIN', 'WAREHOUSE_MANAGER', 'PROCUREMENT_MANAGER'] },
    { label: 'Invoices', route: '/invoices', group: 'Finance & insight', roles: ['ADMIN', 'FINANCE_MANAGER', 'SALES_MANAGER'] },
    { label: 'Payments', route: '/payments', group: 'Finance & insight', roles: ['ADMIN', 'FINANCE_MANAGER'] },
    { label: 'Reports', route: '/reports', group: 'Finance & insight' },
    { label: 'Audit log', route: '/audit-logs', group: 'Finance & insight', roles: ['ADMIN'] },
    { label: 'Users', route: '/users', group: 'Finance & insight', roles: ['ADMIN'] }
  ];

  protected get roleLabel(): string {
    return this.auth.currentRole()?.replaceAll('_', ' ') ?? '';
  }

  protected get initials(): string {
    return (this.auth.currentUser()?.fullName ?? 'U').split(/\s+/).slice(0, 2).map(part => part[0]).join('').toUpperCase();
  }
}
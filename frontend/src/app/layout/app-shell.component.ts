import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { Role } from '../core/models/models';
import { AuthService } from '../core/services/auth.service';

interface NavigationItem {
  label: string;
  route: string;
  group: string;
  primary?: boolean;
  roles?: Role[];
}

@Component({
  selector: 'app-shell',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  template: `
    <div class="app-frame" (keydown.escape)="menuOpen = false">
      <header class="topbar">
        <div class="brand-area">
          <button class="menu-button" type="button" aria-label="Open menu" [attr.aria-expanded]="menuOpen" aria-controls="feature-menu" (click)="menuOpen = !menuOpen">
            <span class="menu-glyph" aria-hidden="true"><i></i><i></i><i></i></span>
            <span>Menu</span>
          </button>
          <a class="brand" routerLink="/dashboard"><span class="brand-mark">O</span><span>OrderCraft</span></a>
        </div>
        <nav class="primary-nav" aria-label="Primary navigation">
          @for (item of primaryNavigation; track item.route) {
            <a [routerLink]="item.route" routerLinkActive="nav-active">{{ item.label }}</a>
          }
        </nav>
      </header>
      @if (menuOpen) {
        <button class="menu-scrim" type="button" aria-label="Close feature menu" (click)="menuOpen = false"></button>
        <aside class="feature-menu" id="feature-menu" aria-label="Menu">
          <div class="menu-heading"><div><span>ORDERCRAFT</span><h2>Menu</h2></div><button type="button" class="close-menu" aria-label="Close menu" (click)="menuOpen = false">×</button></div>
          <nav aria-label="All application features">
            <section class="nav-group mobile-primary" aria-label="Quick access">
              <h3>Quick access</h3>
              @for (item of primaryNavigation; track item.route) {
                <a [routerLink]="item.route" routerLinkActive="nav-active" (click)="menuOpen = false">
                  <span class="nav-marker"></span>{{ item.label }}
                </a>
              }
            </section>
            @for (group of groups; track group) {
              <section class="nav-group" [attr.aria-label]="group">
                <h3>{{ group }}</h3>
                @for (item of navigation; track item.route) {
                  @if (item.group === group && !item.primary && (!item.roles || auth.hasAnyRole(item.roles))) {
                    <a [routerLink]="item.route" routerLinkActive="nav-active" (click)="menuOpen = false">
                      <span class="nav-marker"></span>{{ item.label }}
                    </a>
                  }
                }
              </section>
            }
          </nav>
          <div class="drawer-account">
            <span class="user-avatar">{{ initials }}</span>
            <span class="account-copy"><strong>{{ auth.currentUser()?.fullName }}</strong><small>{{ roleLabel }}</small></span>
            <button class="logout-button" type="button" title="Sign out" aria-label="Sign out" (click)="auth.logout()">↗</button>
          </div>
        </aside>
      }
      <main class="workspace"><router-outlet /></main>
    </div>
  `,
  styles: [`
    :host { --nav-forest: #203746; --nav-teal: #147e8a; --nav-mint: #e1f1f3; display: block; min-height: 100vh; }
    .app-frame { min-height: 100vh; background: #f1f5f7; color: #20313d; }
    .topbar { position: sticky; z-index: 10; top: 0; display: flex; align-items: center; gap: 28px; min-height: 68px; padding: 0 30px; border-bottom: 1px solid #dbe5e9; background: #fbfcfd; }
    .brand-area { display: flex; flex: 0 0 auto; align-items: center; gap: 16px; }
    .brand { display: flex; flex: 0 0 auto; align-items: center; gap: 10px; color: #20313d; text-decoration: none; font-size: 17px; font-weight: 750; }
    .brand-mark { display: grid; place-items: center; width: 32px; aspect-ratio: 1; border-radius: 8px; background: #c6e8eb; color: #17313c; font-weight: 800; }
    .primary-nav { display: flex; align-self: stretch; align-items: center; gap: 4px; min-width: 0; flex: 1; }
    .primary-nav a { position: relative; display: flex; align-self: stretch; align-items: center; margin: 6px 0; padding: 0 9px; border-radius: 5px; color: #526873; text-decoration: none; font-size: 13px; font-weight: 600; white-space: nowrap; transition: background-color .18s ease, color .18s ease, transform .18s ease, box-shadow .18s ease; }
    .primary-nav a:hover { z-index: 1; transform: translateY(-2px) scale(1.025); background: var(--nav-mint); box-shadow: 0 5px 14px #126d7426; color: #116b75; }
    .primary-nav a.nav-active { background: #eef6f7; color: #116b75; }
    .primary-nav a.nav-active::after { position: absolute; right: 9px; bottom: -6px; left: 9px; height: 3px; border-radius: 3px; background: var(--nav-teal); content: ''; }
    .primary-nav a:focus-visible { outline: 2px solid var(--nav-teal); outline-offset: 2px; }
    .user-avatar { display: grid; flex: 0 0 auto; place-items: center; width: 34px; aspect-ratio: 1; border-radius: 50%; background: #2d5962; color: #e0f2f3; font-size: 11px; font-weight: 750; }
    .account-copy { display: flex; min-width: 0; flex: 1; flex-direction: column; gap: 3px; }
    .account-copy strong { overflow: hidden; color: #f5faf6; font-size: 11px; text-overflow: ellipsis; white-space: nowrap; }
    .account-copy small { color: #a9c2c8; font-size: 10px; text-transform: capitalize; }
    .logout-button, .menu-button, .close-menu { border: 0; cursor: pointer; }
    .logout-button { padding: 5px; background: transparent; color: #c7d9de; font-size: 17px; }
    .menu-button { display: inline-flex; align-items: center; gap: 9px; min-height: 38px; padding: 0 12px; border: 1px solid #c7d8de; border-radius: 5px; background: #fff; color: #294b55; font-size: 12px; font-weight: 650; transition: background-color .18s ease, border-color .18s ease, box-shadow .18s ease, transform .18s ease; }
    .menu-button:hover, .menu-button[aria-expanded='true'] { transform: translateY(-1px); border-color: #6aaeb6; background: var(--nav-mint); box-shadow: 0 4px 12px #126d7420; }
    .menu-glyph { display: grid; gap: 3px; width: 15px; }
    .menu-glyph i { display: block; height: 2px; border-radius: 2px; background: currentColor; }
    .workspace { max-width: 1500px; margin: 0 auto; padding: 32px; }
    .menu-scrim { position: fixed; z-index: 7; inset: 68px 0 0; border: 0; background: #12293470; cursor: default; }
    .feature-menu { position: fixed; z-index: 8; inset: 68px auto 0 0; display: flex; box-sizing: border-box; width: max-content; min-width: 220px; max-width: min(300px, 88vw); flex-direction: column; padding: 20px 14px 12px; overflow: hidden; background: var(--nav-forest); box-shadow: 14px 0 36px #0e1c1830; color: #f4faf6; animation: menu-in .22s ease-out; }
    .menu-heading { display: flex; align-items: flex-start; justify-content: space-between; padding: 0 5px 18px; border-bottom: 1px solid #ffffff20; }
    .menu-heading span { color: #e7bd78; font-size: 9px; font-weight: 800; }
    .menu-heading h2 { margin: 5px 0 0; color: #f4faf6; font-size: 20px; }
    .close-menu { display: grid; place-items: center; width: 32px; aspect-ratio: 1; border-radius: 4px; background: #ffffff12; color: #edf5ef; font-size: 23px; line-height: 1; transition: background-color .16s ease, transform .16s ease; }
    .close-menu:hover { transform: rotate(6deg); background: #ffffff24; }
    .feature-menu nav { min-height: 0; flex: 1; overflow-y: auto; }
    .drawer-account { display: flex; flex: 0 0 auto; align-items: center; gap: 10px; margin-top: 10px; padding: 12px 5px 3px; border-top: 1px solid #ffffff24; }
    .nav-group { margin-top: 18px; }
    .mobile-primary { display: none; }
    .nav-group h3 { margin: 0 7px 6px; color: #a8ccd1; font-size: 10px; font-weight: 750; text-transform: uppercase; }
    .nav-group a { display: flex; align-items: center; gap: 9px; min-height: 36px; margin: 1px 0; padding: 0 8px; border-radius: 4px; color: #c9dce1; text-decoration: none; font-size: 13px; transition: background-color .18s ease, color .18s ease, transform .18s ease, box-shadow .18s ease; }
    .nav-group a:hover { transform: translateX(3px); background: #28636f; box-shadow: inset 3px 0 #e7bd78; color: #fff; }
    .nav-group a.nav-active { background: #245c66; color: #e0f2f3; }
    .nav-marker { width: 6px; height: 6px; border: 1px solid #9bbac1; border-radius: 2px; }
    .nav-active .nav-marker { border-color: #a9e8ec; background: #a9e8ec; }
    @keyframes menu-in { from { opacity: .82; transform: translateX(-20px); } to { opacity: 1; transform: translateX(0); } }
    @media (max-width: 1050px) { .topbar { gap: 18px; padding: 0 20px; } .brand-area { gap: 11px; } .primary-nav { gap: 0; } .primary-nav a { padding: 0 7px; font-size: 12px; } }
    @media (max-width: 820px) { .topbar { min-height: 62px; gap: 12px; padding: 0 16px; } .brand-area { gap: 10px; } .primary-nav { display: none; } .mobile-primary { display: block; } .menu-scrim { inset-block-start: 62px; } .feature-menu { inset-block-start: 62px; } .workspace { padding: 22px 18px; } }
    @media (max-width: 420px) { .topbar { padding: 0 11px; } .brand-area { gap: 8px; } .menu-button { gap: 7px; min-height: 36px; padding: 0 9px; font-size: 11px; } .workspace { padding: 18px 12px; } }
    @media (prefers-reduced-motion: reduce) { .feature-menu { animation: none; } }
  `]
})
export class AppShellComponent {
  protected readonly auth = inject(AuthService);
  protected menuOpen = false;
  protected readonly groups = ['Workspace', 'Supply chain', 'Finance & insight'];
  protected readonly navigation: NavigationItem[] = [
    { label: 'Dashboard', route: '/dashboard', group: 'Workspace', primary: true },
    { label: 'Customer orders', route: '/orders', group: 'Workspace', primary: true, roles: ['ADMIN', 'SALES_MANAGER', 'PRODUCTION_MANAGER'] },
    { label: 'Customers', route: '/customers', group: 'Workspace', roles: ['ADMIN', 'SALES_MANAGER'] },
    { label: 'Products', route: '/products', group: 'Workspace', primary: true },
    { label: 'Bills of material', route: '/boms', group: 'Workspace', roles: ['ADMIN', 'PRODUCTION_MANAGER'] },
    { label: 'Production', route: '/production-orders', group: 'Workspace', primary: true, roles: ['ADMIN', 'PRODUCTION_MANAGER'] },
    { label: 'Inventory', route: '/inventory', group: 'Supply chain', primary: true, roles: ['ADMIN', 'WAREHOUSE_MANAGER', 'PRODUCTION_MANAGER', 'PROCUREMENT_MANAGER'] },
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

  protected get primaryNavigation(): NavigationItem[] {
    return this.navigation.filter(item => item.primary && (!item.roles || this.auth.hasAnyRole(item.roles)));
  }

  protected get roleLabel(): string {
    return this.auth.currentRole()?.replaceAll('_', ' ') ?? '';
  }

  protected get initials(): string {
    return (this.auth.currentUser()?.fullName ?? 'U').split(/\s+/).slice(0, 2).map(part => part[0]).join('').toUpperCase();
  }
}
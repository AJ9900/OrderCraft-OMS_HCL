import { Component, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { ApiService } from '../../core/services/api.service';
import { DashboardStats } from '../../core/models/models';

@Component({
  selector: 'app-dashboard',
  imports: [CurrencyPipe, DatePipe],
  template: `
    <section class="dashboard">
      <header class="page-heading">
        <div><p class="eyebrow">OPERATIONS OVERVIEW</p><h1>Dashboard</h1><p class="subheading">A live view of orders, materials, and cash flow.</p></div>
        <span class="updated"><span></span> Live data</span>
      </header>
      @if (loading()) {
        <div class="loading-state"><span class="spinner"></span>Loading operations data…</div>
      } @else if (error()) {
        <div class="error-state" role="alert"><strong>Dashboard unavailable</strong><span>{{ error() }}</span><button type="button" (click)="load()">Retry</button></div>
      } @else if (stats(); as data) {
        <div class="metric-grid">
          <article class="metric"><span>Total orders</span><strong>{{ data.totalOrders }}</strong><small>All recorded orders</small></article>
          <article class="metric"><span>Pending orders</span><strong>{{ data.pendingOrders }}</strong><small>Awaiting next step</small></article>
          <article class="metric metric-green"><span>In production</span><strong>{{ data.ordersInProduction }}</strong><small>Active production and QC</small></article>
          <article class="metric"><span>Completed orders</span><strong>{{ data.completedOrders }}</strong><small>Fulfilled orders</small></article>
          <article class="metric metric-amber"><span>Low stock</span><strong>{{ data.lowStockCount }}</strong><small>At or below minimum stock</small></article>
          <article class="metric"><span>Pending purchase orders</span><strong>{{ data.pendingPurchaseOrders }}</strong><small>Draft, sent, or partial</small></article>
          <article class="metric"><span>Pending invoices</span><strong>{{ data.pendingInvoices }}</strong><small>Issued or partly paid</small></article>
          <article class="metric metric-green"><span>Outstanding</span><strong>{{ data.outstandingPaymentsAmount | currency }}</strong><small>Balance due</small></article>
        </div>
        <section class="data-grid">
          <article class="panel">
            <div class="panel-heading"><div><p class="eyebrow">ORDER FLOW</p><h2>Recent orders</h2></div><span>{{ data.recentOrders.length }} latest</span></div>
            @if (data.recentOrders.length) {
              <div class="table-wrap"><table><thead><tr><th>Order</th><th>Customer</th><th>Delivery</th><th>Status</th><th class="number">Total</th></tr></thead><tbody>
                @for (order of data.recentOrders; track order.id) {
                  <tr><td class="primary-cell">{{ order.orderNumber }}</td><td>{{ order.customer.name || '—' }}</td><td>{{ order.expectedDeliveryDate | date:'mediumDate' }}</td><td><span class="status" [class]="'status status-' + order.status.toLowerCase().replace('_', '-')">{{ order.status.replaceAll('_', ' ') }}</span></td><td class="number">{{ order.grandTotal | currency }}</td></tr>
                }
              </tbody></table></div>
            } @else { <div class="empty-state">No orders have been recorded yet.</div> }
          </article>
          <article class="panel stock-panel">
            <div class="panel-heading"><div><p class="eyebrow">MATERIAL CONTROL</p><h2>Low stock</h2></div><span class="stock-count">{{ data.lowStockItems.length }}</span></div>
            @if (data.lowStockItems.length) {
              <div class="stock-list">
                @for (item of data.lowStockItems; track item.id) {
                  <div class="stock-item"><div class="stock-item-name"><strong>{{ item.product.productName }}</strong><span>{{ item.product.productCode }}</span></div><div class="stock-numbers"><b>{{ item.availableQuantity }} {{ item.product.unit }}</b><span>minimum {{ item.minimumStock }}</span></div></div>
                }
              </div>
            } @else { <div class="empty-state">No low-stock items.</div> }
          </article>
        </section>
      }
    </section>
  `,
  styles: [`
    :host { display: block; }
    .page-heading { display: flex; align-items: flex-end; justify-content: space-between; gap: 20px; margin-bottom: 25px; }
    .eyebrow { margin: 0 0 8px; color: #147e8a; font-size: 10px; font-weight: 800; letter-spacing: .12em; }
    h1 { margin: 0; color: #20313d; font-size: 28px; letter-spacing: 0; }
    .subheading { margin: 8px 0 0; color: #71828a; font-size: 13px; }
    .updated { display: flex; align-items: center; gap: 8px; color: #526873; font-size: 12px; }
    .updated span { width: 7px; aspect-ratio: 1; border-radius: 50%; background: #1b8d94; }
    .metric-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 12px; }
    .metric { min-width: 0; padding: 17px 18px 15px; border: 1px solid #dbe5e9; border-radius: 6px; background: #fff; }
    .metric > span { display: block; color: #52636d; font-size: 12px; font-weight: 600; }
    .metric strong { display: block; overflow: hidden; margin-top: 14px; color: #20313d; font-size: 28px; line-height: 1.1; text-overflow: ellipsis; white-space: nowrap; }
    .metric small { display: block; margin-top: 8px; color: #819098; font-size: 10px; }
    .metric-green { border-top: 3px solid #16838b; padding-top: 15px; }
    .metric-amber { border-top: 3px solid #d99834; padding-top: 15px; }
    .data-grid { display: grid; grid-template-columns: minmax(0, 1.65fr) minmax(270px, .85fr); gap: 14px; margin-top: 16px; }
    .panel { min-width: 0; border: 1px solid #dbe5e9; border-radius: 6px; background: #fff; }
    .panel-heading { display: flex; align-items: center; justify-content: space-between; padding: 17px 18px 14px; border-bottom: 1px solid #e7eef0; }
    .panel-heading .eyebrow { margin-bottom: 5px; }
    h2 { margin: 0; color: #243641; font-size: 15px; letter-spacing: 0; }
    .panel-heading > span { color: #819098; font-size: 11px; }
    .table-wrap { overflow-x: auto; }
    table { width: 100%; border-collapse: collapse; text-align: left; font-size: 11px; }
    th { padding: 11px 12px; color: #71838b; font-size: 10px; font-weight: 650; white-space: nowrap; }
    td { padding: 12px; border-top: 1px solid #edf2f4; color: #50616a; white-space: nowrap; }
    .primary-cell { color: #14727d; font-weight: 700; }
    .number { text-align: right; }
    .status { display: inline-flex; padding: 4px 7px; border-radius: 3px; background: #eef2ef; color: #526158; font-size: 9px; font-weight: 700; }
    .status-ready-for-production, .status-completed { background: #e3f3e9; color: #26704b; }
    .status-material-shortage, .status-cancelled { background: #fff0e8; color: #a6532f; }
    .status-in-production { background: #e7f0fb; color: #376b9c; }
    .stock-count { display: grid; place-items: center; min-width: 25px; height: 25px; border-radius: 50%; background: #fff2dc; color: #a96b11 !important; font-weight: 700; }
    .stock-list { padding: 0 18px; }
    .stock-item { display: flex; align-items: center; justify-content: space-between; gap: 12px; padding: 13px 0; border-bottom: 1px solid #e7eef0; }
    .stock-item:last-child { border-bottom: 0; }
    .stock-item-name, .stock-numbers { display: flex; flex-direction: column; gap: 5px; }
    .stock-item-name strong { color: #334b56; font-size: 11px; }
    .stock-item-name span, .stock-numbers span { color: #819098; font-size: 10px; }
    .stock-numbers { align-items: flex-end; text-align: right; }
    .stock-numbers b { color: #a96b11; font-size: 11px; }
    .empty-state { padding: 24px 18px; color: #859189; font-size: 12px; }
    .loading-state { display: flex; align-items: center; gap: 10px; padding: 50px 0; color: #66766c; font-size: 13px; }
    .spinner { width: 17px; height: 17px; border: 2px solid #d1dfe3; border-top-color: #147e8a; border-radius: 50%; animation: spin .8s linear infinite; }
    .error-state { display: grid; gap: 10px; max-width: 520px; padding: 20px; border: 1px solid #e8c3bd; border-radius: 6px; background: #fff; color: #7b4039; font-size: 13px; }
    .error-state button { justify-self: start; padding: 7px 12px; border: 1px solid #c7d6dc; border-radius: 4px; background: #fff; color: #156c75; cursor: pointer; }
    @keyframes spin { to { transform: rotate(360deg); } }
    @media (max-width: 1050px) { .metric-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } .data-grid { grid-template-columns: 1fr; } }
    @media (max-width: 520px) { .page-heading { align-items: flex-start; } .metric-grid { gap: 8px; } .metric { padding: 14px 12px; } .metric strong { font-size: 23px; } .updated { font-size: 0; } }
  `]
})
export class DashboardComponent {
  private readonly api = inject(ApiService);
  protected readonly stats = signal<DashboardStats | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal('');

  constructor() { this.load(); }

  protected load(): void {
    this.loading.set(true);
    this.error.set('');
    this.api.get<DashboardStats>('/dashboard/stats').subscribe({
      next: value => this.stats.set(value),
      error: failure => this.error.set(failure.error?.message || 'Could not load dashboard data from the backend.'),
      complete: () => this.loading.set(false)
    });
  }
}
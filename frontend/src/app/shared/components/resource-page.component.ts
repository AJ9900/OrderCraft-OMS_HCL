import { Component, DestroyRef, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ApiService } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';

interface ColumnDefinition {
  label: string;
  key: string;
}

interface FieldDefinition {
  key: string;
  label: string;
  type?: string;
  required?: boolean;
  options?: string[];
  multiline?: boolean;
  defaultValue?: string;
  source?: 'customer' | 'supplier' | 'finishedProduct' | 'rawMaterial' | 'customerOrder' | 'purchaseOrder' | 'invoice';
}

interface ResourceDefinition {
  title: string;
  endpoint: string;
  columns: ColumnDefinition[];
  paginated?: boolean;
  createFields?: FieldDefinition[];
  editFields?: FieldDefinition[];
  softDelete?: boolean;
  itemsMode?: 'order' | 'bom' | 'purchaseOrder' | 'goodsReceipt';
  createEndpoint?: string;
}

interface SelectOption {
  value: number;
  label: string;
}

interface ProductChoice {
  id: number;
  productCode: string;
  productName: string;
  sellingPrice: number;
  costPrice: number;
  unit: string;
}

interface CustomerOrderChoice {
  id: number;
  orderNumber: string;
  status: string;
  items: Array<{ product: ProductChoice; quantity: number }>;
}

interface PurchaseOrderChoice {
  id: number;
  poNumber: string;
  status: string;
  items: Array<{ id: number; material: ProductChoice; quantity: number; receivedQuantity: number }>;
}

interface InvoiceChoice {
  id: number;
  invoiceNumber: string;
  status: string;
  grandTotal: number;
  paidAmount: number;
}

interface PageData<T> {
  content: T[];
}

interface ReceiptItemChoice {
  id: number;
  productCode: string;
  productName: string;
  costPrice: number;
  sellingPrice: number;
  unit: string;
}

const resources: Record<string, ResourceDefinition> = {
  users: { title: 'User accounts', endpoint: '/admin/users', columns: [
    { label: 'Username', key: 'username' }, { label: 'Full name', key: 'fullName' },
    { label: 'Email', key: 'email' }, { label: 'Role', key: 'role' }, { label: 'Active', key: 'active' }
  ], createFields: [
    { key: 'username', label: 'Username', required: true }, { key: 'fullName', label: 'Full name', required: true },
    { key: 'email', label: 'Email', type: 'email', required: true }, { key: 'password', label: 'Temporary password (12+ characters)', type: 'password', required: true },
    { key: 'role', label: 'Role', options: ['ADMIN', 'SALES_MANAGER', 'PRODUCTION_MANAGER', 'PROCUREMENT_MANAGER', 'WAREHOUSE_MANAGER', 'FINANCE_MANAGER'], required: true }
  ] },
  orders: { title: 'Customer orders', endpoint: '/orders', columns: [
    { label: 'Order number', key: 'orderNumber' }, { label: 'Customer', key: 'customer.name' },
    { label: 'Order date', key: 'orderDate' }, { label: 'Delivery date', key: 'expectedDeliveryDate' },
    { label: 'Status', key: 'status' }, { label: 'Total', key: 'grandTotal' }
  ], paginated: true, itemsMode: 'order', createFields: [
    { key: 'customerId', label: 'Customer', source: 'customer', required: true },
    { key: 'orderDate', label: 'Order date', type: 'date', required: true, defaultValue: new Date().toISOString().slice(0, 10) },
    { key: 'expectedDeliveryDate', label: 'Expected delivery', type: 'date', required: true },
    { key: 'notes', label: 'Notes', multiline: true }
  ] },
  customers: { title: 'Customers', endpoint: '/customers', columns: [
    { label: 'Code', key: 'customerCode' }, { label: 'Customer', key: 'name' },
    { label: 'Email', key: 'email' }, { label: 'Phone', key: 'phone' }, { label: 'City', key: 'city' }, { label: 'Status', key: 'status' }
  ], paginated: true, softDelete: true, createFields: [
    { key: 'customerCode', label: 'Customer code', required: true }, { key: 'name', label: 'Customer name', required: true },
    { key: 'email', label: 'Email', type: 'email', required: true }, { key: 'phone', label: 'Phone', required: true },
    { key: 'address', label: 'Address', required: true }, { key: 'city', label: 'City', required: true },
    { key: 'state', label: 'State / region', required: true }, { key: 'country', label: 'Country', required: true },
    { key: 'postalCode', label: 'Postal code', required: true }, { key: 'status', label: 'Status', options: ['ACTIVE', 'INACTIVE'], defaultValue: 'ACTIVE' }
  ], editFields: [
    { key: 'customerCode', label: 'Customer code', required: true }, { key: 'name', label: 'Customer name', required: true },
    { key: 'email', label: 'Email', type: 'email', required: true }, { key: 'phone', label: 'Phone', required: true },
    { key: 'address', label: 'Address', required: true }, { key: 'city', label: 'City', required: true },
    { key: 'state', label: 'State / region', required: true }, { key: 'country', label: 'Country', required: true },
    { key: 'postalCode', label: 'Postal code', required: true }, { key: 'status', label: 'Status', options: ['ACTIVE', 'INACTIVE'], defaultValue: 'ACTIVE' }
  ] },
  products: { title: 'Products & materials', endpoint: '/products', columns: [
    { label: 'Code', key: 'productCode' }, { label: 'Product', key: 'productName' },
    { label: 'Type', key: 'type' }, { label: 'Category', key: 'category' }, { label: 'Unit', key: 'unit' }, { label: 'Status', key: 'status' }
  ], paginated: true, softDelete: true, createFields: [
    { key: 'productCode', label: 'Product / material code', required: true }, { key: 'productName', label: 'Name', required: true },
    { key: 'description', label: 'Description', multiline: true }, { key: 'category', label: 'Category', required: true },
    { key: 'type', label: 'Type', options: ['FINISHED_GOOD', 'RAW_MATERIAL'], defaultValue: 'FINISHED_GOOD' },
    { key: 'sellingPrice', label: 'Selling price', type: 'number', required: true, defaultValue: '0' },
    { key: 'costPrice', label: 'Cost price', type: 'number', required: true, defaultValue: '0' },
    { key: 'unit', label: 'Unit', required: true, defaultValue: 'PCS' },
    { key: 'initialQuantity', label: 'Starting quantity', type: 'number', defaultValue: '0' },
    { key: 'minimumStock', label: 'Minimum stock', type: 'number', defaultValue: '10' },
    { key: 'reorderLevel', label: 'Reorder level', type: 'number', defaultValue: '20' }
  ], editFields: [
    { key: 'productCode', label: 'Product / material code', required: true }, { key: 'productName', label: 'Name', required: true },
    { key: 'description', label: 'Description', multiline: true }, { key: 'category', label: 'Category', required: true },
    { key: 'type', label: 'Type', options: ['FINISHED_GOOD', 'RAW_MATERIAL'] },
    { key: 'sellingPrice', label: 'Selling price', type: 'number', required: true },
    { key: 'costPrice', label: 'Cost price', type: 'number', required: true }, { key: 'unit', label: 'Unit', required: true },
    { key: 'status', label: 'Status', options: ['ACTIVE', 'INACTIVE'], defaultValue: 'ACTIVE' }
  ] },
  boms: { title: 'Bills of material', endpoint: '/boms', columns: [
    { label: 'BOM code', key: 'bomCode' }, { label: 'Product', key: 'product.productName' },
    { label: 'Version', key: 'version' }, { label: 'Effective date', key: 'effectiveDate' }, { label: 'Status', key: 'status' }
  ], paginated: true, itemsMode: 'bom', createFields: [
    { key: 'bomCode', label: 'BOM code', required: true }, { key: 'productId', label: 'Finished product', source: 'finishedProduct', required: true },
    { key: 'version', label: 'Version', required: true, defaultValue: '1.0' },
    { key: 'status', label: 'Status', options: ['ACTIVE', 'DRAFT', 'ARCHIVED'], defaultValue: 'ACTIVE' },
    { key: 'effectiveDate', label: 'Effective date', type: 'date', required: true, defaultValue: new Date().toISOString().slice(0, 10) },
    { key: 'notes', label: 'Notes', multiline: true }
  ] },
  inventory: { title: 'Inventory', endpoint: '/inventory', columns: [
    { label: 'Material / product', key: 'product.productName' }, { label: 'Code', key: 'product.productCode' },
    { label: 'On hand', key: 'currentQuantity' }, { label: 'Reserved', key: 'reservedQuantity' },
    { label: 'Available', key: 'availableQuantity' }, { label: 'Minimum', key: 'minimumStock' }
  ], paginated: true, createEndpoint: '/inventory/adjust', createFields: [
    { key: 'productId', label: 'Product / material', source: 'rawMaterial', required: true },
    { key: 'movementType', label: 'Adjustment mode', options: ['ADJUSTMENT', 'IN', 'OUT'], defaultValue: 'ADJUSTMENT' },
    { key: 'quantity', label: 'Quantity', type: 'number', required: true },
    { key: 'minimumStock', label: 'Minimum stock', type: 'number' },
    { key: 'reorderLevel', label: 'Reorder level', type: 'number' },
    { key: 'unitCost', label: 'Unit cost', type: 'number' },
    { key: 'notes', label: 'Reason', multiline: true }
  ] },
  'material-shortages': { title: 'Material shortages', endpoint: '/material-requirements/shortages', columns: [
    { label: 'Material code', key: 'materialCode' }, { label: 'Material', key: 'materialName' },
    { label: 'Required', key: 'requiredQuantity' }, { label: 'Available', key: 'availableQuantity' },
    { label: 'Shortage', key: 'shortageQuantity' }, { label: 'Unit', key: 'unit' }
  ] },
  suppliers: { title: 'Suppliers', endpoint: '/suppliers', columns: [
    { label: 'Code', key: 'supplierCode' }, { label: 'Supplier', key: 'supplierName' },
    { label: 'Email', key: 'email' }, { label: 'Phone', key: 'phone' }, { label: 'Tax identifier', key: 'taxIdentifier' }, { label: 'Status', key: 'status' }
  ], paginated: true, softDelete: true, createFields: [
    { key: 'supplierCode', label: 'Supplier code', required: true }, { key: 'supplierName', label: 'Supplier name', required: true },
    { key: 'email', label: 'Email', type: 'email', required: true }, { key: 'phone', label: 'Phone', required: true },
    { key: 'address', label: 'Address', required: true }, { key: 'taxIdentifier', label: 'Tax identifier' },
    { key: 'status', label: 'Status', options: ['ACTIVE', 'INACTIVE'], defaultValue: 'ACTIVE' }
  ], editFields: [
    { key: 'supplierCode', label: 'Supplier code', required: true }, { key: 'supplierName', label: 'Supplier name', required: true },
    { key: 'email', label: 'Email', type: 'email', required: true }, { key: 'phone', label: 'Phone', required: true },
    { key: 'address', label: 'Address', required: true }, { key: 'taxIdentifier', label: 'Tax identifier' },
    { key: 'status', label: 'Status', options: ['ACTIVE', 'INACTIVE'], defaultValue: 'ACTIVE' }
  ] },
  'purchase-orders': { title: 'Purchase orders', endpoint: '/purchase-orders', columns: [
    { label: 'PO number', key: 'poNumber' }, { label: 'Supplier', key: 'supplier.supplierName' },
    { label: 'PO date', key: 'poDate' }, { label: 'Expected delivery', key: 'expectedDeliveryDate' },
    { label: 'Status', key: 'status' }, { label: 'Total', key: 'totalAmount' }
  ], paginated: true, itemsMode: 'purchaseOrder', createFields: [
    { key: 'supplierId', label: 'Supplier', source: 'supplier', required: true },
    { key: 'poDate', label: 'PO date', type: 'date', required: true, defaultValue: new Date().toISOString().slice(0, 10) },
    { key: 'expectedDeliveryDate', label: 'Expected delivery', type: 'date', required: true },
    { key: 'notes', label: 'Notes', multiline: true }
  ] },
  'goods-receipts': { title: 'Goods receipts', endpoint: '/goods-receipts', columns: [
    { label: 'Receipt', key: 'receiptNumber' }, { label: 'Purchase order', key: 'purchaseOrder.poNumber' },
    { label: 'Date', key: 'receiptDate' }, { label: 'Received by', key: 'receivedBy' }, { label: 'Items', key: 'items.length' }
  ], paginated: true, itemsMode: 'goodsReceipt', createFields: [
    { key: 'poId', label: 'Purchase order', source: 'purchaseOrder', required: true },
    { key: 'receiptDate', label: 'Receipt date', type: 'date', required: true, defaultValue: new Date().toISOString().slice(0, 10) },
    { key: 'receivedBy', label: 'Received by' },
    { key: 'notes', label: 'Notes', multiline: true }
  ] },
  'production-orders': { title: 'Production orders', endpoint: '/production-orders', columns: [
    { label: 'Production order', key: 'productionOrderNumber' }, { label: 'Product', key: 'product.productName' },
    { label: 'Planned', key: 'plannedQuantity' }, { label: 'Produced', key: 'producedQuantity' },
    { label: 'Start date', key: 'startDate' }, { label: 'Status', key: 'status' }
  ], paginated: true, createFields: [
    { key: 'customerOrderId', label: 'Ready customer order', source: 'customerOrder', required: true },
    { key: 'productId', label: 'Finished product', source: 'finishedProduct', required: true },
    { key: 'plannedQuantity', label: 'Planned quantity', type: 'number', required: true },
    { key: 'startDate', label: 'Planned start date', type: 'date', defaultValue: new Date().toISOString().slice(0, 10) },
    { key: 'endDate', label: 'Planned end date', type: 'date' },
    { key: 'notes', label: 'Notes', multiline: true }
  ] },
  invoices: { title: 'Invoices', endpoint: '/invoices', columns: [
    { label: 'Invoice', key: 'invoiceNumber' }, { label: 'Customer', key: 'customer.name' },
    { label: 'Order', key: 'order.orderNumber' }, { label: 'Due date', key: 'dueDate' },
    { label: 'Balance', key: 'outstandingAmount' }, { label: 'Status', key: 'status' }
  ], paginated: true, createFields: [
    { key: 'orderId', label: 'Customer order', source: 'customerOrder', required: true },
    { key: 'invoiceDate', label: 'Invoice date', type: 'date', required: true, defaultValue: new Date().toISOString().slice(0, 10) },
    { key: 'dueDate', label: 'Due date', type: 'date', required: true },
    { key: 'discount', label: 'Discount', type: 'number', defaultValue: '0' }
  ] },
  payments: { title: 'Payments', endpoint: '/payments', columns: [
    { label: 'Payment', key: 'paymentNumber' }, { label: 'Invoice', key: 'invoice.invoiceNumber' },
    { label: 'Date', key: 'paymentDate' }, { label: 'Method', key: 'paymentMethod' }, { label: 'Amount', key: 'amount' }, { label: 'Status', key: 'status' }
  ], paginated: true, createFields: [
    { key: 'invoiceId', label: 'Unpaid invoice', source: 'invoice', required: true },
    { key: 'paymentDate', label: 'Payment date', type: 'date', required: true, defaultValue: new Date().toISOString().slice(0, 10) },
    { key: 'amount', label: 'Amount', type: 'number', required: true },
    { key: 'paymentMethod', label: 'Payment method', options: ['CASH', 'BANK_TRANSFER', 'CARD', 'UPI', 'OTHER'], required: true },
    { key: 'transactionReference', label: 'Transaction reference' }, { key: 'notes', label: 'Notes', multiline: true }
  ] },
  reports: { title: 'Order reports', endpoint: '/reports/orders', columns: [
    { label: 'Order number', key: 'orderNumber' }, { label: 'Customer', key: 'customer.name' },
    { label: 'Order date', key: 'orderDate' }, { label: 'Status', key: 'status' }, { label: 'Total', key: 'grandTotal' }
  ], paginated: true },
  'audit-logs': { title: 'Audit log', endpoint: '/audit-logs', columns: [
    { label: 'Time', key: 'timestamp' }, { label: 'User', key: 'username' }, { label: 'Action', key: 'action' },
    { label: 'Module', key: 'module' }, { label: 'Entity', key: 'entityName' }, { label: 'Entity ID', key: 'entityId' }
  ], paginated: true }
};

@Component({
  selector: 'app-resource-page',
  imports: [FormsModule],
  template: `
    @if (definition(); as page) {
      <section class="resource-page">
        <header class="page-heading">
          <div><p class="eyebrow">ORDERCRAFT DATA</p><h1>{{ page.title }}</h1><p class="subheading">Live records from the operations database.</p></div>
        </header>
        <section class="data-panel">
          <form class="toolbar" (ngSubmit)="search()">
            <label class="search-field"><span>Search records</span><input name="query" [(ngModel)]="query" placeholder="Search by name, code, or number"></label>
            <button class="search-button" type="submit" [disabled]="loading()">Search</button>
            <span class="result-count">{{ totalElements() }} records</span>
            @if (page.createFields) { <button class="new-button" type="button" (click)="startCreate()">New record</button> }
          </form>
          @if (formOpen()) {
            <form class="record-form" (ngSubmit)="save(page)">
              <div class="record-form-heading"><h2>{{ editingId() ? 'Edit record' : 'Create record' }}</h2><button type="button" class="close-button" aria-label="Close form" (click)="formOpen.set(false)">×</button></div>
              <div class="form-grid">
                @for (field of (editingId() ? page.editFields : page.createFields); track field.key) {
                  <label [class.form-wide]="field.multiline">
                    <span>{{ field.label }}</span>
                    @if (field.options || field.source) {
                      <select [name]="field.key" [ngModel]="formValues[field.key]" (ngModelChange)="setFormField(field, $event)" [required]="!!field.required">
                        @if (field.options) { @for (option of field.options; track option) { <option [value]="option">{{ option.replaceAll('_', ' ') }}</option> } }
                        @if (field.source) { @for (option of relationOptions(field.source, field.key); track option.value) { <option [value]="option.value">{{ option.label }}</option> } }
                      </select>
                    } @else if (field.multiline) {
                      <textarea [name]="field.key" [ngModel]="formValues[field.key]" (ngModelChange)="formValues[field.key] = $event" [required]="!!field.required" rows="3"></textarea>
                    } @else {
                      <input [name]="field.key" [type]="field.type || 'text'" [ngModel]="formValues[field.key]" (ngModelChange)="formValues[field.key] = $event" [required]="!!field.required" [min]="field.type === 'number' ? 0 : null" [step]="field.type === 'number' ? '0.01' : null" [autocomplete]="field.key === 'password' ? 'new-password' : 'off'">
                    }
                  </label>
                }
              </div>
              @if (page.itemsMode) {
                <div class="line-editor">
                  <div class="line-heading"><h3>{{ page.itemsMode === 'order' ? 'Order items' : page.itemsMode === 'bom' ? 'Material requirements' : page.itemsMode === 'goodsReceipt' ? 'Received items' : 'Purchase items' }}</h3><button type="button" (click)="addLine(page.itemsMode!)">Add line</button></div>
                  @for (line of lineItems(); track $index; let index = $index) {
                    <div class="line-row">
                      <label><span>{{ page.itemsMode === 'order' ? 'Finished product' : 'Material' }}</span>
                        <select [name]="'line-' + index + '-item'" [ngModel]="line[page.itemsMode === 'order' ? 'productId' : page.itemsMode === 'goodsReceipt' ? 'poItemId' : 'materialId']" (ngModelChange)="updateLine(index, page.itemsMode!, page.itemsMode === 'order' ? 'productId' : page.itemsMode === 'goodsReceipt' ? 'poItemId' : 'materialId', $event)" required>
                          <option value="">Choose item</option>
                          @for (option of itemOptions(page.itemsMode!); track option.id) { <option [value]="option.id">{{ option.productCode }} · {{ option.productName }}</option> }
                        </select>
                      </label>
                      <label><span>{{ page.itemsMode === 'goodsReceipt' ? 'Received quantity' : 'Quantity' }}</span><input [name]="'line-' + index + '-quantity'" type="number" min="0.01" step="0.01" [ngModel]="line[page.itemsMode === 'goodsReceipt' ? 'receivedQuantity' : 'quantity']" (ngModelChange)="updateLine(index, page.itemsMode!, page.itemsMode === 'goodsReceipt' ? 'receivedQuantity' : 'quantity', $event)" required></label>
                      @if (page.itemsMode === 'goodsReceipt') {
                        <span class="line-unit">{{ itemLabel(line['poItemId']) }}</span>
                      } @else {
                      @if (page.itemsMode === 'order' || page.itemsMode === 'purchaseOrder') {
                        <label><span>{{ page.itemsMode === 'order' ? 'Unit price' : 'Unit cost' }}</span><input [name]="'line-' + index + '-price'" type="number" min="0" step="0.01" [ngModel]="line['unitPrice']" (ngModelChange)="updateLine(index, page.itemsMode!, 'unitPrice', $event)" required></label>
                        @if (page.itemsMode === 'order') { <label><span>Discount</span><input [name]="'line-' + index + '-discount'" type="number" min="0" step="0.01" [ngModel]="line['discount']" (ngModelChange)="updateLine(index, page.itemsMode!, 'discount', $event)"></label> }
                        <label><span>Tax</span><input [name]="'line-' + index + '-tax'" type="number" min="0" step="0.01" [ngModel]="line['tax']" (ngModelChange)="updateLine(index, page.itemsMode!, 'tax', $event)"></label>
                      } @else {
                        <label><span>Unit</span><input [name]="'line-' + index + '-unit'" [ngModel]="line['unit']" (ngModelChange)="updateLine(index, page.itemsMode!, 'unit', $event)" required></label>
                        <label><span>Wastage %</span><input [name]="'line-' + index + '-wastage'" type="number" min="0" max="100" step="0.01" [ngModel]="line['wastagePercentage']" (ngModelChange)="updateLine(index, page.itemsMode!, 'wastagePercentage', $event)"></label>
                      }
                      }
                      <button class="remove-line" type="button" aria-label="Remove line" (click)="removeLine(index)">×</button>
                    </div>
                  }
                </div>
              }
              @if (formError()) { <p class="form-error" role="alert">{{ formError() }}</p> }
              <div class="form-actions"><button type="button" class="cancel-button" (click)="formOpen.set(false)">Cancel</button><button class="new-button" type="submit" [disabled]="loading()">{{ loading() ? 'Saving…' : (editingId() ? 'Save changes' : 'Create') }}</button></div>
            </form>
          }
          @if (loading()) {
            <div class="state-message"><span class="spinner"></span>Loading records…</div>
          } @else if (error()) {
            <div class="state-message error" role="alert"><strong>Could not load records</strong><span>{{ error() }}</span><button type="button" (click)="load()">Retry</button></div>
          } @else if (rows().length) {
            <div class="table-wrap"><table><thead><tr>@for (column of page.columns; track column.key) { <th>{{ column.label }}</th> } @if (hasRowActions(page)) { <th>Actions</th> }</tr></thead>
              <tbody>@for (row of rows(); track row['id'] || $index) { <tr>@for (column of page.columns; track column.key) { <td [class.status-cell]="column.key === 'status'">{{ value(row, column.key) }}</td> }
                @if (hasRowActions(page, row)) {
                  <td class="actions-cell">
                    @if (page.editFields) { <button type="button" (click)="startEdit(page, row)">Edit</button> }
                    @if (page.softDelete && row['status'] !== 'INACTIVE') { <button type="button" class="danger-action" (click)="deactivate(page, row)">Deactivate</button> }
                    @if (page.endpoint === '/admin/users') { <button type="button" [class.danger-action]="row['active']" (click)="setUserActive(row)">{{ row['active'] ? 'Deactivate' : 'Activate' }}</button> }
                    @if (page.endpoint === '/orders' && (row['status'] === 'CONFIRMED' || row['status'] === 'MATERIAL_SHORTAGE' || row['status'] === 'MATERIAL_CHECK')) { <button type="button" (click)="checkMaterials(row)">Check materials</button> }
                    @if (page.endpoint === '/orders' && row['status'] === 'DRAFT') { <button type="button" (click)="updateOrderStatus(row, 'CONFIRMED')">Confirm</button> }
                    @if (page.endpoint === '/purchase-orders' && row['status'] === 'DRAFT') { <button type="button" (click)="sendPurchaseOrder(row)">Send PO</button> }
                    @if (page.endpoint === '/production-orders' && row['status'] !== 'COMPLETED' && row['status'] !== 'CANCELLED') { <button type="button" (click)="progressProduction(row)">Update progress</button> }
                  </td>
                }
              </tr> }</tbody>
            </table></div>
            @if (page.paginated) {
              <footer class="pagination"><span>Page {{ pageNumber() + 1 }} of {{ totalPages() }}</span><div><button type="button" (click)="changePage(-1)" [disabled]="pageNumber() <= 0 || loading()">Previous</button><button type="button" (click)="changePage(1)" [disabled]="pageNumber() + 1 >= totalPages() || loading()">Next</button></div></footer>
            }
          } @else {
            <div class="state-message"><strong>No records found</strong><span>Try changing the search term or check back after records are added.</span></div>
          }
        </section>
      </section>
    } @else {
      <section class="unknown-page"><h1>Page unavailable</h1><p>This section is not configured.</p></section>
    }
  `,
})
export class ResourcePageComponent {
  private readonly api = inject(ApiService);
  private readonly toast = inject(ToastService);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  protected readonly definition = signal<ResourceDefinition | null>(null);
  protected readonly rows = signal<Array<Record<string, unknown>>>([]);
  protected readonly pageNumber = signal(0);
  protected readonly totalPages = signal(1);
  protected readonly totalElements = signal(0);
  protected readonly loading = signal(false);
  protected readonly error = signal('');
  protected readonly formOpen = signal(false);
  protected readonly editingId = signal<number | null>(null);
  protected readonly formError = signal('');
  protected readonly lineItems = signal<Array<Record<string, string>>>([]);
  protected readonly customers = signal<SelectOption[]>([]);
  protected readonly suppliers = signal<SelectOption[]>([]);
  protected readonly finishedProducts = signal<ProductChoice[]>([]);
  protected readonly rawMaterials = signal<ProductChoice[]>([]);
  protected readonly customerOrders = signal<CustomerOrderChoice[]>([]);
  protected readonly purchaseOrders = signal<PurchaseOrderChoice[]>([]);
  protected readonly invoices = signal<InvoiceChoice[]>([]);
  protected readonly receiptItems = signal<ReceiptItemChoice[]>([]);
  protected formValues: Record<string, string> = {};
  protected query = '';

  constructor() {
    this.route.paramMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(params => {
      const definition = resources[params.get('module') ?? ''];
      this.definition.set(definition ?? null);
      this.pageNumber.set(0);
      this.query = '';
      this.formOpen.set(false);
      if (definition) this.load();
    });
  }

  protected search(): void {
    this.pageNumber.set(0);
    this.load();
  }

  protected changePage(delta: number): void {
    this.pageNumber.update(value => value + delta);
    this.load();
  }

  protected hasRowActions(definition: ResourceDefinition, row?: Record<string, unknown>): boolean {
    if (definition.editFields || definition.softDelete || definition.endpoint === '/admin/users') return true;
    if (!row) return ['/orders', '/purchase-orders', '/production-orders'].includes(definition.endpoint);
    return definition.endpoint === '/orders' && ['DRAFT', 'CONFIRMED', 'MATERIAL_SHORTAGE', 'MATERIAL_CHECK'].includes(String(row['status']))
      || definition.endpoint === '/purchase-orders' && row['status'] === 'DRAFT'
      || definition.endpoint === '/production-orders' && !['COMPLETED', 'CANCELLED'].includes(String(row['status']));
  }

  protected checkMaterials(row: Record<string, unknown>): void {
    this.api.get<{ summaryMessage: string }>(`/material-requirements/order/${row['id']}`).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: response => { this.toast.info(response.summaryMessage, 'Material check'); this.load(); },
      error: failure => this.toast.error(failure.error?.message || 'Material requirements could not be checked.')
    });
  }

  protected updateOrderStatus(row: Record<string, unknown>, status: string): void {
    this.api.patch(`/orders/${row['id']}/status`, { status }, {}).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => { this.toast.success('Order confirmed.'); this.load(); },
      error: failure => this.toast.error(failure.error?.message || 'The order status could not be changed.')
    });
  }

  protected sendPurchaseOrder(row: Record<string, unknown>): void {
    this.api.patch(`/purchase-orders/${row['id']}/status`, { status: 'SENT' }, {}).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => { this.toast.success('Purchase order marked as sent.'); this.load(); },
      error: failure => this.toast.error(failure.error?.message || 'The purchase order could not be sent.')
    });
  }

  protected progressProduction(row: Record<string, unknown>): void {
    const nextStatus: Record<string, string> = {
      PLANNED: 'MATERIAL_READY',
      MATERIAL_READY: 'IN_PROGRESS',
      IN_PROGRESS: 'QUALITY_CHECK',
      QUALITY_CHECK: 'COMPLETED'
    };
    const current = String(row['status']);
    const next = nextStatus[current];
    if (!next) return;
    const entered = window.prompt(`Produced quantity (maximum ${row['plannedQuantity']})`, String(row['producedQuantity'] ?? 0));
    if (entered === null) return;
    const producedQuantity = Number(entered);
    if (!Number.isFinite(producedQuantity) || producedQuantity < 0 || producedQuantity > Number(row['plannedQuantity'])) {
      this.toast.error('Enter a valid produced quantity within the planned limit.');
      return;
    }
    this.api.patch(`/production-orders/${row['id']}/progress`, { status: next, producedQuantity }, {}).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => { this.toast.success(`Production moved to ${next.replaceAll('_', ' ')}.`); this.load(); },
      error: failure => this.toast.error(failure.error?.message || 'Production progress could not be updated.')
    });
  }

  protected startCreate(): void {
    const fields = this.definition()?.createFields ?? [];
    this.editingId.set(null);
    this.formValues = Object.fromEntries(fields.map(field => [field.key, field.defaultValue ?? '']));
    this.formError.set('');
    this.lineItems.set([]);
    this.formOpen.set(true);
    this.loadRelations();
  }

  protected startEdit(definition: ResourceDefinition, row: Record<string, unknown>): void {
    const fields = definition.editFields ?? [];
    this.editingId.set(Number(row['id']));
    this.formValues = Object.fromEntries(fields.map(field => [field.key, String(row[field.key] ?? field.defaultValue ?? '')]));
    this.formError.set('');
    this.lineItems.set([]);
    this.formOpen.set(true);
  }

  protected relationOptions(source: NonNullable<FieldDefinition['source']>, fieldKey = ''): SelectOption[] {
    if (source === 'customer') return this.customers();
    if (source === 'supplier') return this.suppliers();
    if (source === 'finishedProduct') return this.finishedProducts().map(product => ({ value: product.id, label: `${product.productCode} · ${product.productName}` }));
    if (source === 'rawMaterial') return this.rawMaterials().map(product => ({ value: product.id, label: `${product.productCode} · ${product.productName}` }));
    if (source === 'customerOrder') {
      const eligible = fieldKey === 'customerOrderId'
        ? this.customerOrders().filter(order => order.status === 'READY_FOR_PRODUCTION')
        : this.customerOrders().filter(order => order.status === 'QUALITY_CHECK' || order.status === 'COMPLETED');
      return eligible.map(order => ({ value: order.id, label: `${order.orderNumber} · ${order.status.replaceAll('_', ' ')}` }));
    }
    if (source === 'purchaseOrder') return this.purchaseOrders()
      .filter(order => order.status === 'SENT' || order.status === 'PARTIALLY_RECEIVED')
      .map(order => ({ value: order.id, label: `${order.poNumber} · ${order.status.replaceAll('_', ' ')}` }));
    return this.invoices().filter(invoice => !['PAID', 'CANCELLED'].includes(invoice.status) && invoice.paidAmount < invoice.grandTotal)
      .map(invoice => ({ value: invoice.id, label: `${invoice.invoiceNumber} · balance ${this.money(invoice.grandTotal - invoice.paidAmount)}` }));
  }

  protected itemOptions(mode: NonNullable<ResourceDefinition['itemsMode']>): ProductChoice[] {
    if (mode === 'order') return this.finishedProducts();
    if (mode === 'goodsReceipt') return this.receiptItems();
    return this.rawMaterials();
  }

  protected addLine(mode: NonNullable<ResourceDefinition['itemsMode']>): void {
    const line: Record<string, string> = mode === 'bom'
      ? { materialId: '', quantity: '', unit: 'PCS', wastagePercentage: '0' }
      : mode === 'order'
        ? { productId: '', quantity: '', unitPrice: '', discount: '0', tax: '0' }
        : mode === 'goodsReceipt'
          ? { poItemId: '', receivedQuantity: '' }
        : { materialId: '', quantity: '', unitPrice: '', tax: '0' };
    this.lineItems.update(lines => [...lines, line]);
  }

  protected updateLine(index: number, mode: NonNullable<ResourceDefinition['itemsMode']>, field: string, value: string): void {
    this.lineItems.update(lines => lines.map((line, lineIndex) => {
      if (lineIndex !== index) return line;
      const updated: Record<string, string> = { ...line, [field]: value };
      if (field === 'productId' || field === 'materialId') {
        const product = this.itemOptions(mode).find(option => option.id === Number(value));
        if (product && mode === 'order') updated['unitPrice'] = String(product.sellingPrice);
        if (product && mode === 'purchaseOrder') updated['unitPrice'] = String(product.costPrice);
        if (product && mode === 'bom') updated['unit'] = product.unit;
      }
      return updated;
    }));
  }

  protected removeLine(index: number): void {
    this.lineItems.update(lines => lines.filter((_line, lineIndex) => lineIndex !== index));
  }

  protected itemLabel(itemId: string | undefined): string {
    return this.receiptItems().find(item => item.id === Number(itemId))?.productName ?? '';
  }

  private loadRelations(): void {
    const definition = this.definition();
    const sources = new Set((definition?.createFields ?? []).map(field => field.source).filter(Boolean));
    const needsFinishedProducts = sources.has('finishedProduct') || definition?.itemsMode === 'order';
    const needsRawMaterials = sources.has('rawMaterial') || definition?.itemsMode === 'bom' || definition?.itemsMode === 'purchaseOrder';
    if (sources.has('customer')) this.api.get<Array<{ id: number; name: string; customerCode: string }>>('/customers/active').pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: values => this.customers.set(values.map(value => ({ value: value.id, label: `${value.customerCode} · ${value.name}` }))),
      error: () => this.customers.set([])
    });
    if (sources.has('supplier')) this.api.get<Array<{ id: number; supplierName: string; supplierCode: string }>>('/suppliers/active').pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: values => this.suppliers.set(values.map(value => ({ value: value.id, label: `${value.supplierCode} · ${value.supplierName}` }))),
      error: () => this.suppliers.set([])
    });
    if (needsFinishedProducts) this.api.get<ProductChoice[]>('/products/by-type/FINISHED_GOOD').pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: values => this.finishedProducts.set(values), error: () => this.finishedProducts.set([])
    });
    if (needsRawMaterials) this.api.get<ProductChoice[]>('/products/by-type/RAW_MATERIAL').pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: values => this.rawMaterials.set(values), error: () => this.rawMaterials.set([])
    });
    if (sources.has('customerOrder')) this.api.get<PageData<CustomerOrderChoice>>('/orders', { page: 0, size: 100 }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: page => this.customerOrders.set(page.content), error: () => this.customerOrders.set([])
    });
    if (sources.has('purchaseOrder')) this.api.get<PageData<PurchaseOrderChoice>>('/purchase-orders', { page: 0, size: 100 }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: page => this.purchaseOrders.set(page.content), error: () => this.purchaseOrders.set([])
    });
    if (sources.has('invoice')) this.api.get<PageData<InvoiceChoice>>('/invoices', { page: 0, size: 100 }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: page => this.invoices.set(page.content), error: () => this.invoices.set([])
    });
  }

  protected setFormField(field: FieldDefinition, value: string): void {
    this.formValues[field.key] = value;
    if (field.source === 'purchaseOrder' && value) {
      const order = this.purchaseOrders().find(item => item.id === Number(value));
      this.receiptItems.set((order?.items ?? []).filter(item => item.receivedQuantity < item.quantity).map(item => ({
        id: item.id,
        productCode: item.material.productCode,
        productName: `${item.material.productName} · ${item.quantity - item.receivedQuantity} remaining`,
        costPrice: item.material.costPrice,
        sellingPrice: item.material.sellingPrice,
        unit: item.material.unit
      })));
      this.lineItems.set([]);
    }
  }

  private money(value: number): string {
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value);
  }

  protected save(definition: ResourceDefinition): void {
    const fields = this.editingId() ? definition.editFields ?? [] : definition.createFields ?? [];
    const payload: Record<string, unknown> = Object.fromEntries(fields.map(field => {
      const value = this.formValues[field.key] ?? '';
      if (field.source) return [field.key, Number(value)];
      return [field.key, field.type === 'number' ? Number(value || 0) : field.multiline || field.type === 'date' ? value : value.trim()];
    }));
    if (definition.itemsMode) {
      if (!this.lineItems().length) {
        this.formError.set('Add at least one line item.');
        return;
      }
      payload['items'] = this.lineItems().map(line => Object.fromEntries(Object.entries(line).map(([key, value]) => [
        key,
        ['quantity', 'receivedQuantity', 'unitPrice', 'discount', 'tax', 'wastagePercentage'].includes(key) ? Number(value || 0) : ['materialId', 'productId', 'poItemId'].includes(key) ? Number(value) : value
      ])));
    }
    this.loading.set(true);
    this.formError.set('');
    const request = this.editingId()
      ? this.api.put(`${definition.endpoint}/${this.editingId()}`, payload)
      : this.api.post(definition.createEndpoint ?? definition.endpoint, payload);
    request.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => {
        this.toast.success(this.editingId() ? 'Record updated.' : 'Record created.');
        this.formOpen.set(false);
        this.load();
      },
      error: failure => {
        this.loading.set(false);
        this.formError.set(failure.error?.message || 'The record could not be saved.');
      }
    });
  }

  protected deactivate(definition: ResourceDefinition, row: Record<string, unknown>): void {
    if (!window.confirm(`Deactivate ${String(row['name'] ?? row['productName'] ?? row['supplierName'] ?? 'this record')}?`)) return;
    this.api.delete(`${definition.endpoint}/${row['id']}`).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => { this.toast.success('Record deactivated.'); this.load(); },
      error: failure => this.toast.error(failure.error?.message || 'The record could not be deactivated.')
    });
  }

  protected setUserActive(row: Record<string, unknown>): void {
    const active = !Boolean(row['active']);
    if (!window.confirm(`${active ? 'Activate' : 'Deactivate'} ${String(row['username'])}?`)) return;
    this.api.patch(`${this.definition()?.endpoint}/${row['id']}/active`, {}, { active: String(active) })
      .pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
        next: () => { this.toast.success(`User ${active ? 'activated' : 'deactivated'}.`); this.load(); },
        error: failure => this.toast.error(failure.error?.message || 'The user status could not be changed.')
      });
  }

  protected load(): void {
    const definition = this.definition();
    if (!definition) return;
    this.loading.set(true);
    this.error.set('');
    const params: Record<string, string | number | boolean> = {};
    if (this.query.trim()) params['query'] = this.query.trim();
    if (definition.paginated) {
      params['page'] = this.pageNumber();
      params['size'] = 10;
    }
    this.api.get<unknown>(definition.endpoint, params).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: response => {
        if (Array.isArray(response)) {
          this.rows.set(response as Array<Record<string, unknown>>);
          this.totalElements.set(response.length);
          this.totalPages.set(1);
        } else if (response && typeof response === 'object') {
          const page = response as { content?: unknown[]; totalElements?: number; totalPages?: number };
          this.rows.set((page.content ?? []) as Array<Record<string, unknown>>);
          this.totalElements.set(page.totalElements ?? this.rows().length);
          this.totalPages.set(Math.max(page.totalPages ?? 1, 1));
        } else {
          this.rows.set([]);
          this.totalElements.set(0);
          this.totalPages.set(1);
        }
      },
      error: failure => {
        this.error.set(failure.error?.message || 'The backend did not return these records.');
        this.loading.set(false);
      },
      complete: () => this.loading.set(false)
    });
  }

  protected value(row: Record<string, unknown>, path: string): string {
    const result = path.split('.').reduce<unknown>((current, part) => {
      if (part === 'length' && Array.isArray(current)) return current.length;
      if (current && typeof current === 'object') return (current as Record<string, unknown>)[part];
      return undefined;
    }, row);
    if (result === null || result === undefined || result === '') return '—';
    if (typeof result === 'number' && ['grandTotal', 'totalAmount', 'amount', 'outstandingAmount'].some(key => path.endsWith(key))) {
      return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(result);
    }
    if (typeof result === 'string' && result.includes('_')) return result.replaceAll('_', ' ');
    return String(result);
  }
}
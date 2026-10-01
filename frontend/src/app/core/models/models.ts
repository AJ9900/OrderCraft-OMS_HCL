export type Role =
  | 'ADMIN'
  | 'SALES_MANAGER'
  | 'PRODUCTION_MANAGER'
  | 'PROCUREMENT_MANAGER'
  | 'WAREHOUSE_MANAGER'
  | 'FINANCE_MANAGER';

export interface User {
  id: number;
  username: string;
  email: string;
  fullName: string;
  role: Role;
  active: boolean;
}

export interface AuthResponse {
  token: string;
  type: string;
  id: number;
  username: string;
  email: string;
  fullName: string;
  role: Role;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface Customer {
  id: number;
  customerCode: string;
  name: string;
  email: string;
  phone: string;
  address: string;
  city: string;
  state: string;
  country: string;
  postalCode: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface Product {
  id: number;
  productCode: string;
  productName: string;
  description?: string;
  category: string;
  type: 'FINISHED_GOOD' | 'RAW_MATERIAL';
  sellingPrice: number;
  costPrice: number;
  unit: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface BomItem {
  id?: number;
  material: Product;
  quantity: number;
  unit: string;
  wastagePercentage: number;
}

export interface Bom {
  id: number;
  bomCode: string;
  product: Product;
  version: string;
  status: 'ACTIVE' | 'DRAFT' | 'ARCHIVED';
  effectiveDate: string;
  notes?: string;
  items: BomItem[];
  createdAt: string;
  updatedAt: string;
}

export interface Inventory {
  id: number;
  product: Product;
  currentQuantity: number;
  reservedQuantity: number;
  availableQuantity: number;
  minimumStock: number;
  reorderLevel: number;
  unitCost: number;
  updatedAt: string;
}

export interface StockMovement {
  id: number;
  product: Product;
  movementType: 'IN' | 'OUT' | 'RESERVED' | 'RELEASED' | 'ADJUSTMENT';
  quantity: number;
  referenceType?: string;
  referenceId?: string;
  notes?: string;
  createdBy?: string;
  createdAt: string;
}

export type OrderStatus =
  | 'DRAFT'
  | 'CONFIRMED'
  | 'MATERIAL_CHECK'
  | 'MATERIAL_SHORTAGE'
  | 'READY_FOR_PRODUCTION'
  | 'IN_PRODUCTION'
  | 'QUALITY_CHECK'
  | 'COMPLETED'
  | 'CANCELLED';

export interface OrderItem {
  id?: number;
  product: Product;
  quantity: number;
  unitPrice: number;
  discount: number;
  tax: number;
  total: number;
}

export interface CustomerOrder {
  id: number;
  orderNumber: string;
  customer: Customer;
  orderDate: string;
  expectedDeliveryDate: string;
  status: OrderStatus;
  totalAmount: number;
  taxAmount: number;
  grandTotal: number;
  notes?: string;
  items: OrderItem[];
  createdAt: string;
  updatedAt: string;
}

export interface MaterialRequirement {
  materialId: number;
  materialCode: string;
  materialName: string;
  unit: string;
  unitCost: number;
  requiredQuantity: number;
  availableQuantity: number;
  currentQuantity: number;
  reservedQuantity: number;
  shortageQuantity: number;
  hasShortage: boolean;
}

export interface OrderRequirementResponse {
  orderId: number;
  orderNumber: string;
  customerName: string;
  orderStatus: OrderStatus;
  requirements: MaterialRequirement[];
  hasAnyShortage: boolean;
  summaryMessage: string;
}

export interface Supplier {
  id: number;
  supplierCode: string;
  supplierName: string;
  email: string;
  phone: string;
  address: string;
  taxIdentifier?: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export type PoStatus = 'DRAFT' | 'SENT' | 'PARTIALLY_RECEIVED' | 'RECEIVED' | 'CANCELLED';

export interface PoItem {
  id?: number;
  material: Product;
  quantity: number;
  receivedQuantity: number;
  unitPrice: number;
  tax: number;
  total: number;
}

export interface PurchaseOrder {
  id: number;
  poNumber: string;
  supplier: Supplier;
  poDate: string;
  expectedDeliveryDate: string;
  status: PoStatus;
  totalAmount: number;
  notes?: string;
  items: PoItem[];
  createdAt: string;
  updatedAt: string;
}

export interface GoodsReceiptItem {
  id?: number;
  poItem: PoItem;
  receivedQuantity: number;
}

export interface GoodsReceipt {
  id: number;
  receiptNumber: string;
  purchaseOrder: PurchaseOrder;
  receiptDate: string;
  receivedBy: string;
  notes?: string;
  items: GoodsReceiptItem[];
  createdAt: string;
}

export type ProductionStatus =
  | 'PLANNED'
  | 'MATERIAL_READY'
  | 'IN_PROGRESS'
  | 'QUALITY_CHECK'
  | 'COMPLETED'
  | 'CANCELLED';

export interface ProductionOrder {
  id: number;
  productionOrderNumber: string;
  customerOrder?: CustomerOrder;
  product: Product;
  plannedQuantity: number;
  producedQuantity: number;
  startDate?: string;
  endDate?: string;
  status: ProductionStatus;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export type InvoiceStatus = 'DRAFT' | 'ISSUED' | 'PARTIALLY_PAID' | 'PAID' | 'OVERDUE' | 'CANCELLED';

export interface Invoice {
  id: number;
  invoiceNumber: string;
  customer: Customer;
  order: CustomerOrder;
  invoiceDate: string;
  dueDate: string;
  subtotal: number;
  tax: number;
  discount: number;
  grandTotal: number;
  paidAmount: number;
  status: InvoiceStatus;
  createdAt: string;
  updatedAt: string;
}

export type PaymentMethod = 'CASH' | 'BANK_TRANSFER' | 'CARD' | 'UPI' | 'OTHER';

export interface Payment {
  id: number;
  paymentNumber: string;
  invoice: Invoice;
  paymentDate: string;
  amount: number;
  paymentMethod: PaymentMethod;
  transactionReference?: string;
  status: string;
  notes?: string;
  createdAt: string;
}

export interface AuditLog {
  id: number;
  username: string;
  action: string;
  module: string;
  entityName: string;
  entityId: string;
  oldValue?: string;
  newValue?: string;
  timestamp: string;
}

export interface DashboardStats {
  totalOrders: number;
  pendingOrders: number;
  ordersInProduction: number;
  completedOrders: number;
  lowStockCount: number;
  pendingPurchaseOrders: number;
  pendingInvoices: number;
  outstandingPaymentsAmount: number;
  totalRevenue: number;
  recentOrders: CustomerOrder[];
  lowStockItems: Inventory[];
}

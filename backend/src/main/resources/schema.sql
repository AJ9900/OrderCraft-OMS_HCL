-- ORDERCRAFT Manufacturing OMS - MySQL Database Schema
CREATE DATABASE IF NOT EXISTS ordercarft;
USE ordercarft;

-- 1. Users & RBAC
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_username (username),
    INDEX idx_user_role (role)
);

-- 2. Customers
CREATE TABLE IF NOT EXISTS customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    address VARCHAR(255) NOT NULL,
    city VARCHAR(50) NOT NULL,
    state VARCHAR(50) NOT NULL,
    country VARCHAR(50) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_customer_code (customer_code),
    INDEX idx_customer_name (name)
);

-- 3. Products & Materials
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_code VARCHAR(50) NOT NULL UNIQUE,
    product_name VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    type VARCHAR(30) NOT NULL DEFAULT 'FINISHED_GOOD', -- RAW_MATERIAL, FINISHED_GOOD
    selling_price DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    cost_price DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    unit VARCHAR(20) NOT NULL DEFAULT 'PCS',
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_product_code (product_code),
    INDEX idx_product_category (category),
    INDEX idx_product_type (type)
);

-- 4. Bill of Materials (BOM)
CREATE TABLE IF NOT EXISTS boms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bom_code VARCHAR(50) NOT NULL UNIQUE,
    product_id BIGINT NOT NULL,
    version VARCHAR(20) NOT NULL DEFAULT '1.0',
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, DRAFT, ARCHIVED
    effective_date DATE NOT NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_bom_product (product_id)
);

CREATE TABLE IF NOT EXISTS bom_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bom_id BIGINT NOT NULL,
    material_id BIGINT NOT NULL,
    quantity DECIMAL(12,4) NOT NULL,
    unit VARCHAR(20) NOT NULL,
    wastage_percentage DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    FOREIGN KEY (bom_id) REFERENCES boms(id) ON DELETE CASCADE,
    FOREIGN KEY (material_id) REFERENCES products(id),
    INDEX idx_bom_items_bom (bom_id),
    INDEX idx_bom_items_material (material_id)
);

-- 5. Inventory & Stock Movements
CREATE TABLE IF NOT EXISTS inventories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL UNIQUE,
    current_quantity DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    reserved_quantity DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    available_quantity DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    minimum_stock DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    reorder_level DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    unit_cost DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_inventory_product (product_id)
);

CREATE TABLE IF NOT EXISTS stock_movements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    movement_type VARCHAR(30) NOT NULL, -- IN, OUT, RESERVED, RELEASED, ADJUSTMENT
    quantity DECIMAL(12,2) NOT NULL,
    reference_type VARCHAR(50), -- CUSTOMER_ORDER, PURCHASE_ORDER, PRODUCTION, ADJUSTMENT
    reference_id VARCHAR(100),
    notes VARCHAR(255),
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id),
    INDEX idx_sm_product (product_id),
    INDEX idx_sm_type (movement_type)
);

-- 6. Customer Orders
CREATE TABLE IF NOT EXISTS customer_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    order_date DATE NOT NULL,
    expected_delivery_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    total_amount DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    tax_amount DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    grand_total DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    INDEX idx_order_number (order_number),
    INDEX idx_order_customer (customer_id),
    INDEX idx_order_status (status)
);

CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity DECIMAL(12,2) NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    discount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    tax DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    total DECIMAL(14,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES customer_orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id),
    INDEX idx_order_items_order (order_id),
    INDEX idx_order_items_product (product_id)
);

-- 7. Suppliers & Purchase Orders
CREATE TABLE IF NOT EXISTS suppliers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    supplier_code VARCHAR(50) NOT NULL UNIQUE,
    supplier_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    address VARCHAR(255) NOT NULL,
    tax_identifier VARCHAR(50),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_supplier_code (supplier_code)
);

CREATE TABLE IF NOT EXISTS purchase_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    po_number VARCHAR(50) NOT NULL UNIQUE,
    supplier_id BIGINT NOT NULL,
    po_date DATE NOT NULL,
    expected_delivery_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    total_amount DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
    INDEX idx_po_number (po_number),
    INDEX idx_po_supplier (supplier_id),
    INDEX idx_po_status (status)
);

CREATE TABLE IF NOT EXISTS po_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    po_id BIGINT NOT NULL,
    material_id BIGINT NOT NULL,
    quantity DECIMAL(12,2) NOT NULL,
    received_quantity DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    unit_price DECIMAL(12,2) NOT NULL,
    tax DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    total DECIMAL(14,2) NOT NULL,
    FOREIGN KEY (po_id) REFERENCES purchase_orders(id) ON DELETE CASCADE,
    FOREIGN KEY (material_id) REFERENCES products(id),
    INDEX idx_po_items_po (po_id)
);

-- 8. Goods Receipts
CREATE TABLE IF NOT EXISTS goods_receipts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    receipt_number VARCHAR(50) NOT NULL UNIQUE,
    po_id BIGINT NOT NULL,
    receipt_date DATE NOT NULL,
    received_by VARCHAR(50) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (po_id) REFERENCES purchase_orders(id),
    INDEX idx_gr_po (po_id)
);

CREATE TABLE IF NOT EXISTS goods_receipt_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    goods_receipt_id BIGINT NOT NULL,
    po_item_id BIGINT NOT NULL,
    received_quantity DECIMAL(12,2) NOT NULL,
    FOREIGN KEY (goods_receipt_id) REFERENCES goods_receipts(id) ON DELETE CASCADE,
    FOREIGN KEY (po_item_id) REFERENCES po_items(id)
);

-- 9. Production Orders
CREATE TABLE IF NOT EXISTS production_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    production_order_number VARCHAR(50) NOT NULL UNIQUE,
    customer_order_id BIGINT,
    product_id BIGINT NOT NULL,
    planned_quantity DECIMAL(12,2) NOT NULL,
    produced_quantity DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    start_date DATE,
    end_date DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'PLANNED',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_order_id) REFERENCES customer_orders(id) ON DELETE SET NULL,
    FOREIGN KEY (product_id) REFERENCES products(id),
    INDEX idx_prod_order_number (production_order_number),
    INDEX idx_prod_status (status)
);

-- 10. Invoices & Payments
CREATE TABLE IF NOT EXISTS invoices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    invoice_date DATE NOT NULL,
    due_date DATE NOT NULL,
    subtotal DECIMAL(14,2) NOT NULL,
    tax DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    discount DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    grand_total DECIMAL(14,2) NOT NULL,
    paid_amount DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (order_id) REFERENCES customer_orders(id),
    INDEX idx_inv_number (invoice_number),
    INDEX idx_inv_status (status)
);

CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_number VARCHAR(50) NOT NULL UNIQUE,
    invoice_id BIGINT NOT NULL,
    payment_date DATE NOT NULL,
    amount DECIMAL(14,2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL, -- CASH, BANK_TRANSFER, CARD, UPI, OTHER
    transaction_reference VARCHAR(100),
    status VARCHAR(30) NOT NULL DEFAULT 'COMPLETED',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (invoice_id) REFERENCES invoices(id),
    INDEX idx_pay_invoice (invoice_id)
);

-- 11. Audit Logs
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    action VARCHAR(100) NOT NULL,
    module VARCHAR(100) NOT NULL,
    entity_name VARCHAR(100) NOT NULL,
    entity_id VARCHAR(100),
    old_value TEXT,
    new_value TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_user (username),
    INDEX idx_audit_module (module),
    INDEX idx_audit_timestamp (timestamp)
);

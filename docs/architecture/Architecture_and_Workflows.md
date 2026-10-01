# 📐 System Architecture & Workflow Specifications

This document outlines the architectural blueprints, subsystem interaction sequences, and lifecycle state machines governing OrderCraft.

---

## 1. Manufacturing Lifecycle State Machine

```mermaid
stateDiagram-v2
    [*] --> DRAFT : Customer Places Order
    DRAFT --> CONFIRMED : Sales Confirms
    CONFIRMED --> MATERIAL_CHECK : System Verifies Inventory
    MATERIAL_CHECK --> MATERIAL_SHORTAGE : Required > Available
    MATERIAL_SHORTAGE --> READY_FOR_PRODUCTION : PO Received & Stock Added
    MATERIAL_CHECK --> READY_FOR_PRODUCTION : Sufficient Stock
    READY_FOR_PRODUCTION --> IN_PRODUCTION : Production Order Started
    IN_PRODUCTION --> QUALITY_CHECK : Production Completed
    QUALITY_CHECK --> COMPLETED : Inspection Passed
    COMPLETED --> [*] : Invoiced & Shipped
    DRAFT --> CANCELLED : Cancelled
    CONFIRMED --> CANCELLED : Cancelled
```

---

## 2. Material Requirements Planning (MRP) Flow

```mermaid
sequenceDiagram
    autonumber
    actor User as Sales / Production Manager
    participant UI as Angular Frontend
    participant Ctrl as Order / MRP Controller
    participant Svc as MaterialRequirementService
    participant BOM as BomRepository
    participant Inv as InventoryRepository
    participant DB as MySQL Database

    User->>UI: Clicks "Check Materials" on Order
    UI->>Ctrl: GET /api/material-requirements/order/{orderId}
    Ctrl->>Svc: calculateRequirements(orderId, updateStatus=true)
    Svc->>DB: Fetch Order & Items
    loop For each Finished Good item
        Svc->>BOM: Fetch Active BOM & Lines
        Svc->>Inv: Query Current & Reserved Stock
        Note over Svc: Available = Current - Reserved<br/>Required = ItemQty * BOMLineQty * (1 + Scrap%)<br/>Shortage = max(0, Required - Available)
    end
    alt Shortage Detected
        Svc->>DB: Update Order Status -> MATERIAL_SHORTAGE
    else All Materials Available
        Svc->>DB: Update Order Status -> READY_FOR_PRODUCTION
    end
    Svc-->>Ctrl: Return OrderRequirementResponse (Shortages list)
    Ctrl-->>UI: 200 OK + JSON
    UI-->>User: Displays Shortage Breakdown & PO Recommendation
```

---

## 3. Subsystem Architecture

```mermaid
graph LR
    subgraph Frontend ["Client Layer (Angular 21)"]
        Components["Standalone Components<br/>(Dashboard, Orders, BOM, Invoices)"]
        Services["Signals Services & State<br/>(AuthService, ApiService, ToastService)"]
        Interceptors["Functional HTTP Interceptor<br/>(Bearer Token Injection & 401/403 Handling)"]
    end

    subgraph Backend ["Backend API Layer (Spring Boot 3)"]
        Security["Spring Security Filter Chain<br/>(JwtAuthTokenFilter, CORS, BCrypt)"]
        Controllers["REST API Controllers<br/>(DTO Binding, Bean Validation)"]
        ServiceLayer["Transactional Services<br/>(MRP, Inventory, Order State Machine)"]
        DAOLayer["Spring Data JPA Repositories<br/>(Hibernate ORM, JPQL Queries)"]
    end

    subgraph Storage ["Persistence Layer"]
        MySQL[("MySQL 8.0 Engine<br/>InnoDB, UTF8MB4, ACID")]
    end

    Components --> Services
    Services --> Interceptors
    Interceptors -->|REST / JSON| Security
    Security --> Controllers
    Controllers --> ServiceLayer
    ServiceLayer --> DAOLayer
    DAOLayer --> MySQL
```

---

## 4. Key Business Invariants & Rules

1. **Non-Negative Inventory**: No operation can deduct stock below zero. An adjustment or consumption that violates this condition throws a transactional `BadRequestException`.
2. **Payment Bounds**: Recorded payments cannot exceed the current outstanding balance ($\text{Grand Total} - \text{Paid Amount}$). Once paid amount equals grand total, invoice status automatically updates to `PAID`.
3. **BOM Versioning**: A product may have multiple BOM versions, but only one active version per product may be used for material requirement explosion at any given time.
4. **Audit Trail**: Every high-impact event (`ORDER_CREATED`, `STATUS_CHANGED`, `INVENTORY_ADJUSTED`, `PAYMENT_RECEIVED`) writes an immutable record to the `audit_logs` table.

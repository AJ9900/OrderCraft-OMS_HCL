# 🖥️ OrderCraft: Final Project Presentation Deck & Viva Guide
**HCLTech Training & Capability Development Evaluation**

Use this guide to prepare your slides in PowerPoint, Canva, or Google Slides and to ace your viva evaluation.

---

## 1. Slide-by-Slide Deck Outline

### Slide 1: Title Slide
- **Title:** OrderCraft — Enterprise Manufacturing Order Management System
- **Subtitle:** Decoupled Full-Stack Architecture using Spring Boot 3, Angular 21 & MySQL 8
- **Presenter Name:** [Your Name]
- **Role / Track:** Java Full Stack Developer Trainee
- **Organization / Unit:** HCLTech Engineering & R&D Services
- **Date:** October 2026

### Slide 2: Industry Context & Problem Statement
- **The Problem:** Traditional manufacturing relies on disconnected Excel spreadsheets and siloed legacy ERPs.
- **Key Bottlenecks:**
  - Lack of real-time inventory visibility leading to unexpected stockouts.
  - Slow BOM requirement calculations delaying production kickoff.
  - Manual data entry errors across sales, procurement, and billing.
- **The Goal:** A unified SaaS-grade portal orchestrating the entire lifecycle from sales order to cash collection.

### Slide 3: Solution Architecture & Tech Stack
- **Backend:** Spring Boot 3.4.3 (Java 21 LTS), Spring Data JPA, Hibernate ORM, Spring Security, JJWT 0.12.6.
- **Frontend:** Angular 21 (Standalone Components, Signals reactivity, CSS3 responsive UI, Functional Interceptors).
- **Database:** MySQL 8.0 (ACID compliant, relational integrity, InnoDB, normalized schema).
- **Architecture Pattern:** N-tier Layered Architecture (Angular SPA $\rightarrow$ REST Controllers $\rightarrow$ Services $\rightarrow$ JPA Repositories $\rightarrow$ MySQL).

### Slide 4: End-to-End Business Flow (The Core Value Stream)
- Visual Flow:
  1. **Customer Order Placed** (Draft $\rightarrow$ Confirmed)
  2. **Automated BOM Explosion & MRP Check**
  3. **Shortage Flagged** $\rightarrow$ **Purchase Order Issued to Supplier**
  4. **Goods Receipt Inwards** $\rightarrow$ **Stock Automatically Credited**
  5. **Production Batch Scheduled & Completed**
  6. **Invoice Generated** $\rightarrow$ **Payment Recorded** $\rightarrow$ **Ledger & Audit Trail Updated**

### Slide 5: Core Engineering Highlights
- **Automated MRP Engine:** Formula-driven material requirement calculation taking into account BOM scrap percentages and reserved quantities.
- **Atomic Inventory Updates:** Synchronized stock increments/decrements with immutable stock movement ledger entries.
- **Fine-Grained RBAC:** 6 distinct organizational roles enforced on both client routes and server APIs.
- **Stateless Security:** Bearer JWT tokens with BCrypt-hashed credentials.

### Slide 6: Database Design & Normalization
- Fully normalized schema covering 25+ domain entities.
- Zero cyclic JSON serialization issues using explicit DTO decoupling and eager child mappings.
- Immutable audit log table recording user actions, timestamps, and before/after states.

### Slide 7: Live Demonstration Walkthrough
- Showcase:
  1. One-click demo login screen with role switcher.
  2. Live KPI dashboard with real-time sales and low-stock indicators.
  3. Creating an order for 50 Deluxe Office Chairs $\rightarrow$ Triggering material shortage on metal frame base.
  4. Procurement flow $\rightarrow$ Purchase Order $\rightarrow$ Warehouse Goods Receipt.
  5. Production completion $\rightarrow$ Invoicing and payment settlement.

### Slide 8: CI/CD & DevOps Readiness
- Automated GitHub Actions workflow building the Spring Boot JAR and compiling Angular production bundles.
- Seed data migration scripts for one-click environment spin-up.
- Postman test collection with automated token extraction for continuous API verification.

### Slide 9: Conclusion & Future Roadmap
- **Key Takeaways:** Hands-on mastery of enterprise Java full-stack development, modern frontend state management, relational database modeling, and clean code principles.
- **Future Enhancements:** RFID/Barcode automated scanning, Kafka event streaming for real-time factory telemetry.

---

## 2. Top Viva / Evaluation Questions & Model Answers

### Q1: How does your MRP (Material Requirements Planning) calculation work?
> **Answer:** "When an order is confirmed, the `MaterialRequirementService` retrieves the BOM associated with each finished good in the order lines. It multiplies each required raw material quantity by the order quantity plus scrap percentage. Then it queries the `Inventory` table for `availableQuantity` (which equals `currentQuantity - reservedQuantity`). If the required quantity exceeds available quantity, the difference is flagged as a shortage, and the order status is updated to `MATERIAL_SHORTAGE` to alert procurement."

### Q2: Why did you choose JWT over traditional server-side HTTP Sessions?
> **Answer:** "JWT enables a truly stateless backend. Our Spring Boot server does not need to allocate in-memory session tables, making it horizontal-scaling ready and cloud-native. The client stores the token and sends it in the `Authorization: Bearer` header, which our `JwtAuthTokenFilter` validates cryptographically on every request."

### Q3: How do you prevent negative inventory when multiple orders are placed concurrently?
> **Answer:** "At the service layer, every stock decrement checks that `availableQuantity >= requestedQuantity` inside a `@Transactional` block. If stock is insufficient, a `BadRequestException` is thrown before any update happens, ensuring transactional rollback."

### Q4: How is Role-Based Access Control enforced on both layers?
> **Answer:** "On the frontend, Angular functional route guards and signal-driven helpers check `AuthService.hasAnyRole(...)` to block unauthorized navigation and conditionally render UI menu items. On the backend, Spring Security evaluates `@PreAuthorize` expressions on REST controllers, returning `403 Forbidden` if a user attempts to call an unauthorized endpoint."

### Q5: How did you avoid cyclic references during JSON serialization in JPA relationships?
> **Answer:** "We decoupled entities from REST responses using dedicated Data Transfer Objects (DTOs) and controlled JPA bidirectional links via explicit `@JsonIgnore` or DTO mapping. This prevents circular reference infinite loops during Jackson serialization."

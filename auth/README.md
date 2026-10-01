# 🔐 Authentication & Role-Based Access Control (RBAC)

This module handles identity verification, role-based authorization, session security, and OAuth integrations for OrderCraft.

---

## 1. Security Architecture

- **Stateless Authentication**: Uses HMAC-SHA256 signed JSON Web Tokens (JJWT 0.12.x).
- **Password Security**: Passwords are protected using `BCryptPasswordEncoder` (10 rounds). Plaintext passwords are never stored or logged.
- **Header Structure**: Standard Bearer token authentication:
  ```http
  Authorization: Bearer <jwt-token>
  ```
- **Token Validity**: Configured via `ordercraft.jwt.expiration-ms` (Default: 24 hours / 86400000 ms).

---

## 2. RBAC Permission Matrix

OrderCraft enforces fine-grained authorization at **both backend REST endpoints (Spring Security `@PreAuthorize`)** and **frontend route navigation guards + sidebar visibility**.

| Module / Operation | ADMIN | SALES_MGR | PRODUCTION_MGR | PROCUREMENT_MGR | WAREHOUSE_MGR | FINANCE_MGR |
|:-------------------|:-----:|:---------:|:--------------:|:---------------:|:-------------:|:-----------:|
| **Dashboard**      |  ✅   |    ✅     |       ✅       |       ✅        |      ✅       |     ✅      |
| **Customers**      |  ✅   |    ✅     |       ❌       |       ❌        |      ❌       |     ❌      |
| **Customer Orders**|  ✅   |    ✅     |       ✅ (View)|       ❌        |      ❌       |     ❌      |
| **Products & Raw** |  ✅   |    ✅ (View)|     ✅       |       ✅        |      ✅       |     ❌      |
| **BOM (Bills of M)**| ✅   |    ❌     |       ✅       |       ❌        |      ❌       |     ❌      |
| **Material Shortage**|✅   |    ❌     |       ✅       |       ✅        |      ❌       |     ❌      |
| **Inventory Mgmt** |  ✅   |    ❌     |       ✅ (View)|       ✅ (View) |      ✅       |     ❌      |
| **Suppliers**      |  ✅   |    ❌     |       ❌       |       ✅        |      ❌       |     ❌      |
| **Purchase Orders**|  ✅   |    ❌     |       ❌       |       ✅        |      ✅ (View)|     ❌      |
| **Goods Receipts** |  ✅   |    ❌     |       ❌       |       ✅        |      ✅       |     ❌      |
| **Production Orders**|✅   |    ❌     |       ✅       |       ❌        |      ❌       |     ❌      |
| **Invoices**       |  ✅   |    ✅ (View)|     ❌       |       ❌        |      ❌       |     ✅      |
| **Payments**       |  ✅   |    ❌     |       ❌       |       ❌        |      ❌       |     ✅      |
| **Financial Reports**|✅   |    ❌     |       ❌       |       ❌        |      ❌       |     ✅      |
| **Audit Logs**     |  ✅   |    ❌     |       ❌       |       ❌        |      ❌       |     ❌      |
| **User Management**|  ✅   |    ❌     |       ❌       |       ❌        |      ❌       |     ❌      |

---

## 3. Seeded Accounts for Testing & Viva

All accounts come pre-configured in `backend/src/main/resources/application.properties` and `DataInitializer.java`:

| Role | Username | Password | Default Email |
|:-----|:---------|:---------|:--------------|
| **ADMIN** | `admin` | `Admin@123` | `admin@ordercraft.com` |
| **SALES_MANAGER** | `sales` | `Admin@123` | `sales@ordercraft.com` |
| **PRODUCTION_MANAGER** | `production` | `Admin@123` | `production@ordercraft.com` |
| **PROCUREMENT_MANAGER** | `procurement` | `Admin@123` | `procurement@ordercraft.com` |
| **WAREHOUSE_MANAGER** | `warehouse` | `Admin@123` | `warehouse@ordercraft.com` |
| **FINANCE_MANAGER** | `finance` | `Admin@123` | `finance@ordercraft.com` |

---

## 4. OAuth 2.0 Integration Guide (Google, Microsoft, GitHub)

The frontend login screen includes dedicated social sign-in UI buttons for enterprise readiness. To connect external OAuth2 identity providers:

### A. Google OAuth 2.0
1. Open [Google Cloud Console](https://console.cloud.google.com/) -> APIs & Services -> Credentials.
2. Create an **OAuth 2.0 Client ID** (Web Application).
3. Authorized redirect URI: `http://localhost:8080/login/oauth2/code/google`.
4. Add to `backend/pom.xml`:
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-oauth2-client</artifactId>
   </dependency>
   ```
5. Add to `application.properties`:
   ```properties
   spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID
   spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET
   ```

### B. Microsoft Azure AD
1. Register application in [Azure Portal](https://portal.azure.com/) -> App Registrations.
2. Set Redirect URI to `http://localhost:8080/login/oauth2/code/azure`.
3. Provide tenant ID, client ID, and secret in Spring properties.

### C. GitHub OAuth App
1. Go to GitHub Settings -> Developer settings -> OAuth Apps -> New OAuth App.
2. Authorization callback URL: `http://localhost:8080/login/oauth2/code/github`.

# Smart Automotive Logistics System — Feature Checklist & Setup Guide

## 1. Project Overview

This project is a **Smart Automotive Logistics System** for managing vehicle shipments from manufacturer to dealership. The system focuses on:

- Shipment management
- Real-time / simulated IoT vehicle tracking
- Delay and idle detection
- Alert management
- Shipment audit trail
- Blockchain-based event integrity verification
- Admin blockchain monitoring
- Future role-based access for different stakeholders

The current development stack is:

| Layer                    | Technology                               |
| ------------------------ | ---------------------------------------- |
| Frontend                 | React + Vite                             |
| Backend                  | Spring Boot Java                         |
| Database                 | PostgreSQL                               |
| API Testing              | Postman                                  |
| Map                      | Leaflet + React Leaflet                  |
| Blockchain Current Stage | Mock blockchain record + SHA-256 hashing |
| Future Blockchain Stage  | Hardhat + Solidity + Web3j               |
| IDE                      | VS Code                                  |

---

## 2. Feature Checklist

Legend:

- `[x]` Implemented
- `[ ]` Not implemented yet
- `[~]` Partially implemented / mock version implemented

---

## 3. Backend Features

### 3.1 Shipment Management

- [x] Create shipment API
- [x] View all shipments API
- [x] View shipment by ID API
- [x] Update shipment status API
- [x] Delete shipment API
- [x] Clean DTO structure for shipment request/response
- [x] Shipment status enum
- [x] Backend validation for valid shipment status transitions
- [x] Prevent invalid transition such as `CREATED -> DELIVERED`
- [x] Prevent logistics/admin status actions from bypassing dealership receipt confirmation
- [x] Restrict receipt confirmation to the dealership assigned by `dealership_key`
- [x] Set `DELIVERED` only after assigned dealership receipt confirmation
- [x] Assign `MANUFACTURING` stage and `NOT_STARTED` WIP state during creation
- [x] Reject past estimated arrival values in the browser and backend API
- [x] Restrict manual status actions to logistics transit start and cancellation
- [x] Keep IoT simulation idle until the logistics provider starts transit
- [x] Reserve `ARRIVED` for automatic simulation arrival detection
- [x] Prevent updates after `DELIVERED`
- [x] Prevent updates after `CANCELLED`

Current shipment statuses:

```text
CREATED
READY_FOR_DISPATCH
PICKED_UP
IN_TRANSIT
DELAYED
ARRIVED
DELIVERED
CANCELLED
```

---

### 3.2 Tracking / IoT Simulation

- [x] Tracking event entity
- [x] Tracking event repository
- [x] Tracking event service
- [x] Tracking event controller
- [x] Create tracking event API
- [x] View tracking events by shipment ID
- [x] View latest tracking event by shipment ID
- [x] System automatically generates GPS points
- [x] System automatically varies vehicle speed
- [x] Database-backed random telemetry profiles
- [x] System-owned simulation sessions
- [x] Route-relative simulation movement without accumulated coordinate drift
- [x] Bounded temporary route-deviation profiles with automatic route recovery
- [x] Route-aware simulation event limit for long-distance delivery completion
- [x] Detect normal movement
- [x] Detect delayed movement
- [x] Detect idle vehicle
- [x] Update shipment status to `DELAYED` when delayed/idle condition is detected
- [x] Update shipment status back to `IN_TRANSIT` when movement becomes normal
- [x] Stop active simulation immediately after arrival, delivery, or cancellation
- [x] Restrict delay monitoring to `IN_TRANSIT` and `DELAYED` shipments
- [x] Prevent historical telemetry from reverting `ARRIVED` to `DELAYED`
- [x] Lock logistics workflow actions after `ARRIVED`; dealership receipt is the
  only delivery-completion action

Current condition statuses:

```text
NORMAL
IDLE
DELAYED
ROUTE_DEVIATION
SENSOR_WARNING
```

Implemented logic:

```text
speed <= 1.0  -> IDLE
speed < 15.0  -> DELAYED
speed >= 15.0 -> NORMAL
```

---

### 3.3 Alert System

- [x] Alert entity
- [x] Alert type enum
- [x] Alert severity enum
- [x] Alert status enum
- [x] Alert repository
- [x] Alert service
- [x] Alert controller
- [x] Create alert when vehicle is delayed
- [x] Create alert when vehicle is idle
- [x] Avoid duplicate open alerts of the same type for the same shipment
- [x] View open alerts
- [x] View alerts by shipment
- [x] Acknowledge alert
- [x] Resolve alert

Current alert types:

```text
DELAY
IDLE
ROUTE_DEVIATION
SENSOR_WARNING
MISSING_UPDATE
```

Current alert severities:

```text
LOW
MEDIUM
HIGH
```

Current alert statuses:

```text
OPEN
ACKNOWLEDGED
RESOLVED
```

---

### 3.4 Dashboard Summary API

- [x] Dashboard summary DTO
- [x] Dashboard service
- [x] Dashboard controller
- [x] Count total shipments
- [x] Count created shipments
- [x] Count ready for dispatch shipments
- [x] Count picked up shipments
- [x] Count in transit shipments
- [x] Count delayed shipments
- [x] Count arrived shipments
- [x] Count delivered shipments
- [x] Count cancelled shipments
- [x] Count open alerts

Endpoint:

```http
GET /api/dashboard/summary
```

---

### 3.5 Shipment Event / Audit Trail

- [x] Shipment event entity
- [x] Shipment event type enum
- [x] Shipment event repository
- [x] Shipment event service
- [x] Shipment event controller
- [x] Record event when shipment is created
- [x] Record event when shipment status changes
- [x] Record event when tracking is updated
- [x] Record event when delay is detected
- [x] Record event when idle condition is detected
- [x] Record event when alert is created
- [x] Show shipment event timeline in frontend

Current shipment event types:

```text
SHIPMENT_CREATED
STATUS_CHANGED
TRACKING_UPDATED
DELAY_DETECTED
IDLE_DETECTED
ALERT_CREATED
DELIVERY_CONFIRMED
```

---

### 3.6 Blockchain Record Module

- [x] Blockchain record entity
- [x] Blockchain record status enum
- [x] Blockchain record repository
- [x] Blockchain record service
- [x] Blockchain record controller
- [x] Generate SHA-256 hash for shipment events
- [x] Store event hash
- [x] Store mock transaction hash
- [x] Store mock block number
- [x] Store blockchain record status
- [x] Get blockchain records by shipment ID
- [x] Get all blockchain records for admin monitor
- [x] Verify blockchain record integrity
- [x] Detect hash mismatch
- [x] Mark blockchain record as `FAILED` when verification fails
- [x] Store verification message
- [x] Store verified timestamp
- [~] Real blockchain transaction using Solidity/Hardhat/Web3j

Current blockchain statuses:

```text
PENDING
RECORDED
FAILED
```

Current stage:

```text
Mock blockchain record implemented.
Real Solidity/Hardhat integration not yet implemented.
```

---

### 3.7 Admin Notification System

- [x] Admin notification entity
- [x] Admin notification type enum
- [x] Admin notification status enum
- [x] Admin notification repository
- [x] Admin notification service
- [x] Admin notification controller
- [x] Create admin notification when blockchain tamper/hash mismatch is detected
- [x] Avoid duplicate unread notifications for same reference
- [x] View all admin notifications
- [x] View unread admin notifications
- [x] Mark admin notification as read
- [ ] Automatically email company when blockchain tampering is detected

Current admin notification types:

```text
BLOCKCHAIN_TAMPER_DETECTED
SYSTEM_ALERT
```

Current admin notification statuses:

```text
UNREAD
READ
```

---

### 3.8 Exception Handling

- [x] Resource not found exception
- [x] Bad request exception
- [x] Global exception handler
- [x] Return `404 Not Found` for missing resources
- [x] Return `400 Bad Request` for invalid business rules
- [ ] Cleaner validation error response for form validation fields

---

## 4. Frontend Features

### 4.1 Frontend Base Structure

- [x] React + Vite setup
- [x] React Router setup
- [x] Axios client setup
- [x] API service folder created
- [x] Pages folder created
- [x] Components folder created
- [x] Styles folder created and categorized

Current frontend structure:

```text
frontend/src/
├── api/
├── components/
│   ├── blockchain/
│   ├── dashboard/
│   ├── forms/
│   ├── layout/
│   ├── map/
│   └── shipments/
├── pages/
│   └── admin/
├── styles/
├── App.jsx
└── main.jsx
```

---

### 4.2 Dashboard Page

- [x] Dashboard summary cards
- [x] Total shipments card
- [x] In transit shipments card
- [x] Delayed shipments card
- [x] Delivered shipments card
- [x] Open alerts card
- [x] Open alerts panel
- [x] Refresh button

---

### 4.3 Shipment Pages

- [x] Shipment list page
- [x] View shipment details page
- [x] Create shipment page
- [x] Create shipment form
- [x] Shipment workflow action buttons
- [x] Status badge display
- [x] Shipment information panel
- [x] Latest tracking panel
- [x] Tracking history table
- [x] Shipment event timeline
- [x] Simulate IoT tracking update form
- [x] User-friendly normal shipment workflow

Current workflow buttons:

```text
Start Transit (logistics provider; starts IoT movement)
Cancel (logistics provider; unavailable after arrival)
Confirm Receipt (assigned dealership; completes delivery)
```

---

### 4.4 Map / Tracking UI

- [x] Leaflet map installed
- [x] React Leaflet installed
- [x] Latest vehicle location marker
- [x] Popup with shipment and vehicle details
- [x] Route history polyline
- [x] Placeholder when no tracking data exists

---

### 4.5 Alert UI

- [x] Alerts page
- [x] Open alerts display
- [x] Alert severity badge
- [x] Acknowledge alert button
- [x] Resolve alert button
- [x] Alerts shown inside shipment details page

---

### 4.6 Admin Blockchain Monitor UI

- [x] Admin blockchain monitor page
- [x] View all blockchain records
- [x] Total blockchain record summary
- [x] Recorded count
- [x] Pending count
- [x] Failed count
- [x] Search blockchain records
- [x] Filter by blockchain status
- [x] View event hash
- [x] View transaction hash
- [x] View block number
- [x] Verify blockchain record integrity button
- [x] Show verification result
- [x] Show failed/tampered status
- [ ] Restrict page to admin role only

---

### 4.7 Admin Notification UI

- [x] Admin notifications page
- [x] View all admin notifications
- [x] Filter unread/read notifications
- [x] Mark notification as read
- [x] User-friendly notification card display
- [ ] Show notification count badge in sidebar/topbar
- [ ] Send notification to company via email

---

### 4.8 CSS Organization

- [x] CSS split by category
- [x] Base styles separated
- [x] Layout styles separated
- [x] Form styles separated
- [x] Table styles separated
- [x] Utility styles separated
- [x] Dashboard styles separated
- [x] Shipment styles separated
- [x] Alert styles separated
- [x] Map styles separated
- [x] Blockchain styles separated
- [x] Notification styles separated

Current CSS structure:

```text
frontend/src/styles/
├── base.css
├── layout.css
├── forms.css
├── tables.css
├── utilities.css
├── dashboard.css
├── shipments.css
├── alerts.css
├── map.css
├── blockchain.css
├── notifications.css
└── index.css
```

---

## 5. Features Still Need To Be Implemented

### 5.1 Authentication and Role-Based Access

- [ ] Login page
- [ ] Register user / seed users
- [ ] JWT authentication backend
- [ ] Password hashing
- [ ] User entity
- [ ] Role enum
- [ ] Role-based authorization backend
- [ ] Protected frontend routes
- [ ] Hide admin sidebar links for non-admin users
- [ ] Restrict Admin Blockchain Monitor to admin only
- [ ] Restrict Admin Notifications to admin only

Planned roles:

```text
ADMIN
MANUFACTURER
LOGISTICS_PROVIDER
DEALERSHIP
```

Implemented access:

| Feature                    | Admin | Manufacturer | Logistics Provider | Dealership |
| -------------------------- | ----: | -----------: | -----------------: | ---------: |
| Dashboard                  |   Yes |          Yes |                Yes |        Yes |
| Create Shipment            |    No |          Yes |                 No |         No |
| Update Shipment Status     |    No |           No |       Start/cancel | Receipt only |
| Confirm Delivery Receipt   |    No |           No |                 No | Assigned only |
| Simulate / Tracking Update | Monitor |      System only |            Monitor |         No |
| View Alerts                |   Yes |          Yes |                Yes |        Yes |
| Resolve Alerts             |   Yes |      Limited |                Yes |         No |
| Blockchain Monitor         |   Yes |           No |                 No |         No |
| Admin Notifications        |   Yes |           No |                 No |         No |

---

### 5.2 QR Code Flow

- [ ] Generate QR code for each shipment
- [ ] QR code opens shipment details page
- [ ] Logistics partner scans QR
- [ ] QR scan confirms shipment identity
- [ ] QR scan supports pickup / arrival / delivery confirmation
- [ ] Important QR-triggered events are automatically recorded to blockchain

Planned flow:

```text
Scan QR
↓
Open shipment page
↓
Confirm pickup / arrival / delivery
↓
Create shipment event
↓
Auto blockchain proof
```

---

### 5.3 Real Blockchain Integration

- [ ] Hardhat project setup
- [ ] Solidity smart contract
- [ ] Store shipment event hash on-chain
- [ ] Deploy contract to local Hardhat network
- [ ] Connect Spring Boot to blockchain using Web3j
- [ ] Replace mock transaction hash with real transaction hash
- [ ] Replace mock block number with real block number
- [ ] Verify on-chain hash against database event hash

---

### 5.4 Company Notification

- [ ] Email configuration in Spring Boot
- [ ] Email template for blockchain tamper alert
- [ ] Send company notification when blockchain verification fails
- [ ] Store notification delivery status
- [ ] Optional: admin manually resend notification

---

### 5.5 Reporting

- [ ] Shipment report page
- [ ] Delivery performance report
- [ ] Delay report
- [ ] Alert report
- [ ] Export report as PDF/CSV

---

### 5.6 UI Improvements

- [ ] Better responsive design for mobile/tablet
- [ ] Loading skeletons
- [ ] Toast notifications
- [ ] Confirmation modal before cancel shipment
- [ ] Confirmation modal before resolving alert
- [ ] Better date/time formatting
- [ ] Pagination for long tables
- [ ] Search/filter shipment list

---

## 6. Installation Guide

## 6.1 Required Software

Install these before running the project:

```text
Java 21
Node.js
npm
PostgreSQL
pgAdmin 4
Git
VS Code
Postman
```

Recommended VS Code extensions:

```text
Extension Pack for Java
Spring Boot Extension Pack
Spring Initializr Java Support
ES7+ React/Redux snippets
Prettier
ESLint
PostgreSQL
GitLens
Thunder Client
```

---

## 7. Database Setup

### 7.1 Start PostgreSQL

Open Windows Services and make sure PostgreSQL is running:

```text
postgresql-x64-16
```

or:

```text
postgresql-x64-17
```

### 7.2 Create database

In pgAdmin:

```text
Servers
→ Local PostgreSQL
→ Databases
→ Create Database
```

Database name:

```text
auto_logistics_db
```

Owner:

```text
postgres
```

---

## 8. Backend Setup and Run Commands

### 8.1 Open backend folder

From the project root:

```powershell
cd backend
```

### 8.2 Configure database connection

Open:

```text
backend/src/main/resources/application.properties
```

Recommended development config:

```properties
spring.application.name=autologistics

server.port=8080

spring.datasource.url=jdbc:postgresql://localhost:5432/auto_logistics_db
spring.datasource.username=postgres
spring.datasource.password=admin123

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

Change password if your PostgreSQL password is different.

### 8.3 Run backend

Windows PowerShell:

```powershell
.\mvnw spring-boot:run
```

Mac/Linux/Git Bash:

```bash
./mvnw spring-boot:run
```

Expected successful message:

```text
Tomcat started on port 8080
Started AutologisticsApplication
```

Backend URL:

```text
http://localhost:8080
```

---

## 9. Frontend Setup and Run Commands

### 9.1 Open frontend folder

From the project root:

```powershell
cd frontend
```

### 9.2 Install packages

```powershell
npm install
```

If needed, install key packages manually:

```powershell
npm install axios react-router-dom leaflet react-leaflet
```

### 9.3 Run frontend

```powershell
npm run dev
```

Expected output:

```text
Local: http://localhost:5173/
```

Frontend URL:

```text
http://localhost:5173
```

---

## 10. Running Backend and Frontend Together

Open two terminals.

Terminal 1 — Backend:

```powershell
cd backend
.\mvnw spring-boot:run
```

Terminal 2 — Frontend:

```powershell
cd frontend
npm run dev
```

Then open:

```text
http://localhost:5173
```

---

## 11. Useful API Endpoints

### Shipment

```http
GET    /api/shipments
GET    /api/shipments/{id}
POST   /api/shipments
PUT    /api/shipments/{id}/status?status=IN_TRANSIT
DELETE /api/shipments/{id}
```

### Tracking

```http
GET  /api/shipments/{shipmentId}/tracking-events
GET  /api/shipments/{shipmentId}/tracking-events/latest
POST /api/shipments/{shipmentId}/tracking-events
```

### Simulation

```http
GET /api/shipments/{shipmentId}/simulation/session
GET /api/simulation-sessions
```

### Alerts

```http
GET /api/alerts/open
GET /api/alerts/shipment/{shipmentId}
PUT /api/alerts/{alertId}/acknowledge
PUT /api/alerts/{alertId}/resolve
```

### Dashboard

```http
GET /api/dashboard/summary
```

### Shipment Events

```http
GET /api/shipments/{shipmentId}/events
```

### Blockchain Records

```http
GET  /api/blockchain-records
GET  /api/blockchain-records/shipment/{shipmentId}
POST /api/blockchain-records/{recordId}/verify
```

### Admin Notifications

```http
GET /api/admin/notifications
GET /api/admin/notifications/unread
PUT /api/admin/notifications/{notificationId}/read
```

---

## 12. Demo Flow

### 12.1 Normal shipment flow

```text
Create Shipment
↓
System assigns Manufacturing / Not Started
↓
Start Transit (logistics provider)
↓
System simulates tracking updates
↓
System detects arrival
↓
Confirm Receipt (assigned dealership)
↓
Shipment becomes Delivered
```

### 12.2 Delay detection flow

```text
Open shipment details
↓
Set speed to 8
↓
Click Simulate Next Point
↓
System detects DELAYED
↓
Shipment status becomes DELAYED
↓
Alert is created
↓
Shipment event is created
↓
Important event is recorded in blockchain record
```

### 12.3 Idle detection flow

```text
Open shipment details
↓
Set speed to 0
↓
Click Simulate Next Point
↓
System detects IDLE
↓
Alert is created
↓
Shipment event is created
↓
Important event is recorded in blockchain record
```

### 12.4 Admin blockchain monitor flow

```text
Open Admin Blockchain Monitor
↓
View blockchain records
↓
Click Verify Integrity
↓
If hash matches: status remains RECORDED
↓
If hash mismatch: status becomes FAILED and admin notification is created
```

---

## 13. Recommended Next Development Order

Suggested next implementation order:

1. [ ] Clean validation error response
2. [ ] Notification badge in sidebar/topbar
3. [ ] QR code generation for shipment
4. [ ] QR scan/open shipment page flow
5. [ ] Login and role-based access
6. [ ] Admin-only route protection
7. [ ] Real blockchain with Hardhat/Solidity
8. [ ] Web3j integration with Spring Boot
9. [ ] Company email notification
10. [ ] Reports and exports

---

## 14. Current Status Summary

The system currently has a strong working MVP:

- Shipment management works
- Frontend shipment creation works
- Tracking simulation works
- Map visualization works
- Alert detection works
- Shipment event timeline works
- Mock blockchain verification works
- Admin blockchain monitor works
- Admin notifications work

The main remaining major features are:

- Authentication and role-based access
- QR code workflow
- Real blockchain smart contract integration
- Company notification via email
- Reports/export

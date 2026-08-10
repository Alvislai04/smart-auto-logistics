# Project Progress Tracker

Use this file as the living checklist for the FYP implementation. Change `[ ]`
to `[x]` only after the feature is implemented, tested, and visible in the
working system.

Legend:

- `[x]` Completed
- `[~]` Partially completed or needs improvement
- `[ ]` Not implemented yet

## 1. Core IR Requirements

- [x] Real-time vehicle tracking visibility
- [x] Centralized dashboard monitoring
- [x] Shipment status updates
- [x] ETA visibility
- [x] ETD visibility
- [x] Tracking of logistics stages
- [x] Alert and notification mechanism
- [x] User-friendly dashboard interface
- [x] Responsive web-based access baseline
- [~] Reporting and visual monitoring support
- [x] Secure logistics event recording
- [x] Audit trail of shipment activities
- [x] Status change logging
- [x] Improved data consistency through centralized records
- [x] Operational decision-making support
- [x] Practical workflow-oriented design

## 2. Authentication and Access Control

- [x] User entity
- [x] User company/location profile fields for manufacturer and dealership routing
- [x] Database-backed shipment party profile master data
- [x] User role enum
- [x] Admin-created database user accounts
- [x] Optional first-admin bootstrap configuration
- [x] BCrypt password hashing
- [x] Login API
- [x] JWT generation
- [x] JWT authentication filter
- [x] Frontend login page connected to backend
- [x] Production-style blank email/password login page
- [x] Frontend hardcoded demo login users removed
- [x] Role shortcut login cards removed
- [x] Eye-icon password visibility toggle on login
- [x] Frontend token storage
- [x] Axios bearer token interceptor
- [x] Expired or invalid JWT returns clean 401 response
- [x] Login API is not blocked by stale browser tokens
- [x] Protected frontend routes
- [x] Hide role-inaccessible navigation items
- [x] Backend role authorization for admin APIs
- [x] Manufacturer-only shipment creation authorization
- [x] Database-backed user role option master data
- [x] Admin user management screen
- [x] Create user API
- [x] Update user role/status API
- [x] User self-service profile update API/UI
- [x] User self-service password change API/UI
- [x] Non-admin account profile sync to shipment party profiles
- [ ] Admin reset user password flow

## 3. Shipment Management

- [x] Shipment entity
- [x] Shipment DTOs
- [x] Shipment repository
- [x] Shipment service
- [x] Shipment controller
- [x] Create shipment
- [x] Shipment creation restricted to manufacturer role
- [x] Auto-generated unique shipment code
- [x] Backend-controlled shipment code that users cannot edit from UI
- [x] Database-backed Mercedes-Benz vehicle model selection by unique model key
- [x] View all shipments
- [x] View shipment by ID
- [x] Delete shipment
- [x] Status transition validation
- [x] Workflow status buttons
- [x] Shipment details page
- [x] Shipment list page
- [x] Create shipment page
- [ ] Edit shipment details
- [x] Manufacturer origin dropdown from database party profiles by unique key
- [x] Dealership destination/name dropdown from database party profiles by unique key
- [x] Search shipment list
- [x] Filter shipment list by status
- [x] Filter shipment list by logistics stage
- [x] Pagination for shipment list
- [x] Better date/time formatting
- [ ] Confirmation modal before cancelling shipment

## 4. Shipment Operation Data

- [x] Shipment code
- [x] Manufacturer profile reference
- [x] Dealership profile reference
- [x] Vehicle model code reference
- [x] Vehicle VIN
- [x] Vehicle model
- [x] Origin
- [x] Destination
- [x] Planned origin and destination coordinates
- [x] Dealership name
- [x] Estimated arrival
- [x] Estimated departure
- [x] Current logistics stage
- [x] Stock location
- [x] Work-in-progress status
- [x] Customer or receiver information
- [x] Responsible person
- [x] Delivery receipt confirmation timestamp
- [x] Delivery receipt confirmation user
- [x] Shipment document attachment metadata

## 5. IoT Tracking and Simulation

- [x] Tracking event entity
- [x] Tracking event DTOs
- [x] Tracking event repository
- [x] Tracking event service
- [x] Tracking event controller
- [x] Create tracking event
- [x] View tracking history by shipment
- [x] Latest tracking display
- [x] Simulate next GPS point
- [x] Simulate speed
- [x] Detect normal movement
- [x] Detect delay from low speed
- [x] Detect idle condition
- [x] Route deviation enum support
- [x] Route deviation detection logic
- [x] Missing update detection logic
- [x] Sensor warning detection logic
- [x] Continuous simulation start/pause/stop
- [x] Simulation interval control
- [x] Database-backed simulation session state
- [x] Multi-vehicle simulation dashboard

## 6. Map and Visual Tracking UI

- [x] Leaflet map setup
- [x] Vehicle marker
- [x] Route polyline
- [x] Popup with shipment details
- [x] Empty map state
- [x] Origin and destination markers
- [x] Planned route vs actual route display
- [ ] Route deviation visual warning
- [ ] Better mobile map controls

## 7. Alerts and Notifications

- [x] Alert entity
- [x] Alert type enum
- [x] Alert severity enum
- [x] Alert status enum
- [x] Alert repository
- [x] Alert service
- [x] Alert controller
- [x] Delay alert
- [x] Idle alert
- [x] Prevent duplicate open alert for same shipment/type
- [x] View open alerts
- [x] View alerts by shipment
- [x] Acknowledge alert
- [x] Resolve alert
- [x] Alerts page
- [x] Alerts inside shipment details page
- [x] ETA overdue alert
- [x] Missing update alert
- [x] Route deviation alert
- [x] Sensor warning alert
- [x] Plan-vs-actual progress shortfall alert
- [ ] Notification count badge in sidebar/topbar
- [ ] Toast notification after important user actions

## 8. Shipment Event Audit Trail

- [x] Shipment event entity
- [x] Shipment event type enum
- [x] Shipment event repository
- [x] Shipment event service
- [x] Shipment event controller
- [x] Event when shipment is created
- [x] Event when status changes
- [x] Event when tracking updates
- [x] Event when delay is detected
- [x] Event when idle condition is detected
- [x] Event when alert is created
- [x] Event when delivery receipt is confirmed
- [x] Event timeline UI
- [x] Automatic blockchain recording for important events
- [ ] Show event actor/user
- [ ] Show event source type, such as user action or IoT simulation
- [ ] Export event timeline

## 9. Blockchain Ledger

- [x] Blockchain folder structure
- [x] Hardhat configuration
- [x] Solidity `ShipmentLedger` contract
- [x] Contract deploy script
- [x] Contract tests
- [x] Backend Web3j dependency
- [x] Backend blockchain properties
- [x] Backend blockchain client interface
- [x] Web3j blockchain ledger client
- [x] Store shipment event hash on-chain
- [x] Store real transaction hash
- [x] Store real block number
- [x] Verify database record against on-chain hash
- [x] Mark record failed when blockchain write/read fails
- [x] Admin notification for blockchain action failure
- [x] Admin tamper notification for hash mismatch
- [x] Company notification record for hash mismatch
- [x] Admin blockchain monitor page
- [x] Per-shipment blockchain verification panel
- [ ] Retry failed blockchain record action
- [ ] Display blockchain contract address in admin monitor
- [ ] Display chain/network health status
- [ ] Add backend unit tests with mocked blockchain client

## 10. Admin and Company Notifications

- [x] Admin notification entity
- [x] Admin notification service
- [x] Admin notification controller
- [x] Admin notification UI
- [x] Mark admin notification as read
- [x] Company notification entity
- [x] Company notification service
- [x] Company notification controller
- [x] Company notification admin UI
- [ ] Email configuration
- [ ] Email template for blockchain integrity alert
- [ ] Send email when blockchain verification fails
- [ ] Store email delivery status
- [ ] Manual resend company notification

## 11. Dashboard and Reporting

- [x] Dashboard summary DTO
- [x] Dashboard service
- [x] Dashboard controller
- [x] Total shipment count
- [x] Shipment count by status
- [x] Open alert count
- [x] Role-scoped dashboard summary counts
- [x] Dashboard-scoped open alerts
- [x] Single dashboard overview endpoint for summary and latest open alerts
- [x] Grouped shipment status count queries for faster Supabase dashboard reads
- [x] Dashboard/report read endpoints avoid full active-shipment alert scans
- [x] Dedicated admin dashboard route
- [x] Dedicated manufacturer dashboard route
- [x] Dedicated logistics provider dashboard route
- [x] Dedicated dealership dashboard route
- [x] Dashboard summary cards
- [x] Open alerts panel
- [x] Shipment performance chart
- [x] Delay rate chart
- [x] Delivery completion chart
- [x] Shipment report page
- [x] Delay report page
- [x] Alert report page
- [x] Export CSV
- [ ] Export PDF

## 12. UI and UX Quality

- [x] Sidebar layout
- [x] Topbar layout
- [x] Role-based navigation
- [x] Auth screen
- [x] Normal login screen without demo credentials
- [x] Terminal-style login screen redesign
- [x] Animated logistics map visual on login
- [x] Ticket-style login panel
- [x] Login password visibility icon control
- [x] Login redirects users to their own dashboard first
- [x] Root path redirects authenticated users to role dashboard
- [x] Sidebar dashboard link resolves by current role
- [x] Dashboard page
- [x] Reports page
- [x] Shipment list page
- [x] Shipment details page
- [x] Create shipment form
- [x] Create shipment dropdowns keep placeholder defaults until selected
- [x] Alerts page
- [x] Simulation sessions page
- [x] Admin blockchain monitor page
- [x] Admin notifications page
- [x] Company notifications page
- [x] Integrity cases page
- [x] Admin user accounts page
- [x] My account settings page
- [~] Responsive layout baseline
- [ ] Loading skeletons
- [ ] Empty states for every major list
- [ ] Confirmation modals for destructive actions
- [x] Consistent date/time formatter utility
- [ ] Reusable status badge component
- [ ] Reusable page loading/error component
- [ ] Full mobile QA pass

## 13. CSS and Folder Structure

- [x] `frontend/src/api`
- [x] `frontend/src/auth`
- [x] `frontend/src/components/blockchain`
- [x] `frontend/src/components/dashboard`
- [x] `frontend/src/components/forms`
- [x] `frontend/src/components/icons`
- [x] `frontend/src/components/layout`
- [x] `frontend/src/components/map`
- [x] `frontend/src/components/reports`
- [x] `frontend/src/components/simulation`
- [x] `frontend/src/components/shipments`
- [x] `frontend/src/pages`
- [x] `frontend/src/pages/admin`
- [x] `frontend/src/pages/auth`
- [x] `frontend/src/pages/reports`
- [x] `frontend/src/styles/base.css`
- [x] `frontend/src/styles/layout.css`
- [x] `frontend/src/styles/forms.css`
- [x] `frontend/src/styles/tables.css`
- [x] `frontend/src/styles/dashboard.css`
- [x] `frontend/src/styles/shipments.css`
- [x] `frontend/src/styles/alerts.css`
- [x] `frontend/src/styles/accounts.css`
- [x] `frontend/src/styles/reports.css`
- [x] `frontend/src/styles/simulation.css`
- [x] `frontend/src/styles/map.css`
- [x] `frontend/src/styles/blockchain.css`
- [x] `frontend/src/styles/notifications.css`
- [x] `frontend/src/utils`
- [x] `backend/src/main/java/com/fyp/autologistics/blockchain`
- [x] `backend/src/main/java/com/fyp/autologistics/config`
- [x] `backend/src/main/java/com/fyp/autologistics/controller`
- [x] `backend/src/main/java/com/fyp/autologistics/dto`
- [x] `backend/src/main/java/com/fyp/autologistics/entity`
- [x] `backend/src/main/java/com/fyp/autologistics/enums`
- [x] `backend/src/main/java/com/fyp/autologistics/exception`
- [x] `backend/src/main/java/com/fyp/autologistics/repository`
- [x] `backend/src/main/java/com/fyp/autologistics/service`
- [x] `backend/src/main/java/com/fyp/autologistics/specification`
- [x] `backend/src/main/java/com/fyp/autologistics/util`
- [x] `blockchain/contracts`
- [x] `blockchain/scripts`
- [x] `blockchain/test`

## 14. Testing and Validation

- [x] Backend application context test
- [x] Frontend production build
- [x] Targeted lint for touched auth/blockchain/shipment/alert files
- [x] Solidity contract compile
- [x] Solidity contract tests
- [x] Blockchain production dependency audit
- [x] Full Supabase test-data SQL script
- [x] Full Supabase test-data verification SQL script
- [x] Continuous simulation test-data SQL script
- [~] Full frontend lint
- [ ] Backend service unit tests
- [ ] Backend controller integration tests
- [ ] Frontend component tests
- [ ] End-to-end happy path test
- [ ] End-to-end blockchain failure path test
- [ ] User acceptance test checklist

## 15. Deployment and Cloud Database

- [x] Supabase PostgreSQL schema script
- [x] Supabase schema supports shipment form options and unique shipment codes
- [x] Supabase migration query for database-backed shipment master data
- [x] Supabase migration query for shipment dropdown master data refresh
- [x] Supabase migration query for user account role/profile management
- [x] Supabase migration query for normal login readiness
- [x] Supabase migration query for continuous simulation sessions
- [x] Supabase SQL template for account password reset
- [x] Root `.env` configured for Supabase Session Pooler
- [x] Backend datasource loads Supabase settings from environment/config import
- [x] Local PostgreSQL datasource fallback removed from backend config
- [x] Supabase connection smoke test from Spring Boot runtime
- [ ] Deployment environment variables configured on hosting platform

Current known lint debt:

- [x] `frontend/src/pages/DashboardPage.jsx`
- [ ] `frontend/src/pages/admin/AdminNotificationsPage.jsx`
- [ ] `frontend/src/pages/admin/CompanyNotificationsPage.jsx`
- [ ] `frontend/src/pages/admin/IntegrityCasesPage.jsx`

## 16. Recommended Next Development Order

1. [x] Add richer shipment operation fields: ETD, current stage, stock location,
   WIP status, responsible person, and receipt confirmation.
2. [x] Add shipment list search/filter/pagination.
3. [x] Add advanced alert rules: ETA overdue, missing update, route deviation,
   and plan-vs-actual shortfall.
4. [x] Add reporting pages and CSV export.
5. [ ] Add retry flow for failed blockchain records.
6. [x] Add admin user management.
7. [ ] Add email notification delivery.
8. [ ] Clean remaining frontend lint debt.
9. [ ] Add backend service/controller tests.
10. [ ] Perform full UI responsive QA.

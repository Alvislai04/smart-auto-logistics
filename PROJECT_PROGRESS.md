# Project Progress Tracker

Use this file as the living checklist for the FYP implementation. Change `[ ]`
to `[x]` only after the feature is implemented, tested, and visible in the
working system.

Legend:

- `[x]` Completed
- `[~]` Partially completed or needs improvement
- `[ ]` Not implemented yet
- `[-]` Not planned for the current implementation scope

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
- [x] Admin master-data configuration UI for shipment dropdown and simulation data
- [x] User role enum
- [x] Admin-created database user accounts
- [x] Optional first-admin bootstrap configuration
- [x] BCrypt password hashing
- [x] Login API
- [x] JWT generation
- [x] JWT authentication filter
- [x] Frontend login page connected to backend
- [x] Production-style blank email/password login page
- [x] Professional operations-console login UI redesign
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
- [-] Admin reset user password flow

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
- [x] Admin-managed vehicle model master data
- [x] View all shipments
- [x] View shipment by ID
- [x] Delete shipment
- [x] Status transition validation
- [x] Workflow status buttons
- [x] Shipment details page
- [x] Shipment list page
- [x] Create shipment page
- [x] Edit shipment details
- [x] Manufacturer origin dropdown from database party profiles by unique key
- [x] Dealership destination/name dropdown from database party profiles by unique key
- [x] Admin-managed manufacturer, logistics provider, and dealership party profiles
- [x] Search shipment list
- [x] Filter shipment list by status
- [x] Filter shipment list by logistics stage
- [x] Pagination for shipment list
- [x] Better date/time formatting
- [-] Confirmation modal before cancelling shipment

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
- [-] Shipment document file upload/download storage

## 5. IoT Tracking and Simulation

- [x] Tracking event entity
- [x] Tracking event DTOs
- [x] Tracking event repository
- [x] Tracking event service
- [x] Tracking event controller
- [x] Create tracking event
- [x] View tracking history by shipment
- [x] Latest tracking display
- [x] System automatically generates GPS points
- [x] System automatically varies speed
- [x] Detect normal movement
- [x] Detect delay from low speed
- [x] Detect idle condition
- [x] Route deviation enum support
- [x] Route deviation detection logic
- [x] Missing update detection logic
- [x] Sensor warning detection logic
- [x] User-controlled simulation actions removed from API/UI
- [x] Database-backed random simulation profile state
- [x] Admin-managed simulation profile master data
- [x] Database-backed system-managed simulation session state
- [x] Automatic session provisioning after shipment creation
- [x] Scheduler backfill for existing eligible shipments
- [x] Multi-vehicle simulation monitoring dashboard
- [-] Detailed simulated sensor fields, such as shock, temperature, humidity,
  or sensor health

## 6. Map and Visual Tracking UI

- [x] Leaflet map setup
- [x] Vehicle marker
- [x] Route polyline
- [x] Popup with shipment details
- [x] Empty map state
- [x] Origin and destination markers
- [x] Planned route vs actual route display
- [-] Route deviation visual warning
- [-] Better mobile map controls

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
- [x] Manufacturer access to scoped full alerts page
- [x] Role-scoped open alert list and count API
- [x] Sidebar open-alert notification badge
- [x] Toast notification after important user actions
- [x] ETA overdue alert
- [x] Missing update alert
- [x] Route deviation alert
- [x] Sensor warning alert
- [x] Plan-vs-actual progress shortfall alert
- [x] Low-stock alert
- [x] Order update alert
- [x] Database-backed stock alert rules with generated unique rule keys
- [x] Unique company/model/stage rule scope enforced by service and database
- [x] Stock quantity calculated from non-cancelled shipment records
- [x] Automatic scheduled stock evaluation
- [x] Immediate stock evaluation after shipment creation/status changes
- [x] Low-stock alert update without duplicate active alerts
- [x] Automatic low-stock recovery resolution
- [x] Order-update alert generated from manual status changes
- [x] Order-update alert generated from IoT simulation and alert-engine changes
- [x] Previous active order update resolved when a newer update is generated
- [x] Stock Monitoring page for admin, manufacturer, and dealership roles
- [x] Admin-only stock rule create/edit controls
- [x] Manufacturer/dealership read-only company-scoped stock view
- [x] Alert links distinguish shipment and stock-rule references

## 8. Shipment Event Audit Trail

- [x] Shipment event entity
- [x] Shipment event type enum
- [x] Shipment event repository
- [x] Shipment event service
- [x] Shipment event controller
- [x] Event when shipment is created
- [x] Event when shipment details are edited
- [x] Event when status changes
- [x] Event when tracking updates
- [x] Event when delay is detected
- [x] Event when idle condition is detected
- [x] Event when alert is created
- [x] Event when delivery receipt is confirmed
- [x] Event timeline UI
- [x] Automatic blockchain recording for important events
- [x] Show event actor/user
- [x] Show event source type, such as user action or IoT simulation
- [-] Export event timeline

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
- [x] Retry failed blockchain record action
- [x] Prevent retry for records already written on-chain
- [x] Preserve original stored event hash during retry
- [x] Persist retry attempt count and last retry time
- [x] Display blockchain contract address in admin monitor
- [x] Display RPC, contract, signer, chain, and latest-block health status
- [x] Disable retry controls until blockchain health is ready
- [x] Paginate and filter admin blockchain records
- [x] Add backend unit tests with mocked blockchain client

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
- [-] Email configuration
- [-] Email template for blockchain integrity alert
- [-] Send email when blockchain verification fails
- [-] Store email delivery status
- [-] Manual resend company notification

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
- [x] Dashboard real-time refresh/polling
- [x] Open alerts panel
- [x] Shipment performance chart
- [x] Delay rate chart
- [x] Delivery completion chart
- [x] Shipment report page
- [x] Delay report page
- [x] Alert report page
- [x] Export CSV
- [-] Export PDF

## 12. UI and UX Quality

- [x] Sidebar layout
- [x] Topbar layout
- [x] Role-based navigation
- [x] Auth screen
- [x] Normal login screen without demo credentials
- [x] Professional operations-console login screen redesign
- [x] Animated logistics map visual on login
- [x] Production login card layout
- [x] Login password visibility icon control
- [x] Login redirects users to their own dashboard first
- [x] Root path redirects authenticated users to role dashboard
- [x] Sidebar dashboard link resolves by current role
- [x] Dashboard page
- [x] Dashboard live refresh status control
- [x] Sidebar alert notification badge
- [x] Reusable toast notification feedback
- [x] Reports page
- [x] Shipment list page
- [x] Shipment details page
- [x] Create shipment form
- [x] Edit shipment form
- [x] Event timeline actor/source badges
- [x] Create shipment dropdowns keep placeholder defaults until selected
- [x] Alerts page
- [x] Simulation sessions page
- [x] Admin blockchain monitor page
- [x] Admin notifications page
- [x] Company notifications page
- [x] Integrity cases page
- [x] Admin user accounts page
- [x] Admin master data page
- [x] My account settings page
- [~] Responsive layout baseline
- [-] Loading skeletons
- [-] Empty states for every major list
- [-] Confirmation modals for destructive actions
- [x] Consistent date/time formatter utility
- [-] Reusable status badge component
- [-] Reusable page loading/error component
- [-] Full mobile QA pass

## 13. CSS and Folder Structure

- [x] `frontend/src/api`
- [x] `frontend/src/auth`
- [x] `frontend/src/config`
- [x] `frontend/src/components/blockchain`
- [x] `frontend/src/components/dashboard`
- [x] `frontend/src/components/feedback`
- [x] `frontend/src/components/forms`
- [x] `frontend/src/components/icons`
- [x] `frontend/src/components/layout`
- [x] `frontend/src/components/map`
- [x] `frontend/src/components/reports`
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
- [x] `frontend/src/styles/toast.css`
- [x] `frontend/src/styles/accounts.css`
- [x] `frontend/src/styles/reports.css`
- [x] `frontend/src/styles/simulation.css`
- [x] `frontend/src/styles/map.css`
- [x] `frontend/src/styles/blockchain.css`
- [x] `frontend/src/styles/notifications.css`
- [x] `frontend/src/styles/admin/master-data.css`
- [x] `frontend/src/utils`
- [x] `backend/src/main/java/com/fyp/autologistics/blockchain`
- [x] `backend/src/main/java/com/fyp/autologistics/config`
- [x] `backend/src/main/java/com/fyp/autologistics/controller`
- [x] `backend/src/main/java/com/fyp/autologistics/dto`
- [x] `backend/src/main/java/com/fyp/autologistics/entity`
- [x] `backend/src/main/java/com/fyp/autologistics/enums`
- [x] `backend/src/main/java/com/fyp/autologistics/event`
- [x] `backend/src/main/java/com/fyp/autologistics/exception`
- [x] `backend/src/main/java/com/fyp/autologistics/repository`
- [x] `backend/src/main/java/com/fyp/autologistics/service`
- [x] `backend/src/main/java/com/fyp/autologistics/specification`
- [x] `backend/src/main/java/com/fyp/autologistics/util`
- [x] `blockchain/contracts`
- [x] `blockchain/scripts`
- [x] `blockchain/test`
- [x] `database/migrations`
- [x] `database/testing`

## 14. Testing and Validation

- [x] Backend application context test
- [x] Frontend production build
- [x] Targeted lint for redesigned login page
- [x] Targeted lint for dashboard live refresh
- [x] Targeted lint for touched auth/blockchain/shipment/alert files
- [x] Full frontend lint
- [x] Alert service unit tests for scoped alert access/actions
- [x] Blockchain service unit tests for retry guards, stored-hash preservation,
  successful recovery, and health mapping
- [x] Solidity contract compile
- [x] Solidity contract tests
- [x] Blockchain production dependency audit
- [x] System-managed simulation test-data SQL script
- [x] Admin master-data test-data SQL script
- [x] Edit shipment details test-data SQL script
- [x] Shipment event actor/source test-data SQL script
- [x] Backend service unit tests for shipment transitions, alert scope/actions,
  stock evaluation, order-update events, and blockchain retry/health
- [x] Backend controller integration tests for stock authorization and validation
- [x] Frontend component tests for stock and alert role flows
- [x] End-to-end authenticated API happy path test
- [x] End-to-end blockchain failure and retry path test
- [x] Browser smoke test for blockchain monitor health, pagination, role controls,
  and disabled-ledger retry behavior
- [x] Focused low-stock, recovery, role-scope, duplicate-rule, and order-update
  service tests
- [x] Production Nginx health and unauthenticated protected-route smoke test
- [x] Test-only H2 datasource isolates automated tests from Supabase
- [x] Test contexts use unique in-memory databases
- [x] Frontend route-level code splitting verified in production build
- [x] Read-only production health load baseline: 50/50 successful, 217.57 ms P95
- [x] User acceptance test checklist created at `testing/UAT_CHECKLIST.md`
- [x] Automated test report created at `testing/TEST_REPORT_2026-08-18.md`
- [x] Final approved-scope re-audit found no remaining implementation gaps
- [ ] Manual user acceptance test sign-off with current Supabase accounts

## 15. Deployment and Cloud Database

- [x] Supabase PostgreSQL migration folder
- [x] Supabase schema supports shipment form options and unique shipment codes
- [x] Supabase migration query for database-backed shipment master data
- [x] Supabase migration query for shipment dropdown master data refresh
- [x] Supabase migration query for user account role/profile management
- [x] Supabase migration query for normal login readiness
- [x] Supabase migration query for system-managed simulation profiles/sessions
- [x] Supabase migration query for shipment detail edit audit event
- [x] Supabase migration query for shipment event actor/source metadata
- [x] Supabase SQL template for account password reset
- [x] Root `.env` configured for Supabase Session Pooler
- [x] Backend datasource loads Supabase settings from environment/config import
- [x] Local PostgreSQL datasource fallback removed from backend config
- [x] Supabase connection smoke test from Spring Boot runtime
- [x] Alert badge/toast UX confirmed to require no new Supabase SQL
- [x] Supabase migration query for blockchain retry audit fields
- [x] Supabase migration query for stock rules and low-stock/order-update alerts
- [ ] Deployment environment variables configured on hosting platform
- [x] Backend Java 21 multi-stage production Docker image
- [x] Frontend Vite-to-Nginx multi-stage production Docker image
- [x] Nginx SPA fallback and same-origin `/api` reverse proxy
- [x] Spring Boot/Nginx health endpoint
- [x] Production Docker Compose with external Supabase configuration
- [x] Centralized environment-configurable CORS policy
- [x] Production Hibernate schema validation mode
- [x] Nginx or reverse-proxy deployment alignment
- [x] Production container images built successfully
- [x] Frontend production dependency audit reports zero vulnerabilities

Resolved lint debt:

- [x] `frontend/src/pages/DashboardPage.jsx`
- [x] `frontend/src/pages/admin/AdminNotificationsPage.jsx`
- [x] `frontend/src/pages/admin/CompanyNotificationsPage.jsx`
- [x] `frontend/src/pages/admin/IntegrityCasesPage.jsx`

## 16. Scope Decisions From IR Gap Review

Decision date: 2026-08-13.

Selected for implementation:

- [x] Admin master-data configuration UI
- [x] Edit shipment details
- [x] Audit trail actor/user and event source type
- [x] Dashboard real-time refresh/polling
- [x] Alert notification badge and toast feedback
- [x] Manufacturer access to full alert page
- [x] Blockchain failed-record retry flow
- [x] Blockchain admin contract and network health information
- [x] Deployment/Nginx alignment
- [x] Low-stock alert
- [x] Order update alert

Not planned for the current implementation scope:

- [-] Shipment document file upload/download storage
- [-] Export event timeline
- [-] Route deviation visual warning
- [-] Email or external notification delivery
- [-] Admin reset user password flow
- [-] Report PDF export
- [-] Detailed vehicle condition sensor fields
- [-] Full responsive/mobile polish bundle
- [-] OpenAPI/Swagger documentation
- [-] QR code scanning for shipment or vehicle lookup
- [-] Teams/email communication integration
- [-] Real-road route planning

Later phase:

- [~] Automated testing and evaluation completed; manual Supabase UAT sign-off
  remains.
- [ ] Final report technology-stack update from Vue.js to React + Vite.

## 17. Recommended Next Development Order

1. [x] Add admin master-data configuration UI.
2. [x] Add edit shipment details.
3. [x] Add audit trail actor/user and event source type.
4. [x] Add dashboard real-time refresh/polling.
5. [x] Add alert badge/toast UX and manufacturer alert page access.
6. [x] Add blockchain failed-record retry flow.
7. [x] Add blockchain admin contract and network health information.
8. [x] Add low-stock alert and order update alert.
9. [x] Add deployment/Nginx alignment.
10. [~] Automated testing/evaluation complete; run and sign the manual UAT.

## 18. Stock Alerts and Production Deployment Update

Completion date: 2026-08-18.

- [x] Added `database/migrations/2026-08-18-stock-order-alerts.sql`
- [x] Migration contains no seed, dropdown, or test business data
- [x] Added `GET /api/stock-monitoring`
- [x] Added admin-only `POST /api/stock-monitoring/rules`
- [x] Added admin-only `PUT /api/stock-monitoring/rules/{ruleId}`
- [x] Added `LOW_STOCK` and `ORDER_UPDATE` alert types
- [x] Added shipment-or-stock-rule alert reference integrity constraint
- [x] Added configurable 30-second stock scheduler
- [x] Added stock monitoring sidebar route and responsive page styles
- [x] Added backend and frontend production Dockerfiles
- [x] Added `deployment/compose.production.yml`
- [x] Added `frontend/nginx/default.conf`
- [x] Added `deployment/.env.production.example`
- [x] Backend tests: 30 passed, 0 failed
- [x] Frontend ESLint: passed
- [x] Frontend production build: passed
- [x] npm production audit: 0 vulnerabilities after non-breaking updates
- [x] Docker Compose configuration validation: passed
- [x] Backend and frontend production container builds: passed
- [x] Nginx `/healthz` to Spring Boot health probe: `UP`
- [x] Production login page rendered through Nginx
- [x] Unauthenticated `/stock-monitoring` correctly redirected to `/login`
- [ ] Authenticated browser flow for stock rule create/edit after the Supabase
  migration is run with a known current admin password

## 19. Full Automated Testing And Evaluation Update

Completion date: 2026-08-18.

- [x] Added test-scope H2 dependency and isolated datasource configuration
- [x] Confirmed automated tests do not connect to Supabase
- [x] Added five stock-monitoring controller security/validation tests
- [x] Added five shipment status transition and event publication tests
- [x] Added two order-update listener tests
- [x] Added authenticated JWT shipment lifecycle integration test
- [x] Added blockchain-disabled failure notification and retry E2E assertions
- [x] Backend tests: 30 passed, 0 failed
- [x] Added Vitest, Testing Library, jest-dom, user-event, and jsdom
- [x] Frontend component tests: 7 passed, 0 failed
- [x] Full frontend ESLint and production build passed
- [x] Frontend and blockchain production audits: 0 vulnerabilities
- [x] Solidity tests: 2 passed, 0 failed
- [x] Route-level code splitting reduced initial JS from 570.18 kB to 291.28 kB
- [x] Latest production frontend image built and reports healthy
- [x] Browser confirmed login rendering and protected-route redirect
- [x] Health load baseline: 50 successful, 0 failed, 129.97 ms average,
  217.57 ms P95, 323.97 ms maximum
- [x] Added `testing/UAT_CHECKLIST.md`
- [x] Added `testing/TEST_REPORT_2026-08-18.md`
- [ ] Execute and sign manual UAT using current Supabase accounts

## 20. Architecture Documentation

Completion date: 2026-08-19.

- [x] Added a code-derived UML role and service class diagram
- [x] Documented Administrator, Manufacturer, Logistics Provider, and
  Dealership permissions
- [x] Added a complete core domain entity and relationship diagram
- [x] Distinguished JPA associations from logical key/ID associations
- [x] Added an authorization matrix aligned with `SecurityConfig` and service
  scoping
- [x] Linked the class diagram documentation from the main README
- [x] Added a conventional, high-resolution Administrator module class diagram
  with React boundaries, REST controllers, services, entities, and labelled
  relationships
- [x] Added a native editable `.drawio` version of the Administrator class
  diagram with validated mxGraph structure
- [x] Added a code-derived Manufacturer module class diagram covering 43 role,
  boundary, controller, service, and entity classes with 80 labelled
  relationships
- [x] Added report-ready HTML and high-resolution PNG versions of the
  Manufacturer class diagram
- [x] Added a native editable `.drawio` version of the Manufacturer class
  diagram with validated mxGraph structure
- [x] Added a code-derived Logistics Provider module class diagram covering 37
  role, boundary, controller, service, event-listener, and entity classes with
  66 labelled relationships
- [x] Added report-ready HTML and high-resolution PNG versions of the Logistics
  Provider class diagram
- [x] Added a native editable `.drawio` version of the Logistics Provider class
  diagram with validated mxGraph structure
- [x] Added a code-derived Dealership module class diagram covering 40 role,
  boundary, controller, service, event-listener, and entity classes with 73
  labelled relationships
- [x] Added report-ready HTML and high-resolution PNG versions of the Dealership
  class diagram
- [x] Added a native editable `.drawio` version of the Dealership class diagram
  with validated mxGraph structure

## 21. Clone And Runtime Onboarding

Completion date: 2026-08-20.

- [x] Added `docs/SETUP_AND_RUN_GUIDE.md` as the authoritative fresh-clone guide
- [x] Documented Java 21, Node.js 22, Git, Docker, and Supabase prerequisites
- [x] Documented reproducible frontend and optional blockchain installation
  with `npm ci`
- [x] Documented local Spring Boot and Vite startup, URLs, and health checks
- [x] Documented optional Hardhat node, contract deployment, and backend setup
- [x] Documented production-like Docker/Nginx build, startup, logs, and shutdown
- [x] Added `frontend/.env.example` and explained Vite environment-file scope
- [x] Replaced project-specific Supabase identifiers in committed examples
- [x] Disabled blockchain by default until a real node and contract are ready
- [x] Removed the fixed Docker JWT secret and made `JWT_SECRET` mandatory
- [x] Added optional first-administrator bootstrap variables to Docker Compose
- [x] Removed stale README references to missing setup migrations and test data
- [x] Added smoke-test, automated-test, and common-error troubleshooting steps
- [x] Docker Compose configuration validation passed
- [x] Backend tests passed: 30 tests, 0 failures
- [x] Frontend checks passed: ESLint, 7 component tests, and production build
- [x] Hardhat Solidity tests passed: 2 tests, 0 failures

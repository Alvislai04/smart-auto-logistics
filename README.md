# Smart Automotive Logistics System

Smart Automotive Logistics System is a full-stack Final Year Project system for
vehicle shipment monitoring across manufacturers, logistics providers,
dealerships, and administrators. The system is based on the IR requirement for
IoT-assisted vehicle tracking, centralized logistics visibility, role-based
access, alerts, audit trails, and blockchain-backed shipment event integrity.

## Current Status

The project currently includes:

- Spring Boot backend with PostgreSQL persistence
- React + Vite frontend with role-protected routes
- Normal email/password login with JWT-protected API access
- Professional two-panel login interface with logistics route visual and
  eye-icon password visibility control
- Role-specific dashboard landing after login
- Authenticated dashboard summaries scoped to the user's operational role
- Optimized dashboard overview loading through one backend request with grouped
  database counts
- Live dashboard refresh with configurable polling, visibility-aware updates,
  last-updated status, and non-blocking refresh failure feedback
- Role-scoped shipment, delay, and alert reports
- CSV export for operational reports
- Role-scoped full Alerts page for admin, manufacturer, logistics provider, and
  dealership users
- Sidebar open-alert notification badge backed by a scoped alert count API
- Reusable toast notifications after important alert, shipment, and account
  actions
- Admin-only user account registration and account maintenance
- Admin master-data maintenance for vehicle models, shipment party profiles, and
  simulation scenario profiles
- Database-backed user role options for account registration
- Self-service account profile updates and password changes
- Non-admin company profile data synced into shipment party profiles by
  `party_profile_key`
- Admin-managed database records for shipment dropdowns and simulation behavior
- Supabase PostgreSQL Session Pooler configuration
- Manufacturer shipment creation, listing, details, deletion, and workflow
  status updates
- Shipment creation route/API restricted to the manufacturer role
- Shipment detail editing for admin and linked manufacturer accounts before
  pickup or in-transit tracking begins
- Auto-generated unique shipment codes controlled by the backend
- Database-backed Mercedes-Benz vehicle model dropdown for shipment creation
- Database-backed manufacturer origin and dealership destination dropdowns using
  stable `model_code` and `party_key` values
- API-backed shipment stage and work-in-progress option lists aligned with
  backend workflow rules
- API-backed shipment search, status/stage filtering, and pagination
- Shipment operation data for ETD, ETA, logistics stage, stock/WIP state,
  responsible person, customer receiver details, and document reference
- Planned route coordinates for route deviation and progress monitoring
- Delivery receipt confirmation with audit/blockchain event recording
- System-managed IoT tracking updates with GPS, speed, and condition status
- Database-backed random telemetry profiles for normal, fast, delayed, idle,
  route deviation, and sensor-warning movement
- Automatic simulation session provisioning when shipments are created, with a
  monitoring-only Simulation page for admin/logistics users
- Leaflet map visualization with route history and planned route markers
- Advanced alert rules for delay, idle, ETA overdue, missing tracking updates,
  route deviation, sensor warning, and progress shortfall
- Automatic low-stock monitoring based on database-managed company/model/stage
  rules and live non-cancelled shipment quantities
- Automatic order-update alerts for manual, alert-engine, and IoT-simulation
  shipment status changes
- Role-scoped Stock Monitoring page for administrators, manufacturers, and
  dealerships, with rule management restricted to administrators
- Shipment event timeline for audit trail
- Shipment edit events recorded in the audit timeline and blockchain ledger
- Audit timeline actor/source metadata for user actions, IoT simulation events,
  alert-engine events, system automation, and legacy records
- Real local blockchain ledger integration using Hardhat, Solidity, and Web3j
- Admin blockchain monitor with live node/contract/signer health, configured
  contract address, record filtering, and pagination
- Guarded admin retry for failed blockchain writes with persistent attempt
  history and original stored-hash preservation
- Admin/company notification records for blockchain integrity issues
- Admin Master Data page at `/admin/master-data`
- Production Docker images for Spring Boot and React, with Nginx SPA routing,
  same-origin `/api` reverse proxying, and a health endpoint

The project progress checklist is maintained in
[PROJECT_PROGRESS.md](PROJECT_PROGRESS.md).

The code-derived UML documentation for Administrator, Manufacturer, Logistics
Provider, and Dealership roles is maintained in
[docs/ROLE_CLASS_DIAGRAM.md](docs/ROLE_CLASS_DIAGRAM.md).

The detailed, report-ready Administrator class diagram is available as
[documentation](docs/diagrams/ADMINISTRATOR_CLASS_DIAGRAM.md) and a
[high-resolution PNG](docs/diagrams/ADMINISTRATOR_CLASS_DIAGRAM.png). The native
editable version is available as a
[draw.io document](docs/diagrams/ADMINISTRATOR_CLASS_DIAGRAM.drawio).

The corresponding Manufacturer module diagram is available as
[documentation](docs/diagrams/MANUFACTURER_CLASS_DIAGRAM.md), a
[high-resolution PNG](docs/diagrams/MANUFACTURER_CLASS_DIAGRAM.png), and a
native editable
[draw.io document](docs/diagrams/MANUFACTURER_CLASS_DIAGRAM.drawio).

The Logistics Provider module diagram is available as
[documentation](docs/diagrams/LOGISTICS_PROVIDER_CLASS_DIAGRAM.md), a
[high-resolution PNG](docs/diagrams/LOGISTICS_PROVIDER_CLASS_DIAGRAM.png), and a
native editable
[draw.io document](docs/diagrams/LOGISTICS_PROVIDER_CLASS_DIAGRAM.drawio).

The Dealership module diagram is available as
[documentation](docs/diagrams/DEALERSHIP_CLASS_DIAGRAM.md), a
[high-resolution PNG](docs/diagrams/DEALERSHIP_CLASS_DIAGRAM.png), and a native
editable [draw.io document](docs/diagrams/DEALERSHIP_CLASS_DIAGRAM.drawio).

## Project Structure

```text
smart-auto-logistics/
+-- backend/
|   +-- Dockerfile
|   +-- src/main/java/com/fyp/autologistics/
|   |   +-- blockchain/
|   |   +-- config/
|   |   +-- controller/
|   |   +-- dto/
|   |   +-- entity/
|   |   +-- enums/
|   |   +-- exception/
|   |   +-- repository/
|   |   +-- service/
|   |   +-- specification/
|   |   +-- util/
|   +-- src/main/resources/application.properties
+-- blockchain/
|   +-- contracts/
|   +-- scripts/
|   +-- test/
|   +-- hardhat.config.js
+-- database/
|   +-- migrations/
|   +-- testing/
+-- frontend/
|   +-- Dockerfile
|   +-- nginx/default.conf
|   +-- src/
|       +-- api/
|       +-- auth/
|       +-- components/
|       |   +-- feedback/
|       +-- pages/
|       +-- styles/
|       +-- utils/
+-- deployment/
|   +-- compose.production.yml
|   +-- .env.production.example
+-- docs/
|   +-- ROLE_CLASS_DIAGRAM.md
|   +-- diagrams/
|       +-- ADMINISTRATOR_CLASS_DIAGRAM.drawio
|       +-- ADMINISTRATOR_CLASS_DIAGRAM.html
|       +-- ADMINISTRATOR_CLASS_DIAGRAM.md
|       +-- ADMINISTRATOR_CLASS_DIAGRAM.png
|       +-- MANUFACTURER_CLASS_DIAGRAM.drawio
|       +-- MANUFACTURER_CLASS_DIAGRAM.html
|       +-- MANUFACTURER_CLASS_DIAGRAM.md
|       +-- MANUFACTURER_CLASS_DIAGRAM.png
|       +-- LOGISTICS_PROVIDER_CLASS_DIAGRAM.drawio
|       +-- LOGISTICS_PROVIDER_CLASS_DIAGRAM.html
|       +-- LOGISTICS_PROVIDER_CLASS_DIAGRAM.md
|       +-- LOGISTICS_PROVIDER_CLASS_DIAGRAM.png
|       +-- DEALERSHIP_CLASS_DIAGRAM.drawio
|       +-- DEALERSHIP_CLASS_DIAGRAM.html
|       +-- DEALERSHIP_CLASS_DIAGRAM.md
|       +-- DEALERSHIP_CLASS_DIAGRAM.png
+-- README.md
+-- PROJECT_PROGRESS.md
```

## Technology Stack

| Layer | Technology |
| --- | --- |
| Frontend | React, Vite, React Router, Axios, Leaflet |
| Backend | Java 21, Spring Boot, Spring Security, Spring Data JPA |
| Database | Supabase PostgreSQL |
| Blockchain | Solidity, Hardhat, Web3j |
| Authentication | JWT, BCrypt |
| Build Tools | Maven, npm |

## Environment Configuration

Copy `.env.example` values into your local shell or local `.env` workflow. Do
not commit real secrets.

Required backend variables:

```powershell
$env:DB_URL="jdbc:postgresql://aws-0-ap-northeast-1.pooler.supabase.com:5432/postgres?sslmode=require"
$env:DB_USERNAME="postgres.xgvsmuqdyhzommukmaic"
$env:DB_PASSWORD="your-supabase-database-password"
$env:JWT_SECRET="replace-with-a-long-random-secret"
$env:JWT_EXPIRATION_MS="86400000"
$env:SIMULATION_SCHEDULER_ENABLED="true"
$env:SIMULATION_SCHEDULER_DELAY_MS="5000"
$env:SIMULATION_DEFAULT_INTERVAL_SECONDS="15"
$env:SIMULATION_DEFAULT_MAX_EVENTS="500"
```

Required blockchain variables after deploying the local contract:

```powershell
$env:BLOCKCHAIN_ENABLED="true"
$env:BLOCKCHAIN_RPC_URL="http://127.0.0.1:8545"
$env:BLOCKCHAIN_CONTRACT_ADDRESS="paste-contract-address-from-deployment.local.json"
$env:BLOCKCHAIN_PRIVATE_KEY="paste-local-hardhat-private-key"
```

Frontend API variable:

```powershell
$env:VITE_API_BASE_URL="http://localhost:8080/api"
$env:VITE_DASHBOARD_REFRESH_MS="15000"
$env:VITE_ALERT_BADGE_REFRESH_MS="15000"
```

## Supabase PostgreSQL Setup

Supabase changes are stored as incremental SQL files in `database/migrations/`.
Run the needed migration in Supabase SQL Editor after the base tables exist.

Recommended setup:

1. Create a Supabase project.
2. Open `SQL Editor`.
3. Run your existing/base application schema if the database is empty.
4. Run the latest migration files from `database/migrations/`.
5. Open the Supabase `Connect` panel and copy either the Direct Connection or
   Session Pooler connection details.
6. Start or restart the backend with Supabase database environment variables.

For the shipment dropdown master-data change, run:

```text
database/migrations/2026-08-08-shipment-dropdown-master-data.sql
```

This creates and fills:

- `vehicle_models`, keyed by `model_code`
- `shipment_party_profiles`, keyed by `party_key`

The application reads create-shipment dropdown data from those tables. Do not
hardcode vehicle model names, manufacturer origins, or dealership destinations in
JavaScript or Java.

For the user account management change, run:

```text
database/migrations/2026-08-08-user-account-management.sql
```

This creates and fills `user_role_options`, adds `party_profile_key` to
`app_users`, and allows manufacturer, logistics provider, and dealership account
profiles to sync into `shipment_party_profiles`.

For the normal login process readiness change, run:

```text
database/migrations/2026-08-08-login-production-readiness.sql
```

Before running the first-admin block inside that file, replace the
`REPLACE_WITH_*` values with your own admin name, email, and strong password. If
your Supabase database already has an active admin account, the first-admin block
can be skipped.

For the automatic system-managed simulation change, run:

```text
database/migrations/2026-08-12-system-managed-simulation.sql
```

This creates and fills:

- `simulation_profiles`, keyed by `profile_key`
- `simulation_sessions`, one active system-owned session per eligible shipment

The backend reads random behavior from `simulation_profiles`. Users cannot start
or stop simulations from the UI; the scheduler provisions and advances sessions
automatically.

For the shipment detail editing audit event change, run:

```text
database/migrations/2026-08-13-edit-shipment-details.sql
```

This creates a unique index for `shipments.shipment_code` and updates the
`shipment_events` event type constraint so edited shipment details can be saved
as `SHIPMENT_UPDATED` audit events. Run this before testing the edit form in
Supabase.

For the shipment event actor/source audit trail change, run:

```text
database/migrations/2026-08-13-shipment-event-actor-source.sql
```

This adds actor snapshot fields, event source type, and event payload version to
`shipment_events`. Existing rows are backfilled as `LEGACY_RECORD` with payload
version `1`, so old blockchain hashes can still be verified with their original
payload format while new events use version `2`.

The dashboard real-time refresh change does not require a Supabase SQL query. It
uses the existing optimized `/api/dashboard/overview` endpoint and the frontend
`VITE_DASHBOARD_REFRESH_MS` setting.

The alert badge, toast feedback, and manufacturer alert-page access change does
not require a Supabase SQL query. It uses the existing `alerts`, `shipments`,
and account `party_profile_key` data, plus the frontend
`VITE_ALERT_BADGE_REFRESH_MS` setting.

For blockchain retry attempt tracking, run:

```text
database/migrations/2026-08-13-blockchain-retry-audit.sql
```

This adds `retry_count` and `last_retry_at` to `blockchain_records`, backfills
existing records safely, and enforces a non-negative retry counter. No business
or dropdown values are hardcoded by this migration.

For automatic low-stock and order-update alerts, run:

```text
database/migrations/2026-08-18-stock-order-alerts.sql
```

Run this migration before using Stock Monitoring or changing a shipment status.
It creates `stock_alert_rules`, adds the `LOW_STOCK` and `ORDER_UPDATE` alert
types, and allows every alert to reference exactly one shipment or stock rule.
It does not insert vehicle models, companies, thresholds, or test data.

Administrators configure stock rules at `/stock-monitoring` using active company
and vehicle-model records from Supabase. A rule is unique by `party_key`,
`vehicle_model_code`, and `logistics_stage`. The scheduler compares the minimum
quantity with non-cancelled shipment rows every 30 seconds by default. An open
low-stock alert is updated while the shortage remains and resolves automatically
after recovery or rule deactivation.

Direct connection JDBC example:

```powershell
$env:DB_URL="jdbc:postgresql://db.YOUR_PROJECT_REF.supabase.co:5432/postgres?sslmode=require"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your-supabase-database-password"
```

Session Pooler JDBC example for IPv4-only networks:

```powershell
$env:DB_URL="jdbc:postgresql://aws-0-YOUR_REGION.pooler.supabase.com:5432/postgres?sslmode=require"
$env:DB_USERNAME="postgres.YOUR_PROJECT_REF"
$env:DB_PASSWORD="your-supabase-database-password"
```

Keep the React frontend connected to the Spring Boot backend through
`VITE_API_BASE_URL`. Do not put the Supabase database password in frontend code.

## Production Deployment

The production stack uses two containers:

- `backend`: non-root Java 21 runtime with Spring Boot and Supabase PostgreSQL
- `frontend`: Nginx serving the Vite build and proxying `/api` to the backend

Before deployment, run all required Supabase migrations, including
`2026-08-18-stock-order-alerts.sql`. Fill the environment values represented by
`deployment/.env.production.example`, then start the stack from the project root:

```powershell
docker compose --env-file deployment/.env -f deployment/compose.production.yml up -d --build
```

The default local production URL is `http://localhost:8088`. The reverse-proxy
health endpoint is `http://localhost:8088/healthz`. Production Compose forces
Hibernate schema mode to `validate`, so missing migrations fail startup rather
than silently modifying the production schema.

Nginx serves React routes through `index.html`, caches fingerprinted assets, adds
basic browser security headers, and keeps API requests same-origin. For a
separate frontend origin, set `APP_CORS_ALLOWED_ORIGINS` to a comma-separated
list of approved origins; controller-level localhost CORS rules have been
removed in favor of this centralized configuration.

## Local Setup

### 1. Database

Create or update the tables in Supabase by running the needed SQL files from
`database/migrations/` in the Supabase SQL Editor.

The backend reads database settings from the root `.env` file when it is run
from the `backend` folder. Keep your real Supabase password only in `.env`.

### 2. Blockchain

Install dependencies:

```powershell
cd C:\Users\alvis\Desktop\smart-auto-logistics\blockchain
npm install
```

Start the local blockchain node:

```powershell
npm run node
```

In a second terminal, deploy the ledger contract:

```powershell
cd C:\Users\alvis\Desktop\smart-auto-logistics\blockchain
npm run deploy:local
```

Copy `contractAddress` from `blockchain/deployment.local.json` into
`BLOCKCHAIN_CONTRACT_ADDRESS`.

### 3. Backend

Run the Spring Boot backend:

```powershell
cd C:\Users\alvis\Desktop\smart-auto-logistics\backend
.\mvnw.cmd spring-boot:run
```

Backend URL:

```text
http://localhost:8080
```

### 4. Frontend

Run the React frontend:

```powershell
cd C:\Users\alvis\Desktop\smart-auto-logistics\frontend
npm install
npm run dev
```

Frontend URL:

```text
http://localhost:5173
```

## Login Accounts

Users sign in with real accounts stored in the `app_users` table. The redesigned
login page does not contain demo credentials, role shortcuts, or hardcoded
account data.

For a new database, create the first admin account by either:

- Running `database/migrations/2026-08-08-login-production-readiness.sql` after
  replacing the first-admin values
- Setting `APP_BOOTSTRAP_ADMIN_ENABLED=true` with
  `APP_BOOTSTRAP_ADMIN_FULL_NAME`, `APP_BOOTSTRAP_ADMIN_EMAIL`, and
  `APP_BOOTSTRAP_ADMIN_PASSWORD`

After the first admin can sign in, create manufacturer, logistics provider, and
dealership accounts from `Admin > User Accounts`.

Passwords are stored as BCrypt hashes, so the system cannot show the original
plaintext password later. For accounts created from `Admin > User Accounts`, the
password is the temporary password the admin typed during registration. If your
Supabase database still contains old seeded accounts from earlier development
and you never changed them, those old defaults were:

| Email | Old default password |
| --- | --- |
| `admin@autologistics.com` | `Admin@12345` |
| `manufacturer@autologistics.com` | `Manufacturer@12345` |
| `logistics@autologistics.com` | `Logistics@12345` |
| `dealer@autologistics.com` | `Dealer@12345` |

To set a new password for any account, run:

```text
database/migrations/2026-08-08-account-password-reset-template.sql
```

Replace `REPLACE_WITH_ACCOUNT_EMAIL` and
`REPLACE_WITH_NEW_STRONG_PASSWORD` before running it in Supabase.

## Simulation Test Data

To test the automatic simulation feature with real rows, run:

```text
database/testing/system-managed-simulation-test-data.sql
```

This inserts three Mercedes-Benz shipment records with real Malaysia route
coordinates and active system-owned sessions. After the backend scheduler runs,
the `/simulation` page and shipment details should show changing tracking
events, speed, condition, alerts, and route positions.

## Admin Master Data Test Data

To test the admin Master Data page with real dropdown and simulation profile
records, run:

```text
database/testing/admin-master-data-test-data.sql
```

This upserts role options, Mercedes-Benz vehicle models, manufacturer,
logistics provider, dealership party profiles, and weighted simulation
profiles. After running it, sign in as an admin and open `/admin/master-data`.

## Edit Shipment Test Data

To test the edit shipment details feature with real rows, run:

```text
database/testing/edit-shipment-details-test-data.sql
```

This upserts one editable `CREATED` shipment, one Mercedes-Benz vehicle model,
one manufacturer profile, one dealership profile, and a paused system simulation
session so the shipment remains safe for edit testing. The file also includes a
commented optional update block to link one manufacturer account to the test
manufacturer profile.

## Audit Trail Actor/Source Test Data

To test actor and source labels in the shipment event timeline, run:

```text
database/testing/shipment-event-actor-source-test-data.sql
```

This inserts one `USER_ACTION`, one `IOT_SIMULATION`, and one `ALERT_ENGINE`
event against shipment `EDIT-SHP-READY-001`. Open that shipment details page and
check the event timeline badges and actor/source labels.

## Troubleshooting

### No Active Sessions on `/simulation`

Run `database/migrations/2026-08-12-system-managed-simulation.sql` in Supabase,
restart the backend, and wait one scheduler cycle. Eligible shipments must have
status `CREATED`, `READY_FOR_DISPATCH`, `PICKED_UP`, `IN_TRANSIT`, or `DELAYED`
and complete origin/destination coordinates. To create ready-to-watch test
rows, run `database/testing/system-managed-simulation-test-data.sql`.

### Expired JWT in Browser Storage

If the backend log shows `ExpiredJwtException`, the Spring Boot application is
running correctly, but the browser is still sending an old JWT from local
storage. The backend now returns a clean `401 Unauthorized` response for expired
or invalid tokens, and the frontend clears the stored session before redirecting
to the login page.

Fix:

```text
Open http://localhost:5173/login and sign in again.
```

If the browser is still stuck on an old session, use the app logout button or
clear local storage for `localhost:5173`, then sign in again with an active
account created by the admin.

## Main Workflows

### Shipment Workflow

```text
Manufacturer creates shipment with generated shipment code
-> Select Mercedes-Benz model
-> Select registered manufacturer origin
-> Select registered dealership destination
-> Mark ready for dispatch
-> Mark picked up
-> Mark in transit
-> Backend system generates tracking updates
-> Mark arrived
-> Confirm delivery receipt
-> Shipment becomes delivered
```

### Continuous IoT Simulation Workflow

```text
Manufacturer creates shipment with route coordinates
-> Backend creates a system-owned simulation session
-> Scheduler chooses random database-backed telemetry profile
-> Scheduler generates GPS, speed, and condition tracking events
-> Map, tracking history, alerts, and simulation monitor update
-> Session completes when destination or max event limit is reached
```

### Account Management Workflow

```text
Admin opens User Accounts
-> Select role from database-backed role options
-> Register user with company and location details
-> Non-admin profile syncs to shipment party profile records
-> User opens My Account
-> User updates own profile or changes password
```

### Blockchain Integrity Workflow

```text
Important shipment event occurs
-> Backend hashes event payload with SHA-256
-> Backend writes hash to ShipmentLedger smart contract
-> Database stores transaction hash and block number
-> Admin monitor reports live RPC, contract, signer, chain, and block health
-> Failed writes without an on-chain transaction can be retried by an admin
-> Retry writes the original stored event hash and records attempt history
-> Admin can verify database record against on-chain hash
-> Failed verification creates admin/company integrity notifications
```

If the blockchain node or contract is not configured, the backend does not
generate fake transaction data. The blockchain record is saved as `FAILED` with
a clear message for admin review. Retry is enabled only when the health endpoint
reports the node, deployed contract, and signer as ready. Records that already
have an on-chain transaction are never eligible for another write.

## Useful Commands

Backend tests:

```powershell
cd backend
.\mvnw.cmd test
```

Frontend build:

```powershell
cd frontend
npm run build
```

Frontend lint:

```powershell
cd frontend
npm run lint
```

Frontend component tests:

```powershell
cd frontend
npm test
```

Backend tests use a uniquely named in-memory H2 database configured in
`backend/src/test/resources/application.properties`. Running `mvn test` does not
connect to or change Supabase.

Smart contract compile and test:

```powershell
cd blockchain
npm run compile
npm test
```

Production dependency audit for blockchain workspace:

```powershell
cd blockchain
npm audit --omit=dev
```

Read-only production health baseline:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File `
  .\testing\performance\health-load.ps1 `
  -BaseUrl http://127.0.0.1:8089 `
  -RequestCount 50
```

Manual acceptance testing is tracked in
[`testing/UAT_CHECKLIST.md`](testing/UAT_CHECKLIST.md). The dated automated
evidence is in
[`testing/TEST_REPORT_2026-08-18.md`](testing/TEST_REPORT_2026-08-18.md).

## Recent Implementation Update

The latest completed step added formal automated testing, production smoke
verification, and initial-load optimization on top of the completed stock,
order-notification, and deployment work:

- Added `stock_alert_rules` with generated unique rule keys and database-backed
  company, vehicle model, stage, minimum quantity, and active state
- Added role-scoped `GET /api/stock-monitoring` and admin-only rule create/update
  APIs
- Added scheduled and shipment-event-triggered stock evaluation, automatic
  shortage alert creation/update, and automatic recovery resolution
- Added a responsive Stock Monitoring page with administrator rule controls and
  read-only manufacturer/dealership views
- Added order-update alerts for user, IoT-simulation, and alert-engine status
  transitions while preserving prior notifications as resolved history
- Extended alert references and role scopes so stock alerts never masquerade as
  shipment alerts
- Added the required Supabase migration without seed or test rows
- Added backend/frontend Dockerfiles, production Compose, centralized CORS,
  Spring health probes, and Nginx reverse proxy configuration
- Added isolated H2 test configuration so automated tests never touch Supabase
- Added controller security/validation, shipment lifecycle, order-update, and
  blockchain failure/retry integration coverage
- Added React component coverage for stock rules and role-scoped alert actions
- Added route-level lazy loading, reducing initial JavaScript from 570.18 kB to
  291.28 kB and removing the production build warning
- Removed an unused legacy fixed-URL API client
- Verified 30 backend tests, 7 frontend component tests, full lint/build, 2
  Solidity tests, and zero frontend/blockchain production audit findings
- Built and ran the latest Nginx image, verified backend health and browser
  redirects, and recorded a 50-request health baseline with 217.57 ms P95
- Added a reusable UAT checklist and dated automated test report
- Re-audited the approved IR gap-review scope and found no remaining code
  implementation gaps

## Known Remaining Work

See [PROJECT_PROGRESS.md](PROJECT_PROGRESS.md) for the full checklist. All
functions selected in the IR gap-review scope are implemented and the automated
test suite is green. Remaining sign-off work is manual UAT with current Supabase
accounts, hosting-platform secrets/domain configuration when a host is chosen,
and the planned final-report technology update from Vue.js to React + Vite.

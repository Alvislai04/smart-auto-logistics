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
- Manufacturer shipment creation, listing, details, pre-pickup editing,
  pre-transit milestones, and explicit transit start
- Shipment creation route/API restricted to the manufacturer role
- Shipment detail editing for admin and linked manufacturer accounts before
  pickup or in-transit tracking begins
- Auto-generated unique shipment codes controlled by the backend
- Backend-assigned immutable shipment start time based on the configured
  application timezone; users cannot select a past or future start date
- Database-backed Mercedes-Benz vehicle model dropdown for shipment creation
- Database-backed manufacturer origin and dealership destination dropdowns using
  stable `model_code` and `party_key` values
- API-backed shipment stage and work-in-progress option lists aligned with
  backend workflow rules
- API-backed shipment search, status/stage filtering, and pagination
- Shipment operation data for ETD, ETA, logistics stage, stock/WIP state,
  responsible person, customer receiver details, and document reference
- Planned route coordinates for route deviation and progress monitoring
- Delivery receipt confirmation restricted to the assigned dealership, with
  authenticated actor attribution and audit/blockchain event recording
- System-managed IoT tracking updates with GPS, speed, and condition status
- Database-backed random telemetry profiles for normal, fast, delayed, idle,
  route deviation, and sensor-warning movement
- Automatic simulation session provisioning when the assigned manufacturer
  starts transit, with a
  monitoring-only Simulation page for admin/logistics users
- Immediate simulation shutdown after arrival, delivery, or cancellation so
  terminal shipment statuses cannot be replaced by later telemetry evaluation
- Leaflet map visualization with route history and planned route markers
- Advanced alert rules for delay, idle, ETA overdue, missing tracking updates,
  route deviation, sensor warning, and progress shortfall
- Automatic low-stock monitoring based on database-managed company/model/stage
  rules and live non-cancelled shipment quantities
- Stock rules scoped by company, vehicle model, and logistics stage, with
  API-backed stage labels in the administrator form
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

For a fresh clone, follow the complete
[clone, install, and run guide](docs/SETUP_AND_RUN_GUIDE.md). It covers local
development, Docker/Nginx, Supabase, first-admin bootstrap, the optional local
blockchain, automated checks, and common startup errors.

For public internet deployment, follow the
[Vercel online deployment guide](docs/VERCEL_ONLINE_DEPLOYMENT_GUIDE.md). The
frontend runs on Vercel while the scheduler-dependent Spring Boot API runs as
one Render container connected to Supabase.

The repository was conservatively audited for dead code on 2026-08-28. Unused
Vite template assets, template documentation, the unused Lombok dependency, and
an unreferenced manual blockchain-event endpoint were removed. Shipment events
continue to be recorded automatically through the backend-controlled audit and
blockchain pipeline. The cleanup was verified with a clean backend compile, all
backend and frontend tests, frontend lint/build checks, and the Solidity tests.

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
+-- frontend/
|   +-- Dockerfile
|   +-- nginx/default.conf
|   +-- vercel.json
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
|   +-- SETUP_AND_RUN_GUIDE.md
|   +-- VERCEL_ONLINE_DEPLOYMENT_GUIDE.md
+-- render.yaml
+-- README.md
+-- PROJECT_PROGRESS.md
```

## Technology Stack

| Layer          | Technology                                             |
| -------------- | ------------------------------------------------------ |
| Frontend       | React, Vite, React Router, Axios, Leaflet              |
| Backend        | Java 21, Spring Boot, Spring Security, Spring Data JPA |
| Database       | Supabase PostgreSQL                                    |
| Blockchain     | Solidity, Hardhat, Web3j                               |
| Authentication | JWT, BCrypt                                            |
| Build Tools    | Maven, npm                                             |

## Environment Configuration

Copy `.env.example` to `.env` for the backend and
`frontend/.env.example` to `frontend/.env.local` for Vite. Both destination
files are ignored by Git. Do not commit database passwords, JWT secrets,
blockchain private keys, or first-admin credentials.

The exact variables and safe secret-generation command are documented in the
[setup guide](docs/SETUP_AND_RUN_GUIDE.md#5-configure-local-development).
`APP_TIME_ZONE` defaults to `Asia/Kuala_Lumpur` and controls the automatic
shipment start timestamp in local, Docker, and Render deployments.

## Supabase PostgreSQL Setup

Supabase changes are stored as incremental SQL files in `database/migrations/`.
This checkout assumes the application base schema already exists. A completely
empty Supabase project first needs a schema-only export from the existing
project.

Recommended setup:

1. Create a Supabase project.
2. Open `SQL Editor`.
3. Import the current application schema if the database is empty.
4. Apply migrations that have not already been run, in filename order.
5. Open the Supabase `Connect` panel and copy either the Direct Connection or
   Session Pooler connection details.
6. Start or restart the backend with Supabase database environment variables.

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

## Online Deployment

The recommended hosted architecture is:

- Vercel for the React + Vite frontend
- One Render Docker web service for Spring Boot and its schedulers
- Supabase PostgreSQL for persistent data
- A public EVM test network only when online blockchain recording is enabled

Do not deploy the current Spring Boot API as a horizontally scaled function.
Automatic simulation and stock monitoring run every 5 and 30 seconds inside the
backend, so it must use one application instance. The selected Render Free plan
sleeps after inactivity, pausing those jobs until the next request wakes it.
Follow the [step-by-step Vercel guide](docs/VERCEL_ONLINE_DEPLOYMENT_GUIDE.md)
for the deployment order, environment variables, CORS configuration, and
verification.

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

Use the [clone, install, and run guide](docs/SETUP_AND_RUN_GUIDE.md) for the
complete portable procedure. In summary, configure the ignored environment
files, run `npm ci` in `frontend`, run `.\mvnw.cmd spring-boot:run` from
`backend`, and run `npm run dev` from `frontend`. Blockchain dependencies and
processes are optional.

## Login Accounts

Users sign in with real accounts stored in the `app_users` table. The redesigned
login page does not contain demo credentials, role shortcuts, or hardcoded
account data.

For a database without a working administrator, temporarily set
`APP_BOOTSTRAP_ADMIN_ENABLED=true` with `APP_BOOTSTRAP_ADMIN_FULL_NAME`,
`APP_BOOTSTRAP_ADMIN_EMAIL`, and `APP_BOOTSTRAP_ADMIN_PASSWORD`. Disable and
clear the bootstrap values immediately after the account is created.

After the first admin can sign in, create manufacturer, logistics provider, and
dealership accounts from `Admin > User Accounts`.

Passwords are stored as BCrypt hashes, so the original plaintext password
cannot be displayed later. Accounts created from **Admin > User Accounts** use
the temporary password entered by the administrator, and each user can change
their own password from **My Account**.

## Troubleshooting

### No Active Sessions on `/simulation`

Confirm that the database contains the simulation profile/session schema,
restart the backend, and wait one scheduler cycle. Eligible shipments must have
status `CREATED`, `READY_FOR_DISPATCH`, `PICKED_UP`, `IN_TRANSIT`, or `DELAYED`
and complete origin/destination coordinates.

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
-> Manufacturer optionally marks ready for dispatch and pickup
-> Assigned manufacturer selects Start Transit
-> Backend system generates tracking updates
-> Logistics provider marks shipment arrived
-> Assigned dealership confirms delivery receipt
-> Shipment becomes delivered
```

### Continuous IoT Simulation Workflow

```text
Manufacturer creates shipment with route coordinates
-> Shipment remains stationary during pre-transit processing
-> Assigned manufacturer selects Start Transit
-> Backend creates a system-owned simulation session
-> Scheduler chooses random database-backed telemetry profile
-> Scheduler advances GPS progress along the planned origin-to-destination route
-> Route-deviation profiles apply one bounded, non-accumulating route offset
-> Scheduler records speed and condition tracking events
-> Map, tracking history, alerts, and simulation monitor update
-> Session completes when destination or max event limit is reached
```

The automatic scheduler creates at most one active system-managed session for
each shipment after transit starts. Its safety event limit is expanded from
route distance and weighted profile speeds when the configured default would
stop the shipment before its destination. Completed session history is retained;
a pre-transit legacy session can be replaced when transit starts.
Alert-engine delay decisions are also retained until a later lifecycle action;
normal telemetry does not repeatedly toggle the shipment back to `IN_TRANSIT`.
Normal, delayed, and idle telemetry remains on the planned route. A configured
route-deviation scenario temporarily moves the vehicle within the configured
offset and the next non-deviation update returns it to the planned route.

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

When blockchain integration is disabled, automatic shipment-event anchoring is
skipped and no failed blockchain record or admin notification is created. When
the integration is enabled but a real ledger write fails, the record is saved as
`FAILED` with a clear message for admin review. Retry is enabled only when the
health endpoint reports the node, deployed contract, and signer as ready.
Records that already have an on-chain transaction are never eligible for another
write.

The monitor correctly reports `DISABLED` while `BLOCKCHAIN_ENABLED=false`. An
online deployment becomes healthy only after it is configured with an
internet-reachable RPC endpoint, a deployed `ShipmentLedger` contract address,
and a funded signer private key. A Render service cannot use a blockchain node
running only on the developer computer at `127.0.0.1`.

The Hardhat workspace includes an environment-backed Sepolia network and an
optimized `npm run deploy:sepolia` command. Deployment secrets are stored in
Hardhat's encrypted user keystore, while the generated
`blockchain/deployment.sepolia.json` output remains outside version control.
For hosted transactions, the backend obtains the connected network's chain ID
and current gas price before signing. `BLOCKCHAIN_GAS_PRICE_WEI=0` enables this
automatic pricing; a positive value remains available as an explicit override.

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

The backend uses a deliberately small Hikari pool for Supabase session mode:
two connections at most and no permanently idle connection. This keeps local,
active Render, and rolling-deployment instances within the Free-plan session
pool. Keep the JDBC URL on the Hibernate-compatible session pooler port `5432`.

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

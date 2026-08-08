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
- JWT login using seeded users
- Supabase PostgreSQL Session Pooler configuration
- Shipment creation, listing, details, deletion, and workflow status updates
- Auto-generated unique shipment codes controlled by the backend
- Database-backed Mercedes-Benz vehicle model dropdown for shipment creation
- Database-backed manufacturer origin and dealership destination dropdowns using
  stable `model_code` and `party_key` values
- API-backed shipment search, status/stage filtering, and pagination
- Shipment operation data for ETD, ETA, logistics stage, stock/WIP state,
  responsible person, customer receiver details, and document reference
- Planned route coordinates for route deviation and progress monitoring
- Delivery receipt confirmation with audit/blockchain event recording
- Simulated IoT tracking updates with GPS, speed, and condition status
- Leaflet map visualization with route history and planned route markers
- Advanced alert rules for delay, idle, ETA overdue, missing tracking updates,
  route deviation, sensor warning, and progress shortfall
- Shipment event timeline for audit trail
- Real local blockchain ledger integration using Hardhat, Solidity, and Web3j
- Admin blockchain monitor with integrity verification
- Admin/company notification records for blockchain integrity issues

The project progress checklist is maintained in
[PROJECT_PROGRESS.md](PROJECT_PROGRESS.md).

## Project Structure

```text
smart-auto-logistics/
+-- backend/
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
|   +-- supabase-schema.sql
+-- frontend/
|   +-- src/
|       +-- api/
|       +-- auth/
|       +-- components/
|       +-- pages/
|       +-- styles/
|       +-- utils/
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
```

## Supabase PostgreSQL Setup

The Supabase schema script for this project is maintained at
`database/supabase-schema.sql`.

Recommended setup:

1. Create a Supabase project.
2. Open `SQL Editor`.
3. Create a new query.
4. Paste and run the full contents of `database/supabase-schema.sql`.
5. Open the Supabase `Connect` panel and copy either the Direct Connection or
   Session Pooler connection details.
6. Start the backend with Supabase database environment variables.

Rerun `database/supabase-schema.sql` whenever this project adds database fields.
The script uses `if not exists` checks and seed-user upserts so existing
Supabase projects can receive the latest columns, indexes, and profile data.

For the shipment dropdown master-data change only, run:

```text
database/migrations/2026-08-07-shipment-master-data.sql
```

This creates and fills:

- `vehicle_models`, keyed by `model_code`
- `shipment_party_profiles`, keyed by `party_key`

The application reads create-shipment dropdown data from those tables. Do not
hardcode vehicle model names, manufacturer origins, or dealership destinations in
JavaScript or Java.

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

## Local Setup

### 1. Database

Create the tables in Supabase by running the SQL script from
`database/supabase-schema.sql` in the Supabase SQL Editor.

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

## Seeded Login Accounts

| Role | Email | Password |
| --- | --- | --- |
| Admin | `admin@autologistics.com` | `Admin@12345` |
| Manufacturer | `manufacturer@autologistics.com` | `Manufacturer@12345` |
| Logistics Provider | `logistics@autologistics.com` | `Logistics@12345` |
| Dealership | `dealer@autologistics.com` | `Dealer@12345` |
| Dealership | `dealer-selangor@autologistics.com` | `Dealer@12345` |
| Dealership | `dealer-penang@autologistics.com` | `Dealer@12345` |

These accounts are seeded by the backend and authenticate through JWT. Shipment
dropdown master data is stored separately in Supabase tables, not in frontend or
backend code.

## Troubleshooting

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
clear local storage for `localhost:5173`, then sign in again with one of the
seeded accounts above.

## Main Workflows

### Shipment Workflow

```text
Create shipment with generated shipment code
-> Select Mercedes-Benz model
-> Select registered manufacturer origin
-> Select registered dealership destination
-> Mark ready for dispatch
-> Mark picked up
-> Mark in transit
-> Simulate tracking updates
-> Mark arrived
-> Confirm delivery receipt
-> Shipment becomes delivered
```

### Blockchain Integrity Workflow

```text
Important shipment event occurs
-> Backend hashes event payload with SHA-256
-> Backend writes hash to ShipmentLedger smart contract
-> Database stores transaction hash and block number
-> Admin can verify database record against on-chain hash
-> Failed verification creates admin/company integrity notifications
```

If the blockchain node or contract is not configured, the backend does not
generate fake transaction data. The blockchain record is saved as `FAILED` with
a clear message for admin review.

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

Targeted frontend lint:

```powershell
cd frontend
.\node_modules\.bin\eslint.cmd src\pages\AlertsPage.jsx src\pages\ShipmentDetailsPage.jsx src\components\map\ShipmentMap.jsx
```

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

## Recent Implementation Update

The latest completed step improved shipment creation data integrity:

- Backend now generates unique shipment codes after the shipment database ID is
  assigned
- Create shipment no longer accepts editable shipment-code, origin,
  destination, dealership, or route-coordinate text from the user
- Added database-backed `vehicle_models` and `shipment_party_profiles` master
  tables
- Shipment options API now reads Mercedes-Benz models, manufacturer origins, and
  dealership destinations from Supabase by unique keys
- Updated the Supabase schema script with master-data tables, shipment party
  keys, vehicle model codes, and unique shipment-code indexing
- Updated the create shipment UI with read-only generated/derived fields and
  dropdown-driven route selection

## Known Remaining Work

See [PROJECT_PROGRESS.md](PROJECT_PROGRESS.md) for the full checklist. The next
recommended implementation area is reporting pages and CSV export.

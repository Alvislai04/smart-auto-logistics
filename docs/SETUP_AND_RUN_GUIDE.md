# Clone, Install, and Run Guide

This guide sets up Smart Automotive Logistics on Windows with PowerShell. Use
either local development mode or the Docker and Nginx mode. Both modes connect
the Spring Boot backend to Supabase PostgreSQL; the browser never connects to
the database directly.

## 1. Choose a Run Mode

| Mode | Install on the computer | Application URL | Best for |
| --- | --- | --- | --- |
| Local development | Java 21 and Node.js 22 | `http://localhost:5173` | Coding and debugging |
| Docker and Nginx | Docker Desktop | `http://localhost:8088` | Production-like testing |

The local Hardhat blockchain is optional in both modes. Start with blockchain
integration disabled, confirm login and database access, and then enable it if
blockchain verification is required.

## 2. Prerequisites

Always required:

- Git
- An existing Supabase project containing the application schema
- The Supabase database password

For local development:

- Java Development Kit 21
- Node.js 22 LTS, which includes npm

For Docker mode:

- Docker Desktop with Docker Compose

Verify the installed tools:

```powershell
git --version
java -version
node --version
npm --version
docker --version
docker compose version
```

Only check the commands required by the mode being used.

## 3. Clone the Repository

```powershell
git clone https://github.com/Alvislai04/smart-auto-logistics.git
cd smart-auto-logistics
```

All remaining commands in this guide assume PowerShell is at the repository
root unless a command changes directory explicitly.

## 4. Confirm the Supabase Schema

This checkout currently contains incremental migrations, not a complete
empty-database bootstrap script. The normal clone flow therefore connects to
the existing Supabase project used by the application.

The available incremental migrations are:

```text
database/migrations/2026-08-13-blockchain-retry-audit.sql
database/migrations/2026-08-18-stock-order-alerts.sql
```

Apply a migration in the Supabase SQL Editor only when it has not already been
applied to that database. A completely new Supabase project must first receive
a current schema-only export from the existing project; applying only these
incremental migrations to an empty database is not sufficient.

## 5. Configure Local Development

### 5.1 Backend environment

Create the ignored local environment file:

```powershell
Copy-Item .env.example .env
notepad .env
```

In Supabase, open **Connect**, select the Session Pooler, and enter its values:

```properties
DB_URL=jdbc:postgresql://aws-0-YOUR_REGION.pooler.supabase.com:5432/postgres?sslmode=require
DB_USERNAME=postgres.YOUR_PROJECT_REF
DB_PASSWORD=your-real-database-password
```

The Supabase `postgresql://` URI cannot be pasted directly into `DB_URL`.
Spring Boot requires the `jdbc:postgresql://` form shown above.

Generate a separate JWT signing secret:

```powershell
$bytes = [byte[]]::new(64)
[Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
[Convert]::ToBase64String($bytes)
```

Paste the generated result into `JWT_SECRET`. Do not use the database password
as the JWT secret. Keep this value stable; changing it signs out current users.

For the first run, keep:

```properties
BLOCKCHAIN_ENABLED=false
```

### 5.2 Optional first administrator

If the database already has a working administrator, leave the bootstrap
setting disabled. If no administrator exists, set these values temporarily:

```properties
APP_BOOTSTRAP_ADMIN_ENABLED=true
APP_BOOTSTRAP_ADMIN_FULL_NAME=System Administrator
APP_BOOTSTRAP_ADMIN_EMAIL=your-admin-email@example.com
APP_BOOTSTRAP_ADMIN_PASSWORD=your-strong-temporary-password
```

After the backend creates the account successfully, change
`APP_BOOTSTRAP_ADMIN_ENABLED` to `false`, clear the three bootstrap values, and
restart the backend. Additional accounts must be created through **Admin > User
Accounts**.

### 5.3 Frontend environment

Vite loads environment files from the `frontend` folder, so create this file:

```powershell
Copy-Item frontend\.env.example frontend\.env.local
```

The default local value is already correct:

```properties
VITE_API_BASE_URL=http://localhost:8080/api
```

Never place Supabase credentials or private keys in a `VITE_` variable because
Vite exposes those values to the browser bundle.

## 6. Install the Libraries

Install the frontend exactly from its lock file:

```powershell
Set-Location frontend
npm ci
Set-Location ..
```

The backend includes the Maven Wrapper. Its Java dependencies are downloaded
automatically the first time `mvnw.cmd` runs, so a global Maven installation is
not required.

Install blockchain libraries only when the local blockchain will be used:

```powershell
Set-Location blockchain
npm ci
Set-Location ..
```

Use `npm ci`, rather than `npm install`, after cloning so the versions in each
`package-lock.json` are installed reproducibly.

## 7. Run in Local Development Mode

Open two PowerShell terminals.

Terminal 1, Spring Boot backend:

```powershell
Set-Location backend
.\mvnw.cmd spring-boot:run
```

Wait for `Started AutologisticsApplication`, then verify the backend:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
```

The response should contain `status: UP`.

Terminal 2, React frontend:

```powershell
Set-Location frontend
npm run dev
```

Open `http://localhost:5173/login`. The browser should call the Spring Boot API
at `http://localhost:8080/api`.

Stop either process with `Ctrl+C` in its terminal.

## 8. Enable the Local Blockchain

Skip this section when `BLOCKCHAIN_ENABLED=false` is sufficient.

First install the blockchain libraries as shown in section 6. Then use two more
PowerShell terminals.

Terminal 3, local Hardhat node:

```powershell
Set-Location blockchain
npm run node
```

Keep this terminal running. Copy one of the displayed development account
private keys. These keys are for the local Hardhat network only and must never
be used on a public network.

Terminal 4, compile and deploy the contract:

```powershell
Set-Location blockchain
npm run compile
npm run deploy:local
Get-Content deployment.local.json
```

Update the root `.env` with the deployed contract address and the selected
Hardhat private key:

```properties
BLOCKCHAIN_ENABLED=true
BLOCKCHAIN_RPC_URL=http://127.0.0.1:8545
BLOCKCHAIN_CONTRACT_ADDRESS=0x...
BLOCKCHAIN_PRIVATE_KEY=0x...
```

Restart the Spring Boot backend. The administrator blockchain monitor should
then report the node, contract, and signer as healthy.

## 9. Run with Docker and Nginx

Docker mode builds frontend and backend dependencies inside their images. Java,
Node.js, and npm are not required on the host unless the Hardhat node is also
run locally.

Create the ignored deployment environment file:

```powershell
Copy-Item deployment\.env.production.example deployment\.env
notepad deployment\.env
```

Fill `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and a generated `JWT_SECRET`. For
local Docker testing, keep:

```properties
APP_PORT=8088
APP_CORS_ALLOWED_ORIGINS=http://localhost:8088
APP_TIME_ZONE=Asia/Kuala_Lumpur
BLOCKCHAIN_ENABLED=false
```

The optional first-administrator settings work the same way as in section 5.2.
Disable and clear them after the first successful startup.

Build and start the stack:

```powershell
docker compose --env-file deployment\.env -f deployment\compose.production.yml up -d --build
```

Check container status and the Nginx health endpoint:

```powershell
docker compose --env-file deployment\.env -f deployment\compose.production.yml ps
Invoke-RestMethod http://localhost:8088/healthz
```

Open `http://localhost:8088/login`. Nginx serves the React application and
proxies same-origin `/api` requests to the backend container.

View backend logs when startup fails:

```powershell
docker compose --env-file deployment\.env -f deployment\compose.production.yml logs -f backend
```

Stop the stack without deleting Supabase data:

```powershell
docker compose --env-file deployment\.env -f deployment\compose.production.yml down
```

When Docker uses a Hardhat node running on Windows, set
`BLOCKCHAIN_RPC_URL=http://host.docker.internal:8545`, deploy the contract, fill
the address and private key, and rebuild or restart the backend container.

## 10. Verify the Installation

Complete this short smoke test:

1. The backend health endpoint reports `UP`.
2. The login page opens without a browser console network error.
3. A real active account can sign in and reaches its role-specific dashboard.
4. The sidebar only shows pages allowed for that role.
5. Shipment and alert pages load data from Supabase.
6. An administrator can open User Accounts and Master Data.
7. If enabled, the blockchain monitor reports healthy configuration.

## 11. Run the Automated Checks

Backend tests:

```powershell
Set-Location backend
.\mvnw.cmd test
Set-Location ..
```

Frontend checks:

```powershell
Set-Location frontend
npm run lint
npm test -- --run
npm run build
Set-Location ..
```

Blockchain tests, when its dependencies are installed:

```powershell
Set-Location blockchain
npm test
Set-Location ..
```

## 12. Common Startup Problems

### `502 Bad Gateway` at `http://localhost:8088/healthz`

Nginx is running but the backend is not ready. Read the backend logs. The most
common causes are incorrect Supabase credentials, a missing database table, or
production schema validation failing because a migration was not applied.

### Supabase password authentication failed

Confirm that the Session Pooler username includes the project reference, the
password is the database password rather than the Supabase website password,
and `DB_URL` begins with `jdbc:postgresql://`.

### Login says the email or password is invalid

Confirm the account is active in `app_users` and that this installation points
to the expected Supabase project. Use the first-administrator bootstrap only to
create or recover an administrator you control, then disable it immediately.

### Frontend reports a network error

For local mode, confirm `frontend/.env.local` points to
`http://localhost:8080/api` and restart Vite after changing the file. For Docker
mode, the compiled API base URL is `/api` automatically.

### Port already in use

Stop the existing process using port `5173`, `8080`, `8088`, or `8545`, or
change the relevant frontend, backend, Docker `APP_PORT`, or Hardhat setting.

### Blockchain health is unavailable

Keep `BLOCKCHAIN_ENABLED=false` unless the Hardhat node is running and the
contract has been deployed to that exact node session. Restarting a fresh
Hardhat node requires deploying the contract again and updating its address.

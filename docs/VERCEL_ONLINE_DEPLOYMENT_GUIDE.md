# Vercel Online Deployment Guide

This guide deploys the complete application with this production layout:

```text
Browser
  -> Vercel: React + Vite frontend
  -> Render: Spring Boot REST API and scheduled jobs
  -> Supabase: PostgreSQL database
  -> Optional public EVM testnet: blockchain ledger
```

Vercel supports Vite directly and provides HTTPS deployment URLs. The Spring
Boot API is deliberately deployed as one always-on container instead of a
Vercel Function because this application runs automatic simulation every five
seconds and stock monitoring every thirty seconds. Function scale-to-zero and
horizontal scaling are not appropriate for those in-process schedulers.

The committed `render.yaml` selects Render's paid `starter` plan so the backend
does not sleep. Render's free web services spin down after inactivity, which
would pause the automatic simulation and stock scheduler.

Official references:

- [Vite on Vercel](https://vercel.com/docs/frameworks/frontend/vite)
- [Vercel monorepo root directories](https://vercel.com/docs/monorepos)
- [Vercel environment variables](https://vercel.com/docs/environment-variables)
- [Vercel Functions limits](https://vercel.com/docs/functions/limitations)
- [Render Docker deployment](https://render.com/docs/docker)
- [Render Blueprint specification](https://render.com/docs/blueprint-spec)
- [Render free-service behavior](https://render.com/docs/faq)

## 1. Before Deployment

Confirm all of the following:

1. The latest project version is pushed to a private or public GitHub repository.
2. `frontend/vercel.json`, `render.yaml`, and this guide are included in the
   pushed commit.
3. The existing Supabase database contains the complete application schema and
   required migrations.
4. A real administrator account can sign in locally.
5. `.env`, `frontend/.env.local`, and `deployment/.env` are not committed.

Check ignored secret files before pushing:

```powershell
git check-ignore -v .env frontend\.env.local deployment\.env
git status --short
```

Do not continue if `git status` shows a real secret file as tracked or untracked.

## 2. Prepare the Production Values

Keep these values ready. Do not put them in source files.

| Variable | Production value |
| --- | --- |
| `DB_URL` | Supabase Session Pooler JDBC URL with `?sslmode=require` |
| `DB_USERNAME` | `postgres.YOUR_PROJECT_REF` |
| `DB_PASSWORD` | Supabase database password |
| `APP_CORS_ALLOWED_ORIGINS` | Exact Vercel HTTPS origin, added after Vercel deploy |
| `VITE_API_BASE_URL` | Render HTTPS API URL ending in `/api` |

Session Pooler format:

```properties
DB_URL=jdbc:postgresql://aws-0-YOUR_REGION.pooler.supabase.com:5432/postgres?sslmode=require
DB_USERNAME=postgres.YOUR_PROJECT_REF
```

Use the Supabase database password, not the Supabase dashboard account password.

## 3. Deploy the Spring Boot Backend on Render

Deploy the backend first because Vercel needs its public API URL.

1. Sign in to [Render](https://dashboard.render.com/).
2. Select **New > Blueprint**.
3. Connect the GitHub account that owns the repository.
4. Select the `smart-auto-logistics` repository.
5. Keep the Blueprint path as `render.yaml`.
6. Review the service. It should show:
   - Type: Web Service
   - Runtime: Docker
   - Region: Singapore
   - Plan: Starter
   - Health check: `/actuator/health`
   - Instances: 1
7. Enter the prompted secret variables:

```text
DB_URL                         your Supabase JDBC Session Pooler URL
DB_USERNAME                    postgres.YOUR_PROJECT_REF
DB_PASSWORD                    your Supabase database password
APP_CORS_ALLOWED_ORIGINS       http://localhost:5173
```

The localhost CORS value is temporary until the Vercel URL exists. Render
generates and stores `JWT_SECRET`; do not replace it on every deployment.

8. Confirm the paid Starter service and create the Blueprint.
9. Wait for the Docker build and deployment to complete.
10. Copy the service URL, for example:

```text
https://smart-auto-logistics-api.onrender.com
```

11. Verify backend health:

```powershell
Invoke-RestMethod https://YOUR-RENDER-SERVICE.onrender.com/actuator/health
```

Expected result:

```text
status
------
UP
```

If deployment fails, open the Render service, select **Logs**, and check the
first Spring Boot error. A Hibernate validation error means the Supabase schema
or a required migration is missing.

## 4. Deploy the React Frontend on Vercel

1. Sign in to [Vercel](https://vercel.com/).
2. Select **Add New > Project**.
3. Import the same GitHub repository.
4. In **Configure Project**, set **Root Directory** to `frontend`.
5. Confirm these build settings:

| Setting | Value |
| --- | --- |
| Framework Preset | Vite |
| Install Command | `npm ci` |
| Build Command | `npm run build` |
| Output Directory | `dist` |

6. Add these environment variables for **Production** and **Preview**:

```text
VITE_API_BASE_URL=https://YOUR-RENDER-SERVICE.onrender.com/api
VITE_DASHBOARD_REFRESH_MS=15000
VITE_ALERT_BADGE_REFRESH_MS=15000
```

Only the public backend URL belongs in `VITE_API_BASE_URL`. Never add a database
password, JWT secret, or blockchain private key to a `VITE_` variable because
Vite places those values in the browser bundle.

7. Select **Deploy**.
8. Wait for the build to finish and copy the production URL, for example:

```text
https://smart-auto-logistics.vercel.app
```

The committed `frontend/vercel.json` sends direct browser visits such as
`/login`, `/dashboard`, and `/shipments/2` to the React application instead of
returning a Vercel 404.

## 5. Allow the Vercel Origin in the Backend

The backend currently allows only localhost, so complete the CORS handoff:

1. Open the backend service in Render.
2. Open **Environment**.
3. Change `APP_CORS_ALLOWED_ORIGINS` to the exact Vercel origin:

```text
https://YOUR-PROJECT.vercel.app
```

Do not include a trailing slash or path. To keep local frontend development
against the hosted backend, use a comma-separated list:

```text
https://YOUR-PROJECT.vercel.app,http://localhost:5173
```

4. Save the change and allow Render to redeploy or restart the service.
5. Reload the Vercel login page after the backend is healthy.

Preview deployment URLs are different origins. For production security, do not
allow every `*.vercel.app` project. Test authenticated flows on the production
Vercel URL, or add a specific preview URL temporarily and remove it afterward.

## 6. Verify the Online System

Run this production smoke test:

1. Open the Vercel `/login` URL in a private browser window.
2. Sign in with a real active account.
3. Confirm redirection to the correct role dashboard.
4. Open Shipments, Alerts, Stock Monitoring, and My Account.
5. Open an existing shipment directly in a new tab to confirm SPA routing.
6. Create a shipment as a manufacturer.
7. Wait for automatic tracking values to change.
8. Confirm the Render logs do not show CORS, database, or scheduler errors.
9. Confirm Supabase receives the new shipment, event, and tracking rows.

Useful checks:

```powershell
Invoke-RestMethod https://YOUR-RENDER-SERVICE.onrender.com/actuator/health
Invoke-WebRequest https://YOUR-PROJECT.vercel.app/login -UseBasicParsing
```

## 7. Automatic Deployments

After GitHub is connected:

- Pushes to the production branch redeploy the Vercel frontend.
- The Render Blueprint redeploys the backend when relevant commits are pushed.
- Vercel creates preview deployments for other branches and pull requests.
- Changes to Vercel environment variables affect only new deployments, so
  redeploy after changing `VITE_API_BASE_URL`.

Run automated checks before pushing:

```powershell
Set-Location backend
.\mvnw.cmd test
Set-Location ..\frontend
npm ci
npm run lint
npm test -- --run
npm run build
Set-Location ..
```

## 8. Custom Domain

To use a custom domain:

1. Open the Vercel project.
2. Open **Settings > Domains**.
3. Add the domain and apply Vercel's DNS instructions.
4. Wait for HTTPS verification.
5. Change Render's `APP_CORS_ALLOWED_ORIGINS` to the custom HTTPS origin.
6. Keep the `vercel.app` origin in the list only if it should remain usable.

Example:

```text
https://logistics.example.com,https://YOUR-PROJECT.vercel.app
```

## 9. Blockchain Deployment

The local Hardhat URL `http://127.0.0.1:8545` is reachable only from the local
computer. It cannot be used by the hosted backend.

The initial online deployment keeps:

```text
BLOCKCHAIN_ENABLED=false
```

To make blockchain recording fully online, deploy `ShipmentLedger.sol` to a
durable EVM test network and configure these Render secrets:

```text
BLOCKCHAIN_ENABLED=true
BLOCKCHAIN_RPC_URL=https://your-provider-rpc-url
BLOCKCHAIN_CONTRACT_ADDRESS=0x...
BLOCKCHAIN_PRIVATE_KEY=0x...
```

Use a dedicated deployment wallet with only test-network funds. Never expose
the private key in GitHub, Vercel, frontend variables, screenshots, or logs.
After changing the values, restart the backend and verify the administrator
blockchain health page before processing important shipment events.

## 10. Common Deployment Problems

### The website opens but login shows a network error

Check that `VITE_API_BASE_URL` contains the Render HTTPS URL plus `/api`. After
changing it in Vercel, redeploy because Vite embeds it during the build.

### The browser reports a CORS error

Set `APP_CORS_ALLOWED_ORIGINS` to the exact Vercel origin without a trailing
slash. Confirm Render restarted after the environment change.

### A direct page such as `/shipments/2` returns 404

Confirm the Vercel project Root Directory is `frontend` and that
`frontend/vercel.json` appears in the deployment source.

### Render fails with database authentication errors

Recheck the Supabase Session Pooler host, project-reference username, database
password, JDBC prefix, and `sslmode=require` query parameter.

### Render fails with schema validation errors

Apply the missing migration in Supabase. Production uses Hibernate `validate`
and intentionally does not modify the schema automatically.

### Automatic simulation stops

Confirm the Render service uses an always-on paid instance, has exactly one
application instance, and both scheduler environment variables are `true`. A
free sleeping service cannot provide continuous background simulation.

### Users are signed out after a backend configuration change

Confirm `JWT_SECRET` was not deleted or regenerated. Existing tokens become
invalid whenever the signing secret changes.

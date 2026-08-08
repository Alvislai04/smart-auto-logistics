# Smart Auto Logistics Blockchain

This folder contains the local Ethereum-compatible ledger used by the backend to
store tamper-resistant shipment event hashes.

## Local setup

1. Install dependencies:

   ```bash
   npm install
   ```

2. Start a local Hardhat node:

   ```bash
   npm run node
   ```

3. In a second terminal, deploy the ledger contract:

   ```bash
   npm run deploy:local
   ```

4. Copy `contractAddress` from `deployment.local.json` into the backend
   `BLOCKCHAIN_CONTRACT_ADDRESS` environment variable.

5. Use the first Hardhat account private key as
   `BLOCKCHAIN_PRIVATE_KEY` for local development only.

The backend does not generate fake transaction hashes. If the blockchain node or
contract is unavailable, records are saved as `FAILED` with a clear operational
message so the admin monitor can show the issue.

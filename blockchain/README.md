# Smart Auto Logistics Blockchain

This folder contains the Ethereum-compatible ledger used by the backend to store
tamper-resistant shipment event hashes. It supports a local Hardhat network for
development and Ethereum Sepolia for the deployed Render backend.

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

## Sepolia deployment

Use a dedicated test-only wallet funded with Sepolia ETH. Store the RPC URL and
private key in Hardhat's encrypted keystore instead of project files or terminal
history:

```powershell
npx hardhat keystore set SEPOLIA_RPC_URL
npx hardhat keystore set SEPOLIA_PRIVATE_KEY
npm run deploy:sepolia
```

The deployment command compiles with the optimized production profile and writes
the public transaction details to `deployment.sepolia.json`. This generated file
is ignored by Git because the contract address belongs to a specific deployment.

Configure Render with the same RPC URL, the generated `contractAddress`, and the
dedicated wallet private key before setting `BLOCKCHAIN_ENABLED=true`. Keep
`BLOCKCHAIN_GAS_PRICE_WEI=0` so the backend obtains the current price from the
connected node; use a positive value only when an intentional manual override is
required. Transactions are signed with the chain ID returned by the node.

When blockchain integration is disabled, automatic ledger writes are skipped.
When it is enabled and a real node or contract action fails, the database record
is stored as `FAILED` with a clear operational message for the admin monitor.

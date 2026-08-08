import { network } from "hardhat";
import { writeFile } from "node:fs/promises";

async function main() {
  const { ethers } = await network.connect();
  const ledger = await ethers.deployContract("ShipmentLedger");

  await ledger.waitForDeployment();

  const address = await ledger.getAddress();
  const deployment = await ledger.deploymentTransaction()?.wait();

  const deploymentInfo = {
    contractName: "ShipmentLedger",
    contractAddress: address,
    transactionHash: deployment?.hash,
    blockNumber: deployment?.blockNumber,
    network: network.name,
    deployedAt: new Date().toISOString(),
  };

  await writeFile(
    new URL("../deployment.local.json", import.meta.url),
    `${JSON.stringify(deploymentInfo, null, 2)}\n`,
  );

  console.log(`ShipmentLedger deployed to ${address}`);
  console.log("Deployment details written to blockchain/deployment.local.json");
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});

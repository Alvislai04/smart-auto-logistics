import { network } from "hardhat";
import { writeFile } from "node:fs/promises";

async function main() {
  const { ethers, networkName } = await network.connect();
  const ledger = await ethers.deployContract("ShipmentLedger");

  await ledger.waitForDeployment();

  const address = await ledger.getAddress();
  const deployment = await ledger.deploymentTransaction()?.wait();

  const deploymentInfo = {
    contractName: "ShipmentLedger",
    contractAddress: address,
    transactionHash: deployment?.hash,
    blockNumber: deployment?.blockNumber,
    network: networkName,
    deployedAt: new Date().toISOString(),
  };

  const deploymentLabel = networkName === "localhost" ? "local" : networkName;
  const deploymentFileName = `deployment.${deploymentLabel}.json`;

  await writeFile(
    new URL(`../${deploymentFileName}`, import.meta.url),
    `${JSON.stringify(deploymentInfo, null, 2)}\n`,
  );

  console.log(`ShipmentLedger deployed to ${address}`);
  console.log(`Deployment details written to blockchain/${deploymentFileName}`);
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});

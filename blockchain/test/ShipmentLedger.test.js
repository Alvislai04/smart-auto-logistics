import { describe, it } from "node:test";
import assert from "node:assert/strict";
import { network } from "hardhat";

describe("ShipmentLedger", async () => {
  const { ethers } = await network.connect();

  it("records a shipment event hash once", async () => {
    const ledger = await ethers.deployContract("ShipmentLedger");
    await ledger.waitForDeployment();

    const eventHash = ethers.keccak256(ethers.toUtf8Bytes("shipment-event-1"));

    await ledger.recordShipmentEvent(1, 101, eventHash);

    assert.equal(await ledger.getShipmentEventHash(101), eventHash);
    assert.equal(await ledger.hasShipmentEventRecord(101), true);
    assert.equal(await ledger.getRecordCount(), 1n);
  });

  it("rejects duplicate shipment event records", async () => {
    const ledger = await ethers.deployContract("ShipmentLedger");
    await ledger.waitForDeployment();

    const eventHash = ethers.keccak256(ethers.toUtf8Bytes("shipment-event-2"));

    await ledger.recordShipmentEvent(1, 102, eventHash);

    await assert.rejects(
      ledger.recordShipmentEvent(1, 102, eventHash),
      /ShipmentEventAlreadyRecorded/,
    );
  });
});

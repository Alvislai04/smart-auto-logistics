// SPDX-License-Identifier: MIT
pragma solidity ^0.8.28;

contract ShipmentLedger {
    struct ShipmentEventRecord {
        uint256 shipmentId;
        uint256 shipmentEventId;
        bytes32 eventHash;
        address recorder;
        uint256 recordedAt;
    }

    address public immutable owner;

    mapping(uint256 => ShipmentEventRecord) private recordsByShipmentEventId;
    mapping(uint256 => bool) private recordExists;
    uint256[] private shipmentEventIds;

    event ShipmentEventRecorded(
        uint256 indexed shipmentId,
        uint256 indexed shipmentEventId,
        bytes32 indexed eventHash,
        address recorder,
        uint256 recordedAt
    );

    error EmptyEventHash();
    error ShipmentEventAlreadyRecorded(uint256 shipmentEventId);
    error ShipmentEventNotRecorded(uint256 shipmentEventId);

    constructor() {
        owner = msg.sender;
    }

    function recordShipmentEvent(
        uint256 shipmentId,
        uint256 shipmentEventId,
        bytes32 eventHash
    ) external {
        if (eventHash == bytes32(0)) {
            revert EmptyEventHash();
        }

        if (recordExists[shipmentEventId]) {
            revert ShipmentEventAlreadyRecorded(shipmentEventId);
        }

        recordsByShipmentEventId[shipmentEventId] = ShipmentEventRecord({
            shipmentId: shipmentId,
            shipmentEventId: shipmentEventId,
            eventHash: eventHash,
            recorder: msg.sender,
            recordedAt: block.timestamp
        });

        recordExists[shipmentEventId] = true;
        shipmentEventIds.push(shipmentEventId);

        emit ShipmentEventRecorded(
            shipmentId,
            shipmentEventId,
            eventHash,
            msg.sender,
            block.timestamp
        );
    }

    function getShipmentEventRecord(
        uint256 shipmentEventId
    ) external view returns (ShipmentEventRecord memory) {
        if (!recordExists[shipmentEventId]) {
            revert ShipmentEventNotRecorded(shipmentEventId);
        }

        return recordsByShipmentEventId[shipmentEventId];
    }

    function getShipmentEventHash(
        uint256 shipmentEventId
    ) external view returns (bytes32) {
        if (!recordExists[shipmentEventId]) {
            revert ShipmentEventNotRecorded(shipmentEventId);
        }

        return recordsByShipmentEventId[shipmentEventId].eventHash;
    }

    function hasShipmentEventRecord(
        uint256 shipmentEventId
    ) external view returns (bool) {
        return recordExists[shipmentEventId];
    }

    function getRecordCount() external view returns (uint256) {
        return shipmentEventIds.length;
    }
}

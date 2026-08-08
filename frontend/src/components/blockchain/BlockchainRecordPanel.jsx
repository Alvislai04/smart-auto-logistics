function BlockchainRecordPanel({ blockchainRecords }) {
  const hasRecords = blockchainRecords.length > 0;
  const latestRecord = hasRecords ? blockchainRecords[0] : null;
  const latestStatus = latestRecord?.status ?? "PENDING";
  const recordedCount = blockchainRecords.filter(
    (record) => record.status === "RECORDED",
  ).length;

  return (
    <div className="panel">
      <div className="panel-header">
        <div>
          <h3>Blockchain Verification</h3>
          <p>
            Important shipment events are automatically recorded for integrity
            verification.
          </p>
        </div>

        <span
          className={`verification-badge ${latestStatus.toLowerCase()}`}
        >
          {hasRecords ? latestStatus : "PENDING"}
        </span>
      </div>

      {!hasRecords ? (
        <div className="empty-state">
          <p>
            No blockchain records yet. Important events such as shipment
            creation, status changes, delays, idle detection, and delivery
            confirmation will be recorded automatically.
          </p>
        </div>
      ) : (
        <>
          <div className="verification-summary-grid">
            <div className="verification-summary-card">
              <span>Recorded Events</span>
              <strong>{recordedCount}</strong>
            </div>

            <div className="verification-summary-card">
              <span>Latest Block</span>
              <strong>{latestRecord.blockNumber ?? "Not recorded"}</strong>
            </div>

            <div className="verification-summary-card">
              <span>Status</span>
              <strong>{latestRecord.status}</strong>
            </div>
          </div>

          <div className="blockchain-record-list">
            {blockchainRecords.map((record) => (
              <div key={record.id} className="blockchain-record-item">
                <div>
                  <strong>Shipment Event #{record.shipmentEventId}</strong>
                  <p>
                    <strong>Status:</strong> {record.status}
                  </p>
                  <p>
                    <strong>Block:</strong>{" "}
                    {record.blockNumber ?? "Not recorded"}
                  </p>
                  <p>
                    <strong>Recorded At:</strong> {record.recordedAt}
                  </p>
                  {record.verificationMessage && (
                    <p>
                      <strong>Message:</strong> {record.verificationMessage}
                    </p>
                  )}
                </div>

                <div className="hash-section">
                  <small>Event Hash</small>
                  <code>{record.eventHash}</code>

                  <small>Transaction Hash</small>
                  <code>{record.transactionHash ?? "Not recorded"}</code>
                </div>
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
}

export default BlockchainRecordPanel;

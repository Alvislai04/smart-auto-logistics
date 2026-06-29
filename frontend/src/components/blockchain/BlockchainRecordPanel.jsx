function BlockchainRecordPanel({ blockchainRecords }) {
  const hasRecords = blockchainRecords.length > 0;
  const latestRecord = hasRecords ? blockchainRecords[0] : null;

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
          className={
            hasRecords
              ? "verification-badge verified"
              : "verification-badge pending"
          }
        >
          {hasRecords ? "Verified" : "Pending"}
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
              <span>Total Verified Events</span>
              <strong>{blockchainRecords.length}</strong>
            </div>

            <div className="verification-summary-card">
              <span>Latest Block</span>
              <strong>{latestRecord.blockNumber}</strong>
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
                    <strong>Block:</strong> {record.blockNumber}
                  </p>
                  <p>
                    <strong>Recorded At:</strong> {record.recordedAt}
                  </p>
                </div>

                <div className="hash-section">
                  <small>Event Hash</small>
                  <code>{record.eventHash}</code>

                  <small>Transaction Hash</small>
                  <code>{record.transactionHash}</code>
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

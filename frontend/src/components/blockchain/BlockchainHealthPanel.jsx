function availabilityLabel(value) {
  return value ? "Ready" : "Not ready";
}

function formatCheckedAt(value) {
  if (!value) {
    return "Not checked";
  }

  const checkedAt = new Date(value);
  return Number.isNaN(checkedAt.getTime())
    ? value
    : checkedAt.toLocaleString();
}

function BlockchainHealthPanel({ health }) {
  if (!health) {
    return (
      <section className="panel blockchain-health-panel">
        <div className="empty-state">Blockchain health is unavailable.</div>
      </section>
    );
  }

  const statusClass = health.status?.toLowerCase() ?? "unavailable";

  return (
    <section className="panel blockchain-health-panel">
      <div className="blockchain-health-header">
        <div>
          <p className="section-eyebrow">Ledger infrastructure</p>
          <h3>Network Health</h3>
          <p>{health.message}</p>
        </div>

        <span className={`health-status health-status-${statusClass}`}>
          <span className="health-status-dot" aria-hidden="true" />
          {health.status}
        </span>
      </div>

      <div className="blockchain-health-grid">
        <div>
          <span>RPC Connection</span>
          <strong>{health.connected ? "Connected" : "Disconnected"}</strong>
        </div>
        <div>
          <span>Smart Contract</span>
          <strong>{availabilityLabel(health.contractDeployed)}</strong>
        </div>
        <div>
          <span>Signer</span>
          <strong>{availabilityLabel(health.signerConfigured)}</strong>
        </div>
        <div>
          <span>Chain ID</span>
          <strong>{health.chainId ?? "Unavailable"}</strong>
        </div>
        <div>
          <span>Latest Block</span>
          <strong>{health.latestBlockNumber ?? "Unavailable"}</strong>
        </div>
        <div>
          <span>Last Checked</span>
          <strong>{formatCheckedAt(health.checkedAt)}</strong>
        </div>
      </div>

      <div className="contract-address-row">
        <span>Configured Contract Address</span>
        <code>{health.contractAddress ?? "Not configured"}</code>
      </div>
    </section>
  );
}

export default BlockchainHealthPanel;

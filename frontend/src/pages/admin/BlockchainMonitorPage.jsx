import { useEffect, useMemo, useState } from "react";
import { blockchainApi } from "../../api/blockchainApi";

function BlockchainMonitorPage() {
  const [records, setRecords] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");
  const [verifyingRecordId, setVerifyingRecordId] = useState(null);

  useEffect(() => {
    loadBlockchainRecords();
  }, []);

  const loadBlockchainRecords = async () => {
    try {
      setIsLoading(true);
      setErrorMessage("");

      const data = await blockchainApi.getAll();
      setRecords(data);
    } catch (error) {
      setErrorMessage("Failed to load blockchain records.");
    } finally {
      setIsLoading(false);
    }
  };

  const handleVerifyRecord = async (recordId) => {
    try {
      setVerifyingRecordId(recordId);
      setErrorMessage("");

      await blockchainApi.verifyRecord(recordId);
      await loadBlockchainRecords();
    } catch (error) {
      setErrorMessage(
        error.response?.data?.message || "Failed to verify blockchain record.",
      );
    } finally {
      setVerifyingRecordId(null);
    }
  };

  const filteredRecords = useMemo(() => {
    return records.filter((record) => {
      const matchesStatus =
        statusFilter === "ALL" || record.status === statusFilter;

      const searchableText = [
        record.shipmentId,
        record.shipmentEventId,
        record.eventHash,
        record.transactionHash,
        record.blockNumber,
        record.status,
      ]
        .join(" ")
        .toLowerCase();

      const matchesSearch = searchableText.includes(searchTerm.toLowerCase());

      return matchesStatus && matchesSearch;
    });
  }, [records, searchTerm, statusFilter]);

  const summary = useMemo(() => {
    return {
      total: records.length,
      recorded: records.filter((record) => record.status === "RECORDED").length,
      pending: records.filter((record) => record.status === "PENDING").length,
      failed: records.filter((record) => record.status === "FAILED").length,
    };
  }, [records]);

  if (isLoading) {
    return <p>Loading blockchain monitor...</p>;
  }

  if (errorMessage) {
    return <p className="error-message">{errorMessage}</p>;
  }

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h2>Admin Blockchain Monitor</h2>
          <p>
            Monitor blockchain verification records for important shipment
            events.
          </p>
        </div>

        <button type="button" onClick={loadBlockchainRecords}>
          Refresh
        </button>
      </div>

      <div className="summary-grid blockchain-summary-grid">
        <div className="summary-card">
          <p className="summary-title">Total Records</p>
          <h2>{summary.total}</h2>
          <p className="summary-description">All blockchain proof records</p>
        </div>

        <div className="summary-card">
          <p className="summary-title">Recorded</p>
          <h2>{summary.recorded}</h2>
          <p className="summary-description">Successfully recorded proofs</p>
        </div>

        <div className="summary-card">
          <p className="summary-title">Pending</p>
          <h2>{summary.pending}</h2>
          <p className="summary-description">Awaiting confirmation</p>
        </div>

        <div className="summary-card">
          <p className="summary-title">Failed</p>
          <h2>{summary.failed}</h2>
          <p className="summary-description">Needs admin attention</p>
        </div>
      </div>

      <div className="panel">
        <div className="monitor-toolbar">
          <input
            type="text"
            placeholder="Search by shipment ID, event ID, hash, transaction, block..."
            value={searchTerm}
            onChange={(event) => setSearchTerm(event.target.value)}
          />

          <select
            value={statusFilter}
            onChange={(event) => setStatusFilter(event.target.value)}
          >
            <option value="ALL">All Statuses</option>
            <option value="RECORDED">Recorded</option>
            <option value="PENDING">Pending</option>
            <option value="FAILED">Failed</option>
          </select>
        </div>

        {filteredRecords.length === 0 ? (
          <div className="empty-state">
            <p>No blockchain records found.</p>
          </div>
        ) : (
          <div className="blockchain-monitor-list">
            {filteredRecords.map((record) => (
              <div key={record.id} className="blockchain-monitor-card">
                <div className="blockchain-monitor-card-header">
                  <div>
                    <strong>Record #{record.id}</strong>
                    <p>
                      Shipment #{record.shipmentId} · Event #
                      {record.shipmentEventId}
                    </p>
                  </div>

                  <span
                    className={`verification-badge ${record.status?.toLowerCase()}`}
                  >
                    {record.status}
                  </span>
                </div>

                <div className="blockchain-monitor-actions">
                  <button
                    type="button"
                    onClick={() => handleVerifyRecord(record.id)}
                    disabled={verifyingRecordId === record.id}
                  >
                    {verifyingRecordId === record.id
                      ? "Verifying..."
                      : "Verify Integrity"}
                  </button>
                </div>

                <div className="blockchain-monitor-meta">
                  <div>
                    <span>Block Number</span>
                    <strong>{record.blockNumber}</strong>
                  </div>

                  <div>
                    <span>Recorded At</span>
                    <strong>{record.recordedAt}</strong>
                  </div>
                </div>

                {record.verificationMessage && (
                  <div className="verification-message">
                    <strong>Verification Result:</strong>
                    <p>{record.verificationMessage}</p>
                    {record.verifiedAt && (
                      <small>Verified At: {record.verifiedAt}</small>
                    )}
                  </div>
                )}

                <div className="hash-section">
                  <small>Event Hash</small>
                  <code>{record.eventHash}</code>

                  <small>Transaction Hash</small>
                  <code>{record.transactionHash}</code>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

export default BlockchainMonitorPage;

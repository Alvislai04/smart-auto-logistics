import { useCallback, useEffect, useMemo, useState } from "react";
import { blockchainApi } from "../../api/blockchainApi";
import BlockchainHealthPanel from "../../components/blockchain/BlockchainHealthPanel";
import { useToast } from "../../components/feedback/ToastContext";

const RECORDS_PER_PAGE = 10;

function formatDateTime(value) {
  if (!value) {
    return "Not available";
  }

  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString();
}

function canRetryRecord(record) {
  return (
    record.status === "FAILED" &&
    !record.transactionHash &&
    record.blockNumber == null
  );
}

function canVerifyRecord(record) {
  return (
    record.status === "RECORDED" ||
    (record.status === "PENDING" && Boolean(record.transactionHash)) ||
    (record.status === "FAILED" && Boolean(record.transactionHash))
  );
}

function BlockchainMonitorPage() {
  const { showToast } = useToast();
  const [records, setRecords] = useState([]);
  const [health, setHealth] = useState(null);
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [currentPage, setCurrentPage] = useState(1);
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [activeAction, setActiveAction] = useState(null);

  const loadBlockchainMonitor = useCallback(async ({ initial = false } = {}) => {
    try {
      if (initial) {
        setIsLoading(true);
      } else {
        setIsRefreshing(true);
      }

      setErrorMessage("");
      const [recordData, healthData] = await Promise.all([
        blockchainApi.getAll(),
        blockchainApi.getHealth(),
      ]);

      setRecords(recordData);
      setHealth(healthData);
    } catch (error) {
      setErrorMessage(
        error.response?.data?.message || "Failed to load blockchain monitor.",
      );
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  }, []);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    loadBlockchainMonitor({ initial: true });
  }, [loadBlockchainMonitor]);

  const handleVerifyRecord = async (recordId) => {
    try {
      setActiveAction({ recordId, type: "VERIFY" });
      setErrorMessage("");

      const updatedRecord = await blockchainApi.verifyRecord(recordId);
      await loadBlockchainMonitor();

      showToast({
        type: updatedRecord.status === "RECORDED" ? "success" : "warning",
        title:
          updatedRecord.status === "RECORDED"
            ? "Integrity verified"
            : "Verification needs attention",
        message: updatedRecord.verificationMessage,
      });
    } catch (error) {
      const message =
        error.response?.data?.message || "Failed to verify blockchain record.";
      setErrorMessage(message);
      showToast({ type: "error", title: "Verification failed", message });
    } finally {
      setActiveAction(null);
    }
  };

  const handleRetryRecord = async (recordId) => {
    try {
      setActiveAction({ recordId, type: "RETRY" });
      setErrorMessage("");

      const updatedRecord = await blockchainApi.retryRecord(recordId);
      await loadBlockchainMonitor();

      showToast({
        type: updatedRecord.status === "RECORDED" ? "success" : "warning",
        title:
          updatedRecord.status === "RECORDED"
            ? "Blockchain retry completed"
            : "Blockchain retry unsuccessful",
        message: updatedRecord.verificationMessage,
      });
    } catch (error) {
      const message =
        error.response?.data?.message || "Failed to retry blockchain record.";
      setErrorMessage(message);
      showToast({ type: "error", title: "Retry blocked", message });
    } finally {
      setActiveAction(null);
    }
  };

  const filteredRecords = useMemo(() => {
    const normalizedSearchTerm = searchTerm.trim().toLowerCase();

    return records.filter((record) => {
      const matchesStatus =
        statusFilter === "ALL" || record.status === statusFilter;

      const searchableText = [
        record.id,
        record.shipmentId,
        record.shipmentEventId,
        record.eventHash,
        record.transactionHash,
        record.blockNumber,
        record.status,
      ]
        .filter((value) => value != null)
        .join(" ")
        .toLowerCase();

      return matchesStatus && searchableText.includes(normalizedSearchTerm);
    });
  }, [records, searchTerm, statusFilter]);

  const summary = useMemo(
    () => ({
      total: records.length,
      recorded: records.filter((record) => record.status === "RECORDED").length,
      pending: records.filter((record) => record.status === "PENDING").length,
      failed: records.filter((record) => record.status === "FAILED").length,
    }),
    [records],
  );

  const totalPages = Math.max(
    1,
    Math.ceil(filteredRecords.length / RECORDS_PER_PAGE),
  );
  const visiblePage = Math.min(currentPage, totalPages);
  const visibleRecords = filteredRecords.slice(
    (visiblePage - 1) * RECORDS_PER_PAGE,
    visiblePage * RECORDS_PER_PAGE,
  );
  const retryAvailable = health?.status === "HEALTHY";

  if (isLoading) {
    return <p>Loading blockchain monitor...</p>;
  }

  return (
    <div className="page blockchain-monitor-page">
      <div className="page-header">
        <div>
          <h2>Admin Blockchain Monitor</h2>
          <p>
            Review ledger availability, retry failed writes, and verify shipment
            event integrity.
          </p>
        </div>

        <button
          type="button"
          className="secondary-button"
          onClick={() => loadBlockchainMonitor()}
          disabled={isRefreshing || activeAction !== null}
        >
          {isRefreshing ? "Refreshing..." : "Refresh Monitor"}
        </button>
      </div>

      {errorMessage && <div className="error-message">{errorMessage}</div>}

      <BlockchainHealthPanel health={health} />

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

      <section className="panel">
        <div className="monitor-toolbar">
          <input
            type="search"
            aria-label="Search blockchain records"
            placeholder="Search record, shipment, event, hash, transaction, or block"
            value={searchTerm}
            onChange={(event) => {
              setSearchTerm(event.target.value);
              setCurrentPage(1);
            }}
          />

          <select
            aria-label="Filter blockchain records by status"
            value={statusFilter}
            onChange={(event) => {
              setStatusFilter(event.target.value);
              setCurrentPage(1);
            }}
          >
            <option value="ALL">All statuses</option>
            <option value="RECORDED">Recorded</option>
            <option value="PENDING">Pending</option>
            <option value="FAILED">Failed</option>
          </select>
        </div>

        {filteredRecords.length === 0 ? (
          <div className="empty-state">
            <p>No blockchain records match the current filters.</p>
          </div>
        ) : (
          <>
            <div className="blockchain-result-count">
              Showing {(visiblePage - 1) * RECORDS_PER_PAGE + 1}-
              {Math.min(
                visiblePage * RECORDS_PER_PAGE,
                filteredRecords.length,
              )} of {filteredRecords.length} records
            </div>

            <div className="blockchain-monitor-list">
              {visibleRecords.map((record) => {
              const isVerifying =
                activeAction?.recordId === record.id &&
                activeAction.type === "VERIFY";
              const isRetrying =
                activeAction?.recordId === record.id &&
                activeAction.type === "RETRY";

              return (
                <article key={record.id} className="blockchain-monitor-card">
                  <div className="blockchain-monitor-card-header">
                    <div>
                      <strong>Record #{record.id}</strong>
                      <p>
                        Shipment #{record.shipmentId} - Event #
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
                    {canRetryRecord(record) && (
                      <button
                        type="button"
                        onClick={() => handleRetryRecord(record.id)}
                        disabled={
                          !retryAvailable ||
                          activeAction !== null ||
                          isRefreshing
                        }
                        title={
                          retryAvailable
                            ? "Retry this failed blockchain write"
                            : "Retry becomes available when blockchain health is healthy"
                        }
                      >
                        {isRetrying ? "Retrying..." : "Retry Recording"}
                      </button>
                    )}

                    {canVerifyRecord(record) && (
                      <button
                        type="button"
                        className="secondary-button"
                        onClick={() => handleVerifyRecord(record.id)}
                        disabled={activeAction !== null || isRefreshing}
                      >
                        {isVerifying ? "Verifying..." : "Verify Integrity"}
                      </button>
                    )}
                  </div>

                  <div className="blockchain-monitor-meta">
                    <div>
                      <span>Block Number</span>
                      <strong>{record.blockNumber ?? "Not recorded"}</strong>
                    </div>
                    <div>
                      <span>Record Created</span>
                      <strong>{formatDateTime(record.recordedAt)}</strong>
                    </div>
                    <div>
                      <span>Retry Attempts</span>
                      <strong>{record.retryCount ?? 0}</strong>
                    </div>
                    <div>
                      <span>Last Retry</span>
                      <strong>{formatDateTime(record.lastRetryAt)}</strong>
                    </div>
                  </div>

                  {record.verificationMessage && (
                    <div className="verification-message">
                      <strong>Latest Result</strong>
                      <p>{record.verificationMessage}</p>
                      {record.verifiedAt && (
                        <small>
                          Verified at {formatDateTime(record.verifiedAt)}
                        </small>
                      )}
                    </div>
                  )}

                  <div className="hash-section">
                    <small>Event Hash</small>
                    <code>{record.eventHash}</code>

                    <small>Transaction Hash</small>
                    <code>{record.transactionHash ?? "Not recorded"}</code>
                  </div>
                </article>
              );
              })}
            </div>

            {totalPages > 1 && (
              <nav className="blockchain-pagination" aria-label="Blockchain records pages">
                <button
                  type="button"
                  className="secondary-button"
                  onClick={() => setCurrentPage((page) => Math.max(1, page - 1))}
                  disabled={visiblePage === 1 || activeAction !== null}
                >
                  Previous
                </button>
                <span>
                  Page {visiblePage} of {totalPages}
                </span>
                <button
                  type="button"
                  className="secondary-button"
                  onClick={() =>
                    setCurrentPage((page) => Math.min(totalPages, page + 1))
                  }
                  disabled={visiblePage === totalPages || activeAction !== null}
                >
                  Next
                </button>
              </nav>
            )}
          </>
        )}
      </section>
    </div>
  );
}

export default BlockchainMonitorPage;

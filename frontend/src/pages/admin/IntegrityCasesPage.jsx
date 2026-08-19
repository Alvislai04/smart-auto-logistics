import { useCallback, useEffect, useMemo, useState } from "react";
import { integrityCaseApi } from "../../api/integrityCaseApi";

const CASE_STATUSES = [
  "OPEN",
  "UNDER_INVESTIGATION",
  "COMPANY_NOTIFIED",
  "ESCALATED",
];

const RESOLUTION_TYPES = [
  "FALSE_POSITIVE",
  "LEGACY_TEST_RECORD",
  "DATA_CORRECTION_REQUIRED",
  "CONFIRMED_TAMPERING",
  "SYSTEM_ERROR",
  "OTHER",
];

function IntegrityCasesPage() {
  const [cases, setCases] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [selectedCaseId, setSelectedCaseId] = useState(null);
  const [investigationNotes, setInvestigationNotes] = useState("");
  const [resolutionType, setResolutionType] = useState("FALSE_POSITIVE");
  const [resolutionNotes, setResolutionNotes] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const loadIntegrityCases = useCallback(async () => {
    try {
      setIsLoading(true);
      setErrorMessage("");

      const data = await integrityCaseApi.getAll();
      setCases(data);
    } catch {
      setErrorMessage("Failed to load integrity cases.");
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    loadIntegrityCases();
  }, [loadIntegrityCases]);

  const selectedCase = useMemo(() => {
    return cases.find((integrityCase) => integrityCase.id === selectedCaseId);
  }, [cases, selectedCaseId]);

  const filteredCases = useMemo(() => {
    return cases.filter((integrityCase) => {
      const matchesStatus =
        statusFilter === "ALL" || integrityCase.status === statusFilter;

      const searchableText = [
        integrityCase.id,
        integrityCase.blockchainRecordId,
        integrityCase.shipmentId,
        integrityCase.shipmentEventId,
        integrityCase.status,
        integrityCase.resolutionType,
        integrityCase.title,
        integrityCase.description,
      ]
        .join(" ")
        .toLowerCase();

      const matchesSearch = searchableText.includes(searchTerm.toLowerCase());

      return matchesStatus && matchesSearch;
    });
  }, [cases, searchTerm, statusFilter]);

  const summary = useMemo(() => {
    return {
      total: cases.length,
      open: cases.filter((item) => item.status === "OPEN").length,
      investigating: cases.filter(
        (item) => item.status === "UNDER_INVESTIGATION",
      ).length,
      resolved: cases.filter((item) => item.status === "RESOLVED").length,
      escalated: cases.filter((item) => item.status === "ESCALATED").length,
    };
  }, [cases]);

  const handleSelectCase = (integrityCase) => {
    setSelectedCaseId(integrityCase.id);
    setInvestigationNotes(integrityCase.investigationNotes || "");
    setResolutionType(integrityCase.resolutionType || "FALSE_POSITIVE");
    setResolutionNotes(integrityCase.resolutionNotes || "");
  };

  const handleUpdateStatus = async (status) => {
    if (!selectedCase) {
      return;
    }

    try {
      setIsSaving(true);
      setErrorMessage("");

      await integrityCaseApi.updateStatus(selectedCase.id, {
        status,
        investigationNotes,
      });

      await loadIntegrityCases();
    } catch (error) {
      setErrorMessage(
        error.response?.data?.message || "Failed to update integrity case.",
      );
    } finally {
      setIsSaving(false);
    }
  };

  const handleResolveCase = async () => {
    if (!selectedCase) {
      return;
    }

    if (!resolutionNotes.trim()) {
      setErrorMessage("Resolution notes are required.");
      return;
    }

    try {
      setIsSaving(true);
      setErrorMessage("");

      await integrityCaseApi.resolveCase(selectedCase.id, {
        resolutionType,
        resolutionNotes,
      });

      setSelectedCaseId(null);
      setInvestigationNotes("");
      setResolutionNotes("");
      await loadIntegrityCases();
    } catch (error) {
      setErrorMessage(
        error.response?.data?.message || "Failed to resolve integrity case.",
      );
    } finally {
      setIsSaving(false);
    }
  };

  if (isLoading) {
    return <p>Loading integrity cases...</p>;
  }

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h2>Integrity Case Management</h2>
          <p>
            Investigate blockchain verification failures and record resolution
            outcomes.
          </p>
        </div>

        <button type="button" onClick={loadIntegrityCases}>
          Refresh
        </button>
      </div>

      {errorMessage && <p className="error-message">{errorMessage}</p>}

      <div className="summary-grid integrity-summary-grid">
        <div className="summary-card">
          <p className="summary-title">Total Cases</p>
          <h2>{summary.total}</h2>
          <p className="summary-description">All integrity cases</p>
        </div>

        <div className="summary-card">
          <p className="summary-title">Open</p>
          <h2>{summary.open}</h2>
          <p className="summary-description">Awaiting review</p>
        </div>

        <div className="summary-card">
          <p className="summary-title">Investigating</p>
          <h2>{summary.investigating}</h2>
          <p className="summary-description">Admin in progress</p>
        </div>

        <div className="summary-card">
          <p className="summary-title">Resolved</p>
          <h2>{summary.resolved}</h2>
          <p className="summary-description">Completed cases</p>
        </div>

        <div className="summary-card">
          <p className="summary-title">Escalated</p>
          <h2>{summary.escalated}</h2>
          <p className="summary-description">Needs further handling</p>
        </div>
      </div>

      <div className="integrity-layout">
        <div className="panel">
          <div className="integrity-toolbar">
            <input
              type="text"
              placeholder="Search case, shipment, event, blockchain record..."
              value={searchTerm}
              onChange={(event) => setSearchTerm(event.target.value)}
            />

            <select
              value={statusFilter}
              onChange={(event) => setStatusFilter(event.target.value)}
            >
              <option value="ALL">All Statuses</option>
              <option value="OPEN">Open</option>
              <option value="UNDER_INVESTIGATION">Under Investigation</option>
              <option value="COMPANY_NOTIFIED">Company Notified</option>
              <option value="RESOLVED">Resolved</option>
              <option value="ESCALATED">Escalated</option>
            </select>
          </div>

          {filteredCases.length === 0 ? (
            <div className="empty-state">
              <p>No integrity cases found.</p>
            </div>
          ) : (
            <div className="integrity-case-list">
              {filteredCases.map((integrityCase) => (
                <button
                  type="button"
                  key={integrityCase.id}
                  className={`integrity-case-card ${
                    selectedCaseId === integrityCase.id ? "active" : ""
                  }`}
                  onClick={() => handleSelectCase(integrityCase)}
                >
                  <div className="integrity-case-header">
                    <strong>Case #{integrityCase.id}</strong>
                    <span
                      className={`integrity-status ${integrityCase.status.toLowerCase()}`}
                    >
                      {integrityCase.status}
                    </span>
                  </div>

                  <p>{integrityCase.title}</p>

                  <small>
                    Shipment #{integrityCase.shipmentId} - Event #
                    {integrityCase.shipmentEventId} - Blockchain Record #
                    {integrityCase.blockchainRecordId}
                  </small>
                </button>
              ))}
            </div>
          )}
        </div>

        <div className="panel">
          {!selectedCase ? (
            <div className="empty-state">
              <p>Select an integrity case to investigate.</p>
            </div>
          ) : (
            <div className="integrity-detail">
              <div className="integrity-detail-header">
                <div>
                  <h3>Case #{selectedCase.id}</h3>
                  <p>{selectedCase.description}</p>
                </div>

                <span
                  className={`integrity-status ${selectedCase.status.toLowerCase()}`}
                >
                  {selectedCase.status}
                </span>
              </div>

              <div className="integrity-meta-grid">
                <div>
                  <span>Shipment</span>
                  <strong>#{selectedCase.shipmentId}</strong>
                </div>

                <div>
                  <span>Shipment Event</span>
                  <strong>#{selectedCase.shipmentEventId}</strong>
                </div>

                <div>
                  <span>Blockchain Record</span>
                  <strong>#{selectedCase.blockchainRecordId}</strong>
                </div>

                <div>
                  <span>Created At</span>
                  <strong>{selectedCase.createdAt}</strong>
                </div>
              </div>

              <label>
                Investigation Notes
                <textarea
                  value={investigationNotes}
                  onChange={(event) =>
                    setInvestigationNotes(event.target.value)
                  }
                  placeholder="Write what admin checked, contacted, or confirmed..."
                  disabled={selectedCase.status === "RESOLVED"}
                />
              </label>

              {selectedCase.status !== "RESOLVED" && (
                <div className="integrity-actions">
                  {CASE_STATUSES.map((status) => (
                    <button
                      type="button"
                      key={status}
                      disabled={isSaving}
                      onClick={() => handleUpdateStatus(status)}
                    >
                      Set {status.replaceAll("_", " ")}
                    </button>
                  ))}
                </div>
              )}

              <div className="resolve-section">
                <h4>Resolve Case</h4>

                <label>
                  Resolution Type
                  <select
                    value={resolutionType}
                    onChange={(event) => setResolutionType(event.target.value)}
                    disabled={selectedCase.status === "RESOLVED"}
                  >
                    {RESOLUTION_TYPES.map((type) => (
                      <option key={type} value={type}>
                        {type.replaceAll("_", " ")}
                      </option>
                    ))}
                  </select>
                </label>

                <label>
                  Resolution Notes
                  <textarea
                    value={resolutionNotes}
                    onChange={(event) => setResolutionNotes(event.target.value)}
                    placeholder="Example: Legacy testing data caused mismatch. No real tampering confirmed."
                    disabled={selectedCase.status === "RESOLVED"}
                  />
                </label>

                {selectedCase.status === "RESOLVED" ? (
                  <div className="resolved-message">
                    <strong>Case resolved</strong>
                    <p>{selectedCase.resolutionNotes}</p>
                    <small>Resolved At: {selectedCase.resolvedAt}</small>
                  </div>
                ) : (
                  <button
                    type="button"
                    onClick={handleResolveCase}
                    disabled={isSaving}
                  >
                    {isSaving ? "Saving..." : "Resolve Case"}
                  </button>
                )}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default IntegrityCasesPage;

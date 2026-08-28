import { useCallback, useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { stockMonitoringApi } from "../api/stockMonitoringApi";
import { useAuth } from "../auth/AuthContext";
import { useToast } from "../components/feedback/ToastContext";
import { alertCountRefreshEventName } from "../config/alertConfig";
import { formatDateTime, formatEnumLabel } from "../utils/formatters";

const EMPTY_FORM = {
  partyKey: "",
  vehicleModelCode: "",
  logisticsStage: "",
  minimumQuantity: "",
  active: true,
};

function StockMonitoringPage() {
  const { currentUser } = useAuth();
  const { showToast } = useToast();
  const [searchParams] = useSearchParams();
  const [overview, setOverview] = useState({
    rules: [],
    partyOptions: [],
    vehicleModels: [],
    logisticsStageOptions: [],
  });
  const [formData, setFormData] = useState(EMPTY_FORM);
  const [editingRuleId, setEditingRuleId] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const canManageRules = currentUser?.role === "ADMIN";
  const highlightedRuleId = searchParams.get("rule");

  const loadOverview = useCallback(async ({ background = false } = {}) => {
    try {
      if (!background) {
        setIsLoading(true);
      }
      setErrorMessage("");
      const data = await stockMonitoringApi.getOverview();
      setOverview({
        rules: data?.rules ?? [],
        partyOptions: data?.partyOptions ?? [],
        vehicleModels: data?.vehicleModels ?? [],
        logisticsStageOptions: data?.logisticsStageOptions ?? [],
      });
    } catch (error) {
      setErrorMessage(
        error?.response?.data?.message || "Failed to load stock monitoring.",
      );
    } finally {
      if (!background) {
        setIsLoading(false);
      }
    }
  }, []);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    loadOverview();
  }, [loadOverview]);

  useEffect(() => {
    if (!highlightedRuleId || isLoading) {
      return;
    }

    document
      .getElementById(`stock-rule-${highlightedRuleId}`)
      ?.scrollIntoView({ behavior: "smooth", block: "center" });
  }, [highlightedRuleId, isLoading]);

  const summary = useMemo(() => {
    const activeRules = overview.rules.filter((rule) => rule.active);
    return {
      total: overview.rules.length,
      low: activeRules.filter((rule) => rule.stockLevelStatus === "LOW").length,
      healthy: activeRules.filter(
        (rule) => rule.stockLevelStatus === "HEALTHY",
      ).length,
      inactive: overview.rules.filter((rule) => !rule.active).length,
    };
  }, [overview.rules]);

  const resetForm = () => {
    setFormData(EMPTY_FORM);
    setEditingRuleId(null);
  };

  const handleFieldChange = (event) => {
    const { name, value, type, checked } = event.target;
    setFormData((current) => ({
      ...current,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  const handleEdit = (rule) => {
    setEditingRuleId(rule.id);
    setFormData({
      partyKey: rule.partyKey,
      vehicleModelCode: rule.vehicleModelCode,
      logisticsStage: rule.logisticsStage,
      minimumQuantity: String(rule.minimumQuantity),
      active: rule.active,
    });
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    const minimumQuantity = Number(formData.minimumQuantity);
    if (!Number.isInteger(minimumQuantity) || minimumQuantity < 1) {
      setErrorMessage("Minimum quantity must be a whole number of at least 1.");
      return;
    }

    const payload = {
      ...formData,
      minimumQuantity,
    };

    try {
      setIsSaving(true);
      setErrorMessage("");

      if (editingRuleId) {
        await stockMonitoringApi.updateRule(editingRuleId, payload);
      } else {
        await stockMonitoringApi.createRule(payload);
      }

      showToast({
        type: "success",
        title: editingRuleId ? "Stock rule updated" : "Stock rule created",
        message: "The stock position and alert state were evaluated immediately.",
      });
      resetForm();
      await loadOverview({ background: true });
      window.dispatchEvent(new Event(alertCountRefreshEventName));
    } catch (error) {
      const message =
        error?.response?.data?.message || "Failed to save the stock rule.";
      setErrorMessage(message);
      showToast({
        type: "error",
        title: "Stock rule not saved",
        message,
      });
    } finally {
      setIsSaving(false);
    }
  };

  if (isLoading && overview.rules.length === 0) {
    return <p>Loading stock monitoring...</p>;
  }

  return (
    <div className="page stock-monitoring-page">
      <div className="page-header">
        <div>
          <h2>Stock Monitoring</h2>
          <p>
            Monitor vehicle quantities by company, model, and logistics stage.
          </p>
        </div>

        <button
          type="button"
          className="secondary-button"
          disabled={isLoading}
          onClick={() => loadOverview()}
        >
          {isLoading ? "Refreshing..." : "Refresh"}
        </button>
      </div>

      {errorMessage && <p className="error-message">{errorMessage}</p>}

      <div className="stock-summary-grid" aria-label="Stock monitoring summary">
        <div className="stock-summary-item">
          <span>Configured rules</span>
          <strong>{summary.total}</strong>
        </div>
        <div className="stock-summary-item low">
          <span>Low stock</span>
          <strong>{summary.low}</strong>
        </div>
        <div className="stock-summary-item healthy">
          <span>Healthy</span>
          <strong>{summary.healthy}</strong>
        </div>
        <div className="stock-summary-item inactive">
          <span>Inactive</span>
          <strong>{summary.inactive}</strong>
        </div>
      </div>

      {canManageRules && (
        <section className="panel stock-rule-form-panel">
          <div className="stock-section-heading">
            <div>
              <h3>{editingRuleId ? "Edit Stock Rule" : "Add Stock Rule"}</h3>
              <p>
                Quantities are calculated from non-cancelled shipment records.
              </p>
            </div>
          </div>

          <form className="stock-rule-form" onSubmit={handleSubmit}>
            <label>
              Company
              <select
                name="partyKey"
                value={formData.partyKey}
                disabled={isSaving}
                required
                onChange={handleFieldChange}
              >
                <option value="">Select company</option>
                {overview.partyOptions.map((party) => (
                  <option key={party.key} value={party.key}>
                    {party.name} ({formatEnumLabel(party.role)})
                  </option>
                ))}
              </select>
            </label>

            <label>
              Vehicle model
              <select
                name="vehicleModelCode"
                value={formData.vehicleModelCode}
                disabled={isSaving}
                required
                onChange={handleFieldChange}
              >
                <option value="">Select vehicle model</option>
                {overview.vehicleModels.map((model) => (
                  <option key={model.key} value={model.key}>
                    {model.brand} {model.displayName}
                  </option>
                ))}
              </select>
            </label>

            <label>
              Logistics stage
              <select
                name="logisticsStage"
                value={formData.logisticsStage}
                disabled={isSaving}
                required
                onChange={handleFieldChange}
              >
                <option value="">Select logistics stage</option>
                {overview.logisticsStageOptions.map((stage) => (
                  <option key={stage.key} value={stage.key}>
                    {stage.displayName}
                  </option>
                ))}
              </select>
            </label>

            <label>
              Minimum quantity
              <input
                type="number"
                name="minimumQuantity"
                min="1"
                step="1"
                value={formData.minimumQuantity}
                disabled={isSaving}
                required
                onChange={handleFieldChange}
              />
            </label>

            <label className="stock-active-toggle">
              <input
                type="checkbox"
                name="active"
                checked={formData.active}
                disabled={isSaving}
                onChange={handleFieldChange}
              />
              <span>Rule active</span>
            </label>

            <div className="stock-form-actions">
              {editingRuleId && (
                <button
                  type="button"
                  className="secondary-button"
                  disabled={isSaving}
                  onClick={resetForm}
                >
                  Cancel
                </button>
              )}
              <button type="submit" disabled={isSaving}>
                {isSaving
                  ? "Saving..."
                  : editingRuleId
                    ? "Update rule"
                    : "Create rule"}
              </button>
            </div>
          </form>
        </section>
      )}

      <section className="panel stock-rules-panel">
        <div className="stock-section-heading">
          <div>
            <h3>Live Stock Positions</h3>
            <p>Low-stock alerts clear automatically after the quantity recovers.</p>
          </div>
        </div>

        {overview.rules.length === 0 ? (
          <div className="stock-empty-state">
            <strong>No stock rules configured</strong>
            <p>
              {canManageRules
                ? "Create the first rule above to begin automatic monitoring."
                : "An administrator has not configured stock monitoring for your company yet."}
            </p>
          </div>
        ) : (
          <div className="table-scroll">
            <table className="stock-rules-table">
              <thead>
                <tr>
                  <th>Rule</th>
                  <th>Company</th>
                  <th>Vehicle model</th>
                  <th>Stage</th>
                  <th>Current</th>
                  <th>Minimum</th>
                  <th>Status</th>
                  <th>Last checked</th>
                  {canManageRules && <th>Action</th>}
                </tr>
              </thead>
              <tbody>
                {overview.rules.map((rule) => (
                  <tr
                    id={`stock-rule-${rule.id}`}
                    key={rule.id}
                    className={
                      String(rule.id) === highlightedRuleId
                        ? "stock-rule-highlighted"
                        : undefined
                    }
                  >
                    <td>
                      <strong className="stock-rule-key">{rule.ruleKey}</strong>
                    </td>
                    <td>
                      <strong>{rule.companyName}</strong>
                      <span className="stock-table-subtext">
                        {formatEnumLabel(rule.partyRole)}
                      </span>
                    </td>
                    <td>
                      <strong>{rule.vehicleModelName}</strong>
                      <span className="stock-table-subtext">
                        {rule.vehicleModelCode}
                      </span>
                    </td>
                    <td>{formatEnumLabel(rule.logisticsStage)}</td>
                    <td className="stock-quantity-cell">{rule.currentQuantity}</td>
                    <td>{rule.minimumQuantity}</td>
                    <td>
                      <span
                        className={`stock-level-badge ${rule.stockLevelStatus.toLowerCase()}`}
                      >
                        {formatEnumLabel(rule.stockLevelStatus)}
                      </span>
                    </td>
                    <td>{formatDateTime(rule.lastEvaluatedAt)}</td>
                    {canManageRules && (
                      <td>
                        <button
                          type="button"
                          className="table-action-button"
                          disabled={isSaving}
                          onClick={() => handleEdit(rule)}
                        >
                          Edit
                        </button>
                      </td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
}

export default StockMonitoringPage;

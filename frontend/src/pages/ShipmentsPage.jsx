import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { shipmentApi } from "../api/shipmentApi";
import { useAuth } from "../auth/AuthContext";
import { formatDateTime, formatEnumLabel } from "../utils/formatters";

const shipmentStatuses = [
  "CREATED",
  "READY_FOR_DISPATCH",
  "PICKED_UP",
  "IN_TRANSIT",
  "DELAYED",
  "ARRIVED",
  "DELIVERED",
  "CANCELLED",
];

const logisticsStages = [
  "MANUFACTURING",
  "STOCK_YARD",
  "WORK_IN_PROGRESS",
  "READY_FOR_DISPATCH",
  "IN_TRANSIT",
  "ARRIVED_AT_DEALERSHIP",
  "DELIVERED",
];

const pageSizeOptions = [5, 10, 20, 50];

const defaultQuery = {
  search: "",
  status: "ALL",
  stage: "ALL",
  page: 0,
  size: 10,
};

const defaultPageInfo = {
  page: 0,
  size: 10,
  totalPages: 0,
  totalElements: 0,
  numberOfElements: 0,
  first: true,
  last: true,
};

function buildShipmentQueryParams(query) {
  const params = {
    page: query.page,
    size: query.size,
    sortBy: "createdAt",
    sortDirection: "DESC",
  };

  if (query.search.trim()) {
    params.search = query.search.trim();
  }

  if (query.status !== "ALL") {
    params.status = query.status;
  }

  if (query.stage !== "ALL") {
    params.stage = query.stage;
  }

  return params;
}

function normalizePageResponse(data) {
  if (Array.isArray(data)) {
    return {
      ...defaultPageInfo,
      content: data,
      size: data.length,
      totalElements: data.length,
      numberOfElements: data.length,
      totalPages: data.length > 0 ? 1 : 0,
    };
  }

  return {
    ...defaultPageInfo,
    ...data,
    content: data.content ?? [],
  };
}

function ShipmentsPage() {
  const { currentUser } = useAuth();
  const [shipments, setShipments] = useState([]);
  const [query, setQuery] = useState(defaultQuery);
  const [searchInput, setSearchInput] = useState(defaultQuery.search);
  const [pageInfo, setPageInfo] = useState(defaultPageInfo);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");

  const loadShipments = useCallback(async () => {
    try {
      setIsLoading(true);
      setErrorMessage("");

      const data = await shipmentApi.getAll(buildShipmentQueryParams(query));
      const pageData = normalizePageResponse(data);

      setShipments(pageData.content);
      setPageInfo({
        page: pageData.page,
        size: pageData.size,
        totalPages: pageData.totalPages,
        totalElements: pageData.totalElements,
        numberOfElements: pageData.numberOfElements,
        first: pageData.first,
        last: pageData.last,
      });
    } catch {
      setErrorMessage("Failed to load shipments.");
    } finally {
      setIsLoading(false);
    }
  }, [query]);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    loadShipments();
  }, [loadShipments]);

  const handleSearchSubmit = (event) => {
    event.preventDefault();

    setQuery((previousQuery) => ({
      ...previousQuery,
      search: searchInput.trim(),
      page: 0,
    }));
  };

  const handleFilterChange = (event) => {
    const { name, value } = event.target;

    setQuery((previousQuery) => ({
      ...previousQuery,
      [name]: name === "size" ? Number(value) : value,
      page: 0,
    }));
  };

  const handleResetFilters = () => {
    setSearchInput("");
    setQuery({ ...defaultQuery });
  };

  const handlePageChange = (nextPage) => {
    setQuery((previousQuery) => ({
      ...previousQuery,
      page: Math.max(0, Math.min(nextPage, Math.max(pageInfo.totalPages - 1, 0))),
    }));
  };

  const firstVisibleItem =
    pageInfo.totalElements === 0 ? 0 : pageInfo.page * pageInfo.size + 1;
  const lastVisibleItem =
    firstVisibleItem === 0
      ? 0
      : firstVisibleItem + pageInfo.numberOfElements - 1;
  const currentPageLabel =
    pageInfo.totalPages === 0 ? 0 : pageInfo.page + 1;
  const hasActiveFilters =
    query.search !== "" || query.status !== "ALL" || query.stage !== "ALL";
  const isInitialLoading = isLoading && shipments.length === 0;

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h2>Shipments</h2>
          <p>Search, filter, and review vehicle shipment records.</p>
        </div>

        <div className="header-actions">
          {currentUser?.role === "MANUFACTURER" && (
            <Link className="button-link" to="/shipments/create">
              Create Shipment
            </Link>
          )}

          <button type="button" disabled={isLoading} onClick={loadShipments}>
            {isLoading ? "Refreshing..." : "Refresh"}
          </button>
        </div>
      </div>

      {errorMessage && <p className="error-message">{errorMessage}</p>}

      <div className="shipment-list-toolbar">
        <form className="shipment-filter-form" onSubmit={handleSearchSubmit}>
          <label className="shipment-search-field">
            Search
            <input
              type="search"
              value={searchInput}
              onChange={(event) => setSearchInput(event.target.value)}
              placeholder="Code, VIN, vehicle, route, owner, customer"
            />
          </label>

          <label>
            Status
            <select
              name="status"
              value={query.status}
              onChange={handleFilterChange}
            >
              <option value="ALL">All statuses</option>
              {shipmentStatuses.map((status) => (
                <option key={status} value={status}>
                  {formatEnumLabel(status)}
                </option>
              ))}
            </select>
          </label>

          <label>
            Stage
            <select name="stage" value={query.stage} onChange={handleFilterChange}>
              <option value="ALL">All stages</option>
              {logisticsStages.map((stage) => (
                <option key={stage} value={stage}>
                  {formatEnumLabel(stage)}
                </option>
              ))}
            </select>
          </label>

          <label>
            Rows
            <select name="size" value={query.size} onChange={handleFilterChange}>
              {pageSizeOptions.map((size) => (
                <option key={size} value={size}>
                  {size}
                </option>
              ))}
            </select>
          </label>

          <div className="shipment-filter-actions">
            <button type="submit" disabled={isLoading}>
              Search
            </button>
            <button
              type="button"
              className="secondary-button"
              disabled={isLoading || (!hasActiveFilters && searchInput === "")}
              onClick={handleResetFilters}
            >
              Reset
            </button>
          </div>
        </form>
      </div>

      <div className="shipment-result-meta">
        <span>
          Showing {firstVisibleItem}-{lastVisibleItem} of{" "}
          {pageInfo.totalElements} shipments
        </span>
        {hasActiveFilters && <strong>Filters active</strong>}
      </div>

      {isInitialLoading ? (
        <p>Loading shipments...</p>
      ) : (
        <div className="table-card">
          {isLoading && (
            <p className="table-status-note">Refreshing shipments...</p>
          )}

          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Shipment Code</th>
                <th>Vehicle</th>
                <th>Route</th>
                <th>Stage</th>
                <th>Status</th>
                <th>Schedule</th>
                <th>Owner</th>
                <th>Action</th>
              </tr>
            </thead>

            <tbody>
              {shipments.map((shipment) => (
                <tr key={shipment.id}>
                  <td>{shipment.id}</td>
                  <td>{shipment.shipmentCode}</td>
                  <td>
                    {shipment.vehicleModel}
                    <br />
                    <small>{shipment.vehicleVin}</small>
                  </td>
                  <td>
                    {shipment.origin}
                    <br />
                    <small>{shipment.destination}</small>
                  </td>
                  <td>{formatEnumLabel(shipment.currentStage)}</td>
                  <td>
                    <span className={`status ${shipment.status?.toLowerCase()}`}>
                      {formatEnumLabel(shipment.status)}
                    </span>
                  </td>
                  <td>
                    ETD {formatDateTime(shipment.estimatedDeparture)}
                    <br />
                    <small>
                      ETA {formatDateTime(shipment.estimatedArrival)}
                    </small>
                  </td>
                  <td>
                    {shipment.responsiblePerson || "-"}
                    <br />
                    <small>{shipment.stockLocation || "-"}</small>
                  </td>
                  <td>
                    <Link to={`/shipments/${shipment.id}`}>View</Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {shipments.length === 0 && !isLoading && (
            <p>No shipments match the current filters.</p>
          )}
        </div>
      )}

      <div className="pagination-bar">
        <span>
          Page {currentPageLabel} of {pageInfo.totalPages}
        </span>

        <div className="pagination-actions">
          <button
            type="button"
            className="secondary-button"
            disabled={isLoading || pageInfo.first || pageInfo.totalPages === 0}
            onClick={() => handlePageChange(pageInfo.page - 1)}
          >
            Previous
          </button>
          <button
            type="button"
            className="secondary-button"
            disabled={isLoading || pageInfo.last || pageInfo.totalPages === 0}
            onClick={() => handlePageChange(pageInfo.page + 1)}
          >
            Next
          </button>
        </div>
      </div>
    </div>
  );
}

export default ShipmentsPage;

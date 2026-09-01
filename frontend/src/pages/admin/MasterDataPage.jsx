import { useCallback, useEffect, useMemo, useState } from "react";
import { masterDataApi } from "../../api/masterDataApi";
import {
  formatCoordinate,
  formatDateTime,
  formatEnumLabel,
} from "../../utils/formatters";

const MASTER_DATA_TABS = [
  {
    key: "vehicleModels",
    label: "Vehicle Models",
    description: "Models available in manufacturer shipment forms.",
  },
  {
    key: "partyProfiles",
    label: "Shipment Parties",
    description: "Manufacturers, logistics providers, and dealerships.",
  },
  {
    key: "simulationProfiles",
    label: "Simulation Profiles",
    description: "Weighted scenarios used by system-managed telemetry.",
  },
];

const emptyVehicleModelForm = {
  id: null,
  modelCode: "",
  brand: "",
  displayName: "",
  active: true,
  sortOrder: "0",
};

const emptyPartyProfileForm = {
  id: null,
  partyKey: "",
  partyRole: "",
  companyName: "",
  operatingLocation: "",
  locationLatitude: "",
  locationLongitude: "",
  active: true,
  sortOrder: "0",
};

const emptySimulationProfileForm = {
  id: null,
  profileKey: "",
  displayName: "",
  conditionStatus: "",
  minSpeed: "",
  maxSpeed: "",
  weight: "1",
  routeDeviationEnabled: false,
  routeDeviationLatitudeOffset: "0",
  active: true,
  sortOrder: "0",
};

const emptyOverview = {
  vehicleModels: [],
  partyProfiles: [],
  simulationProfiles: [],
  partyRoleOptions: [],
  conditionStatusOptions: [],
};

function toFormValue(value) {
  return value === null || value === undefined ? "" : String(value);
}

function toNumberOrNull(value) {
  if (value === "" || value === null || value === undefined) {
    return null;
  }

  return Number(value);
}

function toIntegerOrNull(value) {
  const numericValue = toNumberOrNull(value);

  if (numericValue === null) {
    return null;
  }

  return Number.parseInt(numericValue, 10);
}

function getErrorMessage(error, fallbackMessage) {
  return error.response?.data?.message || fallbackMessage;
}

function matchesSearch(values, searchTerm) {
  const normalizedSearch = searchTerm.trim().toLowerCase();

  if (!normalizedSearch) {
    return true;
  }

  return values
    .join(" ")
    .toLowerCase()
    .includes(normalizedSearch);
}

function buildVehicleModelPayload(formData) {
  return {
    modelCode: formData.modelCode.trim(),
    brand: formData.brand.trim(),
    displayName: formData.displayName.trim(),
    active: formData.active,
    sortOrder: toIntegerOrNull(formData.sortOrder),
  };
}

function buildPartyProfilePayload(formData) {
  return {
    partyKey: formData.partyKey.trim(),
    partyRole: formData.partyRole,
    companyName: formData.companyName.trim(),
    operatingLocation: formData.operatingLocation.trim(),
    locationLatitude: toNumberOrNull(formData.locationLatitude),
    locationLongitude: toNumberOrNull(formData.locationLongitude),
    active: formData.active,
    sortOrder: toIntegerOrNull(formData.sortOrder),
  };
}

function buildSimulationProfilePayload(formData) {
  return {
    profileKey: formData.profileKey.trim(),
    displayName: formData.displayName.trim(),
    conditionStatus: formData.conditionStatus,
    minSpeed: toNumberOrNull(formData.minSpeed),
    maxSpeed: toNumberOrNull(formData.maxSpeed),
    weight: toIntegerOrNull(formData.weight),
    routeDeviationEnabled: formData.routeDeviationEnabled,
    routeDeviationLatitudeOffset: toNumberOrNull(
      formData.routeDeviationLatitudeOffset,
    ),
    active: formData.active,
    sortOrder: toIntegerOrNull(formData.sortOrder),
  };
}

function MasterDataPage() {
  const [activeTab, setActiveTab] = useState("vehicleModels");
  const [overview, setOverview] = useState(emptyOverview);
  const [vehicleModelForm, setVehicleModelForm] = useState(emptyVehicleModelForm);
  const [partyProfileForm, setPartyProfileForm] = useState(emptyPartyProfileForm);
  const [simulationProfileForm, setSimulationProfileForm] = useState(
    emptySimulationProfileForm,
  );
  const [searchTerm, setSearchTerm] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  const activeTabConfig = MASTER_DATA_TABS.find((tab) => tab.key === activeTab);

  const loadMasterData = useCallback(async () => {
    try {
      setIsLoading(true);
      setErrorMessage("");

      const data = await masterDataApi.getOverview();
      setOverview({
        vehicleModels: data.vehicleModels ?? [],
        partyProfiles: data.partyProfiles ?? [],
        simulationProfiles: data.simulationProfiles ?? [],
        partyRoleOptions: data.partyRoleOptions ?? [],
        conditionStatusOptions: data.conditionStatusOptions ?? [],
      });
    } catch (error) {
      setErrorMessage(
        getErrorMessage(error, "Failed to load master data configuration."),
      );
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    loadMasterData();
  }, [loadMasterData]);

  const filteredVehicleModels = useMemo(
    () =>
      overview.vehicleModels.filter((model) =>
        matchesSearch(
          [model.modelCode, model.brand, model.displayName, model.active],
          searchTerm,
        ),
      ),
    [overview.vehicleModels, searchTerm],
  );

  const filteredPartyProfiles = useMemo(
    () =>
      overview.partyProfiles.filter((profile) =>
        matchesSearch(
          [
            profile.partyKey,
            profile.partyRole,
            profile.companyName,
            profile.operatingLocation,
            profile.active,
          ],
          searchTerm,
        ),
      ),
    [overview.partyProfiles, searchTerm],
  );

  const filteredSimulationProfiles = useMemo(
    () =>
      overview.simulationProfiles.filter((profile) =>
        matchesSearch(
          [
            profile.profileKey,
            profile.displayName,
            profile.conditionStatus,
            profile.active,
          ],
          searchTerm,
        ),
      ),
    [overview.simulationProfiles, searchTerm],
  );

  const handleTabChange = (tabKey) => {
    setActiveTab(tabKey);
    setSearchTerm("");
    setErrorMessage("");
    setSuccessMessage("");
  };

  const handleVehicleModelChange = (event) => {
    const { name, value, type, checked } = event.target;

    setVehicleModelForm((previousForm) => ({
      ...previousForm,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  const handlePartyProfileChange = (event) => {
    const { name, value, type, checked } = event.target;

    setPartyProfileForm((previousForm) => ({
      ...previousForm,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  const handleSimulationProfileChange = (event) => {
    const { name, value, type, checked } = event.target;

    setSimulationProfileForm((previousForm) => ({
      ...previousForm,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  const resetActiveForm = () => {
    if (activeTab === "vehicleModels") {
      setVehicleModelForm(emptyVehicleModelForm);
    }

    if (activeTab === "partyProfiles") {
      setPartyProfileForm(emptyPartyProfileForm);
    }

    if (activeTab === "simulationProfiles") {
      setSimulationProfileForm(emptySimulationProfileForm);
    }

    setErrorMessage("");
    setSuccessMessage("");
  };

  const handleVehicleModelEdit = (model) => {
    setVehicleModelForm({
      id: model.id,
      modelCode: model.modelCode ?? "",
      brand: model.brand ?? "",
      displayName: model.displayName ?? "",
      active: model.active,
      sortOrder: toFormValue(model.sortOrder),
    });
    setErrorMessage("");
    setSuccessMessage("");
  };

  const handlePartyProfileEdit = (profile) => {
    setPartyProfileForm({
      id: profile.id,
      partyKey: profile.partyKey ?? "",
      partyRole: profile.partyRole ?? "",
      companyName: profile.companyName ?? "",
      operatingLocation: profile.operatingLocation ?? "",
      locationLatitude: toFormValue(profile.locationLatitude),
      locationLongitude: toFormValue(profile.locationLongitude),
      active: profile.active,
      sortOrder: toFormValue(profile.sortOrder),
    });
    setErrorMessage("");
    setSuccessMessage("");
  };

  const handleSimulationProfileEdit = (profile) => {
    setSimulationProfileForm({
      id: profile.id,
      profileKey: profile.profileKey ?? "",
      displayName: profile.displayName ?? "",
      conditionStatus: profile.conditionStatus ?? "",
      minSpeed: toFormValue(profile.minSpeed),
      maxSpeed: toFormValue(profile.maxSpeed),
      weight: toFormValue(profile.weight),
      routeDeviationEnabled: Boolean(profile.routeDeviationEnabled),
      routeDeviationLatitudeOffset: toFormValue(
        profile.routeDeviationLatitudeOffset,
      ),
      active: Boolean(profile.active),
      sortOrder: toFormValue(profile.sortOrder),
    });
    setErrorMessage("");
    setSuccessMessage("");
  };

  const handleVehicleModelSubmit = async () => {
    const payload = buildVehicleModelPayload(vehicleModelForm);

    if (vehicleModelForm.id) {
      await masterDataApi.updateVehicleModel(vehicleModelForm.id, payload);
      setSuccessMessage("Vehicle model updated.");
    } else {
      await masterDataApi.createVehicleModel(payload);
      setVehicleModelForm(emptyVehicleModelForm);
      setSuccessMessage("Vehicle model created.");
    }
  };

  const handlePartyProfileSubmit = async () => {
    const payload = buildPartyProfilePayload(partyProfileForm);

    if (partyProfileForm.id) {
      await masterDataApi.updatePartyProfile(partyProfileForm.id, payload);
      setSuccessMessage("Shipment party profile updated.");
    } else {
      await masterDataApi.createPartyProfile(payload);
      setPartyProfileForm(emptyPartyProfileForm);
      setSuccessMessage("Shipment party profile created.");
    }
  };

  const handleSimulationProfileSubmit = async () => {
    const payload = buildSimulationProfilePayload(simulationProfileForm);

    if (simulationProfileForm.id) {
      await masterDataApi.updateSimulationProfile(
        simulationProfileForm.id,
        payload,
      );
      setSuccessMessage("Simulation profile updated.");
    } else {
      await masterDataApi.createSimulationProfile(payload);
      setSimulationProfileForm(emptySimulationProfileForm);
      setSuccessMessage("Simulation profile created.");
    }
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    try {
      setIsSaving(true);
      setErrorMessage("");
      setSuccessMessage("");

      if (activeTab === "vehicleModels") {
        await handleVehicleModelSubmit();
      }

      if (activeTab === "partyProfiles") {
        await handlePartyProfileSubmit();
      }

      if (activeTab === "simulationProfiles") {
        await handleSimulationProfileSubmit();
      }

      await loadMasterData();
    } catch (error) {
      setErrorMessage(getErrorMessage(error, "Failed to save master data."));
    } finally {
      setIsSaving(false);
    }
  };

  const renderActiveBadge = (active) => (
    <span className={`master-data-badge ${active ? "active" : "inactive"}`}>
      {active ? "Active" : "Inactive"}
    </span>
  );

  const renderVehicleModelForm = () => {
    const isEditing = Boolean(vehicleModelForm.id);

    return (
      <form className="form-panel master-data-form" onSubmit={handleSubmit}>
        <div className="form-section-header">
          <h3>{isEditing ? "Edit Vehicle Model" : "Add Vehicle Model"}</h3>
        </div>

        <div className="form-grid">
          <label>
            Model Code
            <input
              className={isEditing ? "readonly-input" : ""}
              type="text"
              name="modelCode"
              value={vehicleModelForm.modelCode}
              onChange={handleVehicleModelChange}
              placeholder="Example: MB-EQC-400"
              readOnly={isEditing}
              required
            />
          </label>

          <label>
            Brand
            <input
              type="text"
              name="brand"
              value={vehicleModelForm.brand}
              onChange={handleVehicleModelChange}
              placeholder="Example: Mercedes-Benz"
              required
            />
          </label>

          <label>
            Display Name
            <input
              type="text"
              name="displayName"
              value={vehicleModelForm.displayName}
              onChange={handleVehicleModelChange}
              placeholder="Example: EQC 400 4MATIC"
              required
            />
          </label>

          <label>
            Sort Order
            <input
              type="number"
              name="sortOrder"
              value={vehicleModelForm.sortOrder}
              onChange={handleVehicleModelChange}
              min="0"
              max="100000"
              required
            />
          </label>

          <label className="checkbox-field master-data-active-field">
            <input
              type="checkbox"
              name="active"
              checked={vehicleModelForm.active}
              onChange={handleVehicleModelChange}
            />
            Active for shipment forms
          </label>
        </div>

        <div className="form-actions">
          <button type="button" className="secondary-button" onClick={resetActiveForm}>
            Clear
          </button>
          <button type="submit" disabled={isSaving}>
            {isSaving ? "Saving..." : isEditing ? "Save Changes" : "Create Model"}
          </button>
        </div>
      </form>
    );
  };

  const renderPartyProfileForm = () => {
    const isEditing = Boolean(partyProfileForm.id);

    return (
      <form className="form-panel master-data-form" onSubmit={handleSubmit}>
        <div className="form-section-header">
          <h3>{isEditing ? "Edit Shipment Party" : "Add Shipment Party"}</h3>
        </div>

        <div className="form-grid">
          <label>
            Party Key
            <input
              className={isEditing ? "readonly-input" : ""}
              type="text"
              name="partyKey"
              value={partyProfileForm.partyKey}
              onChange={handlePartyProfileChange}
              placeholder="Example: MFG-PEKAN-01"
              readOnly={isEditing}
              required
            />
          </label>

          <label>
            Party Role
            <select
              name="partyRole"
              value={partyProfileForm.partyRole}
              onChange={handlePartyProfileChange}
              required
            >
              <option value="">Select party role</option>
              {overview.partyRoleOptions.map((roleOption) => (
                <option key={roleOption.key} value={roleOption.key}>
                  {roleOption.displayName}
                </option>
              ))}
            </select>
          </label>

          <label>
            Company Name
            <input
              type="text"
              name="companyName"
              value={partyProfileForm.companyName}
              onChange={handlePartyProfileChange}
              placeholder="Example: Mercedes-Benz Malaysia"
              required
            />
          </label>

          <label className="form-field-full">
            Operating Location
            <input
              type="text"
              name="operatingLocation"
              value={partyProfileForm.operatingLocation}
              onChange={handlePartyProfileChange}
              placeholder="Example: Pekan Assembly Plant, Pahang"
              required
            />
          </label>

          <label>
            Latitude
            <input
              type="number"
              name="locationLatitude"
              value={partyProfileForm.locationLatitude}
              onChange={handlePartyProfileChange}
              min="-90"
              max="90"
              step="0.000001"
              placeholder="Example: 3.511300"
              required
            />
          </label>

          <label>
            Longitude
            <input
              type="number"
              name="locationLongitude"
              value={partyProfileForm.locationLongitude}
              onChange={handlePartyProfileChange}
              min="-180"
              max="180"
              step="0.000001"
              placeholder="Example: 103.397400"
              required
            />
          </label>

          <label>
            Sort Order
            <input
              type="number"
              name="sortOrder"
              value={partyProfileForm.sortOrder}
              onChange={handlePartyProfileChange}
              min="0"
              max="100000"
              required
            />
          </label>

          <label className="checkbox-field master-data-active-field">
            <input
              type="checkbox"
              name="active"
              checked={partyProfileForm.active}
              onChange={handlePartyProfileChange}
            />
            Active for operational selection
          </label>
        </div>

        <div className="form-actions">
          <button type="button" className="secondary-button" onClick={resetActiveForm}>
            Clear
          </button>
          <button
            type="submit"
            disabled={isSaving || overview.partyRoleOptions.length === 0}
          >
            {isSaving ? "Saving..." : isEditing ? "Save Changes" : "Create Party"}
          </button>
        </div>
      </form>
    );
  };

  const renderSimulationProfileForm = () => {
    const isEditing = Boolean(simulationProfileForm.id);

    return (
      <form className="form-panel master-data-form" onSubmit={handleSubmit}>
        <div className="form-section-header">
          <h3>
            {isEditing ? "Edit Simulation Profile" : "Add Simulation Profile"}
          </h3>
        </div>

        <div className="form-grid">
          <label>
            Profile Key
            <input
              className={isEditing ? "readonly-input" : ""}
              type="text"
              name="profileKey"
              value={simulationProfileForm.profileKey}
              onChange={handleSimulationProfileChange}
              placeholder="Example: FAST-CLEAR-RUN"
              readOnly={isEditing}
              required
            />
          </label>

          <label>
            Display Name
            <input
              type="text"
              name="displayName"
              value={simulationProfileForm.displayName}
              onChange={handleSimulationProfileChange}
              placeholder="Example: Fast clear run"
              required
            />
          </label>

          <label>
            Condition Status
            <select
              name="conditionStatus"
              value={simulationProfileForm.conditionStatus}
              onChange={handleSimulationProfileChange}
              required
            >
              <option value="">Select condition</option>
              {overview.conditionStatusOptions.map((conditionOption) => (
                <option key={conditionOption.key} value={conditionOption.key}>
                  {conditionOption.displayName}
                </option>
              ))}
            </select>
          </label>

          <label>
            Minimum Speed
            <input
              type="number"
              name="minSpeed"
              value={simulationProfileForm.minSpeed}
              onChange={handleSimulationProfileChange}
              min="0"
              max="250"
              step="0.1"
              placeholder="Example: 45"
              required
            />
          </label>

          <label>
            Maximum Speed
            <input
              type="number"
              name="maxSpeed"
              value={simulationProfileForm.maxSpeed}
              onChange={handleSimulationProfileChange}
              min="0"
              max="250"
              step="0.1"
              placeholder="Example: 95"
              required
            />
          </label>

          <label>
            Weight
            <input
              type="number"
              name="weight"
              value={simulationProfileForm.weight}
              onChange={handleSimulationProfileChange}
              min="1"
              max="1000"
              required
            />
          </label>

          <label>
            Route Deviation Offset (Degrees)
            <input
              type="number"
              name="routeDeviationLatitudeOffset"
              value={simulationProfileForm.routeDeviationLatitudeOffset}
              onChange={handleSimulationProfileChange}
              min="-0.1"
              max="0.1"
              step="0.000001"
              required
            />
          </label>

          <label>
            Sort Order
            <input
              type="number"
              name="sortOrder"
              value={simulationProfileForm.sortOrder}
              onChange={handleSimulationProfileChange}
              min="0"
              max="100000"
              required
            />
          </label>

          <label className="checkbox-field master-data-active-field">
            <input
              type="checkbox"
              name="routeDeviationEnabled"
              checked={simulationProfileForm.routeDeviationEnabled}
              onChange={handleSimulationProfileChange}
            />
            Apply route deviation offset
          </label>

          <label className="checkbox-field master-data-active-field">
            <input
              type="checkbox"
              name="active"
              checked={simulationProfileForm.active}
              onChange={handleSimulationProfileChange}
            />
            Active for random simulation
          </label>
        </div>

        <div className="form-actions">
          <button type="button" className="secondary-button" onClick={resetActiveForm}>
            Clear
          </button>
          <button
            type="submit"
            disabled={isSaving || overview.conditionStatusOptions.length === 0}
          >
            {isSaving
              ? "Saving..."
              : isEditing
                ? "Save Changes"
                : "Create Profile"}
          </button>
        </div>
      </form>
    );
  };

  const renderVehicleModelsTable = () => (
    <div className="table-card master-data-table">
      <table>
        <thead>
          <tr>
            <th>Model</th>
            <th>Brand</th>
            <th>Status</th>
            <th>Sort</th>
            <th>Updated</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          {filteredVehicleModels.map((model) => (
            <tr key={model.id}>
              <td>
                <strong>{model.displayName}</strong>
                <small>{model.modelCode}</small>
              </td>
              <td>{model.brand}</td>
              <td>{renderActiveBadge(model.active)}</td>
              <td>{model.sortOrder}</td>
              <td>{formatDateTime(model.updatedAt)}</td>
              <td>
                <button type="button" onClick={() => handleVehicleModelEdit(model)}>
                  Edit
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {filteredVehicleModels.length === 0 && (
        <div className="empty-state">
          <p>No vehicle models found.</p>
        </div>
      )}
    </div>
  );

  const renderPartyProfilesTable = () => (
    <div className="table-card master-data-table">
      <table>
        <thead>
          <tr>
            <th>Company</th>
            <th>Role</th>
            <th>Location</th>
            <th>Status</th>
            <th>Sort</th>
            <th>Updated</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          {filteredPartyProfiles.map((profile) => (
            <tr key={profile.id}>
              <td>
                <strong>{profile.companyName}</strong>
                <small>{profile.partyKey}</small>
              </td>
              <td>{formatEnumLabel(profile.partyRole)}</td>
              <td>
                {profile.operatingLocation}
                <small>
                  {formatCoordinate(profile.locationLatitude)},{" "}
                  {formatCoordinate(profile.locationLongitude)}
                </small>
              </td>
              <td>{renderActiveBadge(profile.active)}</td>
              <td>{profile.sortOrder}</td>
              <td>{formatDateTime(profile.updatedAt)}</td>
              <td>
                <button
                  type="button"
                  onClick={() => handlePartyProfileEdit(profile)}
                >
                  Edit
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {filteredPartyProfiles.length === 0 && (
        <div className="empty-state">
          <p>No shipment party profiles found.</p>
        </div>
      )}
    </div>
  );

  const renderSimulationProfilesTable = () => (
    <div className="table-card master-data-table">
      <table>
        <thead>
          <tr>
            <th>Profile</th>
            <th>Condition</th>
            <th>Speed</th>
            <th>Weight</th>
            <th>Route Offset</th>
            <th>Status</th>
            <th>Updated</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          {filteredSimulationProfiles.map((profile) => (
            <tr key={profile.id}>
              <td>
                <strong>{profile.displayName}</strong>
                <small>{profile.profileKey}</small>
              </td>
              <td>{formatEnumLabel(profile.conditionStatus)}</td>
              <td>
                {profile.minSpeed} - {profile.maxSpeed} km/h
              </td>
              <td>{profile.weight}</td>
              <td>
                {profile.routeDeviationEnabled ? "Enabled" : "Disabled"}
                <small>{profile.routeDeviationLatitudeOffset}</small>
              </td>
              <td>{renderActiveBadge(profile.active)}</td>
              <td>{formatDateTime(profile.updatedAt)}</td>
              <td>
                <button
                  type="button"
                  onClick={() => handleSimulationProfileEdit(profile)}
                >
                  Edit
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {filteredSimulationProfiles.length === 0 && (
        <div className="empty-state">
          <p>No simulation profiles found.</p>
        </div>
      )}
    </div>
  );

  if (isLoading) {
    return <p>Loading master data...</p>;
  }

  return (
    <div className="page master-data-page">
      <div className="page-header">
        <div>
          <h2>Master Data</h2>
          <p>Maintain operational selections used by shipment and simulation workflows.</p>
        </div>

        <div className="header-actions">
          <button type="button" onClick={loadMasterData}>
            Refresh
          </button>
        </div>
      </div>

      <div className="master-data-tabs" role="tablist" aria-label="Master data sections">
        {MASTER_DATA_TABS.map((tab) => (
          <button
            key={tab.key}
            type="button"
            className={activeTab === tab.key ? "active" : ""}
            onClick={() => handleTabChange(tab.key)}
          >
            <span>{tab.label}</span>
            <small>{tab.description}</small>
          </button>
        ))}
      </div>

      {errorMessage && <p className="error-message">{errorMessage}</p>}
      {successMessage && <p className="success-message">{successMessage}</p>}

      <div className="master-data-layout">
        {activeTab === "vehicleModels" && renderVehicleModelForm()}
        {activeTab === "partyProfiles" && renderPartyProfileForm()}
        {activeTab === "simulationProfiles" && renderSimulationProfileForm()}

        <section className="panel master-data-list-panel">
          <div className="master-data-toolbar">
            <div>
              <h3>{activeTabConfig?.label}</h3>
              <p>{activeTabConfig?.description}</p>
            </div>

            <input
              type="search"
              value={searchTerm}
              onChange={(event) => setSearchTerm(event.target.value)}
              placeholder="Search master data"
            />
          </div>

          {activeTab === "vehicleModels" && renderVehicleModelsTable()}
          {activeTab === "partyProfiles" && renderPartyProfilesTable()}
          {activeTab === "simulationProfiles" && renderSimulationProfilesTable()}
        </section>
      </div>
    </div>
  );
}

export default MasterDataPage;

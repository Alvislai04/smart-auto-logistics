function ShipmentForm({
  formData,
  formOptions,
  shipmentCode = "",
  isReadOnly = false,
  isDepartureAutomatic = false,
  isInitialStateSystemManaged = false,
  minimumEstimatedArrival,
  isLoadingOptions,
  isSubmitting,
  submitButtonLabel,
  onChange,
  onSubmit,
}) {
  const vehicleModels = formOptions?.vehicleModels ?? [];
  const manufacturerOrigins = formOptions?.manufacturerOrigins ?? [];
  const dealershipDestinations = formOptions?.dealershipDestinations ?? [];
  const logisticsStageOptions = formOptions?.logisticsStageOptions ?? [];
  const workInProgressStatusOptions =
    formOptions?.workInProgressStatusOptions ?? [];
  const formDisabled = isReadOnly || isLoadingOptions;

  return (
    <form className="form-panel shipment-form" onSubmit={onSubmit}>
      <section className="form-section">
        <div className="form-section-header">
          <h3>Vehicle and Route</h3>
        </div>

        <div className="form-grid">
          <label>
            Shipment Code
            <input
              className="readonly-input"
              type="text"
              value={shipmentCode || "Generated after save"}
              readOnly
            />
          </label>

          <label>
            Vehicle VIN
            <input
              type="text"
              name="vehicleVin"
              value={formData.vehicleVin}
              onChange={onChange}
              placeholder="Example: VIN123456789"
              disabled={isReadOnly}
              required
            />
          </label>

          <label>
            Vehicle Model
            <select
              name="vehicleModelKey"
              value={formData.vehicleModelKey}
              onChange={onChange}
              disabled={formDisabled}
              required
            >
              <option value="">Select Mercedes-Benz model</option>
              {vehicleModels.map((model) => (
                <option key={model.key} value={model.key}>
                  {model.displayName}
                </option>
              ))}
            </select>
          </label>

          <label>
            Manufacturer Origin
            <select
              name="manufacturerKey"
              value={formData.manufacturerKey}
              onChange={onChange}
              disabled={formDisabled}
              required
            >
              <option value="">Select manufacturer origin</option>
              {manufacturerOrigins.map((manufacturer) => (
                <option key={manufacturer.key} value={manufacturer.key}>
                  {manufacturer.name} - {manufacturer.location}
                </option>
              ))}
            </select>
          </label>

          <label>
            Dealership Destination
            <select
              name="dealershipKey"
              value={formData.dealershipKey}
              onChange={onChange}
              disabled={formDisabled}
              required
            >
              <option value="">Select dealership destination</option>
              {dealershipDestinations.map((dealership) => (
                <option key={dealership.key} value={dealership.key}>
                  {dealership.name} - {dealership.location}
                </option>
              ))}
            </select>
          </label>

          <label>
            Dealership Name
            <input
              className="readonly-input"
              type="text"
              value={formData.dealershipName}
              readOnly
              required
            />
          </label>

          <label className="form-field-full">
            Destination
            <input
              className="readonly-input"
              type="text"
              value={formData.destination}
              readOnly
              required
            />
          </label>
        </div>
      </section>

      <section className="form-section">
        <div className="form-section-header">
          <h3>Schedule and Operations</h3>
        </div>

        <div className="form-grid">
          <label>
            Shipment Start
            <input
              className="readonly-input"
              type={isDepartureAutomatic ? "text" : "datetime-local"}
              value={
                isDepartureAutomatic
                  ? "Assigned automatically when saved"
                  : formData.estimatedDeparture
              }
              readOnly
              required
            />
          </label>

          <label>
            Estimated Arrival
            <input
              type="datetime-local"
              name="estimatedArrival"
              value={formData.estimatedArrival}
              min={minimumEstimatedArrival}
              onChange={onChange}
              disabled={isReadOnly}
              required
            />
          </label>

          {!isInitialStateSystemManaged && (
            <label>
              Current Logistics Stage
              <select
                name="currentStage"
                value={formData.currentStage}
                onChange={onChange}
                disabled={formDisabled}
                required
              >
                <option value="">Select logistics stage</option>
                {logisticsStageOptions.map((stage) => (
                  <option key={stage.key} value={stage.key}>
                    {stage.displayName}
                  </option>
                ))}
              </select>
            </label>
          )}

          <label>
            Stock Location
            <input
              type="text"
              name="stockLocation"
              value={formData.stockLocation}
              onChange={onChange}
              placeholder="Example: Shah Alam Yard B"
              disabled={isReadOnly}
              required
            />
          </label>

          {!isInitialStateSystemManaged && (
            <label>
              WIP Status
              <select
                name="workInProgressStatus"
                value={formData.workInProgressStatus}
                onChange={onChange}
                disabled={formDisabled}
                required
              >
                <option value="">Select WIP status</option>
                {workInProgressStatusOptions.map((status) => (
                  <option key={status.key} value={status.key}>
                    {status.displayName}
                  </option>
                ))}
              </select>
            </label>
          )}

          <label>
            Shipment Document Reference
            <input
              type="text"
              name="shipmentDocumentReference"
              value={formData.shipmentDocumentReference}
              onChange={onChange}
              placeholder="Example: DO-2026-0001"
              disabled={isReadOnly}
            />
          </label>

          <label>
            Responsible Person
            <input
              type="text"
              name="responsiblePerson"
              value={formData.responsiblePerson}
              onChange={onChange}
              placeholder="Example: Ahmad Logistics"
              disabled={isReadOnly}
              required
            />
          </label>

          <label>
            Responsible Contact
            <input
              type="text"
              name="responsibleContact"
              value={formData.responsibleContact}
              onChange={onChange}
              placeholder="Example: +60 12-345 6789"
              disabled={isReadOnly}
              required
            />
          </label>
        </div>
      </section>

      <section className="form-section">
        <div className="form-section-header">
          <h3>Route Monitoring Coordinates</h3>
        </div>

        <div className="form-grid">
          <label>
            Origin Latitude
            <input
              className="readonly-input"
              type="number"
              step="0.000001"
              min="-90"
              max="90"
              value={formData.originLatitude}
              readOnly
              required
            />
          </label>

          <label>
            Origin Longitude
            <input
              className="readonly-input"
              type="number"
              step="0.000001"
              min="-180"
              max="180"
              value={formData.originLongitude}
              readOnly
              required
            />
          </label>

          <label>
            Destination Latitude
            <input
              className="readonly-input"
              type="number"
              step="0.000001"
              min="-90"
              max="90"
              value={formData.destinationLatitude}
              readOnly
              required
            />
          </label>

          <label>
            Destination Longitude
            <input
              className="readonly-input"
              type="number"
              step="0.000001"
              min="-180"
              max="180"
              value={formData.destinationLongitude}
              readOnly
              required
            />
          </label>
        </div>
      </section>

      <section className="form-section">
        <div className="form-section-header">
          <h3>Receiver Details</h3>
        </div>

        <div className="form-grid">
          <label>
            Customer Name
            <input
              type="text"
              name="customerName"
              value={formData.customerName}
              onChange={onChange}
              placeholder="Example: Lim Wei Chen"
              disabled={isReadOnly}
              required
            />
          </label>

          <label>
            Customer Contact
            <input
              type="text"
              name="customerContact"
              value={formData.customerContact}
              onChange={onChange}
              placeholder="Example: +60 17-888 1234"
              disabled={isReadOnly}
              required
            />
          </label>

          <label className="form-field-full">
            Customer Address
            <textarea
              name="customerAddress"
              value={formData.customerAddress}
              onChange={onChange}
              placeholder="Example: 88 Jalan Ampang, Kuala Lumpur"
              rows="3"
              disabled={isReadOnly}
              required
            />
          </label>
        </div>
      </section>

      <div className="form-actions">
        <button type="submit" disabled={isSubmitting || formDisabled}>
          {isSubmitting ? "Saving..." : submitButtonLabel}
        </button>
      </div>
    </form>
  );
}

export default ShipmentForm;

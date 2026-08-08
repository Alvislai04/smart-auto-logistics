import { formatEnumLabel } from "../../utils/formatters";

const logisticsStageOptions = [
  "MANUFACTURING",
  "STOCK_YARD",
  "WORK_IN_PROGRESS",
  "READY_FOR_DISPATCH",
  "IN_TRANSIT",
  "ARRIVED_AT_DEALERSHIP",
  "DELIVERED",
];

const workInProgressStatusOptions = [
  "NOT_APPLICABLE",
  "NOT_STARTED",
  "IN_PROGRESS",
  "ON_HOLD",
  "COMPLETED",
];

function ShipmentForm({
  formData,
  formOptions,
  isLoadingOptions,
  isSubmitting,
  submitButtonLabel,
  onChange,
  onSubmit,
}) {
  const vehicleModels = formOptions?.vehicleModels ?? [];
  const manufacturerOrigins = formOptions?.manufacturerOrigins ?? [];
  const dealershipDestinations = formOptions?.dealershipDestinations ?? [];

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
              value="Generated after save"
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
              required
            />
          </label>

          <label>
            Vehicle Model
            <select
              name="vehicleModelKey"
              value={formData.vehicleModelKey}
              onChange={onChange}
              disabled={isLoadingOptions}
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
              disabled={isLoadingOptions}
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
              disabled={isLoadingOptions}
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
            Estimated Departure
            <input
              type="datetime-local"
              name="estimatedDeparture"
              value={formData.estimatedDeparture}
              onChange={onChange}
              required
            />
          </label>

          <label>
            Estimated Arrival
            <input
              type="datetime-local"
              name="estimatedArrival"
              value={formData.estimatedArrival}
              onChange={onChange}
              required
            />
          </label>

          <label>
            Current Logistics Stage
            <select
              name="currentStage"
              value={formData.currentStage}
              onChange={onChange}
              required
            >
              {logisticsStageOptions.map((stage) => (
                <option key={stage} value={stage}>
                  {formatEnumLabel(stage)}
                </option>
              ))}
            </select>
          </label>

          <label>
            Stock Location
            <input
              type="text"
              name="stockLocation"
              value={formData.stockLocation}
              onChange={onChange}
              placeholder="Example: Shah Alam Yard B"
              required
            />
          </label>

          <label>
            WIP Status
            <select
              name="workInProgressStatus"
              value={formData.workInProgressStatus}
              onChange={onChange}
              required
            >
              {workInProgressStatusOptions.map((status) => (
                <option key={status} value={status}>
                  {formatEnumLabel(status)}
                </option>
              ))}
            </select>
          </label>

          <label>
            Shipment Document Reference
            <input
              type="text"
              name="shipmentDocumentReference"
              value={formData.shipmentDocumentReference}
              onChange={onChange}
              placeholder="Example: DO-2026-0001"
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
              required
            />
          </label>
        </div>
      </section>

      <div className="form-actions">
        <button type="submit" disabled={isSubmitting || isLoadingOptions}>
          {isSubmitting ? "Saving..." : submitButtonLabel}
        </button>
      </div>
    </form>
  );
}

export default ShipmentForm;

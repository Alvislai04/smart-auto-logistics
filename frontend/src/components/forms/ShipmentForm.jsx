function ShipmentForm({
  formData,
  isSubmitting,
  submitButtonLabel,
  onChange,
  onSubmit,
}) {
  return (
    <form className="form-panel" onSubmit={onSubmit}>
      <div className="form-grid">
        <label>
          Shipment Code
          <input
            type="text"
            name="shipmentCode"
            value={formData.shipmentCode}
            onChange={onChange}
            placeholder="Example: SHP-001"
            required
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
          <input
            type="text"
            name="vehicleModel"
            value={formData.vehicleModel}
            onChange={onChange}
            placeholder="Example: Toyota Vios"
            required
          />
        </label>

        <label>
          Origin
          <input
            type="text"
            name="origin"
            value={formData.origin}
            onChange={onChange}
            placeholder="Example: Manufacturer Plant A"
            required
          />
        </label>

        <label>
          Destination
          <input
            type="text"
            name="destination"
            value={formData.destination}
            onChange={onChange}
            placeholder="Example: Kuala Lumpur Dealership"
            required
          />
        </label>

        <label>
          Dealership Name
          <input
            type="text"
            name="dealershipName"
            value={formData.dealershipName}
            onChange={onChange}
            placeholder="Example: KL Auto Dealer"
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
      </div>

      <div className="form-actions">
        <button type="submit" disabled={isSubmitting}>
          {isSubmitting ? "Saving..." : submitButtonLabel}
        </button>
      </div>
    </form>
  );
}

export default ShipmentForm;

import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { shipmentApi } from "../api/shipmentApi";
import ShipmentForm from "../components/forms/ShipmentForm";

const initialFormData = {
  shipmentCode: "",
  vehicleVin: "",
  vehicleModel: "",
  origin: "",
  destination: "",
  dealershipName: "",
  estimatedArrival: "",
};

function CreateShipmentPage() {
  const navigate = useNavigate();

  const [formData, setFormData] = useState(initialFormData);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const handleChange = (event) => {
    const { name, value } = event.target;

    setFormData((previousValue) => ({
      ...previousValue,
      [name]: value,
    }));
  };

  const buildCreateShipmentPayload = () => {
    return {
      shipmentCode: formData.shipmentCode.trim(),
      vehicleVin: formData.vehicleVin.trim(),
      vehicleModel: formData.vehicleModel.trim(),
      origin: formData.origin.trim(),
      destination: formData.destination.trim(),
      dealershipName: formData.dealershipName.trim(),
      estimatedArrival: formData.estimatedArrival,
    };
  };

  const validateForm = () => {
    const requiredFields = [
      "shipmentCode",
      "vehicleVin",
      "vehicleModel",
      "origin",
      "destination",
      "dealershipName",
      "estimatedArrival",
    ];

    const hasEmptyField = requiredFields.some(
      (fieldName) => !formData[fieldName]?.trim(),
    );

    if (hasEmptyField) {
      setErrorMessage("Please complete all required fields.");
      return false;
    }

    return true;
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!validateForm()) {
      return;
    }

    try {
      setIsSubmitting(true);
      setErrorMessage("");

      const payload = buildCreateShipmentPayload();
      const createdShipment = await shipmentApi.create(payload);

      navigate(`/shipments/${createdShipment.id}`);
    } catch (error) {
      setErrorMessage(
        error.response?.data?.message || "Failed to create shipment.",
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h2>Create Shipment</h2>
          <p>Register a new vehicle shipment record.</p>
        </div>

        <button type="button" onClick={() => navigate("/shipments")}>
          Back to Shipments
        </button>
      </div>

      {errorMessage && <p className="error-message">{errorMessage}</p>}

      <ShipmentForm
        formData={formData}
        isSubmitting={isSubmitting}
        submitButtonLabel="Create Shipment"
        onChange={handleChange}
        onSubmit={handleSubmit}
      />
    </div>
  );
}

export default CreateShipmentPage;

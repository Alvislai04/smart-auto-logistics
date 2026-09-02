import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { shipmentApi } from "../api/shipmentApi";
import { useToast } from "../components/feedback/ToastContext";
import ShipmentForm from "../components/forms/ShipmentForm";
import {
  applyDealershipSelection,
  applyLoadedOptions,
  applyManufacturerSelection,
  buildCreateShipmentPayload,
  emptyShipmentFormOptions,
  initialShipmentFormData,
  minimumFutureDateTimeLocalValue,
  normalizeShipmentFormOptions,
  validateShipmentForm,
} from "../utils/shipmentFormUtils";

function CreateShipmentPage() {
  const navigate = useNavigate();
  const { showToast } = useToast();

  const [formData, setFormData] = useState(initialShipmentFormData);
  const [formOptions, setFormOptions] = useState(emptyShipmentFormOptions);
  const [isLoadingOptions, setIsLoadingOptions] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const loadFormOptions = useCallback(async () => {
    try {
      setIsLoadingOptions(true);
      setErrorMessage("");

      const options = await shipmentApi.getFormOptions();
      const normalizedOptions = normalizeShipmentFormOptions(options);

      setFormOptions(normalizedOptions);
      setFormData((previousFormData) =>
        applyLoadedOptions(previousFormData, normalizedOptions),
      );

      if (
        normalizedOptions.vehicleModels.length === 0 ||
        normalizedOptions.manufacturerOrigins.length === 0 ||
        normalizedOptions.dealershipDestinations.length === 0
      ) {
        setErrorMessage(
          "Shipment options are incomplete. Please configure shipment master data before creating shipments.",
        );
      }
    } catch (error) {
      setErrorMessage(
        error.response?.data?.message || "Failed to load shipment form options.",
      );
    } finally {
      setIsLoadingOptions(false);
    }
  }, []);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    loadFormOptions();
  }, [loadFormOptions]);

  const handleChange = (event) => {
    const { name, value } = event.target;

    setFormData((previousValue) => {
      if (name === "manufacturerKey") {
        return applyManufacturerSelection(previousValue, value, formOptions);
      }

      if (name === "dealershipKey") {
        return applyDealershipSelection(previousValue, value, formOptions);
      }

      return {
        ...previousValue,
        [name]: value,
      };
    });
  };

  const validateForm = () => {
    const validationMessage = validateShipmentForm(formData, {
      automaticDeparture: true,
      systemManagedInitialState: true,
    });

    if (validationMessage) {
      setErrorMessage(validationMessage);
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

      const payload = buildCreateShipmentPayload(formData);
      const createdShipment = await shipmentApi.create(payload);

      showToast({
        type: "success",
        title: "Shipment created",
        message: `${createdShipment.shipmentCode} is ready for monitoring.`,
      });
      navigate(`/shipments/${createdShipment.id}`);
    } catch (error) {
      const message =
        error.response?.data?.message || "Failed to create shipment.";
      setErrorMessage(message);
      showToast({
        type: "error",
        title: "Shipment creation failed",
        message,
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h2>Create Shipment</h2>
          <p>Register vehicle, route, operation, and receiver details.</p>
        </div>

        <button type="button" onClick={() => navigate("/shipments")}>
          Back to Shipments
        </button>
      </div>

      {errorMessage && <p className="error-message">{errorMessage}</p>}

      <ShipmentForm
        formData={formData}
        formOptions={formOptions}
        isDepartureAutomatic
        isInitialStateSystemManaged
        minimumEstimatedArrival={minimumFutureDateTimeLocalValue()}
        isLoadingOptions={isLoadingOptions}
        isSubmitting={isSubmitting}
        submitButtonLabel="Create Shipment"
        onChange={handleChange}
        onSubmit={handleSubmit}
      />
    </div>
  );
}

export default CreateShipmentPage;

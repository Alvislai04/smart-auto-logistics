import { useCallback, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { shipmentApi } from "../api/shipmentApi";
import { useAuth } from "../auth/AuthContext";
import { useToast } from "../components/feedback/ToastContext";
import ShipmentForm from "../components/forms/ShipmentForm";
import {
  applyDealershipSelection,
  applyLoadedOptions,
  applyManufacturerSelection,
  buildShipmentPayload,
  emptyShipmentFormOptions,
  initialShipmentFormData,
  isShipmentEditableForUser,
  mapShipmentToFormData,
  normalizeShipmentFormOptions,
  validateShipmentForm,
} from "../utils/shipmentFormUtils";

function EditShipmentPage() {
  const { shipmentId } = useParams();
  const navigate = useNavigate();
  const { currentUser } = useAuth();
  const { showToast } = useToast();

  const [shipment, setShipment] = useState(null);
  const [formData, setFormData] = useState(initialShipmentFormData);
  const [formOptions, setFormOptions] = useState(emptyShipmentFormOptions);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const loadEditData = useCallback(async () => {
    try {
      setIsLoading(true);
      setErrorMessage("");

      const [shipmentData, optionsData] = await Promise.all([
        shipmentApi.getById(shipmentId),
        shipmentApi.getFormOptions(),
      ]);
      const normalizedOptions = normalizeShipmentFormOptions(optionsData);
      const loadedFormData = applyLoadedOptions(
        mapShipmentToFormData(shipmentData),
        normalizedOptions,
      );

      setShipment(shipmentData);
      setFormOptions(normalizedOptions);
      setFormData(loadedFormData);

      if (!isShipmentEditableForUser(shipmentData, currentUser)) {
        setErrorMessage(
          "This shipment can only be edited by admin or the linked manufacturer before pickup.",
        );
      }
    } catch (error) {
      setErrorMessage(
        error.response?.data?.message || "Failed to load shipment edit data.",
      );
    } finally {
      setIsLoading(false);
    }
  }, [currentUser, shipmentId]);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    loadEditData();
  }, [loadEditData]);

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
    const validationMessage = validateShipmentForm(formData);

    if (validationMessage) {
      setErrorMessage(validationMessage);
      return false;
    }

    if (!isShipmentEditableForUser(shipment, currentUser)) {
      setErrorMessage(
        "This shipment can only be edited by admin or the linked manufacturer before pickup.",
      );
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

      const updatedShipment = await shipmentApi.update(
        shipmentId,
        buildShipmentPayload(formData),
      );
      showToast({
        type: "success",
        title: "Shipment updated",
        message: `${updatedShipment.shipmentCode} details were saved.`,
      });
      navigate(`/shipments/${shipmentId}`);
    } catch (error) {
      const message =
        error.response?.data?.message || "Failed to update shipment.";
      setErrorMessage(message);
      showToast({
        type: "error",
        title: "Shipment update failed",
        message,
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoading) {
    return <p>Loading shipment edit form...</p>;
  }

  if (!shipment) {
    return (
      <div className="page">
        {errorMessage ? (
          <p className="error-message">{errorMessage}</p>
        ) : (
          <p>Shipment not found.</p>
        )}
      </div>
    );
  }

  const canSubmit = isShipmentEditableForUser(shipment, currentUser);

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h2>Edit Shipment</h2>
          <p>{shipment.shipmentCode}</p>
        </div>

        <button
          type="button"
          onClick={() => navigate(`/shipments/${shipmentId}`)}
        >
          Back to Details
        </button>
      </div>

      {errorMessage && <p className="error-message">{errorMessage}</p>}

      {!canSubmit && (
        <div className="shipment-edit-lock-note">
          <strong>Editing locked</strong>
          <span>
            Shipment details are locked after pickup, in-transit movement,
            delivery, cancellation, or when the shipment is not linked to your
            manufacturer profile.
          </span>
        </div>
      )}

      <ShipmentForm
        formData={formData}
        formOptions={formOptions}
        shipmentCode={shipment.shipmentCode}
        isReadOnly={!canSubmit}
        isLoadingOptions={false}
        isSubmitting={isSubmitting}
        submitButtonLabel="Save Shipment"
        onChange={handleChange}
        onSubmit={handleSubmit}
      />
    </div>
  );
}

export default EditShipmentPage;

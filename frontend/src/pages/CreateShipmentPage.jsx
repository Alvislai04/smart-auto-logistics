import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { shipmentApi } from "../api/shipmentApi";
import ShipmentForm from "../components/forms/ShipmentForm";

const emptyFormOptions = {
  vehicleModels: [],
  manufacturerOrigins: [],
  dealershipDestinations: [],
};

const initialFormData = {
  vehicleVin: "",
  vehicleModelKey: "",
  manufacturerKey: "",
  dealershipKey: "",
  origin: "",
  destination: "",
  originLatitude: "",
  originLongitude: "",
  destinationLatitude: "",
  destinationLongitude: "",
  dealershipName: "",
  estimatedArrival: "",
  estimatedDeparture: "",
  currentStage: "MANUFACTURING",
  stockLocation: "",
  workInProgressStatus: "NOT_APPLICABLE",
  responsiblePerson: "",
  responsibleContact: "",
  customerName: "",
  customerContact: "",
  customerAddress: "",
  shipmentDocumentReference: "",
};

function toFormValue(value) {
  return value == null ? "" : String(value);
}

function findOptionByKey(options, optionKey) {
  return options.find((option) => option.key === optionKey);
}

function hasOptionKey(options, optionKey) {
  return options.some((option) => option.key === optionKey);
}

function applyManufacturerSelection(formData, manufacturerKey, formOptions) {
  const manufacturer = findOptionByKey(
    formOptions.manufacturerOrigins,
    manufacturerKey,
  );

  return {
    ...formData,
    manufacturerKey: toFormValue(manufacturerKey),
    origin: manufacturer?.location ?? "",
    originLatitude: toFormValue(manufacturer?.latitude),
    originLongitude: toFormValue(manufacturer?.longitude),
  };
}

function applyDealershipSelection(formData, dealershipKey, formOptions) {
  const dealership = findOptionByKey(
    formOptions.dealershipDestinations,
    dealershipKey,
  );

  return {
    ...formData,
    dealershipKey: toFormValue(dealershipKey),
    dealershipName: dealership?.name ?? "",
    destination: dealership?.location ?? "",
    destinationLatitude: toFormValue(dealership?.latitude),
    destinationLongitude: toFormValue(dealership?.longitude),
  };
}

function applyLoadedOptions(formData, formOptions) {
  const vehicleModelKey = hasOptionKey(
    formOptions.vehicleModels,
    formData.vehicleModelKey,
  )
    ? formData.vehicleModelKey
    : "";
  const manufacturerKey = hasOptionKey(
    formOptions.manufacturerOrigins,
    formData.manufacturerKey,
  )
    ? formData.manufacturerKey
    : "";
  const dealershipKey = hasOptionKey(
    formOptions.dealershipDestinations,
    formData.dealershipKey,
  )
    ? formData.dealershipKey
    : "";

  let nextFormData = {
    ...formData,
    vehicleModelKey,
  };

  nextFormData = applyManufacturerSelection(
    nextFormData,
    manufacturerKey,
    formOptions,
  );
  nextFormData = applyDealershipSelection(
    nextFormData,
    dealershipKey,
    formOptions,
  );

  return nextFormData;
}

function CreateShipmentPage() {
  const navigate = useNavigate();

  const [formData, setFormData] = useState(initialFormData);
  const [formOptions, setFormOptions] = useState(emptyFormOptions);
  const [isLoadingOptions, setIsLoadingOptions] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const loadFormOptions = useCallback(async () => {
    try {
      setIsLoadingOptions(true);
      setErrorMessage("");

      const options = await shipmentApi.getFormOptions();
      const normalizedOptions = {
        vehicleModels: options.vehicleModels ?? [],
        manufacturerOrigins: options.manufacturerOrigins ?? [],
        dealershipDestinations: options.dealershipDestinations ?? [],
      };

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
          "Shipment options are incomplete. Please configure manufacturer and dealership profiles.",
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

  const buildCreateShipmentPayload = () => {
    const nullableText = (value) => {
      const trimmedValue = value.trim();
      return trimmedValue || null;
    };

    return {
      manufacturerKey: formData.manufacturerKey,
      dealershipKey: formData.dealershipKey,
      vehicleVin: formData.vehicleVin.trim(),
      vehicleModelKey: formData.vehicleModelKey,
      estimatedArrival: formData.estimatedArrival,
      estimatedDeparture: formData.estimatedDeparture,
      currentStage: formData.currentStage,
      stockLocation: formData.stockLocation.trim(),
      workInProgressStatus: formData.workInProgressStatus,
      responsiblePerson: formData.responsiblePerson.trim(),
      responsibleContact: formData.responsibleContact.trim(),
      customerName: formData.customerName.trim(),
      customerContact: formData.customerContact.trim(),
      customerAddress: formData.customerAddress.trim(),
      shipmentDocumentReference: nullableText(
        formData.shipmentDocumentReference,
      ),
    };
  };

  const validateForm = () => {
    const requiredFields = [
      "manufacturerKey",
      "dealershipKey",
      "vehicleVin",
      "vehicleModelKey",
      "estimatedArrival",
      "estimatedDeparture",
      "currentStage",
      "stockLocation",
      "workInProgressStatus",
      "responsiblePerson",
      "responsibleContact",
      "customerName",
      "customerContact",
      "customerAddress",
    ];

    const hasEmptyField = requiredFields.some(
      (fieldName) => !String(formData[fieldName] ?? "").trim(),
    );

    if (hasEmptyField) {
      setErrorMessage("Please complete all required fields.");
      return false;
    }

    if (!formData.origin || !formData.destination || !formData.dealershipName) {
      setErrorMessage("Please select a valid manufacturer and dealership.");
      return false;
    }

    if (
      new Date(formData.estimatedArrival) <= new Date(formData.estimatedDeparture)
    ) {
      setErrorMessage("Estimated arrival must be after estimated departure.");
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

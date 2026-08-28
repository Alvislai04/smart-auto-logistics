export const editableShipmentStatuses = ["CREATED", "READY_FOR_DISPATCH"];

export const emptyShipmentFormOptions = {
  vehicleModels: [],
  manufacturerOrigins: [],
  dealershipDestinations: [],
  logisticsStageOptions: [],
  workInProgressStatusOptions: [],
};

export const initialShipmentFormData = {
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

export function toFormValue(value) {
  return value == null ? "" : String(value);
}

export function normalizeShipmentFormOptions(options = {}) {
  return {
    vehicleModels: options.vehicleModels ?? [],
    manufacturerOrigins: options.manufacturerOrigins ?? [],
    dealershipDestinations: options.dealershipDestinations ?? [],
    logisticsStageOptions: options.logisticsStageOptions ?? [],
    workInProgressStatusOptions: options.workInProgressStatusOptions ?? [],
  };
}

export function applyManufacturerSelection(formData, manufacturerKey, formOptions) {
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

export function applyDealershipSelection(formData, dealershipKey, formOptions) {
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

export function applyLoadedOptions(formData, formOptions) {
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
  const currentStage = hasOptionKey(
    formOptions.logisticsStageOptions,
    formData.currentStage,
  )
    ? formData.currentStage
    : "";
  const workInProgressStatus = hasOptionKey(
    formOptions.workInProgressStatusOptions,
    formData.workInProgressStatus,
  )
    ? formData.workInProgressStatus
    : "";

  let nextFormData = {
    ...formData,
    vehicleModelKey,
    currentStage,
    workInProgressStatus,
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

export function mapShipmentToFormData(shipment) {
  return {
    vehicleVin: shipment.vehicleVin ?? "",
    vehicleModelKey: shipment.vehicleModelCode ?? "",
    manufacturerKey: shipment.manufacturerKey ?? "",
    dealershipKey: shipment.dealershipKey ?? "",
    origin: shipment.origin ?? "",
    destination: shipment.destination ?? "",
    originLatitude: toFormValue(shipment.originLatitude),
    originLongitude: toFormValue(shipment.originLongitude),
    destinationLatitude: toFormValue(shipment.destinationLatitude),
    destinationLongitude: toFormValue(shipment.destinationLongitude),
    dealershipName: shipment.dealershipName ?? "",
    estimatedArrival: toDateTimeLocalValue(shipment.estimatedArrival),
    estimatedDeparture: toDateTimeLocalValue(shipment.estimatedDeparture),
    currentStage: shipment.currentStage ?? "",
    stockLocation: shipment.stockLocation ?? "",
    workInProgressStatus: shipment.workInProgressStatus ?? "",
    responsiblePerson: shipment.responsiblePerson ?? "",
    responsibleContact: shipment.responsibleContact ?? "",
    customerName: shipment.customerName ?? "",
    customerContact: shipment.customerContact ?? "",
    customerAddress: shipment.customerAddress ?? "",
    shipmentDocumentReference: shipment.shipmentDocumentReference ?? "",
  };
}

export function buildShipmentPayload(formData) {
  return {
    manufacturerKey: formData.manufacturerKey,
    dealershipKey: formData.dealershipKey,
    vehicleVin: formData.vehicleVin.trim(),
    vehicleModelKey: formData.vehicleModelKey,
    estimatedArrival: formData.estimatedArrival,
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
}

export function validateShipmentForm(
  formData,
  { automaticDeparture = false } = {},
) {
  const requiredFields = [
    "manufacturerKey",
    "dealershipKey",
    "vehicleVin",
    "vehicleModelKey",
    "estimatedArrival",
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
    return "Please complete all required fields.";
  }

  if (!formData.origin || !formData.destination || !formData.dealershipName) {
    return "Please select a valid manufacturer and dealership.";
  }

  if (!automaticDeparture && !formData.estimatedDeparture) {
    return "Please complete all required fields.";
  }

  const estimatedDeparture = automaticDeparture
    ? new Date()
    : new Date(formData.estimatedDeparture);
  const estimatedArrival = new Date(formData.estimatedArrival);

  if (estimatedArrival <= estimatedDeparture) {
    return "Estimated arrival must be after estimated departure.";
  }

  return "";
}

export function isShipmentEditableForUser(shipment, currentUser) {
  if (!shipment || !currentUser) {
    return false;
  }

  if (!editableShipmentStatuses.includes(shipment.status)) {
    return false;
  }

  if (currentUser.role === "ADMIN") {
    return true;
  }

  return (
    currentUser.role === "MANUFACTURER" &&
    currentUser.partyProfileKey &&
    currentUser.partyProfileKey === shipment.manufacturerKey
  );
}

function findOptionByKey(options, optionKey) {
  return options.find((option) => option.key === optionKey);
}

function hasOptionKey(options, optionKey) {
  return options.some((option) => option.key === optionKey);
}

function nullableText(value) {
  const trimmedValue = value.trim();
  return trimmedValue || null;
}

function toDateTimeLocalValue(value) {
  if (!value) {
    return "";
  }

  return String(value).slice(0, 16);
}

import axiosClient from "./axiosClient";

function sanitizeParams(params = {}) {
  return Object.fromEntries(
    Object.entries(params).filter(([, value]) => value !== "" && value != null),
  );
}

async function getReport(path, params) {
  const response = await axiosClient.get(path, {
    params: sanitizeParams(params),
  });

  return response.data;
}

async function downloadCsv(path, params) {
  const response = await axiosClient.get(path, {
    params: sanitizeParams(params),
    responseType: "blob",
  });

  return response.data;
}

export const reportApi = {
  getOptions: async () => getReport("/reports/options"),

  getShipmentReport: async (params) => getReport("/reports/shipments", params),

  getDelayReport: async (params) => getReport("/reports/delays", params),

  getAlertReport: async (params) => getReport("/reports/alerts", params),

  downloadShipmentCsv: async (params) =>
    downloadCsv("/reports/shipments.csv", params),

  downloadDelayCsv: async (params) =>
    downloadCsv("/reports/delays.csv", params),

  downloadAlertCsv: async (params) =>
    downloadCsv("/reports/alerts.csv", params),
};

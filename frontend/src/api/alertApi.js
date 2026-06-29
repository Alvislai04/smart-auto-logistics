import axiosClient from "./axiosClient";

export const alertApi = {
  getOpenAlerts: async () => {
    const response = await axiosClient.get("/alerts/open");
    return response.data;
  },

  getByShipmentId: async (shipmentId) => {
    const response = await axiosClient.get(`/alerts/shipment/${shipmentId}`);
    return response.data;
  },

  acknowledge: async (alertId) => {
    const response = await axiosClient.put(`/alerts/${alertId}/acknowledge`);
    return response.data;
  },

  resolve: async (alertId) => {
    const response = await axiosClient.put(`/alerts/${alertId}/resolve`);
    return response.data;
  },
};

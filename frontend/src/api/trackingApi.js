import axiosClient from "./axiosClient";

export const trackingApi = {
  getByShipmentId: async (shipmentId) => {
    const response = await axiosClient.get(
      `/shipments/${shipmentId}/tracking-events`,
    );
    return response.data;
  },

  getLatestByShipmentId: async (shipmentId) => {
    const response = await axiosClient.get(
      `/shipments/${shipmentId}/tracking-events/latest`,
    );
    return response.data;
  },

  simulateNextPoint: async (shipmentId, payload) => {
    const response = await axiosClient.post(
      `/shipments/${shipmentId}/simulation/next-point`,
      payload,
    );
    return response.data;
  },
};

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
};

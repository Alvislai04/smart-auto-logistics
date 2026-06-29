import axiosClient from "./axiosClient";

export const shipmentEventApi = {
  getByShipmentId: async (shipmentId) => {
    const response = await axiosClient.get(`/shipments/${shipmentId}/events`);
    return response.data;
  },
};

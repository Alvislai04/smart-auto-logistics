import axiosClient from "./axiosClient";

export const simulationApi = {
  getSessions: async () => {
    const response = await axiosClient.get("/simulation-sessions");
    return response.data;
  },

  getShipmentSession: async (shipmentId) => {
    const response = await axiosClient.get(
      `/shipments/${shipmentId}/simulation/session`,
    );
    return response.data || null;
  },
};

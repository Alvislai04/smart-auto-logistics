import axiosClient from "./axiosClient";

export const shipmentApi = {
  getAll: async () => {
    const response = await axiosClient.get("/shipments");
    return response.data;
  },

  getById: async (shipmentId) => {
    const response = await axiosClient.get(`/shipments/${shipmentId}`);
    return response.data;
  },

  create: async (payload) => {
    const response = await axiosClient.post("/shipments", payload);
    return response.data;
  },

  updateStatus: async (shipmentId, status) => {
    const response = await axiosClient.put(
      `/shipments/${shipmentId}/status?status=${status}`,
    );
    return response.data;
  },
};

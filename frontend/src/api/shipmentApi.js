import axiosClient from "./axiosClient";

export const shipmentApi = {
  getAll: async (params = {}) => {
    const response = await axiosClient.get("/shipments", { params });
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

  update: async (shipmentId, payload) => {
    const response = await axiosClient.put(`/shipments/${shipmentId}`, payload);
    return response.data;
  },

  getFormOptions: async () => {
    const response = await axiosClient.get("/shipment-options");
    return response.data;
  },

  updateStatus: async (shipmentId, status) => {
    const response = await axiosClient.put(
      `/shipments/${shipmentId}/status?status=${status}`,
    );
    return response.data;
  },

  confirmReceipt: async (shipmentId, payload) => {
    const response = await axiosClient.put(
      `/shipments/${shipmentId}/receipt-confirmation`,
      payload,
    );
    return response.data;
  },
};

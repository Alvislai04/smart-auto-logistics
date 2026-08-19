import axiosClient from "./axiosClient";

export const blockchainApi = {
  getAll: async () => {
    const response = await axiosClient.get("/blockchain-records");
    return response.data;
  },

  getByShipmentId: async (shipmentId) => {
    const response = await axiosClient.get(
      `/blockchain-records/shipment/${shipmentId}`,
    );
    return response.data;
  },

  verifyRecord: async (recordId) => {
    const response = await axiosClient.post(
      `/blockchain-records/${recordId}/verify`,
    );
    return response.data;
  },

  retryRecord: async (recordId) => {
    const response = await axiosClient.post(
      `/blockchain-records/${recordId}/retry`,
    );
    return response.data;
  },

  getHealth: async () => {
    const response = await axiosClient.get("/blockchain-records/health");
    return response.data;
  },
};

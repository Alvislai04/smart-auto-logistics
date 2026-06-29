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
};

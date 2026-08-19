import axiosClient from "./axiosClient";

export const stockMonitoringApi = {
  getOverview: async () => {
    const response = await axiosClient.get("/stock-monitoring");
    return response.data;
  },

  createRule: async (payload) => {
    const response = await axiosClient.post("/stock-monitoring/rules", payload);
    return response.data;
  },

  updateRule: async (ruleId, payload) => {
    const response = await axiosClient.put(
      `/stock-monitoring/rules/${ruleId}`,
      payload,
    );
    return response.data;
  },
};

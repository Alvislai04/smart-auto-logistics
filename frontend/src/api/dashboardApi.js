import axiosClient from "./axiosClient";

export const dashboardApi = {
  getOverview: async (config = {}) => {
    const response = await axiosClient.get("/dashboard/overview", config);
    return response.data;
  },

  getSummary: async () => {
    const response = await axiosClient.get("/dashboard/summary");
    return response.data;
  },

  getOpenAlerts: async () => {
    const response = await axiosClient.get("/dashboard/open-alerts");
    return response.data;
  },
};

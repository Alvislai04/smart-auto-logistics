import axiosClient from "./axiosClient";

export const integrityCaseApi = {
  getAll: async () => {
    const response = await axiosClient.get("/admin/integrity-cases");
    return response.data;
  },

  updateStatus: async (caseId, payload) => {
    const response = await axiosClient.put(
      `/admin/integrity-cases/${caseId}/status`,
      payload,
    );
    return response.data;
  },

  resolveCase: async (caseId, payload) => {
    const response = await axiosClient.put(
      `/admin/integrity-cases/${caseId}/resolve`,
      payload,
    );
    return response.data;
  },
};

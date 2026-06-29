import axiosClient from "./axiosClient";

export const companyNotificationApi = {
  getAll: async () => {
    const response = await axiosClient.get("/admin/company-notifications");
    return response.data;
  },
};

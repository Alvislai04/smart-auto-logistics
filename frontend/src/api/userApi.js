import axiosClient from "./axiosClient";

export const userApi = {
  async getUsers() {
    const response = await axiosClient.get("/admin/users");
    return response.data;
  },

  async getFormOptions() {
    const response = await axiosClient.get("/admin/users/form-options");
    return response.data;
  },

  async createUser(payload) {
    const response = await axiosClient.post("/admin/users", payload);
    return response.data;
  },

  async updateUser(userId, payload) {
    const response = await axiosClient.put(`/admin/users/${userId}`, payload);
    return response.data;
  },

  async getProfile() {
    const response = await axiosClient.get("/account/profile");
    return response.data;
  },

  async updateProfile(payload) {
    const response = await axiosClient.put("/account/profile", payload);
    return response.data;
  },

  async changePassword(payload) {
    await axiosClient.put("/account/password", payload);
  },
};

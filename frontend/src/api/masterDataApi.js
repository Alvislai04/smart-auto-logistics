import axiosClient from "./axiosClient";

export const masterDataApi = {
  async getOverview() {
    const response = await axiosClient.get("/admin/master-data");
    return response.data;
  },

  async createVehicleModel(payload) {
    const response = await axiosClient.post(
      "/admin/master-data/vehicle-models",
      payload,
    );
    return response.data;
  },

  async updateVehicleModel(vehicleModelId, payload) {
    const response = await axiosClient.put(
      `/admin/master-data/vehicle-models/${vehicleModelId}`,
      payload,
    );
    return response.data;
  },

  async createPartyProfile(payload) {
    const response = await axiosClient.post(
      "/admin/master-data/party-profiles",
      payload,
    );
    return response.data;
  },

  async updatePartyProfile(profileId, payload) {
    const response = await axiosClient.put(
      `/admin/master-data/party-profiles/${profileId}`,
      payload,
    );
    return response.data;
  },

  async createSimulationProfile(payload) {
    const response = await axiosClient.post(
      "/admin/master-data/simulation-profiles",
      payload,
    );
    return response.data;
  },

  async updateSimulationProfile(profileId, payload) {
    const response = await axiosClient.put(
      `/admin/master-data/simulation-profiles/${profileId}`,
      payload,
    );
    return response.data;
  },
};

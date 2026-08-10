import axiosClient from "./axiosClient";

export const simulationApi = {
  getOptions: async () => {
    const response = await axiosClient.get("/simulation-sessions/options");
    return response.data;
  },

  getSessions: async () => {
    const response = await axiosClient.get("/simulation-sessions");
    return response.data;
  },

  getShipmentSession: async (shipmentId) => {
    const response = await axiosClient.get(
      `/shipments/${shipmentId}/simulation/session`,
    );
    return response.data || null;
  },

  startSession: async (shipmentId, payload) => {
    const response = await axiosClient.post(
      `/shipments/${shipmentId}/simulation/sessions`,
      payload,
    );
    return response.data;
  },

  pauseSession: async (sessionId) => {
    const response = await axiosClient.put(
      `/simulation-sessions/${sessionId}/pause`,
    );
    return response.data;
  },

  resumeSession: async (sessionId) => {
    const response = await axiosClient.put(
      `/simulation-sessions/${sessionId}/resume`,
    );
    return response.data;
  },

  stopSession: async (sessionId) => {
    const response = await axiosClient.put(
      `/simulation-sessions/${sessionId}/stop`,
    );
    return response.data;
  },
};

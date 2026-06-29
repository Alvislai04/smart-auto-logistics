import axiosClient from "./axiosClient";

export const adminNotificationApi = {
  getAll: async () => {
    const response = await axiosClient.get("/admin/notifications");
    return response.data;
  },

  getUnread: async () => {
    const response = await axiosClient.get("/admin/notifications/unread");
    return response.data;
  },

  markAsRead: async (notificationId) => {
    const response = await axiosClient.put(
      `/admin/notifications/${notificationId}/read`,
    );
    return response.data;
  },
};

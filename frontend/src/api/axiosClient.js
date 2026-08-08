import axios from "axios";
import { clearStoredAuthUser, getStoredAuthUser } from "../auth/authStorage";

const axiosClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api",
  headers: {
    "Content-Type": "application/json",
  },
});

axiosClient.interceptors.request.use(
  (config) => {
    const requestUrl = config.url ?? "";

    if (requestUrl.includes("/auth/")) {
      return config;
    }

    const user = getStoredAuthUser();

    if (user?.token) {
      config.headers.Authorization = `Bearer ${user.token}`;
    }

    return config;
  },
  (error) => Promise.reject(error),
);

axiosClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;
    const requestUrl = error.config?.url ?? "";
    const isAuthRequest = requestUrl.includes("/auth/");

    console.error("API Error:", {
      status,
      url: error.config?.url,
      message: error.response?.data?.message || error.message,
    });

    if (status === 401 && !isAuthRequest) {
      clearStoredAuthUser();

      if (window.location.pathname !== "/login") {
        window.location.href = "/login?session=expired";
      }
    }

    return Promise.reject(error);
  },
);

export default axiosClient;

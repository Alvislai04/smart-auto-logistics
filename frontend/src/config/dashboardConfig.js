const DEFAULT_REFRESH_INTERVAL_MS = 15000;
const MIN_REFRESH_INTERVAL_MS = 5000;

function resolveRefreshIntervalMs(value) {
  const parsedValue = Number(value);

  if (!Number.isFinite(parsedValue) || parsedValue <= 0) {
    return DEFAULT_REFRESH_INTERVAL_MS;
  }

  return Math.max(parsedValue, MIN_REFRESH_INTERVAL_MS);
}

export const dashboardRefreshIntervalMs = resolveRefreshIntervalMs(
  import.meta.env.VITE_DASHBOARD_REFRESH_MS,
);

export const dashboardRefreshIntervalSeconds = Math.round(
  dashboardRefreshIntervalMs / 1000,
);

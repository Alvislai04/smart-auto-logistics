const DEFAULT_ALERT_BADGE_REFRESH_MS = 15000;
const MIN_ALERT_BADGE_REFRESH_MS = 5000;

function resolveAlertBadgeRefreshMs(value) {
  const parsedValue = Number(value);

  if (!Number.isFinite(parsedValue) || parsedValue <= 0) {
    return DEFAULT_ALERT_BADGE_REFRESH_MS;
  }

  return Math.max(parsedValue, MIN_ALERT_BADGE_REFRESH_MS);
}

export const alertBadgeRefreshIntervalMs = resolveAlertBadgeRefreshMs(
  import.meta.env.VITE_ALERT_BADGE_REFRESH_MS,
);

export const alertCountRefreshEventName = "auto-logistics-alerts-changed";

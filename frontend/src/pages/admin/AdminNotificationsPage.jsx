import { useCallback, useEffect, useMemo, useState } from "react";
import { adminNotificationApi } from "../../api/adminNotificationApi";

function AdminNotificationsPage() {
  const [notifications, setNotifications] = useState([]);
  const [filter, setFilter] = useState("ALL");
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");

  const loadNotifications = useCallback(async () => {
    try {
      setIsLoading(true);
      setErrorMessage("");

      const data = await adminNotificationApi.getAll();
      setNotifications(data);
    } catch {
      setErrorMessage("Failed to load admin notifications.");
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    loadNotifications();
  }, [loadNotifications]);

  const filteredNotifications = useMemo(() => {
    if (filter === "ALL") {
      return notifications;
    }

    return notifications.filter(
      (notification) => notification.status === filter,
    );
  }, [notifications, filter]);

  const handleMarkAsRead = async (notificationId) => {
    await adminNotificationApi.markAsRead(notificationId);
    await loadNotifications();
  };

  if (isLoading) {
    return <p>Loading admin notifications...</p>;
  }

  if (errorMessage) {
    return <p className="error-message">{errorMessage}</p>;
  }

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h2>Admin Notifications</h2>
          <p>Review system integrity and blockchain monitoring alerts.</p>
        </div>

        <button type="button" onClick={loadNotifications}>
          Refresh
        </button>
      </div>

      <div className="panel">
        <div className="notification-toolbar">
          <select
            value={filter}
            onChange={(event) => setFilter(event.target.value)}
          >
            <option value="ALL">All Notifications</option>
            <option value="UNREAD">Unread</option>
            <option value="READ">Read</option>
          </select>
        </div>

        {filteredNotifications.length === 0 ? (
          <div className="empty-state">
            <p>No admin notifications found.</p>
          </div>
        ) : (
          <div className="notification-list">
            {filteredNotifications.map((notification) => (
              <div key={notification.id} className="notification-card">
                <div>
                  <div className="notification-card-header">
                    <strong>{notification.title}</strong>
                    <span
                      className={`notification-status ${notification.status.toLowerCase()}`}
                    >
                      {notification.status}
                    </span>
                  </div>

                  <p>{notification.message}</p>

                  <small>
                    Type: {notification.type} - Reference:{" "}
                    {notification.referenceType} #{notification.referenceId}
                  </small>

                  <br />

                  <small>Created At: {notification.createdAt}</small>
                </div>

                {notification.status === "UNREAD" && (
                  <button
                    type="button"
                    onClick={() => handleMarkAsRead(notification.id)}
                  >
                    Mark as Read
                  </button>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

export default AdminNotificationsPage;

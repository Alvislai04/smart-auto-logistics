import { useEffect, useMemo, useState } from "react";
import { companyNotificationApi } from "../../api/companyNotificationApi";

function CompanyNotificationsPage() {
  const [notifications, setNotifications] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    loadCompanyNotifications();
  }, []);

  const loadCompanyNotifications = async () => {
    try {
      setIsLoading(true);
      setErrorMessage("");

      const data = await companyNotificationApi.getAll();
      setNotifications(data);
    } catch (error) {
      setErrorMessage("Failed to load company notifications.");
    } finally {
      setIsLoading(false);
    }
  };

  const filteredNotifications = useMemo(() => {
    return notifications.filter((notification) => {
      const matchesStatus =
        statusFilter === "ALL" || notification.status === statusFilter;

      const searchableText = [
        notification.recipientCompany,
        notification.title,
        notification.message,
        notification.type,
        notification.status,
        notification.referenceType,
        notification.referenceId,
      ]
        .join(" ")
        .toLowerCase();

      const matchesSearch = searchableText.includes(searchTerm.toLowerCase());

      return matchesStatus && matchesSearch;
    });
  }, [notifications, searchTerm, statusFilter]);

  if (isLoading) {
    return <p>Loading company notifications...</p>;
  }

  if (errorMessage) {
    return <p className="error-message">{errorMessage}</p>;
  }

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h2>Company Notifications</h2>
          <p>
            Monitor notifications automatically prepared for company management
            when integrity issues are detected.
          </p>
        </div>

        <button type="button" onClick={loadCompanyNotifications}>
          Refresh
        </button>
      </div>

      <div className="panel">
        <div className="company-notification-toolbar">
          <input
            type="text"
            placeholder="Search by company, title, message, reference..."
            value={searchTerm}
            onChange={(event) => setSearchTerm(event.target.value)}
          />

          <select
            value={statusFilter}
            onChange={(event) => setStatusFilter(event.target.value)}
          >
            <option value="ALL">All Statuses</option>
            <option value="SENT">Sent</option>
            <option value="FAILED">Failed</option>
          </select>
        </div>

        {filteredNotifications.length === 0 ? (
          <div className="empty-state">
            <p>No company notifications found.</p>
          </div>
        ) : (
          <div className="company-notification-list">
            {filteredNotifications.map((notification) => (
              <div key={notification.id} className="company-notification-card">
                <div className="company-notification-header">
                  <div>
                    <strong>{notification.title}</strong>
                    <p>{notification.recipientCompany}</p>
                  </div>

                  <span
                    className={`company-notification-status ${notification.status.toLowerCase()}`}
                  >
                    {notification.status}
                  </span>
                </div>

                <p className="company-notification-message">
                  {notification.message}
                </p>

                <div className="company-notification-meta">
                  <span>Type: {notification.type}</span>
                  <span>
                    Reference: {notification.referenceType} #
                    {notification.referenceId}
                  </span>
                  <span>Sent At: {notification.sentAt}</span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

export default CompanyNotificationsPage;

import { Link } from "react-router-dom";

function AccessDeniedPage() {
  return (
    <div className="page">
      <div className="panel access-denied-panel">
        <h2>Access Denied</h2>
        <p>You do not have permission to access this page.</p>

        <Link className="button-link" to="/">
          Back to Dashboard
        </Link>
      </div>
    </div>
  );
}

export default AccessDeniedPage;

import { useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";

function Topbar() {
  const { currentUser, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login", { replace: true });
  };

  return (
    <header className="topbar">
      <div>
        <h1>Smart Automotive Logistics</h1>
        <p>Vehicle shipment tracking and integrity monitoring</p>
      </div>

      <div className="topbar-user">
        <div>
          <strong>{currentUser?.name}</strong>
          <span>{currentUser?.role?.replaceAll("_", " ")}</span>
        </div>

        <button type="button" onClick={handleLogout}>
          Logout
        </button>
      </div>
    </header>
  );
}

export default Topbar;

import { useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";

function LoginPage() {
  const { systemUsers, loginAsRole } = useAuth();
  const navigate = useNavigate();

  const handleLogin = (role) => {
    loginAsRole(role);
    navigate("/", { replace: true });
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-header">
          <h1>Smart Automotive Logistics System</h1>
          <p>Select your assigned role to access the logistics platform.</p>
        </div>

        <div className="role-login-grid">
          {systemUsers.map((user) => (
            <button
              key={user.id}
              type="button"
              className="role-login-card"
              onClick={() => handleLogin(user.role)}
            >
              <strong>{user.role.replaceAll("_", " ")}</strong>
              <span>{user.name}</span>
              <small>{user.email}</small>
            </button>
          ))}
        </div>

        <p className="auth-note">
          Local role-based access is enabled for the current development phase.
          Backend-secured JWT authentication will be added in the security
          phase.
        </p>
      </div>
    </div>
  );
}

export default LoginPage;

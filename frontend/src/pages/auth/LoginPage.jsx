import { useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";

function LoginPage() {
  const { currentUser, systemUsers, login } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const redirectPath = location.state?.from || "/";
  const hasExpiredSession =
    new URLSearchParams(location.search).get("session") === "expired";
  const [credentials, setCredentials] = useState({
    email: systemUsers[0]?.email ?? "",
    password: systemUsers[0]?.password ?? "",
  });
  const [errorMessage, setErrorMessage] = useState(
    hasExpiredSession ? "Your session expired. Please sign in again." : "",
  );
  const [isSubmitting, setIsSubmitting] = useState(false);

  const selectedRole = useMemo(
    () => systemUsers.find((user) => user.email === credentials.email)?.role,
    [credentials.email, systemUsers],
  );

  useEffect(() => {
    if (currentUser) {
      navigate(redirectPath, { replace: true });
    }
  }, [currentUser, navigate, redirectPath]);

  const handleCredentialChange = (event) => {
    const { name, value } = event.target;

    setCredentials((currentCredentials) => ({
      ...currentCredentials,
      [name]: value,
    }));
    setErrorMessage("");
  };

  const handlePresetSelect = (user) => {
    setCredentials({
      email: user.email,
      password: user.password,
    });
    setErrorMessage("");
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setIsSubmitting(true);
    setErrorMessage("");

    try {
      await login(credentials);
      navigate(redirectPath, { replace: true });
    } catch (error) {
      setErrorMessage(
        error.response?.data?.message ||
          "Unable to sign in. Please check the email and password.",
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-header">
          <h1>Smart Automotive Logistics System</h1>
          <p>Sign in to access the logistics platform.</p>
        </div>

        <form className="login-form" onSubmit={handleSubmit}>
          <label>
            Email
            <input
              type="email"
              name="email"
              value={credentials.email}
              onChange={handleCredentialChange}
              autoComplete="username"
              required
            />
          </label>

          <label>
            Password
            <input
              type="password"
              name="password"
              value={credentials.password}
              onChange={handleCredentialChange}
              autoComplete="current-password"
              required
            />
          </label>

          {errorMessage && (
            <p className="auth-error" role="alert">
              {errorMessage}
            </p>
          )}

          <button type="submit" disabled={isSubmitting}>
            {isSubmitting ? "Signing in..." : "Sign in"}
          </button>
        </form>

        <div className="role-login-grid">
          {systemUsers.map((user) => (
            <button
              key={user.id}
              type="button"
              className={`role-login-card ${
                selectedRole === user.role ? "is-selected" : ""
              }`}
              onClick={() => handlePresetSelect(user)}
            >
              <strong>{user.role.replaceAll("_", " ")}</strong>
              <span>{user.name}</span>
              <small>{user.email}</small>
            </button>
          ))}
        </div>

        <p className="auth-note">
          Seeded development accounts are authenticated by the backend and use
          JWT-protected API access.
        </p>
      </div>
    </div>
  );
}

export default LoginPage;

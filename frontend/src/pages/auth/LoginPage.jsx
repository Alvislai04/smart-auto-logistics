import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { getDashboardPathForRole } from "../../auth/roleDashboardRoutes";
import ArrowRightIcon from "../../components/icons/ArrowRightIcon";
import EyeIcon from "../../components/icons/EyeIcon";

function LoginPage() {
  const { currentUser, login } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const hasExpiredSession =
    new URLSearchParams(location.search).get("session") === "expired";
  const [credentials, setCredentials] = useState({
    email: "",
    password: "",
  });
  const [showPassword, setShowPassword] = useState(false);
  const [errorMessage, setErrorMessage] = useState(
    hasExpiredSession ? "Your session expired. Please sign in again." : "",
  );
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (currentUser) {
      navigate(getDashboardPathForRole(currentUser.role), { replace: true });
    }
  }, [currentUser, navigate]);

  const handleCredentialChange = (event) => {
    const { name, value } = event.target;

    setCredentials((currentCredentials) => ({
      ...currentCredentials,
      [name]: value,
    }));
    setErrorMessage("");
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setIsSubmitting(true);
    setErrorMessage("");

    try {
      const authenticatedUser = await login(credentials);
      navigate(getDashboardPathForRole(authenticatedUser.role), {
        replace: true,
      });
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
    <main className="auth-page">
      <section className="auth-brand-panel" aria-label="Smart Auto Logistics">
        <div className="auth-brand-topline">
          <div className="auth-logo-mark" aria-hidden="true">
            SAL
          </div>
          <div className="auth-wordmark">
            <strong>Smart Auto Logistics</strong>
            <span>Operations Console</span>
          </div>
        </div>

        <div className="auth-visual-shell" aria-hidden="true">
          <div className="auth-visual-header">
            <span>Vehicle Route</span>
            <strong>Active Control View</strong>
          </div>

          <div className="auth-route-board">
            <svg
              className="auth-route-svg"
              preserveAspectRatio="none"
              viewBox="0 0 720 360"
            >
              <defs>
                <pattern
                  height="36"
                  id="authGridPattern"
                  patternUnits="userSpaceOnUse"
                  width="36"
                >
                  <path
                    d="M 36 0 L 0 0 0 36"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="1"
                  />
                </pattern>
              </defs>
              <rect className="auth-grid-fill" height="360" width="720" />
              <path
                className="auth-route-line auth-route-shadow"
                d="M 78 278 C 190 222, 190 128, 315 146 S 448 246, 544 154 S 628 90, 660 104"
              />
              <path
                className="auth-route-line"
                d="M 78 278 C 190 222, 190 128, 315 146 S 448 246, 544 154 S 628 90, 660 104"
              />
              <path
                className="auth-route-alt"
                d="M 112 92 C 208 82, 262 118, 348 96 S 515 48, 620 64"
              />
              <circle className="auth-route-node" cx="78" cy="278" r="9" />
              <circle className="auth-route-node" cx="315" cy="146" r="7" />
              <circle
                className="auth-route-node auth-route-node-end"
                cx="660"
                cy="104"
                r="11"
              />
              <g className="auth-moving-vehicle">
                <circle r="8">
                  <animateMotion
                    dur="8s"
                    path="M 78 278 C 190 222, 190 128, 315 146 S 448 246, 544 154 S 628 90, 660 104"
                    repeatCount="indefinite"
                  />
                </circle>
              </g>
            </svg>

            <div className="auth-route-label auth-route-label-origin">
              <span>Origin</span>
              <strong>Assigned</strong>
            </div>
            <div className="auth-route-label auth-route-label-transit">
              <span>Transit</span>
              <strong>Tracked</strong>
            </div>
            <div className="auth-route-label auth-route-label-destination">
              <span>Destination</span>
              <strong>Verified</strong>
            </div>
          </div>

          <div className="auth-visual-metrics">
            <div>
              <span>Access</span>
              <strong>Role Based</strong>
            </div>
            <div>
              <span>Events</span>
              <strong>Audited</strong>
            </div>
            <div>
              <span>Alerts</span>
              <strong>Monitored</strong>
            </div>
          </div>
        </div>

        <div className="auth-brand-copy">
          <span>Secure Logistics Portal</span>
          <h1>One professional workspace for vehicle delivery control.</h1>
          <p>
            Sign in with your registered account to continue to the dashboard
            assigned to your operational role.
          </p>
        </div>
      </section>

      <section className="auth-login-panel" aria-label="Sign in">
        <form className="auth-login-card" onSubmit={handleSubmit}>
          <div className="auth-card-header">
            <div>
              <span>Authorized Access</span>
              <h2>Welcome back</h2>
            </div>
            <p>Production Login</p>
          </div>

          <div className="auth-card-body">
            <label className="auth-field">
              <span>Email Address</span>
              <input
                type="email"
                name="email"
                value={credentials.email}
                onChange={handleCredentialChange}
                autoComplete="username"
                placeholder="name@company.com"
                required
              />
            </label>

            <label className="auth-field">
              <span>Password</span>
              <div className="password-field">
                <input
                  type={showPassword ? "text" : "password"}
                  name="password"
                  value={credentials.password}
                  onChange={handleCredentialChange}
                  autoComplete="current-password"
                  placeholder="Enter password"
                  required
                />
                <button
                  type="button"
                  aria-label={showPassword ? "Hide password" : "Show password"}
                  className="password-toggle"
                  onClick={() => setShowPassword((currentValue) => !currentValue)}
                  title={showPassword ? "Hide password" : "Show password"}
                >
                  <EyeIcon hidden={showPassword} />
                </button>
              </div>
            </label>

            {errorMessage && (
              <p className="auth-error" role="alert">
                {errorMessage}
              </p>
            )}

            <button className="auth-submit" type="submit" disabled={isSubmitting}>
              {isSubmitting ? "Signing in..." : "Sign in"}
              <ArrowRightIcon />
            </button>
          </div>

          <div className="auth-card-footer">
            <span>Authorized users only</span>
            <span>Smart Auto Logistics</span>
          </div>
        </form>
      </section>
    </main>
  );
}

export default LoginPage;

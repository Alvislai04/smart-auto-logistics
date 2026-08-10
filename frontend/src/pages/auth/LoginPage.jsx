import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/AuthContext";
import { getDashboardPathForRole } from "../../auth/roleDashboardRoutes";
import ArrowRightIcon from "../../components/icons/ArrowRightIcon";
import EyeIcon from "../../components/icons/EyeIcon";

const tickerItems = [
  "Shipment visibility online",
  "GPS route monitoring active",
  "Blockchain audit trail ready",
  "Manufacturer dispatch secured",
  "Dealership receipt workflow live",
  "Alert rules synchronized",
];

const readouts = [
  {
    className: "auth-readout-one",
    label: "ROUTE MONITOR",
    value: "GPS ACTIVE",
    detail: "LIVE POSITION FEED",
  },
  {
    className: "auth-readout-two",
    label: "DISPATCH STATE",
    value: "SECURED",
    detail: "ROLE ACCESS ONLY",
  },
  {
    className: "auth-readout-three",
    label: "EVENT LEDGER",
    value: "READY",
    detail: "HASH VERIFICATION",
  },
  {
    className: "auth-readout-four",
    label: "CONTROL HUB",
    value: "ONLINE",
    detail: "SUPABASE LINKED",
  },
];

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
      navigate(
        getDashboardPathForRole(authenticatedUser.role),
        { replace: true },
      );
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
      <section className="auth-ops-panel" aria-label="Logistics operations">
        <svg
          aria-hidden="true"
          className="auth-grid-svg"
          preserveAspectRatio="xMidYMid slice"
          viewBox="0 0 800 900"
        >
          <defs>
            <pattern
              height="40"
              id="authGridPattern"
              patternUnits="userSpaceOnUse"
              width="40"
            >
              <path
                d="M 40 0 L 0 0 0 40"
                fill="none"
                stroke="#1c242d"
                strokeWidth="1"
              />
            </pattern>
            <linearGradient id="authMapFade" x1="0" x2="0" y1="0" y2="1">
              <stop offset="0%" stopColor="#0b0f14" stopOpacity="0" />
              <stop offset="100%" stopColor="#0b0f14" stopOpacity="0.55" />
            </linearGradient>
          </defs>
          <rect fill="url(#authGridPattern)" height="900" width="800" />
          <rect fill="url(#authMapFade)" height="900" width="800" />

          <path
            className="auth-route-main"
            d="M 90 760 C 180 650, 140 520, 260 470 S 480 430, 470 300 S 620 210, 690 130"
          />
          <path
            className="auth-route-secondary"
            d="M 700 780 C 600 760, 560 680, 460 660 S 260 600, 210 500"
          />
          <circle className="auth-node auth-node-start" cx="90" cy="760" r="4" />
          <circle className="auth-node auth-node-mid" cx="470" cy="300" r="4" />
          <circle className="auth-node auth-node-end" cx="690" cy="130" r="7" />
          <g className="auth-moving-marker">
            <circle fill="#0b0f14" r="5" stroke="#ffb020" strokeWidth="2">
              <animateMotion
                dur="7s"
                path="M 90 760 C 180 650, 140 520, 260 470 S 480 430, 470 300 S 620 210, 690 130"
                repeatCount="indefinite"
              />
            </circle>
          </g>
          <circle cx="620" cy="640" fill="#41505f" r="2.5" />
          <circle cx="330" cy="720" fill="#41505f" r="2.5" />
          <circle cx="560" cy="380" fill="#41505f" r="2.5" />
        </svg>

        <div className="auth-ops-header">
          <div className="auth-wordmark">
            AUTO<span>LOGISTICS</span>
            <small>CONTROL TERMINAL</small>
          </div>
          <div className="auth-network-pill">
            <span className="auth-status-dot" />
            NETWORK ONLINE
          </div>
        </div>

        <div className="auth-hero-copy">
          <p className="auth-eyebrow">// SMART VEHICLE DISPATCH</p>
          <h1>Every vehicle, every route, one control room.</h1>
          <p>
            Monitor shipments, verify logistics events, and keep each delivery
            stage visible from origin to dealership.
          </p>
        </div>

        <div className="auth-readouts">
          {readouts.map((readout) => (
            <div
              className={`auth-readout ${readout.className}`}
              key={readout.label}
            >
              {readout.label}
              <br />
              <span>{readout.value}</span>
              <br />
              {readout.detail}
            </div>
          ))}
        </div>

        <div className="auth-ticker" aria-hidden="true">
          <div className="auth-ticker-track">
            {[...tickerItems, ...tickerItems].map((item, index) => (
              <span key={`${item}-${index}`}>{item}</span>
            ))}
          </div>
        </div>
      </section>

      <section className="auth-side" aria-label="Sign in">
        <form className="auth-ticket" onSubmit={handleSubmit}>
          <div className="auth-ticket-head">
            <div>
              <p>ACCESS DOCUMENT NO.</p>
              <strong>SAL-SECURE-01</strong>
            </div>
            <span>LOGIN</span>
          </div>

          <div className="auth-ticket-body">
            <div className="auth-ticket-title">
              <h2>Sign in to console</h2>
              <p>Use your registered operator account.</p>
            </div>

            <label className="auth-field">
              <span>Email</span>
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

            <p className="auth-divider-note">SECURE LOGISTICS ACCESS</p>
          </div>

          <div className="auth-ticket-foot">
            <div className="auth-barcode" aria-hidden="true">
              <span className="auth-bar-tall" />
              <span className="auth-bar-short" />
              <span className="auth-bar-mid" />
              <span className="auth-bar-short" />
              <span className="auth-bar-mid" />
              <span className="auth-bar-tall" />
              <span className="auth-bar-short" />
              <span className="auth-bar-mid" />
              <span className="auth-bar-tall" />
              <span className="auth-bar-short" />
              <span className="auth-bar-mid" />
              <span className="auth-bar-short" />
            </div>
            <p>
              ISSUED TODAY
              <br />
              VALID SESSION
            </p>
          </div>
        </form>
      </section>
    </div>
  );
}

export default LoginPage;

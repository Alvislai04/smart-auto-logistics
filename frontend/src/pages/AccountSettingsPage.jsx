import { useCallback, useEffect, useState } from "react";
import { userApi } from "../api/userApi";
import { useAuth } from "../auth/AuthContext";
import { formatEnumLabel } from "../utils/formatters";

const emptyProfileForm = {
  fullName: "",
  email: "",
  role: "",
  companyName: "",
  operatingLocation: "",
  locationLatitude: "",
  locationLongitude: "",
};

const emptyPasswordForm = {
  currentPassword: "",
  newPassword: "",
  confirmPassword: "",
};

function toFormValue(value) {
  return value == null ? "" : String(value);
}

function nullableNumber(value) {
  if (value === "" || value === null || value === undefined) {
    return null;
  }

  return Number(value);
}

function getErrorMessage(error, fallbackMessage) {
  return error.response?.data?.message || fallbackMessage;
}

function requiresCompanyProfile(role) {
  return role && role !== "ADMIN";
}

function AccountSettingsPage() {
  const { updateCurrentUser } = useAuth();
  const [profileForm, setProfileForm] = useState(emptyProfileForm);
  const [passwordForm, setPasswordForm] = useState(emptyPasswordForm);
  const [isLoading, setIsLoading] = useState(true);
  const [isSavingProfile, setIsSavingProfile] = useState(false);
  const [isSavingPassword, setIsSavingPassword] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  const companyProfileRequired = requiresCompanyProfile(profileForm.role);

  const loadProfile = useCallback(async () => {
    try {
      setIsLoading(true);
      setErrorMessage("");

      const profile = await userApi.getProfile();
      setProfileForm({
        fullName: profile.fullName ?? "",
        email: profile.email ?? "",
        role: profile.role ?? "",
        companyName: profile.companyName ?? "",
        operatingLocation: profile.operatingLocation ?? "",
        locationLatitude: toFormValue(profile.locationLatitude),
        locationLongitude: toFormValue(profile.locationLongitude),
      });
      updateCurrentUser(profile);
    } catch (error) {
      setErrorMessage(getErrorMessage(error, "Failed to load account profile."));
    } finally {
      setIsLoading(false);
    }
  }, [updateCurrentUser]);

  useEffect(() => {
    loadProfile();
  }, [loadProfile]);

  const handleProfileChange = (event) => {
    const { name, value } = event.target;

    setProfileForm((previousForm) => ({
      ...previousForm,
      [name]: value,
    }));
  };

  const handlePasswordChange = (event) => {
    const { name, value } = event.target;

    setPasswordForm((previousForm) => ({
      ...previousForm,
      [name]: value,
    }));
  };

  const validateProfile = () => {
    if (
      companyProfileRequired &&
      (!profileForm.companyName.trim() ||
        !profileForm.operatingLocation.trim() ||
        profileForm.locationLatitude === "" ||
        profileForm.locationLongitude === "")
    ) {
      setErrorMessage(
        "Company name, operating location, latitude, and longitude are required for your role.",
      );
      return false;
    }

    return true;
  };

  const handleProfileSubmit = async (event) => {
    event.preventDefault();

    if (!validateProfile()) {
      return;
    }

    try {
      setIsSavingProfile(true);
      setErrorMessage("");
      setSuccessMessage("");

      const updatedProfile = await userApi.updateProfile({
        fullName: profileForm.fullName.trim(),
        companyName: profileForm.companyName.trim() || null,
        operatingLocation: profileForm.operatingLocation.trim() || null,
        locationLatitude: nullableNumber(profileForm.locationLatitude),
        locationLongitude: nullableNumber(profileForm.locationLongitude),
      });

      updateCurrentUser(updatedProfile);
      setSuccessMessage("Account profile updated.");
    } catch (error) {
      setErrorMessage(getErrorMessage(error, "Failed to update account profile."));
    } finally {
      setIsSavingProfile(false);
    }
  };

  const handlePasswordSubmit = async (event) => {
    event.preventDefault();

    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      setErrorMessage("Password confirmation does not match.");
      return;
    }

    try {
      setIsSavingPassword(true);
      setErrorMessage("");
      setSuccessMessage("");

      await userApi.changePassword(passwordForm);
      setPasswordForm(emptyPasswordForm);
      setSuccessMessage("Password changed.");
    } catch (error) {
      setErrorMessage(getErrorMessage(error, "Failed to change password."));
    } finally {
      setIsSavingPassword(false);
    }
  };

  if (isLoading) {
    return <p>Loading account settings...</p>;
  }

  return (
    <div className="page account-page">
      <div className="page-header">
        <div>
          <h2>My Account</h2>
          <p>Maintain your profile details and password.</p>
        </div>

        <button type="button" onClick={loadProfile}>
          Refresh
        </button>
      </div>

      {errorMessage && <p className="error-message">{errorMessage}</p>}
      {successMessage && <p className="success-message">{successMessage}</p>}

      <div className="account-settings-layout">
        <form className="form-panel account-form" onSubmit={handleProfileSubmit}>
          <div className="form-section-header">
            <h3>Profile</h3>
          </div>

          <div className="form-grid">
            <label>
              Full Name
              <input
                type="text"
                name="fullName"
                value={profileForm.fullName}
                onChange={handleProfileChange}
                required
              />
            </label>

            <label>
              Email
              <input
                className="readonly-input"
                type="email"
                value={profileForm.email}
                readOnly
              />
            </label>

            <label>
              Role
              <input
                className="readonly-input"
                type="text"
                value={formatEnumLabel(profileForm.role)}
                readOnly
              />
            </label>

            <label>
              Company Name
              <input
                type="text"
                name="companyName"
                value={profileForm.companyName}
                onChange={handleProfileChange}
                required={companyProfileRequired}
              />
            </label>

            <label className="form-field-full">
              Operating Location
              <input
                type="text"
                name="operatingLocation"
                value={profileForm.operatingLocation}
                onChange={handleProfileChange}
                required={companyProfileRequired}
              />
            </label>

            <label>
              Latitude
              <input
                type="number"
                name="locationLatitude"
                value={profileForm.locationLatitude}
                onChange={handleProfileChange}
                min="-90"
                max="90"
                step="0.000001"
                required={companyProfileRequired}
              />
            </label>

            <label>
              Longitude
              <input
                type="number"
                name="locationLongitude"
                value={profileForm.locationLongitude}
                onChange={handleProfileChange}
                min="-180"
                max="180"
                step="0.000001"
                required={companyProfileRequired}
              />
            </label>
          </div>

          <div className="form-actions">
            <button type="submit" disabled={isSavingProfile}>
              {isSavingProfile ? "Saving..." : "Save Profile"}
            </button>
          </div>
        </form>

        <form
          className="form-panel account-password-form"
          onSubmit={handlePasswordSubmit}
        >
          <div className="form-section-header">
            <h3>Password</h3>
          </div>

          <label>
            Current Password
            <input
              type="password"
              name="currentPassword"
              value={passwordForm.currentPassword}
              onChange={handlePasswordChange}
              required
            />
          </label>

          <label>
            New Password
            <input
              type="password"
              name="newPassword"
              value={passwordForm.newPassword}
              onChange={handlePasswordChange}
              required
            />
          </label>

          <label>
            Confirm New Password
            <input
              type="password"
              name="confirmPassword"
              value={passwordForm.confirmPassword}
              onChange={handlePasswordChange}
              required
            />
          </label>

          <div className="form-actions">
            <button type="submit" disabled={isSavingPassword}>
              {isSavingPassword ? "Saving..." : "Change Password"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default AccountSettingsPage;

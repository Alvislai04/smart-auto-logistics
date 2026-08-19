import { useCallback, useEffect, useMemo, useState } from "react";
import { userApi } from "../../api/userApi";
import { useToast } from "../../components/feedback/ToastContext";
import { formatCoordinate, formatDateTime, formatEnumLabel } from "../../utils/formatters";

const emptyForm = {
  id: null,
  fullName: "",
  email: "",
  roleKey: "",
  password: "",
  confirmPassword: "",
  companyName: "",
  operatingLocation: "",
  locationLatitude: "",
  locationLongitude: "",
  active: true,
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

function requiresCompanyProfile(roleKey) {
  return roleKey && roleKey !== "ADMIN";
}

function buildCreatePayload(formData) {
  return {
    fullName: formData.fullName.trim(),
    email: formData.email.trim(),
    roleKey: formData.roleKey,
    password: formData.password,
    confirmPassword: formData.confirmPassword,
    companyName: formData.companyName.trim() || null,
    operatingLocation: formData.operatingLocation.trim() || null,
    locationLatitude: nullableNumber(formData.locationLatitude),
    locationLongitude: nullableNumber(formData.locationLongitude),
    active: formData.active,
  };
}

function buildUpdatePayload(formData) {
  return {
    fullName: formData.fullName.trim(),
    roleKey: formData.roleKey,
    companyName: formData.companyName.trim() || null,
    operatingLocation: formData.operatingLocation.trim() || null,
    locationLatitude: nullableNumber(formData.locationLatitude),
    locationLongitude: nullableNumber(formData.locationLongitude),
    active: formData.active,
  };
}

function UserAccountsPage() {
  const { showToast } = useToast();
  const [users, setUsers] = useState([]);
  const [roleOptions, setRoleOptions] = useState([]);
  const [formData, setFormData] = useState(emptyForm);
  const [searchTerm, setSearchTerm] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  const isEditing = Boolean(formData.id);
  const companyProfileRequired = requiresCompanyProfile(formData.roleKey);

  const loadPageData = useCallback(async () => {
    try {
      setIsLoading(true);
      setErrorMessage("");

      const [usersData, optionsData] = await Promise.all([
        userApi.getUsers(),
        userApi.getFormOptions(),
      ]);

      setUsers(usersData);
      setRoleOptions(optionsData.roleOptions ?? []);
    } catch (error) {
      setErrorMessage(
        getErrorMessage(error, "Failed to load user account data."),
      );
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    loadPageData();
  }, [loadPageData]);

  const filteredUsers = useMemo(() => {
    const normalizedSearch = searchTerm.trim().toLowerCase();

    if (!normalizedSearch) {
      return users;
    }

    return users.filter((user) =>
      [
        user.fullName,
        user.email,
        user.role,
        user.companyName,
        user.operatingLocation,
        user.partyProfileKey,
      ]
        .join(" ")
        .toLowerCase()
        .includes(normalizedSearch),
    );
  }, [searchTerm, users]);

  const handleChange = (event) => {
    const { name, value, type, checked } = event.target;

    setFormData((previousFormData) => ({
      ...previousFormData,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  const handleReset = () => {
    setFormData(emptyForm);
    setErrorMessage("");
    setSuccessMessage("");
  };

  const handleEdit = (user) => {
    setFormData({
      id: user.id,
      fullName: user.fullName ?? "",
      email: user.email ?? "",
      roleKey: user.role ?? "",
      password: "",
      confirmPassword: "",
      companyName: user.companyName ?? "",
      operatingLocation: user.operatingLocation ?? "",
      locationLatitude: toFormValue(user.locationLatitude),
      locationLongitude: toFormValue(user.locationLongitude),
      active: user.active,
    });
    setErrorMessage("");
    setSuccessMessage("");
  };

  const validateForm = () => {
    if (!formData.roleKey) {
      setErrorMessage("Please select an account role.");
      return false;
    }

    if (!isEditing && formData.password !== formData.confirmPassword) {
      setErrorMessage("Password confirmation does not match.");
      return false;
    }

    if (
      companyProfileRequired &&
      (!formData.companyName.trim() ||
        !formData.operatingLocation.trim() ||
        formData.locationLatitude === "" ||
        formData.locationLongitude === "")
    ) {
      setErrorMessage(
        "Company name, operating location, latitude, and longitude are required for non-admin users.",
      );
      return false;
    }

    return true;
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!validateForm()) {
      return;
    }

    try {
      setIsSaving(true);
      setErrorMessage("");
      setSuccessMessage("");

      if (isEditing) {
        const updatedUser = await userApi.updateUser(
          formData.id,
          buildUpdatePayload(formData),
        );
        setSuccessMessage("User account updated.");
        showToast({
          type: "success",
          title: "User account updated",
          message: `${updatedUser.fullName} was saved successfully.`,
        });
      } else {
        const createdUser = await userApi.createUser(buildCreatePayload(formData));
        setSuccessMessage("User account registered.");
        showToast({
          type: "success",
          title: "User account registered",
          message: `${createdUser.fullName} can now sign in.`,
        });
        setFormData(emptyForm);
      }

      await loadPageData();
    } catch (error) {
      const message = getErrorMessage(error, "Failed to save user account.");
      setErrorMessage(message);
      showToast({
        type: "error",
        title: "User account save failed",
        message,
      });
    } finally {
      setIsSaving(false);
    }
  };

  if (isLoading) {
    return <p>Loading user accounts...</p>;
  }

  return (
    <div className="page account-page">
      <div className="page-header">
        <div>
          <h2>User Accounts</h2>
          <p>Register and maintain system access for every operational role.</p>
        </div>

        <div className="header-actions">
          <button type="button" onClick={loadPageData}>
            Refresh
          </button>
        </div>
      </div>

      {errorMessage && <p className="error-message">{errorMessage}</p>}
      {successMessage && <p className="success-message">{successMessage}</p>}

      <div className="account-management-layout">
        <form className="form-panel account-form" onSubmit={handleSubmit}>
          <div className="form-section-header">
            <h3>{isEditing ? "Edit Account" : "Register Account"}</h3>
          </div>

          <div className="form-grid">
            <label>
              Full Name
              <input
                type="text"
                name="fullName"
                value={formData.fullName}
                onChange={handleChange}
                placeholder="Example: Aisyah Rahman"
                required
              />
            </label>

            <label>
              Email
              <input
                className={isEditing ? "readonly-input" : ""}
                type="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                placeholder="Example: user@company.com"
                readOnly={isEditing}
                required
              />
            </label>

            <label>
              Role
              <select
                name="roleKey"
                value={formData.roleKey}
                onChange={handleChange}
                required
              >
                <option value="">Select account role</option>
                {roleOptions.map((role) => (
                  <option key={role.key} value={role.key}>
                    {role.displayName}
                  </option>
                ))}
              </select>
            </label>

            {!isEditing && (
              <>
                <label>
                  Temporary Password
                  <input
                    type="password"
                    name="password"
                    value={formData.password}
                    onChange={handleChange}
                    placeholder="Set initial password"
                    required
                  />
                </label>

                <label>
                  Confirm Password
                  <input
                    type="password"
                    name="confirmPassword"
                    value={formData.confirmPassword}
                    onChange={handleChange}
                    placeholder="Repeat initial password"
                    required
                  />
                </label>
              </>
            )}

            <label>
              Company Name
              <input
                type="text"
                name="companyName"
                value={formData.companyName}
                onChange={handleChange}
                placeholder="Example: Mercedes-Benz Malaysia"
                required={companyProfileRequired}
              />
            </label>

            <label className="form-field-full">
              Operating Location
              <input
                type="text"
                name="operatingLocation"
                value={formData.operatingLocation}
                onChange={handleChange}
                placeholder="Example: Pekan Assembly Plant, Pahang"
                required={companyProfileRequired}
              />
            </label>

            <label>
              Latitude
              <input
                type="number"
                name="locationLatitude"
                value={formData.locationLatitude}
                onChange={handleChange}
                min="-90"
                max="90"
                step="0.000001"
                placeholder="Example: 3.511300"
                required={companyProfileRequired}
              />
            </label>

            <label>
              Longitude
              <input
                type="number"
                name="locationLongitude"
                value={formData.locationLongitude}
                onChange={handleChange}
                min="-180"
                max="180"
                step="0.000001"
                placeholder="Example: 103.397400"
                required={companyProfileRequired}
              />
            </label>

            <label className="checkbox-field account-active-field">
              <input
                type="checkbox"
                name="active"
                checked={formData.active}
                onChange={handleChange}
              />
              Active account
            </label>
          </div>

          <div className="form-actions">
            <button type="button" className="secondary-button" onClick={handleReset}>
              Clear
            </button>
            <button type="submit" disabled={isSaving || roleOptions.length === 0}>
              {isSaving
                ? "Saving..."
                : isEditing
                  ? "Save Changes"
                  : "Register Account"}
            </button>
          </div>
        </form>

        <section className="table-card account-users-panel">
          <div className="account-table-toolbar">
            <h3>Registered Users</h3>
            <input
              type="search"
              value={searchTerm}
              onChange={(event) => setSearchTerm(event.target.value)}
              placeholder="Search users"
            />
          </div>

          {filteredUsers.length === 0 ? (
            <div className="empty-state">
              <p>No user accounts found.</p>
            </div>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>User</th>
                  <th>Role</th>
                  <th>Company</th>
                  <th>Location</th>
                  <th>Status</th>
                  <th>Updated</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {filteredUsers.map((user) => (
                  <tr key={user.id}>
                    <td>
                      <strong>{user.fullName}</strong>
                      <br />
                      <small>{user.email}</small>
                    </td>
                    <td>{formatEnumLabel(user.role)}</td>
                    <td>
                      {user.companyName || "-"}
                      {user.partyProfileKey && (
                        <>
                          <br />
                          <small>{user.partyProfileKey}</small>
                        </>
                      )}
                    </td>
                    <td>
                      {user.operatingLocation || "-"}
                      <br />
                      <small>
                        {formatCoordinate(user.locationLatitude)},{" "}
                        {formatCoordinate(user.locationLongitude)}
                      </small>
                    </td>
                    <td>
                      <span
                        className={`account-status ${
                          user.active ? "active" : "inactive"
                        }`}
                      >
                        {user.active ? "Active" : "Inactive"}
                      </span>
                    </td>
                    <td>{formatDateTime(user.updatedAt)}</td>
                    <td>
                      <button type="button" onClick={() => handleEdit(user)}>
                        Edit
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </section>
      </div>
    </div>
  );
}

export default UserAccountsPage;

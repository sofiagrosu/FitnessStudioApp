import { useEffect, useState } from "react";
import Sidebar from "../components/Sidebar";
import StatCard from "../components/StatCard";
import { Users, Trash2, UserPlus, Plus, Pencil } from "lucide-react";
import {
  getAllTrainers,
  getAllReceptionists,
  addUser,
  deactivateUser,
  updateTrainer,
  updateReceptionist,
} from "../services/adminService";
import {
  getCourses,
  createCourse,
  updateCourse,
  deleteCourse,
} from "../services/courseService";
import {
  getLocations,
  createLocation,
  updateLocation,
  deleteLocation,
} from "../services/locationService";
import { getCurrentUser } from "../services/authService";

const EMPTY_USER_FORM = {
  firstName: "",
  lastName: "",
  email: "",
  password: "",
  phone: "",
};

const EMPTY_COURSE_FORM = {
  name: "",
  type: "YOGA",
  trainerId: "",
  locationId: "",
  dayOfWeek: "MONDAY",
  startTime: "09:00",
  duration: 60,
  maxCapacity: 20,
  recurring: true,
};

const EMPTY_LOCATION_FORM = {
  name: "",
  address: "",
};

const COURSE_TYPES = ["YOGA", "SPINNING", "ZUMBA", "PILATES"];
const DAYS_OF_WEEK = [
  "MONDAY",
  "TUESDAY",
  "WEDNESDAY",
  "THURSDAY",
  "FRIDAY",
  "SATURDAY",
  "SUNDAY",
];

function AdminDashboard() {
  const [trainers, setTrainers] = useState([]);
  const [receptionists, setReceptionists] = useState([]);
  const [courses, setCourses] = useState([]);
  const [locations, setLocations] = useState([]);

  const [tab, setTab] = useState("trainers");
  const [showForm, setShowForm] = useState(false);

  const [editingUser, setEditingUser] = useState(null);
  const [editingCourse, setEditingCourse] = useState(null);
  const [editingLocation, setEditingLocation] = useState(null);

  const [userForm, setUserForm] = useState(EMPTY_USER_FORM);
  const [courseForm, setCourseForm] = useState(EMPTY_COURSE_FORM);
  const [locationForm, setLocationForm] = useState(EMPTY_LOCATION_FORM);

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const currentUser = getCurrentUser();
  const isAdmin = currentUser?.role === "ADMIN";

  useEffect(() => {
    loadData();
  }, []);

  async function loadData() {
    try {
      const [t, r, c, l] = await Promise.all([
        getAllTrainers(),
        getAllReceptionists(),
        getCourses(),
        getLocations(),
      ]);

      setTrainers(t.data || []);
      setReceptionists(r.data || []);
      setCourses(c.data || []);
      setLocations(l.data || []);
    } catch (e) {
      console.error(e);
      setError("Could not load admin data.");
    }
  }

  function resetForms() {
    setEditingUser(null);
    setEditingCourse(null);
    setEditingLocation(null);
    setUserForm(EMPTY_USER_FORM);
    setCourseForm(EMPTY_COURSE_FORM);
    setLocationForm(EMPTY_LOCATION_FORM);
    setError("");
    setSuccess("");
  }

  function handleTabChange(nextTab) {
    setTab(nextTab);
    setShowForm(false);
    resetForms();
  }

  function getAddButtonLabel() {
    if (tab === "trainers") return "Add Trainer";
    if (tab === "receptionists") return "Add Receptionist";
    if (tab === "courses") return "Add Course";
    return "Add Location";
  }

  function getAddButtonIcon() {
    if (tab === "courses" || tab === "locations") return <Plus size={16} />;
    return <UserPlus size={16} />;
  }

  function handleAddButtonClick() {
    resetForms();
    setShowForm(!showForm);
  }

  async function handleAddUser(e) {
    e.preventDefault();

    if (!isAdmin) {
      setError("Only admins can add users.");
      return;
    }

    setError("");
    setSuccess("");

    const role = tab === "trainers" ? "TRAINER" : "RECEPTIONIST";

    try {
      await addUser({ ...userForm, role, type: role });
      setUserForm(EMPTY_USER_FORM);
      setShowForm(false);
      setSuccess(`${role === "TRAINER" ? "Trainer" : "Receptionist"} added successfully.`);
      loadData();
    } catch (err) {
      setError(err.response?.data?.message || "Error adding user.");
    }
  }

  function handleUserEditClick(user) {
    setEditingUser(user);

    setUserForm({
      firstName: user.firstName || "",
      lastName: user.lastName || "",
      email: user.email || "",
      password: "",
      phone: user.phone || "",
    });

    setShowForm(true);
    setError("");
    setSuccess("");
  }

  async function handleUpdateUser(e) {
    e.preventDefault();

    if (!isAdmin) {
      setError("Only admins can update users.");
      return;
    }

    setError("");
    setSuccess("");

    try {
      if (tab === "trainers") {
        await updateTrainer(editingUser.id, userForm);
      } else {
        await updateReceptionist(editingUser.id, userForm);
      }

      setUserForm(EMPTY_USER_FORM);
      setShowForm(false);
      setEditingUser(null);
      setSuccess(`${tab === "trainers" ? "Trainer" : "Receptionist"} updated successfully.`);
      loadData();
    } catch (err) {
      setError(err.response?.data?.message || "Error updating user.");
    }
  }

  async function handleDeactivate(id) {
    if (!isAdmin) {
      setError("Only admins can deactivate users.");
      return;
    }

    if (!window.confirm("Deactivate this user?")) return;

    setError("");
    setSuccess("");

    try {
      await deactivateUser(id);
      setSuccess("User deactivated.");
      loadData();
    } catch (err) {
      setError(err.response?.data?.message || "Error deactivating user.");
    }
  }

  function buildCoursePayload(form) {
    return {
      name: form.name,
      type: form.type,
      trainer: { id: Number(form.trainerId) },
      location: { id: Number(form.locationId) },
      dayOfWeek: form.dayOfWeek,
      startTime: form.startTime.length === 5 ? form.startTime + ":00" : form.startTime,
      duration: Number(form.duration),
      maxCapacity: Number(form.maxCapacity),
      recurring: form.recurring,
      currentOccupancy: 0,
    };
  }

  async function handleAddCourse(e) {
    e.preventDefault();

    if (!isAdmin) {
      setError("Only admins can add courses.");
      return;
    }

    setError("");
    setSuccess("");

    if (!courseForm.trainerId) {
      setError("Please select a trainer.");
      return;
    }

    if (!courseForm.locationId) {
      setError("Please select a location.");
      return;
    }

    try {
      await createCourse(buildCoursePayload(courseForm));
      setCourseForm(EMPTY_COURSE_FORM);
      setShowForm(false);
      setSuccess("Course created successfully.");
      loadData();
    } catch (err) {
      setError(err.response?.data?.message || "Error creating course.");
    }
  }

  function handleEditCourseClick(course) {
    setEditingCourse(course.id);

    setCourseForm({
      name: course.name || "",
      type: course.type || "YOGA",
      trainerId: course.trainer?.id || "",
      locationId: course.location?.id || "",
      dayOfWeek: course.dayOfWeek || "MONDAY",
      startTime: course.startTime?.slice(0, 5) || "09:00",
      duration: course.duration || 60,
      maxCapacity: course.maxCapacity || 20,
      recurring: course.recurring ?? true,
    });

    setShowForm(true);
    setError("");
    setSuccess("");
  }

  async function handleUpdateCourse(e) {
    e.preventDefault();

    if (!isAdmin) {
      setError("Only admins can update courses.");
      return;
    }

    setError("");
    setSuccess("");

    if (!courseForm.trainerId) {
      setError("Please select a trainer.");
      return;
    }

    if (!courseForm.locationId) {
      setError("Please select a location.");
      return;
    }

    try {
      await updateCourse(editingCourse, buildCoursePayload(courseForm));
      setCourseForm(EMPTY_COURSE_FORM);
      setShowForm(false);
      setEditingCourse(null);
      setSuccess("Course updated successfully.");
      loadData();
    } catch (err) {
      setError(err.response?.data?.message || "Error updating course.");
    }
  }

  async function handleDeleteCourse(id) {
    if (!isAdmin) {
      setError("Only admins can delete courses.");
      return;
    }

    if (!window.confirm("Delete this course?")) return;

    setError("");
    setSuccess("");

    try {
      await deleteCourse(id);
      setSuccess("Course deleted.");
      loadData();
    } catch (err) {
      setError(err.response?.data?.message || "Error deleting course.");
    }
  }

  function handleEditLocationClick(location) {
    setEditingLocation(location.id);

    setLocationForm({
      name: location.name || "",
      address: location.address || "",
    });

    setShowForm(true);
    setError("");
    setSuccess("");
  }

  async function handleAddLocation(e) {
    e.preventDefault();

    if (!isAdmin) {
      setError("Only admins can add locations.");
      return;
    }

    setError("");
    setSuccess("");

    try {
      await createLocation(locationForm);
      setLocationForm(EMPTY_LOCATION_FORM);
      setShowForm(false);
      setSuccess("Location created successfully.");
      loadData();
    } catch (err) {
      setError(err.response?.data?.message || "Error creating location.");
    }
  }

  async function handleUpdateLocation(e) {
    e.preventDefault();

    if (!isAdmin) {
      setError("Only admins can update locations.");
      return;
    }

    setError("");
    setSuccess("");

    try {
      await updateLocation(editingLocation, locationForm);
      setEditingLocation(null);
      setLocationForm(EMPTY_LOCATION_FORM);
      setShowForm(false);
      setSuccess("Location updated successfully.");
      loadData();
    } catch (err) {
      setError(err.response?.data?.message || "Error updating location.");
    }
  }

  async function handleDeleteLocation(id) {
    if (!isAdmin) {
      setError("Only admins can delete locations.");
      return;
    }

    if (!window.confirm("Delete this location?")) return;

    setError("");
    setSuccess("");

    try {
      await deleteLocation(id);
      setSuccess("Location deleted.");
      loadData();
    } catch (err) {
      setError(err.response?.data?.message || "Error deleting location.");
    }
  }

  const userList = tab === "trainers" ? trainers : receptionists;

  return (
    <div className="app-shell">
      <Sidebar />

      <main>
        <h1 className="section-title">Admin Panel</h1>
        <p className="section-subtitle">
          Manage trainers, receptionists, courses and locations.
        </p>

        <div className="grid-2" style={{ marginBottom: 32 }}>
          <StatCard
            title="Trainers"
            value={trainers.filter((u) => u.active).length}
            subtitle="Active"
            icon={<Users size={20} />}
          />

          <StatCard
            title="Receptionists"
            value={receptionists.filter((u) => u.active).length}
            subtitle="Active"
            icon={<Users size={20} />}
          />
        </div>

        <div style={{ display: "flex", gap: 12, marginBottom: 20, alignItems: "center" }}>
          {["trainers", "receptionists", "courses", "locations"].map((t) => (
            <button
              key={t}
              className={tab === t ? "primary-btn" : "secondary-btn"}
              onClick={() => handleTabChange(t)}
            >
              {t.charAt(0).toUpperCase() + t.slice(1)}
            </button>
          ))}

          {isAdmin && (
            <button
              className="primary-btn"
              style={{ marginLeft: "auto", display: "flex", alignItems: "center", gap: 8 }}
              onClick={handleAddButtonClick}
            >
              {getAddButtonIcon()}
              {getAddButtonLabel()}
            </button>
          )}
        </div>

        {error && <div className="error-box" style={{ marginBottom: 16 }}>{error}</div>}
        {success && <div className="info-box" style={{ marginBottom: 16 }}>{success}</div>}

        {showForm && (tab === "trainers" || tab === "receptionists") && (
          <div className="card form-card" style={{ marginBottom: 24 }}>
            <h3 style={{ marginBottom: 20 }}>
              {editingUser
                ? `Edit ${tab === "trainers" ? "Trainer" : "Receptionist"}`
                : `New ${tab === "trainers" ? "Trainer" : "Receptionist"}`}
            </h3>

            <form onSubmit={editingUser ? handleUpdateUser : handleAddUser}>
              <label>First Name</label>
              <input
                placeholder="First name"
                value={userForm.firstName}
                required
                onChange={(e) => setUserForm({ ...userForm, firstName: e.target.value })}
              />

              <label>Last Name</label>
              <input
                placeholder="Last name"
                value={userForm.lastName}
                required
                onChange={(e) => setUserForm({ ...userForm, lastName: e.target.value })}
              />

              <label>Email</label>
              <input
                type="email"
                placeholder="email@example.com"
                value={userForm.email}
                required
                onChange={(e) => setUserForm({ ...userForm, email: e.target.value })}
              />

              <label>Phone</label>
              <input
                type="tel"
                placeholder="ex: 0740123456"
                value={userForm.phone}
                pattern="\d{10}"
                maxLength={10}
                title="Phone number must be exactly 10 digits"
                onChange={(e) =>
                  setUserForm({ ...userForm, phone: e.target.value.replace(/\D/g, "") })
                }
              />

              <label>{editingUser ? "New Password (leave blank to keep current)" : "Password"}</label>
              <input
                type="password"
                placeholder={editingUser ? "Leave blank to keep current" : "Password"}
                value={userForm.password}
                required={!editingUser}
                onChange={(e) => setUserForm({ ...userForm, password: e.target.value })}
              />

              <div style={{ display: "flex", gap: 12, marginTop: 10 }}>
                <button type="submit" className="primary-btn" style={{ flex: 1 }}>
                  {editingUser ? "Save changes" : "Save"}
                </button>

                <button
                  type="button"
                  className="secondary-btn"
                  style={{ flex: 1 }}
                  onClick={() => {
                    setShowForm(false);
                    setUserForm(EMPTY_USER_FORM);
                    setEditingUser(null);
                  }}
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        )}

        {showForm && tab === "courses" && (
          <div className="card form-card" style={{ marginBottom: 24 }}>
            <h3 style={{ marginBottom: 20 }}>
              {editingCourse ? "Edit Course" : "New Course"}
            </h3>

            <form onSubmit={editingCourse ? handleUpdateCourse : handleAddCourse}>
              <label>Name</label>
              <input
                placeholder="Course name"
                value={courseForm.name}
                required
                onChange={(e) => setCourseForm({ ...courseForm, name: e.target.value })}
              />

              <label>Type</label>
              <select
                value={courseForm.type}
                onChange={(e) => setCourseForm({ ...courseForm, type: e.target.value })}
                style={{
                  width: "100%",
                  padding: "14px 16px",
                  border: "1px solid var(--border)",
                  borderRadius: 14,
                  marginBottom: 14,
                  background: "#fffaf4",
                  fontFamily: "inherit",
                }}
              >
                {COURSE_TYPES.map((t) => (
                  <option key={t} value={t}>{t}</option>
                ))}
              </select>

              <label>Trainer</label>
              <select
                value={courseForm.trainerId}
                onChange={(e) => setCourseForm({ ...courseForm, trainerId: e.target.value })}
                style={{
                  width: "100%",
                  padding: "14px 16px",
                  border: "1px solid var(--border)",
                  borderRadius: 14,
                  marginBottom: 14,
                  background: "#fffaf4",
                  fontFamily: "inherit",
                }}
              >
                <option value="">-- Select trainer --</option>
                {trainers.filter((t) => t.active).map((t) => (
                  <option key={t.id} value={t.id}>
                    {t.firstName} {t.lastName}
                  </option>
                ))}
              </select>

              <label>Location</label>
              <select
                value={courseForm.locationId}
                onChange={(e) => setCourseForm({ ...courseForm, locationId: e.target.value })}
                style={{
                  width: "100%",
                  padding: "14px 16px",
                  border: "1px solid var(--border)",
                  borderRadius: 14,
                  marginBottom: 14,
                  background: "#fffaf4",
                  fontFamily: "inherit",
                }}
              >
                <option value="">-- Select location --</option>
                {locations.map((l) => (
                  <option key={l.id} value={l.id}>{l.name}</option>
                ))}
              </select>

              <label>Day of Week</label>
              <select
                value={courseForm.dayOfWeek}
                onChange={(e) => setCourseForm({ ...courseForm, dayOfWeek: e.target.value })}
                style={{
                  width: "100%",
                  padding: "14px 16px",
                  border: "1px solid var(--border)",
                  borderRadius: 14,
                  marginBottom: 14,
                  background: "#fffaf4",
                  fontFamily: "inherit",
                }}
              >
                {DAYS_OF_WEEK.map((d) => (
                  <option key={d} value={d}>{d}</option>
                ))}
              </select>

              <label>Start Time</label>
              <input
                type="time"
                value={courseForm.startTime}
                onChange={(e) => setCourseForm({ ...courseForm, startTime: e.target.value })}
              />

              <label>Duration (minutes)</label>
              <input
                type="number"
                min="15"
                max="180"
                value={courseForm.duration}
                onChange={(e) => setCourseForm({ ...courseForm, duration: e.target.value })}
              />

              <label>Max Capacity</label>
              <input
                type="number"
                min="1"
                value={courseForm.maxCapacity}
                onChange={(e) => setCourseForm({ ...courseForm, maxCapacity: e.target.value })}
              />

              <div style={{ display: "flex", gap: 12, marginTop: 10 }}>
                <button type="submit" className="primary-btn" style={{ flex: 1 }}>
                  {editingCourse ? "Save changes" : "Save"}
                </button>

                <button
                  type="button"
                  className="secondary-btn"
                  style={{ flex: 1 }}
                  onClick={() => {
                    setShowForm(false);
                    setCourseForm(EMPTY_COURSE_FORM);
                    setEditingCourse(null);
                  }}
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        )}

        {showForm && tab === "locations" && (
          <div className="card form-card" style={{ marginBottom: 24 }}>
            <h3 style={{ marginBottom: 20 }}>
              {editingLocation ? "Edit Location" : "New Location"}
            </h3>

            <form onSubmit={editingLocation ? handleUpdateLocation : handleAddLocation}>
              <label>Name</label>
              <input
                placeholder="Location name"
                value={locationForm.name}
                required
                onChange={(e) => setLocationForm({ ...locationForm, name: e.target.value })}
              />

              <label>Address</label>
              <input
                placeholder="Location address"
                value={locationForm.address}
                required
                onChange={(e) => setLocationForm({ ...locationForm, address: e.target.value })}
              />

              <div style={{ display: "flex", gap: 12, marginTop: 10 }}>
                <button type="submit" className="primary-btn" style={{ flex: 1 }}>
                  {editingLocation ? "Save changes" : "Save"}
                </button>

                <button
                  type="button"
                  className="secondary-btn"
                  style={{ flex: 1 }}
                  onClick={() => {
                    setShowForm(false);
                    setLocationForm(EMPTY_LOCATION_FORM);
                    setEditingLocation(null);
                  }}
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        )}

        {(tab === "trainers" || tab === "receptionists") && (
          <div className="card table-card">
            <table>
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>

              <tbody>
                {userList.length === 0 ? (
                  <tr>
                    <td colSpan={4} style={{ color: "var(--muted)" }}>
                      No entries found.
                    </td>
                  </tr>
                ) : (
                  userList.map((u) => (
                    <tr key={u.id}>
                      <td><strong>{u.firstName} {u.lastName}</strong></td>
                      <td style={{ color: "var(--muted)" }}>{u.email}</td>
                      <td>
                        <span
                          style={{
                            background: u.active ? "#d1fae5" : "#fee2e2",
                            color: u.active ? "#065f46" : "#991b1b",
                            padding: "4px 12px",
                            borderRadius: 999,
                            fontSize: 12,
                            fontWeight: 700,
                          }}
                        >
                          {u.active ? "Active" : "Inactive"}
                        </span>
                      </td>
                      <td>
                        {u.active && isAdmin && (
                          <div style={{ display: "flex", gap: 12 }}>
                            <button
                              onClick={() => handleUserEditClick(u)}
                              style={{
                                background: "none",
                                color: "var(--accent)",
                                fontWeight: 700,
                                cursor: "pointer",
                                display: "flex",
                                alignItems: "center",
                                gap: 6,
                              }}
                            >
                              <Pencil size={15} /> Edit
                            </button>

                            <button
                              onClick={() => handleDeactivate(u.id)}
                              style={{
                                background: "none",
                                color: "#dc2626",
                                fontWeight: 700,
                                cursor: "pointer",
                                display: "flex",
                                alignItems: "center",
                                gap: 6,
                              }}
                            >
                              <Trash2 size={15} /> Deactivate
                            </button>
                          </div>
                        )}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        )}

        {tab === "courses" && (
          <div className="card table-card">
            <table>
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Type</th>
                  <th>Trainer</th>
                  <th>Day</th>
                  <th>Time</th>
                  <th>Capacity</th>
                  <th style={{ minWidth: 130 }}>Actions</th>
                </tr>
              </thead>

              <tbody>
                {courses.length === 0 ? (
                  <tr>
                    <td colSpan={7} style={{ color: "var(--muted)" }}>
                      No courses found.
                    </td>
                  </tr>
                ) : (
                  courses.map((c) => (
                    <tr key={c.id}>
                      <td><strong>{c.name}</strong></td>
                      <td style={{ color: "var(--muted)" }}>{c.type}</td>
                      <td>{c.trainer ? `${c.trainer.firstName} ${c.trainer.lastName}` : "—"}</td>
                      <td style={{ color: "var(--muted)" }}>{c.dayOfWeek}</td>
                      <td style={{ color: "var(--muted)" }}>{c.startTime?.slice(0, 5)}</td>
                      <td>{c.currentOccupancy}/{c.maxCapacity}</td>
                      <td>
                        {isAdmin && (
                          <div style={{ display: "flex", gap: 12 }}>
                            <button
                              onClick={() => handleEditCourseClick(c)}
                              style={{
                                background: "none",
                                color: "var(--accent)",
                                fontWeight: 700,
                                cursor: "pointer",
                                display: "flex",
                                alignItems: "center",
                                gap: 6,
                              }}
                            >
                              <Pencil size={15} /> Edit
                            </button>

                            <button
                              onClick={() => handleDeleteCourse(c.id)}
                              style={{
                                background: "none",
                                color: "#dc2626",
                                fontWeight: 700,
                                cursor: "pointer",
                                display: "flex",
                                alignItems: "center",
                                gap: 6,
                              }}
                            >
                              <Trash2 size={15} /> Delete
                            </button>
                          </div>
                        )}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        )}

        {tab === "locations" && (
          <div className="card table-card">
            <table>
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Address</th>
                  <th style={{ minWidth: 130 }}>Actions</th>
                </tr>
              </thead>

              <tbody>
                {locations.length === 0 ? (
                  <tr>
                    <td colSpan={3} style={{ color: "var(--muted)" }}>
                      No locations found.
                    </td>
                  </tr>
                ) : (
                  locations.map((location) => (
                    <tr key={location.id}>
                      <td><strong>{location.name}</strong></td>
                      <td style={{ color: "var(--muted)" }}>{location.address}</td>
                      <td>
                        {isAdmin && (
                          <div style={{ display: "flex", gap: 12 }}>
                            <button
                              onClick={() => handleEditLocationClick(location)}
                              style={{
                                background: "none",
                                color: "var(--accent)",
                                fontWeight: 700,
                                cursor: "pointer",
                                display: "flex",
                                alignItems: "center",
                                gap: 6,
                              }}
                            >
                              <Pencil size={15} /> Edit
                            </button>

                            <button
                              onClick={() => handleDeleteLocation(location.id)}
                              style={{
                                background: "none",
                                color: "#dc2626",
                                fontWeight: 700,
                                cursor: "pointer",
                                display: "flex",
                                alignItems: "center",
                                gap: 6,
                              }}
                            >
                              <Trash2 size={15} /> Delete
                            </button>
                          </div>
                        )}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        )}
      </main>
    </div>
  );
}

export default AdminDashboard;
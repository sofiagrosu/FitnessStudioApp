import { useEffect, useState } from "react";
import Sidebar from "../components/Sidebar";
import StatCard from "../components/StatCard";
import { Users, Trash2, UserPlus } from "lucide-react";
import {
  getAllTrainers, getAllReceptionists,
  addUser, deactivateUser,
} from "../services/adminService";

const EMPTY_FORM = {
  firstName: "", lastName: "", email: "", password: "",
};

function AdminDashboard() {
  const [trainers, setTrainers]         = useState([]);
  const [receptionists, setReceptionists] = useState([]);
  const [tab, setTab]                   = useState("trainers");
  const [showForm, setShowForm]         = useState(false);
  const [form, setForm]                 = useState(EMPTY_FORM);
  const [error, setError]               = useState("");
  const [success, setSuccess]           = useState("");

  async function loadData() {
    try {
      const [t, r] = await Promise.all([getAllTrainers(), getAllReceptionists()]);
      setTrainers(t.data);
      setReceptionists(r.data);
    } catch (e) {
      console.error(e);
    }
  }

  useEffect(() => { loadData(); }, []);

  async function handleAdd(e) {
    e.preventDefault();
    setError("");
    setSuccess("");
    const role = tab === "trainers" ? "TRAINER" : "RECEPTIONIST";
    const payload = { ...form, role, type: role };
    try {
      await addUser(payload);
      setForm(EMPTY_FORM);
      setShowForm(false);
      setSuccess(`${role === "TRAINER" ? "Trainer" : "Receptionist"} added successfully.`);
      loadData();
    } catch (err) {
      setError(err.response?.data?.message || "Error adding user.");
    }
  }

  async function handleDeactivate(id) {
    if (!window.confirm("Deactivate this user?")) return;
    try {
      await deactivateUser(id);
      loadData();
    } catch (err) {
      setError(err.response?.data?.message || "Error deactivating user.");
    }
  }

  const list = tab === "trainers" ? trainers : receptionists;

  return (
    <div className="app-shell">
      <Sidebar />
      <main>
        <h1 className="section-title">Admin Panel</h1>
        <p className="section-subtitle">Manage trainers and receptionists.</p>

        {/* Stats */}
        <div className="grid-2" style={{ marginBottom: 32 }}>
          <StatCard title="Trainers"       value={trainers.filter(u => u.active).length}      subtitle="Active" icon={<Users size={20} />} />
          <StatCard title="Receptionists"  value={receptionists.filter(u => u.active).length} subtitle="Active" icon={<Users size={20} />} />
        </div>

        {/* Tabs + Add button */}
        <div style={{ display: "flex", gap: 12, marginBottom: 20, alignItems: "center" }}>
          <button
            className={tab === "trainers" ? "primary-btn" : "secondary-btn"}
            onClick={() => { setTab("trainers"); setShowForm(false); setError(""); setSuccess(""); }}
          >
            Trainers
          </button>
          <button
            className={tab === "receptionists" ? "primary-btn" : "secondary-btn"}
            onClick={() => { setTab("receptionists"); setShowForm(false); setError(""); setSuccess(""); }}
          >
            Receptionists
          </button>
          <button
            className="primary-btn"
            style={{ marginLeft: "auto", display: "flex", alignItems: "center", gap: 8 }}
            onClick={() => { setShowForm(!showForm); setError(""); setSuccess(""); }}
          >
            <UserPlus size={16} />
            Add {tab === "trainers" ? "Trainer" : "Receptionist"}
          </button>
        </div>

        {/* Feedback */}
        {error   && <div className="error-box" style={{ marginBottom: 16 }}>{error}</div>}
        {success && <div className="info-box"  style={{ marginBottom: 16 }}>{success}</div>}

        {/* Add form */}
        {showForm && (
          <div className="card form-card" style={{ marginBottom: 24 }}>
            <h3 style={{ marginBottom: 20 }}>
              New {tab === "trainers" ? "Trainer" : "Receptionist"}
            </h3>
            <form onSubmit={handleAdd}>
              <label>First Name</label>
              <input
                placeholder="First name"
                value={form.firstName}
                onChange={e => setForm({ ...form, firstName: e.target.value })}
                required
              />
              <label>Last Name</label>
              <input
                placeholder="Last name"
                value={form.lastName}
                onChange={e => setForm({ ...form, lastName: e.target.value })}
                required
              />
              <label>Email</label>
              <input
                type="email"
                placeholder="email@example.com"
                value={form.email}
                onChange={e => setForm({ ...form, email: e.target.value })}
                required
              />
              <label>Password</label>
              <input
                type="password"
                placeholder="Password"
                value={form.password}
                onChange={e => setForm({ ...form, password: e.target.value })}
                required
              />
              <div style={{ display: "flex", gap: 12, marginTop: 10 }}>
                <button type="submit" className="primary-btn" style={{ flex: 1 }}>Save</button>
                <button type="button" className="secondary-btn" style={{ flex: 1 }}
                  onClick={() => { setShowForm(false); setForm(EMPTY_FORM); }}>
                  Cancel
                </button>
              </div>
            </form>
          </div>
        )}

        {/* Table */}
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
              {list.length === 0 ? (
                <tr>
                  <td colSpan={4} style={{ color: "var(--muted)" }}>No entries found.</td>
                </tr>
              ) : (
                list.map((u) => (
                  <tr key={u.id}>
                    <td><strong>{u.firstName} {u.lastName}</strong></td>
                    <td style={{ color: "var(--muted)" }}>{u.email}</td>
                    <td>
                      <span style={{
                        background: u.active ? "#d1fae5" : "#fee2e2",
                        color:      u.active ? "#065f46" : "#991b1b",
                        padding: "4px 12px",
                        borderRadius: 999,
                        fontSize: 12,
                        fontWeight: 700,
                      }}>
                        {u.active ? "Active" : "Inactive"}
                      </span>
                    </td>
                    <td>
                      {u.active && (
                        <button
                          onClick={() => handleDeactivate(u.id)}
                          style={{
                            background: "none", color: "#dc2626",
                            fontWeight: 700, cursor: "pointer",
                            display: "flex", alignItems: "center", gap: 6,
                          }}
                        >
                          <Trash2 size={15} /> Deactivate
                        </button>
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </main>
    </div>
  );
}

export default AdminDashboard;

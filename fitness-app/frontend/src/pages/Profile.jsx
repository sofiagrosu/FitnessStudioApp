import Sidebar from "../components/Sidebar";
import { getCurrentUser } from "../services/authService";

function Profile() {
  const user = getCurrentUser();

  return (
    <div className="app-shell">
      <Sidebar />

      <main>
        <h1 className="section-title">Profile</h1>
        <p className="section-subtitle">Your account information.</p>

        <div className="form-card card">
          <p><strong>Name:</strong> {user?.name || "Not logged in"}</p>
          <p><strong>Email:</strong> {user?.email || "-"}</p>
          <p><strong>Role:</strong> {user?.role || "MEMBER"}</p>
          <p><strong>Active:</strong> {String(user?.active ?? true)}</p>
        </div>
      </main>
    </div>
  );
}

export default Profile;
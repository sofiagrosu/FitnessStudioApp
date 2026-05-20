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
          <p><strong>Name:</strong> {user ? `${user.firstName} ${user.lastName}` : "-"}</p>
          <p><strong>Email:</strong> {user?.email || "-"}</p>
          <p><strong>Phone:</strong> {user?.phone || "-"}</p>
          <p><strong>Role:</strong> {user?.role || "-"}</p>

          {user?.qrCode && (
            <p><strong>QR Code:</strong> {user.qrCode}</p>
          )}
        </div>
      </main>
    </div>
  );
}

export default Profile;

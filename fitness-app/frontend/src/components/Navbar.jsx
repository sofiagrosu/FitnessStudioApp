import { Link, useNavigate } from "react-router-dom";
import { Dumbbell, LogOut } from "lucide-react";
import { getCurrentUser, logoutUser } from "../services/authService";

function redirectByRole(role) {
  if (role === "TRAINER")      return "/trainer/dashboard";
  if (role === "ADMIN")        return "/admin/dashboard";
  if (role === "RECEPTIONIST") return "/receptionist/dashboard";
  return "/dashboard";
}

function Navbar() {
  const navigate = useNavigate();
  const user = getCurrentUser();

  function handleLogout() {
    logoutUser();
    navigate("/login");
  }

  return (
    <header className="navbar">
      <Link to="/" className="brand">
        <div className="brand-icon">
          <Dumbbell size={18} />
        </div>
        <span>STRONGER</span>
      </Link>

      <nav className="nav-links">
        <Link to="/">Home</Link>
        <Link to="/courses">Classes</Link>
        <Link to="/memberships">Memberships</Link>
        <Link to="/locations">Locations</Link>
      </nav>

      <div className="nav-actions">
        {user ? (
          <>
            <span
              onClick={() => navigate(redirectByRole(user.role))}
              style={{ fontSize: 14, fontWeight: 600, cursor: "pointer", color: "var(--accent)" }}
            >
              {user.firstName} {user.lastName}
            </span>
            <button
              onClick={handleLogout}
              className="nav-login"
              style={{ display: "flex", alignItems: "center", gap: 6, cursor: "pointer", background: "none", border: "none" }}
            >
              <LogOut size={15} />
              Logout
            </button>
          </>
        ) : (
          <>
            <Link to="/login" className="nav-login">Login</Link>
            <Link to="/register" className="nav-join">Join now</Link>
          </>
        )}
      </div>
    </header>
  );
}

export default Navbar;

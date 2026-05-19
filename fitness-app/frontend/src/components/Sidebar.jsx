import { Link, useLocation, useNavigate } from "react-router-dom";
import {
  LayoutDashboard, CalendarDays, CreditCard, MapPin,
  User, LogOut, CheckCircle, Dumbbell, LogIn
} from "lucide-react";
import { getCurrentUser, logoutUser } from "../services/authService";

const links = [
  { path: "/dashboard", label: "Dashboard", icon: LayoutDashboard },
  { path: "/courses", label: "Classes", icon: Dumbbell },
  { path: "/memberships", label: "Memberships", icon: CalendarDays },
  { path: "/payments", label: "Payments", icon: CreditCard },
  { path: "/checkins", label: "Check-ins", icon: CheckCircle },
  { path: "/locations", label: "Locations", icon: MapPin },
  { path: "/profile", label: "Profile", icon: User },
];

function Sidebar() {
  const location = useLocation();
  const navigate = useNavigate();
  const user = getCurrentUser();

  function handleLogout() {
    logoutUser();
    navigate("/login");
  }

  return (
    <aside className="sidebar">
      <Link to="/" className="brand sidebar-brand">
        <div className="brand-icon">
          <Dumbbell size={18} />
        </div>
        <span>STRONGER</span>
      </Link>

      <div className="sidebar-links">
        {links.map((item) => {
          const Icon = item.icon;
          const active = location.pathname === item.path;

          return (
            <Link key={item.path} to={item.path} className={`sidebar-link ${active ? "active" : ""}`}>
              <Icon size={18} />
              <span>{item.label}</span>
            </Link>
          );
        })}
      </div>

      {user ? (
        <button className="logout-btn" onClick={handleLogout}>
          <LogOut size={18} />
          Logout
        </button>
      ) : (
        <button className="logout-btn" onClick={() => navigate("/login")}>
          <LogIn size={18} />
          Login
        </button>
      )}
    </aside>
  );
}

export default Sidebar;
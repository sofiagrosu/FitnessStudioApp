import { Link, useLocation, useNavigate } from "react-router-dom";
import {
  LayoutDashboard, CalendarDays, CreditCard, MapPin,
  User, LogOut, CheckCircle, Dumbbell, LogIn, Users, ClipboardList
} from "lucide-react";
import { getCurrentUser, logoutUser } from "../services/authService";

const memberLinks = [
  { path: "/dashboard",   label: "Dashboard",   icon: LayoutDashboard },
  { path: "/courses",     label: "Classes",      icon: Dumbbell },
  { path: "/memberships", label: "Memberships",  icon: CalendarDays },
  { path: "/payments",    label: "Payments",     icon: CreditCard },
  { path: "/locations",   label: "Locations",    icon: MapPin },
  { path: "/profile",     label: "Profile",      icon: User },
];

const trainerLinks = [
  { path: "/trainer/dashboard", label: "My Courses",  icon: Dumbbell },
  { path: "/locations",         label: "Locations",   icon: MapPin },
  { path: "/profile",           label: "Profile",     icon: User },
];

const adminLinks = [
  { path: "/admin/dashboard", label: "Management", icon: Users },
  { path: "/locations",       label: "Locations",  icon: MapPin },
  { path: "/profile",         label: "Profile",    icon: User },
];

const receptionistLinks = [
  { path: "/receptionist/dashboard", label: "Check-in",  icon: CheckCircle },
  { path: "/locations",              label: "Locations", icon: MapPin },
  { path: "/profile",                label: "Profile",   icon: User },
];

function getLinks(role) {
  switch (role) {
    case "TRAINER":      return trainerLinks;
    case "ADMIN":        return adminLinks;
    case "RECEPTIONIST": return receptionistLinks;
    default:             return memberLinks;
  }
}

function Sidebar() {
  const location = useLocation();
  const navigate  = useNavigate();
  const user      = getCurrentUser();
  const links     = getLinks(user?.role);

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

      {user && (
        <div style={{ marginBottom: 24, padding: "10px 14px", background: "var(--accent-light)", borderRadius: 14 }}>
          <p style={{ fontSize: 12, color: "var(--muted)", marginBottom: 2 }}>{user.role}</p>
          <p style={{ fontWeight: 700, fontSize: 14 }}>{user.firstName} {user.lastName}</p>
        </div>
      )}

      <div className="sidebar-links">
        {links.map((item) => {
          const Icon   = item.icon;
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

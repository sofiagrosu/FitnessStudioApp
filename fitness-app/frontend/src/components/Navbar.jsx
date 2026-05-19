import { Link } from "react-router-dom";
import { Dumbbell } from "lucide-react";

function Navbar() {
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
        <Link to="/login" className="nav-login">Login</Link>
        <Link to="/register" className="nav-join">Join now</Link>
      </div>
    </header>
  );
}

export default Navbar;
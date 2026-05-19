import { Navigate } from "react-router-dom";
import { getCurrentUser } from "../services/authService";

function redirectByRole(role) {
  if (role === "TRAINER")      return "/trainer/dashboard";
  if (role === "ADMIN")        return "/admin/dashboard";
  if (role === "RECEPTIONIST") return "/receptionist/dashboard";
  return "/dashboard";
}

function ProtectedRoute({ children, allowedRoles }) {
  const user = getCurrentUser();

  if (!user) return <Navigate to="/login" replace />;

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return <Navigate to={redirectByRole(user.role)} replace />;
  }


  return children;
}

export default ProtectedRoute;

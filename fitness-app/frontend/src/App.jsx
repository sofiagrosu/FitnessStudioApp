import { Routes, Route } from "react-router-dom";
import ProtectedRoute from "./components/ProtectedRoute";
import Home            from "./pages/Home";
import Login           from "./pages/Login";
import Register        from "./pages/Register";
import Dashboard       from "./pages/Dashboard";
import Courses         from "./pages/Courses";
import Memberships     from "./pages/Memberships";
import Payments        from "./pages/Payments";
import Locations       from "./pages/Locations";
import Profile         from "./pages/Profile";
import TrainerDashboard      from "./pages/TrainerDashboard";
import AdminDashboard        from "./pages/AdminDashboard";
import ReceptionistDashboard from "./pages/ReceptionistDashboard";

function App() {
  return (
    <Routes>
      {/* Public */}
      <Route path="/"         element={<Home />} />
      <Route path="/login"    element={<Login />} />
      <Route path="/register" element={<Register />} />

      {/* Member */}
      <Route path="/dashboard"   element={<ProtectedRoute allowedRoles={["MEMBER"]}><Dashboard /></ProtectedRoute>} />
      <Route path="/courses"     element={<Courses />} />
      <Route path="/memberships" element={<Memberships />} />
      <Route path="/payments"    element={<ProtectedRoute allowedRoles={["MEMBER"]}><Payments /></ProtectedRoute>} />
      <Route path="/locations"   element={<Locations />} />
      <Route path="/profile"     element={<ProtectedRoute><Profile /></ProtectedRoute>} />

      {/* Trainer */}
      <Route path="/trainer/dashboard" element={
        <ProtectedRoute allowedRoles={["TRAINER"]}>
          <TrainerDashboard />
        </ProtectedRoute>
      } />

      {/* Admin */}
      <Route path="/admin/dashboard" element={
        <ProtectedRoute allowedRoles={["ADMIN"]}>
          <AdminDashboard />
        </ProtectedRoute>
      } />

      {/* Receptionist */}
      <Route path="/receptionist/dashboard" element={
        <ProtectedRoute allowedRoles={["RECEPTIONIST"]}>
          <ReceptionistDashboard />
        </ProtectedRoute>
      } />
    </Routes>
  );
}

export default App;

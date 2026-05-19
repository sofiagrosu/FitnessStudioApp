import { Routes, Route } from "react-router-dom";
import ProtectedRoute from "./components/ProtectedRoute";
import Home from "./pages/Home";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import Courses from "./pages/Courses";
import Memberships from "./pages/Memberships";
import Payments from "./pages/Payments";
import CheckIns from "./pages/CheckIns";
import Locations from "./pages/Locations";
import Profile from "./pages/Profile";

function App() {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/dashboard" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
      <Route path="/courses" element={<Courses />} />
      <Route path="/memberships" element={<Memberships />} />
      <Route path="/payments" element={<Payments />} />
      <Route path="/checkins" element={<CheckIns />} />
      <Route path="/locations" element={<Locations />} />
      <Route path="/profile" element={<Profile />} />
    </Routes>
  );
}

export default App;
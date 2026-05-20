import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { registerUser } from "../services/authService";

function Register() {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    firstName: "",
    lastName: "",
    email: "",
    password: "",
    phone: "",
    active: true,
    role: "MEMBER",
  });

  const [error, setError] = useState("");

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");

    if (!form.firstName.trim()) { setError("First name is required."); return; }
    if (!form.lastName.trim())  { setError("Last name is required.");  return; }
    if (!form.email.trim())     { setError("Email is required.");       return; }
    if (!form.password.trim())  { setError("Password is required.");    return; }
    if (!/^\d{10}$/.test(form.phone)) {
      setError("Phone number must be exactly 10 digits.");
      return;
    }

    try {
      await registerUser(form);
      navigate("/dashboard");
    } catch (err) {
      setError(err.response?.data?.message || "Registration failed. Please try again.");
    }
  }

  return (
    <div className="auth-page">
      <form className="auth-card card" onSubmit={handleSubmit}>
        <h1>Create account</h1>
        <p>Join the fitness platform.</p>

        {error && <div className="error-box">{error}</div>}

        <input
          placeholder="First name"
          value={form.firstName}
          onChange={(e) => setForm({ ...form, firstName: e.target.value })}
        />

        <input
          placeholder="Last name"
          value={form.lastName}
          onChange={(e) => setForm({ ...form, lastName: e.target.value })}
        />

        <input
          placeholder="Email"
          value={form.email}
          onChange={(e) => setForm({ ...form, email: e.target.value })}
        />

        <input
          placeholder="Password"
          type="password"
          value={form.password}
          onChange={(e) => setForm({ ...form, password: e.target.value })}
        />

        <input
          placeholder="Phone (10 digits)"
          value={form.phone}
          inputMode="numeric"
          maxLength={10}
          onChange={(e) => {
            const val = e.target.value.replace(/\D/g, "");
            setForm({ ...form, phone: val });
          }}
        />

        <button className="primary-btn">Create account</button>

        <span>
          Already have an account? <Link to="/login">Login</Link>
        </span>
      </form>
    </div>
  );
}

export default Register;
import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { loginUser } from "../services/authService";

function Login() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: "", password: "" });
  const [error, setError] = useState("");

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");

    try {
      const user = await loginUser(form.email, form.password);
      if (user.role === "TRAINER")            navigate("/trainer/dashboard");
      else if (user.role === "ADMIN")         navigate("/admin/dashboard");
      else if (user.role === "RECEPTIONIST")  navigate("/receptionist/dashboard");
      else                                    navigate("/dashboard");
    } catch {
      setError("Login failed. Check email and password.");
    }
  }

  return (
    <div className="auth-page">
      <form className="auth-card card" onSubmit={handleSubmit}>
        <h1>Welcome back</h1>
        <p>Login to your fitness account.</p>

        {error && <div className="error-box">{error}</div>}

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

        <button className="primary-btn">Login</button>

        <span>
          No account? <Link to="/register">Create one</Link>
        </span>
      </form>
    </div>
  );
}

export default Login;
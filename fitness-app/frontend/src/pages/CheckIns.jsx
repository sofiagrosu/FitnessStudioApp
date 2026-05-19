import { useState } from "react";
import Sidebar from "../components/Sidebar";
import { checkIn } from "../services/checkInService";

function CheckIns() {
  const [form, setForm] = useState({
    qrCode: "",
    locationId: "1",
    zoneId: "1",
  });

  const [message, setMessage] = useState("");

  async function handleCheckIn(e) {
    e.preventDefault();

    try {
      const res = await checkIn(form.qrCode, Number(form.locationId), Number(form.zoneId));
      setMessage(res.data?.message || "Check-in completed.");
    } catch {
      setMessage("Check-in failed. Check member code/location/zone.");
    }
  }

  return (
    <div className="app-shell">
      <Sidebar />

      <main>
        <h1 className="section-title">Check-ins</h1>
        <p className="section-subtitle">Use member access code to check in.</p>

        <form className="form-card card" onSubmit={handleCheckIn}>
          <label>Member access code</label>
          <input
            placeholder="Example: M17AB537"
            value={form.qrCode}
            onChange={(e) => setForm({ ...form, qrCode: e.target.value })}
          />

          <label>Location ID</label>
          <input
            value={form.locationId}
            onChange={(e) => setForm({ ...form, locationId: e.target.value })}
          />

          <label>Zone ID</label>
          <input
            value={form.zoneId}
            onChange={(e) => setForm({ ...form, zoneId: e.target.value })}
          />

          <button className="primary-btn">Check in</button>

          {message && <div className="info-box">{message}</div>}
        </form>
      </main>
    </div>
  );
}

export default CheckIns;
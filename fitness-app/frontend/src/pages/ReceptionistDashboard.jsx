import { useEffect, useState } from "react";
import Sidebar from "../components/Sidebar";
import StatCard from "../components/StatCard";
import { getCurrentUser } from "../services/authService";
import { checkIn, checkOut, getOpenCheckIns, getOccupancy } from "../services/checkInService";
import { getLocations } from "../services/locationService";
import { Users, QrCode, CheckCircle, LogOut } from "lucide-react";

function ReceptionistDashboard() {
  const user = getCurrentUser();

  const [locations, setLocations]     = useState([]);
  const [selectedLoc, setSelectedLoc] = useState("");
  const [selectedZone, setSelectedZone] = useState("");
  const [qrCode, setQrCode]           = useState("");
  const [openCheckIns, setOpenCheckIns] = useState([]);
  const [occupancy, setOccupancy]     = useState(null);
  const [message, setMessage]         = useState(null);
  const [error, setError]             = useState(null);

  useEffect(() => {
    getLocations().then(r => {
      setLocations(r.data);
      if (r.data.length > 0) {
        setSelectedLoc(r.data[0].id);
        if (r.data[0].zones?.length > 0) setSelectedZone(r.data[0].zones[0].id);
      }
    });
  }, []);

  useEffect(() => {
    if (!selectedLoc) return;
    refreshCheckIns();
  }, [selectedLoc]);

  async function refreshCheckIns() {
    try {
      const [open, occ] = await Promise.all([
        getOpenCheckIns(selectedLoc),
        getOccupancy(selectedLoc),
      ]);
      setOpenCheckIns(open.data);
      setOccupancy(occ.data);
    } catch (e) {
      console.error(e);
    }
  }

  const currentLocation = locations.find(l => l.id == selectedLoc);
  const zones = currentLocation?.zones || [];

  async function handleCheckIn(e) {
    e.preventDefault();
    setMessage(null);
    setError(null);
    if (!qrCode.trim()) { setError("Introdu codul QR."); return; }
    if (!selectedZone) { setError("Selectează zona."); return; }
    try {
      const res = await checkIn(qrCode.trim(), Number(selectedLoc), Number(selectedZone));
      setMessage(`✅ Check-in reușit: ${res.data.member?.firstName ?? ""} ${res.data.member?.lastName ?? ""}`);
      setQrCode("");
      refreshCheckIns();
    } catch (err) {
      setError(err.response?.data?.message || "Eroare la check-in.");
    }
  }

  async function handleCheckOut(checkInId) {
    setMessage(null);
    setError(null);
    try {
      await checkOut(checkInId);
      setMessage("✅ Check-out reușit.");
      refreshCheckIns();
    } catch (err) {
      setError(err.response?.data?.message || "Eroare la check-out.");
    }
  }

  return (
    <div className="app-shell">
      <Sidebar />
      <main>
        <h1 className="section-title">Receptionist</h1>
        <p className="section-subtitle">Bine ai venit, {user?.firstName}.</p>

        {/* Stats */}
        <div className="grid-2" style={{ marginBottom: 32 }}>
          <StatCard
            title="Locație activă"
            value={currentLocation?.name ?? "—"}
            subtitle={currentLocation?.address ?? ""}
            icon={<CheckCircle size={20} />}
          />
          <StatCard
            title="Persoane în sală"
            value={occupancy ?? 0}
            subtitle="Check-in-uri deschise"
            icon={<Users size={20} />}
          />
        </div>

        <div className="grid-2">
          {/* Check-in form */}
          <div className="card form-card" style={{ maxWidth: "100%" }}>
            <h3 style={{ marginBottom: 20 }}>Check-in prin QR</h3>

            {message && <div className="info-box"  style={{ marginBottom: 14 }}>{message}</div>}
            {error   && <div className="error-box" style={{ marginBottom: 14 }}>{error}</div>}

            <form onSubmit={handleCheckIn}>
              <label>Locație</label>
              <select
                value={selectedLoc}
                onChange={e => {
                  setSelectedLoc(e.target.value);
                  const loc = locations.find(l => l.id == e.target.value);
                  setSelectedZone(loc?.zones?.[0]?.id ?? "");
                }}
                style={{ width: "100%", padding: "14px 16px", border: "1px solid var(--border)", borderRadius: 14, marginBottom: 14, background: "#fffaf4", fontFamily: "inherit" }}
              >
                {locations.map(l => <option key={l.id} value={l.id}>{l.name}</option>)}
              </select>

              <label>Zonă</label>
              <select
                value={selectedZone}
                onChange={e => setSelectedZone(e.target.value)}
                style={{ width: "100%", padding: "14px 16px", border: "1px solid var(--border)", borderRadius: 14, marginBottom: 14, background: "#fffaf4", fontFamily: "inherit" }}
              >
                {zones.map(z => <option key={z.id} value={z.id}>{z.name} (max {z.maxCapacity})</option>)}
              </select>

              <label>Cod QR</label>
              <input
                placeholder="ex: MEM-A2645C8F"
                value={qrCode}
                onChange={e => setQrCode(e.target.value)}
              />

              <button type="submit" className="primary-btn" style={{ marginTop: 4 }}>
                Check-in
              </button>
            </form>
          </div>

          {/* Open check-ins */}
          <div className="card table-card" style={{ height: "fit-content" }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 16 }}>
              <h3>În sală acum</h3>
              <button className="secondary-btn" style={{ padding: "8px 14px", fontSize: 13 }} onClick={refreshCheckIns}>
                Refresh
              </button>
            </div>

            {openCheckIns.length === 0 ? (
              <p style={{ color: "var(--muted)" }}>Nicio persoană în sală.</p>
            ) : (
              <table>
                <thead>
                  <tr>
                    <th>Membru</th>
                    <th>Zonă</th>
                    <th>Acțiune</th>
                  </tr>
                </thead>
                <tbody>
                  {openCheckIns.map(c => (
                    <tr key={c.id}>
                      <td><strong>{c.member?.firstName} {c.member?.lastName}</strong></td>
                      <td style={{ color: "var(--muted)" }}>{c.zone?.name ?? "—"}</td>
                      <td>
                        <button
                          onClick={() => handleCheckOut(c.id)}
                          style={{ background: "none", color: "var(--accent)", fontWeight: 700, cursor: "pointer", display: "flex", alignItems: "center", gap: 6 }}
                        >
                          <LogOut size={15} /> Check-out
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      </main>
    </div>
  );
}

export default ReceptionistDashboard;

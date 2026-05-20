import { useEffect, useState } from "react";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import { getCurrentUser } from "../services/authService";
import { getLocations } from "../services/locationService";

const defaultImages = [
  "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?q=80&w=1200&auto=format&fit=crop",
  "https://images.unsplash.com/photo-1571902943202-507ec2618e8f?q=80&w=1200&auto=format&fit=crop",
  "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?q=80&w=1200&auto=format&fit=crop",
];

function Locations() {
  const [locations, setLocations] = useState([]);
  const [message, setMessage] = useState("");

  const user = getCurrentUser();

  useEffect(() => {
    loadLocations();
  }, []);

  async function loadLocations() {
    try {
      const res = await getLocations();

      if (!res.data || res.data.length === 0) {
        setLocations([]);
        setMessage("No locations available.");
        return;
      }

      setLocations(res.data);
      setMessage("");
    } catch {
      setLocations([]);
      setMessage("Could not load locations from backend.");
    }
  }

  const content = (
    <>
      <h1 className="section-title">Locations</h1>

      <p className="section-subtitle">
        Explore available fitness locations.
      </p>

      {message && <div className="info-box">{message}</div>}

      {locations.length > 0 ? (
        <div className="grid-3">
          {locations.map((loc, index) => (
            <div className="location-card card" key={loc.id}>
              <div className="empty-image">
                <img
                  src={
                    loc.image ||
                    defaultImages[index % defaultImages.length]
                  }
                  alt={loc.name}
                />
              </div>

              <h3>{loc.name}</h3>

              <p>
                {loc.address || "Premium fitness location"}
              </p>

              <span className="tag">
                ID: {loc.id}
              </span>
            </div>
          ))}
        </div>
      ) : (
        !message && (
          <div className="info-box">
            No locations available yet.
          </div>
        )
      )}
    </>
  );

  if (!user) {
    return (
      <>
        <Navbar />

        <main style={{ padding: "2rem" }}>
          {content}
        </main>
      </>
    );
  }

  return (
    <div className="app-shell">
      <Sidebar />

      <main>{content}</main>
    </div>
  );
}

export default Locations;
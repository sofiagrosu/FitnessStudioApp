import { useEffect, useState } from "react";
import Sidebar from "../components/Sidebar";
import { getLocations } from "../services/locationService";

function Locations() {
  const [locations, setLocations] = useState([]);
  const [message, setMessage] = useState("");

  const demoLocations = [
    {
      id: 1,
      name: "Central Gym",
      address: "Str. Avram Iancu 20, Cluj-Napoca",
      image:
        "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?q=80&w=1200&auto=format&fit=crop",
    },
    {
      id: 2,
      name: "Zorilor Gym",
      address: "Str. Observatorului 123, Cluj-Napoca",
      image:
        "https://images.unsplash.com/photo-1571902943202-507ec2618e8f?q=80&w=1200&auto=format&fit=crop",
    },
    {
      id: 3,
      name: "Mărăști Gym",
      address: "Str. Fabricii 10, Cluj-Napoca",
      image:
        "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?q=80&w=1200&auto=format&fit=crop",
    },
  ];

  useEffect(() => {
    async function load() {
      try {
        const res = await getLocations();

        console.log("LOCATIONS RESPONSE:", res.data);

        if (!res.data || res.data.length === 0) {
          setLocations(demoLocations);
          setMessage("No backend locations found. Showing demo locations.");
          return;
        }

        setLocations(res.data);
      } catch {
        setLocations(demoLocations);
        setMessage("Backend unavailable. Showing demo locations.");
      }
    }

    load();
  }, []);

  return (
    <div className="app-shell">
      <Sidebar />

      <main>
        <h1 className="section-title">Locations</h1>
        <p className="section-subtitle">Explore premium locations.</p>

        {message && <div className="info-box">{message}</div>}

        <div className="grid-3">
          {locations.map((loc) => (
            <div className="location-card card" key={loc.id}>
              <div className="empty-image">
                <img src={loc.image} alt={loc.name} />
              </div>

              <h3>{loc.name}</h3>

              <p>{loc.address || "Premium fitness location"}</p>

              <span className="tag">ID: {loc.id}</span>
            </div>
          ))}
        </div>
      </main>
    </div>
  );
}

export default Locations;
import { useEffect, useState } from "react";
import Sidebar from "../components/Sidebar";
import StatCard from "../components/StatCard";
import { getCurrentUser } from "../services/authService";
import { getTrainerSchedule, getEnrolledCount, getEnrolledSignUps } from "../services/trainerService";
import { Dumbbell, Users, Clock, ChevronDown, ChevronUp } from "lucide-react";

function TrainerDashboard() {
  const user = getCurrentUser();
  const [courses, setCourses]     = useState([]);
  const [enrolled, setEnrolled]   = useState({});
  const [signups, setSignups]     = useState({});
  const [expanded, setExpanded]   = useState(null);
  const [loading, setLoading]     = useState(true);

  useEffect(() => {
    async function load() {
      try {
        const res  = await getTrainerSchedule(user.id);
        const list = res.data;
        setCourses(list);

        const counts = {};
        await Promise.all(list.map(async (c) => {
          try {
            const r = await getEnrolledCount(user.id, c.id);
            counts[c.id] = r.data;
          } catch { counts[c.id] = null; }
        }));
        setEnrolled(counts);
      } catch (e) {
        console.error(e);
      } finally {
        setLoading(false);
      }
    }
    load();
  }, [user.id]);

  async function toggleExpand(courseId) {
    if (expanded === courseId) { setExpanded(null); return; }
    setExpanded(courseId);
    if (!signups[courseId]) {
      try {
        const res = await getEnrolledSignUps(user.id, courseId);
        setSignups(prev => ({ ...prev, [courseId]: res.data }));
      } catch (e) { console.error(e); }
    }
  }

  const totalEnrolled = Object.values(enrolled).reduce(
    (sum, e) => sum + (e?.enrolledCount ?? 0), 0
  );

  return (
    <div className="app-shell">
      <Sidebar />
      <main>
        <h1 className="section-title">My Courses</h1>
        <p className="section-subtitle">Bine ai venit, {user?.firstName}. Iată programul tău.</p>

        <div className="grid-2" style={{ marginBottom: 32 }}>
          <StatCard title="Total Courses"  value={courses.length}  subtitle="Assigned to you"              icon={<Dumbbell size={20} />} />
          <StatCard title="Total Enrolled" value={totalEnrolled}   subtitle="Members across all courses"   icon={<Users size={20} />} />
        </div>

        {loading ? (
          <p style={{ color: "var(--muted)" }}>Loading...</p>
        ) : courses.length === 0 ? (
          <p style={{ color: "var(--muted)" }}>No courses assigned yet.</p>
        ) : (
          <div style={{ display: "flex", flexDirection: "column", gap: 16 }}>
            {courses.map((c) => {
              const info = enrolled[c.id];
              const pct  = info ? Math.round((info.enrolledCount / info.maxCapacity) * 100) : 0;
              const isOpen = expanded === c.id;

              return (
                <div key={c.id} className="card" style={{ border: "1px solid var(--border)", overflow: "hidden" }}>
                  {/* Course header */}
                  <div
                    style={{ padding: "20px 24px", display: "flex", justifyContent: "space-between", alignItems: "center", cursor: "pointer" }}
                    onClick={() => toggleExpand(c.id)}
                  >
                    <div style={{ display: "flex", gap: 32, alignItems: "center" }}>
                      <div>
                        <p style={{ fontWeight: 700, fontSize: 17, marginBottom: 4 }}>{c.name}</p>
                        <p style={{ color: "var(--muted)", fontSize: 13, display: "flex", alignItems: "center", gap: 6 }}>
                          <Clock size={13} />
                          {c.dayOfWeek?.charAt(0) + c.dayOfWeek?.slice(1).toLowerCase()} · {c.startTime} · {c.durationMinutes} min
                        </p>
                      </div>
                      {info && (
                        <div style={{ minWidth: 160 }}>
                          <div className="occupancy-row">
                            <span style={{ fontSize: 13 }}>{info.enrolledCount} / {info.maxCapacity} înscriși</span>
                            <span style={{ fontSize: 13, color: "var(--muted)" }}>{info.availableSpots} libere</span>
                          </div>
                          <div className="progress"><div style={{ width: `${pct}%` }} /></div>
                        </div>
                      )}
                    </div>
                    {isOpen ? <ChevronUp size={18} /> : <ChevronDown size={18} />}
                  </div>

                  {/* Signups list */}
                  {isOpen && (
                    <div style={{ borderTop: "1px solid var(--border)", padding: "0 24px 20px" }}>
                      <p style={{ fontWeight: 700, marginTop: 16, marginBottom: 12 }}>Listă înscrieri</p>
                      {!signups[c.id] ? (
                        <p style={{ color: "var(--muted)" }}>Loading...</p>
                      ) : signups[c.id].length === 0 ? (
                        <p style={{ color: "var(--muted)" }}>Niciun membru înscris.</p>
                      ) : (
                        <table>
                          <thead>
                            <tr>
                              <th>Membru ID</th>
                              <th>Data înscrierii</th>
                            </tr>
                          </thead>
                          <tbody>
                            {signups[c.id].map(s => (
                              <tr key={s.id}>
                                <td><strong>Membru #{s.memberId}</strong></td>
                                <td style={{ color: "var(--muted)", fontSize: 13 }}>
                                  {s.bookingTime ? new Date(
                                    s.bookingTime[0], s.bookingTime[1]-1, s.bookingTime[2]
                                  ).toLocaleDateString("ro-RO") : "—"}
                                </td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      )}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </main>
    </div>
  );
}

export default TrainerDashboard;

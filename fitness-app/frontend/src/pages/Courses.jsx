import { useEffect, useState } from "react";
import Sidebar from "../components/Sidebar";
import { getCourses, joinCourse } from "../services/courseService";
import { getCurrentUser } from "../services/authService";
import { Clock, MapPin, User, CalendarDays } from "lucide-react";

const demoCourses = [
  {
    id: 1,
    name: "Strength Training",
    type: "STRENGTH",
    trainerName: "Alex Pop",
    trainerInfo: "Strength coach · 6 years experience",
    dayOfWeek: "MONDAY",
    startTime: "18:00",
    duration: 60,
    locationName: "Central Gym",
    currentOccupancy: 12,
    maxCapacity: 20,
    image: "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?q=80&w=1200&auto=format&fit=crop",
  },
  {
    id: 2,
    name: "Yoga Flow",
    type: "YOGA",
    trainerName: "Ioana Radu",
    trainerInfo: "Yoga instructor · mobility & recovery",
    dayOfWeek: "TUESDAY",
    startTime: "09:00",
    duration: 45,
    locationName: "Zorilor Gym",
    currentOccupancy: 8,
    maxCapacity: 15,
    image: "https://images.unsplash.com/photo-1599901860904-17e6ed7083a0?q=80&w=1200&auto=format&fit=crop",
  },
  {
    id: 3,
    name: "HIIT Blast",
    type: "HIIT",
    trainerName: "Andrei M.",
    trainerInfo: "Conditioning trainer · high intensity",
    dayOfWeek: "FRIDAY",
    startTime: "19:00",
    duration: 50,
    locationName: "Mărăști Gym",
    currentOccupancy: 6,
    maxCapacity: 20,
    image: "https://images.unsplash.com/photo-1518611012118-696072aa579a?q=80&w=1200&auto=format&fit=crop",
  },
];

function Courses() {
  const [courses, setCourses] = useState([]);
  const [message, setMessage] = useState("");

  useEffect(() => {
    loadCourses();
  }, []);

  async function loadCourses() {
    try {
      const res = await getCourses();

      const normalized = res.data.map((course, index) => ({
        ...demoCourses[index % demoCourses.length],
        ...course,
        trainerName: course.trainerName || demoCourses[index % demoCourses.length].trainerName,
        trainerInfo: demoCourses[index % demoCourses.length].trainerInfo,
        locationName: course.locationName || demoCourses[index % demoCourses.length].locationName,
        image: demoCourses[index % demoCourses.length].image,
      }));

      setCourses(normalized.length ? normalized : demoCourses);
    } catch {
      setCourses(demoCourses);
      setMessage("Backend unavailable. Showing demo classes.");
    }
  }

  async function handleJoin(courseId) {
    const user = getCurrentUser();

    if (!user) {
      setMessage("You must be logged in to join a class.");
      return;
    }

    try {
      await joinCourse(courseId, user.id);
      setMessage("Successfully joined class.");
    } catch {
      setMessage("Could not join class. It may be full or already joined.");
    }
  }

  return (
    <div className="app-shell">
      <Sidebar />

      <main>
        <h1 className="section-title">Classes</h1>
        <p className="section-subtitle">
          Browse classes, trainers, schedule and occupancy.
        </p>

        {message && <div className="info-box">{message}</div>}

        <div className="grid-2">
          {courses.map((course) => {
            const percent = Math.round(
              ((course.currentOccupancy || 0) / course.maxCapacity) * 100
            );

            return (
              <div className="course-card card" key={course.id}>
                <div className="course-image">
                  <img src={course.image} alt={course.name} />
                </div>

                <div className="course-content">
                  <div className="course-top">
                    <div>
                      <h3>{course.name}</h3>
                      <p>{course.type}</p>
                    </div>
                    <span className="tag">{percent >= 100 ? "Full" : "Available"}</span>
                  </div>

                  <div className="course-info">
                    <span><User size={15} /> {course.trainerName}</span>
                    <span>{course.trainerInfo}</span>
                    <span><CalendarDays size={15} /> {course.dayOfWeek}</span>
                    <span><Clock size={15} /> {course.startTime} · {course.duration} min</span>
                    <span><MapPin size={15} /> {course.locationName}</span>
                  </div>

                  <div className="occupancy">
                    <div className="occupancy-row">
                      <span>{course.currentOccupancy}/{course.maxCapacity} spots</span>
                      <span>{percent}% full</span>
                    </div>
                    <div className="progress">
                      <div style={{ width: `${percent}%` }} />
                    </div>
                  </div>

                  <button className="primary-btn course-btn" onClick={() => handleJoin(course.id)}>
                    {percent >= 100 ? "Join waitlist" : "Join class"}
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      </main>
    </div>
  );
}

export default Courses;
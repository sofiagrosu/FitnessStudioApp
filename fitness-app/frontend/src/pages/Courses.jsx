import { useEffect, useState } from "react";
import Sidebar from "../components/Sidebar";
import { getCourses, joinCourse, getMemberCourses, getCourseSignups, getCourseWaitlist, leaveCourse } from "../services/courseService";
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
  const [courses, setCourses]             = useState([]);
  const [enrolledIds, setEnrolledIds]     = useState(new Set());
  const [message, setMessage]             = useState("");
  const [loadingId, setLoadingId]         = useState(null); // courseId being processed

  const user = getCurrentUser();

  useEffect(() => {
    loadAll();
  }, []);

  async function loadAll() {
    await loadCourses();
    if (user) await loadEnrolled();
  }

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

  async function loadEnrolled() {
    try {
      const res = await getMemberCourses(user.id);
      const ids = new Set(res.data.map((c) => c.id));
      setEnrolledIds(ids);
    } catch {
      setEnrolledIds(new Set());
    }
  }

  async function handleJoin(courseId) {
    if (!user) {
      setMessage("You must be logged in to join a class.");
      return;
    }

    setLoadingId(courseId);
    setMessage("");
    try {
      await joinCourse(courseId, user.id);
      setMessage("Successfully joined class!");
      // Reload both courses (counter update) and enrolled list
      await loadCourses();
      await loadEnrolled();
    } catch (err) {
      const status = err?.response?.status;
      if (status === 400) {
        setMessage("You need an active paid subscription to join a class. Go to Memberships to get one.");
      } else if (status === 409) {
        setMessage("You are already signed up for this class.");
      } else {
        setMessage("Could not join class. Please try again.");
      }
    } finally {
      setLoadingId(null);
    }
  }

  async function handleLeave(courseId) {
    if (!user) return;

    setLoadingId(courseId);
    setMessage("");
    try {
      // 1. Look in enrolled signups first
      const signupsRes = await getCourseSignups(courseId);
      let mySignUp = signupsRes.data.find((s) => s.member?.id === user.id);

      // 2. If not enrolled, look in waitlist (waitlisted members also have a SignUp)
      if (!mySignUp) {
        const waitlistRes = await getCourseWaitlist(courseId);
        const myWaitlistEntry = waitlistRes.data.find((e) => e.member?.id === user.id);
        if (myWaitlistEntry?.signUp) {
          mySignUp = myWaitlistEntry.signUp;
        }
      }

      if (!mySignUp) {
        setMessage("Could not find your sign-up. Please refresh and try again.");
        return;
      }

      await leaveCourse(mySignUp.id, user.id);
      setMessage("Successfully left the class.");
      // Reload both courses (counter update) and enrolled list
      await loadCourses();
      await loadEnrolled();
    } catch {
      setMessage("Could not leave class. Please try again.");
    } finally {
      setLoadingId(null);
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
            const isEnrolled = enrolledIds.has(course.id);
            const isLoading  = loadingId === course.id;
            const isFull     = percent >= 100;

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
                    <span className="tag">
                      {isEnrolled ? "Enrolled" : isFull ? "Full" : "Available"}
                    </span>
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
                      <div style={{ width: `${Math.min(percent, 100)}%` }} />
                    </div>
                  </div>

                  {isEnrolled ? (
                    <button
                      className="primary-btn course-btn"
                      onClick={() => handleLeave(course.id)}
                      disabled={isLoading}
                      style={{ background: "#e74c3c" }}
                    >
                      {isLoading ? "Processing..." : "Leave class"}
                    </button>
                  ) : (
                    <button
                      className="primary-btn course-btn"
                      onClick={() => handleJoin(course.id)}
                      disabled={isLoading}
                    >
                      {isLoading ? "Processing..." : isFull ? "Join waitlist" : "Join class"}
                    </button>
                  )}
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

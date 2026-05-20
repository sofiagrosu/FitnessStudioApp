import { useEffect, useState } from "react";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import {
  getCourses,
  joinCourse,
  getMemberCourses,
  getCourseSignups,
  getCourseWaitlist,
  leaveCourse,
} from "../services/courseService";
import { getCurrentUser } from "../services/authService";
import { Clock, MapPin, User, CalendarDays } from "lucide-react";

const courseImages = {
  YOGA:
    "https://images.unsplash.com/photo-1599901860904-17e6ed7083a0?q=80&w=1200&auto=format&fit=crop",
  SPINNING:
    "https://images.unsplash.com/photo-1534258936925-c58bed479fcb?q=80&w=1200&auto=format&fit=crop",
  ZUMBA:
    "https://images.unsplash.com/photo-1518611012118-696072aa579a?q=80&w=1200&auto=format&fit=crop",
  PILATES:
    "https://images.unsplash.com/photo-1518310383802-640c2de311b2?q=80&w=1200&auto=format&fit=crop",
};

function Courses() {
  const [courses, setCourses] = useState([]);
  const [enrolledIds, setEnrolledIds] = useState(new Set());
  const [message, setMessage] = useState("");
  const [loadingId, setLoadingId] = useState(null);

  const user = getCurrentUser();
  const isMember = user?.role === "MEMBER";

  useEffect(() => {
    loadAll();
  }, []);

  async function loadAll() {
    await loadCourses();

    if (isMember && user?.id) {
      await loadEnrolled();
    }
  }

  async function loadCourses() {
    try {
      const res = await getCourses();
      setCourses(res.data || []);
    } catch {
      setCourses([]);
      setMessage("Could not load classes from backend.");
    }
  }

  async function loadEnrolled() {
    try {
      const res = await getMemberCourses(user.id);
      const ids = new Set(res.data.map((course) => course.id));
      setEnrolledIds(ids);
    } catch {
      setEnrolledIds(new Set());
    }
  }

  async function handleJoin(courseId) {
    if (!isMember || !user?.id) {
      setMessage("You must be logged in as a member to join a class.");
      return;
    }

    setLoadingId(courseId);
    setMessage("");

    try {
      await joinCourse(courseId, user.id);
      setMessage("Successfully joined class!");
      await loadCourses();
      await loadEnrolled();
    } catch (err) {
      const status = err?.response?.status;

      if (status === 400) {
        setMessage(
          "You need an active paid subscription to join a class. Go to Memberships to get one."
        );
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
    if (!user?.id) return;

    setLoadingId(courseId);
    setMessage("");

    try {
      const signupsRes = await getCourseSignups(courseId);
      let mySignUp = signupsRes.data.find((s) => s.member?.id === user.id);

      if (!mySignUp) {
        const waitlistRes = await getCourseWaitlist(courseId);
        const myWaitlistEntry = waitlistRes.data.find(
          (entry) => entry.member?.id === user.id
        );

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
      await loadCourses();
      await loadEnrolled();
    } catch {
      setMessage("Could not leave class. Please try again.");
    } finally {
      setLoadingId(null);
    }
  }

  function renderCourses() {
    return (
      <div className="grid-2">
        {courses.map((course) => {
          const type = course.type || "YOGA";
          const image = courseImages[type] || courseImages.YOGA;

          const currentOccupancy = course.currentOccupancy || 0;
          const maxCapacity = course.maxCapacity || 1;
          const percent = Math.round((currentOccupancy / maxCapacity) * 100);

          const isEnrolled = enrolledIds.has(course.id);
          const isLoading = loadingId === course.id;
          const isFull = percent >= 100;

          return (
            <div className="course-card card" key={course.id}>
              <div className="course-image">
                <img src={image} alt={course.name || type} />
              </div>

              <div className="course-content">
                <div className="course-top">
                  <div>
                    <h3>{course.name || type}</h3>
                    <p>{type}</p>
                  </div>

                  <span className="tag">
                    {isEnrolled ? "Enrolled" : isFull ? "Full" : "Available"}
                  </span>
                </div>

                <div className="course-info">
                  <span>
                    <User size={15} /> {course.trainerName || "Trainer"}
                  </span>

                  {course.trainerInfo && <span>{course.trainerInfo}</span>}

                  <span>
                    <CalendarDays size={15} /> {course.dayOfWeek || "Weekly"}
                  </span>

                  <span>
                    <Clock size={15} /> {course.startTime || "--:--"} ·{" "}
                    {course.duration || 60} min
                  </span>

                  <span>
                    <MapPin size={15} />{" "}
                    {course.locationName || "Fitness location"}
                  </span>
                </div>

                <div className="occupancy">
                  <div className="occupancy-row">
                    <span>
                      {currentOccupancy}/{maxCapacity} spots
                    </span>
                    <span>{Math.min(percent, 100)}% full</span>
                  </div>

                  <div className="progress">
                    <div style={{ width: `${Math.min(percent, 100)}%` }} />
                  </div>
                </div>

                {isMember && (
                  isEnrolled ? (
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
                      {isLoading
                        ? "Processing..."
                        : isFull
                        ? "Join waitlist"
                        : "Join class"}
                    </button>
                  )
                )}
              </div>
            </div>
          );
        })}
      </div>
    );
  }

  if (!user) {
    return (
      <>
        <Navbar />

        <main style={{ padding: "2rem" }}>
          <h1 className="section-title">Classes</h1>
          <p className="section-subtitle">
            Browse Yoga, Spinning, Zumba and Pilates classes.
          </p>

          {message && <div className="info-box">{message}</div>}

          {courses.length > 0 ? (
            renderCourses()
          ) : (
            <div className="info-box">No classes available yet.</div>
          )}
        </main>
      </>
    );
  }

  return (
    <div className="app-shell">
      <Sidebar />

      <main>
        <h1 className="section-title">Classes</h1>
        <p className="section-subtitle">
          Browse Yoga, Spinning, Zumba and Pilates classes.
        </p>

        {message && <div className="info-box">{message}</div>}

        {courses.length > 0 ? (
          renderCourses()
        ) : (
          <div className="info-box">No classes available yet.</div>
        )}
      </main>
    </div>
  );
}

export default Courses;
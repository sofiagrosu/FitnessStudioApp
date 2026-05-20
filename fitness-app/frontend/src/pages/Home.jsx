import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  CalendarCheck,
  CreditCard,
  MapPin,
  ShieldCheck,
  Users,
  Check,
} from "lucide-react";

import Navbar from "../components/Navbar";
import { getCourses } from "../services/courseService";
import { getLocations } from "../services/locationService";

const heroImage =
  "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?q=80&w=1400&auto=format&fit=crop";

const courseImages = {
  YOGA: "https://images.unsplash.com/photo-1599901860904-17e6ed7083a0?q=80&w=1200&auto=format&fit=crop",
  SPINNING: "https://images.unsplash.com/photo-1534258936925-c58bed479fcb?q=80&w=1200&auto=format&fit=crop",
  ZUMBA: "https://images.unsplash.com/photo-1518611012118-696072aa579a?q=80&w=1200&auto=format&fit=crop",
  PILATES: "https://images.unsplash.com/photo-1518310383802-640c2de311b2?q=80&w=1200&auto=format&fit=crop",
};

const locationImages = [
  "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?q=80&w=1200&auto=format&fit=crop",
  "https://images.unsplash.com/photo-1571902943202-507ec2618e8f?q=80&w=1200&auto=format&fit=crop",
  "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?q=80&w=1200&auto=format&fit=crop",
];

const plans = [
  {
    name: "MONTHLY",
    price: "150",
    features: ["Gym access", "Classes", "Check-ins"],
  },
  {
    name: "THREE_MONTHS",
    price: "400",
    features: ["Better price", "All classes", "All locations"],
  },
  {
    name: "YEARLY",
    price: "1200",
    features: ["Best value", "Full access", "Priority support"],
  },
];

function Home() {
  const [courses, setCourses] = useState([]);
  const [locations, setLocations] = useState([]);

  useEffect(() => {
    loadHomeData();
  }, []);

  async function loadHomeData() {
    try {
      const coursesRes = await getCourses();
      setCourses(coursesRes.data || []);
    } catch {
      setCourses([]);
    }

    try {
      const locationsRes = await getLocations();
      setLocations(locationsRes.data || []);
    } catch {
      setLocations([]);
    }
  }

  const courseTypes = ["YOGA", "SPINNING", "ZUMBA", "PILATES"];

  const visibleCourses =
    courses.length > 0
      ? courses.slice(0, 4)
      : courseTypes.map((type, index) => ({
          id: index + 1,
          name: type,
          type,
          trainerName: "Trainer",
          dayOfWeek: "Scheduled weekly",
          startTime: "",
          duration: 60,
        }));

  const visibleLocations = locations.slice(0, 3);

  return (
    <>
      <Navbar />

      <main className="home-page">
        <section className="hero-section card">
          <div className="hero-content">
            <p className="eyebrow">FITNESS. COMMUNITY. PROGRESS.</p>

            <h1>
              Train smarter.
              <br />
              Manage your <span>fitness journey.</span>
            </h1>

            <p>
              Book classes, manage memberships, track check-ins and payments —
              all in one clean fitness platform.
            </p>

            <div className="hero-actions">
              <Link to="/register" className="primary-btn">
                Get started
              </Link>

              <Link to="/courses" className="secondary-btn">
                View classes
              </Link>
            </div>
          </div>

          <div className="hero-visual">
            <div className="hero-image-placeholder hero-real-image">
              <img src={heroImage} alt="Premium Gym Studio" />
              <span>Premium Gym Studio</span>
            </div>
          </div>
        </section>

        <section className="features-row card">
          <div className="feature-item">
            <CalendarCheck />
            <h4>Class booking</h4>
            <p>Book and join your favorite classes.</p>
          </div>

          <div className="feature-item">
            <ShieldCheck />
            <h4>Memberships</h4>
            <p>Manage, upgrade or renew your plan.</p>
          </div>

          <div className="feature-item">
            <Users />
            <h4>Check-ins</h4>
            <p>Track visits and activity history.</p>
          </div>

          <div className="feature-item">
            <CreditCard />
            <h4>Payments</h4>
            <p>View history and download receipts.</p>
          </div>

          <div className="feature-item">
            <MapPin />
            <h4>Locations</h4>
            <p>Find gyms and occupancy.</p>
          </div>
        </section>

        <section className="home-section">
          <div className="section-header">
            <div>
              <h2>Available classes</h2>
              <p>Yoga, Spinning, Zumba and Pilates sessions.</p>
            </div>

            <Link to="/courses">View all</Link>
          </div>

          <div className="class-preview-grid">
            {visibleCourses.map((course) => {
              const type = course.type || course.name;
              const image = courseImages[type] || courseImages.YOGA;

              return (
                <div className="class-preview-card card" key={course.id || type}>
                  <div className="class-thumb">
                    <img src={image} alt={course.name || type} />
                  </div>

                  <h3>{course.name || type}</h3>

                  <p>{type}</p>

                  <div className="class-meta">
                    <span>
                      {course.dayOfWeek || "Weekly"}{" "}
                      {course.startTime ? `· ${course.startTime}` : ""}
                    </span>
                  </div>
                </div>
              );
            })}
          </div>
        </section>

        <section className="home-section">
          <div className="section-header">
            <div>
              <h2>Memberships</h2>
              <p>Choose the plan that fits your goals.</p>
            </div>

            <Link to="/memberships">View plans</Link>
          </div>

          <div className="grid-3">
            {plans.map((plan, index) => (
              <div
                className={`plan-card card ${index === 1 ? "featured-plan" : ""}`}
                key={plan.name}
              >
                {index === 1 && <span className="plan-badge">Most popular</span>}
                <h3>{plan.name}</h3>
                <div className="plan-price">
                  {plan.price}
                  <span> RON / month</span>
                </div>

                <div className="plan-features">
                  {plan.features.map((f) => (
                    <div className="plan-feature" key={f}>
                      <Check size={16} />
                      <span>{f}</span>
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </section>

        <section className="home-section">
          <div className="section-header">
            <div>
              <h2>Our Locations</h2>
              <p>Available gyms across the city.</p>
            </div>

            <Link to="/locations">View all</Link>
          </div>

          <div className="grid-3">
            {visibleLocations.map((loc, index) => (
              <div className="location-card card" key={loc.id}>
                <div className="empty-image">
                  <img
                    src={loc.image || locationImages[index % locationImages.length]}
                    alt={loc.name}
                  />
                </div>

                <h3>{loc.name}</h3>
                <p>{loc.address}</p>
              </div>
            ))}
          </div>
        </section>

        <section className="stats-strip">
          <div>
            <strong>25+</strong>
            <span>Active members</span>
          </div>

          <div>
            <strong>{courses.length || 4}</strong>
            <span>Available classes</span>
          </div>

          <div>
            <strong>{locations.length || 0}</strong>
            <span>Available locations</span>
          </div>

          <div>
            <strong>4</strong>
            <span>Course types</span>
          </div>
        </section>
      </main>
    </>
  );
}

export default Home;
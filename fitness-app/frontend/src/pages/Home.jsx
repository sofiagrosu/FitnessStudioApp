import { Link } from "react-router-dom";
import {
  CalendarCheck,
  CreditCard,
  MapPin,
  ShieldCheck,
  Users,
} from "lucide-react";

import Navbar from "../components/Navbar";
import PlanCard from "../components/PlanCard";

const heroImage =
  "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?q=80&w=1400&auto=format&fit=crop";

const classImages = [
  "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?q=80&w=1200&auto=format&fit=crop",
  "https://images.unsplash.com/photo-1599901860904-17e6ed7083a0?q=80&w=1200&auto=format&fit=crop",
  "https://images.unsplash.com/photo-1518611012118-696072aa579a?q=80&w=1200&auto=format&fit=crop",
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

const classes = [
  {
    name: "Strength Training",
    trainer: "Alex Pop",
    time: "Mon, Wed, Fri · 18:00",
    spots: "12 / 20",
  },
  {
    name: "Yoga Flow",
    trainer: "Ioana R.",
    time: "Tue, Thu · 09:00",
    spots: "8 / 15",
  },
  {
    name: "HIIT Blast",
    trainer: "Andrei M.",
    time: "Mon, Wed, Fri · 19:00",
    spots: "6 / 20",
  },
];

function Home() {
  return (
    <>
      <Navbar />

      <main className="home-page">
        <section className="hero-section card">
          <div className="hero-content">
            <p className="eyebrow">
              FITNESS. COMMUNITY. PROGRESS.
            </p>

            <h1>
              Train smarter.
              <br />
              Manage your <span>fitness journey.</span>
            </h1>

            <p>
              Book classes, manage memberships,
              track check-ins and payments —
              all in one clean fitness platform.
            </p>

            <div className="hero-actions">
              <Link
                to="/register"
                className="primary-btn"
              >
                Get started
              </Link>

              <Link
                to="/courses"
                className="secondary-btn"
              >
                View classes
              </Link>
            </div>
          </div>

          <div className="hero-visual">
            <div className="hero-image-placeholder hero-real-image">
              <img
                src={heroImage}
                alt="Premium Gym Studio"
              />

              <span>Premium Gym Studio</span>
            </div>
          </div>
        </section>

        <section className="features-row card">
          <div className="feature-item">
            <CalendarCheck />
            <h4>Class booking</h4>
            <p>
              Book and join your favorite classes.
            </p>
          </div>

          <div className="feature-item">
            <ShieldCheck />
            <h4>Memberships</h4>
            <p>
              Manage, upgrade or renew your plan.
            </p>
          </div>

          <div className="feature-item">
            <Users />
            <h4>Check-ins</h4>
            <p>
              Track visits and activity history.
            </p>
          </div>

          <div className="feature-item">
            <CreditCard />
            <h4>Payments</h4>
            <p>
              View history and download receipts.
            </p>
          </div>

          <div className="feature-item">
            <MapPin />
            <h4>Locations</h4>
            <p>
              Find gyms, zones and occupancy.
            </p>
          </div>
        </section>

        <section className="home-section">
          <div className="section-header">
            <div>
              <h2>Popular classes</h2>
              <p>
                Explore the most booked sessions this week.
              </p>
            </div>

            <Link to="/courses">
              View all
            </Link>
          </div>

          <div className="class-preview-grid">
            {classes.map((item, index) => (
              <div
                className="class-preview-card card"
                key={item.name}
              >
                <div className="class-thumb">
                  <img
                    src={classImages[index]}
                    alt={item.name}
                  />
                </div>

                <h3>{item.name}</h3>

                <p>
                  With {item.trainer}
                </p>

                <div className="class-meta">
                  <span>{item.time}</span>
                  <strong>{item.spots}</strong>
                </div>
              </div>
            ))}
          </div>
        </section>

        <section className="home-section">
          <div className="section-header">
            <div>
              <h2>Memberships</h2>
              <p>
                Choose the plan that fits your goals.
              </p>
            </div>

            <Link to="/memberships">
              View plans
            </Link>
          </div>

          <div className="grid-3">
            {plans.map((plan) => (
              <PlanCard
                key={plan.name}
                plan={plan}
                featured={plan.name === "THREE_MONTHS"}
              />
            ))}
          </div>
        </section>

        <section className="stats-strip">
          <div>
            <strong>1200+</strong>
            <span>Active members</span>
          </div>

          <div>
            <strong>50+</strong>
            <span>Weekly classes</span>
          </div>

          <div>
            <strong>3</strong>
            <span>Premium locations</span>
          </div>

          <div>
            <strong>15+</strong>
            <span>Trainers</span>
          </div>
        </section>
      </main>
    </>
  );
}

export default Home;

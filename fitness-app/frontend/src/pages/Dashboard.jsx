import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Sidebar from "../components/Sidebar";
import StatCard from "../components/StatCard";
import { CalendarDays, CreditCard, Dumbbell, Users } from "lucide-react";
import { getCurrentUser } from "../services/authService";
import { getActiveSubscription } from "../services/subscriptionService";
import { getPayments } from "../services/paymentService";

function Dashboard() {
  const [subscription, setSubscription] = useState(null);
  const [payments, setPayments]         = useState([]);
  const [checkIns, setCheckIns]         = useState([]);
  const navigate = useNavigate();

  const user = getCurrentUser();

  useEffect(() => {
    if (!user) return;

    // Abonament activ
    getActiveSubscription(user.id)
      .then((res) => setSubscription(res.data))
      .catch(() => setSubscription(null));

    // Plati
    getPayments(user.id)
      .then((res) => setPayments(res.data))
      .catch(() => setPayments([]));

    // Check-in-uri
    import("../services/checkInService").then(({ getMemberCheckIns }) => {
      getMemberCheckIns(user.id)
        .then((res) => setCheckIns(res.data))
        .catch(() => setCheckIns([]));
    });
  }, []);

  // --- date abonament ---
  function subscriptionValue() {
    if (!subscription) return "None";
    if (!subscription.paid) return "Unpaid";
    return subscription.type.replace("_", " ");
  }

  function subscriptionSubtitle() {
    if (!subscription) return "Go to Memberships to choose a plan";
    if (!subscription.paid) return "Go to Memberships to pay";
    if (subscription.endDate) return `Valid until ${subscription.endDate}`;
    if (subscription.remainingEntries != null)
      return `${subscription.remainingEntries} entries remaining`;
    return "Active";
  }

  // --- ultima plată ---
  const lastPayment = payments.length > 0 ? payments[payments.length - 1] : null;

  return (
    <div className="app-shell">
      <Sidebar />

      <main>
        <h1 className="section-title">Dashboard</h1>
        <p className="section-subtitle">Welcome back to your fitness journey.</p>

        <div className="grid-2">
          <div onClick={() => navigate("/memberships")} style={{ cursor: "pointer" }}>
            <StatCard
              title="Subscription"
              value={subscriptionValue()}
              subtitle={subscriptionSubtitle()}
              icon={<Dumbbell size={20} />}
            />
          </div>

          <StatCard
            title="Remaining Entries"
            value={
              subscription?.type === "TEN_ENTRIES"
                ? (subscription?.remainingEntries ?? "—")
                : "∞"
            }
            subtitle={
              subscription?.type === "TEN_ENTRIES"
                ? "10-entry plan"
                : subscription
                ? "Unlimited (monthly/annual)"
                : "No active plan"
            }
            icon={<CalendarDays size={20} />}
          />

          <StatCard
            title="Total Check-ins"
            value={checkIns.length}
            subtitle="All time"
            icon={<Users size={20} />}
          />

          <StatCard
            title="Last Payment"
            value={lastPayment ? `${lastPayment.amount} RON` : "—"}
            subtitle={
              lastPayment
                ? `${lastPayment.method} · ${lastPayment.paymentDate?.slice(0, 10)}`
                : "No payments yet"
            }
            icon={<CreditCard size={20} />}
          />
        </div>
      </main>
    </div>
  );
}

export default Dashboard;

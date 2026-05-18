import Sidebar from "../components/Sidebar";
import StatCard from "../components/StatCard";
import {
  CalendarDays,
  CreditCard,
  Dumbbell,
  Users
} from "lucide-react";

function Dashboard() {
  return (
    <div className="app-shell">
      <Sidebar />

      <main>
        <h1 className="section-title">Dashboard</h1>
        <p className="section-subtitle">
          Welcome back to your fitness journey.
        </p>

        <div className="grid-2">
          <StatCard
            title="Active Membership"
            value="PRO"
            subtitle="Valid until Aug 2025"
            icon={<Dumbbell size={20} />}
          />

          <StatCard
            title="Remaining Entries"
            value="12"
            subtitle="Monthly plan"
            icon={<CalendarDays size={20} />}
          />

          <StatCard
            title="Total Check-ins"
            value="48"
            subtitle="This year"
            icon={<Users size={20} />}
          />

          <StatCard
            title="Recent Payment"
            value="250 RON"
            subtitle="Paid yesterday"
            icon={<CreditCard size={20} />}
          />
        </div>
      </main>
    </div>
  );
}

export default Dashboard;
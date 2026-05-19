import { useState } from "react";
import Sidebar from "../components/Sidebar";
import PlanCard from "../components/PlanCard";
import { createSubscription } from "../services/subscriptionService";
import { getCurrentUser } from "../services/authService";

const plans = [
  { name: "MONTHLY", price: "150", features: ["Gym access", "Classes", "Check-ins"] },
  { name: "THREE_MONTHS", price: "400", features: ["Better price", "All classes", "All locations"] },
  { name: "YEARLY", price: "1200", features: ["Best value", "Full access", "Priority support"] },
];

function Memberships() {
  const [message, setMessage] = useState("");

  async function choosePlan(plan) {
    const user = getCurrentUser();

    if (!user) {
      setMessage("You must be logged in to choose a plan.");
      return;
    }

    try {
      await createSubscription(user.id, plan.name, Number(plan.price));
      setMessage(`Subscription ${plan.name} created successfully.`);
    } catch {
      setMessage("Could not create subscription. Check SubscriptionType names in backend.");
    }
  }

  return (
    <div className="app-shell">
      <Sidebar />

      <main>
        <h1 className="section-title">Memberships</h1>
        <p className="section-subtitle">Choose or manage your subscription.</p>

        {message && <div className="info-box">{message}</div>}

        <div className="grid-3">
          {plans.map((plan, index) => (
            <div key={plan.name} onClick={() => choosePlan(plan)}>
              <PlanCard plan={plan} featured={index === 1} />
            </div>
          ))}
        </div>
      </main>
    </div>
  );
}

export default Memberships;
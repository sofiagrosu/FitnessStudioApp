import { useState, useEffect } from "react";
import Sidebar from "../components/Sidebar";
import PlanCard from "../components/PlanCard";
import { createSubscription, getActiveSubscription, cancelSubscription } from "../services/subscriptionService";
import { registerPayment } from "../services/paymentService";
import { getCurrentUser } from "../services/authService";

const plans = [
  { type: "MONTHLY",     name: "Monthly",    price: "150",  features: ["Gym access", "All classes", "Check-ins"] },
  { type: "ANNUAL",      name: "Annual",     price: "1200", features: ["Best value", "Full access", "Priority support"] },
  { type: "TEN_ENTRIES", name: "10 Entries", price: "100",  features: ["10 gym entries", "Flexible usage", "No expiry date"] },
];

function Memberships() {
  const [message, setMessage]           = useState("");
  const [subscription, setSubscription] = useState(null);
  const [paying, setPaying]             = useState(false);
  const [loading, setLoading]           = useState(true);

  const user = getCurrentUser();

  useEffect(() => {
    if (!user) { setLoading(false); return; }
    getActiveSubscription(user.id)
      .then((res) => setSubscription(res.data))
      .catch(() => setSubscription(null))
      .finally(() => setLoading(false));
  }, []);

  async function choosePlan(plan) {
    if (!user) { setMessage("You must be logged in to choose a plan."); return; }
    if (subscription) { setMessage("You already have an active subscription."); return; }

    try {
      const res = await createSubscription(user.id, plan.type, Number(plan.price));
      setSubscription(res.data);
      setMessage("Subscription created! Click 'Pay now' to activate it.");
    } catch {
      setMessage("Could not create subscription. Please try again.");
    }
  }

  async function handleCancel() {
    if (!subscription) return;
    try {
      await cancelSubscription(subscription.id);
      setSubscription(null);
      setMessage("Subscription cancelled. You can now choose a new plan.");
    } catch {
      setMessage("Could not cancel subscription. Please try again.");
    }
  }

  async function handlePay() {
    if (!subscription) return;
    setPaying(true);
    try {
      await registerPayment(user.id, subscription.id, subscription.price);
      setMessage("Payment successful! Your subscription is now active.");
      const res = await getActiveSubscription(user.id);
      setSubscription(res.data);
    } catch {
      setMessage("Payment failed. Please try again.");
    } finally {
      setPaying(false);
    }
  }

  if (loading) return <div className="app-shell"><Sidebar /><main><p>Loading...</p></main></div>;

  return (
    <div className="app-shell">
      <Sidebar />

      <main>
        <h1 className="section-title">Memberships</h1>
        <p className="section-subtitle">Choose or manage your subscription.</p>

        {message && <div className="info-box">{message}</div>}

        <div className="grid-3">
          {plans.map((plan, index) => {
            const isCurrent = subscription?.type === plan.type;
            return (
              <PlanCard
                key={plan.type}
                plan={plan}
                featured={index === 1}
                isCurrent={isCurrent}
                isPaid={isCurrent && subscription?.paid}
                onChoose={() => choosePlan(plan)}
                onPay={handlePay}
                onCancel={handleCancel}
                paying={paying}
              />
            );
          })}
        </div>
      </main>
    </div>
  );
}

export default Memberships;

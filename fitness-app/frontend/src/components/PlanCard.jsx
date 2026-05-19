import { Check } from "lucide-react";

function PlanCard({ plan, featured }) {
  return (
    <div className={`plan-card card ${featured ? "featured-plan" : ""}`}>
      {featured && <span className="plan-badge">Most popular</span>}

      <h3>{plan.name}</h3>

      <div className="plan-price">
        {plan.price}
        <span> RON / month</span>
      </div>

      <div className="plan-features">
        {plan.features.map((feature) => (
          <div className="plan-feature" key={feature}>
            <Check size={16} />
            <span>{feature}</span>
          </div>
        ))}
      </div>

      <button className="primary-btn">
        Choose plan
      </button>
    </div>
  );
}

export default PlanCard;
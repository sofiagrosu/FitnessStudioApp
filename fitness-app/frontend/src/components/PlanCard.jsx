import { Check } from "lucide-react";

function PlanCard({ plan, featured, isCurrent, isPaid, onChoose, onPay, onCancel, paying }) {
  return (
    <div className={`plan-card card ${featured ? "featured-plan" : ""} ${isCurrent ? "current-plan" : ""}`}>
      {featured && !isCurrent && <span className="plan-badge">Most popular</span>}
      {isCurrent && <span className="plan-badge">Your plan</span>}

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

      {isCurrent && isPaid && (
        <button className="primary-btn" disabled style={{ opacity: 0.6, cursor: "default" }}>
          ✓ Active
        </button>
      )}

      {isCurrent && !isPaid && (
        <button className="primary-btn" onClick={onPay} disabled={paying}>
          {paying ? "Processing..." : "Pay now"}
        </button>
      )}

      {isCurrent && (
        <button
          onClick={onCancel}
          style={{
            marginTop: "0.5rem",
            width: "100%",
            padding: "0.5rem",
            background: "transparent",
            border: "1px solid #ccc",
            borderRadius: "8px",
            cursor: "pointer",
            color: "#888",
            fontSize: "0.85rem",
          }}
        >
          Cancel plan
        </button>
      )}

      {!isCurrent && (
        <button className="primary-btn" onClick={onChoose}>
          Choose plan
        </button>
      )}
    </div>
  );
}

export default PlanCard;

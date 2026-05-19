function StatCard({ title, value, subtitle, icon }) {
  return (
    <div className="stat-card card">
      <div>
        <p className="stat-title">{title}</p>
        <h3>{value}</h3>
        <p className="stat-subtitle">{subtitle}</p>
      </div>

      {icon && <div className="stat-icon">{icon}</div>}
    </div>
  );
}

export default StatCard;
import "./SummaryMetricCard.css";

interface SummaryMetricCardProps {
  icon: string;
  label: string;
  children: React.ReactNode;
}

export default function SummaryMetricCard({ icon, label, children }: SummaryMetricCardProps) {
  return (
    <div className="metric-card">
      <div className="metric-card__header">
        <span className="material-symbols-outlined metric-card__icon">{icon}</span>
        <span className="metric-card__label">{label}</span>
      </div>
      <div className="metric-card__value">{children}</div>
    </div>
  );
}

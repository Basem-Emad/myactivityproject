import "./ChartCard.css";

interface ChartCardProps {
  title: string;
  subtitle?: string;
  badge?: string;
  children: React.ReactNode;
  className?: string;
}

export default function ChartCard({ title, subtitle, badge, children, className = "" }: ChartCardProps) {
  return (
    <div className={`chart-card ${className}`}>
      <div className="chart-card__header">
        <div>
          <h3 className="chart-card__title">{title}</h3>
          {subtitle && <p className="chart-card__subtitle">{subtitle}</p>}
        </div>
        {badge && <span className="chart-card__badge">{badge}</span>}
      </div>
      <div className="chart-card__content">{children}</div>
    </div>
  );
}

function SummaryCard({ title, value, description }) {
  return (
    <div className="summary-card">
      <p className="summary-title">{title}</p>
      <h2>{value}</h2>
      <p className="summary-description">{description}</p>
    </div>
  );
}

export default SummaryCard;

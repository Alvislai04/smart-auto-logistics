function MetricBarChart({ title, metrics = [] }) {
  const maxValue = Math.max(...metrics.map((metric) => metric.value), 1);

  return (
    <section className="report-chart panel">
      <h3>{title}</h3>

      <div className="report-chart-bars">
        {metrics.map((metric) => {
          const width = `${Math.max((metric.value / maxValue) * 100, 4)}%`;

          return (
            <div className="report-chart-row" key={metric.key}>
              <div className="report-chart-label">
                <span>{metric.label}</span>
                <strong>{metric.value}</strong>
              </div>
              <div className="report-chart-track" aria-hidden="true">
                <span style={{ width }} />
              </div>
            </div>
          );
        })}
      </div>
    </section>
  );
}

export default MetricBarChart;

-- Flyway migration: create daily_sales_reports table
CREATE TABLE IF NOT EXISTS daily_sales_reports (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  processed_count BIGINT NOT NULL,
  total_revenue DECIMAL(19,4) NOT NULL,
  duration_ms BIGINT NOT NULL,
  strategy VARCHAR(128) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- High-value indexes for common order dashboard, archive, and transport lookups.
-- Review against the live schema before enabling Flyway in production.
CREATE INDEX idx_orders_order_number_regel ON orders (Verkooporder, regel);
CREATE INDEX idx_orders_user_delivery ON orders (Gebruiker_I, Leverdatum);
CREATE INDEX idx_archived_orders_order_number_regel ON archived_orders (Verkooporder, regel);
CREATE INDEX idx_tra_order_lines_order ON tra_order_lines (orderType, orderId);

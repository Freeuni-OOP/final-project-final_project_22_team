ALTER TABLE price_alerts
    ADD COLUMN last_notification_price DECIMAL(10, 2) DEFAULT NULL;
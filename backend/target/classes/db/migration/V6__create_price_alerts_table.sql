CREATE TABLE price_alerts (
                              id BIGINT NOT NULL AUTO_INCREMENT,
                              user_id BIGINT NOT NULL,
                              product_id BIGINT NOT NULL,
                              target_price DECIMAL(10, 2) NOT NULL,
                              is_triggered BOOLEAN NOT NULL DEFAULT FALSE,
                              PRIMARY KEY (id),
                              CONSTRAINT fk_alert_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                              CONSTRAINT fk_alert_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB;
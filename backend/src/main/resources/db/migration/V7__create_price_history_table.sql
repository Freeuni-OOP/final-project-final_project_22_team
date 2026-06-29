CREATE TABLE price_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL ,
    price DECIMAL(10, 2) NOT NULL,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_price_history_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB;
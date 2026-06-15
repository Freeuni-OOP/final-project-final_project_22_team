CREATE TABLE shop_products (
                               id BIGINT NOT NULL AUTO_INCREMENT,
                               product_id BIGINT NOT NULL,
                               shop_id BIGINT NOT NULL,
                               price DECIMAL(10, 2) NOT NULL,
                               stock_status VARCHAR(50) NOT NULL DEFAULT 'In Stock',
                               product_url VARCHAR(512) NOT NULL,
                               last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                               PRIMARY KEY (id),
                               CONSTRAINT fk_shop_product_main FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
                               CONSTRAINT fk_shop_product_shop FOREIGN KEY (shop_id) REFERENCES shops(id) ON DELETE CASCADE
) ENGINE=InnoDB;
CREATE TABLE products (
                          id BIGINT NOT NULL AUTO_INCREMENT,
                          category_id BIGINT NOT NULL,
                          name VARCHAR(255) NOT NULL,
                          brand VARCHAR(100),
                          description TEXT,
                          image_url VARCHAR(512),
                          PRIMARY KEY (id),
                          CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT
) ENGINE=InnoDB;
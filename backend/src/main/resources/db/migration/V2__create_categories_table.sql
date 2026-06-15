CREATE TABLE categories (
                            id BIGINT NOT NULL AUTO_INCREMENT,
                            name VARCHAR(100) NOT NULL,
                            parent_id BIGINT NULL,
                            PRIMARY KEY (id),
                            CONSTRAINT fk_category_parent FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL
) ENGINE=InnoDB;
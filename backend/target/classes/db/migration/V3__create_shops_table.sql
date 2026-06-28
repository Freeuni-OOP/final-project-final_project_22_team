CREATE TABLE shops (
                       id BIGINT NOT NULL AUTO_INCREMENT,
                       name VARCHAR(100) NOT NULL,
                       base_url VARCHAR(255) NOT NULL,
                       PRIMARY KEY (id)
) ENGINE=InnoDB;
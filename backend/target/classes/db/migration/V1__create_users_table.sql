CREATE TABLE users (
                       id BIGINT NOT NULL AUTO_INCREMENT,
                       email VARCHAR(150) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       PRIMARY KEY (id)
) ENGINE=InnoDB;
USE hikebuddy;

CREATE TABLE User (
                      id INT AUTO_INCREMENT PRIMARY KEY,
                      username VARCHAR(50) UNIQUE NOT NULL,
                      password_hash VARCHAR(255) NOT NULL,
                      salt VARCHAR(100) NOT NULL,
                      hiking_level ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED') DEFAULT 'BEGINNER',
                      bio TEXT,
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE HikeRoute (
                           id INT AUTO_INCREMENT PRIMARY KEY,
                           name VARCHAR(100) NOT NULL,
                           region VARCHAR(100),
                           difficulty ENUM('EASY', 'MEDIUM', 'HARD'),
                           distance DECIMAL(5,2),
                           description TEXT
);

CREATE TABLE JourneyEntry (
                              id INT AUTO_INCREMENT PRIMARY KEY,
                              user_id INT NOT NULL,
                              hike_route_id INT,
                              date DATE,
                              distance DECIMAL(5,2),
                              difficulty ENUM('EASY', 'MEDIUM', 'HARD'),
                              status ENUM('WISHLIST', 'PENDING', 'COMPLETED') DEFAULT 'PENDING',
                              notes TEXT,
                              FOREIGN KEY (user_id) REFERENCES User(id),
                              FOREIGN KEY (hike_route_id) REFERENCES HikeRoute(id)
);

CREATE TABLE StoryFolder (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             user_id INT NOT NULL,
                             journey_entry_id INT,
                             name VARCHAR(100) NOT NULL,
                             description TEXT,
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             FOREIGN KEY (user_id) REFERENCES User(id),
                             FOREIGN KEY (journey_entry_id) REFERENCES JourneyEntry(id)
);

CREATE TABLE Photo (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       folder_id INT NOT NULL,
                       file_path VARCHAR(255) NOT NULL,
                       uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       FOREIGN KEY (folder_id) REFERENCES StoryFolder(id)
);

CREATE TABLE FriendRequest (
                               id INT AUTO_INCREMENT PRIMARY KEY,
                               sender_id INT NOT NULL,
                               receiver_id INT NOT NULL,
                               status ENUM('PENDING', 'ACCEPTED', 'DECLINED') DEFAULT 'PENDING',
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               FOREIGN KEY (sender_id) REFERENCES User(id),
                               FOREIGN KEY (receiver_id) REFERENCES User(id),
                               UNIQUE KEY unique_request (sender_id, receiver_id)
);

CREATE TABLE Friendship (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            user_id_1 INT NOT NULL,
                            user_id_2 INT NOT NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (user_id_1) REFERENCES User(id),
                            FOREIGN KEY (user_id_2) REFERENCES User(id),
                            UNIQUE KEY unique_friendship (user_id_1, user_id_2)
);

CREATE TABLE Gear (
                      id INT AUTO_INCREMENT PRIMARY KEY,
                      user_id INT NOT NULL,
                      name VARCHAR(100) NOT NULL,
                      is_checked BOOLEAN DEFAULT FALSE,
                      FOREIGN KEY (user_id) REFERENCES User(id)
);

CREATE TABLE AvailableDay (
                              id INT AUTO_INCREMENT PRIMARY KEY,
                              user_id INT NOT NULL,
                              available_date DATE NOT NULL,
                              FOREIGN KEY (user_id) REFERENCES User(id),
                              UNIQUE KEY unique_user_date (user_id, available_date)
);

CREATE TABLE Badge (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       user_id INT NOT NULL,
                       badge_type VARCHAR(50) NOT NULL,
                       earned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       FOREIGN KEY (user_id) REFERENCES User(id)
);
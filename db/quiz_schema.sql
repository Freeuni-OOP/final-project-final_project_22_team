-- ============================================================
-- Quiz Website — Database Schema
-- CS108-style Quiz Website (Servlets/JSP + MySQL)
-- ============================================================

CREATE DATABASE IF NOT EXISTS quizdb
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE quizdb;

-- ============================================================
-- 1. USERS  (owner: A)
-- ============================================================
CREATE TABLE users (
    user_id        INT AUTO_INCREMENT PRIMARY KEY,
    username       VARCHAR(50)  NOT NULL UNIQUE,
    password_hash  VARCHAR(255) NOT NULL,
    salt           VARCHAR(64)  NOT NULL,
    is_admin       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_banned      BOOLEAN      NOT NULL DEFAULT FALSE
) ENGINE=InnoDB;

-- session tokens for "remember me" cookies (extension)
CREATE TABLE sessions (
    session_id     VARCHAR(64) PRIMARY KEY,   -- random token, stored in cookie
    user_id        INT NOT NULL,
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at     TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- 2. QUIZZES & QUESTIONS  (owner: B)
-- ============================================================
CREATE TABLE quizzes (
    quiz_id            INT AUTO_INCREMENT PRIMARY KEY,
    title              VARCHAR(150) NOT NULL,
    description        TEXT,
    creator_id         INT NOT NULL,
    randomize_order    BOOLEAN NOT NULL DEFAULT FALSE,
    one_page           BOOLEAN NOT NULL DEFAULT TRUE,   -- TRUE = single page, FALSE = one question per page
    immediate_correction BOOLEAN NOT NULL DEFAULT FALSE, -- only relevant if one_page = FALSE
    practice_allowed   BOOLEAN NOT NULL DEFAULT TRUE,
    category           VARCHAR(50),     -- single category (extension)
    created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_removed         BOOLEAN NOT NULL DEFAULT FALSE,  -- soft delete, for admin "remove quiz"
    FOREIGN KEY (creator_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- tags (many-to-many extension)
CREATE TABLE tags (
    tag_id    INT AUTO_INCREMENT PRIMARY KEY,
    name      VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE quiz_tags (
    quiz_id   INT NOT NULL,
    tag_id    INT NOT NULL,
    PRIMARY KEY (quiz_id, tag_id),
    FOREIGN KEY (quiz_id) REFERENCES quizzes(quiz_id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id)  REFERENCES tags(tag_id)  ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE questions (
    question_id    INT AUTO_INCREMENT PRIMARY KEY,
    quiz_id        INT NOT NULL,
    question_type  ENUM('RESPONSE','FILL_BLANK','MULTIPLE_CHOICE','PICTURE_RESPONSE',
                         'MULTI_ANSWER','MULTI_CHOICE_MULTI_ANSWER') NOT NULL,
    prompt_text    TEXT NOT NULL,        -- question text (with ___ placeholder for fill-in-blank)
    image_url      VARCHAR(500),         -- used only for PICTURE_RESPONSE
    display_order  INT NOT NULL DEFAULT 0,
    answers_ordered BOOLEAN NOT NULL DEFAULT FALSE,  -- for MULTI_ANSWER: order matters or not
    FOREIGN KEY (quiz_id) REFERENCES quizzes(quiz_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- multiple-choice options (also reused for multi-choice-multi-answer)
CREATE TABLE question_options (
    option_id      INT AUTO_INCREMENT PRIMARY KEY,
    question_id    INT NOT NULL,
    option_text    VARCHAR(255) NOT NULL,
    is_correct     BOOLEAN NOT NULL DEFAULT FALSE,
    display_order  INT NOT NULL DEFAULT 0,
    FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- accepted free-text answers (Question-Response, Fill-Blank, Picture-Response, Multi-Answer)
-- one row per legal answer; for multi-answer questions, slot_index identifies which blank
CREATE TABLE question_answers (
    answer_id      INT AUTO_INCREMENT PRIMARY KEY,
    question_id    INT NOT NULL,
    slot_index     INT NOT NULL DEFAULT 0,   -- 0 for single-answer types, 0..N-1 for multi-answer
    answer_text    VARCHAR(255) NOT NULL,
    FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- 3. QUIZ ATTEMPTS / HISTORY / SCORING  (owner: B + C)
-- ============================================================
CREATE TABLE quiz_attempts (
    attempt_id      INT AUTO_INCREMENT PRIMARY KEY,
    quiz_id         INT NOT NULL,
    user_id         INT NOT NULL,
    is_practice     BOOLEAN NOT NULL DEFAULT FALSE,
    score_correct   INT NOT NULL DEFAULT 0,     -- number of correct sub-answers
    score_total     INT NOT NULL DEFAULT 0,     -- total possible sub-answers
    time_seconds    INT NOT NULL DEFAULT 0,     -- completion time
    started_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at    TIMESTAMP NULL,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(quiz_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_quiz_score (quiz_id, score_correct DESC, time_seconds ASC),
    INDEX idx_user_history (user_id, completed_at DESC)
) ENGINE=InnoDB;

-- individual responses within an attempt (for "review your answers" page)
CREATE TABLE attempt_responses (
    response_id     INT AUTO_INCREMENT PRIMARY KEY,
    attempt_id      INT NOT NULL,
    question_id     INT NOT NULL,
    slot_index      INT NOT NULL DEFAULT 0,
    response_text   VARCHAR(255),
    is_correct      BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (attempt_id)  REFERENCES quiz_attempts(attempt_id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(question_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- 4. FRIENDS  (owner: C)
-- ============================================================
-- one row per friendship request/relationship; status tracks lifecycle
CREATE TABLE friendships (
    friendship_id   INT AUTO_INCREMENT PRIMARY KEY,
    requester_id    INT NOT NULL,
    addressee_id    INT NOT NULL,
    status          ENUM('PENDING','ACCEPTED','REJECTED') NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    responded_at    TIMESTAMP NULL,
    FOREIGN KEY (requester_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (addressee_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY uniq_pair (requester_id, addressee_id)
) ENGINE=InnoDB;

-- ============================================================
-- 5. MAIL / MESSAGES  (owner: C)
-- ============================================================
CREATE TABLE messages (
    message_id      INT AUTO_INCREMENT PRIMARY KEY,
    sender_id       INT NOT NULL,
    recipient_id    INT NOT NULL,
    message_type    ENUM('FRIEND_REQUEST','CHALLENGE','NOTE') NOT NULL,
    related_quiz_id INT NULL,           -- used for CHALLENGE messages
    body_text       TEXT,               -- used for NOTE messages
    is_read         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id)    REFERENCES users(user_id)  ON DELETE CASCADE,
    FOREIGN KEY (recipient_id) REFERENCES users(user_id)  ON DELETE CASCADE,
    FOREIGN KEY (related_quiz_id) REFERENCES quizzes(quiz_id) ON DELETE SET NULL,
    INDEX idx_recipient_unread (recipient_id, is_read)
) ENGINE=InnoDB;

-- ============================================================
-- 6. ACHIEVEMENTS  (owner: D)
-- ============================================================
CREATE TABLE achievement_types (
    achievement_type_id INT AUTO_INCREMENT PRIMARY KEY,
    code            VARCHAR(50) NOT NULL UNIQUE,   -- e.g. 'AMATEUR_AUTHOR'
    display_name    VARCHAR(100) NOT NULL,
    description      VARCHAR(255) NOT NULL,
    icon_url         VARCHAR(255)
) ENGINE=InnoDB;

CREATE TABLE user_achievements (
    user_achievement_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id              INT NOT NULL,
    achievement_type_id  INT NOT NULL,
    earned_at            TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (achievement_type_id) REFERENCES achievement_types(achievement_type_id) ON DELETE CASCADE,
    UNIQUE KEY uniq_user_achievement (user_id, achievement_type_id)
) ENGINE=InnoDB;

-- seed the required achievement types
INSERT INTO achievement_types (code, display_name, description) VALUES
('AMATEUR_AUTHOR',     'Amateur Author',     'შექმენი პირველი quiz-ი'),
('PROLIFIC_AUTHOR',    'Prolific Author',    'შექმენი 5 quiz'),
('PRODIGIOUS_AUTHOR',  'Prodigious Author',  'შექმენი 10 quiz'),
('QUIZ_MACHINE',       'Quiz Machine',       'ჩააბარე 10 quiz'),
('I_AM_THE_GREATEST',  'I am the Greatest',  'საუკეთესო score ერთ-ერთ quiz-ში'),
('PRACTICE_MAKES_PERFECT', 'Practice Makes Perfect', 'გაიარე quiz practice mode-ში');

-- ============================================================
-- 7. ADMIN  (owner: D)
-- ============================================================
CREATE TABLE announcements (
    announcement_id  INT AUTO_INCREMENT PRIMARY KEY,
    created_by        INT NOT NULL,
    title              VARCHAR(150) NOT NULL,
    body_text          TEXT NOT NULL,
    created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active          BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- Helpful views (optional, can be used directly in JSP/DAO layer)
-- ============================================================

-- top scorers per quiz: best attempt per user, ranked by score desc, time asc
CREATE VIEW v_quiz_leaderboard AS
SELECT qa.quiz_id, qa.user_id, u.username,
       qa.score_correct, qa.score_total, qa.time_seconds, qa.completed_at
FROM quiz_attempts qa
JOIN users u ON u.user_id = qa.user_id
WHERE qa.is_practice = FALSE AND qa.completed_at IS NOT NULL;

-- quiz-level summary stats
CREATE VIEW v_quiz_stats AS
SELECT quiz_id,
       COUNT(*) AS attempts_count,
       AVG(score_correct / score_total) AS avg_pct_correct,
       AVG(time_seconds) AS avg_time_seconds
FROM quiz_attempts
WHERE is_practice = FALSE AND completed_at IS NOT NULL
GROUP BY quiz_id;

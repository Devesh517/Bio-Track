-- =====================================================================
-- BioTrack - Smart Health Monitoring System
-- Database Schema (MySQL / XAMPP)
-- =====================================================================

    CREATE DATABASE IF NOT EXISTS biotrack;
        USE biotrack;

-- ---------------------------------------------------------------------
-- USERS
-- ---------------------------------------------------------------------
    CREATE TABLE IF NOT EXISTS users (
                                     id              INT AUTO_INCREMENT PRIMARY KEY,
                                     user_id         VARCHAR(20) NOT NULL UNIQUE,       -- e.g. BT1025 (shown in UI)
    full_name       VARCHAR(120) NOT NULL,
    email           VARCHAR(150) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    gender          VARCHAR(20),
    blood_group     VARCHAR(10),
    dob             DATE,
    phone           VARCHAR(20),
    height_cm       DECIMAL(5,2),
    role            VARCHAR(20)  DEFAULT 'PATIENT',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    );

-- ---------------------------------------------------------------------
-- HEALTH RECORDS  (one row per reading / date -> mirrors the dashboard)
-- ---------------------------------------------------------------------
    CREATE TABLE IF NOT EXISTS health_records (
                                              id              INT AUTO_INCREMENT PRIMARY KEY,
                                              user_id         VARCHAR(20) NOT NULL,
    record_date     DATE NOT NULL,
    record_time     TIME DEFAULT NULL,
    heart_rate      INT,                -- bpm
    bp_systolic     INT,                -- mmHg
    bp_diastolic    INT,                -- mmHg
    blood_sugar     DECIMAL(6,2),       -- mg/dL
    spo2            DECIMAL(5,2),       -- %
    weight_kg       DECIMAL(6,2),
    bmi             DECIMAL(5,2),
    notes           VARCHAR(255),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_health_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_health_user_date (user_id, record_date)
    );

-- ---------------------------------------------------------------------
-- DOCUMENTS  (uploaded reports: pdf / image / docx etc.)
-- ---------------------------------------------------------------------
    CREATE TABLE IF NOT EXISTS documents (
                                         id              INT AUTO_INCREMENT PRIMARY KEY,
                                         user_id         VARCHAR(20) NOT NULL,
    file_name       VARCHAR(255) NOT NULL,
    file_type       VARCHAR(20),         -- pdf, png, jpg, docx, xlsx, txt ...
    category        VARCHAR(50),         -- Lab Report, X-Ray, Prescription, Generated Report ...
    file_path       VARCHAR(500) NOT NULL,
    file_size_kb    DECIMAL(10,2),
    uploaded_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_doc_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
    );

-- ---------------------------------------------------------------------
-- CHAT HISTORY  (BioTrack AI assistant conversations)
-- ---------------------------------------------------------------------
    CREATE TABLE IF NOT EXISTS chat_history (
                                            id              INT AUTO_INCREMENT PRIMARY KEY,
                                            user_id         VARCHAR(20) NOT NULL,
    role            VARCHAR(10) NOT NULL,   -- 'user' or 'assistant'
    message         TEXT NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_chat_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
    );

-- ---------------------------------------------------------------------
-- REMINDERS  (matches "Reminders" panel on the dashboard)
-- ---------------------------------------------------------------------
    CREATE TABLE IF NOT EXISTS reminders (
                                         id              INT AUTO_INCREMENT PRIMARY KEY,
                                         user_id         VARCHAR(20) NOT NULL,
    title           VARCHAR(120) NOT NULL,
    reminder_date   DATE,
    reminder_time   TIME,
    is_done         BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_reminder_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
    );

-- ---------------------------------------------------------------------
-- Sample user so the app has something to log in with out of the box
-- password = "password123" (BCrypt hash placeholder - replace via app signup)
-- ---------------------------------------------------------------------
    INSERT INTO users (user_id, full_name, email, password_hash, gender, blood_group, height_cm)
    VALUES ('BT1025', 'Devesh', 'devesh@example.com', '$2a$10$replaceWithRealBcryptHash', 'MALE', 'O_POSITIVE', 175.0)
    ON DUPLICATE KEY UPDATE user_id = user_id;
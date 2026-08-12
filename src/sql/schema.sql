CREATE DATABASE IF NOT EXISTS biotrack;
USE biotrack;
CREATE TABLE users (
                       user_id               INT AUTO_INCREMENT PRIMARY KEY,
                       name                  VARCHAR(100)  NOT NULL,
                       age                   INT           NOT NULL,
                       gender                VARCHAR(20),
                       phone                 VARCHAR(20),
                       email                 VARCHAR(100),
                       blood_group           VARCHAR(5),
                       emergency_contact     VARCHAR(150),
                       profile_picture       LONGBLOB NULL,
                       profile_picture_type  VARCHAR(10) NULL,
                       conditions            VARCHAR(255) NULL,
                       allergies             VARCHAR(255) NULL,
                       password_hash         VARCHAR(255),
                       created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
-- ---------------------------------------------------------------------
-- health_records
-- ---------------------------------------------------------------------
CREATE TABLE health_records (
                                record_id          INT AUTO_INCREMENT PRIMARY KEY,
                                user_id             INT NOT NULL,
                                weight              DOUBLE,
                                height              DOUBLE,
                                bmi                 DOUBLE,
                                temperature         DOUBLE,
                                blood_pressure       INT,
                                bp_systolic          INT NULL,
                                bp_diastolic         INT NULL,
                                heart_rate            INT,
                                sugar_level            INT,
                                spo2                   DOUBLE NULL,
                                cholesterol_total      INT NULL,
                                sugar_context          VARCHAR(10) NOT NULL DEFAULT 'FASTING',
                                notes                  TEXT,
                                recorded_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                KEY idx_health_records_user (user_id),
                                CONSTRAINT fk_health_records_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);
CREATE TABLE documents (
                           doc_id              INT AUTO_INCREMENT PRIMARY KEY,
                           user_id             INT NOT NULL,
                           file_name           VARCHAR(255) NOT NULL,
                           file_type           VARCHAR(10),
                           category             VARCHAR(30) NOT NULL DEFAULT 'Other',
                           linked_record_id     INT NULL,
                           file_size            BIGINT,
                           file_data             LONGBLOB,
                           uploaded_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           KEY idx_documents_user (user_id),
                           CONSTRAINT fk_documents_user FOREIGN KEY (user_id) REFERENCES users(user_id),
                           CONSTRAINT fk_documents_record FOREIGN KEY (linked_record_id)
                               REFERENCES health_records(record_id) ON DELETE SET NULL
);
CREATE TABLE user_settings (
                               user_id               INT PRIMARY KEY,
                               theme                 VARCHAR(10) NOT NULL DEFAULT 'light',
                               recent_records_count  INT NOT NULL DEFAULT 5,
                               updated_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                               CONSTRAINT fk_user_settings_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
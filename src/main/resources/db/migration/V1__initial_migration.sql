CREATE TABLE majors (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(255) NOT NULL UNIQUE,
                        description TEXT,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE study_years (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             name VARCHAR(16) NOT NULL UNIQUE,
                             level INT NOT NULL,
                             description VARCHAR(255)
);
CREATE TABLE instructors (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             email VARCHAR(255) NOT NULL UNIQUE,
                             name VARCHAR(255) NOT NULL,
                             phone VARCHAR(20),
                             specialization VARCHAR(255),
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE students (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          email VARCHAR(255) NOT NULL UNIQUE,
                          name VARCHAR(255) NOT NULL,
                          phone VARCHAR(20),
                          major_id INT NOT NULL,
                          study_year_id INT NOT NULL,
                          is_active BOOLEAN DEFAULT TRUE,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          CONSTRAINT fk_students_major
                              FOREIGN KEY (major_id) REFERENCES majors(id),
                          CONSTRAINT fk_students_study_year
                              FOREIGN KEY (study_year_id) REFERENCES study_years(id)
);
CREATE TABLE subjects (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          major_id INT NOT NULL,
                          study_year_id INT NOT NULL,
                          monthly_price INT NOT NULL,
                          description TEXT,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          CONSTRAINT uq_subjects_name_major
                              UNIQUE (name, major_id),
                          CONSTRAINT fk_subjects_major
                              FOREIGN KEY (major_id) REFERENCES majors(id),
                          CONSTRAINT fk_subjects_study_year
                              FOREIGN KEY (study_year_id) REFERENCES study_years(id),
                          CHECK (monthly_price >= 0)
);
CREATE TABLE subjects_groups (
                                 id INT AUTO_INCREMENT PRIMARY KEY,
                                 name VARCHAR(255) NOT NULL,
                                 subject_id INT NOT NULL,
                                 instructor_id INT,
                                 start_date DATE NOT NULL,
                                 end_date DATE NOT NULL,
                                 max_capacity INT DEFAULT 30,
                                 status ENUM('planned', 'active', 'completed', 'cancelled') DEFAULT 'planned',
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 CONSTRAINT uq_subjects_groups_name_subject_id
                                     UNIQUE (name, subject_id),
                                 CONSTRAINT fk_subjects_groups_subject
                                     FOREIGN KEY (subject_id) REFERENCES subjects(id)
                                         ON UPDATE CASCADE ON DELETE CASCADE,
                                 CONSTRAINT fk_subjects_groups_instructor
                                     FOREIGN KEY (instructor_id) REFERENCES instructors(id)
                                         ON UPDATE CASCADE ON DELETE SET NULL,
                                 CHECK (end_date > start_date),
                                 CHECK (max_capacity > 0)
);
CREATE TABLE registrations (
                               student_id INT NOT NULL,
                               group_id INT NOT NULL,
                               registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               status ENUM('active', 'dropped', 'completed') DEFAULT 'active',
                               PRIMARY KEY (student_id, group_id),
                               CONSTRAINT fk_registrations_student
                                   FOREIGN KEY (student_id) REFERENCES students(id)
                                       ON UPDATE CASCADE ON DELETE CASCADE,
                               CONSTRAINT fk_registrations_group
                                   FOREIGN KEY (group_id) REFERENCES subjects_groups(id)
                                       ON UPDATE CASCADE ON DELETE CASCADE
);
CREATE TABLE sessions (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          subjects_group_id INT NOT NULL,
                          day_of_week ENUM('monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday', 'sunday') NOT NULL,
                          start_time TIME NOT NULL,
                          duration_minutes INT NOT NULL,
                          is_active BOOLEAN DEFAULT TRUE,
                          CONSTRAINT fk_sessions_subjects_group
                              FOREIGN KEY (subjects_group_id) REFERENCES subjects_groups(id)
                                  ON UPDATE CASCADE ON DELETE CASCADE,
                          CHECK (duration_minutes > 0)
);
CREATE TABLE session_instances (
                                   id INT AUTO_INCREMENT PRIMARY KEY,
                                   session_id INT NOT NULL,
                                   session_date DATE NOT NULL,
                                   actual_start_time TIME,
                                   actual_end_time TIME,
                                   status ENUM('scheduled', 'completed', 'cancelled') DEFAULT 'scheduled',
                                   notes TEXT,
                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   CONSTRAINT fk_session_instances_session
                                       FOREIGN KEY (session_id) REFERENCES sessions(id)
                                           ON UPDATE CASCADE ON DELETE CASCADE,
                                   CONSTRAINT uq_session_instances_session_date
                                       UNIQUE (session_id, session_date)
);
CREATE TABLE incidents (
                           id INT AUTO_INCREMENT PRIMARY KEY,
                           session_instance_id INT NOT NULL,
                           incident_type ENUM('cancellation', 'delay', 'room_change', 'instructor_change', 'other') NOT NULL,
                           description TEXT NOT NULL,
                           reported_by VARCHAR(255),
                           reported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           resolved_at TIMESTAMP NULL,
                           CONSTRAINT fk_incidents_session_instance
                               FOREIGN KEY (session_instance_id) REFERENCES session_instances(id)
                                   ON UPDATE CASCADE ON DELETE CASCADE
);
CREATE TABLE attendance (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            student_id INT NOT NULL,
                            session_instance_id INT NOT NULL,
                            status ENUM('present', 'absent', 'late', 'excused') NOT NULL,
                            notes TEXT,
                            marked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            CONSTRAINT fk_attendance_student
                                FOREIGN KEY (student_id) REFERENCES students(id)
                                    ON UPDATE CASCADE ON DELETE CASCADE,
                            CONSTRAINT fk_attendance_session_instance
                                FOREIGN KEY (session_instance_id) REFERENCES session_instances(id)
                                    ON UPDATE CASCADE ON DELETE CASCADE,
                            CONSTRAINT uq_attendance_student_session
                                UNIQUE (student_id, session_instance_id)
);
CREATE INDEX idx_students_major_year ON students(major_id, study_year_id);
CREATE INDEX idx_subjects_major_year ON subjects(major_id, study_year_id);
CREATE INDEX idx_sessions_group_day ON sessions(subjects_group_id, day_of_week);
CREATE INDEX idx_session_instances_date ON session_instances(session_date);
CREATE INDEX idx_attendance_session ON attendance(session_instance_id);
CREATE INDEX idx_registrations_status ON registrations(status);
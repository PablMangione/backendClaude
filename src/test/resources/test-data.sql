-- ===================================================================
-- DATOS BASE PARA TESTING - H2 Database
-- ===================================================================
-- IMPORTANTE: H2 con MODE=MYSQL pero hay diferencias en tipos y formato
-- Usamos el formato más compatible posible
-- -------------------------------------------------------------------
-- MAJORS (Carreras)
-- -------------------------------------------------------------------
INSERT INTO majors (id, name, description, created_at) VALUES
(1, 'Ingeniería Informática', 'Carrera enfocada en desarrollo de software y sistemas', CURRENT_TIMESTAMP),
(2, 'Medicina', 'Carrera de ciencias de la salud', CURRENT_TIMESTAMP),
(3, 'Derecho', 'Carrera de ciencias jurídicas', CURRENT_TIMESTAMP),
(4, 'Ingeniería Industrial', 'Carrera de optimización de procesos industriales', CURRENT_TIMESTAMP),
(5, 'Psicología', 'Carrera de ciencias del comportamiento humano', CURRENT_TIMESTAMP);
-- -------------------------------------------------------------------
-- STUDY_YEARS (Años de estudio)
-- -------------------------------------------------------------------
INSERT INTO study_years (id, name, level, description) VALUES
(1, 'Primer Año', 1, 'Primer año de estudios universitarios'),
(2, 'Segundo Año', 2, 'Segundo año de estudios universitarios'),
(3, 'Tercer Año', 3, 'Tercer año de estudios universitarios'),
(4, 'Cuarto Año', 4, 'Cuarto año de estudios universitarios'),
(5, 'Quinto Año', 5, 'Quinto año de estudios universitarios');

-- -------------------------------------------------------------------
-- INSTRUCTORS (Profesores)
-- -------------------------------------------------------------------
INSERT INTO instructors (id, email, name, phone, specialization, created_at) VALUES
(1, 'prof.garcia@academia.com', 'Dr. García López', '666111222', 'Programación y Algoritmos', CURRENT_TIMESTAMP),
(2, 'prof.martinez@academia.com', 'Dra. Martínez Ruiz', '666333444', 'Bases de Datos', CURRENT_TIMESTAMP),
(3, 'prof.rodriguez@academia.com', 'Dr. Rodríguez Pérez', '666555666', 'Matemáticas', CURRENT_TIMESTAMP),
(4, 'prof.sanchez@academia.com', 'Dra. Sánchez Moreno', '666777888', 'Anatomía', CURRENT_TIMESTAMP),
(5, 'prof.lopez@academia.com', 'Dr. López Fernández', '666999000', 'Derecho Civil', CURRENT_TIMESTAMP);

-- -------------------------------------------------------------------
-- STUDENTS (Estudiantes)
-- IMPORTANTE: H2 maneja BOOLEAN como TRUE/FALSE (no 1/0 como MySQL)
-- -------------------------------------------------------------------
INSERT INTO students (id, email, name, phone, major_id, study_year_id, is_active, created_at) VALUES
-- Estudiantes de Informática (3 activos, 1 inactivo)
(1, 'juan.perez@student.com', 'Juan Pérez García', '600111222', 1, 1, TRUE, CURRENT_TIMESTAMP),
(2, 'maria.garcia@student.com', 'María García López', '600333444', 1, 2, TRUE, CURRENT_TIMESTAMP),
(3, 'carlos.lopez@student.com', 'Carlos López Martín', '600555666', 1, 1, TRUE, CURRENT_TIMESTAMP),
(4, 'inactive.student@student.com', 'Estudiante Inactivo', '600777888', 1, 1, FALSE, CURRENT_TIMESTAMP),

-- Estudiantes de Medicina (2 activos)
(5, 'ana.martinez@student.com', 'Ana Martínez Ruiz', '600999000', 2, 1, TRUE, CURRENT_TIMESTAMP),
(6, 'luis.sanchez@student.com', 'Luis Sánchez Torres', '601111222', 2, 2, TRUE, CURRENT_TIMESTAMP),

-- Estudiantes de Derecho (1 activo)
(7, 'sofia.rodriguez@student.com', 'Sofía Rodríguez Vega', '601333444', 3, 1, TRUE, CURRENT_TIMESTAMP),

-- Estudiantes de Ing. Industrial (4 activos) - Para hacer esta carrera popular
(8, 'miguel.torres@student.com', 'Miguel Torres Ruiz', '601555666', 4, 1, TRUE, CURRENT_TIMESTAMP),
(9, 'laura.jimenez@student.com', 'Laura Jiménez López', '601777888', 4, 2, TRUE, CURRENT_TIMESTAMP),
(10, 'david.moreno@student.com', 'David Moreno García', '601999000', 4, 3, TRUE, CURRENT_TIMESTAMP),
(11, 'elena.vega@student.com', 'Elena Vega Martín', '602111222', 4, 1, TRUE, CURRENT_TIMESTAMP);

-- Psicología sin estudiantes (para testing de carreras vacías)

-- -------------------------------------------------------------------
-- SUBJECTS (Materias)
-- -------------------------------------------------------------------
INSERT INTO subjects (id, name, major_id, study_year_id, monthly_price, description, created_at) VALUES
-- Informática
(1, 'Programación I', 1, 1, 150, 'Introducción a la programación', CURRENT_TIMESTAMP),
(2, 'Estructuras de Datos', 1, 2, 180, 'Algoritmos y estructuras de datos', CURRENT_TIMESTAMP),
(3, 'Bases de Datos', 1, 3, 200, 'Diseño y gestión de bases de datos', CURRENT_TIMESTAMP),

-- Medicina
(4, 'Anatomía Humana', 2, 1, 220, 'Estudio del cuerpo humano', CURRENT_TIMESTAMP),
(5, 'Fisiología', 2, 2, 250, 'Funcionamiento del organismo', CURRENT_TIMESTAMP),

-- Derecho
(6, 'Derecho Civil I', 3, 1, 160, 'Fundamentos del derecho civil', CURRENT_TIMESTAMP),
(7, 'Derecho Penal', 3, 2, 170, 'Derecho penal y procesal', CURRENT_TIMESTAMP),

-- Industrial
(8, 'Matemáticas I', 4, 1, 140, 'Cálculo diferencial e integral', CURRENT_TIMESTAMP),
(9, 'Estadística', 4, 2, 160, 'Estadística aplicada', CURRENT_TIMESTAMP);

-- -------------------------------------------------------------------
-- SUBJECTS_GROUPS (Grupos de materias)
-- IMPORTANTE: H2 requiere formato específico para ENUM y DATE
-- -------------------------------------------------------------------
INSERT INTO subjects_groups (id, name, subject_id, instructor_id, start_date, end_date, max_capacity, status, created_at) VALUES
(1, 'Grupo A', 1, 1, '2024-09-01', '2024-12-15', 30, 'ACTIVE', CURRENT_TIMESTAMP),
(2, 'Grupo B', 1, 1, '2024-09-01', '2024-12-15', 25, 'ACTIVE', CURRENT_TIMESTAMP),
(3, 'Grupo A', 2, 2, '2024-09-01', '2024-12-15', 30, 'ACTIVE', CURRENT_TIMESTAMP),
(4, 'Grupo A', 4, 4, '2024-09-01', '2024-12-15', 20, 'ACTIVE', CURRENT_TIMESTAMP),
(5, 'Grupo A', 6, 5, '2024-09-01', '2024-12-15', 35, 'PLANNED', CURRENT_TIMESTAMP);

-- -------------------------------------------------------------------
-- REGISTRATIONS (Inscripciones)
-- IMPORTANTE: H2 maneja ENUM diferente - usar STRING values
-- -------------------------------------------------------------------
INSERT INTO registrations (student_id, group_id, registration_date, status) VALUES
-- Estudiantes de Informática en diferentes grupos
(1, 1, CURRENT_TIMESTAMP, 'ACTIVE'),
(2, 1, CURRENT_TIMESTAMP, 'ACTIVE'),
(3, 2, CURRENT_TIMESTAMP, 'ACTIVE'),
-- Estudiante inactivo también tiene registración (para testing)
(4, 1, CURRENT_TIMESTAMP, 'ACTIVE'),

-- Estudiantes de Medicina
(5, 4, CURRENT_TIMESTAMP, 'ACTIVE'),
(6, 4, CURRENT_TIMESTAMP, 'ACTIVE'),

-- Estudiante de Derecho
(7, 5, CURRENT_TIMESTAMP, 'ACTIVE');

-- -------------------------------------------------------------------
-- SESSIONS (Sesiones programadas)
-- -------------------------------------------------------------------
INSERT INTO sessions (id, subjects_group_id, day_of_week, start_time, duration_minutes, is_active) VALUES
(1, 1, 'MONDAY', '09:00:00', 120, TRUE),
(2, 1, 'WEDNESDAY', '09:00:00', 120, TRUE),
(3, 2, 'TUESDAY', '14:00:00', 120, TRUE),
(4, 3, 'FRIDAY', '10:00:00', 150, TRUE),
(5, 4, 'THURSDAY', '08:00:00', 180, TRUE);

-- Nota: Datos adicionales (session_instances, attendance, incidents)
-- se pueden agregar según necesidades específicas de cada consulta
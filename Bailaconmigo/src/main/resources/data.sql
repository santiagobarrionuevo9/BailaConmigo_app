-- Insertar usuarios
INSERT INTO users ( full_name, email, password, gender, birthdate, city, role, subscription_type, subscription_expiration)
VALUES ( 'Carlos Rodriguez', 'carlos@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Masculino', '1990-05-15', 'Buenos Aires', 'BAILARIN', 'PRO', '2025-12-31');

INSERT INTO users ( full_name, email, password, gender, birthdate, city, role, subscription_type, subscription_expiration)
VALUES ( 'Maria Gomez', 'maria@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Femenino', '1992-08-22', 'Córdoba', 'BAILARIN', 'BASICO', '2025-06-30');

INSERT INTO users ( full_name, email, password, gender, birthdate, city, role, subscription_type, subscription_expiration)
VALUES ( 'Juan Perez', 'juan@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Masculino', '1985-12-10', 'Rosario', 'ORGANIZADOR', 'PRO', '2025-12-15');

INSERT INTO users ( full_name, email, password, gender, birthdate, city, role, subscription_type, subscription_expiration)
VALUES ( 'Laura Martinez', 'laura@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Femenino', '1988-03-25', 'Mendoza', 'BAILARIN', 'PRO', '2025-11-20');

INSERT INTO users ( full_name, email, password, gender, birthdate, city, role, subscription_type, subscription_expiration)
VALUES ( 'Roberto Sanchez', 'roberto@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Masculino', '1995-07-08', 'Buenos Aires', 'BAILARIN', 'SIN_SUSCRIPCION', NULL);

INSERT INTO users ( full_name, email, password, gender, birthdate, city, role, subscription_type, subscription_expiration)
VALUES ( 'Ana Lopez', 'ana@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Femenino', '1993-02-14', 'La Plata', 'BAILARIN', 'BASICO', '2025-08-15');

INSERT INTO users ( full_name, email, password, gender, birthdate, city, role, subscription_type, subscription_expiration)
VALUES ( 'Santiago Fernández', 'santiago@example.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Masculino', '1987-11-30', 'Córdoba', 'ORGANIZADOR', 'PRO', '2026-01-31');

INSERT INTO users ( full_name, email, password, gender, birthdate, city, role, subscription_type, subscription_expiration)
VALUES ( 'Admin User', 'admin@bailaconmigo.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'No especificado', '1980-01-01', 'Buenos Aires', 'ADMIN', 'PRO', '2030-12-31');

-- Insertar perfiles de bailarines
INSERT INTO dancer_profiles ( user_id, full_name, age, city, level, about_me, availability)
VALUES ( 1, 'Carlos Rodriguez', 33, 'Buenos Aires', 'AVANZADO', 'Bailarín de tango con más de 10 años de experiencia. Me encanta compartir mi pasión por el baile y conocer nuevos compañeros.', 'Lunes a Viernes: 18:00 - 22:00, Fines de semana: disponible todo el día');

INSERT INTO dancer_profiles ( user_id, full_name, age, city, level, about_me, availability)
VALUES ( 2, 'Maria Gomez', 31, 'Córdoba', 'PROFESIONAL', 'Profesora de salsa y bachata. Participo en competencias nacionales e internacionales. Busco parejas de baile para practicar nuevas coreografías.', 'Martes y Jueves: 17:00 - 21:00, Sábados: 10:00 - 14:00');

INSERT INTO dancer_profiles ( user_id, full_name, age, city, level, about_me, availability)
VALUES ( 4, 'Laura Martinez', 37, 'Mendoza', 'INTERMEDIO', 'Apasionada por el baile contemporáneo. Practico desde hace 5 años y siempre estoy buscando mejorar mi técnica con nuevos compañeros.', 'Lunes, Miércoles y Viernes: 19:00 - 21:00');

INSERT INTO dancer_profiles ( user_id, full_name, age, city, level, about_me, availability)
VALUES ( 5, 'Roberto Sanchez', 28, 'Buenos Aires', 'PRINCIPIANTE', 'Nuevo en el mundo del baile, con muchas ganas de aprender salsa y bachata. Busco compañera paciente para practicar pasos básicos.', 'Disponible fines de semana y algunos días entre semana a coordinar');

INSERT INTO dancer_profiles ( user_id, full_name, age, city, level, about_me, availability)
VALUES ( 6, 'Ana Lopez', 30, 'La Plata', 'AVANZADO', 'Bailarina de kizomba y bachata sensual. He participado en varios shows y me encanta enseñar a quienes recién comienzan.', 'Lunes a Jueves: 18:00 - 22:00, Domingos: 15:00 - 19:00');

-- Insertar estilos de baile para cada bailarín
INSERT INTO dancer_styles (dancer_id, style) VALUES (1, 'TANGO');
INSERT INTO dancer_styles (dancer_id, style) VALUES (1, 'BACHATA');

INSERT INTO dancer_styles (dancer_id, style) VALUES (2, 'SALSA');
INSERT INTO dancer_styles (dancer_id, style) VALUES (2, 'BACHATA');
INSERT INTO dancer_styles (dancer_id, style) VALUES (2, 'KIZOMBA');

INSERT INTO dancer_styles (dancer_id, style) VALUES (3, 'CONTEMPORANEO');
INSERT INTO dancer_styles (dancer_id, style) VALUES (3, 'JAZZ');

INSERT INTO dancer_styles (dancer_id, style) VALUES (4, 'SALSA');
INSERT INTO dancer_styles (dancer_id, style) VALUES (4, 'BACHATA');

INSERT INTO dancer_styles (dancer_id, style) VALUES (5, 'KIZOMBA');
INSERT INTO dancer_styles (dancer_id, style) VALUES (5, 'BACHATA');

-- Insertar elementos multimedia
INSERT INTO media ( url, profile_id) VALUES ( '/uploads/dancers/carlos_tango1.jpg', 1);
INSERT INTO media ( url, profile_id) VALUES ( '/uploads/dancers/carlos_tango2.jpg', 1);
INSERT INTO media ( url, profile_id) VALUES ( '/uploads/dancers/maria_salsa1.jpg', 2);
INSERT INTO media ( url, profile_id) VALUES ( '/uploads/dancers/maria_bachata1.mp4', 2);
INSERT INTO media ( url, profile_id) VALUES ( '/uploads/dancers/laura_contemporaneo.jpg', 3);
INSERT INTO media ( url, profile_id) VALUES ( '/uploads/dancers/roberto_practica.jpg', 4);
INSERT INTO media ( url, profile_id) VALUES ( '/uploads/dancers/ana_kizomba1.jpg', 5);
INSERT INTO media ( url, profile_id) VALUES ( '/uploads/dancers/ana_bachata1.mp4', 5);

-- Insertar calificaciones
INSERT INTO ratings ( rater_id, dancer_profile_id, stars, comment)
VALUES ( 2, 1, 5, 'Excelente bailarín de tango, muy paciente para enseñar los pasos.');

INSERT INTO ratings ( rater_id, dancer_profile_id, stars, comment)
VALUES ( 4, 1, 4, 'Muy buen compañero de baile, explica bien los movimientos.');

INSERT INTO ratings ( rater_id, dancer_profile_id, stars, comment)
VALUES ( 1, 2, 5, 'María es una profesional, aprendí mucho practicando con ella.');

INSERT INTO ratings ( rater_id, dancer_profile_id, stars, comment)
VALUES ( 5, 2, 5, 'Increíble técnica en salsa, muy recomendable.');

INSERT INTO ratings ( rater_id, dancer_profile_id, stars, comment)
VALUES ( 1, 3, 4, 'Laura tiene un estilo muy personal y fluido en contemporáneo.');

INSERT INTO ratings ( rater_id, dancer_profile_id, stars, comment)
VALUES ( 2, 5, 5, 'Ana es una excelente maestra de kizomba, muy recomendada.');

INSERT INTO ratings ( rater_id, dancer_profile_id, stars, comment)
VALUES ( 6, 1, 4, 'Carlos tiene mucha experiencia en tango, aprendí mucho en una sesión.');
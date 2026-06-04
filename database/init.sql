use prode_k2_mundial;
-- ============================================
-- MUNDIAL FIFA 2026 - DATOS OFICIALES + SIMULADOS
-- ============================================

-- Limpiar tablas existentes
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE predictions;
TRUNCATE TABLE matches;
TRUNCATE TABLE teams;
TRUNCATE TABLE scoring_config;
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- EQUIPOS (48 selecciones)
-- ============================================

INSERT INTO teams (name, code, flag_url, group_name, confederation) VALUES
-- GRUPO A
('México', 'MEX', '/flags/mex.svg', 'A', 'CONCACAF'),
('Sudáfrica', 'RSA', '/flags/rsa.svg', 'A', 'CAF'),
('Corea del Sur', 'KOR', '/flags/kor.svg', 'A', 'AFC'),
('Chequia', 'CZE', '/flags/cze.svg', 'A', 'UEFA'),

-- GRUPO B
('Canadá', 'CAN', '/flags/can.svg', 'B', 'CONCACAF'),
('Bosnia y Herzegovina', 'BIH', '/flags/bih.svg', 'B', 'UEFA'),
('Catar', 'QAT', '/flags/qat.svg', 'B', 'AFC'),
('Suiza', 'SUI', '/flags/sui.svg', 'B', 'UEFA'),

-- GRUPO C
('Brasil', 'BRA', '/flags/bra.svg', 'C', 'CONMEBOL'),
('Marruecos', 'MAR', '/flags/mar.svg', 'C', 'CAF'),
('Haití', 'HAI', '/flags/hai.svg', 'C', 'CONCACAF'),
('Escocia', 'SCO', '/flags/sco.svg', 'C', 'UEFA'),

-- GRUPO D
('Estados Unidos', 'USA', '/flags/usa.svg', 'D', 'CONCACAF'),
('Paraguay', 'PAR', '/flags/par.svg', 'D', 'CONMEBOL'),
('Australia', 'AUS', '/flags/aus.svg', 'D', 'AFC'),
('Turquía', 'TUR', '/flags/tur.svg', 'D', 'UEFA'),

-- GRUPO E
('Alemania', 'GER', '/flags/ger.svg', 'E', 'UEFA'),
('Curazao', 'CUW', '/flags/cuw.svg', 'E', 'CONCACAF'),
('Costa de Marfil', 'CIV', '/flags/civ.svg', 'E', 'CAF'),
('Ecuador', 'ECU', '/flags/ecu.svg', 'E', 'CONMEBOL'),

-- GRUPO F
('Países Bajos', 'NED', '/flags/ned.svg', 'F', 'UEFA'),
('Japón', 'JPN', '/flags/jpn.svg', 'F', 'AFC'),
('Suecia', 'SWE', '/flags/swe.svg', 'F', 'UEFA'),
('Túnez', 'TUN', '/flags/tun.svg', 'F', 'CAF'),

-- GRUPO G
('Bélgica', 'BEL', '/flags/bel.svg', 'G', 'UEFA'),
('Egipto', 'EGY', '/flags/egy.svg', 'G', 'CAF'),
('Irán', 'IRN', '/flags/irn.svg', 'G', 'AFC'),
('Nueva Zelanda', 'NZL', '/flags/nzl.svg', 'G', 'OFC'),

-- GRUPO H
('España', 'ESP', '/flags/esp.svg', 'H', 'UEFA'),
('Cabo Verde', 'CPV', '/flags/cpv.svg', 'H', 'CAF'),
('Arabia Saudita', 'KSA', '/flags/ksa.svg', 'H', 'AFC'),
('Uruguay', 'URU', '/flags/uru.svg', 'H', 'CONMEBOL'),

-- GRUPO I
('Francia', 'FRA', '/flags/fra.svg', 'I', 'UEFA'),
('Senegal', 'SEN', '/flags/sen.svg', 'I', 'CAF'),
('Irak', 'IRQ', '/flags/irq.svg', 'I', 'AFC'),
('Noruega', 'NOR', '/flags/nor.svg', 'I', 'UEFA'),

-- GRUPO J
('Argentina', 'ARG', '/flags/arg.svg', 'J', 'CONMEBOL'),
('Argelia', 'ALG', '/flags/alg.svg', 'J', 'CAF'),
('Austria', 'AUT', '/flags/aut.svg', 'J', 'UEFA'),
('Jordania', 'JOR', '/flags/jor.svg', 'J', 'AFC'),

-- GRUPO K
('Portugal', 'POR', '/flags/por.svg', 'K', 'UEFA'),
('RD Congo', 'COD', '/flags/cod.svg', 'K', 'CAF'),
('Uzbekistán', 'UZB', '/flags/uzb.svg', 'K', 'AFC'),
('Colombia', 'COL', '/flags/col.svg', 'K', 'CONMEBOL'),

-- GRUPO L
('Inglaterra', 'ENG', '/flags/eng.svg', 'L', 'UEFA'),
('Croacia', 'CRO', '/flags/cro.svg', 'L', 'UEFA'),
('Ghana', 'GHA', '/flags/gha.svg', 'L', 'CAF'),
('Panamá', 'PAN', '/flags/pan.svg', 'L', 'CONCACAF');

-- ============================================
-- CONFIGURACIÓN DE PUNTUACIÓN
-- ============================================

INSERT INTO scoring_config (
    points_correct_winner, 
    points_correct_goals_one_team, 
    points_exact_result, 
    exact_replaces_all, 
    lock_hours_before_match, 
    bonus_round_of16, 
    bonus_quarter_finals, 
    bonus_semi_finals, 
    bonus_final, 
    active
) VALUES (3, 2, 4, true, 1, 0, 1, 2, 3, true);

-- ============================================
-- PARTIDOS FASE DE GRUPOS
-- ============================================

-- FECHA 1 (11-14 de junio 2026)
INSERT INTO matches (home_team_id, away_team_id, match_date_time, status, phase, group_name, match_number, stadium, city, country) VALUES

-- GRUPO A - Fecha 1
((SELECT id FROM teams WHERE code = 'MEX'), (SELECT id FROM teams WHERE code = 'RSA'), '2026-06-11 18:00:00', 'SCHEDULED', 'GROUP_STAGE', 'A', 1, 'Estadio Azteca', 'Ciudad de México', 'México'),
((SELECT id FROM teams WHERE code = 'KOR'), (SELECT id FROM teams WHERE code = 'CZE'), '2026-06-11 21:00:00', 'SCHEDULED', 'GROUP_STAGE', 'A', 2, 'Estadio BBVA', 'Monterrey', 'México'),

-- GRUPO B - Fecha 1
((SELECT id FROM teams WHERE code = 'CAN'), (SELECT id FROM teams WHERE code = 'BIH'), '2026-06-12 12:00:00', 'SCHEDULED', 'GROUP_STAGE', 'B', 3, 'BMO Field', 'Toronto', 'Canadá'),
((SELECT id FROM teams WHERE code = 'QAT'), (SELECT id FROM teams WHERE code = 'SUI'), '2026-06-12 15:00:00', 'SCHEDULED', 'GROUP_STAGE', 'B', 4, 'BC Place', 'Vancouver', 'Canadá'),

-- GRUPO C - Fecha 1
((SELECT id FROM teams WHERE code = 'BRA'), (SELECT id FROM teams WHERE code = 'HAI'), '2026-06-12 18:00:00', 'SCHEDULED', 'GROUP_STAGE', 'C', 5, 'SoFi Stadium', 'Los Ángeles', 'USA'),
((SELECT id FROM teams WHERE code = 'MAR'), (SELECT id FROM teams WHERE code = 'SCO'), '2026-06-12 21:00:00', 'SCHEDULED', 'GROUP_STAGE', 'C', 6, 'Hard Rock Stadium', 'Miami', 'USA'),

-- GRUPO D - Fecha 1
((SELECT id FROM teams WHERE code = 'USA'), (SELECT id FROM teams WHERE code = 'PAR'), '2026-06-13 18:00:00', 'SCHEDULED', 'GROUP_STAGE', 'D', 7, 'MetLife Stadium', 'East Rutherford', 'USA'),
((SELECT id FROM teams WHERE code = 'AUS'), (SELECT id FROM teams WHERE code = 'TUR'), '2026-06-13 15:00:00', 'SCHEDULED', 'GROUP_STAGE', 'D', 8, 'Lumen Field', 'Seattle', 'USA'),

-- GRUPO E - Fecha 1
((SELECT id FROM teams WHERE code = 'GER'), (SELECT id FROM teams WHERE code = 'CUW'), '2026-06-13 12:00:00', 'SCHEDULED', 'GROUP_STAGE', 'E', 9, 'Mercedes-Benz Stadium', 'Atlanta', 'USA'),
((SELECT id FROM teams WHERE code = 'CIV'), (SELECT id FROM teams WHERE code = 'ECU'), '2026-06-13 21:00:00', 'SCHEDULED', 'GROUP_STAGE', 'E', 10, 'NRG Stadium', 'Houston', 'USA'),

-- GRUPO F - Fecha 1
((SELECT id FROM teams WHERE code = 'NED'), (SELECT id FROM teams WHERE code = 'SWE'), '2026-06-14 12:00:00', 'SCHEDULED', 'GROUP_STAGE', 'F', 11, 'Gillette Stadium', 'Foxborough', 'USA'),
((SELECT id FROM teams WHERE code = 'JPN'), (SELECT id FROM teams WHERE code = 'TUN'), '2026-06-14 15:00:00', 'SCHEDULED', 'GROUP_STAGE', 'F', 12, 'Levi''s Stadium', 'Santa Clara', 'USA'),

-- GRUPO G - Fecha 1
((SELECT id FROM teams WHERE code = 'BEL'), (SELECT id FROM teams WHERE code = 'NZL'), '2026-06-14 18:00:00', 'SCHEDULED', 'GROUP_STAGE', 'G', 13, 'Lincoln Financial Field', 'Filadelfia', 'USA'),
((SELECT id FROM teams WHERE code = 'EGY'), (SELECT id FROM teams WHERE code = 'IRN'), '2026-06-14 21:00:00', 'SCHEDULED', 'GROUP_STAGE', 'G', 14, 'AT&T Stadium', 'Arlington', 'USA'),

-- GRUPO H - Fecha 1
((SELECT id FROM teams WHERE code = 'ESP'), (SELECT id FROM teams WHERE code = 'CPV'), '2026-06-15 12:00:00', 'SCHEDULED', 'GROUP_STAGE', 'H', 15, 'Estadio Akron', 'Guadalajara', 'México'),
((SELECT id FROM teams WHERE code = 'KSA'), (SELECT id FROM teams WHERE code = 'URU'), '2026-06-15 15:00:00', 'SCHEDULED', 'GROUP_STAGE', 'H', 16, 'Estadio Azteca', 'Ciudad de México', 'México'),

-- GRUPO I - Fecha 1
((SELECT id FROM teams WHERE code = 'FRA'), (SELECT id FROM teams WHERE code = 'IRQ'), '2026-06-15 18:00:00', 'SCHEDULED', 'GROUP_STAGE', 'I', 17, 'MetLife Stadium', 'East Rutherford', 'USA'),
((SELECT id FROM teams WHERE code = 'SEN'), (SELECT id FROM teams WHERE code = 'NOR'), '2026-06-15 21:00:00', 'SCHEDULED', 'GROUP_STAGE', 'I', 18, 'Hard Rock Stadium', 'Miami', 'USA'),

-- GRUPO J - Fecha 1
((SELECT id FROM teams WHERE code = 'ARG'), (SELECT id FROM teams WHERE code = 'JOR'), '2026-06-16 18:00:00', 'SCHEDULED', 'GROUP_STAGE', 'J', 19, 'SoFi Stadium', 'Los Ángeles', 'USA'),
((SELECT id FROM teams WHERE code = 'ALG'), (SELECT id FROM teams WHERE code = 'AUT'), '2026-06-16 15:00:00', 'SCHEDULED', 'GROUP_STAGE', 'J', 20, 'Arrowhead Stadium', 'Kansas City', 'USA'),

-- GRUPO K - Fecha 1
((SELECT id FROM teams WHERE code = 'POR'), (SELECT id FROM teams WHERE code = 'COD'), '2026-06-16 12:00:00', 'SCHEDULED', 'GROUP_STAGE', 'K', 21, 'NRG Stadium', 'Houston', 'USA'),
((SELECT id FROM teams WHERE code = 'COL'), (SELECT id FROM teams WHERE code = 'UZB'), '2026-06-16 21:00:00', 'SCHEDULED', 'GROUP_STAGE', 'K', 22, 'Mercedes-Benz Stadium', 'Atlanta', 'USA'),

-- GRUPO L - Fecha 1
((SELECT id FROM teams WHERE code = 'ENG'), (SELECT id FROM teams WHERE code = 'PAN'), '2026-06-17 18:00:00', 'SCHEDULED', 'GROUP_STAGE', 'L', 23, 'MetLife Stadium', 'East Rutherford', 'USA'),
((SELECT id FROM teams WHERE code = 'CRO'), (SELECT id FROM teams WHERE code = 'GHA'), '2026-06-17 15:00:00', 'SCHEDULED', 'GROUP_STAGE', 'L', 24, 'AT&T Stadium', 'Arlington', 'USA');



-- FECHA 2 (18-21 de junio 2026)
INSERT INTO matches (home_team_id, away_team_id, match_date_time, status, phase, group_name, match_number, stadium, city, country) VALUES

-- GRUPO A - Fecha 2
((SELECT id FROM teams WHERE code = 'MEX'), (SELECT id FROM teams WHERE code = 'KOR'), '2026-06-18 18:00:00', 'SCHEDULED', 'GROUP_STAGE', 'A', 25, 'Estadio Azteca', 'Ciudad de México', 'México'),
((SELECT id FROM teams WHERE code = 'RSA'), (SELECT id FROM teams WHERE code = 'CZE'), '2026-06-18 15:00:00', 'SCHEDULED', 'GROUP_STAGE', 'A', 26, 'Estadio BBVA', 'Monterrey', 'México'),

-- GRUPO B - Fecha 2
((SELECT id FROM teams WHERE code = 'CAN'), (SELECT id FROM teams WHERE code = 'QAT'), '2026-06-18 12:00:00', 'SCHEDULED', 'GROUP_STAGE', 'B', 27, 'BMO Field', 'Toronto', 'Canadá'),
((SELECT id FROM teams WHERE code = 'BIH'), (SELECT id FROM teams WHERE code = 'SUI'), '2026-06-18 21:00:00', 'SCHEDULED', 'GROUP_STAGE', 'B', 28, 'BC Place', 'Vancouver', 'Canadá'),

-- GRUPO C - Fecha 2
((SELECT id FROM teams WHERE code = 'BRA'), (SELECT id FROM teams WHERE code = 'MAR'), '2026-06-19 18:00:00', 'SCHEDULED', 'GROUP_STAGE', 'C', 29, 'SoFi Stadium', 'Los Ángeles', 'USA'),
((SELECT id FROM teams WHERE code = 'HAI'), (SELECT id FROM teams WHERE code = 'SCO'), '2026-06-19 15:00:00', 'SCHEDULED', 'GROUP_STAGE', 'C', 30, 'Hard Rock Stadium', 'Miami', 'USA'),

-- GRUPO D - Fecha 2
((SELECT id FROM teams WHERE code = 'USA'), (SELECT id FROM teams WHERE code = 'AUS'), '2026-06-19 21:00:00', 'SCHEDULED', 'GROUP_STAGE', 'D', 31, 'MetLife Stadium', 'East Rutherford', 'USA'),
((SELECT id FROM teams WHERE code = 'PAR'), (SELECT id FROM teams WHERE code = 'TUR'), '2026-06-19 12:00:00', 'SCHEDULED', 'GROUP_STAGE', 'D', 32, 'Lumen Field', 'Seattle', 'USA'),

-- GRUPO E - Fecha 2
((SELECT id FROM teams WHERE code = 'GER'), (SELECT id FROM teams WHERE code = 'CIV'), '2026-06-20 18:00:00', 'SCHEDULED', 'GROUP_STAGE', 'E', 33, 'Mercedes-Benz Stadium', 'Atlanta', 'USA'),
((SELECT id FROM teams WHERE code = 'CUW'), (SELECT id FROM teams WHERE code = 'ECU'), '2026-06-20 15:00:00', 'SCHEDULED', 'GROUP_STAGE', 'E', 34, 'NRG Stadium', 'Houston', 'USA'),

-- GRUPO F - Fecha 2
((SELECT id FROM teams WHERE code = 'NED'), (SELECT id FROM teams WHERE code = 'JPN'), '2026-06-20 12:00:00', 'SCHEDULED', 'GROUP_STAGE', 'F', 35, 'Gillette Stadium', 'Foxborough', 'USA'),
((SELECT id FROM teams WHERE code = 'SWE'), (SELECT id FROM teams WHERE code = 'TUN'), '2026-06-20 21:00:00', 'SCHEDULED', 'GROUP_STAGE', 'F', 36, 'Levi''s Stadium', 'Santa Clara', 'USA'),

-- GRUPO G - Fecha 2
((SELECT id FROM teams WHERE code = 'BEL'), (SELECT id FROM teams WHERE code = 'EGY'), '2026-06-21 18:00:00', 'SCHEDULED', 'GROUP_STAGE', 'G', 37, 'Lincoln Financial Field', 'Filadelfia', 'USA'),
((SELECT id FROM teams WHERE code = 'IRN'), (SELECT id FROM teams WHERE code = 'NZL'), '2026-06-21 15:00:00', 'SCHEDULED', 'GROUP_STAGE', 'G', 38, 'AT&T Stadium', 'Arlington', 'USA'),

-- GRUPO H - Fecha 2
((SELECT id FROM teams WHERE code = 'ESP'), (SELECT id FROM teams WHERE code = 'KSA'), '2026-06-21 12:00:00', 'SCHEDULED', 'GROUP_STAGE', 'H', 39, 'Estadio Akron', 'Guadalajara', 'México'),
((SELECT id FROM teams WHERE code = 'CPV'), (SELECT id FROM teams WHERE code = 'URU'), '2026-06-21 21:00:00', 'SCHEDULED', 'GROUP_STAGE', 'H', 40, 'Estadio Azteca', 'Ciudad de México', 'México'),

-- GRUPO I - Fecha 2
((SELECT id FROM teams WHERE code = 'FRA'), (SELECT id FROM teams WHERE code = 'SEN'), '2026-06-22 18:00:00', 'SCHEDULED', 'GROUP_STAGE', 'I', 41, 'MetLife Stadium', 'East Rutherford', 'USA'),
((SELECT id FROM teams WHERE code = 'IRQ'), (SELECT id FROM teams WHERE code = 'NOR'), '2026-06-22 15:00:00', 'SCHEDULED', 'GROUP_STAGE', 'I', 42, 'Hard Rock Stadium', 'Miami', 'USA'),

-- GRUPO J - Fecha 2
((SELECT id FROM teams WHERE code = 'ARG'), (SELECT id FROM teams WHERE code = 'ALG'), '2026-06-22 21:00:00', 'SCHEDULED', 'GROUP_STAGE', 'J', 43, 'SoFi Stadium', 'Los Ángeles', 'USA'),
((SELECT id FROM teams WHERE code = 'JOR'), (SELECT id FROM teams WHERE code = 'AUT'), '2026-06-22 12:00:00', 'SCHEDULED', 'GROUP_STAGE', 'J', 44, 'Arrowhead Stadium', 'Kansas City', 'USA'),

-- GRUPO K - Fecha 2
((SELECT id FROM teams WHERE code = 'POR'), (SELECT id FROM teams WHERE code = 'COL'), '2026-06-23 18:00:00', 'SCHEDULED', 'GROUP_STAGE', 'K', 45, 'NRG Stadium', 'Houston', 'USA'),
((SELECT id FROM teams WHERE code = 'COD'), (SELECT id FROM teams WHERE code = 'UZB'), '2026-06-23 15:00:00', 'SCHEDULED', 'GROUP_STAGE', 'K', 46, 'Mercedes-Benz Stadium', 'Atlanta', 'USA'),

-- GRUPO L - Fecha 2
((SELECT id FROM teams WHERE code = 'ENG'), (SELECT id FROM teams WHERE code = 'CRO'), '2026-06-23 21:00:00', 'SCHEDULED', 'GROUP_STAGE', 'L', 47, 'MetLife Stadium', 'East Rutherford', 'USA'),
((SELECT id FROM teams WHERE code = 'PAN'), (SELECT id FROM teams WHERE code = 'GHA'), '2026-06-23 12:00:00', 'SCHEDULED', 'GROUP_STAGE', 'L', 48, 'AT&T Stadium', 'Arlington', 'USA');



-- FECHA 3 (25-28 de junio 2026)
INSERT INTO matches (home_team_id, away_team_id, match_date_time, status, phase, group_name, match_number, stadium, city, country) VALUES

-- GRUPO A - Fecha 3 (simultáneos)
((SELECT id FROM teams WHERE code = 'MEX'), (SELECT id FROM teams WHERE code = 'CZE'), '2026-06-25 18:00:00', 'SCHEDULED', 'GROUP_STAGE', 'A', 49, 'Estadio Azteca', 'Ciudad de México', 'México'),
((SELECT id FROM teams WHERE code = 'KOR'), (SELECT id FROM teams WHERE code = 'RSA'), '2026-06-25 18:00:00', 'SCHEDULED', 'GROUP_STAGE', 'A', 50, 'Estadio BBVA', 'Monterrey', 'México'),

-- GRUPO B - Fecha 3 (simultáneos)
((SELECT id FROM teams WHERE code = 'CAN'), (SELECT id FROM teams WHERE code = 'SUI'), '2026-06-25 15:00:00', 'SCHEDULED', 'GROUP_STAGE', 'B', 51, 'BMO Field', 'Toronto', 'Canadá'),
((SELECT id FROM teams WHERE code = 'QAT'), (SELECT id FROM teams WHERE code = 'BIH'), '2026-06-25 15:00:00', 'SCHEDULED', 'GROUP_STAGE', 'B', 52, 'BC Place', 'Vancouver', 'Canadá'),

-- GRUPO C - Fecha 3 (simultáneos)
((SELECT id FROM teams WHERE code = 'BRA'), (SELECT id FROM teams WHERE code = 'SCO'), '2026-06-25 21:00:00', 'SCHEDULED', 'GROUP_STAGE', 'C', 53, 'SoFi Stadium', 'Los Ángeles', 'USA'),
((SELECT id FROM teams WHERE code = 'MAR'), (SELECT id FROM teams WHERE code = 'HAI'), '2026-06-25 21:00:00', 'SCHEDULED', 'GROUP_STAGE', 'C', 54, 'Hard Rock Stadium', 'Miami', 'USA'),

-- GRUPO D - Fecha 3 (simultáneos)
((SELECT id FROM teams WHERE code = 'USA'), (SELECT id FROM teams WHERE code = 'TUR'), '2026-06-26 18:00:00', 'SCHEDULED', 'GROUP_STAGE', 'D', 55, 'MetLife Stadium', 'East Rutherford', 'USA'),
((SELECT id FROM teams WHERE code = 'AUS'), (SELECT id FROM teams WHERE code = 'PAR'), '2026-06-26 18:00:00', 'SCHEDULED', 'GROUP_STAGE', 'D', 56, 'Lumen Field', 'Seattle', 'USA'),

-- GRUPO E - Fecha 3 (simultáneos)
((SELECT id FROM teams WHERE code = 'GER'), (SELECT id FROM teams WHERE code = 'ECU'), '2026-06-26 15:00:00', 'SCHEDULED', 'GROUP_STAGE', 'E', 57, 'Mercedes-Benz Stadium', 'Atlanta', 'USA'),
((SELECT id FROM teams WHERE code = 'CIV'), (SELECT id FROM teams WHERE code = 'CUW'), '2026-06-26 15:00:00', 'SCHEDULED', 'GROUP_STAGE', 'E', 58, 'NRG Stadium', 'Houston', 'USA'),

-- GRUPO F - Fecha 3 (simultáneos)
((SELECT id FROM teams WHERE code = 'NED'), (SELECT id FROM teams WHERE code = 'TUN'), '2026-06-26 21:00:00', 'SCHEDULED', 'GROUP_STAGE', 'F', 59, 'Gillette Stadium', 'Foxborough', 'USA'),
((SELECT id FROM teams WHERE code = 'JPN'), (SELECT id FROM teams WHERE code = 'SWE'), '2026-06-26 21:00:00', 'SCHEDULED', 'GROUP_STAGE', 'F', 60, 'Levi''s Stadium', 'Santa Clara', 'USA'),

-- GRUPO G - Fecha 3 (simultáneos)
((SELECT id FROM teams WHERE code = 'BEL'), (SELECT id FROM teams WHERE code = 'IRN'), '2026-06-27 18:00:00', 'SCHEDULED', 'GROUP_STAGE', 'G', 61, 'Lincoln Financial Field', 'Filadelfia', 'USA'),
((SELECT id FROM teams WHERE code = 'EGY'), (SELECT id FROM teams WHERE code = 'NZL'), '2026-06-27 18:00:00', 'SCHEDULED', 'GROUP_STAGE', 'G', 62, 'AT&T Stadium', 'Arlington', 'USA'),

-- GRUPO H - Fecha 3 (simultáneos)
((SELECT id FROM teams WHERE code = 'ESP'), (SELECT id FROM teams WHERE code = 'URU'), '2026-06-27 15:00:00', 'SCHEDULED', 'GROUP_STAGE', 'H', 63, 'Estadio Akron', 'Guadalajara', 'México'),
((SELECT id FROM teams WHERE code = 'KSA'), (SELECT id FROM teams WHERE code = 'CPV'), '2026-06-27 15:00:00', 'SCHEDULED', 'GROUP_STAGE', 'H', 64, 'Estadio Azteca', 'Ciudad de México', 'México'),

-- GRUPO I - Fecha 3 (simultáneos)
((SELECT id FROM teams WHERE code = 'FRA'), (SELECT id FROM teams WHERE code = 'NOR'), '2026-06-27 21:00:00', 'SCHEDULED', 'GROUP_STAGE', 'I', 65, 'MetLife Stadium', 'East Rutherford', 'USA'),
((SELECT id FROM teams WHERE code = 'SEN'), (SELECT id FROM teams WHERE code = 'IRQ'), '2026-06-27 21:00:00', 'SCHEDULED', 'GROUP_STAGE', 'I', 66, 'Hard Rock Stadium', 'Miami', 'USA'),

-- GRUPO J - Fecha 3 (simultáneos)
((SELECT id FROM teams WHERE code = 'ARG'), (SELECT id FROM teams WHERE code = 'AUT'), '2026-06-28 18:00:00', 'SCHEDULED', 'GROUP_STAGE', 'J', 67, 'SoFi Stadium', 'Los Ángeles', 'USA'),
((SELECT id FROM teams WHERE code = 'ALG'), (SELECT id FROM teams WHERE code = 'JOR'), '2026-06-28 18:00:00', 'SCHEDULED', 'GROUP_STAGE', 'J', 68, 'Arrowhead Stadium', 'Kansas City', 'USA'),

-- GRUPO K - Fecha 3 (simultáneos)
((SELECT id FROM teams WHERE code = 'POR'), (SELECT id FROM teams WHERE code = 'UZB'), '2026-06-28 15:00:00', 'SCHEDULED', 'GROUP_STAGE', 'K', 69, 'NRG Stadium', 'Houston', 'USA'),
((SELECT id FROM teams WHERE code = 'COL'), (SELECT id FROM teams WHERE code = 'COD'), '2026-06-28 15:00:00', 'SCHEDULED', 'GROUP_STAGE', 'K', 70, 'Mercedes-Benz Stadium', 'Atlanta', 'USA'),

-- GRUPO L - Fecha 3 (simultáneos)
((SELECT id FROM teams WHERE code = 'ENG'), (SELECT id FROM teams WHERE code = 'GHA'), '2026-06-28 21:00:00', 'SCHEDULED', 'GROUP_STAGE', 'L', 71, 'MetLife Stadium', 'East Rutherford', 'USA'),
((SELECT id FROM teams WHERE code = 'CRO'), (SELECT id FROM teams WHERE code = 'PAN'), '2026-06-28 21:00:00', 'SCHEDULED', 'GROUP_STAGE', 'L', 72, 'AT&T Stadium', 'Arlington', 'USA');

-- ============================================
-- VERIFICACIÓN
-- ============================================

SELECT 'EQUIPOS POR GRUPO:' AS info;
SELECT group_name, COUNT(*) as equipos FROM teams GROUP BY group_name ORDER BY group_name;

SELECT 'TOTAL EQUIPOS:' AS info, COUNT(*) AS total FROM teams;
SELECT 'TOTAL PARTIDOS FASE DE GRUPOS:' AS info, COUNT(*) AS total FROM matches WHERE phase = 'GROUP_STAGE';
SELECT 'CONFIGURACIÓN ACTIVA:' AS info, COUNT(*) AS total FROM scoring_config WHERE active = true;

-- Ver muestra de partidos
SELECT 
    m.match_number,
    m.group_name,
    t1.name AS local,
    t2.name AS visitante,
    m.match_date_time,
    m.stadium,
    m.city
FROM matches m
JOIN teams t1 ON m.home_team_id = t1.id
JOIN teams t2 ON m.away_team_id = t2.id
ORDER BY m.match_number
LIMIT 20;


SET FOREIGN_KEY_CHECKS = 0;
SET SQL_SAFE_UPDATES = 0;
-- Actualizar flag_url con CDN de flagcdn.com (gratis, sin API key)
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/mx.png' WHERE code = 'MEX';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/za.png' WHERE code = 'RSA';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/kr.png' WHERE code = 'KOR';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/dk.png' WHERE code = 'DEN';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/ca.png' WHERE code = 'CAN';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/qa.png' WHERE code = 'QAT';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/ch.png' WHERE code = 'SUI';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/it.png' WHERE code = 'ITA';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/br.png' WHERE code = 'BRA';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/ma.png' WHERE code = 'MAR';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/ht.png' WHERE code = 'HAI';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/gb-sct.png' WHERE code = 'SCO';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/us.png' WHERE code = 'USA';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/py.png' WHERE code = 'PAR';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/au.png' WHERE code = 'AUS';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/tr.png' WHERE code = 'TUR';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/de.png' WHERE code = 'GER';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/cw.png' WHERE code = 'CUW';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/ci.png' WHERE code = 'CIV';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/ec.png' WHERE code = 'ECU';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/nl.png' WHERE code = 'NED';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/jp.png' WHERE code = 'JPN';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/tn.png' WHERE code = 'TUN';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/ua.png' WHERE code = 'UKR';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/be.png' WHERE code = 'BEL';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/eg.png' WHERE code = 'EGY';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/ir.png' WHERE code = 'IRN';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/nz.png' WHERE code = 'NZL';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/es.png' WHERE code = 'ESP';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/cv.png' WHERE code = 'CPV';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/sa.png' WHERE code = 'KSA';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/uy.png' WHERE code = 'URU';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/bo.png' WHERE code = 'BOL';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/fr.png' WHERE code = 'FRA';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/sn.png' WHERE code = 'SEN';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/no.png' WHERE code = 'NOR';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/ar.png' WHERE code = 'ARG';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/dz.png' WHERE code = 'ALG';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/at.png' WHERE code = 'AUT';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/jo.png' WHERE code = 'JOR';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/jm.png' WHERE code = 'JAM';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/pt.png' WHERE code = 'POR';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/uz.png' WHERE code = 'UZB';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/co.png' WHERE code = 'COL';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/gb-eng.png' WHERE code = 'ENG';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/hr.png' WHERE code = 'CRO';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/gh.png' WHERE code = 'GHA';
UPDATE teams SET flag_url = 'https://flagcdn.com/w80/pa.png' WHERE code = 'PAN';

-- Verificar
SELECT code, name, flag_url FROM teams ORDER BY code;
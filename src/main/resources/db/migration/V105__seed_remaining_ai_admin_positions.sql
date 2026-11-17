-- V105: Seed remaining AI admin positions
-- Creates 22 Younger Industries office managers and positions the 5 entity-linked AI characters
--
-- Office managers are positioned near their capital cities with a 0.5-unit offset from country leaders
-- to avoid overlap in the 3D galaxy view.
-- Entity-linked AI characters are positioned in the Demo galaxy around a small cluster.

-- ============================================================
-- Part 1: Create 22 Younger Industries office managers (IDs 35-56)
-- ============================================================
-- Each manager is positioned at their office's capital city with a slight offset
-- from the country leader's position (V102 data) to avoid overlap.

-- ID 35: Washington D.C. Office Manager (near US leader ID 2: 6.85, 6.43, -5.29)
INSERT INTO ai_characters (id, name, role, empire_name, character_type, galaxy_name,
                           pos_x, pos_y, pos_z, home_pos_x, home_pos_y, home_pos_z,
                           ai_admin, active, can_promote_to_co_server, description)
SELECT 35, 'Washington D.C. Office Manager', 'Office Manager', 'Younger Industries', 'NPC', 'Milky Way',
       6.35, 6.93, -5.79, 6.35, 6.93, -5.79,
       TRUE, TRUE, FALSE,
       'AI character managing the Younger Industries Washington D.C. branch office.'
WHERE NOT EXISTS (SELECT 1 FROM ai_characters WHERE id = 35);

-- ID 36: Beijing Office Manager (near China leader ID 3: 3.60, 6.44, 7.03)
INSERT INTO ai_characters (id, name, role, empire_name, character_type, galaxy_name,
                           pos_x, pos_y, pos_z, home_pos_x, home_pos_y, home_pos_z,
                           ai_admin, active, can_promote_to_co_server, description)
SELECT 36, 'Beijing Office Manager', 'Office Manager', 'Younger Industries', 'NPC', 'Milky Way',
       3.10, 6.94, 7.53, 3.10, 6.94, 7.53,
       TRUE, TRUE, FALSE,
       'AI character managing the Younger Industries Beijing branch office.'
WHERE NOT EXISTS (SELECT 1 FROM ai_characters WHERE id = 36);

-- ID 37: Moscow Office Manager (near Russia leader ID 4: 6.47, 8.30, -1.93)
INSERT INTO ai_characters (id, name, role, empire_name, character_type, galaxy_name,
                           pos_x, pos_y, pos_z, home_pos_x, home_pos_y, home_pos_z,
                           ai_admin, active, can_promote_to_co_server, description)
SELECT 37, 'Moscow Office Manager', 'Office Manager', 'Younger Industries', 'NPC', 'Milky Way',
       5.97, 8.80, -2.43, 5.97, 8.80, -2.43,
       TRUE, TRUE, FALSE,
       'AI character managing the Younger Industries Moscow branch office.'
WHERE NOT EXISTS (SELECT 1 FROM ai_characters WHERE id = 37);

-- ID 38: London Office Manager (near UK leader ID 5: 0.02, 7.90, -6.57)
INSERT INTO ai_characters (id, name, role, empire_name, character_type, galaxy_name,
                           pos_x, pos_y, pos_z, home_pos_x, home_pos_y, home_pos_z,
                           ai_admin, active, can_promote_to_co_server, description)
SELECT 38, 'London Office Manager', 'Office Manager', 'Younger Industries', 'NPC', 'Milky Way',
       -0.48, 8.40, -7.07, -0.48, 8.40, -7.07,
       TRUE, TRUE, FALSE,
       'AI character managing the Younger Industries London branch office.'
WHERE NOT EXISTS (SELECT 1 FROM ai_characters WHERE id = 38);

-- ID 39: Berlin Office Manager (near Germany leader ID 6: 2.30, 8.03, -5.83)
INSERT INTO ai_characters (id, name, role, empire_name, character_type, galaxy_name,
                           pos_x, pos_y, pos_z, home_pos_x, home_pos_y, home_pos_z,
                           ai_admin, active, can_promote_to_co_server, description)
SELECT 39, 'Berlin Office Manager', 'Office Manager', 'Younger Industries', 'NPC', 'Milky Way',
       1.80, 8.53, -6.33, 1.80, 8.53, -6.33,
       TRUE, TRUE, FALSE,
       'AI character managing the Younger Industries Berlin branch office.'
WHERE NOT EXISTS (SELECT 1 FROM ai_characters WHERE id = 39);

-- ID 40: Paris Office Manager (near France leader ID 7: 0.41, 7.56, -6.28)
INSERT INTO ai_characters (id, name, role, empire_name, character_type, galaxy_name,
                           pos_x, pos_y, pos_z, home_pos_x, home_pos_y, home_pos_z,
                           ai_admin, active, can_promote_to_co_server, description)
SELECT 40, 'Paris Office Manager', 'Office Manager', 'Younger Industries', 'NPC', 'Milky Way',
       -0.09, 8.06, -6.78, -0.09, 8.06, -6.78,
       TRUE, TRUE, FALSE,
       'AI character managing the Younger Industries Paris branch office.'
WHERE NOT EXISTS (SELECT 1 FROM ai_characters WHERE id = 40);

-- ID 41: Tokyo Office Manager (near Japan leader ID 8: 5.93, 5.84, 5.57)
INSERT INTO ai_characters (id, name, role, empire_name, character_type, galaxy_name,
                           pos_x, pos_y, pos_z, home_pos_x, home_pos_y, home_pos_z,
                           ai_admin, active, can_promote_to_co_server, description)
SELECT 41, 'Tokyo Office Manager', 'Office Manager', 'Younger Industries', 'NPC', 'Milky Way',
       5.43, 6.34, 6.07, 5.43, 6.34, 6.07,
       TRUE, TRUE, FALSE,
       'AI character managing the Younger Industries Tokyo branch office.'
WHERE NOT EXISTS (SELECT 1 FROM ai_characters WHERE id = 41);

-- ID 42: New Delhi Office Manager (near India leader ID 9: 4.57, 4.79, 7.73)
INSERT INTO ai_characters (id, name, role, empire_name, character_type, galaxy_name,
                           pos_x, pos_y, pos_z, home_pos_x, home_pos_y, home_pos_z,
                           ai_admin, active, can_promote_to_co_server, description)
SELECT 42, 'New Delhi Office Manager', 'Office Manager', 'Younger Industries', 'NPC', 'Milky Way',
       4.07, 5.29, 8.23, 4.07, 5.29, 8.23,
       TRUE, TRUE, FALSE,
       'AI character managing the Younger Industries New Delhi branch office.'
WHERE NOT EXISTS (SELECT 1 FROM ai_characters WHERE id = 42);

-- ID 43: Brasilia Office Manager (near Brazil leader ID 10: -7.27, -2.73, -5.33)
INSERT INTO ai_characters (id, name, role, empire_name, character_type, galaxy_name,
                           pos_x, pos_y, pos_z, home_pos_x, home_pos_y, home_pos_z,
                           ai_admin, active, can_promote_to_co_server, description)
SELECT 43, 'Brasilia Office Manager', 'Office Manager', 'Younger Industries', 'NPC', 'Milky Way',
       -7.77, -3.23, -5.83, -7.77, -3.23, -5.83,
       TRUE, TRUE, FALSE,
       'AI character managing the Younger Industries Brasilia branch office.'
WHERE NOT EXISTS (SELECT 1 FROM ai_characters WHERE id = 43);

-- ID 44: Ottawa Office Manager (near Canada leader ID 11: 6.53, 7.13, -5.93)
INSERT INTO ai_characters (id, name, role, empire_name, character_type, galaxy_name,
                           pos_x, pos_y, pos_z, home_pos_x, home_pos_y, home_pos_z,
                           ai_admin, active, can_promote_to_co_server, description)
SELECT 44, 'Ottawa Office Manager', 'Office Manager', 'Younger Industries', 'NPC', 'Milky Way',
       7.03, 7.63, -6.43, 7.03, 7.63, -6.43,
       TRUE, TRUE, FALSE,
       'AI character managing the Younger Industries Ottawa branch office.'
WHERE NOT EXISTS (SELECT 1 FROM ai_characters WHERE id = 44);

-- ID 45: Canberra Office Manager (near Australia leader ID 12: 6.37, -5.78, 4.83)
INSERT INTO ai_characters (id, name, role, empire_name, character_type, galaxy_name,
                           pos_x, pos_y, pos_z, home_pos_x, home_pos_y, home_pos_z,
                           ai_admin, active, can_promote_to_co_server, description)
SELECT 45, 'Canberra Office Manager', 'Office Manager', 'Younger Industries', 'NPC', 'Milky Way',
       5.87, -6.28, 5.33, 5.87, -6.28, 5.33,
       TRUE, TRUE, FALSE,
       'AI character managing the Younger Industries Canberra branch office.'
WHERE NOT EXISTS (SELECT 1 FROM ai_characters WHERE id = 45);

-- ID 46: Seoul Office Manager (near South Korea leader ID 13: 5.23, 6.10, 6.17)
INSERT INTO ai_characters (id, name, role, empire_name, character_type, galaxy_name,
                           pos_x, pos_y, pos_z, home_pos_x, home_pos_y, home_pos_z,
                           ai_admin, active, can_promote_to_co_server, description)
SELECT 46, 'Seoul Office Manager', 'Office Manager', 'Younger Industries', 'NPC', 'Milky Way',
       4.73, 6.60, 6.67, 4.73, 6.60, 6.67,
       TRUE, TRUE, FALSE,
       'AI character managing the Younger Industries Seoul branch office.'
WHERE NOT EXISTS (SELECT 1 FROM ai_characters WHERE id = 46);

-- ID 47: Mexico City Office Manager (near Mexico leader ID 14: -8.63, 3.33, -3.53)
INSERT INTO ai_characters (id, name, role, empire_name, character_type, galaxy_name,
                           pos_x, pos_y, pos_z, home_pos_x, home_pos_y, home_pos_z,
                           ai_admin, active, can_promote_to_co_server, description)
SELECT 47, 'Mexico City Office Manager', 'Office Manager', 'Younger Industries', 'NPC', 'Milky Way',
       -9.13, 3.83, -4.03, -9.13, 3.83, -4.03,
       TRUE, TRUE, FALSE,
       'AI character managing the Younger Industries Mexico City branch office.'
WHERE NOT EXISTS (SELECT 1 FROM ai_characters WHERE id = 47);

-- ID 48: Rome Office Manager (near Italy leader ID 15: 2.17, 6.69, -6.36)
INSERT INTO ai_characters (id, name, role, empire_name, character_type, galaxy_name,
                           pos_x, pos_y, pos_z, home_pos_x, home_pos_y, home_pos_z,
                           ai_admin, active, can_promote_to_co_server, description)
SELECT 48, 'Rome Office Manager', 'Office Manager', 'Younger Industries', 'NPC', 'Milky Way',
       1.67, 7.19, -6.86, 1.67, 7.19, -6.86,
       TRUE, TRUE, FALSE,
       'AI character managing the Younger Industries Rome branch office.'
WHERE NOT EXISTS (SELECT 1 FROM ai_characters WHERE id = 48);

-- ID 49: Madrid Office Manager (near Spain leader ID 16: -0.65, 6.50, -6.39)
INSERT INTO ai_characters (id, name, role, empire_name, character_type, galaxy_name,
                           pos_x, pos_y, pos_z, home_pos_x, home_pos_y, home_pos_z,
                           ai_admin, active, can_promote_to_co_server, description)
SELECT 49, 'Madrid Office Manager', 'Office Manager', 'Younger Industries', 'NPC', 'Milky Way',
       -1.15, 7.00, -6.89, -1.15, 7.00, -6.89,
       TRUE, TRUE, FALSE,
       'AI character managing the Younger Industries Madrid branch office.'
WHERE NOT EXISTS (SELECT 1 FROM ai_characters WHERE id = 49);

-- ID 50: Abuja Office Manager (near Nigeria leader ID 17: 1.29, 1.58, -9.87)
INSERT INTO ai_characters (id, name, role, empire_name, character_type, galaxy_name,
                           pos_x, pos_y, pos_z, home_pos_x, home_pos_y, home_pos_z,
                           ai_admin, active, can_promote_to_co_server, description)
SELECT 50, 'Abuja Office Manager', 'Office Manager', 'Younger Industries', 'NPC', 'Milky Way',
       0.79, 2.08, -10.37, 0.79, 2.08, -10.37,
       TRUE, TRUE, FALSE,
       'AI character managing the Younger Industries Abuja branch office.'
WHERE NOT EXISTS (SELECT 1 FROM ai_characters WHERE id = 50);

-- ID 51: Riyadh Office Manager (near Saudi Arabia leader ID 18: 8.13, 4.20, -4.87)
INSERT INTO ai_characters (id, name, role, empire_name, character_type, galaxy_name,
                           pos_x, pos_y, pos_z, home_pos_x, home_pos_y, home_pos_z,
                           ai_admin, active, can_promote_to_co_server, description)
SELECT 51, 'Riyadh Office Manager', 'Office Manager', 'Younger Industries', 'NPC', 'Milky Way',
       7.63, 4.70, -5.37, 7.63, 4.70, -5.37,
       TRUE, TRUE, FALSE,
       'AI character managing the Younger Industries Riyadh branch office.'
WHERE NOT EXISTS (SELECT 1 FROM ai_characters WHERE id = 51);

-- ID 52: Ankara Office Manager (near Turkey leader ID 19: 5.73, 6.45, -5.63)
INSERT INTO ai_characters (id, name, role, empire_name, character_type, galaxy_name,
                           pos_x, pos_y, pos_z, home_pos_x, home_pos_y, home_pos_z,
                           ai_admin, active, can_promote_to_co_server, description)
SELECT 52, 'Ankara Office Manager', 'Office Manager', 'Younger Industries', 'NPC', 'Milky Way',
       5.23, 6.95, -6.13, 5.23, 6.95, -6.13,
       TRUE, TRUE, FALSE,
       'AI character managing the Younger Industries Ankara branch office.'
WHERE NOT EXISTS (SELECT 1 FROM ai_characters WHERE id = 52);

-- ID 53: Buenos Aires Office Manager (near Argentina leader ID 20: -8.87, -5.68, -3.13)
INSERT INTO ai_characters (id, name, role, empire_name, character_type, galaxy_name,
                           pos_x, pos_y, pos_z, home_pos_x, home_pos_y, home_pos_z,
                           ai_admin, active, can_promote_to_co_server, description)
SELECT 53, 'Buenos Aires Office Manager', 'Office Manager', 'Younger Industries', 'NPC', 'Milky Way',
       -9.37, -6.18, -3.63, -9.37, -6.18, -3.63,
       TRUE, TRUE, FALSE,
       'AI character managing the Younger Industries Buenos Aires branch office.'
WHERE NOT EXISTS (SELECT 1 FROM ai_characters WHERE id = 53);

-- ID 54: Pretoria Office Manager (near South Africa leader ID 21: 4.85, -4.35, -7.23)
INSERT INTO ai_characters (id, name, role, empire_name, character_type, galaxy_name,
                           pos_x, pos_y, pos_z, home_pos_x, home_pos_y, home_pos_z,
                           ai_admin, active, can_promote_to_co_server, description)
SELECT 54, 'Pretoria Office Manager', 'Office Manager', 'Younger Industries', 'NPC', 'Milky Way',
       4.35, -4.85, -7.73, 4.35, -4.85, -7.73,
       TRUE, TRUE, FALSE,
       'AI character managing the Younger Industries Pretoria branch office.'
WHERE NOT EXISTS (SELECT 1 FROM ai_characters WHERE id = 54);

-- ID 55: Jakarta Office Manager (Indonesia - no country leader, positioned independently)
INSERT INTO ai_characters (id, name, role, empire_name, character_type, galaxy_name,
                           pos_x, pos_y, pos_z, home_pos_x, home_pos_y, home_pos_z,
                           ai_admin, active, can_promote_to_co_server, description)
SELECT 55, 'Jakarta Office Manager', 'Office Manager', 'Younger Industries', 'NPC', 'Milky Way',
       3.77, 0.58, 9.93, 3.77, 0.58, 9.93,
       TRUE, TRUE, FALSE,
       'AI character managing the Younger Industries Jakarta branch office.'
WHERE NOT EXISTS (SELECT 1 FROM ai_characters WHERE id = 55);

-- ID 56: Brussels Office Manager (EU - no country leader, positioned independently)
INSERT INTO ai_characters (id, name, role, empire_name, character_type, galaxy_name,
                           pos_x, pos_y, pos_z, home_pos_x, home_pos_y, home_pos_z,
                           ai_admin, active, can_promote_to_co_server, description)
SELECT 56, 'Brussels Office Manager', 'Office Manager', 'Younger Industries', 'NPC', 'Milky Way',
       -0.52, 8.40, -6.57, -0.52, 8.40, -6.57,
       TRUE, TRUE, FALSE,
       'AI character managing the Younger Industries Brussels branch office.'
WHERE NOT EXISTS (SELECT 1 FROM ai_characters WHERE id = 56);

-- ============================================================
-- Part 2: Assign managers to Younger Industries offices
-- ============================================================
-- Match offices by city name to assign the correct manager

UPDATE younger_offices SET manager_id = 35 WHERE city = 'Washington D.C.' AND manager_id IS NULL;
UPDATE younger_offices SET manager_id = 36 WHERE city = 'Beijing' AND manager_id IS NULL;
UPDATE younger_offices SET manager_id = 37 WHERE city = 'Moscow' AND manager_id IS NULL;
UPDATE younger_offices SET manager_id = 38 WHERE city = 'London' AND manager_id IS NULL;
UPDATE younger_offices SET manager_id = 39 WHERE city = 'Berlin' AND manager_id IS NULL;
UPDATE younger_offices SET manager_id = 40 WHERE city = 'Paris' AND manager_id IS NULL;
UPDATE younger_offices SET manager_id = 41 WHERE city = 'Tokyo' AND manager_id IS NULL;
UPDATE younger_offices SET manager_id = 42 WHERE city = 'New Delhi' AND manager_id IS NULL;
UPDATE younger_offices SET manager_id = 43 WHERE city = 'Brasilia' AND manager_id IS NULL;
UPDATE younger_offices SET manager_id = 44 WHERE city = 'Ottawa' AND manager_id IS NULL;
UPDATE younger_offices SET manager_id = 45 WHERE city = 'Canberra' AND manager_id IS NULL;
UPDATE younger_offices SET manager_id = 46 WHERE city = 'Seoul' AND manager_id IS NULL;
UPDATE younger_offices SET manager_id = 47 WHERE city = 'Mexico City' AND manager_id IS NULL;
UPDATE younger_offices SET manager_id = 48 WHERE city = 'Rome' AND manager_id IS NULL;
UPDATE younger_offices SET manager_id = 49 WHERE city = 'Madrid' AND manager_id IS NULL;
UPDATE younger_offices SET manager_id = 50 WHERE city = 'Abuja' AND manager_id IS NULL;
UPDATE younger_offices SET manager_id = 51 WHERE city = 'Riyadh' AND manager_id IS NULL;
UPDATE younger_offices SET manager_id = 52 WHERE city = 'Ankara' AND manager_id IS NULL;
UPDATE younger_offices SET manager_id = 53 WHERE city = 'Buenos Aires' AND manager_id IS NULL;
UPDATE younger_offices SET manager_id = 54 WHERE city = 'Pretoria' AND manager_id IS NULL;
UPDATE younger_offices SET manager_id = 55 WHERE city = 'Jakarta' AND manager_id IS NULL;
UPDATE younger_offices SET manager_id = 56 WHERE city = 'Brussels' AND manager_id IS NULL;

-- ============================================================
-- Part 3: Position entity-linked AI characters in Demo galaxy
-- ============================================================
-- These 5 AI characters (IDs 2001-2005) represent ship, station, vehicle, and facility computers
-- Positioned around a small cluster in the Demo galaxy

-- ID 2001: Ship Computer (mining frigate) — positioned near Demo mining site
UPDATE ai_characters SET
  galaxy_name = 'Demo',
  pos_x = 2.0, pos_y = 1.0, pos_z = 3.0,
  home_pos_x = 2.0, home_pos_y = 1.0, home_pos_z = 3.0
WHERE id = 2001;

-- ID 2002: Ship Computer (military destroyer) — positioned near Demo military station
UPDATE ai_characters SET
  galaxy_name = 'Demo',
  pos_x = -3.0, pos_y = 2.0, pos_z = -1.0,
  home_pos_x = -3.0, home_pos_y = 2.0, home_pos_z = -1.0
WHERE id = 2002;

-- ID 2003: Station Computer (space station) — positioned at Demo galaxy center
UPDATE ai_characters SET
  galaxy_name = 'Demo',
  pos_x = 0.0, pos_y = 0.0, pos_z = 0.0,
  home_pos_x = 0.0, home_pos_y = 0.0, home_pos_z = 0.0
WHERE id = 2003;

-- ID 2004: Vehicle Computer (ground vehicle) — positioned on Demo planet surface
UPDATE ai_characters SET
  galaxy_name = 'Demo',
  pos_x = 1.0, pos_y = -2.0, pos_z = 1.0,
  home_pos_x = 1.0, home_pos_y = -2.0, home_pos_z = 1.0
WHERE id = 2004;

-- ID 2005: Facility Computer (planet facility) — positioned at Demo facility location
UPDATE ai_characters SET
  galaxy_name = 'Demo',
  pos_x = -1.0, pos_y = -1.0, pos_z = 2.0,
  home_pos_x = -1.0, home_pos_y = -1.0, home_pos_z = 2.0
WHERE id = 2005;

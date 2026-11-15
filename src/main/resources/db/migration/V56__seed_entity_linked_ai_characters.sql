-- V56: Seed sample entity-linked AI characters for testing and demo
-- These represent ship, station, vehicle, and facility computers connected to the AI network

-- 2 sample SHIP AI characters with different training levels
INSERT INTO ai_characters (name, role, empire_name, character_type, primary_skill, training_level,
                           linked_ship_id, total_experience, leadership_rating, active, hirable, daily_salary, hiring_status, description)
SELECT 2001, 'Ship Computer', 'Demo', 'SHIP', 'Naval Combat', 'BASIC',
       1001, 50, 5, TRUE, FALSE, 0, 'UNAVAILABLE',
       'AI character representing a Demo mining frigate computer (BASIC tier).'
WHERE NOT EXISTS (SELECT 1 FROM ai_characters WHERE id = 2001);

INSERT INTO ai_characters (name, role, empire_name, character_type, primary_skill, training_level,
                           linked_ship_id, total_experience, leadership_rating, active, hirable, daily_salary, hiring_status, description)
SELECT 2002, 'Ship Computer', 'Demo', 'SHIP', 'Naval Combat', 'ADVANCED',
       1002, 600, 7, TRUE, FALSE, 0, 'UNAVAILABLE',
       'AI character representing a Demo military destroyer computer (ADVANCED tier).'
WHERE NOT EXISTS (SELECT 1 FROM ai_characters WHERE id = 2002);

-- 1 sample STATION AI character
INSERT INTO ai_characters (name, role, empire_name, character_type, primary_skill, training_level,
                           linked_station_id, total_experience, leadership_rating, active, hirable, daily_salary, hiring_status, description)
SELECT 2003, 'Station Computer', 'Demo', 'STATION', 'Station Operations', 'BASIC',
       2001, 20, 5, TRUE, FALSE, 0, 'UNAVAILABLE',
       'AI character representing a Demo space station computer (BASIC tier).'
WHERE NOT EXISTS (SELECT 1 FROM ai_characters WHERE id = 2003);

-- 1 sample VEHICLE AI character
INSERT INTO ai_characters (name, role, empire_name, character_type, primary_skill, training_level,
                           linked_vehicle_id, linked_entity_id, total_experience, leadership_rating, active, hirable, daily_salary, hiring_status, description)
SELECT 2004, 'Vehicle Computer', 'Demo', 'VEHICLE', 'Armor Tactics', 'BASIC',
       3001, 'VEHICLE-001', 10, 5, TRUE, FALSE, 0, 'UNAVAILABLE',
       'AI character representing a Demo ground vehicle computer (BASIC tier).'
WHERE NOT EXISTS (SELECT 1 FROM ai_characters WHERE id = 2004);

-- 1 sample FACILITY AI character
INSERT INTO ai_characters (name, role, empire_name, character_type, primary_skill, training_level,
                           linked_facility_id, total_experience, leadership_rating, active, hirable, daily_salary, hiring_status, description)
SELECT 2005, 'Facility Computer', 'Demo', 'FACILITY', 'Engineering', 'BASIC',
       4001, 5, 5, TRUE, FALSE, 0, 'UNAVAILABLE',
       'AI character representing a Demo planet facility computer (BASIC tier).'
WHERE NOT EXISTS (SELECT 1 FROM ai_characters WHERE id = 2005);

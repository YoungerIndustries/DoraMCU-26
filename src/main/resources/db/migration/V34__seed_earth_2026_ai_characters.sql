-- V34: Seed AI characters for all Earth 2026 countries and regions

-- 1. Seed AI characters for country leaders (20 countries)
-- Country IDs match V33 seed order: US=1, CN=2, RU=3, GB=4, DE=5, FR=6, JP=7, IN=8, BR=9, CA=10,
-- AU=11, KR=12, MX=13, IT=14, ES=15, NG=16, SA=17, TR=18, AR=19, ZA=20

INSERT INTO ai_characters (name, role, empire_name, linked_player_id, active, can_promote_to_co_server, description)
VALUES
-- United States
('Joe Biden', 'President', 'United States', NULL, TRUE, FALSE,
 'AI character representing the US President, controlling American military and diplomatic operations.'),
-- China
('Xi Jinping', 'President', 'China', NULL, TRUE, FALSE,
 'AI character representing the Chinese President, controlling Chinese state operations.'),
-- Russia
('Vladimir Putin', 'President', 'Russia', NULL, TRUE, FALSE,
 'AI character representing the Russian President, controlling Russian military and state operations.'),
-- United Kingdom
('Rishi Sunak', 'Prime Minister', 'United Kingdom', NULL, TRUE, FALSE,
 'AI character representing the UK Prime Minister, controlling British government operations.'),
-- Germany
('Olaf Scholz', 'Chancellor', 'Germany', NULL, TRUE, FALSE,
 'AI character representing the German Chancellor, controlling German government operations.'),
-- France
('Emmanuel Macron', 'President', 'France', NULL, TRUE, FALSE,
 'AI character representing the French President, controlling French government operations.'),
-- Japan
('Fumio Kishida', 'Prime Minister', 'Japan', NULL, TRUE, FALSE,
 'AI character representing the Japanese Prime Minister, controlling Japanese government operations.'),
-- India
('Narendra Modi', 'Prime Minister', 'India', NULL, TRUE, FALSE,
 'AI character representing the Indian Prime Minister, controlling Indian government operations.'),
-- Brazil
('Luiz Inacio Lula da Silva', 'President', 'Brazil', NULL, TRUE, FALSE,
 'AI character representing the Brazilian President, controlling Brazilian government operations.'),
-- Canada
('Justin Trudeau', 'Prime Minister', 'Canada', NULL, TRUE, FALSE,
 'AI character representing the Canadian Prime Minister, controlling Canadian government operations.'),
-- Australia
('Anthony Albanese', 'Prime Minister', 'Australia', NULL, TRUE, FALSE,
 'AI character representing the Australian Prime Minister, controlling Australian government operations.'),
-- South Korea
('Yoon Suk Yeol', 'President', 'South Korea', NULL, TRUE, FALSE,
 'AI character representing the South Korean President, controlling South Korean government operations.'),
-- Mexico
('Claudia Sheinbaum', 'President', 'Mexico', NULL, TRUE, FALSE,
 'AI character representing the Mexican President, controlling Mexican government operations.'),
-- Italy
('Giorgia Meloni', 'Prime Minister', 'Italy', NULL, TRUE, FALSE,
 'AI character representing the Italian Prime Minister, controlling Italian government operations.'),
-- Spain
('Pedro Sanchez', 'Prime Minister', 'Spain', NULL, TRUE, FALSE,
 'AI character representing the Spanish Prime Minister, controlling Spanish government operations.'),
-- Nigeria
('Bola Tinubu', 'President', 'Nigeria', NULL, TRUE, FALSE,
 'AI character representing the Nigerian President, controlling Nigerian government operations.'),
-- Saudi Arabia
('Salman bin Abdulaziz', 'King', 'Saudi Arabia', NULL, TRUE, FALSE,
 'AI character representing the Saudi King, controlling Saudi government operations.'),
-- Turkey
('Recep Tayyip Erdogan', 'President', 'Turkey', NULL, TRUE, FALSE,
 'AI character representing the Turkish President, controlling Turkish government operations.'),
-- Argentina
('Javier Milei', 'President', 'Argentina', NULL, TRUE, FALSE,
 'AI character representing the Argentine President, controlling Argentine government operations.'),
-- South Africa
('Cyril Ramaphosa', 'President', 'South Africa', NULL, TRUE, FALSE,
 'AI character representing the South African President, controlling South African government operations.');

-- 2. Assign AI leaders to countries
-- AI character IDs match insert order: US=2, CN=3, RU=4, GB=5, DE=6, FR=7, JP=8, IN=9, BR=10, CA=11,
-- AU=12, KR=13, MX=14, IT=15, ES=16, NG=17, SA=18, TR=19, AR=20, ZA=21
-- (ID=1 is Sif Younger from DataInitializer)

UPDATE earth2026_countries SET ai_leader_id = 2, leader_name = 'Joe Biden', leader_title = 'President' WHERE id = 1;
UPDATE earth2026_countries SET ai_leader_id = 3, leader_name = 'Xi Jinping', leader_title = 'President' WHERE id = 2;
UPDATE earth2026_countries SET ai_leader_id = 4, leader_name = 'Vladimir Putin', leader_title = 'President' WHERE id = 3;
UPDATE earth2026_countries SET ai_leader_id = 5, leader_name = 'Rishi Sunak', leader_title = 'Prime Minister' WHERE id = 4;
UPDATE earth2026_countries SET ai_leader_id = 6, leader_name = 'Olaf Scholz', leader_title = 'Chancellor' WHERE id = 5;
UPDATE earth2026_countries SET ai_leader_id = 7, leader_name = 'Emmanuel Macron', leader_title = 'President' WHERE id = 6;
UPDATE earth2026_countries SET ai_leader_id = 8, leader_name = 'Fumio Kishida', leader_title = 'Prime Minister' WHERE id = 7;
UPDATE earth2026_countries SET ai_leader_id = 9, leader_name = 'Narendra Modi', leader_title = 'Prime Minister' WHERE id = 8;
UPDATE earth2026_countries SET ai_leader_id = 10, leader_name = 'Luiz Inacio Lula da Silva', leader_title = 'President' WHERE id = 9;
UPDATE earth2026_countries SET ai_leader_id = 11, leader_name = 'Justin Trudeau', leader_title = 'Prime Minister' WHERE id = 10;
UPDATE earth2026_countries SET ai_leader_id = 12, leader_name = 'Anthony Albanese', leader_title = 'Prime Minister' WHERE id = 11;
UPDATE earth2026_countries SET ai_leader_id = 13, leader_name = 'Yoon Suk Yeol', leader_title = 'President' WHERE id = 12;
UPDATE earth2026_countries SET ai_leader_id = 14, leader_name = 'Claudia Sheinbaum', leader_title = 'President' WHERE id = 13;
UPDATE earth2026_countries SET ai_leader_id = 15, leader_name = 'Giorgia Meloni', leader_title = 'Prime Minister' WHERE id = 14;
UPDATE earth2026_countries SET ai_leader_id = 16, leader_name = 'Pedro Sanchez', leader_title = 'Prime Minister' WHERE id = 15;
UPDATE earth2026_countries SET ai_leader_id = 17, leader_name = 'Bola Tinubu', leader_title = 'President' WHERE id = 16;
UPDATE earth2026_countries SET ai_leader_id = 18, leader_name = 'Salman bin Abdulaziz', leader_title = 'King' WHERE id = 17;
UPDATE earth2026_countries SET ai_leader_id = 19, leader_name = 'Recep Tayyip Erdogan', leader_title = 'President' WHERE id = 18;
UPDATE earth2026_countries SET ai_leader_id = 20, leader_name = 'Javier Milei', leader_title = 'President' WHERE id = 19;
UPDATE earth2026_countries SET ai_leader_id = 21, leader_name = 'Cyril Ramaphosa', leader_title = 'President' WHERE id = 20;

-- 3. Seed AI characters for region governors (12 regions)
-- Region IDs match V33 seed order: CA=1, TX=2, NY=3, FL=4, Bavaria=5, Berlin=6,
-- Tokyo=7, Osaka=8, Guangdong=9, Shanghai=10, Maharashtra=11, Karnataka=12

INSERT INTO ai_characters (name, role, empire_name, linked_player_id, active, can_promote_to_co_server, description)
VALUES
-- US: California
('Gavin Newsom', 'Governor', 'United States', NULL, TRUE, FALSE,
 'AI character representing the Governor of California, controlling state operations.'),
-- US: Texas
('Greg Abbott', 'Governor', 'United States', NULL, TRUE, FALSE,
 'AI character representing the Governor of Texas, controlling state operations.'),
-- US: New York
('Kathy Hochul', 'Governor', 'United States', NULL, TRUE, FALSE,
 'AI character representing the Governor of New York, controlling state operations.'),
-- US: Florida
('Ron DeSantis', 'Governor', 'United States', NULL, TRUE, FALSE,
 'AI character representing the Governor of Florida, controlling state operations.'),
-- Germany: Bavaria
('Markus Soder', 'Minister-President', 'Germany', NULL, TRUE, FALSE,
 'AI character representing the Minister-President of Bavaria, controlling state operations.'),
-- Germany: Berlin
('Kai Wegner', 'Regierender Burgermeister', 'Germany', NULL, TRUE, FALSE,
 'AI character representing the Governing Mayor of Berlin, controlling city-state operations.'),
-- Japan: Tokyo
('Yuriko Koike', 'Governor', 'Japan', NULL, TRUE, FALSE,
 'AI character representing the Governor of Tokyo, controlling prefecture operations.'),
-- Japan: Osaka
('Ichiro Matsui', 'Governor', 'Japan', NULL, TRUE, FALSE,
 'AI character representing the Governor of Osaka, controlling prefecture operations.'),
-- China: Guangdong
('Huang Kunhu', 'Governor', 'China', NULL, TRUE, FALSE,
 'AI character representing the Governor of Guangdong, controlling province operations.'),
-- China: Shanghai
('Gong Zheng', 'Party Secretary', 'China', NULL, TRUE, FALSE,
 'AI character representing the Party Secretary of Shanghai, controlling municipality operations.'),
-- India: Maharashtra
('Bhagat Singh Koshyari', 'Governor', 'India', NULL, TRUE, FALSE,
 'AI character representing the Governor of Maharashtra, controlling state operations.'),
-- India: Karnataka
('Vajubhai Vala', 'Governor', 'India', NULL, TRUE, FALSE,
 'AI character representing the Governor of Karnataka, controlling state operations.');

-- 4. Assign AI governors to regions
-- AI character IDs: CA=22, TX=23, NY=24, FL=25, Bavaria=26, Berlin=27,
-- Tokyo=28, Osaka=29, Guangdong=30, Shanghai=31, Maharashtra=32, Karnataka=33

UPDATE earth2026_regions SET ai_governor_id = 22, governor_name = 'Gavin Newsom', governor_title = 'Governor' WHERE id = 1;
UPDATE earth2026_regions SET ai_governor_id = 23, governor_name = 'Greg Abbott', governor_title = 'Governor' WHERE id = 2;
UPDATE earth2026_regions SET ai_governor_id = 24, governor_name = 'Kathy Hochul', governor_title = 'Governor' WHERE id = 3;
UPDATE earth2026_regions SET ai_governor_id = 25, governor_name = 'Ron DeSantis', governor_title = 'Governor' WHERE id = 4;
UPDATE earth2026_regions SET ai_governor_id = 26, governor_name = 'Markus Soder', governor_title = 'Minister-President' WHERE id = 5;
UPDATE earth2026_regions SET ai_governor_id = 27, governor_name = 'Kai Wegner', governor_title = 'Regierender Burgermeister' WHERE id = 6;
UPDATE earth2026_regions SET ai_governor_id = 28, governor_name = 'Yuriko Koike', governor_title = 'Governor' WHERE id = 7;
UPDATE earth2026_regions SET ai_governor_id = 29, governor_name = 'Ichiro Matsui', governor_title = 'Governor' WHERE id = 8;
UPDATE earth2026_regions SET ai_governor_id = 30, governor_name = 'Huang Kunhu', governor_title = 'Governor' WHERE id = 9;
UPDATE earth2026_regions SET ai_governor_id = 31, governor_name = 'Gong Zheng', governor_title = 'Party Secretary' WHERE id = 10;
UPDATE earth2026_regions SET ai_governor_id = 32, governor_name = 'Bhagat Singh Koshyari', governor_title = 'Governor' WHERE id = 11;
UPDATE earth2026_regions SET ai_governor_id = 33, governor_name = 'Vajubhai Vala', governor_title = 'Governor' WHERE id = 12;

-- 5. Update Earth planet AI overseer
-- Assign the UN Secretary-General AI (create if not exists)
INSERT INTO ai_characters (name, role, empire_name, linked_player_id, active, can_promote_to_co_server, description)
SELECT 34, 'UN Secretary-General', 'United Nations', NULL, TRUE, TRUE,
 'AI character representing the UN Secretary-General, overseeing all Earth 2026 planet operations.'
WHERE NOT EXISTS (SELECT 1 FROM ai_characters WHERE name = 'UN Secretary-General AI');

UPDATE earth2026_planets SET ai_overseer_id = 34, overseer_name = 'UN Secretary-General AI'
WHERE id = 1 AND NOT EXISTS (SELECT 1 FROM ai_characters WHERE name = 'UN Secretary-General AI');

-- V33: Add Earth 2026 planet regions, countries, economy, jobs, contracts, and planets

-- 1. Planets table
CREATE TABLE IF NOT EXISTS earth2026_planets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL UNIQUE,
    planet_type VARCHAR(50) NOT NULL,
    base_tech_level VARCHAR(20) NOT NULL,
    time_period VARCHAR(50) NOT NULL,
    total_population_billions DOUBLE NOT NULL,
    country_count INT NOT NULL DEFAULT 0,
    region_count INT NOT NULL DEFAULT 0,
    location_count INT NOT NULL DEFAULT 0,
    ai_overseer_id BIGINT,
    overseer_name VARCHAR(200),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    is_initialized BOOLEAN NOT NULL DEFAULT FALSE,
    description TEXT,
    avg_temperature_c DOUBLE,
    gravity DOUBLE NOT NULL DEFAULT 1.0,
    atmosphere VARCHAR(100),
    radius_km DOUBLE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. Countries table
CREATE TABLE IF NOT EXISTS earth2026_countries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    iso_code VARCHAR(2) NOT NULL UNIQUE,
    iso_code_3 VARCHAR(3),
    capital_city VARCHAR(100),
    government_type VARCHAR(100) NOT NULL,
    political_ideology VARCHAR(100),
    population_millions DOUBLE NOT NULL,
    gdp_trillions DOUBLE NOT NULL,
    gdp_per_capita INT NOT NULL,
    tech_level VARCHAR(20) NOT NULL,
    military_strength INT NOT NULL,
    economic_power INT NOT NULL,
    diplomatic_influence INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    ai_leader_id BIGINT,
    leader_name VARCHAR(200),
    leader_title VARCHAR(100),
    planet_id BIGINT NOT NULL,
    description TEXT,
    continent VARCHAR(100),
    official_languages VARCHAR(500),
    currency VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_earth2026_country_name (name),
    INDEX idx_earth2026_country_planet (planet_id)
);

-- 3. Regions table
CREATE TABLE IF NOT EXISTS earth2026_regions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    type VARCHAR(50) NOT NULL,
    country_id BIGINT NOT NULL,
    population_millions DOUBLE,
    gdp_billions DOUBLE,
    tech_level VARCHAR(20),
    capital_city VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    ai_governor_id BIGINT,
    governor_name VARCHAR(200),
    governor_title VARCHAR(100),
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_earth2026_region_name (name),
    INDEX idx_earth2026_region_country (country_id)
);

-- 4. Economies table
CREATE TABLE IF NOT EXISTS earth2026_economies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    planet_id BIGINT NOT NULL UNIQUE,
    gdp_trillions DOUBLE NOT NULL,
    growth_rate DOUBLE,
    inflation_rate DOUBLE,
    unemployment_rate DOUBLE,
    market_size INT NOT NULL,
    trade_openness INT NOT NULL,
    technology_index INT NOT NULL,
    resource_abundance INT NOT NULL,
    financial_stability INT NOT NULL,
    price_level DOUBLE NOT NULL,
    market_condition VARCHAR(50) NOT NULL,
    dominant_industries VARCHAR(500),
    export_commodities VARCHAR(500),
    import_commodities VARCHAR(500),
    trade_restricted BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_earth2026_economy_planet (planet_id)
);

-- 5. Jobs table
CREATE TABLE IF NOT EXISTS earth2026_jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    category VARCHAR(100) NOT NULL,
    job_level INT NOT NULL,
    min_salary INT NOT NULL,
    max_salary INT NOT NULL,
    min_char_level INT NOT NULL,
    min_int INT,
    min_str INT,
    min_cha INT,
    xp_per_turn INT NOT NULL,
    planet_id BIGINT NOT NULL,
    country_id BIGINT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    description TEXT,
    benefits VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_earth2026_job_title (title),
    INDEX idx_earth2026_job_category (category),
    INDEX idx_earth2026_job_planet (planet_id)
);

-- 6. Contracts table
CREATE TABLE IF NOT EXISTS earth2026_contracts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    type VARCHAR(100) NOT NULL,
    description TEXT,
    payment_usd INT NOT NULL,
    xp_reward INT NOT NULL,
    min_level INT NOT NULL,
    min_str INT,
    min_int INT,
    min_cha INT,
    difficulty INT NOT NULL,
    duration_turns INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'available',
    assignee_id BIGINT,
    planet_id BIGINT NOT NULL,
    country_id BIGINT,
    location_id BIGINT,
    offering_org VARCHAR(200),
    expiry_date TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    INDEX idx_earth2026_contract_status (status),
    INDEX idx_earth2026_contract_planet (planet_id),
    INDEX idx_earth2026_contract_assignee (assignee_id)
);

-- 7. Seed Earth planet
INSERT INTO earth2026_planets (name, planet_type, base_tech_level, time_period, total_population_billions,
    country_count, region_count, location_count, overseer_name, description, avg_temperature_c, gravity, atmosphere, radius_km, is_initialized)
VALUES ('Earth', 'Terrestrial', 'TL5', 'MODERN', 8.0, 0, 0, 8, 'UN Secretary-General AI',
    'Modern Earth in 2026 - the hub planet for all DoraMCU games. Characters start here before transitioning to other game galaxies.',
    15.0, 1.0, 'Nitrogen-Oxygen', 6371.0, TRUE);

-- 8. Seed major countries (Planet ID = 1 for Earth)
SET @earth_planet_id = 1;

INSERT INTO earth2026_countries (name, iso_code, iso_code_3, capital_city, government_type, political_ideology,
    population_millions, gdp_trillions, gdp_per_capita, tech_level, military_strength, economic_power,
    diplomatic_influence, planet_id, continent, official_languages, currency, leader_title, description)
VALUES
('United States', 'US', 'USA', 'Washington D.C.', 'Federal Republic', 'Liberal Democracy',
 340.0, 27.36, 81000, 'TL5', 10, 10, 10, @earth_planet_id, 'North America', 'English', 'USD - US Dollar', 'President',
 'The most powerful nation on Earth with the largest economy and military.'),
('China', 'CN', 'CHN', 'Beijing', 'People''s Republic', 'Authoritarian',
 1412.0, 19.37, 13700, 'TL5', 9, 9, 8, @earth_planet_id, 'Asia', 'Mandarin Chinese', 'CNY - Yuan', 'President',
 'The most populous nation with rapid technological advancement.'),
('Russia', 'RU', 'RUS', 'Moscow', 'Federal Semi-Presidential Republic', 'Authoritarian',
 144.0, 2.24, 15600, 'TL5', 9, 5, 7, @earth_planet_id, 'Europe/Asia', 'Russian', 'RUB - Ruble', 'President',
 'The largest country by area with significant military and resource power.'),
('United Kingdom', 'GB', 'GBR', 'London', 'Constitutional Monarchy', 'Liberal Democracy',
 67.0, 3.35, 50000, 'TL5', 7, 7, 8, @earth_planet_id, 'Europe', 'English', 'GBP - Pound Sterling', 'Prime Minister',
 'A major financial and cultural center with strong diplomatic influence.'),
('Germany', 'DE', 'DEU', 'Berlin', 'Federal Parliamentary Republic', 'Social Democracy',
 84.0, 4.43, 52700, 'TL5', 6, 8, 8, @earth_planet_id, 'Europe', 'German', 'EUR - Euro', 'Chancellor',
 'The largest economy in Europe with advanced manufacturing and technology.'),
('France', 'FR', 'FRA', 'Paris', 'Semi-Presidential Republic', 'Social Democracy',
 68.0, 3.05, 44800, 'TL5', 7, 7, 8, @earth_planet_id, 'Europe', 'French', 'EUR - Euro', 'President',
 'A major European power with strong cultural and diplomatic influence.'),
('Japan', 'JP', 'JPN', 'Tokyo', 'Constitutional Monarchy', 'Liberal Democracy',
 125.0, 4.23, 33800, 'TL5', 5, 8, 7, @earth_planet_id, 'Asia', 'Japanese', 'JPY - Yen', 'Prime Minister',
 'A technological leader with advanced robotics and electronics industry.'),
('India', 'IN', 'IND', 'New Delhi', 'Federal Parliamentary Republic', 'Liberal Democracy',
 1428.0, 4.31, 3000, 'TL5', 7, 6, 7, @earth_planet_id, 'Asia', 'Hindi, English', 'INR - Rupee', 'Prime Minister',
 'The most populous country with a rapidly growing economy and tech sector.'),
('Brazil', 'BR', 'BRA', 'Brasilia', 'Federal Republic', 'Liberal Democracy',
 214.0, 2.17, 10100, 'TL5', 6, 6, 6, @earth_planet_id, 'South America', 'Portuguese', 'BRL - Real', 'President',
 'The largest South American nation with vast natural resources.'),
('Canada', 'CA', 'CAN', 'Ottawa', 'Federal Parliamentary Democracy', 'Social Democracy',
 40.0, 2.14, 53500, 'TL5', 5, 6, 6, @earth_planet_id, 'North America', 'English, French', 'CAD - Canadian Dollar', 'Prime Minister',
 'A resource-rich nation with high quality of life and strong ties to the US.'),
('Australia', 'AU', 'AUS', 'Canberra', 'Federal Parliamentary Democracy', 'Liberal Democracy',
 26.0, 1.72, 66200, 'TL5', 4, 6, 5, @earth_planet_id, 'Oceania', 'English', 'AUD - Australian Dollar', 'Prime Minister',
 'An island continent with abundant resources and high living standards.'),
('South Korea', 'KR', 'KOR', 'Seoul', 'Presidential Republic', 'Liberal Democracy',
 52.0, 1.81, 34800, 'TL5', 6, 7, 6, @earth_planet_id, 'Asia', 'Korean', 'KRW - Won', 'President',
 'A technological powerhouse with advanced electronics and shipbuilding.'),
('Mexico', 'MX', 'MEX', 'Mexico City', 'Federal Republic', 'Liberal Democracy',
 130.0, 1.41, 10800, 'TL5', 4, 5, 5, @earth_planet_id, 'North America', 'Spanish', 'MXN - Peso', 'President',
 'A major North American economy with strong cultural influence.'),
('Italy', 'IT', 'ITA', 'Rome', 'Parliamentary Republic', 'Social Democracy',
 59.0, 2.25, 38100, 'TL5', 5, 6, 6, @earth_planet_id, 'Europe', 'Italian', 'EUR - Euro', 'Prime Minister',
 'A cultural and economic leader in Southern Europe.'),
('Spain', 'ES', 'ESP', 'Madrid', 'Parliamentary Monarchy', 'Social Democracy',
 47.0, 1.58, 33600, 'TL5', 5, 5, 5, @earth_planet_id, 'Europe', 'Spanish', 'EUR - Euro', 'Prime Minister',
 'A major European nation with strong tourism and cultural heritage.'),
('Nigeria', 'NG', 'NGA', 'Abuja', 'Federal Republic', 'Liberal Democracy',
 223.0, 0.47, 2100, 'TL4', 5, 3, 5, @earth_planet_id, 'Africa', 'English', 'NGN - Naira', 'President',
 'The most populous African nation with significant oil resources.'),
('Saudi Arabia', 'SA', 'SAU', 'Riyadh', 'Absolute Monarchy', 'Authoritarian',
 36.0, 1.11, 30800, 'TL5', 6, 5, 6, @earth_planet_id, 'Asia', 'Arabic', 'SAR - Riyal', 'King',
 'The world''s largest oil exporter with significant economic power.'),
('Turkey', 'TR', 'TUR', 'Ankara', 'Presidential Republic', 'Authoritarian',
 85.0, 0.91, 10700, 'TL5', 7, 5, 6, @earth_planet_id, 'Europe/Asia', 'Turkish', 'TRY - Lira', 'President',
 'A strategic nation bridging Europe and Asia with a strong military.'),
('Argentina', 'AR', 'ARG', 'Buenos Aires', 'Presidential Republic', 'Liberal Democracy',
 46.0, 0.65, 14100, 'TL5', 4, 4, 4, @earth_planet_id, 'South America', 'Spanish', 'ARS - Peso', 'President',
 'A South American nation with rich agricultural resources.'),
('South Africa', 'ZA', 'ZAF', 'Pretoria', 'Parliamentary Republic', 'Liberal Democracy',
 60.0, 0.41, 6800, 'TL5', 4, 4, 5, @earth_planet_id, 'Africa', '11 Official Languages', 'ZAR - Rand', 'President',
 'The most industrialized nation in Africa with diverse resources.');

-- Update Earth planet country count
UPDATE earth2026_planets SET country_count = 20 WHERE id = @earth_planet_id;

-- 9. Seed regions for key countries
INSERT INTO earth2026_regions (name, type, country_id, population_millions, gdp_billions, capital_city, governor_title)
VALUES
-- US States
('California', 'State', 1, 39.0, 3500.0, 'Sacramento', 'Governor'),
('Texas', 'State', 1, 30.0, 2300.0, 'Austin', 'Governor'),
('New York', 'State', 1, 19.0, 1900.0, 'Albany', 'Governor'),
('Florida', 'State', 1, 22.0, 1400.0, 'Tallahassee', 'Governor'),
-- German States
('Bavaria', 'State', 5, 13.0, 600.0, 'Munich', 'Minister-President'),
('Berlin', 'State', 5, 3.7, 200.0, 'Berlin', 'Regierender Burgermeister'),
-- Japanese Prefectures
('Tokyo', 'Prefecture', 7, 14.0, 1500.0, 'Tokyo', 'Governor'),
('Osaka', 'Prefecture', 7, 8.8, 800.0, 'Osaka', 'Governor'),
-- Chinese Provinces
('Guangdong', 'Province', 2, 126.0, 1800.0, 'Guangzhou', 'Governor'),
('Shanghai', 'Municipality', 2, 25.0, 1200.0, 'Shanghai', 'Party Secretary'),
-- Indian States
('Maharashtra', 'State', 8, 123.0, 400.0, 'Mumbai', 'Governor'),
('Karnataka', 'State', 8, 68.0, 300.0, 'Bengaluru', 'Governor');

-- Update Earth planet region count
UPDATE earth2026_planets SET region_count = 12 WHERE id = @earth_planet_id;

-- 10. Seed Earth economy
INSERT INTO earth2026_economies (planet_id, gdp_trillions, growth_rate, inflation_rate, unemployment_rate,
    market_size, trade_openness, technology_index, resource_abundance, financial_stability,
    price_level, market_condition, dominant_industries, export_commodities, import_commodities)
VALUES (@earth_planet_id, 105.0, 3.0, 2.5, 5.0, 8, 7, 8, 7, 7, 1.0, 'stable',
 'Technology,Finance,Manufacturing,Healthcare,Energy',
 'Technology,Pharmaceuticals,Agriculture,Machinery',
 'Energy,Rare Earth Metals,Luxury Goods');

-- 11. Seed jobs
INSERT INTO earth2026_jobs (title, category, job_level, min_salary, max_salary, min_char_level, min_int, min_str, min_cha, xp_per_turn, planet_id, description, benefits)
VALUES
('Software Engineer', 'Technology', 5, 80000, 200000, 3, 13, NULL, NULL, 50, @earth_planet_id, 'Design and develop software systems.', 'Health insurance, 401k, stock options'),
('CEO', 'Business', 10, 500000, 5000000, 8, 15, NULL, 15, 200, @earth_planet_id, 'Chief Executive Officer of a corporation.', 'Full executive package'),
('Police Officer', 'Law Enforcement', 3, 45000, 90000, 1, NULL, 12, NULL, 30, @earth_planet_id, 'Protect and serve the community.', 'Pension, health insurance'),
('Doctor', 'Healthcare', 8, 150000, 400000, 5, 14, NULL, 12, 100, @earth_planet_id, 'Provide medical care to patients.', 'Malpractice insurance, retirement'),
('Teacher', 'Education', 4, 40000, 80000, 2, 12, NULL, 10, 25, @earth_planet_id, 'Educate students.', 'Pension, summer break'),
('Military General', 'Military', 10, 200000, 500000, 7, 12, 14, 12, 150, @earth_planet_id, 'Command military operations.', 'Full military benefits'),
('Diplomat', 'Government', 7, 100000, 250000, 4, 13, NULL, 14, 80, @earth_planet_id, 'Represent the nation abroad.', 'Diplomatic immunity, housing'),
('Scientist', 'Research', 7, 90000, 200000, 4, 15, NULL, NULL, 70, @earth_planet_id, 'Conduct research and development.', 'Research grants, conference travel'),
('Journalist', 'Media', 4, 35000, 100000, 2, 11, NULL, 12, 20, @earth_planet_id, 'Report news and investigate stories.', 'Press credentials'),
('Detective', 'Law Enforcement', 5, 50000, 120000, 2, 12, 10, 10, 40, @earth_planet_id, 'Investigate crimes.', 'Hazard pay, pension'),
('Pilot', 'Transportation', 6, 80000, 200000, 3, 11, 10, NULL, 60, @earth_planet_id, 'Fly aircraft.', 'Travel benefits, health insurance'),
('Chef', 'Hospitality', 4, 30000, 80000, 1, NULL, NULL, 10, 15, @earth_planet_id, 'Prepare and cook food.', 'Meals, tips'),
('Artist', 'Arts', 3, 20000, 100000, 1, NULL, NULL, 13, 10, @earth_planet_id, 'Create visual or performing art.', 'Flexible schedule'),
('Athlete', 'Sports', 6, 50000, 10000000, 1, NULL, 16, NULL, 50, @earth_planet_id, 'Compete in professional sports.', 'Sponsorships, endorsements'),
('Spy', 'Intelligence', 8, 100000, 500000, 5, 14, 12, 14, 120, @earth_planet_id, 'Conduct covert operations.', 'Classified benefits');

-- 12. Seed contracts
INSERT INTO earth2026_contracts (title, type, description, payment_usd, xp_reward, min_level, min_str, min_int, min_cha,
    difficulty, duration_turns, planet_id, offering_org, expiry_date)
VALUES
('Security Detail', 'Employment', 'Provide security for a high-profile event.', 5000, 100, 3, 12, NULL, NULL, 4, 2, @earth_planet_id, 'Wackenhut Corp', DATE_ADD(NOW(), INTERVAL 30 DAY)),
('Trade Negotiation', 'Freelance', 'Negotiate a trade deal between two corporations.', 10000, 200, 5, NULL, NULL, 14, 6, 3, @earth_planet_id, 'UN Trade Commission', DATE_ADD(NOW(), INTERVAL 60 DAY)),
('Investigation', 'Freelance', 'Investigate suspicious activity at a facility.', 3000, 80, 2, NULL, 12, NULL, 3, 1, @earth_planet_id, 'Private Detective Agency', DATE_ADD(NOW(), INTERVAL 14 DAY)),
('Military Patrol', 'Military', 'Patrol a border region for threats.', 8000, 150, 4, 12, NULL, NULL, 5, 2, @earth_planet_id, 'Department of Defense', DATE_ADD(NOW(), INTERVAL 45 DAY)),
('Medical Mission', 'Government', 'Provide medical aid in an underserved area.', 4000, 120, 3, NULL, 13, NULL, 4, 3, @earth_planet_id, 'WHO', DATE_ADD(NOW(), INTERVAL 90 DAY)),
('Tech Development', 'Employment', 'Develop a new software system for a corporation.', 15000, 250, 4, NULL, 14, NULL, 7, 5, @earth_planet_id, 'TechCorp Inc', DATE_ADD(NOW(), INTERVAL 120 DAY)),
('Diplomatic Mission', 'Government', 'Represent the nation at an international summit.', 20000, 300, 6, NULL, NULL, 15, 8, 4, @earth_planet_id, 'State Department', DATE_ADD(NOW(), INTERVAL 60 DAY)),
('Rescue Operation', 'Freelance', 'Rescue hostages from a dangerous location.', 25000, 400, 5, 14, NULL, 12, 9, 2, @earth_planet_id, 'Private Military Corp', DATE_ADD(NOW(), INTERVAL 30 DAY));

-- Update Earth planet location count (from existing locations)
UPDATE earth2026_planets SET location_count = (SELECT COUNT(*) FROM earth2026_locations) WHERE id = @earth_planet_id;

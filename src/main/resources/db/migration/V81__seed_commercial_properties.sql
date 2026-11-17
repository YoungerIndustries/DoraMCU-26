-- V81: Seed commercial property data

-- Warehouses (logistics hubs in port and industrial cities)
INSERT INTO commercial_properties
    (property_name, property_type, location_id, planet_id, square_footage, zoning_type,
     property_value, monthly_income, appreciation_rate, maintenance_cost,
     owner_id, owner_type, status, latitude, longitude, description)
VALUES
    ('Pacific Coast Logistics Center', 'WAREHOUSE', 1, 1, 150000, 'Industrial',
     2500000.00, 18000.00, 3.50, 2200.00,
     1, 'CORPORATION', 'AVAILABLE', 34.0522, -118.2437,
     'Large automated warehouse facility on the Pacific coast with direct port access and rail connections. Ideal for import/export operations.'),

    ('Shanghai Distribution Hub', 'WAREHOUSE', 1, 1, 200000, 'Industrial',
     3200000.00, 24000.00, 4.20, 3000.00,
     1, 'CORPORATION', 'AVAILABLE', 31.2304, 121.4737,
     'Modern distribution center in Shanghai with climate-controlled storage and automated sorting systems.'),

    ('Rotterdam Port Warehouse', 'WAREHOUSE', 1, 1, 180000, 'Industrial',
     2800000.00, 21000.00, 3.80, 2800.00,
     1, 'CORPORATION', 'AVAILABLE', 51.9244, 4.4777,
     'Strategic warehouse at Europe''s largest port with multimodal transport links and customs clearance facilities.');

-- Industrial Facilities (manufacturing and processing plants)
INSERT INTO commercial_properties
    (property_name, property_type, location_id, planet_id, square_footage, zoning_type,
     property_value, monthly_income, appreciation_rate, maintenance_cost,
     owner_id, owner_type, status, latitude, longitude, description)
VALUES
    ('Detroit Advanced Manufacturing Plant', 'INDUSTRIAL_FACILITY', 1, 1, 300000, 'Heavy Industrial',
     5000000.00, 35000.00, 2.80, 5500.00,
     1, 'CORPORATION', 'AVAILABLE', 42.3314, -83.0458,
     'State-of-the-art manufacturing facility with robotic assembly lines and clean room capabilities for precision engineering.'),

    ('Stuttgart Engineering Complex', 'INDUSTRIAL_FACILITY', 1, 1, 250000, 'Heavy Industrial',
     4500000.00, 32000.00, 3.10, 4800.00,
     1, 'CORPORATION', 'AVAILABLE', 48.7758, 9.1829,
     'High-tech engineering complex specializing in automotive and aerospace component manufacturing with R&D laboratories.'),

    ('Tokyo Electronics Factory', 'INDUSTRIAL_FACILITY', 1, 1, 220000, 'Industrial',
     4800000.00, 38000.00, 3.60, 5200.00,
     1, 'CORPORATION', 'AVAILABLE', 35.6762, 139.6503,
     'Semiconductor and electronics manufacturing facility with advanced clean rooms and testing laboratories.');

-- Retail Spaces (high-traffic commercial areas)
INSERT INTO commercial_properties
    (property_name, property_type, location_id, planet_id, square_footage, zoning_type,
     property_value, monthly_income, appreciation_rate, maintenance_cost,
     owner_id, owner_type, status, latitude, longitude, description)
VALUES
    ('Manhattan Premium Retail Plaza', 'RETAIL_SPACE', 1, 1, 50000, 'Commercial',
     8000000.00, 65000.00, 4.50, 8000.00,
     1, 'CORPORATION', 'AVAILABLE', 40.7128, -74.0060,
     'Prime retail space in Manhattan with high foot traffic, luxury finishes, and prominent signage opportunities.'),

    ('London Oxford Street Retail Complex', 'RETAIL_SPACE', 1, 1, 45000, 'Commercial',
     7500000.00, 58000.00, 4.30, 7500.00,
     1, 'CORPORATION', 'AVAILABLE', 51.5074, -0.1278,
     'Iconic retail location on Oxford Street with multiple storefronts and underground storage.'),

    ('Dubai Mall Extension Wing', 'RETAIL_SPACE', 1, 1, 60000, 'Commercial',
     9000000.00, 72000.00, 5.00, 9500.00,
     1, 'CORPORATION', 'AVAILABLE', 25.2048, 55.2708,
     'Luxury retail extension in Dubai with direct metro access and premium tenant mix including flagship stores.');

-- Office Parks (corporate headquarters and business centers)
INSERT INTO commercial_properties
    (property_name, property_type, location_id, planet_id, square_footage, zoning_type,
     property_value, monthly_income, appreciation_rate, maintenance_cost,
     owner_id, owner_type, status, latitude, longitude, description)
VALUES
    ('Silicon Valley Tech Campus', 'OFFICE_PARK', 1, 1, 400000, 'Office',
     12000000.00, 95000.00, 5.20, 12000.00,
     1, 'CORPORATION', 'AVAILABLE', 37.3875, -122.0575,
     'Modern tech campus with open-plan offices, conference centers, and on-site amenities including cafeterias and fitness centers.'),

    ('Singapore Business Bay Tower', 'OFFICE_PARK', 1, 1, 350000, 'Office',
     15000000.00, 120000.00, 5.50, 15000.00,
     1, 'CORPORATION', 'AVAILABLE', 1.3521, 103.8198,
     'Grade-A office tower in Singapore''s financial district with panoramic views and smart building technology.'),

    ('Zurich Financial District Complex', 'OFFICE_PARK', 1, 1, 280000, 'Office',
     11000000.00, 88000.00, 4.00, 11000.00,
     1, 'CORPORATION', 'AVAILABLE', 47.3769, 8.5417,
     'Premium office complex in Zurich''s banking district with secure data centers and private banking facilities.');

-- Mixed-Use Developments (combined residential, commercial, and retail)
INSERT INTO commercial_properties
    (property_name, property_type, location_id, planet_id, square_footage, zoning_type,
     property_value, monthly_income, appreciation_rate, maintenance_cost,
     owner_id, owner_type, status, latitude, longitude, description)
VALUES
    ('New York Hudson Yards Extension', 'MIXED_USE', 1, 1, 500000, 'Mixed-Use',
     25000000.00, 180000.00, 4.80, 22000.00,
     1, 'CORPORATION', 'AVAILABLE', 40.7537, -74.0027,
     'Mixed-use development combining luxury apartments, retail spaces, and office towers with public plazas and parks.'),

    ('Tokyo Shibuya Crossings Complex', 'MIXED_USE', 1, 1, 450000, 'Mixed-Use',
     22000000.00, 165000.00, 5.10, 20000.00,
     1, 'CORPORATION', 'AVAILABLE', 35.6595, 139.7004,
     'Iconic mixed-use development at Shibuya featuring retail, dining, offices, and entertainment with direct station access.'),

    ('London Canary Wharf Waterfront', 'MIXED_USE', 1, 1, 420000, 'Mixed-Use',
     20000000.00, 155000.00, 4.60, 19000.00,
     1, 'CORPORATION', 'AVAILABLE', 51.5054, -0.0235,
     'Waterfront mixed-use complex in Canary Wharf with office towers, residential apartments, and a shopping mall with marina views.');

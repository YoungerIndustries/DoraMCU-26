-- V100: Seed Earth 2026 Game Objects for 3D World
-- Populates the game_object table with objects for the Younger Industries HQ and surrounding area
-- These objects are referenced by FpsMap scene object placements via UUID

-- ============================================
-- EARTH 2026: BUILDINGS
-- ============================================

INSERT INTO game_object (object_uuid, object_type, object_subtype, name, description, game_module, model_3d_path, texture_path, scale_factor, color_hex, created_at, updated_at)
VALUES 
('earth2026-building-hq', 'building', 'headquarters', 'Younger Industries HQ', 
 'The main headquarters building of Younger Industries in San Diego',
 'Earth2026', 'models/earth2026/hq.glb', 'textures/buildings/modern.png', 1.0, '#4A5568', NOW(), NOW()),

('earth2026-building-apartment', 'building', 'residential', 'Younger Industries Residences',
 'The residential apartment building for corporation employees',
 'Earth2026', 'models/earth2026/apartment.glb', 'textures/buildings/modern.png', 1.0, '#718096', NOW(), NOW()),

('earth2026-building-office', 'building', 'office', 'Office Building',
 'A standard office building in the Younger Industries complex',
 'Earth2026', 'models/earth2026/office.glb', 'textures/buildings/industrial.png', 1.0, '#A0AEC0', NOW(), NOW()),

('earth2026-building-warehouse', 'building', 'warehouse', 'Warehouse',
 'Industrial warehouse for storage and logistics',
 'Earth2026', 'models/earth2026/warehouse.glb', 'textures/buildings/industrial.png', 1.0, '#718096', NOW(), NOW()),

('earth2026-building-bunker', 'building', 'bunker', 'Secure Bunker',
 'Underground secure facility for sensitive operations',
 'Earth2026', 'models/earth2026/bunker.glb', 'textures/buildings/bunker.png', 1.0, '#4A5568', NOW(), NOW());

-- ============================================
-- EARTH 2026: OFFICE FURNITURE
-- ============================================

INSERT INTO game_object (object_uuid, object_type, object_subtype, name, description, game_module, model_3d_path, texture_path, scale_factor, color_hex, created_at, updated_at)
VALUES
('earth2026-desk-executive', 'desk', 'executive', 'Executive Desk',
 'Large executive desk with built-in terminal',
 'Earth2026', 'models/earth2026/desk_executive.glb', 'textures/buildings/modern.png', 1.0, '#8B6914', NOW(), NOW()),

('earth2026-desk-standard', 'desk', 'standard', 'Standard Desk',
 'Standard office desk with monitor stand',
 'Earth2026', 'models/earth2026/desk_standard.glb', 'textures/buildings/modern.png', 1.0, '#A0522D', NOW(), NOW()),

('earth2026-chair-office', 'chair', 'office', 'Office Chair',
 'Ergonomic office chair',
 'Earth2026', 'models/earth2026/chair_office.glb', NULL, 1.0, '#2D3748', NOW(), NOW()),

('earth2026-chair-executive', 'chair', 'executive', 'Executive Chair',
 'Premium leather executive chair',
 'Earth2026', 'models/earth2026/chair_executive.glb', NULL, 1.0, '#1A202C', NOW(), NOW()),

('earth2026-terminal-holographic', 'terminal', 'holographic', 'Holographic Terminal',
 'Advanced holographic display terminal',
 'Earth2026', 'models/earth2026/terminal_holo.glb', NULL, 1.0, '#00FFFF', NOW(), NOW()),

('earth2026-terminal-secure', 'terminal', 'secure', 'Secure Terminal',
 'Encrypted terminal for classified operations',
 'Earth2026', 'models/earth2026/terminal_secure.glb', NULL, 1.0, '#FF0000', NOW(), NOW()),

('earth2026-terminal-standard', 'terminal', 'standard', 'Standard Terminal',
 'Standard computer terminal',
 'Earth2026', 'models/earth2026/terminal_standard.glb', NULL, 1.0, '#4A5568', NOW(), NOW()),

('earth2026-filing-cabinet', 'filing_cabinet', 'steel', 'Steel Filing Cabinet',
 'Heavy-duty steel filing cabinet',
 'Earth2026', 'models/earth2026/filing_cabinet.glb', NULL, 1.0, '#696969', NOW(), NOW()),

('earth2026-bookshelf', 'bookshelf', 'wood', 'Wooden Bookshelf',
 'Wooden bookshelf with documents and reference materials',
 'Earth2026', 'models/earth2026/bookshelf.glb', NULL, 1.0, '#8B6914', NOW(), NOW()),

('earth2026-coffee-station', 'coffee_station', 'cafe', 'Coffee Station',
 'Coffee and refreshment station',
 'Earth2026', 'models/earth2026/coffee_station.glb', NULL, 1.0, '#654321', NOW(), NOW());

-- ============================================
-- EARTH 2026: OUTDOOR OBJECTS
-- ============================================

INSERT INTO game_object (object_uuid, object_type, object_subtype, name, description, game_module, model_3d_path, texture_path, scale_factor, color_hex, created_at, updated_at)
VALUES
('earth2026-tree-palm', 'tree', 'palm', 'Palm Tree',
 'Palm tree typical of San Diego climate',
 'Earth2026', 'models/earth2026/tree_palm.glb', NULL, 1.0, '#228B22', NOW(), NOW()),

('earth2026-tree-oak', 'tree', 'oak', 'Oak Tree',
 'Large oak tree in the office park',
 'Earth2026', 'models/earth2026/tree_oak.glb', NULL, 1.0, '#2E8B57', NOW(), NOW()),

('earth2026-tree-bush', 'tree', 'bush', 'Landscaping Bush',
 'Decorative landscaping bush',
 'Earth2026', 'models/earth2026/tree_bush.glb', NULL, 1.0, '#32CD32', NOW(), NOW()),

('earth2026-street-lamp', 'street_lamp', 'led', 'LED Street Lamp',
 'Modern LED street lamp',
 'Earth2026', 'models/earth2026/street_lamp.glb', NULL, 1.0, '#FFAA00', NOW(), NOW()),

('earth2026-bench', 'bench', 'park', 'Park Bench',
 'Outdoor park bench',
 'Earth2026', 'models/earth2026/bench.glb', NULL, 1.0, '#8B4513', NOW(), NOW()),

('earth2026-bench-modern', 'bench', 'modern', 'Modern Bench',
 'Sleek modern bench for the office park',
 'Earth2026', 'models/earth2026/bench_modern.glb', NULL, 1.0, '#A0AEC0', NOW(), NOW()),

('earth2026-rock-landscape', 'rock', 'landscape', 'Landscape Rock',
 'Decorative landscape rock',
 'Earth2026', 'models/earth2026/rock.glb', NULL, 1.0, '#808080', NOW(), NOW()),

('earth2026-fountain', 'fountain', 'plaza', 'Plaza Fountain',
 'Decorative fountain in the plaza',
 'Earth2026', 'models/earth2026/fountain.glb', NULL, 1.0, '#4169E1', NOW(), NOW()),

('earth2026-sign-hq', 'sign', 'hq', 'HQ Entrance Sign',
 'Younger Industries HQ entrance sign',
 'Earth2026', 'models/earth2026/sign_hq.glb', NULL, 1.0, '#1A202C', NOW(), NOW()),

('earth2026-sign-directional', 'sign', 'directional', 'Directional Sign',
 'Directional signpost',
 'Earth2026', 'models/earth2026/sign_directional.glb', NULL, 1.0, '#4A5568', NOW(), NOW());

-- ============================================
-- EARTH 2026: VEHICLES
-- ============================================

INSERT INTO game_object (object_uuid, object_type, object_subtype, name, description, game_module, model_3d_path, texture_path, scale_factor, color_hex, created_at, updated_at)
VALUES
('earth2026-vehicle-sedan', 'vehicle', 'sedan', 'Executive Sedan',
 'Luxury sedan for executive transport',
 'Earth2026', 'models/earth2026/vehicle_sedan.glb', 'textures/vehicles/truck.png', 1.0, '#1A202C', NOW(), NOW()),

('earth2026-vehicle-suv', 'vehicle', 'suv', 'Security SUV',
 'Armored SUV for security transport',
 'Earth2026', 'models/earth2026/vehicle_suv.glb', 'textures/vehicles/apc.png', 1.0, '#2D3748', NOW(), NOW()),

('earth2026-vehicle-helicopter', 'vehicle', 'helicopter', 'Executive Helicopter',
 'Helicopter for rapid executive transport',
 'Earth2026', 'models/earth2026/vehicle_helicopter.glb', 'textures/vehicles/apc.png', 1.0, '#4A5568', NOW(), NOW());

-- ============================================
-- EARTH 2026: DECORATIVE OBJECTS
-- ============================================

INSERT INTO game_object (object_uuid, object_type, object_subtype, name, description, game_module, model_3d_path, texture_path, scale_factor, color_hex, created_at, updated_at)
VALUES
('earth2026-planter', 'planter', 'concrete', 'Concrete Planter',
 'Large concrete planter with plants',
 'Earth2026', 'models/earth2026/planter.glb', NULL, 1.0, '#A0AEC0', NOW(), NOW()),

('earth2026-trash-can', 'trash_can', 'modern', 'Modern Trash Can',
 'Sleek modern trash receptacle',
 'Earth2026', 'models/earth2026/trash_can.glb', NULL, 1.0, '#718096', NOW(), NOW()),

('earth2026-bike-rack', 'bike_rack', 'steel', 'Bike Rack',
 'Steel bicycle parking rack',
 'Earth2026', 'models/earth2026/bike_rack.glb', NULL, 1.0, '#4A5568', NOW(), NOW()),

('earth2026-fire-hydrant', 'fire_hydrant', 'standard', 'Fire Hydrant',
 'Standard fire hydrant',
 'Earth2026', 'models/earth2026/fire_hydrant.glb', NULL, 1.0, '#FF0000', NOW(), NOW()),

('earth2026-mailbox', 'mailbox', 'cluster', 'Mail Cluster',
 'Office mail cluster box',
 'Earth2026', 'models/earth2026/mailbox.glb', NULL, 1.0, '#1A202C', NOW(), NOW()),

('earth2026-flag-pole', 'flag_pole', 'corporate', 'Corporate Flag Pole',
 'Flag pole with Younger Industries flag',
 'Earth2026', 'models/earth2026/flag_pole.glb', NULL, 1.0, '#C0C0C0', NOW(), NOW()),

('earth2026-statue', 'statue', 'founder', 'Founder Statue',
 'Statue of the company founder',
 'Earth2026', 'models/earth2026/statue.glb', NULL, 1.0, '#B8860B', NOW(), NOW()),

('earth2026-crates', 'crate', 'shipping', 'Shipping Crates',
 'Stacked shipping crates',
 'Earth2026', 'models/earth2026/crates.glb', NULL, 1.0, '#DEB887', NOW(), NOW()),

('earth2026-barrel', 'barrel', 'industrial', 'Industrial Barrel',
 'Industrial storage barrel',
 'Earth2026', 'models/earth2026/barrel.glb', NULL, 1.0, '#4A4A4A', NOW(), NOW());

-- ============================================
-- EARTH 2026: SECURITY OBJECTS
-- ============================================

INSERT INTO game_object (object_uuid, object_type, object_subtype, name, description, game_module, model_3d_path, texture_path, scale_factor, color_hex, created_at, updated_at)
VALUES
('earth2026-security-camera', 'security_camera', 'dome', 'Dome Security Camera',
 'Dome security camera',
 'Earth2026', 'models/earth2026/security_camera.glb', NULL, 1.0, '#2D3748', NOW(), NOW()),

('earth2026-security-booth', 'security_booth', 'entrance', 'Security Booth',
 'Entrance security checkpoint booth',
 'Earth2026', 'models/earth2026/security_booth.glb', NULL, 1.0, '#4A5568', NOW(), NOW()),

('earth2026-barrier-gate', 'barrier_gate', 'vehicle', 'Vehicle Barrier Gate',
 'Automated vehicle access barrier',
 'Earth2026', 'models/earth2026/barrier_gate.glb', NULL, 1.0, '#FFD700', NOW(), NOW()),

('earth2026-fence', 'fence', 'perimeter', 'Perimeter Fence',
 'Security perimeter fence',
 'Earth2026', 'models/earth2026/fence.glb', NULL, 1.0, '#718096', NOW(), NOW());

-- ============================================
-- EARTH 2026: PORTAL SYSTEM
-- ============================================

INSERT INTO game_object (object_uuid, object_type, object_subtype, name, description, game_module, model_3d_path, texture_path, scale_factor, color_hex, created_at, updated_at)
VALUES
('earth2026-portal-frame', 'portal', 'frame', 'Portal Frame',
 'Interdimensional portal frame structure',
 'Earth2026', 'models/earth2026/portal_frame.glb', NULL, 1.0, '#8A2BE2', NOW(), NOW()),

('earth2026-portal-console', 'portal', 'console', 'Portal Control Console',
 'Control console for portal operations',
 'Earth2026', 'models/earth2026/portal_console.glb', NULL, 1.0, '#4A0080', NOW(), NOW());

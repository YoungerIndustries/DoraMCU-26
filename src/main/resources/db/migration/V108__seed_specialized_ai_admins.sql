-- V108: Seed specialized AI admin characters for 20 countries
-- Adds: Military Commanders, Diplomatic Envoys, Intelligence Directors, Science Officers
-- Total: 80 new AI characters (4 types × 20 countries)
-- Positioned in Milky Way galaxy near country leader capital cities

-- ============================================================================
-- Military Commanders (IDs 57-76): One per country, positioned near military bases
-- Color coding: Red in 3D view
-- ============================================================================

INSERT INTO ai_characters (id, name, role, empire_name, character_type, galaxy_name, pos_x, pos_y, pos_z, home_pos_x, home_pos_y, home_pos_z, ai_admin, active, description, disposition_score)
VALUES
-- US Military Commander (near Washington D.C. leader at 6.35, 6.93, -5.79)
(57, 'General James Morrison', 'Military Commander', 'United States', 'NPC', 'Milky Way', 6.85, 7.43, -5.29, 6.85, 7.43, -5.29, TRUE, TRUE, 'US Military Commander overseeing all armed forces operations and fleet deployments.', 0),
-- China Military Commander (near Beijing leader at 3.10, 6.94, 7.53)
(58, 'General Wei Zhang', 'Military Commander', 'China', 'NPC', 'Milky Way', 3.60, 7.44, 8.03, 3.60, 7.44, 8.03, TRUE, TRUE, 'China Military Commander responsible for PLA operations and territorial defense.', 0),
-- Russia Military Commander (near Moscow leader at 5.97, 8.80, -2.43)
(59, 'Admiral Dmitry Volkov', 'Military Commander', 'Russia', 'NPC', 'Milky Way', 6.47, 9.30, -1.93, 6.47, 9.30, -1.93, TRUE, TRUE, 'Russia Military Commander overseeing naval and ground forces.', 0),
-- UK Military Commander (near London leader at -0.48, 8.40, -7.07)
(60, 'General Sarah Thompson', 'Military Commander', 'United Kingdom', 'NPC', 'Milky Way', -0.98, 8.90, -6.57, -0.98, 8.90, -6.57, TRUE, TRUE, 'UK Military Commander responsible for British armed forces.', 0),
-- Germany Military Commander (near Berlin leader at 1.80, 8.53, -6.33)
(61, 'General Hans Mueller', 'Military Commander', 'Germany', 'NPC', 'Milky Way', 2.30, 9.03, -5.83, 2.30, 9.03, -5.83, TRUE, TRUE, 'Germany Military Commander overseeing Bundeswehr operations.', 0),
-- France Military Commander (near Paris leader at -0.09, 8.06, -6.78)
(62, 'General Marie Dubois', 'Military Commander', 'France', 'NPC', 'Milky Way', -0.59, 8.56, -6.28, -0.59, 8.56, -6.28, TRUE, TRUE, 'France Military Commander responsible for French armed forces.', 0),
-- Japan Military Commander (near Tokyo leader at 5.43, 6.34, 6.07)
(63, 'Admiral Takeshi Yamamoto', 'Military Commander', 'Japan', 'NPC', 'Milky Way', 5.93, 6.84, 6.57, 5.93, 6.84, 6.57, TRUE, TRUE, 'Japan Military Commander overseeing Self-Defense Forces.', 0),
-- India Military Commander (near New Delhi leader at 4.07, 5.29, 8.23)
(64, 'General Rajesh Kumar', 'Military Commander', 'India', 'NPC', 'Milky Way', 4.57, 5.79, 8.73, 4.57, 5.79, 8.73, TRUE, TRUE, 'India Military Commander responsible for Indian armed forces.', 0),
-- Brazil Military Commander (near Brasília leader at -7.77, -3.23, -5.83)
(65, 'General Carlos Silva', 'Military Commander', 'Brazil', 'NPC', 'Milky Way', -7.27, -2.73, -5.33, -7.27, -2.73, -5.33, TRUE, TRUE, 'Brazil Military Commander overseeing Brazilian armed forces.', 0),
-- Canada Military Commander (near Ottawa leader at 7.03, 7.63, -6.43)
(66, 'General Robert MacDonald', 'Military Commander', 'Canada', 'NPC', 'Milky Way', 7.53, 8.13, -5.93, 7.53, 8.13, -5.93, TRUE, TRUE, 'Canada Military Commander responsible for Canadian Forces.', 0),
-- Australia Military Commander (near Canberra leader at 5.87, -6.28, 5.33)
(67, 'General Emily Wilson', 'Military Commander', 'Australia', 'NPC', 'Milky Way', 6.37, -5.78, 5.83, 6.37, -5.78, 5.83, TRUE, TRUE, 'Australia Military Commander overseeing Australian Defense Force.', 0),
-- South Korea Military Commander (near Seoul leader at 4.73, 6.60, 6.67)
(68, 'General Park Ji-hoon', 'Military Commander', 'South Korea', 'NPC', 'Milky Way', 5.23, 7.10, 7.17, 5.23, 7.10, 7.17, TRUE, TRUE, 'South Korea Military Commander responsible for ROK Armed Forces.', 0),
-- Mexico Military Commander (near Mexico City leader at -9.13, 3.83, -4.03)
(69, 'General Miguel Hernandez', 'Military Commander', 'Mexico', 'NPC', 'Milky Way', -8.63, 4.33, -3.53, -8.63, 4.33, -3.53, TRUE, TRUE, 'Mexico Military Commander overseeing Mexican armed forces.', 0),
-- Italy Military Commander (near Rome leader at 1.67, 7.19, -6.86)
(70, 'General Marco Rossi', 'Military Commander', 'Italy', 'NPC', 'Milky Way', 2.17, 7.69, -6.36, 2.17, 7.69, -6.36, TRUE, TRUE, 'Italy Military Commander responsible for Italian armed forces.', 0),
-- Spain Military Commander (near Madrid leader at -1.15, 7.00, -6.89)
(71, 'General Carlos Garcia', 'Military Commander', 'Spain', 'NPC', 'Milky Way', -1.65, 7.50, -6.39, -1.65, 7.50, -6.39, TRUE, TRUE, 'Spain Military Commander overseeing Spanish armed forces.', 0),
-- Nigeria Military Commander (near Abuja leader at 0.79, 2.08, -10.37)
(72, 'General Adebayo Okafor', 'Military Commander', 'Nigeria', 'NPC', 'Milky Way', 1.29, 2.58, -9.87, 1.29, 2.58, -9.87, TRUE, TRUE, 'Nigeria Military Commander responsible for Nigerian armed forces.', 0),
-- Saudi Arabia Military Commander (near Riyadh leader at 7.63, 4.70, -5.37)
(73, 'General Ahmed Al-Saud', 'Military Commander', 'Saudi Arabia', 'NPC', 'Milky Way', 8.13, 5.20, -4.87, 8.13, 5.20, -4.87, TRUE, TRUE, 'Saudi Arabia Military Commander overseeing Saudi armed forces.', 0),
-- Turkey Military Commander (near Ankara leader at 5.23, 6.95, -6.13)
(74, 'General Mehmet Yilmaz', 'Military Commander', 'Turkey', 'NPC', 'Milky Way', 5.73, 7.45, -5.63, 5.73, 7.45, -5.63, TRUE, TRUE, 'Turkey Military Commander responsible for Turkish armed forces.', 0),
-- Argentina Military Commander (near Buenos Aires leader at -9.37, -6.18, -3.63)
(75, 'General Juan Fernandez', 'Military Commander', 'Argentina', 'NPC', 'Milky Way', -8.87, -5.68, -3.13, -8.87, -5.68, -3.13, TRUE, TRUE, 'Argentina Military Commander overseeing Argentine armed forces.', 0),
-- South Africa Military Commander (near Pretoria leader at 4.35, -4.85, -7.73)
(76, 'General Thabo Mokoena', 'Military Commander', 'South Africa', 'NPC', 'Milky Way', 4.85, -4.35, -7.23, 4.85, -4.35, -7.23, TRUE, TRUE, 'South Africa Military Commander responsible for South African National Defence Force.', 0)
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- Diplomatic Envoys (IDs 77-96): One per country, positioned near embassies
-- Color coding: Cyan in 3D view
-- ============================================================================

INSERT INTO ai_characters (id, name, role, empire_name, character_type, galaxy_name, pos_x, pos_y, pos_z, home_pos_x, home_pos_y, home_pos_z, ai_admin, active, description, disposition_score)
VALUES
-- US Diplomatic Envoy
(77, 'Ambassador Elizabeth Carter', 'Diplomatic Envoy', 'United States', 'NPC', 'Milky Way', 5.85, 6.43, -6.29, 5.85, 6.43, -6.29, TRUE, TRUE, 'US Diplomatic Envoy handling international relations and treaties.', 0),
-- China Diplomatic Envoy
(78, 'Ambassador Li Wei', 'Diplomatic Envoy', 'China', 'NPC', 'Milky Way', 2.60, 6.44, 7.03, 2.60, 6.44, 7.03, TRUE, TRUE, 'China Diplomatic Envoy responsible for foreign affairs and negotiations.', 0),
-- Russia Diplomatic Envoy
(79, 'Ambassador Olga Petrova', 'Diplomatic Envoy', 'Russia', 'NPC', 'Milky Way', 5.47, 8.30, -2.93, 5.47, 8.30, -2.93, TRUE, TRUE, 'Russia Diplomatic Envoy overseeing diplomatic relations.', 0),
-- UK Diplomatic Envoy
(80, 'Ambassador James Wilson', 'Diplomatic Envoy', 'United Kingdom', 'NPC', 'Milky Way', 0.02, 7.90, -7.57, 0.02, 7.90, -7.57, TRUE, TRUE, 'UK Diplomatic Envoy handling British foreign policy.', 0),
-- Germany Diplomatic Envoy
(81, 'Ambassador Anna Schmidt', 'Diplomatic Envoy', 'Germany', 'NPC', 'Milky Way', 1.30, 8.03, -6.83, 1.30, 8.03, -6.83, TRUE, TRUE, 'Germany Diplomatic Envoy responsible for EU and international relations.', 0),
-- France Diplomatic Envoy
(82, 'Ambassador Pierre Laurent', 'Diplomatic Envoy', 'France', 'NPC', 'Milky Way', 0.41, 7.56, -7.28, 0.41, 7.56, -7.28, TRUE, TRUE, 'France Diplomatic Envoy overseeing French diplomatic missions.', 0),
-- Japan Diplomatic Envoy
(83, 'Ambassador Yuki Tanaka', 'Diplomatic Envoy', 'Japan', 'NPC', 'Milky Way', 4.93, 5.84, 5.57, 4.93, 5.84, 5.57, TRUE, TRUE, 'Japan Diplomatic Envoy handling Asian and global relations.', 0),
-- India Diplomatic Envoy
(84, 'Ambassador Priya Sharma', 'Diplomatic Envoy', 'India', 'NPC', 'Milky Way', 3.57, 4.79, 7.73, 3.57, 4.79, 7.73, TRUE, TRUE, 'India Diplomatic Envoy responsible for South Asian diplomacy.', 0),
-- Brazil Diplomatic Envoy
(85, 'Ambassador Ana Costa', 'Diplomatic Envoy', 'Brazil', 'NPC', 'Milky Way', -8.27, -3.73, -6.33, -8.27, -3.73, -6.33, TRUE, TRUE, 'Brazil Diplomatic Envoy overseeing Latin American relations.', 0),
-- Canada Diplomatic Envoy
(86, 'Ambassador Catherine Brown', 'Diplomatic Envoy', 'Canada', 'NPC', 'Milky Way', 6.53, 7.13, -6.93, 6.53, 7.13, -6.93, TRUE, TRUE, 'Canada Diplomatic Envoy handling North American diplomacy.', 0),
-- Australia Diplomatic Envoy
(87, 'Ambassador David Chen', 'Diplomatic Envoy', 'Australia', 'NPC', 'Milky Way', 5.37, -6.78, 4.83, 5.37, -6.78, 4.83, TRUE, TRUE, 'Australia Diplomatic Envoy responsible for Pacific relations.', 0),
-- South Korea Diplomatic Envoy
(88, 'Ambassador Kim Soo-jin', 'Diplomatic Envoy', 'South Korea', 'NPC', 'Milky Way', 4.23, 6.10, 6.17, 4.23, 6.10, 6.17, TRUE, TRUE, 'South Korea Diplomatic Envoy overseeing East Asian diplomacy.', 0),
-- Mexico Diplomatic Envoy
(89, 'Ambassador Maria Rodriguez', 'Diplomatic Envoy', 'Mexico', 'NPC', 'Milky Way', -9.63, 3.33, -4.53, -9.63, 3.33, -4.53, TRUE, TRUE, 'Mexico Diplomatic Envoy handling Central American relations.', 0),
-- Italy Diplomatic Envoy
(90, 'Ambassador Sofia Bianchi', 'Diplomatic Envoy', 'Italy', 'NPC', 'Milky Way', 1.17, 6.69, -7.36, 1.17, 6.69, -7.36, TRUE, TRUE, 'Italy Diplomatic Envoy responsible for Mediterranean diplomacy.', 0),
-- Spain Diplomatic Envoy
(91, 'Ambassador Isabel Martinez', 'Diplomatic Envoy', 'Spain', 'NPC', 'Milky Way', -0.65, 6.50, -7.39, -0.65, 6.50, -7.39, TRUE, TRUE, 'Spain Diplomatic Envoy overseeing Iberian and African relations.', 0),
-- Nigeria Diplomatic Envoy
(92, 'Ambassador Chioma Nwosu', 'Diplomatic Envoy', 'Nigeria', 'NPC', 'Milky Way', 0.29, 1.58, -10.87, 0.29, 1.58, -10.87, TRUE, TRUE, 'Nigeria Diplomatic Envoy handling West African diplomacy.', 0),
-- Saudi Arabia Diplomatic Envoy
(93, 'Ambassador Fatima Al-Rashid', 'Diplomatic Envoy', 'Saudi Arabia', 'NPC', 'Milky Way', 7.13, 4.20, -5.87, 7.13, 4.20, -5.87, TRUE, TRUE, 'Saudi Arabia Diplomatic Envoy responsible for Middle Eastern relations.', 0),
-- Turkey Diplomatic Envoy
(94, 'Ambassador Ayse Demir', 'Diplomatic Envoy', 'Turkey', 'NPC', 'Milky Way', 4.73, 6.45, -6.63, 4.73, 6.45, -6.63, TRUE, TRUE, 'Turkey Diplomatic Envoy overseeing Eurasian diplomacy.', 0),
-- Argentina Diplomatic Envoy
(95, 'Ambassador Lucia Gonzalez', 'Diplomatic Envoy', 'Argentina', 'NPC', 'Milky Way', -9.87, -6.68, -4.13, -9.87, -6.68, -4.13, TRUE, TRUE, 'Argentina Diplomatic Envoy handling South American relations.', 0),
-- South Africa Diplomatic Envoy
(96, 'Ambassador Nomsa Dlamini', 'Diplomatic Envoy', 'South Africa', 'NPC', 'Milky Way', 3.85, -5.35, -8.23, 3.85, -5.35, -8.23, TRUE, TRUE, 'South Africa Diplomatic Envoy responsible for African Union diplomacy.', 0)
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- Intelligence Directors (IDs 97-116): One per country, positioned near intelligence facilities
-- Color coding: Dark Gray in 3D view
-- ============================================================================

INSERT INTO ai_characters (id, name, role, empire_name, character_type, galaxy_name, pos_x, pos_y, pos_z, home_pos_x, home_pos_y, home_pos_z, ai_admin, active, description, disposition_score)
VALUES
-- US Intelligence Director
(97, 'Director Robert Hayes', 'Intelligence Director', 'United States', 'NPC', 'Milky Way', 7.35, 7.93, -6.79, 7.35, 7.93, -6.79, TRUE, TRUE, 'US Intelligence Director overseeing CIA, NSA, and domestic intelligence operations.', 0),
-- China Intelligence Director
(98, 'Director Chen Ming', 'Intelligence Director', 'China', 'NPC', 'Milky Way', 4.10, 7.94, 8.53, 4.10, 7.94, 8.53, TRUE, TRUE, 'China Intelligence Director responsible for MSS and military intelligence.', 0),
-- Russia Intelligence Director
(99, 'Director Viktor Sokolov', 'Intelligence Director', 'Russia', 'NPC', 'Milky Way', 6.97, 9.80, -3.43, 6.97, 9.80, -3.43, TRUE, TRUE, 'Russia Intelligence Director overseeing FSB and SVR operations.', 0),
-- UK Intelligence Director
(100, 'Director Catherine Blake', 'Intelligence Director', 'United Kingdom', 'NPC', 'Milky Way', 0.52, 9.40, -8.07, 0.52, 9.40, -8.07, TRUE, TRUE, 'UK Intelligence Director responsible for MI6 and MI5 coordination.', 0),
-- Germany Intelligence Director
(101, 'Director Klaus Weber', 'Intelligence Director', 'Germany', 'NPC', 'Milky Way', 2.80, 9.53, -7.33, 2.80, 9.53, -7.33, TRUE, TRUE, 'Germany Intelligence Director overseeing BND and BfV operations.', 0),
-- France Intelligence Director
(102, 'Director Jean-Pierre Moreau', 'Intelligence Director', 'France', 'NPC', 'Milky Way', 0.91, 9.06, -7.78, 0.91, 9.06, -7.78, TRUE, TRUE, 'France Intelligence Director responsible for DGSE and DGSI.', 0),
-- Japan Intelligence Director
(103, 'Director Hiroshi Nakamura', 'Intelligence Director', 'Japan', 'NPC', 'Milky Way', 6.43, 7.34, 7.07, 6.43, 7.34, 7.07, TRUE, TRUE, 'Japan Intelligence Director overseeing Public Security Intelligence Agency.', 0),
-- India Intelligence Director
(104, 'Director Arjun Patel', 'Intelligence Director', 'India', 'NPC', 'Milky Way', 5.07, 6.29, 9.23, 5.07, 6.29, 9.23, TRUE, TRUE, 'India Intelligence Director responsible for RAW and IB operations.', 0),
-- Brazil Intelligence Director
(105, 'Director Paulo Santos', 'Intelligence Director', 'Brazil', 'NPC', 'Milky Way', -8.77, -4.23, -6.83, -8.77, -4.23, -6.83, TRUE, TRUE, 'Brazil Intelligence Director overseeing ABIN operations.', 0),
-- Canada Intelligence Director
(106, 'Director Michael Thompson', 'Intelligence Director', 'Canada', 'NPC', 'Milky Way', 8.03, 8.63, -7.43, 8.03, 8.63, -7.43, TRUE, TRUE, 'Canada Intelligence Director responsible for CSIS operations.', 0),
-- Australia Intelligence Director
(107, 'Director Sarah Mitchell', 'Intelligence Director', 'Australia', 'NPC', 'Milky Way', 6.87, -7.28, 6.33, 6.87, -7.28, 6.33, TRUE, TRUE, 'Australia Intelligence Director overseeing ASIO and ASD.', 0),
-- South Korea Intelligence Director
(108, 'Director Lee Min-ho', 'Intelligence Director', 'South Korea', 'NPC', 'Milky Way', 5.73, 7.60, 7.67, 5.73, 7.60, 7.67, TRUE, TRUE, 'South Korea Intelligence Director responsible for NIS operations.', 0),
-- Mexico Intelligence Director
(109, 'Director Carlos Ramirez', 'Intelligence Director', 'Mexico', 'NPC', 'Milky Way', -8.13, 4.83, -5.03, -8.13, 4.83, -5.03, TRUE, TRUE, 'Mexico Intelligence Director overseeing CISEN operations.', 0),
-- Italy Intelligence Director
(110, 'Director Giuseppe Romano', 'Intelligence Director', 'Italy', 'NPC', 'Milky Way', 2.67, 8.19, -7.86, 2.67, 8.19, -7.86, TRUE, TRUE, 'Italy Intelligence Director responsible for AISE and AISI.', 0),
-- Spain Intelligence Director
(111, 'Director Fernando Lopez', 'Intelligence Director', 'Spain', 'NPC', 'Milky Way', -2.15, 8.00, -7.89, -2.15, 8.00, -7.89, TRUE, TRUE, 'Spain Intelligence Director overseeing CNI operations.', 0),
-- Nigeria Intelligence Director
(112, 'Director Emeka Obi', 'Intelligence Director', 'Nigeria', 'NPC', 'Milky Way', 1.79, 3.08, -11.37, 1.79, 3.08, -11.37, TRUE, TRUE, 'Nigeria Intelligence Director responsible for DSS operations.', 0),
-- Saudi Arabia Intelligence Director
(113, 'Director Khalid Al-Fahad', 'Intelligence Director', 'Saudi Arabia', 'NPC', 'Milky Way', 8.63, 5.70, -6.37, 8.63, 5.70, -6.37, TRUE, TRUE, 'Saudi Arabia Intelligence Director overseeing GIP operations.', 0),
-- Turkey Intelligence Director
(114, 'Director Ozgur Kaya', 'Intelligence Director', 'Turkey', 'NPC', 'Milky Way', 6.23, 7.95, -7.13, 6.23, 7.95, -7.13, TRUE, TRUE, 'Turkey Intelligence Director responsible for MIT operations.', 0),
-- Argentina Intelligence Director
(115, 'Director Ricardo Fernandez', 'Intelligence Director', 'Argentina', 'NPC', 'Milky Way', -8.37, -5.18, -4.63, -8.37, -5.18, -4.63, TRUE, TRUE, 'Argentina Intelligence Director overseeing ARGINTEL.', 0),
-- South Africa Intelligence Director
(116, 'Director Sipho Ndlovu', 'Intelligence Director', 'South Africa', 'NPC', 'Milky Way', 5.35, -3.85, -8.73, 5.35, -3.85, -8.73, TRUE, TRUE, 'South Africa Intelligence Director responsible for NSS operations.', 0)
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- Science Officers (IDs 117-136): One per country, positioned near research facilities
-- Color coding: Orange in 3D view
-- ============================================================================

INSERT INTO ai_characters (id, name, role, empire_name, character_type, galaxy_name, pos_x, pos_y, pos_z, home_pos_x, home_pos_y, home_pos_z, ai_admin, active, description, disposition_score)
VALUES
-- US Science Officer
(117, 'Dr. Sarah Johnson', 'Science Officer', 'United States', 'NPC', 'Milky Way', 5.35, 5.93, -4.79, 5.35, 5.93, -4.79, TRUE, TRUE, 'US Science Officer overseeing NASA, DARPA, and national research initiatives.', 0),
-- China Science Officer
(118, 'Dr. Wang Lei', 'Science Officer', 'China', 'NPC', 'Milky Way', 2.10, 5.94, 6.53, 2.10, 5.94, 6.53, TRUE, TRUE, 'China Science Officer responsible for CAS and space program research.', 0),
-- Russia Science Officer
(119, 'Dr. Elena Ivanova', 'Science Officer', 'Russia', 'NPC', 'Milky Way', 4.97, 7.80, -1.43, 4.97, 7.80, -1.43, TRUE, TRUE, 'Russia Science Officer overseeing Roscosmos and RAS research.', 0),
-- UK Science Officer
(120, 'Dr. Richard Clarke', 'Science Officer', 'United Kingdom', 'NPC', 'Milky Way', -1.48, 7.40, -6.07, -1.48, 7.40, -6.07, TRUE, TRUE, 'UK Science Officer responsible for STFC and national labs.', 0),
-- Germany Science Officer
(121, 'Dr. Ingrid Fischer', 'Science Officer', 'Germany', 'NPC', 'Milky Way', 0.80, 7.53, -5.33, 0.80, 7.53, -5.33, TRUE, TRUE, 'Germany Science Officer overseeing DLR and Max Planck Institutes.', 0),
-- France Science Officer
(122, 'Dr. Claire Dupont', 'Science Officer', 'France', 'NPC', 'Milky Way', -1.09, 7.06, -5.78, -1.09, 7.06, -5.78, TRUE, TRUE, 'France Science Officer responsible for CNRS and CEA research.', 0),
-- Japan Science Officer
(123, 'Dr. Kenji Sato', 'Science Officer', 'Japan', 'NPC', 'Milky Way', 4.43, 5.34, 5.07, 4.43, 5.34, 5.07, TRUE, TRUE, 'Japan Science Officer overseeing JAXA and JST research.', 0),
-- India Science Officer
(124, 'Dr. Ananya Reddy', 'Science Officer', 'India', 'NPC', 'Milky Way', 3.07, 4.29, 7.23, 3.07, 4.29, 7.23, TRUE, TRUE, 'India Science Officer responsible for ISRO and DST research.', 0),
-- Brazil Science Officer
(125, 'Dr. Ricardo Oliveira', 'Science Officer', 'Brazil', 'NPC', 'Milky Way', -6.77, -2.23, -4.83, -6.77, -2.23, -4.83, TRUE, TRUE, 'Brazil Science Officer overseeing AEB and CNPq research.', 0),
-- Canada Science Officer
(126, 'Dr. Jennifer Lee', 'Science Officer', 'Canada', 'NPC', 'Milky Way', 6.03, 6.63, -5.43, 6.03, 6.63, -5.43, TRUE, TRUE, 'Canada Science Officer responsible for CSA and NSERC research.', 0),
-- Australia Science Officer
(127, 'Dr. Michael Brown', 'Science Officer', 'Australia', 'NPC', 'Milky Way', 4.87, -5.28, 4.33, 4.87, -5.28, 4.33, TRUE, TRUE, 'Australia Science Officer overseeing CSIRO and NASA collaboration.', 0),
-- South Korea Science Officer
(128, 'Dr. Park Min-ji', 'Science Officer', 'South Korea', 'NPC', 'Milky Way', 3.73, 5.60, 5.67, 3.73, 5.60, 5.67, TRUE, TRUE, 'South Korea Science Officer responsible for KARI and MSIT research.', 0),
-- Mexico Science Officer
(129, 'Dr. Elena Morales', 'Science Officer', 'Mexico', 'NPC', 'Milky Way', -10.13, 2.83, -5.03, -10.13, 2.83, -5.03, TRUE, TRUE, 'Mexico Science Officer overseeing CONACYT research.', 0),
-- Italy Science Officer
(130, 'Dr. Alessandro Conti', 'Science Officer', 'Italy', 'NPC', 'Milky Way', 0.67, 6.19, -5.86, 0.67, 6.19, -5.86, TRUE, TRUE, 'Italy Science Officer responsible for ASI and CNR research.', 0),
-- Spain Science Officer
(131, 'Dr. Carmen Ruiz', 'Science Officer', 'Spain', 'NPC', 'Milky Way', -2.15, 6.00, -5.89, -2.15, 6.00, -5.89, TRUE, TRUE, 'Spain Science Officer overseeing CSIC research.', 0),
-- Nigeria Science Officer
(132, 'Dr. Amina Bello', 'Science Officer', 'Nigeria', 'NPC', 'Milky Way', -0.21, 1.08, -9.37, -0.21, 1.08, -9.37, TRUE, TRUE, 'Nigeria Science Officer responsible for NAS and space research.', 0),
-- Saudi Arabia Science Officer
(133, 'Dr. Omar Al-Harbi', 'Science Officer', 'Saudi Arabia', 'NPC', 'Milky Way', 6.63, 3.70, -4.37, 6.63, 3.70, -4.37, TRUE, TRUE, 'Saudi Arabia Science Officer overseeing SDAIA and research initiatives.', 0),
-- Turkey Science Officer
(134, 'Dr. Elif Yildiz', 'Science Officer', 'Turkey', 'NPC', 'Milky Way', 4.23, 5.95, -5.13, 4.23, 5.95, -5.13, TRUE, TRUE, 'Turkey Science Officer responsible for TUBITAK research.', 0),
-- Argentina Science Officer
(135, 'Dr. Maria Gonzalez', 'Science Officer', 'Argentina', 'NPC', 'Milky Way', -10.37, -7.18, -4.63, -10.37, -7.18, -4.63, TRUE, TRUE, 'Argentina Science Officer overseeing CONICET research.', 0),
-- South Africa Science Officer
(136, 'Dr. Lindiwe Mthembu', 'Science Officer', 'South Africa', 'NPC', 'Milky Way', 3.35, -5.85, -6.73, 3.35, -5.85, -6.73, TRUE, TRUE, 'South Africa Science Officer responsible for CSIR and SANSA research.', 0)
ON CONFLICT (id) DO NOTHING;

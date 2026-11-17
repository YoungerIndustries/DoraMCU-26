-- =============================================================================
-- Reset Player and Corporation Data for Clean Restart
-- =============================================================================
-- This script clears all player and corporation data while preserving:
--   - Reference/seed data (rules tables, galaxy definitions, star systems, etc.)
--   - Schema structure (no DROP TABLE statements)
--
-- Run this script against the H2 database to reset player/corporation state.
-- =============================================================================

SET REFERENTIAL_INTEGRITY FALSE;

-- =============================================================================
-- 1. CORE PLAYER & USER DATA
-- =============================================================================
DELETE FROM players;
DELETE FROM users;
DELETE FROM devices;
DELETE FROM player_character_registry;
DELETE FROM promo_codes;

-- =============================================================================
-- 2. EVE CORPORATIONS & AI CHARACTERS
-- =============================================================================
DELETE FROM eve_corporations;
DELETE FROM treasuries;
DELETE FROM treasury_loans;
DELETE FROM ai_characters;
DELETE FROM ai_commander_experiences;
DELETE FROM ai_commander_hiring;
DELETE FROM voice_profiles;
DELETE FROM ai_assistant_actions;

-- =============================================================================
-- 3. CORPORATION DATA
-- =============================================================================
DELETE FROM corporation_saves;
DELETE FROM corporation_office_state;
DELETE FROM corporation_relationships;
DELETE FROM corporation_interaction_proposals;
DELETE FROM corporate_contracts;
DELETE FROM corporate_contract_assignments;
DELETE FROM corporate_finance_transactions;
DELETE FROM corporate_hiring;
DELETE FROM corporate_meetings;
DELETE FROM crew;
DELETE FROM crew_member;
DELETE FROM character_training_tour;

-- =============================================================================
-- 4. STOCK MARKET
-- =============================================================================
DELETE FROM stock_listings;
DELETE FROM stock_holdings;
DELETE FROM stock_orders;
DELETE FROM stock_trades;
DELETE FROM dividend_payments;

-- =============================================================================
-- 5. EARTH 2026 CHARACTERS & RELATED DATA
-- =============================================================================
DELETE FROM earth2026_characters;
DELETE FROM earth2026_chat_messages;
DELETE FROM earth2026_contracts;
DELETE FROM earth2026_online_status;
DELETE FROM earth2026_parties;
DELETE FROM e26_companions;
DELETE FROM e26_connections;
DELETE FROM e26_families;
DELETE FROM e26_family_members;
DELETE FROM e26_org_members;
DELETE FROM e26_organizations;
DELETE FROM e26_crafting_projects;
DELETE FROM e26_crafting_recipes;

-- =============================================================================
-- 6. EARTH 2026 STRUCTURES & PROPERTIES (player-owned)
-- =============================================================================
DELETE FROM apartment_buildings;
DELETE FROM apartment_units;
DELETE FROM commercial_properties;
DELETE FROM property_listings;
DELETE FROM property_transactions;
DELETE FROM office_units;
DELETE FROM office_rent_payments;
DELETE FROM younger_industries_hq;
DELETE FROM younger_offices;
DELETE FROM kiosk_upgrades;
DELETE FROM hq_events;
DELETE FROM hq_notifications;
DELETE FROM hq_npcs;
DELETE FROM hq_portals;
DELETE FROM portal_travel;
DELETE FROM work_orders;
DELETE FROM planet_structures;

-- =============================================================================
-- 7. SURVIVAL MODE DATA
-- =============================================================================
DELETE FROM survival_states;
DELETE FROM survival_resources;
DELETE FROM survival_encounters;
DELETE FROM base_structures;
DELETE FROM dungeon_instances;

-- =============================================================================
-- 8. CAPTAIN'S LOG DATA
-- =============================================================================
DELETE FROM cl_log_entries;
DELETE FROM cl_characters;
DELETE FROM cl_character_talents;
DELETE FROM cl_combats;
DELETE FROM cl_crew_members;
DELETE FROM cl_equipment;
DELETE FROM cl_focuses;
DELETE FROM cl_missions;
DELETE FROM cl_ship_talents;
DELETE FROM cl_starships;
DELETE FROM cl_values;
DELETE FROM cl_campaigns;

-- =============================================================================
-- 9. SHARED CHARACTERS
-- =============================================================================
DELETE FROM shared_characters;
DELETE FROM shared_character_skills;
DELETE FROM shared_character_travel_log;

-- =============================================================================
-- 10. DND5E CAMPAIGNS & CHARACTERS (player-created)
-- =============================================================================
DELETE FROM dnd5e_campaign_characters;
DELETE FROM dnd5e_campaigns;
DELETE FROM dnd5e_characters;
DELETE FROM dnd5e_character_backgrounds;
DELETE FROM dnd5e_character_classes;
DELETE FROM dnd5e_character_feats;
DELETE FROM dnd5e_character_growing_items;
DELETE FROM dnd5e_character_magic_items;
DELETE FROM dnd5e_character_skills;
DELETE FROM dnd5e_character_tool_proficiencies;
DELETE FROM dnd5e_combatants;
DELETE FROM dnd5e_combats;
DELETE FROM dnd5e_crafting_projects;
DELETE FROM dnd5e_inventory_items;
DELETE FROM dnd5e_sessions;
DELETE FROM dnd5e_exhaustion;
DELETE FROM dnd5e_fear_dread;
DELETE FROM dnd5e_heroic_inspiration;
DELETE FROM dnd5e_tension_pools;
DELETE FROM dnd5e_spell_slots;

-- =============================================================================
-- 11. PLANET DATA (player-owned corporations, settlements, industries)
-- =============================================================================
DELETE FROM planet_corporations;
DELETE FROM planet_settlements;
DELETE FROM planet_industries;
DELETE FROM planet_stations;
DELETE FROM planet_vehicles;
DELETE FROM planet_contracts;
DELETE FROM planet_quests;
DELETE FROM planet_building_modules;
DELETE FROM planet_trade_routes;
DELETE FROM planet_terrain_cells;
DELETE FROM galaxy_object_states;

-- =============================================================================
-- 12. GALAXY TREASURY & BONDS
-- =============================================================================
DELETE FROM galaxy_treasuries;
DELETE FROM galaxy_treasury_cycles;
DELETE FROM galaxy_bonds;

-- =============================================================================
-- 13. GAME SESSIONS
-- =============================================================================
DELETE FROM game_sessions_persisted;
DELETE FROM game_mode_state;

-- =============================================================================
-- 14. STAR TREK 4X DATA
-- =============================================================================
DELETE FROM st4x_game_sessions;
DELETE FROM st4x_factions;
DELETE FROM st4x_colonies;
DELETE FROM st4x_diplomatic_relations;
DELETE FROM st4x_trade_routes;
DELETE FROM st4x_tech_research;
DELETE FROM st4x_mission_updates;
DELETE FROM st4x_planetary_battles;
DELETE FROM st4x_ground_units;
DELETE FROM st4x_star_systems;

-- =============================================================================
-- 15. FOURX DATA
-- =============================================================================
DELETE FROM fourx_sessions;
DELETE FROM fourx_factions;
DELETE FROM fourx_colonies;
DELETE FROM fourx_diplomatic_relations;
DELETE FROM fourx_trade_routes;

-- =============================================================================
-- 16. BOND (007) DATA
-- =============================================================================
DELETE FROM bond_game_sessions;
DELETE FROM bond_empires;
DELETE FROM bond_agents;
DELETE FROM bond_territories;
DELETE FROM bond_diplomatic_relations;
DELETE FROM bond_spy_networks;
DELETE FROM bond_covert_operations;
DELETE FROM bond_tech_research;

-- =============================================================================
-- 17. TRAVELLER DATA
-- =============================================================================
DELETE FROM traveller_character_position;
DELETE FROM traveller_contract_assignments;
DELETE FROM traveller_planet_landing;
DELETE FROM traveller_air_combat_mission;
DELETE FROM traveller_aircraft_design;
DELETE FROM traveller_custom_ship_design;
DELETE FROM traveller_ship_component;
DELETE FROM traveller_monsters;
DELETE FROM traveller_monster_abilities;
DELETE FROM traveller_star_system;

-- =============================================================================
-- 18. AI ADMIN DATA
-- =============================================================================
DELETE FROM ai_admin_orders;
DELETE FROM ai_admin_test_cases;
DELETE FROM ai_admin_test_reports;

-- =============================================================================
-- 19. SKINS & DECALS (player-created)
-- =============================================================================
DELETE FROM skins;
DELETE FROM skin_variants;
DELETE FROM decals;
DELETE FROM faction_skin_themes;

-- =============================================================================
-- 20. HULL MODULES (player-created)
-- =============================================================================
DELETE FROM hull_modules;
DELETE FROM hull_module_blueprints;

-- =============================================================================
-- 21. WORMHOLES (player-created)
-- =============================================================================
DELETE FROM wormholes;

-- =============================================================================
-- 22. E26 MONSTERS & NPCs (player-created)
-- =============================================================================
DELETE FROM e26_monsters;
DELETE FROM e26_npcs;

-- =============================================================================
-- 23. DUNGEON INSTANCES
-- =============================================================================
DELETE FROM dungeon_instances;

-- =============================================================================
-- Reset auto-increment sequences where possible (H2 syntax)
-- =============================================================================
ALTER TABLE players ALTER COLUMN id RESTART WITH 1;
ALTER TABLE users ALTER COLUMN id RESTART WITH 1;
ALTER TABLE eve_corporations ALTER COLUMN id RESTART WITH 1;
ALTER TABLE earth2026_characters ALTER COLUMN id RESTART WITH 1;
ALTER TABLE promo_codes ALTER COLUMN id RESTART WITH 1;

SET REFERENTIAL_INTEGRITY TRUE;

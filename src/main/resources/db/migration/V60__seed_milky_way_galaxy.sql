-- Seed the Milky Way galaxy (Earth 2026) with all game modes including FPS
-- This is the default starting galaxy/game for DoraMCU

-- Add Earth 2026 lore to game_lores table if not present
INSERT INTO game_lores (name, slug, genre, category, short_description, description,
    technology_level, tone, factions, species, naming_convention, galaxy_shape,
    suggested_empire_count, suitable_for_rpg, suitable_for_4x, suitable_for_tycoon,
    suitable_for_tactical, suitable_for_survival, recommended_game_modes, active)
VALUES ('Earth 2026', 'earth_2026', 'SCI_FI', 'Modern',
    'Modern-day Earth — the starting hub for all characters before transitioning to other galaxies.',
    'Earth 2026 is the default starting point in DoraMCU. Characters begin here in a modern-day setting with real-world locations, corporations, and organizations before choosing to travel to other game galaxies through portals. Features modern firearms, urban environments, and contemporary scenarios.',
    'Near Future', 'Realistic',
    '["Younger Industries", "UN", "Corporate Security", "Government Agencies"]',
    '["Humans"]',
    'Real-world', 'Single Planet',
    1, TRUE, TRUE, TRUE, TRUE, TRUE,
    '["RPG", "FOUR_X", "TYCOON", "TACTICAL", "SURVIVAL", "SANDBOX", "MMORPG", "ROGUELIKE", "SIMULATION", "ADVENTURE", "FPS"]',
    TRUE);

INSERT INTO galaxies (name, total_sectors, hyperlane_density, lore_slug, game_modes)
VALUES ('Milky Way', 100, 1.0, 'earth_2026',
        'RPG,FOUR_X,TYCOON,TACTICAL,SURVIVAL,SANDBOX,MMORPG,ROGUELIKE,SIMULATION,ADVENTURE,FPS');

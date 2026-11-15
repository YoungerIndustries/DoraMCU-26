-- Add suitability flags for the 6 new game modes to game_lores table
-- Supports: Sandbox, MMORPG, Roguelike, Simulation, Adventure, FPS

ALTER TABLE game_lores
    ADD COLUMN suitable_for_sandbox BOOLEAN DEFAULT FALSE,
    ADD COLUMN suitable_for_mmorpg BOOLEAN DEFAULT FALSE,
    ADD COLUMN suitable_for_roguelike BOOLEAN DEFAULT FALSE,
    ADD COLUMN suitable_for_simulation BOOLEAN DEFAULT FALSE,
    ADD COLUMN suitable_for_adventure BOOLEAN DEFAULT FALSE,
    ADD COLUMN suitable_for_fps BOOLEAN DEFAULT FALSE;

-- Update existing lores with appropriate suitability flags for new game modes

-- Star Wars: good for sandbox building, mmorpg, roguelike dungeons, adventure stories, fps combat
UPDATE game_lores SET suitable_for_sandbox = TRUE, suitable_for_mmorpg = TRUE, suitable_for_roguelike = TRUE, suitable_for_simulation = FALSE, suitable_for_adventure = TRUE, suitable_for_fps = TRUE WHERE slug = 'star-wars';

-- Star Trek: simulation (diplomacy), mmorpg, adventure exploration, fps combat
UPDATE game_lores SET suitable_for_sandbox = FALSE, suitable_for_mmorpg = TRUE, suitable_for_roguelike = FALSE, suitable_for_simulation = TRUE, suitable_for_adventure = TRUE, suitable_for_fps = TRUE WHERE slug = 'star-trek';

-- Mass Effect: sandbox, mmorpg, roguelike, adventure, fps
UPDATE game_lores SET suitable_for_sandbox = TRUE, suitable_for_mmorpg = TRUE, suitable_for_roguelike = TRUE, suitable_for_simulation = FALSE, suitable_for_adventure = TRUE, suitable_for_fps = TRUE WHERE slug = 'mass-effect';

-- Warhammer 40K: survival already set, roguelike (grim deaths), fps combat
UPDATE game_lores SET suitable_for_sandbox = FALSE, suitable_for_mmorpg = TRUE, suitable_for_roguelike = TRUE, suitable_for_simulation = TRUE, suitable_for_adventure = FALSE, suitable_for_fps = TRUE WHERE slug = 'warhammer-40k';

-- Dune: simulation (politics, spice economy), adventure
UPDATE game_lores SET suitable_for_sandbox = FALSE, suitable_for_mmorpg = FALSE, suitable_for_roguelike = FALSE, suitable_for_simulation = TRUE, suitable_for_adventure = TRUE, suitable_for_fps = FALSE WHERE slug = 'dune';

-- The Expanse: simulation, adventure, fps
UPDATE game_lores SET suitable_for_sandbox = FALSE, suitable_for_mmorpg = FALSE, suitable_for_roguelike = FALSE, suitable_for_simulation = TRUE, suitable_for_adventure = TRUE, suitable_for_fps = TRUE WHERE slug = 'the-expanse';

-- Halo: sandbox, mmorpg, roguelike, adventure, fps (excellent fps lore)
UPDATE game_lores SET suitable_for_sandbox = TRUE, suitable_for_mmorpg = TRUE, suitable_for_roguelike = TRUE, suitable_for_simulation = FALSE, suitable_for_adventure = TRUE, suitable_for_fps = TRUE WHERE slug = 'halo';

-- Babylon 5: simulation (politics), adventure
UPDATE game_lores SET suitable_for_sandbox = FALSE, suitable_for_mmorpg = TRUE, suitable_for_roguelike = FALSE, suitable_for_simulation = TRUE, suitable_for_adventure = TRUE, suitable_for_fps = FALSE WHERE slug = 'babylon-5';

-- Firefly: sandbox, adventure, survival already set
UPDATE game_lores SET suitable_for_sandbox = TRUE, suitable_for_mmorpg = FALSE, suitable_for_roguelike = FALSE, suitable_for_simulation = FALSE, suitable_for_adventure = TRUE, suitable_for_fps = TRUE WHERE slug = 'firefly';

-- Stargate: sandbox, mmorpg, roguelike, adventure, fps
UPDATE game_lores SET suitable_for_sandbox = TRUE, suitable_for_mmorpg = TRUE, suitable_for_roguelike = TRUE, suitable_for_simulation = FALSE, suitable_for_adventure = TRUE, suitable_for_fps = TRUE WHERE slug = 'stargate';

-- Red Dwarf: sandbox already set, adventure already set, roguelike
UPDATE game_lores SET suitable_for_sandbox = TRUE, suitable_for_mmorpg = FALSE, suitable_for_roguelike = TRUE, suitable_for_simulation = FALSE, suitable_for_adventure = TRUE, suitable_for_fps = FALSE WHERE slug = 'red-dwarf';

-- D&D Forgotten Realms: sandbox, mmorpg, roguelike (dungeons), adventure
UPDATE game_lores SET suitable_for_sandbox = TRUE, suitable_for_mmorpg = TRUE, suitable_for_roguelike = TRUE, suitable_for_simulation = FALSE, suitable_for_adventure = TRUE, suitable_for_fps = FALSE WHERE slug = 'dnd-forgotten-realms';

-- Lord of the Rings: sandbox, adventure already set, roguelike
UPDATE game_lores SET suitable_for_sandbox = TRUE, suitable_for_mmorpg = FALSE, suitable_for_roguelike = TRUE, suitable_for_simulation = FALSE, suitable_for_adventure = TRUE, suitable_for_fps = FALSE WHERE slug = 'lord-of-the-rings';

-- World of Warcraft: sandbox, mmorpg already set, roguelike, adventure
UPDATE game_lores SET suitable_for_sandbox = TRUE, suitable_for_mmorpg = TRUE, suitable_for_roguelike = TRUE, suitable_for_simulation = FALSE, suitable_for_adventure = TRUE, suitable_for_fps = FALSE WHERE slug = 'world-of-warcraft';

-- Elder Scrolls: sandbox already set, mmorpg, roguelike, adventure already set
UPDATE game_lores SET suitable_for_sandbox = TRUE, suitable_for_mmorpg = TRUE, suitable_for_roguelike = TRUE, suitable_for_simulation = FALSE, suitable_for_adventure = TRUE, suitable_for_fps = FALSE WHERE slug = 'elder-scrolls';

-- Warhammer Fantasy: roguelike, survival already set
UPDATE game_lores SET suitable_for_sandbox = FALSE, suitable_for_mmorpg = FALSE, suitable_for_roguelike = TRUE, suitable_for_simulation = FALSE, suitable_for_adventure = FALSE, suitable_for_fps = FALSE WHERE slug = 'warhammer-fantasy';

-- Alien: roguelike (permadeath fits horror), fps (excellent fps horror)
UPDATE game_lores SET suitable_for_sandbox = FALSE, suitable_for_mmorpg = FALSE, suitable_for_roguelike = TRUE, suitable_for_simulation = FALSE, suitable_for_adventure = FALSE, suitable_for_fps = TRUE WHERE slug = 'alien';

-- Dead Space: roguelike (permadeath), fps (excellent fps horror)
UPDATE game_lores SET suitable_for_sandbox = FALSE, suitable_for_mmorpg = FALSE, suitable_for_roguelike = TRUE, suitable_for_simulation = FALSE, suitable_for_adventure = FALSE, suitable_for_fps = TRUE WHERE slug = 'dead-space';

-- Fallout: sandbox already set, mmorpg, roguelike, simulation, adventure, fps
UPDATE game_lores SET suitable_for_sandbox = TRUE, suitable_for_mmorpg = TRUE, suitable_for_roguelike = TRUE, suitable_for_simulation = TRUE, suitable_for_adventure = TRUE, suitable_for_fps = TRUE WHERE slug = 'fallout';

-- Avatar: simulation (ecosystem), adventure, fps
UPDATE game_lores SET suitable_for_sandbox = FALSE, suitable_for_mmorpg = FALSE, suitable_for_roguelike = FALSE, suitable_for_simulation = TRUE, suitable_for_adventure = TRUE, suitable_for_fps = TRUE WHERE slug = 'avatar';

-- Transformers: sandbox, roguelike, fps
UPDATE game_lores SET suitable_for_sandbox = TRUE, suitable_for_mmorpg = FALSE, suitable_for_roguelike = TRUE, suitable_for_simulation = FALSE, suitable_for_adventure = FALSE, suitable_for_fps = TRUE WHERE slug = 'transformers';

-- Pokemon: sandbox, mmorpg already set, adventure already set
UPDATE game_lores SET suitable_for_sandbox = TRUE, suitable_for_mmorpg = TRUE, suitable_for_roguelike = FALSE, suitable_for_simulation = FALSE, suitable_for_adventure = TRUE, suitable_for_fps = FALSE WHERE slug = 'pokemon';

-- Minecraft: sandbox already set, survival already set, adventure already set, tycoon already set, roguelike
UPDATE game_lores SET suitable_for_sandbox = TRUE, suitable_for_mmorpg = TRUE, suitable_for_roguelike = TRUE, suitable_for_simulation = FALSE, suitable_for_adventure = TRUE, suitable_for_fps = FALSE WHERE slug = 'minecraft';

-- Destiny: mmorpg already set, sandbox, roguelike, fps (loot shooter)
UPDATE game_lores SET suitable_for_sandbox = TRUE, suitable_for_mmorpg = TRUE, suitable_for_roguelike = TRUE, suitable_for_simulation = FALSE, suitable_for_adventure = FALSE, suitable_for_fps = TRUE WHERE slug = 'destiny';

-- Genshin Impact: sandbox, adventure already set, roguelike
UPDATE game_lores SET suitable_for_sandbox = TRUE, suitable_for_mmorpg = TRUE, suitable_for_roguelike = TRUE, suitable_for_simulation = FALSE, suitable_for_adventure = TRUE, suitable_for_fps = FALSE WHERE slug = 'genshin-impact';

-- Earth 2026: all modes suitable
UPDATE game_lores SET suitable_for_sandbox = TRUE, suitable_for_mmorpg = TRUE, suitable_for_roguelike = TRUE, suitable_for_simulation = TRUE, suitable_for_adventure = TRUE, suitable_for_fps = TRUE WHERE slug = 'earth-2026';

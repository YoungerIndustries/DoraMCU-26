-- V61: Seed MechWarrior / BattleTech rules and lore into unified rules database

-- ==================== LORE ====================

INSERT INTO unified_rules_entries (game_system, title, source, chapter, category, page, rule_text, notes, active) VALUES
('MECH_WARRIOR', 'The Inner Sphere', 'MechWarrior Lore', 'Lore', 'SETTING', NULL,
 'The Inner Sphere is the region of space humanity colonized after achieving faster-than-light travel via the K-F Drive. It consists of a spiral arm of star systems stretching from the central star Terra (Earth) outward in a rough spiral pattern. The Inner Sphere is divided among five Great Houses, each ruling a Successor State: the Federated Suns (House Davion), the Lyran Commonwealth (House Steiner), the Draconis Combine (House Kurita), the Free Worlds League (House Marik), and the Capellan Confederation (House Liao). Terra itself is controlled by ComStar, the neutral interstellar communications organization that maintains the Star Drive Network.',
 'The five Great Houses are the primary political powers of the Inner Sphere. ComStar controls Terra and the communication network.'),

('MECH_WARRIOR', 'The Star League', 'MechWarrior Lore', 'Lore', 'HISTORY', NULL,
 'The Star League was a multinational administrative and peacekeeping treaty formed approximately 250 years before its collapse in 2781. During the Star League era, mankind achieved technological marvels, and the relative stability is still viewed as a utopian period. The Star League Defense Force (SLDF), led by General Aleksandr Kerensky, was the military arm of the Star League. After the destruction of House Cameron and the collapse of the Star League in 2781, Kerensky attempted to reconcile the Great Houses but ultimately led Operation Exodus in 2784, taking the bulk of the SLDF beyond the borders of known space.',
 'The Star League era is considered the golden age of the Inner Sphere. Kerensky''s Clan Exodus led to the formation of the Clans.'),

('MECH_WARRIOR', 'The Succession Wars', 'MechWarrior Lore', 'Lore', 'HISTORY', NULL,
 'Following the fall of the Star League, the five Great Houses fought four devastating wars over the position of First Lord. The Succession Wars lasted nearly 300 years and caused unimaginable devastation: hundreds of billions died, hundreds of worlds were rendered lifeless, and technological progress was pushed back centuries as much knowledge became lostech. The four wars were: First Succession War (2786-2821), Second Succession War (2830-2864), Third Succession War (2866-3025), and Fourth Succession War (3028-3030). The Fourth Succession War brought dramatic change when the Lyran Commonwealth and Federated Suns united to form the Federated Commonwealth.',
 'The Succession Wars devastated the Inner Sphere and caused massive technological regression.'),

('MECH_WARRIOR', 'House Davion - Federated Suns', 'MechWarrior Lore', 'Lore', 'GREAT_HOUSES', NULL,
 'House Davion rules the Federated Suns from the capital world of New Avalon. Known for their military prowess and chivalric traditions, the Davions are considered the most martial of the Great Houses. The Federated Suns is characterized by its frontier culture, strong military tradition, and the famous "Davion Grin" — a battle cry that strikes fear into enemies. The Federated Suns excels in combined arms tactics and produces some of the finest MechWarriors in the Inner Sphere. Notable for the Battle of Tukayyid and the formation of the Federated Commonwealth with the Lyran Commonwealth.',
 'Motto: "The Sun Rises on the Federated Commonwealth." Known for military excellence and frontier spirit.'),

('MECH_WARRIOR', 'House Steiner - Lyran Commonwealth', 'MechWarrior Lore', 'Lore', 'GREAT_HOUSES', NULL,
 'House Steiner rules the Lyran Commonwealth from the capital world of Tharkad. The Lyran Commonwealth is the Inner Sphere''s greatest economic power, with many resource-rich worlds, shrewd business acumen, and a strong industrial base. The Lyrans are known for their Germanic cultural influences, advanced technology, and the Lyran Combat Group (LCG) doctrine that emphasizes concentrated firepower. The Commonwealth prospered even during the worst of the Succession Wars, with overall industrial output exceeding any other Successor State.',
 'Motto: "The Steiner Sun Never Sets." Known for economic power, technology, and the Lyran Combat Group doctrine.'),

('MECH_WARRIOR', 'House Kurita - Draconis Combine', 'MechWarrior Lore', 'Lore', 'GREAT_HOUSES', NULL,
 'House Kurita rules the Draconis Combine from the capital world of Luthien. Founded in 2319 by Shiro Kurita, the Combine is the only Great House that proclaims unabashedly its right to rule all of the Inner Sphere. Embracing "the way of the warrior," the Draconis Combine has a strong Japanese cultural influence. Personal honor and self-reliance bordering on xenophobia are encouraged. The Combine is known for its aggressive expansionism, the samurai-like Bushido code followed by its warriors, and the elite Elite Guard units.',
 'Motto: "The Combine is Strength." Known for warrior culture, Bushido code, and aggressive expansionism.'),

('MECH_WARRIOR', 'House Marik - Free Worlds League', 'MechWarrior Lore', 'Lore', 'GREAT_HOUSES', NULL,
 'House Marik rules the Free Worlds League from the capital world of Alpheratz. The Free Worlds League is characterized by its libertarian political culture, where individual worlds enjoy significant autonomy. The League is known for its political intrigue, internal instability, and the "Marik Madness" — a series of civil wars and succession disputes that have plagued the state. Despite its political weaknesses, the Free Worlds League possesses significant industrial capacity and a proud military tradition.',
 'Known for political intrigue, libertarian culture, and internal instability.'),

('MECH_WARRIOR', 'House Liao - Capellan Confederation', 'MechWarrior Lore', 'Lore', 'GREAT_HOUSES', NULL,
 'House Liao rules the Capellan Confederation from the capital world of Sian. The Capellan Confederation is characterized by its Chinese cultural influences, authoritarian government, and emphasis on espionage and subterfuge. The Confederation is known for its large population, extensive use of infantry and vehicles alongside BattleMechs, and the Capellan Intelligence Directorate (CID). House Liao is considered the most cunning and deceptive of the Great Houses, often using political maneuvering and espionage as weapons.',
 'Known for espionage, authoritarian rule, and large population-based military.'),

('MECH_WARRIOR', 'The Clans', 'MechWarrior Lore', 'Lore', 'GREAT_HOUSES', NULL,
 'The Clans are the descendants of the Star League Defense Force that followed General Kerensky in Operation Exodus beyond the borders of known space in 2784. After centuries in the Deep Periphery, the Clans developed unique technologies and a rigid caste-based society organized into warrior clans. They returned to the Inner Sphere in 2785 (C.T. - Clan Time) with the goal of conquering it, believing themselves superior to the "Baseborn" of the Inner Sphere. Major Clans include Alpha, Beta, Gamma, Delta, and Epsilon Clans, each with distinct cultures and technologies.',
 'The Clans returned to the Inner Sphere in 2785 C.T. (3050 A.T.) and launched the Great Refusal and subsequent invasions.'),

('MECH_WARRIOR', 'BattleMechs', 'MechWarrior Lore', 'Lore', 'TECHNOLOGY', NULL,
 'BattleMechs are giant humanoid war machines, typically 4 to 10 meters tall and weighing 20 to 100 tons. They are the cornerstone of Inner Sphere military power. Each BattleMech is piloted by a MechWarrior who controls the machine from an armored cockpit using a neural interface called the "Cockpit Interface" or "Direct Neural Interface (DNI)" in advanced models. BattleMechs are classified by weight: Light (20-35 tons), Medium (40-55 tons), Heavy (60-75 tons), and Assault (80-100 tons). Each class has distinct roles: Light mechs serve as scouts, Medium mechs are versatile workhorses, Heavy mechs provide frontline firepower, and Assault mechs are the spearheads of frontal assaults.',
 'BattleMechs are the defining technology of the BattleTech universe. The term "MechWarrior" refers to the pilot.'),

('MECH_WARRIOR', 'ComStar', 'MechWarrior Lore', 'Lore', 'ORGANIZATIONS', NULL,
 'ComStar is the neutral interstellar communications organization that controls Terra and maintains the Star Drive Network, the network of beacons that make faster-than-light travel possible. Founded after the fall of the Star League, ComStar evolved from the Communications and Electronics Corporation (ComGuards) into a powerful religious and political organization. ComStar controls the flow of information across the Inner Sphere and wields enormous influence despite its official neutrality. The organization maintains the SLDF''s remaining forces on Terra and guards the secrets of the K-F Drive.',
 'ComStar''s control of the Star Drive Network gives it de facto control over interstellar travel and communication.'),

-- ==================== CORE GAME MECHANICS ====================

('MECH_WARRIOR', 'Core Mechanic: 2D6 System', 'BattleTech Rulebook', 'Core Rules', 'CORE_MECHANICS', NULL,
 'BattleTech uses a 2D6 (two six-sided dice) resolution system. When making a check, roll 2D6 and add any modifiers. If the total meets or exceeds the Target Number (TN), the check succeeds. The probability distribution of 2D6 creates a bell curve: 7 is the most likely result (16.7%), while 2 and 12 are the least likely (2.8% each). This means moderate results are far more common than extreme ones, creating predictable and balanced outcomes.',
 '2D6 bell curve: 2=1/36, 3=2/36, 4=3/36, 5=4/36, 6=5/36, 7=6/36, 8=5/36, 9=4/36, 10=3/36, 11=2/36, 12=1/36.'),

('MECH_WARRIOR', 'Sequence of Play', 'BattleTech Rulebook', 'Core Rules', 'CORE_MECHANICS', NULL,
 'A BattleTech game consists of a series of turns. Each turn consists of several phases performed in order: 1) Initiative Phase — One player from each side rolls 2D6. The side with the higher result has won Initiative for the turn. Re-roll ties. 2) Movement Phase — The side that lost Initiative moves first. The side that won Initiative moves second. 3) Weapon Attack Phase — The side that won Initiative fires first. The side that lost Initiative fires second. During each phase, players may move their units and fire weapons according to the rules.',
 'Initiative determines movement and firing order. Losing Initiative moves first but fires second.'),

('MECH_WARRIOR', 'Initiative Phase', 'BattleTech Rulebook', 'Combat', 'INITIATIVE', NULL,
 'At the start of each turn, one player from each side rolls 2D6 to determine their side''s Initiative. The side with the higher result has won Initiative throughout the turn. Re-roll all ties. The side with Initiative fires first in the Weapon Attack Phase but moves second in the Movement Phase. The side without Initiative moves first but fires second. Winning initiative is strategically important as it allows you to react to your opponent''s movements.',
 'Higher 2D6 roll wins initiative. Ties are re-rolled.'),

('MECH_WARRIOR', 'Movement Phase', 'BattleTech Rulebook', 'Combat', 'MOVEMENT', NULL,
 'During the Movement Phase, the side that lost Initiative moves their units first, and the side that won Initiative moves second. Each unit may move up to its Movement Points (MP) allowance. Movement is measured in hexes on the map. A unit may choose to stand still instead of moving. After movement, a unit may perform actions such as firing weapons, bracing, or performing physical attacks. The type of terrain affects movement cost: Clear terrain costs 1 MP per hex, Light Woods costs +1, Heavy Woods costs +2, and difficult terrain may impose additional penalties.',
 'Movement is measured in hexes. Terrain type affects movement cost.'),

('MECH_WARRIOR', 'Weapon Attack Phase', 'BattleTech Rulebook', 'Combat', 'FIRING', NULL,
 'During the Weapon Attack Phase, the side that won Initiative fires first, and the side that lost Initiative fires second. Each unit may fire its weapons at targets within range. To resolve a weapon attack: 1) Determine the hit location by rolling 2D6 and consulting the Hit Location Table. 2) Calculate the To-Hit Number based on the weapon''s range bracket, target''s movement, and any modifiers. 3) Roll 2D6 and add modifiers. If the total meets or exceeds the To-Hit Number, the attack hits. 4) Apply damage based on the weapon''s damage value.',
 'The side with Initiative fires first. To-Hit Number depends on range, movement, and modifiers.'),

-- ==================== MOVEMENT RULES ====================

('MECH_WARRIOR', 'Movement Points', 'BattleTech Rulebook', 'Movement', 'MOVEMENT', NULL,
 'Each BattleMech has a Walking Speed and a Running (Sprint) Speed, measured in kilometers per hour. These are converted to Movement Points (MP) for game purposes. Walking Speed divided by 16 (rounded down) gives the Walk MP. Running Speed divided by 16 (rounded down) gives the Run MP. A unit may walk or run each turn, but not both. Running prevents the unit from firing weapons that turn due to recoil. Jump Jets allow vertical movement over obstacles and terrain, consuming additional MP.',
 'Walk MP = Walking Speed / 16. Run MP = Running Speed / 16. Running prevents weapon fire.'),

('MECH_WARRIOR', 'Terrain Movement Costs', 'BattleTech Rulebook', 'Movement', 'MOVEMENT', NULL,
 'Movement costs vary by terrain type. Each hex costs 1 MP plus a terrain modifier to enter: Clear terrain costs +0 (1 MP total), Light Woods costs +1 (2 MP total), Heavy Woods costs +2 (3 MP total), Hills cost +2 (3 MP total), Mountains are generally impassable, Water varies by depth, and Urban terrain costs +1 to +3 depending on density. Some terrain types impose additional penalties, such as pinning or movement restrictions.',
 'Clear=1MP, Light Woods=2MP, Heavy Woods=3MP, Hills=3MP, Mountains=Impassable.'),

('MECH_WARRIOR', 'Facing and Direction', 'BattleTech Rulebook', 'Movement', 'MOVEMENT', NULL,
 'Each unit has a facing direction, represented by an arrow on the counter. A unit may change facing during movement. Changing facing costs 1 MP per 60 degrees of rotation. A unit may fire weapons only at targets within its arc of fire: Front Arc (directly ahead, ±30 degrees), Side Arc (left or right, ±60 degrees from the side), and Rear Arc (behind, ±30 degrees from the rear). Firing from the side or rear arc imposes penalties.',
 'Facing changes cost 1 MP per 60 degrees. Arc of fire affects weapon accuracy.'),

-- ==================== COMBAT RULES ====================

('MECH_WARRIOR', 'To-Hit Number Calculation', 'BattleTech Rulebook', 'Combat', 'TO_HIT', NULL,
 'The To-Hit Number (THN) for a weapon attack is calculated as: THN = Base THN + Range Modifier + Movement Modifier + Terrain Modifier. The Base THN depends on the weapon type and range bracket (Short, Medium, Long). Range Modifiers are typically +0 for Short, +2 for Medium, and +4 for Long range. Movement Modifiers depend on the attacker''s and target''s movement: if both moved, add +2; if only one moved, add +1. Terrain Modifiers apply when firing through or into difficult terrain.',
 'THN = Base + Range Mod + Movement Mod + Terrain Mod. Short=+0, Medium=+2, Long=+4.'),

('MECH_WARRIOR', 'Hit Location Table', 'BattleTech Rulebook', 'Combat', 'HIT_LOCATION', NULL,
 'When a weapon attack hits a BattleMech, roll 2D6 to determine which location is struck: 2 = Center Torso, 3 = Right Arm, 4 = Right Arm, 5 = Right Leg, 6 = Right Leg, 7 = Center Torso, 8 = Left Torso, 9 = Left Leg, 10 = Left Leg, 11 = Left Arm, 12 = Head. The Center Torso is the most common hit location (2/12 chance). The Head is the rarest (1/12 chance) but most critical. Arms and Legs are hit with equal probability (2/12 each).',
 '2D6 roll: 2=Center Torso, 3-4=Right Arm, 5-6=Right Leg, 7=Center Torso, 8=Left Torso, 9-10=Left Leg, 11=Left Arm, 12=Head.'),

('MECH_WARRIOR', 'Armor and Damage', 'BattleTech Rulebook', 'Combat', 'DAMAGE', NULL,
 'Each BattleMech location has an Armor Value (AV) that absorbs incoming damage. When a weapon hits, its damage is first applied to the armor. If the damage exceeds the armor value, the excess damage is applied to the internal structure. When the internal structure of a location is destroyed, the location is lost: arms fall off, legs cause the mech to fall, and head hits can kill the pilot. The Center Torso contains the life support and engine; destroying it causes a catastrophic explosion.',
 'Damage first reduces armor, then internal structure. Destroying the Center Torso causes a catastrophic explosion.'),

('MECH_WARRIOR', 'Critical Hits', 'BattleTech Rulebook', 'Combat', 'CRITICAL_HITS', NULL,
 'When a BattleMech''s armor in a location is reduced to zero, subsequent hits may cause critical hits. Roll to hit the location again; if it hits, check for critical hits. Each BattleMech has a critical hit slot map. When a critical hit occurs, roll on the Critical Hit Table for that location. Critical hits can destroy weapons, ammunition, heat sinks, the cockpit, or the engine. Destroying the engine causes the mech to shut down. Destroying the cockpit kills the pilot. Ammunition explosions deal massive damage to the containing location and may cause chain reactions.',
 'Critical hits occur when armor is depleted. They can destroy components, cause ammo explosions, or kill the pilot.'),

('MECH_WARRIOR', 'Physical Attacks', 'BattleTech Rulebook', 'Combat', 'PHYSICAL_ATTACKS', NULL,
 'BattleMechs may perform physical attacks including punching, kicking, and ramming. To make a physical attack, the attacker must be adjacent to the target and facing it. Roll 2D6 and add the attacker''s Piloting Skill modifier. If the total meets or exceeds the Target Number, the attack hits. Damage is based on the attacker''s tonnage and the type of attack. Physical attacks can knock targets down, push them back, or cause structural damage. Kicking a damaged leg can cause the target to fall.',
 'Physical attacks require adjacency and proper facing. Damage is based on tonnage and attack type.'),

-- ==================== HEAT SYSTEM ====================

('MECH_WARRIOR', 'Heat System Overview', 'BattleTech Rulebook', 'Combat', 'HEAT', NULL,
 'BattleMechs generate heat when firing weapons and using jump jets. Each weapon generates a specific amount of heat when fired. The mech''s heat sinks dissipate heat each turn. If the heat level exceeds the heat sink capacity, the excess heat accumulates. At certain heat thresholds, the mech may shut down: at 14, 18, 22, 26, and 30 heat points, the mech attempts an automatic shutdown. A conscious MechWarrior can override the shutdown by rolling 2D6 against a Target Number. At 30 or more heat points, shutdown is unavoidable and the mech becomes immobile until heat drops.',
 'Heat accumulates from weapon fire and jump jets. Shutdown occurs at heat thresholds: 14, 18, 22, 26, 30.'),

('MECH_WARRIOR', 'Heat Sink Capacity', 'BattleTech Rulebook', 'Combat', 'HEAT', NULL,
 'Each BattleMech is equipped with heat sinks that dissipate heat each turn. Standard heat sinks dissipate 1 heat point per sink. Double Heat Sinks (DHS) dissipate 2 heat points per sink but take up twice the critical hit space. The number of heat sinks is determined by the mech''s design. If the accumulated heat exceeds the heat sink capacity, the excess heat is carried over to the next turn. Managing heat is a critical aspect of BattleMech combat, as overheating can render a mech immobile.',
 'Standard sinks: 1 heat/sink. Double Heat Sinks: 2 heat/sink. Excess heat carries over to next turn.'),

('MECH_WARRIOR', 'Shutdown Procedure', 'BattleTech Rulebook', 'Combat', 'HEAT', NULL,
 'When a mech''s heat reaches a shutdown threshold (14, 18, 22, 26, or 30), roll 2D6 once during the Heat Phase. If the roll meets or exceeds the Target Number for the current heat level, the pilot avoids shutdown for that turn. If the roll fails, the mech shuts down and becomes immobile until the heat drops below the threshold. At 30 or more heat points, shutdown is automatic and cannot be avoided. A shut-down mech cannot move or fire weapons but can still be targeted.',
 'Roll 2D6 against TN at each threshold. At 30+ heat, shutdown is automatic and unavoidable.'),

-- ==================== MECHWARRIOR PILOT RULES ====================

('MECH_WARRIOR', 'MechWarrior Skills', 'BattleTech Rulebook', 'Pilot', 'SKILLS', NULL,
 'Each MechWarrior has two primary skills: Gunnery and Piloting. Gunnery affects weapon accuracy — a higher Gunnery skill reduces the To-Hit Number. Piloting affects movement and physical attacks — a higher Piloting skill reduces movement penalties and improves physical attack accuracy. Skills are rated from 0 to 10, with 5 being average. Elite MechWarriors may have skills of 7 or higher. Green MechWarriors typically have skills of 3 or lower.',
 'Gunnery affects weapon accuracy. Piloting affects movement and physical attacks. Skills range from 0 to 10.'),

('MECH_WARRIOR', 'Consciousness and Wounds', 'BattleTech Rulebook', 'Pilot', 'WOUNDS', NULL,
 'When a MechWarrior takes damage (wounds), they must make a Consciousness Roll. Roll 2D6 and consult the Consciousness Table. If the result is equal to or greater than the MechWarrior''s consciousness number, the warrior remains conscious. If the roll fails, the warrior is immediately knocked unconscious. An unconscious MechWarrior cannot control their mech. During the End Phase of each turn after the turn in which the MechWarrior lost consciousness, roll 2D6 to attempt to recover. If the result meets or exceeds the consciousness number, the warrior regains consciousness. MechWarriors can survive up to 5 points of damage; beyond that, they die.',
 'Consciousness rolls determine if a pilot remains functional. 5 wounds = death. Unconscious pilots cannot control their mech.'),

('MECH_WARRIOR', 'Pilot Experience Levels', 'BattleTech Rulebook', 'Pilot', 'EXPERIENCE', NULL,
 'MechWarriors are classified by experience level: Green (new recruits, skills 0-3), Veteran (experienced combatants, skills 4-6), Elite (highly skilled warriors, skills 7-8), and Ace (legendary pilots, skills 9-10). Experience level affects skill values and may grant special abilities. Green MechWarriors are prone to errors and may panic under fire. Veteran MechWarriors are reliable combatants. Elite MechWarriors can perform advanced maneuvers. Aces are legendary figures who can turn the tide of battle.',
 'Green (0-3), Veteran (4-6), Elite (7-8), Ace (9-10). Experience affects skills and special abilities.'),

-- ==================== WEAPON SYSTEMS ====================

('MECH_WARRIOR', 'Energy Weapons', 'BattleTech Rulebook', 'Weapons', 'ENERGY', NULL,
 'Energy weapons fire beams of concentrated energy. They do not require ammunition but generate significant heat. Types include: Small Laser (5 damage, 1 ton, 5 heat, range 9/18/27), Medium Laser (5 damage, 1 ton, 3 heat, range 12/25/37), Large Laser (8 damage, 5 tons, 8 heat, range 15/30/45), and Particle Projection Cannon (PPC) (10 damage, 7 tons, 10 heat, range 15/30/45, plus 5 rear-facing damage). Energy weapons are reliable and do not risk ammunition explosions, but they generate substantial heat.',
 'Lasers and PPCs. No ammo needed but generate significant heat. PPC also deals rear-facing damage.'),

('MECH_WARRIOR', 'Ballistic Weapons', 'BattleTech Rulebook', 'Weapons', 'BALLISTIC', NULL,
 'Ballistic weapons fire physical projectiles. They require ammunition but generate minimal heat. Types include: Autocannon/2 (AC/2) (2 damage, 6 tons, 1 heat, range 12/24/36, 25 rounds/ton), Autocannon/5 (AC/5) (5 damage, 8 tons, 4 heat, range 6/12/18, 20 rounds/ton), Autocannon/10 (AC/10) (10 damage, 12 tons, 5 heat, range 6/12/18, 8 rounds/ton), and Autocannon/20 (AC/20) (20 damage, 14 tons, 7 heat, range 3/9/15, 5 rounds/ton). Ballistic weapons excel at focused damage but risk ammunition explosions if the ammo bin is hit.',
 'Autocannons of various calibers. High focused damage but require ammo and risk explosions.'),

('MECH_WARRIOR', 'Missile Weapons', 'BattleTech Rulebook', 'Weapons', 'MISSILE', NULL,
 'Missile weapons fire guided projectiles. They require ammunition and generate moderate heat. Types include: Short Range Missiles (SRM) — SRM-2 (2 damage, 2 tons, 1 heat, range 3/6/9), SRM-4 (4 damage, 3 tons, 2 heat, range 3/6/9), SRM-6 (6 damage, 4 tons, 3 heat, range 3/6/9); Long Range Missiles (LRM) — LRM-5 (5 damage, 2 tons, 2 heat, range 9/18/27), LRM-10 (10 damage, 4 tons, 5 heat, range 9/18/27), LRM-15 (15 damage, 7 tons, 5 heat, range 12/24/36), LRM-20 (20 damage, 10 tons, 6 heat, range 12/24/36). Missiles spread damage across multiple locations but can be focused by attacking from the side.',
 'SRMs for short range, LRMs for long range. Damage spreads across locations. Ammo explosions are a risk.'),

('MECH_WARRIOR', 'Weapon Range Brackets', 'BattleTech Rulebook', 'Weapons', 'RANGE', NULL,
 'Each weapon has three range brackets: Short, Medium, and Long. The To-Hit Number increases with range. Short range typically adds +0 to the THN, Medium range adds +2, and Long range adds +4. Some weapons have a minimum range and cannot fire at targets closer than the minimum. The range brackets are measured in hexes on the map. A weapon''s effectiveness decreases with range due to the increasing THN.',
 'Short=+0 THN, Medium=+2 THN, Long=+4 THN. Some weapons have minimum range.'),

-- ==================== MECH CLASSIFICATIONS ====================

('MECH_WARRIOR', 'Light BattleMechs (20-35 tons)', 'BattleTech Rulebook', 'Mechs', 'LIGHT', NULL,
 'Light BattleMechs weigh from 20 to 35 tons. They have limited armor and weapon capacity but superior mobility. Light mechs benefit from smaller, more efficient fusion reactors that provide higher speed relative to their size. They are typically used as scouts and anti-personnel units. Their low production cost and high proliferation make them widespread. Some light mechs are designated as combat mechs with relatively heavy armament for their class. Examples include the Commando (CMD-1), Shadow Hawk (SHD-2H), and Grasshopper (GRP-2A).',
 'Fast, lightly armed, and armored. Used as scouts and anti-personnel units. Low cost and high availability.'),

('MECH_WARRIOR', 'Medium BattleMechs (40-55 tons)', 'BattleTech Rulebook', 'Mechs', 'MEDIUM', NULL,
 'Medium BattleMechs weigh from 40 to 55 tons and are the workhorses of most armies. They offer the best combination of speed, armor, and payload, giving them unmatched versatility. Medium mechs can often outmaneuver heavier opponents and bring their weapons to bear with better effect. They serve as command units among light mechs and as the backbone of most lance compositions. Examples include the Wolverine (WVR-6R), Griffin (GF-6M), Centurion (CTR-A), and Enforcer (ENC-2A).',
 'The workhorse class. Best balance of speed, armor, and payload. Unmatched versatility.'),

('MECH_WARRIOR', 'Heavy BattleMechs (60-75 tons)', 'BattleTech Rulebook', 'Mechs', 'HEAVY', NULL,
 'Heavy BattleMechs weigh from 60 to 75 tons. They carry more armor and weapons than medium mechs but sacrifice some speed. Heavy mechs are designed for frontline combat, providing substantial firepower and durability. They can mount heavy weapons like the AC/20 and multiple LRMs. Heavy mechs are the anvil against which lighter units break. Examples include the Atlas (AS7-D), Thunderbolt (TB-L), and Black Knight (BKD-1).',
 'Frontline combatants with heavy armor and firepower. Sacrifice speed for durability and payload.'),

('MECH_WARRIOR', 'Assault BattleMechs (80-100 tons)', 'BattleTech Rulebook', 'Mechs', 'ASSAULT', NULL,
 'Assault BattleMechs are the heaviest regular class, weighing from 80 to 100 tons. They can mount up to 50 tons of weapons and very thick armor. Assault mechs project tremendous firepower and perform best as spearheads of frontal assaults or in defending entrenched positions. The tradeoff is speed and maneuverability — assault mechs are generally very slow. They are typically used as command units, heavy assault platforms, and fire-support units. Due to their prohibitive cost, they make up only a small fraction of all BattleMechs. Examples include the Battlemaster (BLK-NT), Warhammer (WHM-6A), and Jenner (NRB-Nak).',
 'Maximum firepower and armor at the cost of speed. Used as spearheads and command units. Rare and expensive.'),

-- ==================== BATTLE FORMATIONS ====================

('MECH_WARRIOR', 'Lance Composition', 'BattleTech Rulebook', 'Formations', 'LANCE', NULL,
 'A Lance is the basic tactical unit in BattleTech, consisting of 4 BattleMechs. Lances are typically composed of a mix of mech classes to provide balanced capabilities. A standard lance might include 1 light, 2 medium, and 1 heavy mech. The composition depends on the mission and available resources. Lances are commanded by a Lance Commander who coordinates the actions of the individual MechWarriors. Effective lance tactics involve coordinating fire, movement, and support.',
 'A lance consists of 4 BattleMechs. Typical composition: 1 light, 2 medium, 1 heavy.'),

('MECH_WARRIOR', 'Company Structure', 'BattleTech Rulebook', 'Formations', 'COMPANY', NULL,
 'A Company consists of 10 Lances (40 BattleMechs). Companies are the basic operational unit and are commanded by a Company Commander. A standard company composition follows the DCMS (Draconis Combine Military Standard) ratio: 6 light, 15 medium, 9 heavy, and 6 assault mechs. However, actual compositions vary widely based on availability, doctrine, and mission requirements. Companies may be organized into battalions (3-4 companies) and regiments (3-4 battalions).',
 '10 lances = 40 mechs. DCMS ratio: 6 light, 15 medium, 9 heavy, 6 assault.'),

-- ==================== VEHICLES ====================

('MECH_WARRIOR', 'Vehicles in BattleTech', 'BattleTech Rulebook', 'Vehicles', 'OVERVIEW', NULL,
 'Vehicles are an important part of BattleTech combat. They include tanks, infantry carriers, artillery, and aerospace fighters. Vehicles are classified by tonnage and type. Tanks are the most common vehicle type, ranging from light reconnaissance vehicles to heavy battle tanks. Infantry carriers transport troops to the battlefield. Artillery provides long-range fire support. Aerospace fighters provide air superiority and ground attack capabilities. Vehicles are generally cheaper than BattleMechs but are less versatile and more vulnerable to certain types of attacks.',
 'Vehicles include tanks, infantry carriers, artillery, and aerospace fighters. Cheaper but less versatile than mechs.'),

-- ==================== SCENARIOS ====================

('MECH_WARRIOR', 'Scenario Types', 'BattleTech Rulebook', 'Scenarios', 'TYPES', NULL,
 'BattleTech scenarios vary in type and complexity. Common scenario types include: 1) Skirmish — small-scale engagements between lances or companies. 2) Battle — large-scale engagements between regiments or larger forces. 3) Raid — one side attempts to capture or destroy a specific objective. 4) Defense — one side defends a position against an attacking force. 5) Patrol — a force searches for and engages enemy units. Scenarios may include specific victory conditions, such as destroying all enemy units, capturing objectives, or surviving for a set number of turns.',
 'Skirmish, Battle, Raid, Defense, and Patrol are common scenario types with varying victory conditions.'),

('MECH_WARRIOR', 'Map and Terrain', 'BattleTech Rulebook', 'Scenarios', 'MAP', NULL,
 'BattleTech is played on a hexagonal grid map. Each hex represents a specific area of terrain. Common terrain types include: Clear (open ground), Light Woods (sparse trees), Heavy Woods (dense forest), Hills (elevated terrain), Mountains (impassable), Water (rivers, lakes), and Urban (cities and buildings). Terrain affects movement, line of sight, and weapon accuracy. Maps may be pre-designed or generated randomly. The map size depends on the scenario scale, with larger battles requiring larger maps.',
 'Hex grid maps with terrain types affecting movement, LOS, and accuracy. Map size scales with scenario.'),

-- ==================== RULES TABLES ====================

INSERT INTO unified_rules_tables (game_system, name, source, chapter, description, num_sides, active) VALUES
('MECH_WARRIOR', 'Hit Location Table', 'BattleTech Rulebook', 'Combat', 'Roll 2D6 to determine which location is struck on a BattleMech. 2=Center Torso, 3-4=Right Arm, 5-6=Right Leg, 7=Center Torso, 8=Left Torso, 9-10=Left Leg, 11=Left Arm, 12=Head.', 6, TRUE),

('MECH_WARRIOR', 'Consciousness Table', 'BattleTech Rulebook', 'Pilot', 'Roll 2D6 when a MechWarrior takes a wound. Compare to the consciousness number based on total wounds. Wound 1: TN 3, Wound 2: TN 5, Wound 3: TN 7, Wound 4: TN 9, Wound 5: Death.', 6, TRUE),

('MECH_WARRIOR', 'Shutdown Thresholds', 'BattleTech Rulebook', 'Combat', 'At these heat levels, a mech attempts automatic shutdown. Heat 14: TN 10, Heat 18: TN 8, Heat 22: TN 7, Heat 26: TN 6, Heat 30+: Automatic shutdown, cannot avoid.', 6, TRUE),

('MECH_WARRIOR', 'Movement Terrain Costs', 'BattleTech Rulebook', 'Movement', 'MP cost to enter each terrain type. Clear: 1 MP, Light Woods: 2 MP, Heavy Woods: 3 MP, Hills: 3 MP, Mountains: Impassable, Urban: 2-4 MP, Water: Variable.', 6, TRUE),

('MECH_WARRIOR', 'To-Hit Modifiers', 'BattleTech Rulebook', 'Combat', 'Range and movement modifiers for To-Hit Number. Short Range: +0, Medium Range: +2, Long Range: +4. Both moved: +2, One moved: +1, Neither moved: +0.', 6, TRUE);

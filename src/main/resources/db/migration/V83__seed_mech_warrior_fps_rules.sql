-- V83: Comprehensive MechWarrior FPS-specific rules from all MechWarrior video games
-- Covers: MW'89, MW'93, MW3050, MW2:31st Century Combat, MW2:Ghost Bear's Legacy,
-- MW2:Mercenaries, MW3, MW4:Vengeance, MW4:Mercenaries, MWO, MW5:Mercenaries,
-- MW5:Clans, MW5:Dark Age, MechCommander, MechCommander 2, Mech Assault, BattleTech 2018

INSERT INTO unified_rules_entries (game_system, title, source, chapter, category, page, rule_text, notes, active) VALUES

('MECH_WARRIOR', 'FPS Cockpit View', 'MechWarrior Video Games', 'FPS Core', 'COCKPIT', NULL,
'All MechWarrior FPS games feature a first-person cockpit view as the primary perspective. The player sees the BattleMech from inside the cockpit, with instrument panels, targeting displays, and weapon status indicators visible. The cockpit provides a sense of scale and immersion, showing the mech limbs moving below and the battlefield ahead. MW2 introduced this as the defining feature, separating it from the tabletop board game. MW5:Mercenaries and MW5:Clans maintain cockpit view with enhanced HUD elements including damage indicators, heat gauges, and weapon cooldown displays. A third-person external view is available in MW2, MW3, MW4, MWO, and MW5 for tactical overview.',
'Cockpit view is the signature FPS mechanic. Third-person view available in most games for tactical overview.'),

('MECH_WARRIOR', 'FPS Control Modes', 'MechWarrior Video Games', 'FPS Core', 'CONTROLS', NULL,
'MechWarrior games offer multiple control modes ranging from simple to advanced. Basic mode (Point and Shoot) provides simplified controls where the mech aims and fires automatically. Advanced mode allows independent control of the mech legs and torso, with the player controlling left leg, right leg, and torso twist separately. MW2 introduced this control spectrum. MW5:Mercenaries uses a simplified control scheme with WASD movement, mouse aiming, and weapon firing via mouse buttons. The player can walk, run, and use jump jets. Torso twist allows the upper body to rotate independently for aiming while moving. Balance and stability are simulated — rapid movement or taking damage can cause the mech to fall.',
'Control modes range from simple point-and-shoot to advanced leg/torso control. Torso twist is a key FPS mechanic.'),

('MECH_WARRIOR', 'FPS Weapon Targeting System', 'MechWarrior Video Games', 'FPS Combat', 'TARGETING', NULL,
'FPS targeting in MechWarrior games uses a crosshair-based system with weapon-specific targeting reticles. The player aims using mouse or joystick, with the crosshair showing the predicted hit location. MW5:Mercenaries features weapon grouping — weapons can be assigned to groups that fire together or in sequence (chain fire). Alpha Strike fires all weapons simultaneously. Chain Fire fires weapons in sequence to manage heat. Each weapon has a cooldown period after firing — ballistic weapons reload while energy weapons cool down. The HUD shows weapon status with cooldown timers. MWO uses a different targeting system with weapon skills and quirk trees that affect accuracy and damage.',
'Crosshair-based targeting with weapon grouping, alpha strike, and chain fire. Cooldown timers shown on HUD.'),

('MECH_WARRIOR', 'FPS Component-Based Damage', 'MechWarrior Video Games', 'FPS Combat', 'DAMAGE', NULL,
'FPS damage modeling in MechWarrior games uses component-based damage. Each BattleMech has multiple components: head, center torso, left torso, right torso, left arm, right arm, left leg, right leg. Damage is applied to the specific component hit. MW3 introduced improved damage modeling with separate upper and lower arm/leg sections. MW5:Mercenaries uses a simplified component system where each part has armor and internal structure integrity. When integrity reaches zero, the part is destroyed — arms are ripped off, legs cause the mech to fall, and head hits can kill the pilot. The center torso contains the life support and engine; destroying it causes a catastrophic explosion. MWO uses critical damage where weapons can destroy equipment even when armor remains, with crit-seeking weapons (machine guns, LBX autocannons) having higher critical damage chance.',
'Component-based damage with separate integrity for each part. Crit-seeking weapons in MWO.'),

('MECH_WARRIOR', 'FPS Heat Management System', 'MechWarrior Video Games', 'FPS Combat', 'HEAT', NULL,
'Heat management is a core FPS mechanic in all MechWarrior games. Each weapon generates heat when fired. The mech has heat sinks that dissipate heat over time. The heat gauge on the HUD shows current heat level. If heat reaches 100%, the mech overheats and shuts down temporarily, becoming immobile. MW2 introduced heat override — the player can override the automatic shutdown to continue fighting, but this risks internal damage and reactor explosion. MW5:Mercenaries uses a simplified heat system with a gauge that fills and empties. The override function helps keep the mech operational but risks internal damage. Heat management is critical for energy weapon users, as lasers and PPCs generate the most heat. Ballistic weapons generate minimal heat but have longer cooldowns.',
'Heat gauge fills with weapon fire. Override risks internal damage. Energy weapons generate most heat.'),

('MECH_WARRIOR', 'FPS Mission-Based Campaigns', 'MechWarrior Video Games', 'FPS Campaign', 'MISSIONS', NULL,
'MechWarrior FPS games use mission-based campaigns with objectives, briefings, and debriefings. MW2:31st Century Combat features 32 missions across multiple planets with data links and situation reports. MW4:Vengeance has 26 campaign missions organized into 7 operations with 3-6 missions each. MW5:Mercenaries uses a contract system where the player accepts missions from various factions, with negotiation and consequences. Each mission has specific objectives such as destroying enemy mechs, capturing positions, escorting units, or defending locations. Missions may have time limits or specific victory conditions. Between missions, the player can customize mechs, hire pilots, and manage resources. MW5:Clans adds a story-driven campaign with cutscenes and a linear progression.',
'Mission-based campaigns with objectives, briefings, and debriefings. Contract system in MW5.'),

('MECH_WARRIOR', 'FPS Mech Customization System', 'MechWarrior Video Games', 'FPS Progression', 'CUSTOMIZATION', NULL,
'Mech customization is a major FPS mechanic. MW4:Vengeance introduced hardpoint-based weapon systems where specific weapons can only be mounted on designated hardpoints. MW5:Mercenaries allows full customization of weapons, armor, and components. The player can choose weapon loadouts, armor distribution, and internal components. MW5:Clans adds manual armor distribution where the player assigns armor to each slot individually. Salvage from missions provides new mechs and weapons. Destroyed weapons are lost if the component is destroyed. Repair costs scale with damage taken. The customization system allows for different play styles: fast scouts, heavy brawlers, long-range snipers, and balanced loadouts.',
'Hardpoint-based weapons in MW4. Full customization in MW5 with salvage and repair costs.'),

('MECH_WARRIOR', 'FPS Pilot Progression System', 'MechWarrior Video Games', 'FPS Progression', 'PILOTS', NULL,
'Pilot progression varies across MechWarrior FPS games. MW5:Mercenaries introduced pilot skills with six categories: Ballistic, Energy, Missile (weapon skills) and Shielding, Heat Management, Evasion (piloting skills). Pilots gain experience from missions and can be trained to improve skills. MW5:Clans adds pilot traits — special abilities like being good with specific mechs or weapon types. Pilots can be sent to training to gain XP and traits. MechCommander 2 features pilot experience and skills that improve abilities and unlock specialized skills. BattleTech 2018 uses a pilot stress system where pilots can become stressed or broken under pressure, affecting performance. Pilots have four stats: Gunnery, Piloting, Guts, and Tactics, each affecting different aspects of combat.',
'Pilot skills, traits, and progression systems. Stress system in BattleTech 2018.'),

('MECH_WARRIOR', 'FPS Multiplayer Game Modes', 'MechWarrior Video Games', 'FPS Multiplayer', 'MODES', NULL,
'MechWarrior FPS games feature various multiplayer modes. MWO offers Quickplay (team-based matches on 10+ maps with 4+ modes), Faction Play (prolonged team-based engagements conquering planets), and Event Queue (special events like Solaris weekend with 1v1 and 2v2 fights). MW5:Mercenaries supports online co-op with friends. MW4:Vengeance features deathmatch-style Instant Action where players fight waves of enemy mechs. Mech Assault focused on console multiplayer arenas with simplified controls. Multiplayer modes include team deathmatch, capture the flag, and objective-based missions. MWO Legends adds Faction Play with territory control across the Inner Sphere.',
'Quickplay, Faction Play, co-op, deathmatch, and objective-based multiplayer modes.'),

('MECH_WARRIOR', 'FPS Terrain and Environment', 'MechWarrior Video Games', 'FPS Environment', 'TERRAIN', NULL,
'FPS terrain in MechWarrior games affects combat significantly. MW5:Mercenaries features diverse environments including urban areas, forests, deserts, and mountains. Buildings can be destroyed, providing dynamic cover. Terrain affects movement speed, stability, and line of sight. Hills provide elevation advantage and can block missile targeting. Urban areas offer cover but may restrict movement. MW4:Vengeance features huge maps with diverse terrain and long draw distances for long-range engagements. BattleTech 2018 includes rough terrain, cover, water, radioactive areas, and high thermal areas. Terrain stability affects mech balance — rough terrain increases stability damage and decreases movement. Environmental factors like weather and time of day can affect visibility and combat.',
'Destructible buildings, elevation advantages, and dynamic terrain. Environmental factors affect combat.'),

('MECH_WARRIOR', 'FPS Vehicle Combat', 'MechWarrior Video Games', 'FPS Combat', 'VEHICLES', NULL,
'MechWarrior FPS games include vehicle combat alongside mechs. MW5:Mercenaries features tanks, ground vehicles, aircraft, and drop ships as enemy units. Vehicles have different armor profiles and weapon systems compared to mechs. Tanks are generally faster on flat terrain but lack vertical mobility. Aircraft provide air superiority and ground attack capabilities. Drop ships can deploy troops and provide fire support. MechCommander series emphasizes combined arms tactics with mechs, vehicles, and infantry working together. Vehicles are generally cheaper than mechs but are less versatile and more vulnerable to certain types of attacks. AMS (Anti-Missile Systems) turrets provide defense against missile attacks.',
'Tanks, aircraft, drop ships, and AMS turrets. Combined arms tactics in MechCommander.'),

('MECH_WARRIOR', 'FPS Electronic Warfare', 'MechWarrior Video Games', 'FPS Combat', 'EW', NULL,
'Electronic warfare is a mechanic in some MechWarrior FPS games. MW3 introduced ECM (Electronic Countermeasures) and ECCM (Electronic Counter-Countermeasures). ECM disrupts enemy targeting and radar, while ECCM counters ECM effects. MW4:Vengeance features installable options like Beal active probes for longer radar range and faster target lock, and ECM to defeat enemy target lock and radar. Night vision and light amplification are installable options for some chassis, making them better at night fighting. Electronic warfare adds a layer of tactical depth, allowing players to disrupt enemy targeting and gain information advantages.',
'ECM, ECCM, active probes, and night vision. Adds tactical depth to combat.'),

('MECH_WARRIOR', 'FPS Squad Tactics', 'MechWarrior Video Games', 'FPS Tactics', 'SQUAD', NULL,
'Squad tactics are featured in MechWarrior FPS games. MW4:Vengeance allows the player to control up to three squadmates, issuing basic orders such as attack and move. MW5:Mercenaries features a lance system where the player commands a team of mechs, with AI-controlled lancemates. MW5:Clans adds tactical map command where the player can individually order each unit on what to do, with the game pausing outside of multiplayer. MechCommander series is RTS-style, allowing full control of multiple units with group commands and formation tactics. Effective squad tactics involve coordinating fire, movement, and support, with different mech classes fulfilling different roles (scout, assault, support, command).',
'Squadmate control, lance system, and tactical map command. RTS-style in MechCommander.'),

('MECH_WARRIOR', 'FPS Called Shots', 'MechWarrior Video Games', 'FPS Combat', 'CALLED_SHOTS', NULL,
'Called shots allow the player to target specific parts of an enemy mech. BattleTech 2018 features called shots as a core mechanic, allowing focused firepower on a specific part to inflict greater damage. When a mech is knocked down, enemies can make called shots to specific regions like the cockpit. MW5:Mercenaries implicitly uses called shots through aiming — the player aims at specific parts to deal targeted damage. Called shots are riskier but can quickly disable key components. Precision Strike ability in BattleTech 2018 allows called shots on standing mechs, costing morale to use. Called shots are essential for strategic combat, allowing the player to disable weapons, legs, or the cockpit.',
'Target specific parts for focused damage. Precision Strike ability in BattleTech 2018.'),

('MECH_WARRIOR', 'FPS Activatable Abilities', 'MechWarrior Video Games', 'FPS Combat', 'ABILITIES', NULL,
'Activatable abilities are special skills that pilots can use in combat. BattleTech 2018 features activatable abilities that cost morale to use. Examples include: Bulwark (passive ability granting 50% to guarded levels when stationary), Juggernaut (successful melee attack deals extra damage), and Precision Strike (allows called shots on standing mechs). MW5:Clans adds pilot traits that provide passive bonuses. MechCommander 2 features pilot skills that unlock specialized abilities. Activatable abilities add customization and strategic depth, allowing pilots to specialize in different combat styles.',
'Bulwark, Juggernaut, Precision Strike. Morale cost for activation in BattleTech 2018.'),

('MECH_WARRIOR', 'FPS Stability System', 'MechWarrior Video Games', 'FPS Combat', 'STABILITY', NULL,
'Stability is a critical FPS mechanic in MechWarrior games. Mechs are bipedal walkers that must maintain balance. Weapon impacts can destabilize a mech, causing it to be knocked down. When knocked down, the mech is temporarily unable to move or fire effectively, and the pilot may take damage. Rough terrain increases stability damage and decreases movement. MW5:Mercenaries simulates stability — rapid movement or taking damage can cause the mech to fall. BattleTech 2018 includes stability as a core mechanic, with piloting skills affecting stability thresholds. Jump jets can also affect stability, as the landing impact can destabilize the mech. Stability management is essential for maintaining combat effectiveness.',
'Bipedal balance simulation. Knocked down mechs are vulnerable. Piloting skills affect stability.'),

('MECH_WARRIOR', 'FPS Salvage and Economy', 'MechWarrior Video Games', 'FPS Progression', 'ECONOMY', NULL,
'Salvage and economy are key progression mechanics. MW2:Mercenaries introduced mercenary play with economy, mech buying, and mission choice. MW4:Vengeance features battlefield salvage where the player can loot destroyed mechs for weapons and components. MW5:Mercenaries uses a contract system where the player earns money and reputation from missions. Salvage from missions provides new mechs and weapons. Destroyed weapons are lost if the component is destroyed. Repair costs scale with damage taken. The economy system allows the player to grow their mercenary outfit, hiring better pilots and acquiring better mechs. MW5:Clans adds clan-specific salvage and technology acquisition.',
'Mercenary economy with salvage, repair costs, and pilot hiring. Contract system in MW5.'),

('MECH_WARRIOR', 'FPS Clan vs Inner Sphere', 'MechWarrior Video Games', 'FPS Factions', 'CLANS', NULL,
'Clan vs Inner Sphere is a major faction distinction in MechWarrior FPS games. MW2:31st Century Combat features the Refusal War between Clan Wolf and Clan Jade Falcon. MW5:Clans adds Clan-specific tech with superior cooling, weapons, and armor. Clan mechs are tougher, faster, and more heat resistant than Inner Sphere mechs. Clan weapons are more powerful and outrange Inner Sphere weapons. MW5:Mercenaries features Clan interdiction where Clan forces may appear as hostile third factions near Clan-controlled planets. The faction system affects available mechs, weapons, and mission types. Clan mechs generally have better performance but are rarer and more expensive.',
'Clan tech superiority with better cooling, weapons, and armor. Faction-specific mechs and missions.'),

('MECH_WARRIOR', 'FPS Weapon Cooldowns', 'MechWarrior Video Games', 'FPS Combat', 'COOLDOWNS', NULL,
'Weapon cooldowns are a core FPS mechanic in MW5:Mercenaries and MW5:Clans. After being fired, weapons go on cooldown — ballistic weapons reload while energy weapons cool down. The HUD shows cooldown timers for each weapon group. Some weapons have longer cooldowns (Gauss Rifle) while others are ready again quickly (Lasers). Cooldown management is essential for maintaining firepower. Weapon grouping allows the player to fire weapons in sequence (chain fire) to manage cooldowns and heat. Alpha Strike fires all weapons simultaneously for maximum damage but generates more heat and longer cooldowns. Cooldowns add a layer of tactical depth, requiring the player to manage when and how to fire weapons.',
'Cooldown timers on HUD. Chain fire manages cooldowns and heat. Alpha strike for maximum damage.'),

('MECH_WARRIOR', 'FPS Jump Jets', 'MechWarrior Video Games', 'FPS Movement', 'JUMP_JETS', NULL,
'Jump jets allow vertical movement in MechWarrior FPS games. MW2 introduced jump jets as a key movement mechanic. MW5:Mercenaries features jump jets that allow the mech to leap over obstacles and terrain. Jump jets consume heat and can affect stability upon landing. The player can use jump jets to gain elevation advantage, escape dangerous situations, or close distance with enemies. Jump jet usage is limited by heat and fuel. Some mechs have more jump jets than others, affecting their mobility. Jump jets are essential for navigating complex terrain and maintaining tactical positioning.',
'Vertical movement with heat cost. Essential for terrain navigation and tactical positioning.'),

('MECH_WARRIOR', 'FPS Night Vision and Sensors', 'MechWarrior Video Games', 'FPS Sensors', 'SENSORS', NULL,
'Night vision and sensors are important FPS mechanics. MW3 introduced night fighting with light amplification as an installable option. MW4:Vengeance features Beal active probes for longer radar range and faster target lock. MW5:Mercenaries includes night vision and zooming in as standard features. Sensors provide information about enemy positions, distances, and types. The HUD shows enemy contacts with range and bearing. Sensor range varies by mech and can be improved with upgrades. Electronic warfare can disrupt sensors, making targeting more difficult. Night vision allows combat in low-light conditions, giving an advantage to mechs equipped with it.',
'Night vision, zoom, and sensor range. Electronic warfare can disrupt sensors.'),

('MECH_WARRIOR', 'FPS Reactor Explosion', 'MechWarrior Video Games', 'FPS Combat', 'REACTOR', NULL,
'Reactor explosion is the ultimate failure state in MechWarrior FPS games. When a mech takes excessive damage, the reactor can go critical and explode. MW5:Mercenaries features reactor explosion as a risk when using heat override excessively. If the reactor explodes, the mech is destroyed and cannot be salvaged. The center torso contains the reactor; destroying it causes a catastrophic explosion. Reactor explosion is the most devastating outcome, eliminating the mech entirely. Players must manage heat and damage carefully to avoid reactor explosion. In MW5:Clans, the long time to kill is reduced, making reactor explosions more common.',
'Ultimate failure state. Heat override increases risk. Center torso destruction causes explosion.'),

('MECH_WARRIOR', 'FPS Ejection and Survival', 'MechWarrior Video Games', 'FPS Combat', 'EJECTION', NULL,
'Ejection allows the pilot to survive when the mech is destroyed. When a mech is critically damaged, the pilot can eject to survive. Ejection is automatic in some cases (e.g., all weapons destroyed) or manual when the player chooses to eject. The pilot survives but the mech is lost. MW5:Mercenaries features ejection when the mech is destroyed, with the pilot returning to base for repairs. Ejection is a last resort, as the mech cannot be recovered after ejection. In multiplayer modes, ejection removes the player from the current match. Ejection is essential for pilot survival but results in mission failure or partial success.',
'Pilot survival when mech is destroyed. Last resort option. Mission failure or partial success.'),

('MECH_WARRIOR', 'FPS Instant Action Mode', 'MechWarrior Video Games', 'FPS Modes', 'INSTANT_ACTION', NULL,
'Instant Action mode allows quick, customized battles. MW4:Vengeance features Instant Action where players can pick a campaign mission or wave mission, select up to four other mechs, and fight deathmatch-style. The map is randomly selected and the player faces waves of enemy mechs. MW5:Mercenaries includes a fully-featured Instant Action system that allows the player to customize and kick off isolated missions at any time, free from campaign restrictions. Instant Action is ideal for testing loadouts, practicing combat, or quick sessions without campaign progression. It supports both single-player and multiplayer modes.',
'Quick customized battles. Test loadouts and practice combat. Single-player and multiplayer support.'),

('MECH_WARRIOR', 'FPS Mech Roles and Classes', 'MechWarrior Video Games', 'FPS Tactics', 'ROLES', NULL,
'Mech classes define roles in FPS combat. Light mechs (20-35 tons) serve as scouts with superior mobility but limited armor and weapons. Medium mechs (40-55 tons) are versatile workhorses with balanced speed, armor, and payload. Heavy mechs (60-75 tons) provide frontline firepower with substantial armor and weapons but reduced speed. Assault mechs (80-100 tons) are the heaviest class with maximum firepower and armor but minimal speed. Each class has distinct roles in combat: scouts gather information, medium mechs provide flexible support, heavy mechs anchor the frontline, and assault mechs lead charges. Effective combat requires understanding and utilizing each class strengths and weaknesses.',
'Light=Scout, Medium=Versatile, Heavy=Frontline, Assault=Charge. Understanding roles is essential.'),

('MECH_WARRIOR', 'FPS Weapon Synergy', 'MechWarrior Video Games', 'FPS Combat', 'WEAPON_SYNERGY', NULL,
'Weapon types and synergy are critical in FPS combat. Energy weapons (Lasers, PPCs) generate heat but have no ammo risk. Ballistic weapons (Autocannons, Gauss Rifles) deal high damage with minimal heat but have long cooldowns and ammo risk. Missile weapons (SRMs, LRMs) provide area damage and can be focused by attacking from the side. Weapon synergy involves combining different types for balanced performance. For example, pairing energy weapons with ballistic weapons allows sustained fire while managing heat. MWO promotes weapon synergy through cooldown adjustments — Clan streak SRMs cooldown was reduced to sync with Medium Pulse Lasers. Effective loadouts balance heat generation, cooldown times, and damage output.',
'Energy=Heat, Ballistic=Cooldown, Missile=Area. Synergy balances heat, cooldown, and damage.'),

('MECH_WARRIOR', 'FPS Ammo Management', 'MechWarrior Video Games', 'FPS Combat', 'AMMO', NULL,
'Ammo management is essential for ballistic and missile weapons. Each weapon carries a limited amount of ammo. When ammo is depleted, the weapon cannot fire until resupplied. MW5:Mercenaries features ammo drops during missions to resupply. Ammo bins are vulnerable to critical hits — if destroyed, the ammo explodes, dealing massive damage to the containing location and potentially causing chain reactions. Storing ammo in the center torso or feet reduces the risk of critical hits. MW5:Clans emphasizes ammo management — being able to carry two UAC20s or two LRM20 sounds amazing, but ammo storage will go quickly if not careful. Effective ammo management requires balancing firepower with sustainability.',
'Limited ammo with resupply drops. Critical hits cause explosions. Store ammo in center torso or feet.'),

('MECH_WARRIOR', 'FPS Mech Quirks and Skills', 'MechWarrior Video Games', 'FPS Progression', 'QUIRKS', NULL,
'Mech quirks and skills provide unique bonuses. MWO features mech quirk trees that grant passive bonuses to specific mechs. Quirks can improve speed, armor, heat dissipation, or weapon performance. Skill trees allow the player to unlock and upgrade abilities over time. MWO Legends adds mech skills that can be unlocked and customized. Quirks and skills add customization and strategic depth, allowing the player to tailor mechs to specific roles. Some quirks are mech-specific, while others are available to multiple mechs. Effective use of quirks and skills can significantly enhance a mech performance in combat.',
'Mech-specific passive bonuses. Skill trees for customization. Enhances mech performance.'),

('MECH_WARRIOR', 'FPS Faction Reputation', 'MechWarrior Video Games', 'FPS Progression', 'REPUTATION', NULL,
'Faction reputation affects mission availability and rewards. MW5:Mercenaries features a reputation system where the player relationships with various factions fluctuate according to the Succession Wars and player choices. Completing missions for a faction improves reputation, while failing or attacking them reduces it. Higher reputation unlocks better contracts and rewards. Lower reputation may result in fewer mission options or hostile encounters. MW5:Clans adds clan-specific reputation with the player choosing to join a clan and progressing through the ranks. Faction reputation adds a layer of strategic depth, requiring the player to balance relationships with multiple factions.',
'Reputation affects mission availability and rewards. Balance relationships with multiple factions.'),

('MECH_WARRIOR', 'FPS World Generation', 'MechWarrior Video Games', 'FPS World', 'WORLD_GEN', NULL,
'World generation creates diverse battlefields. MW5:Mercenaries uses procedural world generation to create the vast scope of the Inner Sphere with huge variety and replayability. Each world has unique terrain, climate, and resources. The procedural generation ensures that no two missions are identical, providing fresh challenges and opportunities. MW5:Clans uses handcrafted maps for the campaign but retains procedural generation for Instant Action. World generation adds replayability and variety, ensuring that the player encounters different environments and challenges across missions.',
'Procedural generation for variety and replayability. Unique terrain, climate, and resources per world.'),

('MECH_WARRIOR', 'FPS Torso Twist', 'MechWarrior Video Games', 'FPS Movement', 'TORSO_TWIST', NULL,
'Torso twist allows independent upper body rotation. MW2 introduced torso twist as a key mechanic, allowing the upper body to rotate independently for aiming while moving. MW5:Mercenaries maintains torso twist, enabling the player to aim and fire while the mech legs move in a different direction. Torso twist is essential for maintaining firepower while maneuvering. The torso can rotate 360 degrees, allowing the mech to fire in any direction without turning the entire body. Effective torso twist usage requires coordinating leg movement with torso rotation to maintain balance and accuracy.',
'Independent upper body rotation. Essential for firepower while maneuvering. 360-degree rotation.'),

('MECH_WARRIOR', 'FPS Time to Kill (TTK)', 'MechWarrior Video Games', 'FPS Combat', 'TTK', NULL,
'Time to kill (TTK) varies across MechWarrior FPS games. MW5:Mercenaries features methodical gameplay with high TTK — players rarely gun down an enemy in seconds. Instead, they chip away at the armor of specific components to blow off limbs and weapons. MW5:Clans reduces TTK significantly, with mechs dying in an instant if not eliminated quickly. The TTK affects the combat pace and tactical decisions. High TTK emphasizes sustained combat and tactical positioning, while low TTK emphasizes quick reactions and aggressive play. Understanding TTK is essential for adapting to different game modes and scenarios.',
'High TTK in MW5:Mercenaries, low TTK in MW5:Clans. Affects combat pace and tactical decisions.'),

('MECH_WARRIOR', 'FPS Environmental Destruction', 'MechWarrior Video Games', 'FPS Environment', 'DESTRUCTION', NULL,
'Environmental destruction adds dynamic combat. MW5:Mercenaries features destructible buildings and terrain. The player can smash through buildings, creating new paths and cover. Environmental destruction adds dynamic elements to combat, allowing the player to adapt to changing battlefield conditions. MW4:Vengeance features huge maps with diverse terrain and long draw distances for long-range engagements. BattleTech 2018 includes environmental factors like weather and time of day that can affect visibility and combat. Effective environmental destruction requires understanding the terrain layout and using destruction to tactical advantage.',
'Destructible buildings and terrain. Dynamic battlefield conditions. Tactical advantage through destruction.'),

('MECH_WARRIOR', 'FPS Critical Hit Slots', 'MechWarrior Video Games', 'FPS Combat', 'CRIT_SLOTS', NULL,
'Critical hit slots determine component vulnerability. MWO uses critical hit slots where each component has a specific number of slots that can be hit. When armor is depleted, subsequent hits can cause critical hits that destroy equipment. The number of critical hit slots varies by component size and type. Larger components have more slots, making them less vulnerable to critical hits. Effective critical hit slot management requires understanding the component layout and protecting critical components. Critical hit slots add a layer of strategic depth to mech design and combat.',
'Component-specific critical hit slots. Larger components have more slots. Strategic depth in mech design.'),

('MECH_WARRIOR', 'FPS Weapon Spread Patterns', 'MechWarrior Video Games', 'FPS Combat', 'WEAPON_SPREAD', NULL,
'Weapon spread affects accuracy at range. MWO features weapon spread where missiles and some energy weapons spread damage across multiple locations. The spread pattern depends on the weapon type and range. Missiles spread damage across multiple locations but can be focused by attacking from the side. Effective weapon spread management requires understanding the spread pattern and adjusting aim accordingly. Weapon spread adds a layer of tactical depth, requiring the player to consider the target size and distance when selecting weapons.',
'Missiles spread damage across locations. Focused by attacking from the side. Tactical depth in weapon selection.'),

('MECH_WARRIOR', 'FPS Solaris Mode', 'MechWarrior Video Games', 'FPS Multiplayer', 'SOLARIS', NULL,
'Solaris mode features intense 1v1 and 2v2 fights. MWO features Solaris weekend with 1v1 and 2v2 intense fights. Solaris mode is a special event that tests the player skills in head-to-head combat. The mode is named after the Solaris VII combat games, a popular attraction in the BattleTech universe. Solaris mode emphasizes individual skill and tactical prowess. Effective Solaris play requires understanding the mech capabilities and mastering combat techniques. Solaris mode is a proving ground for elite mech warriors.',
'1v1 and 2v2 intense fights. Tests individual skill and tactical prowess. Elite mech warrior proving ground.'),

('MECH_WARRIOR', 'FPS Drop Deck Mode', 'MechWarrior Video Games', 'FPS Multiplayer', 'DROP_DECK', NULL,
'Drop Deck mode allows multiple mechs per player. MWO features Drop Deck modes where players can bring up to 4 mechs to keep them in the fight longer. Drop Deck mode allows the player to switch between mechs during combat, providing flexibility and sustainability. Effective Drop Deck play requires understanding the strengths and weaknesses of each mech and switching at the right moments. Drop Deck mode adds a layer of strategic depth, allowing the player to adapt to different combat situations.',
'Up to 4 mechs per player. Switch between mechs during combat. Flexibility and sustainability.'),

('MECH_WARRIOR', 'FPS Trial of Position', 'MechWarrior Video Games', 'FPS Campaign', 'TRIAL', NULL,
'Trial of Position is a clan initiation ritual. MW2:31st Century Combat features a Trial of Position where the player proves their worth as a mech warrior. The trial is a combat test that determines the player rank and status within the clan. Successful completion of the trial grants the player the rank of MechWarrior and access to clan resources. The Trial of Position is a rite of passage that establishes the player credibility and reputation within the clan. Effective trial performance requires understanding the clan combat doctrines and demonstrating skill and courage.',
'Clan initiation ritual. Determines rank and status. Rite of passage for mech warriors.'),

('MECH_WARRIOR', 'FPS Blood Name Progression', 'MechWarrior Video Games', 'FPS Progression', 'BLOOD_NAME', NULL,
'Blood Name is the highest clan honor. MW5:Clans features the Blood Name as the ultimate goal for clan mech warriors. Achieving a Blood Name requires exceptional combat performance and recognition from the clan. The player plays as Jaden, who desires to be Star Commander and eventually obtain a blood name. Blood Name is a prestigious title that grants the warrior significant status and privileges within the clan. Effective Blood Name pursuit requires understanding the clan values and demonstrating exceptional skill and leadership.',
'Highest clan honor. Exceptional combat performance required. Prestigious title with significant status.'),

('MECH_WARRIOR', 'FPS Clan Interdiction', 'MechWarrior Video Games', 'FPS Factions', 'INTERDICTION', NULL,
'Clan interdiction adds unexpected threats. MW5:Mercenaries features Clan interdiction where Clan forces may appear as hostile third factions near Clan-controlled planets. The player can choose to engage or evacuate, with the employers compensating for removing the threat. Clan interdiction adds unpredictability and challenge to missions. Effective interdiction management requires understanding the Clan threat level and adapting tactics accordingly. Clan forces are generally superior to Inner Sphere forces, making interdiction encounters particularly dangerous.',
'Unexpected Clan threats near Clan-controlled planets. Engage or evacuate. Superior Clan forces.'),

('MECH_WARRIOR', 'FPS Pilot Stress System', 'MechWarrior Video Games', 'FPS Progression', 'STRESS', NULL,
'Pilot stress affects combat performance. BattleTech 2018 features a pilot stress system where pilots can become stressed or broken under pressure. Stress increases when the pilot takes damage, loses mechs, or faces overwhelming odds. Stressed pilots have reduced accuracy and may make poor decisions. Broken pilots cannot fight until they recover. Morale is replenished if the player is doing well in battle but not so much if losing. Effective stress management requires understanding the pilot psychological limits and providing support when needed. The stress system adds a layer of realism and strategic depth to pilot management.',
'Stress reduces accuracy and decision-making. Morale replenishment for recovery. Realistic pilot management.'),

('MECH_WARRIOR', 'FPS Pilot Specialization', 'MechWarrior Video Games', 'FPS Progression', 'SPECIALIZATION', NULL,
'Pilot specialization enhances combat effectiveness. BattleTech 2018 features pilot specialization where pilots can specialize in one of four branches (Gunnery, Piloting, Guts, Tactics) and have a secondary ability in another. MechCommander 2 features pilot skills that unlock specialized abilities. Effective pilot specialization requires understanding the pilot strengths and weaknesses and assigning them to appropriate mechs and roles. Pilot specialization adds a layer of strategic depth to pilot management.',
'Four branches: Gunnery, Piloting, Guts, Tactics. Secondary abilities for versatility. Strategic depth in pilot management.'),

('MECH_WARRIOR', 'FPS Combined Arms Tactics', 'MechWarrior Video Games', 'FPS Tactics', 'COMBINED_ARMS', NULL,
'Combined arms tactics integrate mechs, vehicles, and infantry. MechCommander series emphasizes combined arms tactics with mechs, vehicles, and infantry working together. Inner Sphere enemies use combined arms tactics with mechs, vehicles, and infantry. Effective combined arms understanding requires knowing the strengths and weaknesses of each unit type. Combined arms are essential for tactical planning and combat effectiveness.',
'Mechs, vehicles, and infantry working together. Inner Sphere doctrine. Essential for tactical planning.'),

('MECH_WARRIOR', 'FPS Resource Points System', 'MechWarrior Video Games', 'FPS Progression', 'RESOURCE_POINTS', NULL,
'Resource points provide in-mission support. MechCommander 2 features resource points that can be used to call down support vehicles such as repair trucks, stationary cannons, or new pilots. Resource points are collected by capturing enemy warehouses and supply trucks. Effective resource point usage requires understanding the support options and the mission requirements. Resource points are essential for sustained combat and mission success.',
'Call down support vehicles. Collected from enemy warehouses and supply trucks. Essential for sustained combat.'),

('MECH_WARRIOR', 'FPS Group Commands', 'MechWarrior Video Games', 'FPS Tactics', 'GROUP_COMMANDS', NULL,
'Group commands allow coordinated unit control. MechCommander 2 features group commands where the player can group mechs by using CTRL plus numbers 1-9. Mechs are broken down by short, medium, and long range for attack coordination. Effective group command usage requires understanding the unit capabilities and the mission requirements. Group commands are essential for coordinated combat and mission success.',
'Group mechs by CTRL+1-9. Broken down by range for attack coordination. Essential for coordinated combat.'),

('MECH_WARRIOR', 'FPS Faction Play', 'MechWarrior Video Games', 'FPS Multiplayer', 'FACTION_PLAY', NULL,
'Faction play provides prolonged team-based engagements. MWO features faction play where players lend their skills and allegiance to a faction in prolonged team-based engagements, conquering and defending planets in their name to earn rewards and expand the faction territory across the Inner Sphere. Effective faction play requires understanding the faction dynamics and the long-term implications of each decision. Faction play is essential for strategic multiplayer engagement.',
'Prolonged team-based engagements. Conquer and defend planets. Essential for strategic multiplayer engagement.'),

('MECH_WARRIOR', 'FPS Quickplay Mode', 'MechWarrior Video Games', 'FPS Multiplayer', 'QUICKPLAY', NULL,
'Quickplay mode provides fast action and casual play. MWO features quickplay where players can field their mech in team-based game modes and maps. Quickplay provides fast action and casual play. Effective quickplay usage requires understanding the game modes and maps. Quickplay is essential for casual multiplayer engagement and skill development.',
'Fast action and casual play. Team-based game modes and maps. Essential for casual multiplayer engagement.'),

('MECH_WARRIOR', 'FPS Currency and Experience', 'MechWarrior Video Games', 'FPS Progression', 'CURRENCY', NULL,
'Currency and experience systems provide the progression framework. MWO Legends features a currency system where players earn in-game currency through missions and events. Currency is used to purchase and customize mechs. Experience is used to unlock mech skills and customization options. Effective currency and experience usage requires understanding the mech capabilities and the customization options. Currency and experience systems are essential for progression and customization.',
'Earn currency and experience through missions and events. Purchase and customize mechs. Essential for progression and customization.'),

('MECH_WARRIOR', 'FPS Skill Trees', 'MechWarrior Video Games', 'FPS Progression', 'SKILL_TREES', NULL,
'Skill trees provide customization and specialization. MWO Legends features skill trees where players can unlock and upgrade abilities. Skill trees provide customization and specialization. Effective skill tree usage requires understanding the abilities and the customization options. Skill trees are essential for progression and customization.',
'Unlock and upgrade abilities. Customization and specialization. Essential for progression and customization.'),

('MECH_WARRIOR', 'FPS Loadout Customization', 'MechWarrior Video Games', 'FPS Progression', 'LOADOUT', NULL,
'Loadout customization provides tactical flexibility. MW5:Mercenaries features loadout customization where the player can customize the mech weapons, armor, and components. Loadout customization provides tactical flexibility. Effective loadout customization requires understanding the weapon characteristics and the mission requirements. Loadout customization is essential for tactical flexibility and optimization.',
'Customize weapons, armor, and components. Tactical flexibility. Essential for tactical flexibility and optimization.'),

('MECH_WARRIOR', 'FPS Weight Management', 'MechWarrior Video Games', 'FPS Customization', 'WEIGHT_MANAGEMENT', NULL,
'Weight management is essential for mech customization. MW5:Mercenaries features weight management where the player must balance the mech weight with the available tonnage. Weight management affects speed, armor, and weapon capacity. Effective weight management requires understanding the weight distribution and the available tonnage. Weight management is essential for mech customization and optimization.',
'Balance weight with available tonnage. Affects speed, armor, and weapon capacity. Essential for mech customization and optimization.'),

('MECH_WARRIOR', 'FPS Alpha Strike Timing', 'MechWarrior Video Games', 'FPS Combat', 'ALPHA_STRIKE', NULL,
'Alpha strike timing is critical for maximum damage. MW5:Mercenaries features alpha strike timing where the player fires all weapons simultaneously for maximum damage. Alpha strike timing requires understanding the weapon cooldowns and the combat scenarios. Effective alpha strike timing allows the player to deal maximum damage in a single burst. Alpha strike timing is essential for burst damage and tactical surprise.',
'Fire all weapons simultaneously for maximum damage. Understand weapon cooldowns and combat scenarios. Essential for burst damage and tactical surprise.'),

('MECH_WARRIOR', 'FPS Chain Fire Sequencing', 'MechWarrior Video Games', 'FPS Combat', 'CHAIN_FIRE', NULL,
'Chain fire sequencing manages heat and cooldowns. MW5:Mercenaries features chain fire sequencing where weapons fire in sequence to manage heat and cooldowns. Chain fire sequencing requires understanding the weapon characteristics and the combat scenarios. Effective chain fire sequencing allows the player to maintain sustained firepower without overheating. Chain fire sequencing is essential for sustained combat effectiveness.',
'Weapons fire in sequence to manage heat and cooldowns. Understand weapon characteristics and combat scenarios. Essential for sustained combat effectiveness.'),

('MECH_WARRIOR', 'FPS Heat Override Risk', 'MechWarrior Video Games', 'FPS Combat', 'HEAT_OVERRIDE', NULL,
'Heat override risk is a critical decision. MW5:Mercenaries features heat override risk where the player can override the automatic shutdown to continue fighting, but this risks internal damage and reactor explosion. Heat override risk requires understanding the heat management and the combat scenarios. Effective heat override risk management allows the player to continue fighting when necessary while minimizing the risk of reactor explosion. Heat override risk is essential for sustained combat effectiveness.',
'Override automatic shutdown to continue fighting. Risks internal damage and reactor explosion. Essential for sustained combat effectiveness.'),

('MECH_WARRIOR', 'FPS Cover Usage Strategy', 'MechWarrior Video Games', 'FPS Tactics', 'COVER', NULL,
'Cover usage protects the mech from enemy fire. MW5:Mercenaries emphasizes cover usage — get a building between you and the enemy, use rocks to block missile barges, and use hills to break target locks. Cover reduces incoming damage and provides protection from enemy fire. Effective cover usage requires understanding the terrain layout and using cover to protect the mech. Cover is essential for survivability and tactical positioning.',
'Buildings, rocks, and hills provide cover. Reduces incoming damage. Essential for survivability.'),

('MECH_WARRIOR', 'FPS Flanking Strategy', 'MechWarrior Video Games', 'FPS Tactics', 'FLANKING', NULL,
'Flanking strategy attacks from unexpected angles. MW5:Mercenaries features flanking strategy where the player attacks from unexpected angles to gain tactical advantages. Flanking strategy requires understanding the enemy positions and the terrain layout. Effective flanking strategy allows the player to attack from unexpected angles and gain tactical advantages. Flanking strategy is essential for tactical surprise and combat effectiveness.',
'Attack from unexpected angles to gain tactical advantages. Understand enemy positions and terrain layout. Essential for tactical surprise and combat effectiveness.'),

('MECH_WARRIOR', 'FPS Rear Armor Targeting', 'MechWarrior Video Games', 'FPS Tactics', 'REAR_ARMOR', NULL,
'Rear armor is typically weaker than front armor. MW5:Mercenaries emphasizes rear armor weakness — if a teammate is focused on the enemy, get behind them to target the often weaker rear armor. Rear armor is generally thinner than front armor, making it more vulnerable to damage. Effective rear armor targeting requires understanding the enemy positioning and maneuvering to attack from behind. Rear armor weakness is a key tactical consideration in combat.',
'Weaker rear armor is more vulnerable. Maneuver to attack from behind. Key tactical consideration.'),

('MECH_WARRIOR', 'FPS Elevation Advantage', 'MechWarrior Video Games', 'FPS Combat', 'ELEVATION', NULL,
'Elevation advantage improves targeting accuracy. MechCommander 2 features elevation advantage where mechs on higher ground have a higher chance of hitting attacking mechs in the head. MW5:Mercenaries includes elevation as a key mechanic, with hills providing elevation advantage and blocking missile targeting. Effective elevation management requires understanding the terrain layout and using elevation to gain tactical advantage. Elevation advantage is essential for weapon accuracy and tactical positioning.',
'Higher ground improves head hit chance. Hills block missile targeting. Essential for tactical advantage.'),

('MECH_WARRIOR', 'FPS Line of Sight', 'MechWarrior Video Games', 'FPS Combat', 'LINE_OF_SIGHT', NULL,
'Line of sight is essential for targeting. BattleTech 2018 emphasizes line of sight as a core mechanic. Certain weapons require unobstructed line of sight while others offer indirect fire. Terrain can block line of sight, providing cover and protection. Effective line of sight management requires understanding the terrain layout and using cover to protect the mech. Line of sight is essential for weapon accuracy and tactical positioning.',
'Unobstructed view required for some weapons. Terrain provides cover. Essential for weapon accuracy.'),

('MECH_WARRIOR', 'FPS Indirect Fire', 'MechWarrior Video Games', 'FPS Combat', 'INDIRECT_FIRE', NULL,
'Indirect fire allows attacks without line of sight. BattleTech 2018 features indirect fire as a core mechanic. Certain weapons, like LRMs, can fire indirectly, hitting targets behind cover or elevation. Indirect fire has reduced accuracy but can bypass cover and protection. Effective indirect fire usage requires understanding the weapon capabilities and the terrain layout. Indirect fire adds a layer of tactical depth, allowing the player to attack targets that are otherwise protected.',
'Attacks without line of sight. Reduced accuracy but bypasses cover. Tactical depth in combat.'),

('MECH_WARRIOR', 'FPS Endo-Steel Structure', 'MechWarrior Video Games', 'FPS Customization', 'ENDO_STEEL', NULL,
'Endo-Steel structure reduces weight and increases speed. Endo-Steel is a lightweight material that reduces the mech weight, allowing for more armor or weapons. Endo-Steel is a Clan technology that is rare in the Inner Sphere. Effective Endo-Steel usage requires understanding the weight distribution and the available tonnage. Endo-Steel is essential for speed and mobility.',
'Lightweight material for more armor or weapons. Clan technology, rare in Inner Sphere. Essential for speed and mobility.'),

('MECH_WARRIOR', 'FPS Ferro-Fibrous Armor', 'MechWarrior Video Games', 'FPS Customization', 'FERRO_FIBROUS', NULL,
'Ferro-Fibrous armor increases armor value without increasing weight. Ferro-Fibrous armor is a lightweight armor that provides more protection than standard armor without increasing the mech weight. Ferro-Fibrous is a Clan technology that is rare in the Inner Sphere. Effective Ferro-Fibrous usage requires understanding the armor distribution and the available tonnage. Ferro-Fibrous is essential for survivability and protection.',
'More protection without weight increase. Clan technology, rare in Inner Sphere. Essential for survivability.'),

('MECH_WARRIOR', 'FPS Double Heat Sinks', 'MechWarrior Video Games', 'FPS Customization', 'DOUBLE_HEAT_SINKS', NULL,
'Double Heat Sinks (DHS) dissipate 2 heat points per sink but take up twice the critical hit space. DHS is a Clan technology that is rare in the Inner Sphere. Standard heat sinks dissipate 1 heat point per sink. Effective DHS usage requires understanding the heat generation of the weapon loadout and the available critical hit space. DHS is essential for heat management and sustained combat.',
'Dissipate 2 heat/sink vs 1 heat/sink for standard. Clan technology. Essential for heat management.'),

('MECH_WARRIOR', 'FPS Clan Technology Superiority', 'MechWarrior Video Games', 'FPS Factions', 'CLAN_TECH', NULL,
'Clan technology provides superior performance. Clan mechs feature advanced technologies like Endo-Steel structure, Ferro-Fibrous armor, Double Heat Sinks, and superior weapons. Clan technology is superior to Inner Sphere technology, providing better performance in all aspects. MW5:Clans emphasizes Clan technology superiority, with Clan mechs being tougher, faster, and more heat resistant. Effective Clan technology usage requires understanding the advantages and disadvantages of each technology. Clan technology is essential for competitive combat.',
'Superior performance in all aspects. Endo-Steel, Ferro-Fibrous, DHS, and advanced weapons. Essential for competitive combat.'),

('MECH_WARRIOR', 'FPS Campaign Contexts', 'MechWarrior Video Games', 'FPS Campaign', 'CAMPAIGNS', NULL,
'MechWarrior FPS games feature different campaign contexts. MW2:31st Century Combat features the Refusal War between Clan Wolf and Clan Jade Falcon. MW3 features Operation Serpent, the Inner Sphere counterattack against the Clans. MW4:Vengeance features the Federated Commonwealth Civil War. MW5:Mercenaries features the Succession Wars with the player choosing sides. MW5:Clans features the Clan Invasion. Each campaign context provides unique lore, factions, and mission types. Understanding the campaign context is essential for immersion and strategic planning.',
'Refusal War, Operation Serpent, FC Civil War, Succession Wars, Clan Invasion. Unique lore and factions per campaign.'),

('MECH_WARRIOR', 'FPS Methodical Gameplay', 'MechWarrior Video Games', 'FPS Core', 'METHODICAL', NULL,
'Methodical gameplay is the hallmark of MechWarrior FPS games. MW5:Mercenaries features methodical gameplay where the player feels incredibly powerful as they lumber around and smash through buildings. Every cannon shot feels massive, and the player feels the impact on both themselves and the enemy. Battling from inside these massive walking tanks simply feels good. The methodical gameplay is retained outside of combat as the player spends hours customizing mechs, managing resources, and planning missions. Effective methodical gameplay requires patience, planning, and understanding the mech capabilities.',
'Massive cannon shots and impact feedback. Customization and resource management. Patience and planning required.'),

('MECH_WARRIOR', 'FPS Combat Simulation', 'MechWarrior Video Games', 'FPS Core', 'SIMULATION', NULL,
'Combat simulation is the core FPS experience. All MechWarrior FPS games aim to simulate the experience of piloting a BattleMech in combat. The simulation includes realistic physics, weapon behavior, and mech movement. MW2 was praised for its simulation-focused gameplay, with the control complexity likened to that of a flight simulator. MW5:Mercenaries maintains the simulation focus with methodical gameplay and high time to kill. The simulation experience is enhanced by the cockpit view, instrument panels, and weapon status indicators. Effective simulation requires understanding the mech capabilities and limitations.',
'Realistic physics, weapon behavior, and mech movement. Cockpit view and instrument panels. High time to kill.'),

('MECH_WARRIOR', 'FPS HUD Elements', 'MechWarrior Video Games', 'FPS Interface', 'HUD', NULL,
'HUD elements provide critical information. MW5:Mercenaries features a comprehensive HUD with damage indicators, heat gauges, weapon cooldown displays, and minimap. The damage indicator shows the current armor and internal structure integrity for each component. The heat gauge shows the current heat level and warning when approaching critical. Weapon cooldown displays show the status of each weapon group. The minimap shows enemy positions and objectives. Effective HUD usage requires understanding and monitoring all elements simultaneously. The HUD is essential for situational awareness and tactical decision-making.',
'Damage indicators, heat gauges, weapon cooldowns, and minimap. Essential for situational awareness.'),

('MECH_WARRIOR', 'FPS Targeting Computer', 'MechWarrior Video Games', 'FPS Interface', 'TARGETING_COMPUTER', NULL,
'Targeting computer aids weapon accuracy. MW5:Mercenaries features a targeting computer that predicts hit locations and shows weapon status. The targeting computer provides information about enemy positions, distances, and types. The HUD shows enemy contacts with range and bearing. Targeting computer range varies by mech and can be improved with upgrades. Electronic warfare can disrupt the targeting computer, making targeting more difficult. Effective targeting computer usage requires understanding the limitations and using sensors to compensate.',
'Predicts hit locations and shows weapon status. Enemy position and distance information. Can be disrupted by electronic warfare.'),

('MECH_WARRIOR', 'FPS Sensor Range', 'MechWarrior Video Games', 'FPS Sensors', 'SENSOR_RANGE', NULL,
'Sensor range determines detection capability. MW4:Vengeance features Beal active probes for longer radar range and faster target lock. MW5:Mercenaries includes sensor range as a key mechanic, with the player able to detect enemies within a certain radius. Sensor range varies by mech and can be improved with upgrades. Electronic warfare can disrupt sensors, reducing detection range. Effective sensor range management requires understanding the limitations and using terrain and tactics to compensate. Sensor range is essential for situational awareness and tactical decision-making.',
'Detection capability varies by mech. Upgrades improve range. Essential for situational awareness.'),

('MECH_WARRIOR', 'FPS Difficulty Settings', 'MechWarrior Video Games', 'FPS Campaign', 'DIFFICULTY', NULL,
'Difficulty settings affect combat challenge. MW5:Mercenaries features difficulty settings that affect enemy AI, damage, and rewards. Higher difficulty provides greater challenge and rewards. Effective difficulty setting selection requires understanding the player skill level and the desired challenge. Difficulty settings are essential for balancing challenge and enjoyment.',
'Affects enemy AI, damage, and rewards. Higher difficulty = greater challenge and rewards. Balance challenge and enjoyment.'),

('MECH_WARRIOR', 'FPS AI Behavior', 'MechWarrior Video Games', 'FPS Combat', 'AI_BEHAVIOR', NULL,
'AI behavior affects enemy tactics and strategies. MW5:Mercenaries features AI behavior that varies by difficulty and faction. Enemy AI can use cover, flank, and coordinate attacks. Effective AI behavior understanding requires knowing the enemy tactics and adapting accordingly. AI behavior is essential for tactical planning and combat effectiveness.',
'Varies by difficulty and faction. Uses cover, flanks, and coordinates attacks. Essential for tactical planning.'),

('MECH_WARRIOR', 'FPS Weather Effects', 'MechWarrior Video Games', 'FPS Environment', 'WEATHER', NULL,
'Weather effects combat visibility and performance. BattleTech 2018 includes weather effects like rain, snow, and fog that can affect visibility and combat performance. Weather effects can reduce sensor range and weapon accuracy. Effective weather effect management requires understanding the limitations and adapting tactics accordingly. Weather effects add a layer of realism and tactical depth to combat.',
'Rain, snow, and fog affect visibility and performance. Reduce sensor range and weapon accuracy. Realism and tactical depth.'),

('MECH_WARRIOR', 'FPS Time of Day Effects', 'MechWarrior Video Games', 'FPS Environment', 'TIME_OF_DAY', NULL,
'Time of day affects visibility and combat. MW3 and MW4:Vengeance feature time of day effects that can affect visibility and combat. Night fighting requires night vision or light amplification. Effective time of day management requires understanding the visibility limitations and adapting tactics accordingly. Time of day adds a layer of realism and tactical depth to combat.',
'Night fighting requires night vision. Visibility limitations affect combat. Realism and tactical depth.'),

('MECH_WARRIOR', 'FPS Hazardous Terrain', 'MechWarrior Video Games', 'FPS Environment', 'HAZARDOUS_TERRAIN', NULL,
'Hazardous terrain includes radioactive areas and high thermal zones. BattleTech 2018 includes radioactive terrain that can damage mechs and pilots over time, and high thermal areas that can increase heat generation and reduce heat dissipation. Hazardous terrain is a hazard that requires careful navigation and avoidance. Effective hazardous terrain management requires understanding the hazard level and the available routes. Hazardous terrain adds a layer of realism and tactical depth to combat.',
'Radioactive areas damage mechs and pilots. High thermal areas increase heat. Hazard requiring careful navigation.'),

('MECH_WARRIOR', 'FPS Contract System', 'MechWarrior Video Games', 'FPS Campaign', 'CONTRACTS', NULL,
'Contract system provides mission variety and rewards. MW5:Mercenaries uses a contract system where the player accepts missions from various factions, with negotiation and consequences. Each contract has specific objectives, rewards, and consequences. Effective contract system usage requires understanding the faction relationships and the mission requirements. Contract system is essential for campaign progression and reward acquisition.',
'Missions from various factions with negotiation and consequences. Essential for campaign progression and rewards.'),

('MECH_WARRIOR', 'FPS Negotiation System', 'MechWarrior Video Games', 'FPS Campaign', 'NEGOTIATION', NULL,
'Negotiation system affects mission terms and rewards. MW5:Mercenaries features a negotiation system where the player can negotiate mission terms and rewards. Effective negotiation requires understanding the faction relationships and the mission requirements. Negotiation system is essential for maximizing rewards and minimizing risks.',
'Negotiate mission terms and rewards. Essential for maximizing rewards and minimizing risks. Understand faction relationships.'),

('MECH_WARRIOR', 'FPS Consequence System', 'MechWarrior Video Games', 'FPS Campaign', 'CONSEQUENCES', NULL,
'Consequence system affects faction relationships and mission availability. MW5:Mercenaries features a consequence system where each victory has consequences that affect faction relationships and mission availability. Effective consequence system usage requires understanding the faction dynamics and the long-term implications of each decision. Consequence system is essential for strategic planning and campaign progression.',
'Each victory has consequences. Affects faction relationships and mission availability. Essential for strategic planning.'),

('MECH_WARRIOR', 'FPS Pilot Hiring and Training', 'MechWarrior Video Games', 'FPS Progression', 'PILOT_HIRING', NULL,
'Pilot hiring and training expand the mercenary outfit. MW5:Mercenaries features pilot hiring where the player can hire new pilots to join their mercenary outfit. Pilots can be sent to training to gain XP and traits. Pilot hiring and training require understanding the pilot capabilities and the available resources. Effective pilot hiring and training allow the player to expand their mercenary outfit and take on more missions. Pilot hiring and training are essential for growth and progression.',
'Hire new pilots and send them to training. Understand pilot capabilities and available resources. Essential for growth and progression.'),

('MECH_WARRIOR', 'FPS Financial Management', 'MechWarrior Video Games', 'FPS Progression', 'FINANCIAL', NULL,
'Financial management is essential for sustainability. MW5:Mercenaries features financial management where the player must manage their resources to afford mech repairs, new weapons, and pilot hiring. Financial management requires understanding the income and expenses. Effective financial management allows the player to grow their mercenary outfit and acquire better equipment. Financial management is essential for sustainability and progression.',
'Manage resources for repairs, weapons, and pilot hiring. Understand income and expenses. Essential for sustainability and progression.'),

('MECH_WARRIOR', 'FPS Role-Based Loadouts', 'MechWarrior Video Games', 'FPS Customization', 'ROLE_LOADOUTS', NULL,
'Role-based loadouts optimize for specific roles. MW5:Mercenaries features role-based loadouts where the player can create loadouts optimized for specific roles such as scout, assault, support, or command. Role-based loadouts require understanding the role requirements and the weapon characteristics. Effective role-based loadouts allow the player to optimize combat effectiveness for specific roles and missions. Role-based loadouts are essential for tactical flexibility and combat effectiveness.',
'Optimized for scout, assault, support, or command roles. Understand role requirements and weapon characteristics. Essential for tactical flexibility.'),

('MECH_WARRIOR', 'FPS Terrain-Specific Loadouts', 'MechWarrior Video Games', 'FPS Customization', 'TERRAIN_LOADOUTS', NULL,
'Terrain-specific loadouts optimize for specific terrain. MW5:Mercenaries features terrain-specific loadouts where the player creates loadouts optimized for specific terrain types including urban, open terrain, forest, desert, and mountain. Terrain-specific loadouts require understanding the terrain characteristics and the weapon characteristics. Effective terrain-specific loadouts allow the player to optimize combat effectiveness for specific terrain types. Terrain-specific loadouts are essential for terrain adaptation and combat effectiveness.',
'Optimized for urban, open, forest, desert, and mountain terrain. Understand terrain characteristics and weapon characteristics. Essential for terrain adaptation.'),

('MECH_WARRIOR', 'FPS Mission-Specific Loadouts', 'MechWarrior Video Games', 'FPS Customization', 'MISSION_LOADOUTS', NULL,
'Mission-specific loadouts optimize for specific missions. MW5:Mercenaries features mission-specific loadouts where the player creates loadouts optimized for specific missions and objectives. Mission-specific loadouts require understanding the mission requirements and the weapon characteristics. Effective mission-specific loadouts allow the player to optimize combat effectiveness for specific missions and objectives. Mission-specific loadouts are essential for mission success and optimization.',
'Optimized for specific missions and objectives. Understand mission requirements and weapon characteristics. Essential for mission success.'),

('MECH_WARRIOR', 'FPS Weapon Strategy Types', 'MechWarrior Video Games', 'FPS Tactics', 'WEAPON_STRATEGY', NULL,
'Weapon strategy types include energy, ballistic, missile, and combined approaches. Energy weapon strategy maximizes sustained firepower but generates heat. Ballistic weapon strategy maximizes burst damage but has long cooldowns. Missile weapon strategy provides area damage but requires ammo management. Combined weapon strategy balances firepower and heat by mixing weapon types. Effective weapon strategy requires understanding the weapon characteristics and the combat scenarios. Weapon strategy is essential for optimized combat and firepower.',
'Energy=sustained, Ballistic=burst, Missile=area, Combined=balanced. Understand weapon characteristics and combat scenarios. Essential for optimized combat.'),

('MECH_WARRIOR', 'FPS Component Targeting Strategies', 'MechWarrior Video Games', 'FPS Tactics', 'COMPONENT_TARGETING', NULL,
'Component targeting strategies include head shots, leg shots, arm shots, torso shots, and ammo bin shots. Head shots target the cockpit for instant kill. Leg shots target the legs to immobilize the enemy. Arm shots target the arms to disable weapons. Torso shots target the torso for maximum damage. Center torso shots target the reactor for catastrophic explosion. Ammo bin shots target the ammo bins for chain reactions. Effective component targeting requires understanding the component layout and the weapon characteristics. Component targeting is essential for tactical advantage and damage optimization.',
'Head=kill, Leg=immobilize, Arm=disable, Torso=damage, Center Torso=explosion, Ammo=chain reaction. Essential for tactical advantage.'),

('MECH_WARRIOR', 'FPS Missile Defense Strategy', 'MechWarrior Video Games', 'FPS Tactics', 'MISSILE_DEFENSE', NULL,
'Missile defense strategy protects against missile attacks. MW5:Mercenaries features missile defense strategy where the player uses AMS and terrain to defend against missile attacks. Missile defense strategy requires understanding the missile attack patterns and the available defenses. Effective missile defense strategy allows the player to protect against missile attacks and maintain combat effectiveness. Missile defense strategy is essential for sustained combat and missile defense.',
'Use AMS and terrain to defend against missile attacks. Understand missile attack patterns and available defenses. Essential for sustained combat.'),

('MECH_WARRIOR', 'FPS Heat Management Strategy', 'MechWarrior Video Games', 'FPS Tactics', 'HEAT_STRATEGY', NULL,
'Heat management strategy prevents overheating. MW5:Mercenaries features heat management strategy where the player manages heat to prevent overheating and shutdown. Heat management strategy requires understanding the heat generation and dissipation. Effective heat management strategy allows the player to maintain sustained firepower and prevent overheating. Heat management strategy is essential for sustained combat and overheating prevention.',
'Manage heat to prevent overheating and shutdown. Understand heat generation and dissipation. Essential for sustained combat.'),

('MECH_WARRIOR', 'FPS Vulnerability Management', 'MechWarrior Video Games', 'FPS Tactics', 'VULNERABILITY', NULL,
'Vulnerability management reduces exposure to enemy fire. MW5:Mercenaries features vulnerability management where the player uses cover and terrain to reduce exposure to enemy fire. Vulnerability management requires understanding the terrain layout and the enemy positions. Effective vulnerability management allows the player to reduce exposure and maintain combat effectiveness. Vulnerability management is essential for survivability and combat effectiveness.',
'Use cover and terrain to reduce exposure to enemy fire. Understand terrain layout and enemy positions. Essential for survivability.'),

('MECH_WARRIOR', 'FPS Terrain Usage Strategy', 'MechWarrior Video Games', 'FPS Tactics', 'TERRAIN_STRATEGY', NULL,
'Terrain usage strategy optimizes battlefield positioning. MW5:Mercenaries features terrain usage strategy where the player uses terrain to gain tactical advantages. Terrain usage strategy requires understanding the terrain layout and the enemy positions. Effective terrain usage strategy allows the player to gain tactical advantages and maintain combat effectiveness. Terrain usage strategy is essential for battlefield positioning and combat effectiveness.',
'Use terrain to gain tactical advantages. Understand terrain layout and enemy positions. Essential for battlefield positioning.'),

('MECH_WARRIOR', 'FPS Monthly Events', 'MechWarrior Video Games', 'FPS Multiplayer', 'MONTHLY_EVENTS', NULL,
'Monthly events provide special rewards and modes. MWO Legends features monthly events that grant access to even more items, currency, and mechs. Monthly events provide variety and excitement. Effective monthly event participation requires understanding the event rules and objectives. Monthly events are essential for variety and excitement in multiplayer play.',
'Special rewards and modes. Items, currency, and mechs. Essential for variety and excitement.'),

('MECH_WARRIOR', 'FPS Unlimited Ammo Mode', 'MechWarrior Video Games', 'FPS Multiplayer', 'UNLIMITED_AMMO', NULL,
'Unlimited Ammo mode removes ammo constraints. MWO features Unlimited Ammo as a modifier in special events. Unlimited Ammo mode allows the player to fire weapons without worrying about ammo depletion. This mode emphasizes heat management and cooldowns over ammo conservation. Effective Unlimited Ammo play requires understanding the heat and cooldown characteristics of each weapon. Unlimited Ammo mode is ideal for testing loadouts and practicing combat without the constraint of ammo management.',
'No ammo depletion. Emphasizes heat and cooldown management. Ideal for testing and practice.'),

('MECH_WARRIOR', 'FPS Extra Armor Mode', 'MechWarrior Video Games', 'FPS Multiplayer', 'EXTRA_ARMOR', NULL,
'Extra Armor mode increases survivability. MWO features Extra Armor as a modifier in special events. Extra Armor mode provides additional armor to all mechs, increasing survivability and time to kill. This mode emphasizes sustained combat and tactical positioning over quick kills. Effective Extra Armor play requires understanding the armor distribution and targeting key components. Extra Armor mode is ideal for testing loadouts and practicing combat with increased durability.',
'Additional armor for all mechs. Increased survivability and time to kill. Sustained combat emphasis.'),

('MECH_WARRIOR', 'FPS Wave Missions', 'MechWarrior Video Games', 'FPS Modes', 'WAVE_MISSIONS', NULL,
'Wave missions provide escalating challenges. MW4:Vengeance features wave missions where the player faces waves of enemy mechs. Each wave increases in difficulty and intensity. Effective wave mission strategy requires understanding the enemy composition and adapting tactics accordingly. Wave missions are essential for testing loadouts and practicing combat.',
'Escalating waves of enemy mechs. Increasing difficulty and intensity. Essential for testing and practice.'),

('MECH_WARRIOR', 'FPS Deathmatch Mode', 'MechWarrior Video Games', 'FPS Multiplayer', 'DEATHMATCH', NULL,
'Deathmatch mode provides competitive combat. MW4:Vengeance features deathmatch mode where players fight waves of enemy mechs in a competitive environment. Deathmatch mode emphasizes individual skill and tactical prowess. Effective deathmatch play requires understanding the mech capabilities and mastering combat techniques. Deathmatch mode is essential for competitive play and skill development.',
'Competitive combat against waves of enemies. Emphasizes individual skill and tactical prowess. Essential for competitive play.'),

('MECH_WARRIOR', 'FPS Team-Based Modes', 'MechWarrior Video Games', 'FPS Multiplayer', 'TEAM_MODES', NULL,
'Team-based modes provide cooperative competitive combat. MWO features team deathmatch, capture the flag, and objective-based missions where players compete in teams. Team-based modes emphasize teamwork and coordination. Effective team-based play requires understanding the team composition and coordinating with teammates. Team-based modes are essential for team-based competitive play.',
'Team deathmatch, capture the flag, objective-based. Emphasizes teamwork and coordination. Essential for team-based play.'),

('MECH_WARRIOR', 'FPS Private Match', 'MechWarrior Video Games', 'FPS Multiplayer', 'PRIVATE_MATCH', NULL,
'Private match provides custom competitive combat. MWO features private match where players can create custom matches with specific rules and settings. Private match allows for customized gameplay and experimentation. Effective private match usage requires understanding the rules and settings. Private match is essential for custom competitive play and experimentation.',
'Custom matches with specific rules and settings. Allows for customized gameplay and experimentation. Essential for custom competitive play.'),

('MECH_WARRIOR', 'FPS Territory Control', 'MechWarrior Video Games', 'FPS Multiplayer', 'TERRITORY_CONTROL', NULL,
'Territory control provides strategic multiplayer objectives. MWO Legends features territory control where players conquer and defend planets for their faction. Territory control emphasizes long-term strategic planning and teamwork. Effective territory control requires understanding the faction dynamics and the map layout. Territory control is essential for strategic multiplayer engagement.',
'Conquer and defend planets for your faction. Long-term strategic planning and teamwork. Essential for strategic multiplayer.'),

('MECH_WARRIOR', 'FPS Inner Sphere Map', 'MechWarrior Video Games', 'FPS World', 'INNER_SPHERE_MAP', NULL,
'Inner Sphere map provides the strategic overview. MWO Legends features the Inner Sphere map where players can see the faction territories and the ongoing conflicts. The Inner Sphere map provides the strategic overview of the multiplayer world. Effective Inner Sphere map usage requires understanding the faction dynamics and the map layout. Inner Sphere map is essential for strategic multiplayer engagement.',
'Strategic overview of faction territories and conflicts. Essential for strategic multiplayer engagement. Understand faction dynamics.'),

('MECH_WARRIOR', 'FPS Rookie Progression', 'MechWarrior Video Games', 'FPS Progression', 'ROOKIE', NULL,
'Rookie progression provides the starting experience. MWO Legends features rookie progression where the player starts with 16 pre-configured powerful mechs and earns in-game currency and experience to purchase and customize their own stable of mechs. Effective rookie progression requires understanding the mech capabilities and the customization options. Rookie progression is essential for new player onboarding and skill development.',
'16 pre-configured mechs for starters. Earn currency and experience for customization. Essential for new player onboarding.'),

('MECH_WARRIOR', 'FPS Mech-Pilot Synergy', 'MechWarrior Video Games', 'FPS Progression', 'MECH_PILOT_SYNERGY', NULL,
'Mech-pilot synergy enhances combat effectiveness. MW5:Clans features mech-pilot synergy where pilots can have special traits that enhance their performance with specific mechs. Mech-pilot synergy requires understanding the pilot traits and the mech characteristics. Effective mech-pilot synergy allows the player to optimize combat effectiveness and mission success. Mech-pilot synergy is essential for sustained combat effectiveness and pilot management.',
'Pilots have special traits for specific mechs. Understand pilot traits and mech characteristics. Essential for sustained combat effectiveness.'),

('MECH_WARRIOR', 'FPS Customization Depth', 'MechWarrior Video Games', 'FPS Customization', 'CUSTOMIZATION_DEPTH', NULL,
'Mech customization depth provides extensive personalization. MW5:Mercenaries features extensive mech customization where the player can customize every aspect of the mech including weapons, armor, components, and internal systems. Mech customization depth requires understanding the mech characteristics and the available options. Effective mech customization depth allows the player to create unique and optimized mechs for different missions and combat scenarios. Mech customization depth is essential for personalization and optimization.',
'Customize every aspect of the mech. Understand mech characteristics and available options. Essential for personalization and optimization.'),

('MECH_WARRIOR', 'FPS Loadout Variety', 'MechWarrior Video Games', 'FPS Customization', 'LOADOUT_VARIETY', NULL,
'Loadout variety provides tactical flexibility. MW5:Mercenaries features loadout variety where the player can create different loadouts for different missions and combat scenarios. Loadout variety requires understanding the weapon characteristics and the mission requirements. Effective loadout variety allows the player to adapt to different combat scenarios and optimize combat effectiveness. Loadout variety is essential for tactical flexibility and combat effectiveness.',
'Different loadouts for different missions and scenarios. Understand weapon characteristics and mission requirements. Essential for tactical flexibility.'),

('MECH_WARRIOR', 'FPS Hardpoint Customization', 'MechWarrior Video Games', 'FPS Customization', 'HARDPOINT_CUSTOMIZATION', NULL,
'Hardpoint customization provides weapon placement optimization. MW4:Vengeance features hardpoint customization where specific weapons can only be mounted on designated hardpoints. Hardpoint customization provides weapon placement optimization. Effective hardpoint customization requires understanding the hardpoint layout and the weapon characteristics. Hardpoint customization is essential for weapon placement optimization and combat effectiveness.',
'Specific weapons on designated hardpoints. Weapon placement optimization. Essential for weapon placement optimization and combat effectiveness.'),

('MECH_WARRIOR', 'FPS Armor Distribution', 'MechWarrior Video Games', 'FPS Customization', 'ARMOR_DISTRIBUTION', NULL,
'Armor distribution provides protection optimization. MW5:Clans features armor distribution where the player can manually add armor to each slot. Armor distribution provides protection optimization. Effective armor distribution requires understanding the armor distribution and the mission requirements. Armor distribution is essential for protection optimization and survivability.',
'Manually add armor to each slot. Protection optimization. Essential for protection optimization and survivability.'),

('MECH_WARRIOR', 'FPS Tonnage Limits', 'MechWarrior Video Games', 'FPS Customization', 'TONNAGE_LIMITS', NULL,
'Tonnage limits restrict mech customization. MW5:Mercenaries features tonnage limits where the player cannot exceed the mech maximum tonnage. Tonnage limits affect speed, armor, and weapon capacity. Effective tonnage limit management requires understanding the weight distribution and the available tonnage. Tonnage limits are essential for mech customization and optimization.',
'Cannot exceed maximum tonnage. Affects speed, armor, and weapon capacity. Essential for mech customization and optimization.'),

('MECH_WARRIOR', 'FPS Tradeoff Decisions', 'MechWarrior Video Games', 'FPS Customization', 'TRADEOFFS', NULL,
'Tradeoff decisions are key customization choices. MW5:Mercenaries features tradeoff decisions where the player must choose between speed vs armor, weapon vs armor, and heat vs firepower. Faster mechs have less armor, while heavier mechs have more armor but less speed. Heavily armed mechs have less armor, while heavily armored mechs have fewer weapons. Energy weapons generate more heat but provide sustained firepower, while ballistic weapons generate less heat but have longer cooldowns. Effective tradeoff decisions require understanding the mission requirements and the combat scenarios. Tradeoff decisions are essential for mech customization and optimization.',
'Speed vs armor, weapon vs armor, heat vs firepower. Understand mission requirements and combat scenarios. Essential for mech customization and optimization.'),

('MECH_WARRIOR', 'FPS Cooldown Management', 'MechWarrior Video Games', 'FPS Combat', 'COOLDOWN_MANAGEMENT', NULL,
'Cooldown management is essential for sustained firepower. MW5:Mercenaries features cooldown management where the player must manage the weapon cooldowns to maintain sustained firepower. Cooldown management requires understanding the weapon characteristics and the combat scenarios. Effective cooldown management allows the player to maintain sustained firepower without overheating. Cooldown management is essential for sustained combat effectiveness.',
'Manage weapon cooldowns for sustained firepower. Understand weapon characteristics and combat scenarios. Essential for sustained combat effectiveness.'),

('MECH_WARRIOR', 'FPS Weapon Group Strategy', 'MechWarrior Video Games', 'FPS Combat', 'WEAPON_GROUP_STRATEGY', NULL,
'Weapon group strategy optimizes firepower. MW5:Mercenaries features weapon group strategy where the player can create multiple weapon groups with different firing patterns. Weapon group strategy requires understanding the weapon characteristics and the combat scenarios. Effective weapon group strategy allows the player to optimize firepower and heat management. Weapon group strategy is essential for tactical flexibility and combat effectiveness.',
'Multiple weapon groups with different firing patterns. Understand weapon characteristics and combat scenarios. Essential for tactical flexibility and combat effectiveness.'),

('MECH_WARRIOR', 'FPS Reactor Explosion Prevention', 'MechWarrior Video Games', 'FPS Combat', 'REACTOR_PREVENTION', NULL,
'Reactor explosion prevention is critical for survival. MW5:Mercenaries features reactor explosion prevention where the player must manage heat and damage to avoid reactor explosion. Reactor explosion prevention requires understanding the heat management and the combat scenarios. Effective reactor explosion prevention allows the player to survive and continue fighting. Reactor explosion prevention is essential for pilot survival and mission success.',
'Manage heat and damage to avoid reactor explosion. Understand heat management and combat scenarios. Essential for pilot survival and mission success.'),

('MECH_WARRIOR', 'FPS Ejection Decision', 'MechWarrior Video Games', 'FPS Combat', 'EJECTION_DECISION', NULL,
'Ejection decision is a last resort. MW5:Mercenaries features ejection decision where the player can eject to survive when the mech is critically damaged. Ejection decision requires understanding the mech condition and the combat scenarios. Effective ejection decision allows the player to survive and continue the mission with another mech. Ejection decision is essential for pilot survival and mission continuation.',
'Eject to survive when mech is critically damaged. Understand mech condition and combat scenarios. Essential for pilot survival and mission continuation.'),

('MECH_WARRIOR', 'FPS Salvage Value', 'MechWarrior Video Games', 'FPS Progression', 'SALVAGE_VALUE', NULL,
'Salvage value determines the rewards from destroyed mechs. MW5:Mercenaries features salvage value where the player can salvage destroyed enemy mechs for weapons and components. Salvage value depends on the mech condition and available components. Effective salvage value management requires understanding the mech condition and the available components. Salvage value is essential for resource acquisition and progression.',
'Salvage destroyed enemy mechs for weapons and components. Depends on mech condition and available components. Essential for resource acquisition and progression.'),

('MECH_WARRIOR', 'FPS Repair Costs', 'MechWarrior Video Games', 'FPS Progression', 'REPAIR_COSTS', NULL,
'Repair costs scale with damage taken. MW5:Mercenaries features repair costs where the player must pay for mech repairs after missions. Repair costs scale with damage taken — the more damage a mech takes, the more costly the repair bill. Effective repair cost management requires understanding the damage taken and the available resources. Repair costs are essential for resource management and sustainability.',
'Pay for mech repairs after missions. Scale with damage taken. Essential for resource management and sustainability.'),

('MECH_WARRIOR', 'FPS Pilot Roster Management', 'MechWarrior Video Games', 'FPS Progression', 'PILOT_ROSTER', NULL,
'Pilot roster management is essential for sustainability. MW5:Mercenaries features pilot roster management where the player must manage their pilot roster to ensure they have enough pilots for missions. Pilot roster management requires understanding the pilot capabilities and the available resources. Effective pilot roster management allows the player to maintain their pilot roster and continue missions. Pilot roster management is essential for sustained combat effectiveness and pilot management.',
'Manage pilot roster to ensure enough pilots for missions. Understand pilot capabilities and available resources. Essential for sustained combat effectiveness.'),

('MECH_WARRIOR', 'FPS Pilot Health and Recovery', 'MechWarrior Video Games', 'FPS Progression', 'PILOT_HEALTH', NULL,
'Pilot health and recovery affect combat performance. BattleTech 2018 features pilot health where pilots can take damage in combat, reducing their effectiveness. Injured pilots can receive medical treatment to restore health and combat effectiveness. Effective pilot health management requires understanding the pilot condition and the available resources. Pilot health and recovery are essential for sustained combat effectiveness and pilot management.',
'Pilots can take damage in combat. Medical treatment restores health. Essential for sustained combat effectiveness and pilot management.'),

('MECH_WARRIOR', 'FPS Pilot Death and Replacement', 'MechWarrior Video Games', 'FPS Progression', 'PILOT_DEATH', NULL,
'Pilot death and replacement are permanent losses. BattleTech 2018 features pilot death where pilots can die in combat if they take excessive damage or stress. Pilot death is permanent, requiring the player to hire a replacement. Effective pilot death prevention requires understanding the pilot health status and providing support when needed. Pilot death and replacement are essential for realistic pilot management and combat effectiveness. Pilots can die in combat and injuries are so common place that its always recommended to have alternates just in case.',
'Permanent loss requiring replacement. Realistic pilot management. Have alternates ready.'),

('MECH_WARRIOR', 'FPS Shutdown Recovery', 'MechWarrior Video Games', 'FPS Tactics', 'SHUTDOWN_RECOVERY', NULL,
'Shutdown recovery restores combat effectiveness. MW5:Mercenaries features shutdown recovery where the player must recover from shutdown to resume combat. Shutdown recovery requires understanding the heat dissipation and the combat scenarios. Effective shutdown recovery allows the player to resume combat quickly and minimize vulnerability. Shutdown recovery is essential for sustained combat and vulnerability reduction.',
'Recover from shutdown to resume combat. Understand heat dissipation and combat scenarios. Essential for sustained combat and vulnerability reduction.'),

('MECH_WARRIOR', 'FPS Overheating Prevention', 'MechWarrior Video Games', 'FPS Tactics', 'OVERHEATING_PREVENTION', NULL,
'Overheating prevention is critical for sustained combat. MW5:Mercenaries features overheating prevention where the player must manage heat to prevent overheating and shutdown. Overheating prevention requires understanding the heat generation and dissipation. Effective overheating prevention allows the player to maintain sustained firepower and prevent shutdown. Overheating prevention is essential for sustained combat and overheating prevention.',
'Manage heat to prevent overheating and shutdown. Understand heat generation and dissipation. Essential for sustained combat and overheating prevention.'),

('MECH_WARRIOR', 'FPS Knockdown Recovery', 'MechWarrior Video Games', 'FPS Combat', 'KNOCKDOWN_RECOVERY', NULL,
'Knockdown recovery is essential for survivability. When a mech is knocked down, it is temporarily unable to move or fire effectively. The player must recover balance before resuming combat. MW5:Mercenaries simulates knockdown recovery, with the player needing to stabilize the mech before continuing. Effective knockdown recovery requires understanding the mech stability limits and adjusting movement and firing accordingly. Knockdown recovery is essential for maintaining combat effectiveness.',
'Temporarily immobilized when knocked down. Stabilize before resuming combat. Essential for combat effectiveness.'),

('MECH_WARRIOR', 'FPS Physical Attacks', 'MechWarrior Video Games', 'FPS Combat', 'PHYSICAL_ATTACKS', NULL,
'Physical attacks include punching, kicking, and ramming. BattleTech 2018 features physical attacks as a core mechanic. Punching and kicking can damage the enemy mech and knock them down. Ramming can push the enemy mech back or knock them down. Physical attacks are risky but can be effective in close-quarters situations. Effective physical attack usage requires understanding the mech capabilities and the enemy positioning. Physical attacks add a layer of tactical depth to close-quarters combat.',
'Punching, kicking, and ramming. Risky but effective in close quarters. Tactical depth in melee combat.'),

('MECH_WARRIOR', 'FPS Melee Combat', 'MechWarrior Video Games', 'FPS Combat', 'MELEE', NULL,
'Melee combat allows close-quarters attacks. BattleTech 2018 features melee combat with the Juggernaut ability dealing extra damage on successful melee attacks. MW5:Mercenaries includes melee combat as a last resort when weapons are depleted or at extremely close range. Melee combat is risky but can be effective in close-quarters situations. Effective melee combat requires understanding the mech capabilities and the enemy positioning. Melee combat is a last resort but can be decisive in close-quarters situations.',
'Close-quarters attacks with extra damage. Risky but effective in close quarters. Last resort but decisive.'),

('MECH_WARRIOR', 'FPS Close Range Combat', 'MechWarrior Video Games', 'FPS Combat', 'CLOSE_RANGE', NULL,
'Close range weaponry excels in melee combat. MW5:Mercenaries emphasizes close range weaponry — if a mech has close range weaponry and you have long range, maintain distance to avoid being overwhelmed. Close range weapons like SRMs and machine guns are effective at short ranges but lose effectiveness at longer ranges. Effective close range combat requires understanding the weapon ranges and maintaining optimal distance. Close range weaponry is essential for brawling and close-quarters combat.',
'SRMs and machine guns excel at short ranges. Maintain optimal distance. Essential for brawling.'),

('MECH_WARRIOR', 'FPS Long Range Sniping', 'MechWarrior Video Games', 'FPS Combat', 'LONG_RANGE', NULL,
'Long range sniping allows attacks from distance. MW5:Mercenaries features long range sniping with weapons like LRMs and ER Large Lasers. Long range sniping allows the player to attack enemies from a safe distance, avoiding return fire. Effective long range sniping requires understanding the weapon ranges and maintaining distance. Long range sniping is essential for controlling the battlefield and eliminating high-value targets.',
'LRMs and ER Large Lasers for long range. Safe distance from enemies. Essential for battlefield control.'),

('MECH_WARRIOR', 'FPS Support Vehicles', 'MechWarrior Video Games', 'FPS Progression', 'SUPPORT_VEHICLES', NULL,
'Support vehicles provide in-mission assistance. MechCommander 2 features support vehicles such as repair trucks, stationary cannons, and new pilots. Support vehicles can be called down using resource points. Effective support vehicle usage requires understanding the support options and the mission requirements. Support vehicles are essential for sustained combat and mission success.',
'Repair trucks, stationary cannons, and new pilots. Called down using resource points. Essential for sustained combat.'),

('MECH_WARRIOR', 'FPS Formation Tactics', 'MechWarrior Video Games', 'FPS Tactics', 'FORMATION', NULL,
'Formation tactics coordinate unit movement and fire. MechCommander 2 features formation tactics where the player can coordinate unit movement and fire. Formations include wedge, line, and column. Effective formation tactic usage requires understanding the unit capabilities and the mission requirements. Formation tactics are essential for coordinated combat and mission success.',
'Wedge, line, and column formations. Coordinate movement and fire. Essential for coordinated combat.'),

('MECH_WARRIOR', 'FPS Mission Briefings and Debriefings', 'MechWarrior Video Games', 'FPS Campaign', 'BRIEFINGS', NULL,
'Mission briefings and debriefings provide context and feedback. MW2:31st Century Combat features extensive data links and situation reports for every mission. MW5:Mercenaries includes mission briefings that provide context and information about the mission objectives and enemy forces, and debriefings that provide feedback and rewards for mission completion. Effective mission briefing and debriefing usage requires understanding the context and planning accordingly. Mission briefings and debriefings are essential for situational awareness, tactical planning, learning, and improvement.',
'Context, information, feedback, and rewards. Essential for situational awareness, tactical planning, learning, and improvement.'),

('MECH_WARRIOR', 'FPS Victory Conditions', 'MechWarrior Video Games', 'FPS Campaign', 'VICTORY_CONDITIONS', NULL,
'Victory conditions determine mission success. MW5:Mercenaries features victory conditions such as destroying all enemy units, capturing objectives, or surviving for a set number of turns. Victory conditions may vary by mission type and difficulty. Effective victory condition understanding requires knowing the requirements and planning accordingly. Victory conditions are essential for mission success and reward acquisition.',
'Destroy enemies, capture objectives, or survive. Varies by mission type and difficulty. Essential for mission success.'),

('MECH_WARRIOR', 'FPS Battlefield Objectives', 'MechWarrior Video Games', 'FPS Campaign', 'BATTLEFIELD_OBJECTIVES', NULL,
'Battlefield objectives define the mission goals. MW5:Mercenaries features battlefield objectives such as capturing positions, destroying enemy units, or defending locations. Battlefield objectives may have time limits or specific victory conditions. Effective battlefield objective completion requires understanding the objectives and adapting tactics accordingly. Battlefield objectives are essential for mission success and reward acquisition.',
'Capture, destroy, or defend. Time limits and specific victory conditions. Essential for mission success and rewards.'),

('MECH_WARRIOR', 'FPS DropShip Deployment', 'MechWarrior Video Games', 'FPS Environment', 'DROPSHIP', NULL,
'DropShip deployment provides access to the battlefield. MW5:Mercenaries features DropShip deployment where the player mech is transported to the battlefield via DropShip. DropShips can deploy troops and provide fire support. Effective DropShip deployment requires understanding the deployment zone and the battlefield layout. DropShip deployment is essential for battlefield access and tactical positioning.',
'Transport to battlefield via DropShip. Deploy troops and provide fire support. Essential for battlefield access.'),

('MECH_WARRIOR', 'FPS Battlefield Layout', 'MechWarrior Video Games', 'FPS Environment', 'BATTLEFIELD', NULL,
'Battlefield layout affects tactical decisions. MW5:Mercenaries features diverse battlefield layouts including urban areas, forests, deserts, and mountains. The battlefield layout determines the available cover, elevation, and movement options. Effective battlefield layout understanding requires analyzing the terrain and planning accordingly. Battlefield layout is essential for tactical positioning and combat effectiveness.',
'Diverse layouts: urban, forests, deserts, mountains. Determines cover, elevation, and movement. Essential for tactical positioning.'),

('MECH_WARRIOR', 'FPS Cover Types', 'MechWarrior Video Games', 'FPS Environment', 'COVER_TYPES', NULL,
'Cover types provide different levels of protection. BattleTech 2018 includes various cover types including buildings, rocks, and terrain features. Each cover type provides different levels of protection and can be destroyed or bypassed. Effective cover type usage requires understanding the protection level and the available options. Cover types are essential for survivability and tactical positioning.',
'Buildings, rocks, and terrain features. Different protection levels. Essential for survivability and tactical positioning.'),

('MECH_WARRIOR', 'FPS Dynamic Cover', 'MechWarrior Video Games', 'FPS Environment', 'DYNAMIC_COVER', NULL,
'Dynamic cover changes during combat. MW5:Mercenaries features dynamic cover where buildings and terrain can be destroyed, creating new paths and cover. Dynamic cover adds dynamic elements to combat, allowing the player to adapt to changing battlefield conditions. Effective dynamic cover usage requires understanding the destruction patterns and adapting tactics accordingly. Dynamic cover is essential for tactical flexibility and battlefield control.',
'Buildings and terrain can be destroyed. Creates new paths and cover. Essential for tactical flexibility.'),

('MECH_WARRIOR', 'FPS Water Terrain', 'MechWarrior Video Games', 'FPS Environment', 'WATER', NULL,
'Water terrain affects movement and stability. BattleTech 2018 includes water terrain that can affect movement and stability. Water terrain is a hazard that requires careful navigation. Effective water terrain management requires understanding the depth and the available routes. Water terrain adds a layer of realism and tactical depth to combat.',
'Affects movement and stability. Hazard requiring careful navigation. Realism and tactical depth.'),

('MECH_WARRIOR', 'FPS Rough Terrain', 'MechWarrior Video Games', 'FPS Environment', 'ROUGH_TERRAIN', NULL,
'Rough terrain increases stability damage and decreases movement. BattleTech 2018 includes rough terrain that can increase stability damage and decrease movement. Rough terrain is a hazard that requires careful navigation. Effective rough terrain management requires understanding the stability impact and adapting tactics accordingly. Rough terrain adds a layer of realism and tactical depth to combat.',
'Increases stability damage and decreases movement. Hazard requiring careful navigation. Realism and tactical depth.'),

('MECH_WARRIOR', 'FPS 10+ Maps', 'MechWarrior Video Games', 'FPS Multiplayer', 'MAPS', NULL,
'10+ maps provide variety in multiplayer play. MWO features 10+ maps with diverse terrain and layouts. Each map provides unique challenges and opportunities. Effective map usage requires understanding the terrain and layout. Maps are essential for variety and tactical depth in multiplayer play.',
'Diverse terrain and layouts. Unique challenges and opportunities. Essential for variety and tactical depth.'),

('MECH_WARRIOR', 'FPS 4+ Game Modes', 'MechWarrior Video Games', 'FPS Multiplayer', 'GAME_MODES', NULL,
'4+ game modes provide variety in multiplayer play. MWO features 4+ game modes including team deathmatch, capture the flag, objective-based missions, and faction play. Each mode provides unique challenges and opportunities. Effective game mode usage requires understanding the rules and objectives. Game modes are essential for variety and tactical depth in multiplayer play.',
'Team deathmatch, capture the flag, objective-based, faction play. Unique challenges and opportunities. Essential for variety and tactical depth.'),

('MECH_WARRIOR', 'FPS Event Queue', 'MechWarrior Video Games', 'FPS Multiplayer', 'EVENT_QUEUE', NULL,
'Event queue provides special events and modes. MWO features event queue with special events like Solaris weekend and other limited-time modes. Event queue provides variety and excitement. Effective event queue participation requires understanding the event rules and objectives. Event queue is essential for variety and excitement in multiplayer play.',
'Special events like Solaris weekend. Limited-time modes for variety and excitement. Essential for variety in multiplayer.'),

('MECH_WARRIOR', 'FPS Enemy Tactics', 'MechWarrior Video Games', 'FPS Combat', 'ENEMY_TACTICS', NULL,
'Enemy tactics vary by faction and situation. MW5:Mercenaries features enemy tactics that vary by faction and situation. Inner Sphere enemies use combined arms tactics with mechs, vehicles, and infantry. Clan enemies use superior technology and tactics. Effective enemy tactic understanding requires knowing the faction doctrines and adapting accordingly. Enemy tactics are essential for tactical planning and combat effectiveness.',
'Varies by faction and situation. Combined arms and superior technology. Essential for tactical planning.'),

('MECH_WARRIOR', 'FPS Mission Objectives', 'MechWarrior Video Games', 'FPS Campaign', 'OBJECTIVES', NULL,
'Mission objectives define the goals and victory conditions. MW5:Mercenaries features mission objectives such as destroying enemy mechs, capturing positions, escorting units, or defending locations. Missions may have time limits or specific victory conditions. Effective mission objective completion requires understanding the objectives and adapting tactics accordingly. Mission objectives are essential for campaign progression and reward acquisition.',
'Destroy, capture, escort, or defend. Time limits and specific victory conditions. Essential for campaign progression.'),

('MECH_WARRIOR', 'FPS Faction Dynamics', 'MechWarrior Video Games', 'FPS Campaign', 'FACTION_DYNAMICS', NULL,
'Faction dynamics affect mission availability and rewards. MW5:Mercenaries features faction dynamics where the relationships with various factions fluctuate according to the Succession Wars and player choices. Effective faction dynamics management requires understanding the faction relationships and the long-term implications of each decision. Faction dynamics are essential for strategic planning and campaign progression.',
'Relationships fluctuate with Succession Wars and player choices. Essential for strategic planning and campaign progression.'),

('MECH_WARRIOR', 'FPS Solaris VII Combat Games', 'MechWarrior Video Games', 'FPS Multiplayer', 'SOLARIS_VII', NULL,
'Solaris VII Combat Games are the premier mech combat arena. MWO features Solaris VII as a special event mode with 1v1 and 2v2 intense fights. Solaris VII is a neutral world where mech warriors compete in combat games for fame and fortune. Effective Solaris VII play requires understanding the combat rules and the opponent capabilities. Solaris VII is essential for proving skill and earning reputation.',
'Neutral world for mech combat games. 1v1 and 2v2 intense fights. Essential for proving skill and earning reputation.'),

('MECH_WARRIOR', 'FPS Star Commander Progression', 'MechWarrior Video Games', 'FPS Progression', 'STAR_COMMANDER', NULL,
'Star Commander is a clan leadership rank. MW5:Clans features the Star Commander as a leadership position within the clan. The player progresses through the ranks, starting as a kit (trainee) and advancing to Star Commander. Star Commander leads a star of mechs in combat, coordinating tactics and strategy. Effective Star Commander performance requires understanding the clan combat doctrines and leading by example. The Star Commander rank is a testament to the warrior skill, leadership, and dedication to the clan.',
'Clan leadership rank. Leads a star of mechs. Testament to skill, leadership, and dedication.'),

('MECH_WARRIOR', 'FPS Kit Training', 'MechWarrior Video Games', 'FPS Progression', 'KIT_TRAINING', NULL,
'Kit training is the clan initiation process. MW5:Clans features the kit training system where the player starts as a trainee and progresses through the ranks. Kits are trained in combat, tactics, and clan culture. The training includes tutorial missions and a trial of combat before the game actually begins. Effective kit training requires understanding the clan values and demonstrating skill and dedication. The kit training system establishes the foundation for the player mech warrior career.',
'Clan initiation process. Tutorial missions and trial of combat. Foundation for mech warrior career.'),

('MECH_WARRIOR', 'FPS Cockpit Interface', 'MechWarrior Video Games', 'FPS Interface', 'COCKPIT_INTERFACE', NULL,
'Cockpit interface provides control and information. All MechWarrior FPS games feature a cockpit interface with instrument panels, targeting displays, and weapon status indicators. The cockpit interface provides a sense of scale and immersion, showing the mech limbs moving below and the battlefield ahead. MW5:Mercenaries enhances the cockpit interface with damage indicators, heat gauges, and weapon cooldown displays. The cockpit interface is essential for situational awareness and tactical decision-making. Effective cockpit interface usage requires understanding and monitoring all elements simultaneously.',
'Instrument panels, targeting displays, and weapon status. Sense of scale and immersion. Essential for situational awareness.'),

('MECH_WARRIOR', 'FPS Radar Lock', 'MechWarrior Video Games', 'FPS Sensors', 'RADAR_LOCK', NULL,
'Radar lock enables guided weapon targeting. MW4:Vengeance features faster target lock with Beal active probes. MW5:Mercenaries includes radar lock as a key mechanic for guided weapons like LRMs. Radar lock requires line of sight and can be disrupted by electronic warfare. Effective radar lock management requires understanding the lock time and maintaining line of sight. Radar lock is essential for guided weapon accuracy and effectiveness.',
'Guided weapon targeting. Line of sight required. Can be disrupted by electronic warfare.'),

('MECH_WARRIOR', 'FPS Mech Customization Options', 'MechWarrior Video Games', 'FPS Progression', 'CUSTOMIZATION_OPTIONS', NULL,
'Customization options provide personalization and optimization. MWO Legends features customization options where players can customize their mechs with different loadouts, armor, and components. Customization options provide personalization and optimization. Effective customization requires understanding the mech capabilities and the available options. Customization options are essential for progression and optimization.',
'Different loadouts, armor, and components. Personalization and optimization. Essential for progression and optimization.'),

('MECH_WARRIOR', 'FPS Component Upgrade Path', 'MechWarrior Video Games', 'FPS Progression', 'COMPONENT_UPGRADE', NULL,
'Component upgrade path enhances mech capabilities. MW5:Mercenaries features component upgrade path where the player can acquire better components through missions and salvage. Component upgrades provide incremental improvements in performance and efficiency. Effective component upgrade path requires understanding the component characteristics and the available upgrades. Component upgrade path is essential for sustained mech performance.',
'Incremental improvements in performance and efficiency. Acquire through missions and salvage. Essential for sustained mech performance.'),

('MECH_WARRIOR', 'FPS Weapon Upgrade Path', 'MechWarrior Video Games', 'FPS Progression', 'WEAPON_UPGRADE', NULL,
'Weapon upgrade path provides incremental improvements. MW5:Mercenaries features weapon upgrade path where the player can acquire better weapons through missions and salvage. Weapon upgrades provide incremental improvements in damage, range, and heat efficiency. Effective weapon upgrade path requires understanding the weapon characteristics and the available upgrades. Weapon upgrade path is essential for sustained combat effectiveness.',
'Incremental improvements in damage, range, and heat. Acquire through missions and salvage. Essential for sustained combat.'),

('MECH_WARRIOR', 'FPS Armor Upgrade Path', 'MechWarrior Video Games', 'FPS Progression', 'ARMOR_UPGRADE', NULL,
'Armor upgrade path increases survivability. MW5:Mercenaries features armor upgrade path where the player can acquire better armor through missions and salvage. Armor upgrades provide incremental improvements in protection and weight efficiency. Effective armor upgrade path requires understanding the armor distribution and the available upgrades. Armor upgrade path is essential for sustained survivability.',
'Incremental improvements in protection and weight. Acquire through missions and salvage. Essential for sustained survivability.'),

('MECH_WARRIOR', 'FPS Pilot Specialization Assignment', 'MechWarrior Video Games', 'FPS Progression', 'SPECIALIZATION_ASSIGNMENT', NULL,
'Pilot specialization assignment optimizes combat effectiveness. BattleTech 2018 features pilot specialization assignment where the player can assign pilots to mechs based on their specialization. Pilot specialization assignment requires understanding the pilot capabilities and the mech characteristics. Effective pilot specialization assignment allows the player to optimize combat effectiveness and mission success. Pilot specialization assignment is essential for sustained combat effectiveness and pilot management.',
'Assign pilots to mechs based on specialization. Understand pilot capabilities and mech characteristics. Essential for sustained combat effectiveness.'),

('MECH_WARRIOR', 'FPS Pilot Breakdown and Recovery', 'MechWarrior Video Games', 'FPS Progression', 'PILOT_BREAKDOWN', NULL,
'Pilot breakdown and recovery are critical failure states. BattleTech 2018 features pilot breakdown where pilots can become broken under extreme stress and cannot fight until they recover. Pilot breakdown is a critical failure state that requires immediate attention. Effective pilot breakdown prevention requires understanding the pilot psychological limits and providing support when needed. Pilot breakdown and recovery are essential for realistic pilot management and combat effectiveness.',
'Broken under extreme stress. Cannot fight until recovery. Essential for realistic pilot management and combat effectiveness.'),

('MECH_WARRIOR', 'FPS Pilot Stress Management', 'MechWarrior Video Games', 'FPS Progression', 'STRESS_MANAGEMENT', NULL,
'Pilot stress management is essential for sustained combat. BattleTech 2018 features pilot stress management where the player must manage the pilot stress levels to maintain combat effectiveness. Pilot stress management requires understanding the pilot psychological limits and providing support when needed. Effective pilot stress management allows the player to maintain combat effectiveness and prevent pilot breakdown. Pilot stress management is essential for sustained combat effectiveness.',
'Manage pilot stress levels to maintain combat effectiveness. Understand psychological limits and provide support. Essential for sustained combat effectiveness.'),

('MECH_WARRIOR', 'FPS Medical Attention', 'MechWarrior Video Games', 'FPS Progression', 'MEDICAL_ATTENTION', NULL,
'Medical attention restores pilot health. BattleTech 2018 features medical attention where injured pilots can receive medical treatment to restore health and combat effectiveness. Medical attention requires understanding the pilot condition and the available resources. Effective medical attention allows the player to restore pilot health and continue missions. Medical attention is essential for sustained combat effectiveness and pilot management.',
'Injured pilots receive medical treatment. Understand pilot condition and available resources. Essential for sustained combat effectiveness and pilot management.'),

('MECH_WARRIOR', 'FPS Pilot Death Prevention', 'MechWarrior Video Games', 'FPS Progression', 'PILOT_DEATH_PREVENTION', NULL,
'Pilot death prevention is essential for sustainability. BattleTech 2018 features pilot death prevention where the player must manage the pilot health and stress to prevent pilot death. Pilot death prevention requires understanding the pilot condition and the available resources. Effective pilot death prevention allows the player to maintain their pilot roster and continue missions. Pilot death prevention is essential for sustained combat effectiveness and pilot management.',
'Manage pilot health and stress to prevent death. Understand pilot condition and available resources. Essential for sustained combat effectiveness and pilot management.'),

('MECH_WARRIOR', 'FPS Pilot Replacement', 'MechWarrior Video Games', 'FPS Progression', 'PILOT_REPLACEMENT', NULL,
'Pilot replacement is necessary when pilots die. BattleTech 2018 features pilot replacement where the player must hire new pilots to replace those who died in combat. Pilot replacement requires understanding the pilot capabilities and the available resources. Effective pilot replacement allows the player to maintain their pilot roster and continue missions. Pilot replacement is essential for sustained combat effectiveness and pilot management.',
'Hire new pilots to replace those who died. Understand pilot capabilities and available resources. Essential for sustained combat effectiveness and pilot management.'),

('MECH_WARRIOR', 'FPS Scout Loadout', 'MechWarrior Video Games', 'FPS Customization', 'SCOUT_LOADOUT', NULL,
'Scout loadout emphasizes speed and sensors. MW5:Mercenaries features scout loadouts where the player creates mechs optimized for speed and sensors. Scout loadouts require understanding the speed and sensor characteristics. Effective scout loadouts allow the player to gather information and provide early warning. Scout loadouts are essential for reconnaissance and tactical awareness.',
'Emphasizes speed and sensors. Gather information and provide early warning. Essential for reconnaissance and tactical awareness.'),

('MECH_WARRIOR', 'FPS Assault Loadout', 'MechWarrior Video Games', 'FPS Customization', 'ASSAULT_LOADOUT', NULL,
'Assault loadout emphasizes firepower and armor. MW5:Mercenaries features assault loadouts where the player creates mechs optimized for firepower and armor. Assault loadouts require understanding the firepower and armor characteristics. Effective assault loadouts allow the player to lead charges and break through enemy lines. Assault loadouts are essential for frontline combat and breakthrough missions.',
'Emphasizes firepower and armor. Lead charges and break through enemy lines. Essential for frontline combat and breakthrough missions.'),

('MECH_WARRIOR', 'FPS Support Loadout', 'MechWarrior Video Games', 'FPS Customization', 'SUPPORT_LOADOUT', NULL,
'Support loadout emphasizes sustained firepower and heat management. MW5:Mercenaries features support loadouts where the player creates mechs optimized for sustained firepower and heat management. Support loadouts require understanding the sustained firepower and heat management characteristics. Effective support loadouts allow the player to provide sustained fire support and maintain combat effectiveness. Support loadouts are essential for sustained combat and fire support missions.',
'Emphasizes sustained firepower and heat management. Provide sustained fire support. Essential for sustained combat and fire support missions.'),

('MECH_WARRIOR', 'FPS Command Loadout', 'MechWarrior Video Games', 'FPS Customization', 'COMMAND_LOADOUT', NULL,
'Command loadout emphasizes sensors and electronic warfare. MW5:Mercenaries features command loadouts where the player creates mechs optimized for sensors and electronic warfare. Command loadouts require understanding the sensor and electronic warfare characteristics. Effective command loadouts allow the player to provide command and control support and disrupt enemy targeting. Command loadouts are essential for command and control missions and electronic warfare.',
'Emphasizes sensors and electronic warfare. Provide command and control support. Essential for command and control missions and electronic warfare.'),

('MECH_WARRIOR', 'FPS Sniper Loadout', 'MechWarrior Video Games', 'FPS Customization', 'SNIPER_LOADOUT', NULL,
'Sniper loadout emphasizes long-range weapons and accuracy. MW5:Mercenaries features sniper loadouts where the player creates mechs optimized for long-range weapons and accuracy. Sniper loadouts require understanding the long-range weapon and accuracy characteristics. Effective sniper loadouts allow the player to eliminate high-value targets from a safe distance. Sniper loadouts are essential for long-range combat and target elimination.',
'Emphasizes long-range weapons and accuracy. Eliminate high-value targets from distance. Essential for long-range combat and target elimination.'),

('MECH_WARRIOR', 'FPS Brawler Loadout', 'MechWarrior Video Games', 'FPS Customization', 'BRAWLER_LOADOUT', NULL,
'Brawler loadout emphasizes close-range weapons and melee combat. MW5:Mercenaries features brawler loadouts where the player creates mechs optimized for close-range weapons and melee combat. Brawler loadouts require understanding the close-range weapon and melee combat characteristics. Effective brawler loadouts allow the player to engage in close-quarters combat and overwhelm enemies. Brawler loadouts are essential for close-quarters combat and melee engagements.',
'Emphasizes close-range weapons and melee combat. Engage in close-quarters combat. Essential for close-quarters combat and melee engagements.'),

('MECH_WARRIOR', 'FPS Balanced Loadout', 'MechWarrior Video Games', 'FPS Customization', 'BALANCED_LOADOUT', NULL,
'Balanced loadout provides versatility and adaptability. MW5:Mercenaries features balanced loadouts where the player creates mechs optimized for versatility and adaptability. Balanced loadouts require understanding the weapon and armor characteristics. Effective balanced loadouts allow the player to adapt to different combat scenarios and maintain combat effectiveness. Balanced loadouts are essential for versatility and adaptability.',
'Optimized for versatility and adaptability. Adapt to different combat scenarios. Essential for versatility and adaptability.'),

('MECH_WARRIOR', 'FPS Urban Combat Loadout', 'MechWarrior Video Games', 'FPS Customization', 'URBAN_LOADOUT', NULL,
'Urban combat loadout emphasizes close-range weapons and maneuverability. MW5:Mercenaries features urban combat loadouts where the player creates mechs optimized for close-range weapons and maneuverability. Urban combat loadouts require understanding the close-range weapon and maneuverability characteristics. Effective urban combat loadouts allow the player to navigate urban environments and engage in close-quarters combat. Urban combat loadouts are essential for urban combat and close-quarters engagements.',
'Emphasizes close-range weapons and maneuverability. Navigate urban environments and engage in close-quarters combat. Essential for urban combat.'),

('MECH_WARRIOR', 'FPS Open Terrain Loadout', 'MechWarrior Video Games', 'FPS Customization', 'OPEN_TERRAIN_LOADOUT', NULL,
'Open terrain loadout emphasizes long-range weapons and speed. MW5:Mercenaries features open terrain loadouts where the player creates mechs optimized for long-range weapons and speed. Open terrain loadouts require understanding the long-range weapon and speed characteristics. Effective open terrain loadouts allow the player to engage enemies from a distance and maintain mobility. Open terrain loadouts are essential for open terrain combat and long-range engagements.',
'Emphasizes long-range weapons and speed. Engage enemies from distance and maintain mobility. Essential for open terrain combat.'),

('MECH_WARRIOR', 'FPS Forest Combat Loadout', 'MechWarrior Video Games', 'FPS Customization', 'FOREST_LOADOUT', NULL,
'Forest combat loadout emphasizes maneuverability and cover usage. MW5:Mercenaries features forest combat loadouts where the player creates mechs optimized for maneuverability and cover usage. Forest combat loadouts require understanding the maneuverability and cover usage characteristics. Effective forest combat loadouts allow the player to navigate forest environments and use cover effectively. Forest combat loadouts are essential for forest combat and cover-based engagements.',
'Emphasizes maneuverability and cover usage. Navigate forest environments and use cover effectively. Essential for forest combat.'),

('MECH_WARRIOR', 'FPS Desert Combat Loadout', 'MechWarrior Video Games', 'FPS Customization', 'DESERT_LOADOUT', NULL,
'Desert combat loadout emphasizes heat management and long-range weapons. MW5:Mercenaries features desert combat loadouts where the player creates mechs optimized for heat management and long-range weapons. Desert combat loadouts require understanding the heat management and long-range weapon characteristics. Effective desert combat loadouts allow the player to engage enemies from a distance and manage heat in hot environments. Desert combat loadouts are essential for desert combat and heat management.',
'Emphasizes heat management and long-range weapons. Engage enemies from distance and manage heat in hot environments. Essential for desert combat.'),

('MECH_WARRIOR', 'FPS Mountain Combat Loadout', 'MechWarrior Video Games', 'FPS Customization', 'MOUNTAIN_LOADOUT', NULL,
'Mountain combat loadout emphasizes elevation advantage and jump jets. MW5:Mercenaries features mountain combat loadouts where the player creates mechs optimized for elevation advantage and jump jets. Mountain combat loadouts require understanding the elevation advantage and jump jet characteristics. Effective mountain combat loadouts allow the player to gain elevation advantage and navigate mountainous terrain. Mountain combat loadouts are essential for mountain combat and elevation-based engagements.',
'Emphasizes elevation advantage and jump jets. Gain elevation advantage and navigate mountainous terrain. Essential for mountain combat.'),

('MECH_WARRIOR', 'FPS Night Combat Loadout', 'MechWarrior Video Games', 'FPS Customization', 'NIGHT_LOADOUT', NULL,
'Night combat loadout emphasizes night vision and sensors. MW5:Mercenaries features night combat loadouts where the player creates mechs optimized for night vision and sensors. Night combat loadouts require understanding the night vision and sensor characteristics. Effective night combat loadouts allow the player to engage enemies in low-light conditions and maintain situational awareness. Night combat loadouts are essential for night combat and low-light engagements.',
'Emphasizes night vision and sensors. Engage enemies in low-light conditions and maintain situational awareness. Essential for night combat.'),

('MECH_WARRIOR', 'FPS Electronic Warfare Loadout', 'MechWarrior Video Games', 'FPS Customization', 'EW_LOADOUT', NULL,
'Electronic warfare loadout emphasizes ECM and ECCM. MW4:Vengeance features electronic warfare loadouts where the player creates mechs optimized for ECM and ECCM. Electronic warfare loadouts require understanding the ECM and ECCM characteristics. Effective electronic warfare loadouts allow the player to disrupt enemy targeting and maintain targeting accuracy. Electronic warfare loadouts are essential for electronic warfare and targeting disruption.',
'Emphasizes ECM and ECCM. Disrupt enemy targeting and maintain targeting accuracy. Essential for electronic warfare and targeting disruption.'),

('MECH_WARRIOR', 'FPS AMS Loadout', 'MechWarrior Video Games', 'FPS Customization', 'AMS_LOADOUT', NULL,
'AMS loadout emphasizes anti-missile defense. MW5:Mercenaries features AMS loadouts where the player creates mechs optimized for anti-missile defense. AMS loadouts require understanding the anti-missile defense characteristics. Effective AMS loadouts allow the player to defend against missile attacks and maintain combat effectiveness. AMS loadouts are essential for missile defense and sustained combat.',
'Emphasizes anti-missile defense. Defend against missile attacks and maintain combat effectiveness. Essential for missile defense and sustained combat.'),

('MECH_WARRIOR', 'FPS Missile Attack Strategy', 'MechWarrior Video Games', 'FPS Tactics', 'MISSILE_ATTACK', NULL,
'Missile attack strategy maximizes missile effectiveness. MW5:Mercenaries features missile attack strategy where the player uses missiles to overwhelm enemy defenses and deal area damage. Missile attack strategy requires understanding the missile characteristics and the enemy defenses. Effective missile attack strategy allows the player to maximize missile effectiveness and deal area damage. Missile attack strategy is essential for area damage and overwhelming enemy defenses.',
'Use missiles to overwhelm enemy defenses and deal area damage. Understand missile characteristics and enemy defenses. Essential for area damage and overwhelming enemy defenses.'),

('MECH_WARRIOR', 'FPS Energy Weapon Strategy', 'MechWarrior Video Games', 'FPS Tactics', 'ENERGY_STRATEGY', NULL,
'Energy weapon strategy maximizes sustained firepower. MW5:Mercenaries features energy weapon strategy where the player uses energy weapons for sustained firepower. Energy weapon strategy requires understanding the heat management and the weapon characteristics. Effective energy weapon strategy allows the player to maintain sustained firepower and overwhelm enemies. Energy weapon strategy is essential for sustained combat and firepower.',
'Use energy weapons for sustained firepower. Understand heat management and weapon characteristics. Essential for sustained combat and firepower.'),

('MECH_WARRIOR', 'FPS Ballistic Weapon Strategy', 'MechWarrior Video Games', 'FPS Tactics', 'BALLISTIC_STRATEGY', NULL,
'Ballistic weapon strategy maximizes burst damage. MW5:Mercenaries features ballistic weapon strategy where the player uses ballistic weapons for burst damage. Ballistic weapon strategy requires understanding the cooldown management and the weapon characteristics. Effective ballistic weapon strategy allows the player to deal burst damage and eliminate high-value targets. Ballistic weapon strategy is essential for burst damage and target elimination.',
'Use ballistic weapons for burst damage. Understand cooldown management and weapon characteristics. Essential for burst damage and target elimination.'),

('MECH_WARRIOR', 'FPS Combined Weapon Strategy', 'MechWarrior Video Games', 'FPS Tactics', 'COMBINED_STRATEGY', NULL,
'Combined weapon strategy balances firepower and heat. MW5:Mercenaries features combined weapon strategy where the player uses a combination of energy and ballistic weapons to balance firepower and heat. Combined weapon strategy requires understanding the weapon characteristics and the heat management. Effective combined weapon strategy allows the player to maintain sustained firepower and deal burst damage. Combined weapon strategy is essential for balanced combat and firepower.',
'Combine energy and ballistic weapons to balance firepower and heat. Understand weapon characteristics and heat management. Essential for balanced combat and firepower.'),

('MECH_WARRIOR', 'FPS Cooldown Synchronization', 'MechWarrior Video Games', 'FPS Tactics', 'COOLDOWN_SYNC', NULL,
'Cooldown synchronization optimizes weapon firing. MWO features cooldown synchronization where the player synchronizes weapon cooldowns to maintain sustained firepower. Cooldown synchronization requires understanding the weapon characteristics and the cooldown times. Effective cooldown synchronization allows the player to maintain sustained firepower and overwhelm enemies. Cooldown synchronization is essential for sustained combat and firepower.',
'Synchronize weapon cooldowns to maintain sustained firepower. Understand weapon characteristics and cooldown times. Essential for sustained combat and firepower.'),

('MECH_WARRIOR', 'FPS Head Shot Strategy', 'MechWarrior Video Games', 'FPS Tactics', 'HEAD_SHOT', NULL,
'Head shot strategy targets the cockpit for instant kill. BattleTech 2018 features head shot strategy where the player targets the cockpit to kill the pilot and destroy the mech instantly. Head shot strategy requires understanding the cockpit location and the weapon characteristics. Effective head shot strategy allows the player to destroy the mech instantly and gain tactical advantages. Head shot strategy is essential for instant kill and tactical advantage.',
'Target the cockpit to kill the pilot and destroy the mech instantly. Understand cockpit location and weapon characteristics. Essential for instant kill and tactical advantage.'),

('MECH_WARRIOR', 'FPS Leg Shot Strategy', 'MechWarrior Video Games', 'FPS Tactics', 'LEG_SHOT', NULL,
'Leg shot strategy targets the legs to immobilize the enemy. MW5:Mercenaries features leg shot strategy where the player targets the legs to immobilize the enemy mech. Leg shot strategy requires understanding the leg location and the weapon characteristics. Effective leg shot strategy allows the player to immobilize the enemy mech and gain tactical advantages. Leg shot strategy is essential for immobilization and tactical advantage.',
'Target the legs to immobilize the enemy mech. Understand leg location and weapon characteristics. Essential for immobilization and tactical advantage.'),

('MECH_WARRIOR', 'FPS Arm Shot Strategy', 'MechWarrior Video Games', 'FPS Tactics', 'ARM_SHOT', NULL,
'Arm shot strategy targets the arms to disable weapons. MW5:Mercenaries features arm shot strategy where the player targets the arms to disable the enemy weapons. Arm shot strategy requires understanding the arm location and the weapon characteristics. Effective arm shot strategy allows the player to disable the enemy weapons and gain tactical advantages. Arm shot strategy is essential for weapon disabling and tactical advantage.',
'Target the arms to disable the enemy weapons. Understand arm location and weapon characteristics. Essential for weapon disabling and tactical advantage.'),

('MECH_WARRIOR', 'FPS Torso Shot Strategy', 'MechWarrior Video Games', 'FPS Tactics', 'TORSO_SHOT', NULL,
'Torso shot strategy targets the torso for maximum damage. MW5:Mercenaries features torso shot strategy where the player targets the torso for maximum damage. Torso shot strategy requires understanding the torso location and the weapon characteristics. Effective torso shot strategy allows the player to deal maximum damage and destroy the mech. Torso shot strategy is essential for maximum damage and mech destruction.',
'Target the torso for maximum damage. Understand torso location and weapon characteristics. Essential for maximum damage and mech destruction.'),

('MECH_WARRIOR', 'FPS Center Torso Shot Strategy', 'MechWarrior Video Games', 'FPS Tactics', 'CENTER_TORSO_SHOT', NULL,
'Center torso shot strategy targets the reactor for catastrophic explosion. MW5:Mercenaries features center torso shot strategy where the player targets the center torso to destroy the reactor and cause a catastrophic explosion. Center torso shot strategy requires understanding the center torso location and the weapon characteristics. Effective center torso shot strategy allows the player to destroy the mech with a catastrophic explosion and gain tactical advantages. Center torso shot strategy is essential for catastrophic explosion and mech destruction.',
'Target the center torso to destroy the reactor and cause catastrophic explosion. Understand center torso location and weapon characteristics. Essential for catastrophic explosion and mech destruction.'),

('MECH_WARRIOR', 'FPS Ammo Bin Shot Strategy', 'MechWarrior Video Games', 'FPS Tactics', 'AMMO_BIN_SHOT', NULL,
'Ammo bin shot strategy targets the ammo bins for chain reactions. MW5:Mercenaries features ammo bin shot strategy where the player targets the ammo bins to cause chain reactions and catastrophic damage. Ammo bin shot strategy requires understanding the ammo bin location and the weapon characteristics. Effective ammo bin shot strategy allows the player to cause chain reactions and catastrophic damage. Ammo bin shot strategy is essential for chain reactions and catastrophic damage.',
'Target the ammo bins to cause chain reactions and catastrophic damage. Understand ammo bin location and weapon characteristics. Essential for chain reactions and catastrophic damage.'),

('MECH_WARRIOR', 'FPS Inner Sphere Technology', 'MechWarrior Video Games', 'FPS Factions', 'IS_TECH', NULL,
'Inner Sphere technology is more common but less advanced. Inner Sphere mechs use standard technology with less advanced materials and weapons. Inner Sphere technology is more common and easier to maintain than Clan technology. Effective Inner Sphere technology usage requires understanding the limitations and compensating with tactics and pilot skill. Inner Sphere technology is essential for accessibility and sustainability.',
'More common and easier to maintain. Less advanced but accessible. Compensate with tactics and pilot skill.'),

('MECH_WARRIOR', 'FPS Succession Wars Context', 'MechWarrior Video Games', 'FPS Campaign', 'SUCCESSION_WARS', NULL,
'Succession Wars provide the campaign context. MW5:Mercenaries features the Succession Wars as the backdrop for the campaign, with the player choosing sides and affecting the outcome. The Succession Wars are a series of conflicts between the Great Houses of the Inner Sphere. Effective Succession Wars understanding requires knowing the lore and the faction motivations. Succession Wars context is essential for immersion and strategic planning.',
'Conflicts between Great Houses. Player choices affect outcome. Essential for immersion and strategic planning.'),

('MECH_WARRIOR', 'FPS Clan Invasion Context', 'MechWarrior Video Games', 'FPS Campaign', 'CLAN_INVASION', NULL,
'Clan Invasion provides the campaign context for MW5:Clans. MW5:Clans features the Clan Invasion as the backdrop for the campaign, with the player joining a clan and progressing through the ranks. The Clan Invasion is the conflict between the Clans and the Inner Sphere. Effective Clan Invasion understanding requires knowing the lore and the faction motivations. Clan Invasion context is essential for immersion and strategic planning.',
'Clan vs Inner Sphere conflict. Player joins a clan and progresses. Essential for immersion and strategic planning.'),

('MECH_WARRIOR', 'FPS Refusal War Context', 'MechWarrior Video Games', 'FPS Campaign', 'REFUSAL_WAR', NULL,
'Refusal War provides the campaign context for MW2. MW2:31st Century Combat features the Refusal War between Clan Wolf and Clan Jade Falcon as the backdrop for the campaign. The Refusal War is a conflict between two clans that refused to join the Clan invasion of the Inner Sphere. Effective Refusal War understanding requires knowing the lore and the faction motivations. Refusal War context is essential for immersion and strategic planning.',
'Clan Wolf vs Clan Jade Falcon conflict. Refused to join Clan invasion. Essential for immersion and strategic planning.'),

('MECH_WARRIOR', 'FPS Operation Serpent Context', 'MechWarrior Video Games', 'FPS Campaign', 'OPERATION_SERPENT', NULL,
'Operation Serpent provides the campaign context for MW3. MW3 features Operation Serpent as the backdrop for the campaign, with the player participating in the Inner Sphere counterattack against the Clans. Operation Serpent is a coordinated effort to drive the Clans from the Inner Sphere. Effective Operation Serpent understanding requires knowing the lore and the faction motivations. Operation Serpent context is essential for immersion and strategic planning.',
'Inner Sphere counterattack against the Clans. Coordinated effort to drive Clans from Inner Sphere. Essential for immersion and strategic planning.'),

('MECH_WARRIOR', 'FPS Federated Commonwealth Civil War', 'MechWarrior Video Games', 'FPS Campaign', 'FC_CIVIL_WAR', NULL,
'Federated Commonwealth Civil War provides the campaign context for MW4. MW4:Vengeance features the Federated Commonwealth Civil War as the backdrop for the campaign, with the player choosing sides in the conflict. The Federated Commonwealth Civil War is a conflict between the Federated Suns and the Lyran Commonwealth. Effective Federated Commonwealth Civil War understanding requires knowing the lore and the faction motivations. Federated Commonwealth Civil War context is essential for immersion and strategic planning.',
'Federated Suns vs Lyran Commonwealth conflict. Player chooses sides. Essential for immersion and strategic planning.'),

('MECH_WARRIOR', 'FPS Fallen Mech Manning', 'MechWarrior Video Games', 'FPS Progression', 'FALLEN_MECH', NULL,
'Fallen mechs can be manned by new pilots. MechCommander 2 features fallen mech manning where new pilots can man fallen mechs, turning the tide of battle. Effective fallen mech manning requires understanding the pilot capabilities and the mech condition. Fallen mech manning is essential for sustained combat and mission success.',
'New pilots can man fallen mechs. Turns the tide of battle. Essential for sustained combat.'),

('MECH_WARRIOR', 'FPS Repair Trucks', 'MechWarrior Video Games', 'FPS Progression', 'REPAIR_TRUCKS', NULL,
'Repair trucks provide in-mission repairs. MechCommander 2 features repair trucks that can repair damaged mechs during missions. Repair trucks are called down using resource points. Effective repair truck usage requires understanding the repair capabilities and the mission requirements. Repair trucks are essential for sustained combat and mission success.',
'Repair damaged mechs during missions. Called down using resource points. Essential for sustained combat.'),

('MECH_WARRIOR', 'FPS Stationary Cannons', 'MechWarrior Video Games', 'FPS Progression', 'STATIONARY_CANNONS', NULL,
'Stationary cannons provide defensive fire support. MechCommander 2 features stationary cannons that can be deployed to provide defensive fire support. Stationary cannons are called down using resource points. Effective stationary cannon usage requires understanding the fire support capabilities and the mission requirements. Stationary cannons are essential for defensive positions and mission success.',
'Defensive fire support. Deployed using resource points. Essential for defensive positions.'),

('MECH_WARRIOR', 'FPS New Pilots', 'MechWarrior Video Games', 'FPS Progression', 'NEW_PILOTS', NULL,
'New pilots can be called down during missions. MechCommander 2 features new pilots that can be called down using resource points. New pilots provide additional combat power and can man fallen mechs. Effective new pilot usage requires understanding the pilot capabilities and the mission requirements. New pilots are essential for sustained combat and mission success.',
'Additional combat power and can man fallen mechs. Called down using resource points. Essential for sustained combat.'),

('MECH_WARRIOR', 'FPS Pilot Gunnery Skill', 'MechWarrior Video Games', 'FPS Progression', 'GUNNERY', NULL,
'Gunnery skill improves weapon accuracy. BattleTech 2018 features Gunnery as one of the four pilot stats, improving hit chance. MechCommander 2 features Gunnery as a pilot skill that improves weapon accuracy. Effective Gunnery skill development requires understanding the weapon types and ranges. Gunnery skill is essential for weapon accuracy and combat effectiveness.',
'Improves hit chance. Essential for weapon accuracy. Develop based on weapon types and ranges.'),

('MECH_WARRIOR', 'FPS Pilot Piloting Skill', 'MechWarrior Video Games', 'FPS Progression', 'PILOTING_SKILL', NULL,
'Piloting skill improves movement and evasion. BattleTech 2018 features Piloting as one of the four pilot stats, increasing evasion and melee attacks (as well as stability thresholds). MechCommander 2 features Piloting as a pilot skill that improves movement and evasion. Effective Piloting skill development requires understanding the mech capabilities and the terrain layout. Piloting skill is essential for movement and evasion.',
'Increases evasion and melee attacks. Stability thresholds improved. Essential for movement and evasion.'),

('MECH_WARRIOR', 'FPS Pilot Guts Skill', 'MechWarrior Video Games', 'FPS Progression', 'GUTS', NULL,
'Guts skill improves heat resistance and health. BattleTech 2018 features Guts as one of the four pilot stats, increasing health and heat threshold. MechCommander 2 features Guts as a pilot skill that improves heat resistance and health. Effective Guts skill development requires understanding the heat management and the pilot health status. Guts skill is essential for heat resistance and pilot survival.',
'Increases health and heat threshold. Essential for heat resistance and pilot survival. Develop based on heat management.'),

('MECH_WARRIOR', 'FPS Pilot Tactics Skill', 'MechWarrior Video Games', 'FPS Progression', 'TACTICS', NULL,
'Tactics skill improves detection range and indirect fire. BattleTech 2018 features Tactics as one of the four pilot stats, giving better detection range as well as decreasing penalty for indirect fire. MechCommander 2 features Tactics as a pilot skill that improves detection range and indirect fire. Effective Tactics skill development requires understanding the sensor range and the terrain layout. Tactics skill is essential for detection and indirect fire.',
'Better detection range and decreased indirect fire penalty. Essential for detection and indirect fire. Develop based on sensor range and terrain.'),

('MECH_WARRIOR', 'FPS Weapon Heat Generation', 'MechWarrior Video Games', 'FPS Combat', 'WEAPON_HEAT', NULL,
'Weapon heat generation varies by type. Energy weapons (Lasers, PPCs) generate the most heat, while ballistic weapons (Autocannons, Gauss Rifles) generate minimal heat. Missile weapons (SRMs, LRMs) generate moderate heat. Effective heat management requires understanding the heat generation of each weapon and balancing the loadout accordingly. Heat generation is essential for sustained combat and tactical decision-making.',
'Energy=Most heat, Ballistic=Minimal heat, Missile=Moderate heat. Balance loadout for heat management.'),

('MECH_WARRIOR', 'FPS Heat Sink Types', 'MechWarrior Video Games', 'FPS Customization', 'HEAT_SINK_TYPES', NULL,
'Heat sink types affect heat dissipation. Standard heat sinks dissipate 1 heat point per sink. Double Heat Sinks (DHS) dissipate 2 heat points per sink but take up twice the critical hit space. Clan heat sinks are more efficient than Inner Sphere heat sinks. Effective heat sink selection requires understanding the heat generation of the weapon loadout and the available critical hit space. Heat sinks are essential for heat management and sustained combat.',
'Standard=1 heat/sink, Double=2 heat/sink. Clan heat sinks more efficient. Essential for heat management.'),

('MECH_WARRIOR', 'FPS Mech Balance and Falling', 'MechWarrior Video Games', 'FPS Combat', 'BALANCE', NULL,
'Mech balance and falling are simulated in FPS games. Mechs are bipedal walkers that can fall if they lose balance. MW5:Mercenaries simulates balance — rapid movement or taking damage can cause the mech to fall. When a mech falls, it is temporarily unable to move or fire effectively. The player must recover balance before resuming combat. Falling can be caused by weapon impacts, rough terrain, or excessive movement. Effective balance management requires understanding the mech stability limits and adjusting movement and firing accordingly. BattleTech 2018 includes stability as a core mechanic, with piloting skills affecting stability thresholds.',
'Bipedal balance simulation. Falling causes temporary immobilization. Piloting skills affect stability.'),

('MECH_WARRIOR', 'FPS Mech Jump Jet Management', 'MechWarrior Video Games', 'FPS Movement', 'JUMP_MANAGEMENT', NULL,
'Jump jet management is essential for mobility. Jump jets consume heat and can affect stability upon landing. The player must balance jump jet usage with heat management to avoid overheating. MW5:Mercenaries features jump jets that allow the mech to leap over obstacles and terrain. Some mechs have more jump jets than others, affecting their mobility. Effective jump jet management requires understanding the mech jump capabilities and the terrain layout. Jump jets are essential for navigating complex terrain and maintaining tactical positioning, but overuse can lead to overheating and vulnerability.',
'Heat cost for vertical movement. Balance with heat management. Essential for terrain navigation.'),

('MECH_WARRIOR', 'FPS Mech Armor Distribution', 'MechWarrior Video Games', 'FPS Customization', 'ARMOR_DIST', NULL,
'Armor distribution affects survivability. MW5:Clans adds manual armor distribution where the player assigns armor to each slot individually. This allows for customized armor profiles — heavy front armor for brawlers or balanced armor for versatile mechs. The total armor is limited by the mech weight, requiring the player to balance armor distribution with weapon loadout. Effective armor distribution considers the mech role and expected combat scenarios. For example, a brawler may prioritize front armor, while a sniper may prioritize center torso armor to protect the reactor.',
'Manual armor assignment per slot. Balance with weapon loadout. Customize for role and scenario.'),

('MECH_WARRIOR', 'FPS Mech Hardpoints', 'MechWarrior Video Games', 'FPS Customization', 'HARDPOINTS', NULL,
'Mech hardpoints restrict weapon placement. MW4:Vengeance introduced hardpoint-based weapon systems where specific weapons can only be mounted on designated hardpoints. This system is more realistic but also more restrictive than the previous slot-based system. MW5:Mercenaries uses a simplified hardpoint system where weapons can be placed in available slots, but some weapons require specific hardpoints. Hardpoints add strategic depth to customization, requiring the player to consider weapon placement and compatibility. Effective hardpoint management ensures that the mech can carry the desired weapon loadout while maintaining balance and mobility.',
'Restricts weapon placement for realism. Strategic depth in customization. Balance loadout with hardpoints.'),

('MECH_WARRIOR', 'FPS Mech Ejection Sequence', 'MechWarrior Video Games', 'FPS Combat', 'EJECTION_SEQUENCE', NULL,
'Ejection sequence saves the pilot life. When a mech is critically damaged, the pilot can eject to survive. The ejection sequence involves the pilot seat launching from the cockpit, deploying a parachute, and landing safely. MW5:Mercenaries features ejection when the mech is destroyed, with the pilot returning to base for repairs. Ejection is a last resort, as the mech cannot be recovered after ejection. In multiplayer modes, ejection removes the player from the current match. Ejection is essential for pilot survival but results in mission failure or partial success.',
'Pilot seat launches from cockpit. Parachute deployment and safe landing. Last resort for pilot survival.'),

('MECH_WARRIOR', 'FPS Mech Reactor Critical', 'MechWarrior Video Games', 'FPS Combat', 'REACTOR_CRITICAL', NULL,
'Reactor critical is the ultimate failure state. When a mech takes excessive damage, the reactor can go critical and explode. MW5:Mercenaries features reactor critical as a risk when using heat override excessively. If the reactor goes critical, the mech explodes, destroying all components and killing the pilot. Reactor critical is the most devastating outcome, eliminating the mech entirely. Players must manage heat and damage carefully to avoid reactor critical. In MW5:Clans, the long time to kill is reduced, making reactor critical more common.',
'Ultimate failure state. Heat override increases risk. Mech explodes and pilot dies.'),

('MECH_WARRIOR', 'FPS Mech Ammo Explosion Chain', 'MechWarrior Video Games', 'FPS Combat', 'AMMO_CHAIN', NULL,
'Ammo explosion chain reactions can be devastating. When ammo bins are destroyed, the ammo explodes, dealing massive damage to the containing location and potentially causing chain reactions. MW5:Mercenaries features ammo explosion chain reactions where multiple ammo bins can explode in sequence, causing catastrophic damage. Effective ammo management requires understanding the explosion risk and storing ammo in separate locations to minimize chain reactions. Ammo explosion chain reactions add a layer of risk to ballistic and missile weapon usage.',
'Massive damage from ammo explosions. Chain reactions can be catastrophic. Store ammo in separate locations.'),

('MECH_WARRIOR', 'FPS Mech Pilot Injuries', 'MechWarrior Video Games', 'FPS Progression', 'INJURIES', NULL,
'Pilot injuries affect combat performance. BattleTech 2018 features pilot injuries where pilots can be injured in combat, reducing their effectiveness. Injuries can be caused by knockdowns, critical hits, or excessive stress. Injured pilots have reduced accuracy and may make poor decisions. Effective injury management requires understanding the pilot health status and providing medical attention when needed. Pilot injuries add a layer of realism and strategic depth to pilot management.',
'Reduced accuracy and decision-making. Medical attention required. Realistic pilot management.'),

('MECH_WARRIOR', 'FPS Mech Pilot Morale', 'MechWarrior Video Games', 'FPS Progression', 'PILOT_MORALE', NULL,
'Pilot morale affects combat performance. BattleTech 2018 features pilot morale where pilots can become stressed or broken under pressure. Morale is replenished if the player is doing well in battle but not so much if losing. Effective pilot morale management requires understanding the pilot psychological limits and providing support when needed. Pilot morale is essential for combat effectiveness and pilot survival.',
'Stressed or broken under pressure. Replenished by success. Essential for combat effectiveness and pilot survival.'),

('MECH_WARRIOR', 'FPS Mech Pilot Traits', 'MechWarrior Video Games', 'FPS Progression', 'PILOT_TRAITS', NULL,
'Pilot traits provide special abilities. MW5:Clans features pilot traits where pilots can have special traits like being good with specific mechs or weapon types. Pilot traits provide passive bonuses that enhance combat effectiveness. Effective pilot trait management requires understanding the trait characteristics and the mission requirements. Pilot traits are essential for customization and combat effectiveness.',
'Special traits like being good with specific mechs or weapon types. Passive bonuses for combat effectiveness. Essential for customization and combat effectiveness.'),

('MECH_WARRIOR', 'FPS Mech Pilot Training', 'MechWarrior Video Games', 'FPS Progression', 'PILOT_TRAINING', NULL,
'Pilot training improves pilot capabilities. MW5:Mercenaries features pilot training where the player can send pilots to training to gain XP and traits. Pilot training requires understanding the pilot capabilities and the available resources. Effective pilot training allows the player to improve their pilots and take on more challenging missions. Pilot training is essential for growth and progression.',
'Send pilots to training to gain XP and traits. Understand pilot capabilities and available resources. Essential for growth and progression.'),

('MECH_WARRIOR', 'FPS Mech Pilot Hiring', 'MechWarrior Video Games', 'FPS Progression', 'PILOT_HIRING_DETAIL', NULL,
'Pilot hiring expands the mercenary outfit. MW5:Mercenaries features pilot hiring where the player can hire new pilots to join their mercenary outfit. Pilot hiring requires understanding the pilot capabilities and the available resources. Effective pilot hiring allows the player to expand their mercenary outfit and take on more missions. Pilot hiring is essential for growth and progression.',
'Hire new pilots to join the mercenary outfit. Understand pilot capabilities and available resources. Essential for growth and progression.'),

('MECH_WARRIOR', 'FPS Mech Financial Management', 'MechWarrior Video Games', 'FPS Progression', 'FINANCIAL_DETAIL', NULL,
'Financial management is essential for sustainability. MW5:Mercenaries features financial management where the player must manage their resources to afford mech repairs, new weapons, and pilot hiring. Financial management requires understanding the income and expenses. Effective financial management allows the player to grow their mercenary outfit and acquire better equipment. Financial management is essential for sustainability and progression.',
'Manage resources for repairs, weapons, and pilot hiring. Understand income and expenses. Essential for sustainability and progression.'),

('MECH_WARRIOR', 'FPS Mech Repair Costs', 'MechWarrior Video Games', 'FPS Progression', 'REPAIR_DETAIL', NULL,
'Repair costs scale with damage taken. MW5:Mercenaries features repair costs where the player must pay for mech repairs after missions. Repair costs scale with damage taken — the more damage a mech takes, the more costly the repair bill. Effective repair cost management requires understanding the damage taken and the available resources. Repair costs are essential for resource management and sustainability.',
'Pay for mech repairs after missions. Scale with damage taken. Essential for resource management and sustainability.'),

('MECH_WARRIOR', 'FPS Mech Salvage Value', 'MechWarrior Video Games', 'FPS Progression', 'SALVAGE_DETAIL', NULL,
'Salvage value determines the rewards from destroyed mechs. MW5:Mercenaries features salvage value where the player can salvage destroyed enemy mechs for weapons and components. Salvage value depends on the mech condition and available components. Effective salvage value management requires understanding the mech condition and the available components. Salvage value is essential for resource acquisition and progression.',
'Salvage destroyed enemy mechs for weapons and components. Depends on mech condition and available components. Essential for resource acquisition and progression.'),

('MECH_WARRIOR', 'FPS Mech Ejection Decision', 'MechWarrior Video Games', 'FPS Combat', 'EJECTION_DECISION_DETAIL', NULL,
'Ejection decision is a last resort. MW5:Mercenaries features ejection decision where the player can eject to survive when the mech is critically damaged. Ejection decision requires understanding the mech condition and the combat scenarios. Effective ejection decision allows the player to survive and continue the mission with another mech. Ejection decision is essential for pilot survival and mission continuation.',
'Eject to survive when mech is critically damaged. Understand mech condition and combat scenarios. Essential for pilot survival and mission continuation.'),

('MECH_WARRIOR', 'FPS Mech Reactor Explosion Prevention', 'MechWarrior Video Games', 'FPS Combat', 'REACTOR_PREVENTION_DETAIL', NULL,
'Reactor explosion prevention is critical for survival. MW5:Mercenaries features reactor explosion prevention where the player must manage heat and damage to avoid reactor explosion. Reactor explosion prevention requires understanding the heat management and the combat scenarios. Effective reactor explosion prevention allows the player to survive and continue fighting. Reactor explosion prevention is essential for pilot survival and mission success.',
'Manage heat and damage to avoid reactor explosion. Understand heat management and combat scenarios. Essential for pilot survival and mission success.'),

('MECH_WARRIOR', 'FPS Mech Heat Override Risk', 'MechWarrior Video Games', 'FPS Combat', 'HEAT_OVERRIDE_DETAIL', NULL,
'Heat override risk is a critical decision. MW5:Mercenaries features heat override risk where the player can override the automatic shutdown to continue fighting, but this risks internal damage and reactor explosion. Heat override risk requires understanding the heat management and the combat scenarios. Effective heat override risk management allows the player to continue fighting when necessary while minimizing the risk of reactor explosion. Heat override risk is essential for sustained combat effectiveness.',
'Override automatic shutdown to continue fighting. Risks internal damage and reactor explosion. Essential for sustained combat effectiveness.'),

('MECH_WARRIOR', 'FPS Mech Weapon Group Strategy', 'MechWarrior Video Games', 'FPS Combat', 'WEAPON_GROUP_DETAIL', NULL,
'Weapon group strategy optimizes firepower. MW5:Mercenaries features weapon group strategy where the player can create multiple weapon groups with different firing patterns. Weapon group strategy requires understanding the weapon characteristics and the combat scenarios. Effective weapon group strategy allows the player to optimize firepower and heat management. Weapon group strategy is essential for tactical flexibility and combat effectiveness.',
'Multiple weapon groups with different firing patterns. Understand weapon characteristics and combat scenarios. Essential for tactical flexibility and combat effectiveness.'),

('MECH_WARRIOR', 'FPS Mech Cooldown Management', 'MechWarrior Video Games', 'FPS Combat', 'COOLDOWN_DETAIL', NULL,
'Cooldown management is essential for sustained firepower. MW5:Mercenaries features cooldown management where the player must manage the weapon cooldowns to maintain sustained firepower. Cooldown management requires understanding the weapon characteristics and the combat scenarios. Effective cooldown management allows the player to maintain sustained firepower without overheating. Cooldown management is essential for sustained combat effectiveness.',
'Manage weapon cooldowns for sustained firepower. Understand weapon characteristics and combat scenarios. Essential for sustained combat effectiveness.'),

('MECH_WARRIOR', 'FPS Mech Tradeoff Decisions', 'MechWarrior Video Games', 'FPS Customization', 'TRADEOFF_DETAIL', NULL,
'Tradeoff decisions are key customization choices. MW5:Mercenaries features tradeoff decisions where the player must choose between speed vs armor, weapon vs armor, and heat vs firepower. Faster mechs have less armor, while heavier mechs have more armor but less speed. Heavily armed mechs have less armor, while heavily armored mechs have fewer weapons. Energy weapons generate more heat but provide sustained firepower, while ballistic weapons generate less heat but have longer cooldowns. Effective tradeoff decisions require understanding the mission requirements and the combat scenarios. Tradeoff decisions are essential for mech customization and optimization.',
'Speed vs armor, weapon vs armor, heat vs firepower. Understand mission requirements and combat scenarios. Essential for mech customization and optimization.'),

('MECH_WARRIOR', 'FPS Mech Tonnage Limits', 'MechWarrior Video Games', 'FPS Customization', 'TONNAGE_DETAIL', NULL,
'Tonnage limits restrict mech customization. MW5:Mercenaries features tonnage limits where the player cannot exceed the mech maximum tonnage. Tonnage limits affect speed, armor, and weapon capacity. Effective tonnage limit management requires understanding the weight distribution and the available tonnage. Tonnage limits are essential for mech customization and optimization.',
'Cannot exceed maximum tonnage. Affects speed, armor, and weapon capacity. Essential for mech customization and optimization.'),

('MECH_WARRIOR', 'FPS Mech Armor Distribution', 'MechWarrior Video Games', 'FPS Customization', 'ARMOR_DIST_DETAIL', NULL,
'Armor distribution provides protection optimization. MW5:Clans features armor distribution where the player can manually add armor to each slot. Armor distribution provides protection optimization. Effective armor distribution requires understanding the armor distribution and the mission requirements. Armor distribution is essential for protection optimization and survivability.',
'Manually add armor to each slot. Protection optimization. Essential for protection optimization and survivability.'),

('MECH_WARRIOR', 'FPS Mech Hardpoint Customization', 'MechWarrior Video Games', 'FPS Customization', 'HARDPOINT_DETAIL', NULL,
'Hardpoint customization provides weapon placement optimization. MW4:Vengeance features hardpoint customization where specific weapons can only be mounted on designated hardpoints. Hardpoint customization provides weapon placement optimization. Effective hardpoint customization requires understanding the hardpoint layout and the weapon characteristics. Hardpoint customization is essential for weapon placement optimization and combat effectiveness.',
'Specific weapons on designated hardpoints. Weapon placement optimization. Essential for weapon placement optimization and combat effectiveness.'),

('MECH_WARRIOR', 'FPS Mech Loadout Variety', 'MechWarrior Video Games', 'FPS Customization', 'LOADOUT_DETAIL', NULL,
'Loadout variety provides tactical flexibility. MW5:Mercenaries features loadout variety where the player can create different loadouts for different missions and combat scenarios. Loadout variety requires understanding the weapon characteristics and the mission requirements. Effective loadout variety allows the player to adapt to different combat scenarios and optimize combat effectiveness. Loadout variety is essential for tactical flexibility and combat effectiveness.',
'Different loadouts for different missions and scenarios. Understand weapon characteristics and mission requirements. Essential for tactical flexibility.'),

('MECH_WARRIOR', 'FPS Mech Customization Depth', 'MechWarrior Video Games', 'FPS Customization', 'CUSTOMIZATION_DETAIL', NULL,
'Mech customization depth provides extensive personalization. MW5:Mercenaries features extensive mech customization where the player can customize every aspect of the mech including weapons, armor, components, and internal systems. Mech customization depth requires understanding the mech characteristics and the available options. Effective mech customization depth allows the player to create unique and optimized mechs for different missions and combat scenarios. Mech customization depth is essential for personalization and optimization.',
'Customize every aspect of the mech. Understand mech characteristics and available options. Essential for personalization and optimization.'),

('MECH_WARRIOR', 'FPS Mech-Pilot Synergy', 'MechWarrior Video Games', 'FPS Progression', 'SYNERGY', NULL,
'Mech-pilot synergy enhances combat effectiveness. MW5:Clans features mech-pilot synergy where pilots can have special traits that enhance their performance with specific mechs. Mech-pilot synergy requires understanding the pilot traits and the mech characteristics. Effective mech-pilot synergy allows the player to optimize combat effectiveness and mission success. Mech-pilot synergy is essential for sustained combat effectiveness and pilot management.',
'Pilots have special traits for specific mechs. Understand pilot traits and mech characteristics. Essential for sustained combat effectiveness.');
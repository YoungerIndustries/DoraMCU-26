/* Game UI Profile Configuration
   Maps game types to UI profiles with panel layouts, camera modes, and HUD elements */

const GameUIProfile = {
    // ── Profile Definitions ──

    FPS: {
        id: 'fps',
        displayName: 'First-Person Shooter',
        description: 'Full-screen 3D with overlay HUD',
        cameraMode: 'fps',
        layout: 'fullscreen',
        gameTypes: ['FPS', 'MECHWARRIOR'],
        panels: {
            info: {
                title: 'Player Status',
                components: [
                    { type: 'stat-bar', label: 'Health', binding: 'health' },
                    { type: 'stat-bar', label: 'Shield', binding: 'shield' },
                    { type: 'stat-bar', label: 'Armor', binding: 'armor' },
                    { type: 'stat-bar', label: 'Ammo', binding: 'ammo' },
                    { type: 'stat-number', label: 'Score', binding: 'score' }
                ]
            },
            prompt: {
                title: 'Kill Feed',
                components: [
                    { type: 'kill-feed', binding: 'kill-feed' }
                ]
            },
            responses: {
                title: 'Weapons & Actions',
                categories: ['fire', 'reload', 'switch-weapon', 'move', 'interact'],
                components: [
                    { type: 'action-button', icon: '💥', label: 'Fire', binding: 'fire' },
                    { type: 'action-button', icon: '🔄', label: 'Reload', binding: 'reload' },
                    { type: 'action-button', icon: '🔫', label: 'Switch Weapon', binding: 'switch-weapon' }
                ]
            },
            actions: {
                title: 'Scoreboard',
                components: [
                    { type: 'scoreboard', binding: 'scoreboard' },
                    { type: 'chat-input', placeholder: 'Team chat...', binding: 'team-chat' }
                ]
            }
        },
        stateBar: {
            items: ['health', 'shield', 'armor', 'ammo', 'score', 'team_score'],
            layout: 'horizontal'
        },
        view3D: {
            cameraMode: 'fps',
            showCrosshair: true,
            showMinimap: true,
            pointerLock: true,
            weaponModel: true
        },
        theme: {
            accentColor: '#10b981',
            panelBackground: '#1a2332',
            borderColor: '#2a3a52'
        }
    },

    STRATEGY_4X: {
        id: 'strategy_4x',
        displayName: '4X Empire Strategy',
        description: 'Split view with galaxy map + strategy panels',
        cameraMode: 'galaxy',
        layout: 'split',
        gameTypes: ['DORAMCU_4X', 'STAR_TREK_4X', 'ZERO_ZERO_SEVEN', 'TRANSPORT_TYCOON'],
        panels: {
            info: {
                title: 'Empire Status',
                components: [
                    { type: 'selector', label: 'Session', binding: 'session-select' },
                    { type: 'selector', label: 'Faction', binding: 'faction-select' },
                    { type: 'stat-number', label: 'Turn', binding: 'turn' },
                    { type: 'stat-bar', label: 'Credits', binding: 'credits' },
                    { type: 'stat-bar', label: 'Minerals', binding: 'minerals' },
                    { type: 'stat-bar', label: 'Energy', binding: 'energy' },
                    { type: 'stat-bar', label: 'Influence', binding: 'influence' }
                ]
            },
            prompt: {
                title: 'Mission Briefing',
                components: [
                    { type: 'narrative-text', label: 'Waiting for mission...', binding: 'mission-briefing' }
                ]
            },
            responses: {
                title: 'Strategy Actions',
                categories: ['explore', 'expand', 'exploit', 'exterminate', 'diplomacy', 'research'],
                components: [
                    { type: 'action-button', icon: '🔭', label: 'Scan System', binding: 'scan-system' },
                    { type: 'action-button', icon: '🚀', label: 'Colonize', binding: 'colonize' },
                    { type: 'action-button', icon: '⚔️', label: 'Combat', binding: 'combat' },
                    { type: 'action-button', icon: '🤝', label: 'Diplomacy', binding: 'diplomacy' },
                    { type: 'action-button', icon: '🔬', label: 'Research', binding: 'research' },
                    { type: 'action-button', icon: '🏗️', label: 'Build', binding: 'build' },
                    { type: 'action-button', icon: '📦', label: 'Trade', binding: 'trade' },
                    { type: 'action-button', icon: '⏭️', label: 'End Turn', binding: 'end-turn' }
                ]
            },
            actions: {
                title: 'Event Log',
                components: [
                    { type: 'event-log', binding: 'event-log' },
                    { type: 'chat-input', placeholder: 'Command...', binding: 'command-chat' }
                ]
            }
        },
        stateBar: {
            items: ['turn', 'credits', 'minerals', 'energy', 'influence'],
            layout: 'horizontal'
        },
        view3D: {
            cameraMode: 'galaxy',
            showCrosshair: false,
            showMinimap: true,
            orbitControls: true,
            galaxyMap: true
        },
        theme: {
            accentColor: '#3b82f6',
            panelBackground: '#1e293b',
            borderColor: '#334155'
        }
    },

    RPG_NARRATIVE: {
        id: 'rpg_narrative',
        displayName: 'Character-Driven RPG',
        description: 'Split view with narrative + character sheet + action buttons',
        cameraMode: 'narrative',
        layout: 'split',
        gameTypes: ['TRAVELLER', 'CAPTAINS_LOG', 'EARTH_2026', 'BABYLON_5', 'DND_5E', 'SFB', 'STAR_WARS'],
        panels: {
            info: {
                title: 'Character Sheet',
                components: [
                    { type: 'selector', label: 'Character', binding: 'character-select' },
                    { type: 'selector', label: 'Campaign', binding: 'campaign-select' },
                    { type: 'stat-bar', label: 'HP', binding: 'hp' },
                    { type: 'stat-bar', label: 'Temp HP', binding: 'temp_hp' },
                    { type: 'stat-bar', label: 'Spell Slots', binding: 'spell_slots' },
                    { type: 'condition-icons', binding: 'conditions' }
                ]
            },
            prompt: {
                title: 'DM Narrative',
                components: [
                    { type: 'narrative-text', label: 'The adventure begins...', binding: 'dm-narrative' }
                ]
            },
            responses: {
                title: 'Action Buttons',
                categories: ['attack', 'cast_spell', 'use_item', 'move', 'dodge', 'help', 'rest'],
                components: [
                    { type: 'action-button', icon: '⚔️', label: 'Attack', binding: 'attack' },
                    { type: 'action-button', icon: '✨', label: 'Cast Spell', binding: 'cast_spell' },
                    { type: 'action-button', icon: '🎒', label: 'Use Item', binding: 'use_item' },
                    { type: 'action-button', icon: '🏃', label: 'Move', binding: 'move' },
                    { type: 'action-button', icon: '🛡️', label: 'Defend', binding: 'defend' },
                    { type: 'action-button', icon: '🎲', label: 'Roll Dice', binding: 'roll_dice' },
                    { type: 'action-button', icon: '💬', label: 'Talk', binding: 'talk' },
                    { type: 'action-button', icon: '🔍', label: 'Search', binding: 'search' }
                ]
            },
            actions: {
                title: 'Combat Log',
                components: [
                    { type: 'dice-roller', binding: 'dice-roller' },
                    { type: 'combat-log', binding: 'combat-log' },
                    { type: 'chat-input', placeholder: 'Action description...', binding: 'action-chat' }
                ]
            }
        },
        stateBar: {
            items: ['hp', 'temp_hp', 'spell_slots', 'conditions'],
            layout: 'horizontal'
        },
        view3D: {
            cameraMode: 'narrative',
            showCrosshair: false,
            showMinimap: false,
            cinematicCamera: true,
            characterModels: true
        },
        theme: {
            accentColor: '#a855f7',
            panelBackground: '#1a1a2e',
            borderColor: '#2d2d44'
        }
    },

    GROUND_COMBAT: {
        id: 'ground_combat',
        displayName: 'Tactical Battlefield',
        description: 'Tactical 3D view + unit panels + order queue',
        cameraMode: 'tactical',
        layout: 'split',
        gameTypes: ['THIRD_REICH', 'STARSHIP_TROOPERS'],
        panels: {
            info: {
                title: 'Unit Status',
                components: [
                    { type: 'selector', label: 'Unit', binding: 'unit-select' },
                    { type: 'stat-bar', label: 'Unit HP', binding: 'unit_hp' },
                    { type: 'stat-bar', label: 'Ammo', binding: 'ammo' },
                    { type: 'stat-bar', label: 'Morale', binding: 'morale' },
                    { type: 'stat-number', label: 'Turn Phase', binding: 'turn_phase' }
                ]
            },
            prompt: {
                title: 'Battlefield Log',
                components: [
                    { type: 'battle-log', binding: 'battle-log' }
                ]
            },
            responses: {
                title: 'Order Queue',
                categories: ['move', 'attack', 'suppress', 'take_cover', 'call_artillery', 'retreat'],
                components: [
                    { type: 'action-button', icon: '🏃', label: 'Move', binding: 'move' },
                    { type: 'action-button', icon: '🎯', label: 'Attack', binding: 'attack' },
                    { type: 'action-button', icon: '💨', label: 'Suppress', binding: 'suppress' },
                    { type: 'action-button', icon: '🛡️', label: 'Take Cover', binding: 'take_cover' },
                    { type: 'action-button', icon: '💣', label: 'Call Artillery', binding: 'call_artillery' },
                    { type: 'action-button', icon: '🏳️', label: 'Retreat', binding: 'retreat' },
                    { type: 'action-button', icon: '⏭️', label: 'End Turn', binding: 'end_turn' }
                ]
            },
            actions: {
                title: 'Squad Panel',
                components: [
                    { type: 'squad-list', binding: 'squad-list' },
                    { type: 'chat-input', placeholder: 'Order...', binding: 'order-chat' }
                ]
            }
        },
        stateBar: {
            items: ['unit_hp', 'ammo', 'morale', 'turn_phase'],
            layout: 'horizontal'
        },
        view3D: {
            cameraMode: 'tactical',
            showCrosshair: false,
            showMinimap: true,
            gridOverlay: true,
            isometricCamera: true
        },
        theme: {
            accentColor: '#f59e0b',
            panelBackground: '#1c1917',
            borderColor: '#292524'
        }
    }
};

// ── Game Type to Profile Mapping ──

const GameTypeToProfile = {};
Object.values(GameUIProfile).forEach(profile => {
    profile.gameTypes.forEach(gameType => {
        GameTypeToProfile[gameType] = profile.id;
    });
});

/**
 * Get the UI profile for a given game type.
 * @param {string} gameType - Game type enum name (e.g., 'TRAVELLER', 'FPS')
 * @returns {object} UI profile configuration
 */
function getProfileForGameType(gameType) {
    const profileId = GameTypeToProfile[gameType];
    if (!profileId) {
        console.warn(`[GameUIProfile] No profile for game type: ${gameType}, defaulting to RPG_NARRATIVE`);
        return GameUIProfile.RPG_NARRATIVE;
    }
    return GameUIProfile[profileId.toUpperCase().replace(/ /g, '_')];
}

/**
 * Get all game types for a profile.
 * @param {string} profileId - Profile ID (e.g., 'fps', 'strategy_4x')
 * @returns {string[]} Array of game type names
 */
function getGameTypesForProfile(profileId) {
    const profile = GameUIProfile[profileId.toUpperCase().replace(/ /g, '_')];
    return profile ? profile.gameTypes : [];
}

/**
 * Get all profile IDs.
 * @returns {string[]} Array of profile IDs
 */
function getAllProfileIds() {
    return Object.keys(GameUIProfile);
}

// Expose globally
window.GameUIProfile = GameUIProfile;
window.getProfileForGameType = getProfileForGameType;
window.getGameTypesForProfile = getGameTypesForProfile;
window.getAllProfileIds = getAllProfileIds;

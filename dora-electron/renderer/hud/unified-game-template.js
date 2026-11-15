/* Unified Game Template System
   Merges game-specific templates with UI profile defaults */

class UnifiedGameTemplate {
    constructor() {
        this.templates = {};
        this.currentProfile = null;
        this.currentGameType = null;
    }

    /**
     * Load a template for a specific game type.
     * Merges the game-specific template with the UI profile defaults.
     * @param {string} gameType - Game type enum name (e.g., 'TRAVELLER', 'FPS')
     * @param {object} gameSpecificTemplate - Optional game-specific template overrides
     * @returns {object} Merged template configuration
     */
    loadTemplate(gameType, gameSpecificTemplate = null) {
        // Get the UI profile for this game type
        const profile = window.getProfileForGameType(gameType);
        this.currentProfile = profile;
        this.currentGameType = gameType;

        // Start with profile defaults
        const mergedTemplate = {
            id: gameType.toLowerCase(),
            name: gameType,
            gameType: gameType,
            profileId: profile.id,
            profileDisplayName: profile.displayName,
            layout: profile.layout,
            cameraMode: profile.cameraMode,
            theme: { ...profile.theme },
            panels: JSON.parse(JSON.stringify(profile.panels)),
            stateBar: { ...profile.stateBar },
            view3D: { ...profile.view3D }
        };

        // Merge game-specific overrides if provided
        if (gameSpecificTemplate) {
            this._mergeTemplate(mergedTemplate, gameSpecificTemplate);
        }

        // Store the merged template
        this.templates[gameType] = mergedTemplate;

        console.log(`[UnifiedGameTemplate] Loaded template for ${gameType} with profile ${profile.id}`);
        return mergedTemplate;
    }

    /**
     * Merge game-specific template overrides into the base template.
     * @param {object} base - Base template from UI profile
     * @param {object} overrides - Game-specific overrides
     */
    _mergeTemplate(base, overrides) {
        // Merge theme
        if (overrides.theme) {
            base.theme = { ...base.theme, ...overrides.theme };
        }

        // Merge panels
        if (overrides.panels) {
            for (const panelKey of Object.keys(overrides.panels)) {
                if (base.panels[panelKey]) {
                    const panelOverride = overrides.panels[panelKey];
                    if (panelOverride.title) {
                        base.panels[panelKey].title = panelOverride.title;
                    }
                    if (panelOverride.components) {
                        // Merge components by binding
                        const componentMap = {};
                        base.panels[panelKey].components.forEach(comp => {
                            componentMap[comp.binding] = comp;
                        });
                        panelOverride.components.forEach(comp => {
                            componentMap[comp.binding] = comp;
                        });
                        base.panels[panelKey].components = Object.values(componentMap);
                    }
                    if (panelOverride.visible !== undefined) {
                        base.panels[panelKey].visible = panelOverride.visible;
                    }
                }
            }
        }

        // Merge state bar
        if (overrides.stateBar) {
            if (overrides.stateBar.items) {
                base.stateBar.items = overrides.stateBar.items;
            }
            if (overrides.stateBar.layout) {
                base.stateBar.layout = overrides.stateBar.layout;
            }
        }

        // Merge 3D view
        if (overrides.view3D) {
            base.view3D = { ...base.view3D, ...overrides.view3D };
        }

        // Merge layout
        if (overrides.layout) {
            base.layout = overrides.layout;
        }
    }

    /**
     * Get the current template.
     * @returns {object|null} Current template or null
     */
    getCurrentTemplate() {
        return this.templates[this.currentGameType] || null;
    }

    /**
     * Get the current UI profile.
     * @returns {object|null} Current UI profile or null
     */
    getCurrentProfile() {
        return this.currentProfile;
    }

    /**
     * Get all loaded templates.
     * @returns {object} Map of game type to template
     */
    getAllTemplates() {
        return this.templates;
    }

    /**
     * Clear all templates.
     */
    clear() {
        this.templates = {};
        this.currentProfile = null;
        this.currentGameType = null;
    }

    /**
     * Apply template to HUD controller.
     * @param {object} hudController - HUDController instance
     */
    applyToHUD(hudController) {
        const template = this.getCurrentTemplate();
        if (!template || !hudController) {
            console.warn('[UnifiedGameTemplate] No template or HUD controller to apply');
            return;
        }

        // Apply theme
        if (template.theme) {
            const root = document.documentElement;
            root.style.setProperty('--hud-accent-primary', template.theme.accentColor);
            root.style.setProperty('--hud-panel-background', template.theme.panelBackground);
            root.style.setProperty('--hud-border-color', template.theme.borderColor);
        }

        // Apply panel titles
        if (template.panels) {
            if (template.panels.info && template.panels.info.title) {
                const infoTitle = document.querySelector('.hud-panel-info .panel-title');
                if (infoTitle) infoTitle.textContent = template.panels.info.title;
            }
            if (template.panels.prompt && template.panels.prompt.title) {
                const promptTitle = document.querySelector('.hud-panel-prompt .panel-title');
                if (promptTitle) promptTitle.textContent = template.panels.prompt.title;
            }
            if (template.panels.responses && template.panels.responses.title) {
                const responsesTitle = document.querySelector('.hud-panel-responses .panel-title');
                if (responsesTitle) responsesTitle.textContent = template.panels.responses.title;
            }
            if (template.panels.actions && template.panels.actions.title) {
                const actionsTitle = document.querySelector('.hud-panel-actions .panel-title');
                if (actionsTitle) actionsTitle.textContent = template.panels.actions.title;
            }
        }

        console.log(`[UnifiedGameTemplate] Applied template ${template.id} to HUD`);
    }
}

// Expose globally
window.UnifiedGameTemplate = UnifiedGameTemplate;

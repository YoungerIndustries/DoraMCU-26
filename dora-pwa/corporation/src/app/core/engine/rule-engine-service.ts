import { Injectable } from '@angular/core';

/**
 * Result of executing a game rule.
 */
export interface RuleResult {
  success: boolean;
  message: string;
  effects: Record<string, unknown>[];
  timestamp: number;
}

/**
 * Represents a loaded game rule set for a specific system.
 */
export interface GameRuleSet {
  system: string;
  version: number;
  rules: Record<string, unknown>;
}

/**
 * Executes game rules locally for Traveller, Earth 2026, and other systems.
 *
 * Loads rule definitions from JSON files and provides methods for
 * executing game mechanics like skill checks, combat, and narrative events.
 */
@Injectable({
  providedIn: 'root'
})
export class RuleEngineService {
  private ruleSets = new Map<string, GameRuleSet>();

  /**
   * Load a game rule set from JSON data.
   */
  loadRuleSet(system: string, rules: Record<string, unknown>) {
    const ruleSet: GameRuleSet = {
      system,
      version: (rules['version'] as number) || 1,
      rules
    };
    this.ruleSets.set(system, ruleSet);
    console.log(`[RuleEngine] Loaded rule set: ${system} v${ruleSet.version}`);
  }

  /**
   * Fetch and load a game rule set from a URL.
   */
  async loadRuleSetFromUrl(system: string, url: string) {
    try {
      const response = await fetch(url);
      if (!response.ok) {
        throw new Error(`Failed to load rules: ${response.statusText}`);
      }
      const rules = await response.json();
      this.loadRuleSet(system, rules);
      return true;
    } catch (error) {
      console.error(`[RuleEngine] Failed to load rules from ${url}:`, error);
      return false;
    }
  }

  /**
   * Execute a skill check based on the game system rules.
   */
  executeSkillCheck(system: string, skill: string, difficulty: number, modifiers: number = 0): RuleResult {
    const ruleSet = this.ruleSets.get(system);
    if (!ruleSet) {
      return {
        success: false,
        message: `Rule set not loaded for system: ${system}`,
        effects: [],
        timestamp: Date.now()
      };
    }

    // Simple d6-based check (Traveller-style)
    const roll = this.rollDice(3, 6) + modifiers;
    const target = difficulty * 2; // Simplified difficulty scaling
    const success = roll >= target;

    return {
      success,
      message: success
        ? `Skill check passed! Rolled ${roll} vs ${target}`
        : `Skill check failed. Rolled ${roll} vs ${target}`,
      effects: [
        { type: 'skill_check', skill, roll, target, success }
      ],
      timestamp: Date.now()
    };
  }

  /**
   * Execute a combat action based on the game system rules.
   */
  executeCombatAction(system: string, action: string, target: Record<string, unknown>): RuleResult {
    const ruleSet = this.ruleSets.get(system);
    if (!ruleSet) {
      return {
        success: false,
        message: `Rule set not loaded for system: ${system}`,
        effects: [],
        timestamp: Date.now()
      };
    }

    // Simplified combat resolution
    const hitRoll = this.rollDice(2, 6);
    const damageRoll = this.rollDice(1, 8);
    const hit = hitRoll >= 4;

    return {
      success: hit,
      message: hit
        ? `Attack hit! Dealt ${damageRoll} damage`
        : `Attack missed`,
      effects: hit ? [
        { type: 'damage', amount: damageRoll, target }
      ] : [],
      timestamp: Date.now()
    };
  }

  /**
   * Generate a narrative event based on the current game state.
   */
  generateNarrativeEvent(system: string, context: Record<string, unknown>): RuleResult {
    const ruleSet = this.ruleSets.get(system);
    if (!ruleSet) {
      return {
        success: false,
        message: `Rule set not loaded for system: ${system}`,
        effects: [],
        timestamp: Date.now()
      };
    }

    // Simplified narrative event generation
    const eventRoll = this.rollDice(1, 20);
    let eventType = 'routine';
    if (eventRoll >= 18) eventType = 'critical';
    else if (eventRoll >= 12) eventType = 'significant';

    return {
      success: true,
      message: `Generated ${eventType} narrative event`,
      effects: [
        { type: 'narrative_event', eventType, context }
      ],
      timestamp: Date.now()
    };
  }

  /**
   * Roll dice and return the total.
   */
  private rollDice(count: number, sides: number): number {
    let total = 0;
    for (let i = 0; i < count; i++) {
      total += Math.floor(Math.random() * sides) + 1;
    }
    return total;
  }

  /**
   * Check if a rule set is loaded for the given system.
   */
  hasRuleSet(system: string): boolean {
    return this.ruleSets.has(system);
  }

  /**
   * Get the loaded rule set for the given system.
   */
  getRuleSet(system: string): GameRuleSet | undefined {
    return this.ruleSets.get(system);
  }
}

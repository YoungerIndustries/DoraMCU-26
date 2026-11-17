/** Traveller character abilities (DEX, ST, END, INT, EDU, SOC, APP) */
export interface CharacterAbilities {
  dex: number;  // Dexterity (1-20)
  st: number;   // Strength (1-20)
  end: number;  // Endurance (1-20)
  int: number;  // Intelligence (1-20)
  edu: number;  // Education (1-20)
  soc: number;  // Social Standing (1-20)
  app: number;  // Appearance (1-20)
}

/** Individual ability with display info */
export interface AbilityInfo {
  key: string;
  name: string;
  score: number;
  modifier: number;  // (score - 10) / 2, rounded down
  color: string;     // CSS color based on score range
}

/** Traveller skill with category and level */
export interface CharacterSkill {
  name: string;
  category: string;  // SOCIAL, COMBAT, TECHNICAL, SCIENTIFIC, etc.
  level: number;     // 0-20
  description?: string;
}

/** Career tour entry from Traveller character */
export interface CharacterCareer {
  tourNumber: number;
  career: string;     // Army, Navy, Merchant, Civilian, etc.
  rank: string;       // Rank achieved
  termLength: number; // Years served
  wounds?: number;    // Character wounds from this tour
  discharges?: string; // Discharge type
}

/** Character corporation stats */
export interface CharacterStats {
  characterId: number;
  characterName: string;
  age: number;
  wealth: number;
  totalXpEarned: number;
  contractsCompleted: number;
  contractsActive: number;
  levelUpThreshold: number;  // XP needed for next level
  isLevelUpReady: boolean;
  assignments: CharacterAssignment[];
  qualifiedContracts: QualifiedContract[];
}

/** Character assignment to a contract */
export interface CharacterAssignment {
  id: number;
  contractId: number;
  contractTitle: string;
  assignedAt: string;
  xpEarned: number;
  status: string;  // ACTIVE, COMPLETED, FAILED
}

/** Contract that character qualifies for based on skills */
export interface QualifiedContract {
  id: number;
  title: string;
  type: string;
  payment: number;
  xpReward: number;
  difficulty: number;
  durationTurns: number;
  requiredSkills?: string[];
  characterBonus: number;  // Performance bonus from character's abilities/skills
  qualificationReason: string;  // Why character qualifies (e.g., "ENTREPRENEUR skill")
}

/** XP thresholds for character level-up */
export const CHARACTER_XP_THRESHOLDS = [
  { level: 1, xpRequired: 0 },
  { level: 2, xpRequired: 100 },
  { level: 3, xpRequired: 300 },
  { level: 4, xpRequired: 600 },
  { level: 5, xpRequired: 1000 },
  { level: 6, xpRequired: 1500 },
  { level: 7, xpRequired: 2100 },
  { level: 8, xpRequired: 2800 },
  { level: 9, xpRequired: 3600 },
  { level: 10, xpRequired: 4500 },
];

/** Ability to contract type mapping */
export const ABILITY_CONTRACT_MAP: Record<string, string[]> = {
  int: ['MISSION'],      // Intelligence boosts research/scientific missions
  edu: ['TRADE'],        // Education boosts commercial contracts
  soc: ['MISSION'],      // Social Standing boosts diplomacy missions
  end: ['SECURITY'],     // Endurance boosts combat/protection contracts
  dex: ['LOGISTICS'],    // Dexterity boosts supply/transport contracts
  st: ['SECURITY', 'LOGISTICS', 'MISSION', 'TRADE'],  // Strength gives small bonus to all
  app: ['TRADE'],        // Appearance gives negotiation charm
};

/** Skill category to contract type mapping */
export const SKILL_CATEGORY_CONTRACT_MAP: Record<string, string[]> = {
  SOCIAL: ['TRADE', 'MISSION'],
  COMBAT: ['SECURITY'],
  TECHNICAL: ['LOGISTICS'],
  SCIENTIFIC: ['MISSION'],
  MEDICAL: ['MISSION'],
  PILOT: ['LOGISTICS', 'MISSION'],
  STOW: ['LOGISTICS'],
  CRIMINAL: ['SECURITY', 'MISSION'],
};

/** Get ability color based on score */
export function getAbilityColor(score: number): string {
  if (score >= 14) return '#4caf50';  // Green - excellent
  if (score >= 10) return '#2196f3';  // Blue - above average
  if (score >= 7) return '#ff9800';   // Orange - average
  return '#f44336';                    // Red - below average
}

/** Get ability modifier: (score - 10) / 2, rounded down */
export function getAbilityModifier(score: number): number {
  return Math.floor((score - 10) / 2);
}

/** Get current level based on XP */
export function getCharacterLevel(xp: number): number {
  let level = 1;
  for (const threshold of CHARACTER_XP_THRESHOLDS) {
    if (xp >= threshold.xpRequired) {
      level = threshold.level;
    } else {
      break;
    }
  }
  return level;
}

/** Get XP needed for next level */
export function getXpForNextLevel(xp: number): number {
  for (const threshold of CHARACTER_XP_THRESHOLDS) {
    if (xp < threshold.xpRequired) {
      return threshold.xpRequired - xp;
    }
  }
  return 0;  // Max level
}

/** Get progress percentage to next level */
export function getLevelProgress(xp: number): number {
  const currentLevel = getCharacterLevel(xp);
  if (currentLevel >= 10) return 100;  // Max level
  
  const currentThreshold = CHARACTER_XP_THRESHOLDS[currentLevel - 1]?.xpRequired || 0;
  const nextThreshold = CHARACTER_XP_THRESHOLDS[currentLevel]?.xpRequired || 0;
  
  if (nextThreshold === currentThreshold) return 100;
  return Math.min(100, Math.floor(((xp - currentThreshold) / (nextThreshold - currentThreshold)) * 100));
}

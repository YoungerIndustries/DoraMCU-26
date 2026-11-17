import { getAbilityColor, getAbilityModifier, getCharacterLevel, getXpForNextLevel, getLevelProgress } from './traveller-character';

describe('TravellerCharacter Utilities', () => {
  describe('getAbilityColor', () => {
    it('should return green for excellent abilities (14+)', () => {
      expect(getAbilityColor(14)).toBe('#4caf50');
      expect(getAbilityColor(20)).toBe('#4caf50');
    });

    it('should return blue for above average abilities (10-13)', () => {
      expect(getAbilityColor(10)).toBe('#2196f3');
      expect(getAbilityColor(13)).toBe('#2196f3');
    });

    it('should return orange for average abilities (7-9)', () => {
      expect(getAbilityColor(7)).toBe('#ff9800');
      expect(getAbilityColor(9)).toBe('#ff9800');
    });

    it('should return red for below average abilities (1-6)', () => {
      expect(getAbilityColor(1)).toBe('#f44336');
      expect(getAbilityColor(6)).toBe('#f44336');
    });
  });

  describe('getAbilityModifier', () => {
    it('should calculate modifier for score 10', () => {
      expect(getAbilityModifier(10)).toBe(0);
    });

    it('should calculate modifier for score 14', () => {
      expect(getAbilityModifier(14)).toBe(2);
    });

    it('should calculate modifier for score 8', () => {
      expect(getAbilityModifier(8)).toBe(-1);
    });

    it('should calculate modifier for score 1', () => {
      expect(getAbilityModifier(1)).toBe(-4);
    });

    it('should calculate modifier for score 20', () => {
      expect(getAbilityModifier(20)).toBe(5);
    });

    it('should floor the result for odd differences', () => {
      expect(getAbilityModifier(11)).toBe(0); // (11-10)/2 = 0.5, floored to 0
      expect(getAbilityModifier(9)).toBe(-1); // (9-10)/2 = -0.5, floored to -1
    });
  });

  describe('getCharacterLevel', () => {
    it('should return level 1 for 0 XP', () => {
      expect(getCharacterLevel(0)).toBe(1);
    });

    it('should return level 2 for 100 XP', () => {
      expect(getCharacterLevel(100)).toBe(2);
    });

    it('should return level 3 for 300 XP', () => {
      expect(getCharacterLevel(300)).toBe(3);
    });

    it('should return level 5 for 1000 XP', () => {
      expect(getCharacterLevel(1000)).toBe(5);
    });

    it('should return level 10 for max XP', () => {
      expect(getCharacterLevel(4500)).toBe(10);
      expect(getCharacterLevel(10000)).toBe(10);
    });

    it('should return correct level for XP between thresholds', () => {
      expect(getCharacterLevel(99)).toBe(1);
      expect(getCharacterLevel(299)).toBe(2);
      expect(getCharacterLevel(599)).toBe(3);
    });
  });

  describe('getXpForNextLevel', () => {
    it('should return XP needed for level 1 to 2', () => {
      expect(getXpForNextLevel(0)).toBe(100);
    });

    it('should return XP needed for level 2 to 3', () => {
      expect(getXpForNextLevel(100)).toBe(200);
    });

    it('should return XP needed for level 5 to 6', () => {
      expect(getXpForNextLevel(1000)).toBe(500);
    });

    it('should return 0 for max level', () => {
      expect(getXpForNextLevel(4500)).toBe(0);
      expect(getXpForNextLevel(10000)).toBe(0);
    });

    it('should return correct XP for mid-level', () => {
      expect(getXpForNextLevel(500)).toBe(100); // level 2, needs 300, has 500 → 200
    });
  });

  describe('getLevelProgress', () => {
    it('should return 0% at level threshold', () => {
      expect(getLevelProgress(0)).toBe(0);
    });

    it('should return 100% at max level', () => {
      expect(getLevelProgress(4500)).toBe(100);
      expect(getLevelProgress(10000)).toBe(100);
    });

    it('should return 50% at midpoint', () => {
      // Level 1: 0-100 XP, midpoint is 50
      expect(getLevelProgress(50)).toBe(50);
    });

    it('should return correct progress between levels', () => {
      // Level 2: 100-300 XP, at 200 XP should be 50%
      expect(getLevelProgress(200)).toBe(50);
    });

    it('should cap at 100%', () => {
      expect(getLevelProgress(99)).toBeLessThanOrEqual(100);
    });
  });
});

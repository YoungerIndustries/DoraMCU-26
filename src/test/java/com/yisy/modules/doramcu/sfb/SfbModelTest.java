package com.yisy.modules.doramcu.sfb;

import com.yisy.modules.doramcu.sfb.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SfbShield Tests")
class SfbShieldTest {

    @Test
    @DisplayName("Constructor initializes shield correctly")
    void constructorInitializesShield() {
        SfbShield shield = new SfbShield(0, 5);
        assertEquals(0, shield.getDirection());
        assertEquals(5, shield.getTotalBoxes());
        assertEquals(5, shield.getBoxesRemaining());
        assertEquals(0, shield.getReinforcementPoints());
    }

    @Test
    @DisplayName("Absorb damage uses reinforcement first")
    void absorbDamageUsesReinforcementFirst() {
        SfbShield shield = new SfbShield(0, 3);
        shield.reinforce(2);
        int penetrated = shield.absorbDamage(5);
        assertEquals(0, shield.getReinforcementPoints());
        assertEquals(0, shield.getBoxesRemaining()); // 3 boxes - 3 damage from boxes = 0
        assertEquals(0, penetrated); // 5 damage - 2 reinforcement - 3 boxes = 0 penetrated
    }

    @Test
    @DisplayName("Absorb damage when shield is full")
    void absorbDamageWhenFull() {
        SfbShield shield = new SfbShield(0, 5);
        int penetrated = shield.absorbDamage(3);
        assertEquals(2, shield.getBoxesRemaining());
        assertEquals(0, penetrated);
    }

    @Test
    @DisplayName("Absorb damage when shield is depleted")
    void absorbDamageWhenDepleted() {
        SfbShield shield = new SfbShield(0, 2);
        int penetrated = shield.absorbDamage(5);
        assertEquals(0, shield.getBoxesRemaining());
        assertEquals(3, penetrated);
    }

    @Test
    @DisplayName("Reinforce adds points")
    void reinforceAddsPoints() {
        SfbShield shield = new SfbShield(0, 3);
        shield.reinforce(4);
        assertEquals(4, shield.getReinforcementPoints());
        assertEquals(7, shield.getTotalProtection());
    }

    @Test
    @DisplayName("Repair restores boxes")
    void repairRestoresBoxes() {
        SfbShield shield = new SfbShield(0, 5);
        shield.absorbDamage(3);
        shield.repair(2);
        assertEquals(4, shield.getBoxesRemaining());
    }

    @Test
    @DisplayName("Repair does not exceed total boxes")
    void repairDoesNotExceedTotal() {
        SfbShield shield = new SfbShield(0, 5);
        shield.repair(3);
        assertEquals(5, shield.getBoxesRemaining());
    }

    @Test
    @DisplayName("Is down when no boxes or reinforcement")
    void isDownWhenNoProtection() {
        SfbShield shield = new SfbShield(0, 2);
        shield.absorbDamage(2);
        assertTrue(shield.isDown());
    }

    @Test
    @DisplayName("Not down when reinforcement remains")
    void notDownWhenReinforcementRemains() {
        SfbShield shield = new SfbShield(0, 2);
        shield.reinforce(3);
        shield.absorbDamage(2);
        assertFalse(shield.isDown());
    }
}

@DisplayName("SfbWeaponBank Tests")
class SfbWeaponBankTest {

    @Test
    @DisplayName("Constructor initializes weapon bank")
    void constructorInitializesWeaponBank() {
        SfbWeaponBank bank = new SfbWeaponBank("P1", SfbWeaponBank.WeaponType.PHASER_1,
                SfbWeaponBank.FiringArc.F, 1);
        assertEquals("P1", bank.getId());
        assertEquals(SfbWeaponBank.WeaponType.PHASER_1, bank.getWeaponType());
        assertEquals(SfbWeaponBank.FiringArc.F, bank.getFiringArc());
        assertEquals(1, bank.getEnergyCost());
        assertTrue(bank.isOperational());
    }

    @Test
    @DisplayName("Can fire at target in forward arc")
    void canFireAtForwardTarget() {
        SfbWeaponBank bank = new SfbWeaponBank("P1", SfbWeaponBank.WeaponType.PHASER_1,
                SfbWeaponBank.FiringArc.F, 1);
        assertTrue(bank.canFireAt(0, 0)); // facing 0, target direction 0
        assertTrue(bank.canFireAt(0, 1));
        assertTrue(bank.canFireAt(0, 5));
    }

    @Test
    @DisplayName("Cannot fire at target outside arc")
    void cannotFireAtTargetOutsideArc() {
        SfbWeaponBank bank = new SfbWeaponBank("P1", SfbWeaponBank.WeaponType.PHASER_1,
                SfbWeaponBank.FiringArc.F, 1);
        assertFalse(bank.canFireAt(0, 3)); // facing 0, target direction 3 (behind)
    }

    @Test
    @DisplayName("Cannot fire when not operational")
    void cannotFireWhenNotOperational() {
        SfbWeaponBank bank = new SfbWeaponBank("P1", SfbWeaponBank.WeaponType.PHASER_1,
                SfbWeaponBank.FiringArc.A, 1);
        bank.setOperational(false);
        assertFalse(bank.canFireAt(0, 0));
    }

    @Test
    @DisplayName("Fire reduces ammo for projectile weapons")
    void fireReducesAmmo() {
        SfbWeaponBank bank = new SfbWeaponBank("T1", SfbWeaponBank.WeaponType.PHOTON_TORPEDO,
                SfbWeaponBank.FiringArc.F, 2);
        bank.setAmmo(5);
        assertTrue(bank.fire());
        assertEquals(4, bank.getAmmo());
    }

    @Test
    @DisplayName("Fire does not affect energy weapons ammo")
    void fireDoesNotAffectEnergyWeapons() {
        SfbWeaponBank bank = new SfbWeaponBank("P1", SfbWeaponBank.WeaponType.PHASER_1,
                SfbWeaponBank.FiringArc.F, 1);
        bank.setAmmo(0); // unlimited
        assertTrue(bank.fire());
        assertEquals(0, bank.getAmmo());
    }

    @Test
    @DisplayName("Base damage varies by range band")
    void baseDamageVariesByRange() {
        SfbWeaponBank bank = new SfbWeaponBank("P1", SfbWeaponBank.WeaponType.PHASER_1,
                SfbWeaponBank.FiringArc.F, 1);
        assertEquals(5, bank.getBaseDamage(0));
        assertEquals(5, bank.getBaseDamage(2));
        assertEquals(4, bank.getBaseDamage(3));
        assertEquals(1, bank.getBaseDamage(6));
    }

    @Test
    @DisplayName("Hit probability varies by range band")
    void hitProbabilityVariesByRange() {
        SfbWeaponBank bank = new SfbWeaponBank("P1", SfbWeaponBank.WeaponType.PHASER_1,
                SfbWeaponBank.FiringArc.F, 1);
        assertEquals(6, bank.getHitProbability(0));
        assertEquals(5, bank.getHitProbability(3));
        assertEquals(2, bank.getHitProbability(6));
    }
}

@DisplayName("SfbSSD Tests")
class SfbSSDTest {

    @Test
    @DisplayName("Add and retrieve system boxes")
    void addAndRetrieveSystemBoxes() {
        SfbSSD ssd = new SfbSSD();
        ssd.addSystem("WARP_1", SfbSSD.SystemCategory.WARP_ENGINES);
        ssd.addSystem("WARP_2", SfbSSD.SystemCategory.WARP_ENGINES);
        assertEquals(2, ssd.getAvailableWarpPower());
    }

    @Test
    @DisplayName("Mark out box reduces available power")
    void markOutBoxReducesPower() {
        SfbSSD ssd = new SfbSSD();
        ssd.addSystem("WARP_1", SfbSSD.SystemCategory.WARP_ENGINES);
        ssd.addSystem("WARP_2", SfbSSD.SystemCategory.WARP_ENGINES);
        ssd.markOutBox("WARP_1");
        assertEquals(1, ssd.getAvailableWarpPower());
    }

    @Test
    @DisplayName("Repair box restores power")
    void repairBoxRestoresPower() {
        SfbSSD ssd = new SfbSSD();
        ssd.addSystem("WARP_1", SfbSSD.SystemCategory.WARP_ENGINES);
        ssd.markOutBox("WARP_1");
        ssd.repairBox("WARP_1");
        assertEquals(1, ssd.getAvailableWarpPower());
    }

    @Test
    @DisplayName("Crippled when all control spaces marked out")
    void crippledWhenAllControlSpacesOut() {
        SfbSSD ssd = new SfbSSD();
        ssd.addSystem("CTRL_1", SfbSSD.SystemCategory.CONTROL_SPACES);
        ssd.addSystem("CTRL_2", SfbSSD.SystemCategory.CONTROL_SPACES);
        ssd.markOutBox("CTRL_1");
        ssd.markOutBox("CTRL_2");
        assertTrue(ssd.isCrippled());
    }

    @Test
    @DisplayName("Not crippled when control space remains")
    void notCrippledWhenControlSpaceRemains() {
        SfbSSD ssd = new SfbSSD();
        ssd.addSystem("CTRL_1", SfbSSD.SystemCategory.CONTROL_SPACES);
        ssd.addSystem("CTRL_2", SfbSSD.SystemCategory.CONTROL_SPACES);
        ssd.markOutBox("CTRL_1");
        assertFalse(ssd.isCrippled());
    }

    @Test
    @DisplayName("Destroyed when excess damage reaches hull")
    void destroyedWhenExcessDamageReachesHull() {
        SfbSSD ssd = new SfbSSD();
        ssd.setHullBoxes(5);
        ssd.addExcessDamage(5);
        assertTrue(ssd.isDestroyed());
    }

    @Test
    @DisplayName("Not destroyed when excess damage below hull")
    void notDestroyedWhenExcessDamageBelowHull() {
        SfbSSD ssd = new SfbSSD();
        ssd.setHullBoxes(5);
        ssd.addExcessDamage(3);
        assertFalse(ssd.isDestroyed());
    }

    @Test
    @DisplayName("Get operational weapons")
    void getOperationalWeapons() {
        SfbSSD ssd = new SfbSSD();
        ssd.addSystem("WEAPON_1", SfbSSD.SystemCategory.WEAPONS);
        ssd.addSystem("WEAPON_2", SfbSSD.SystemCategory.WEAPONS);
        ssd.markOutBox("WEAPON_1");
        assertEquals(1, ssd.getOperationalWeapons().size());
    }
}

@DisplayName("SfbEnergyAllocation Tests")
class SfbEnergyAllocationTest {

    @Test
    @DisplayName("Calculate total available power")
    void calculateTotalAvailablePower() {
        SfbEnergyAllocation ea = new SfbEnergyAllocation();
        ea.setWarpPower(10);
        ea.setImpulsePower(5);
        ea.setAuxiliaryPower(3);
        assertEquals(18, ea.getTotalAvailablePower());
    }

    @Test
    @DisplayName("Calculate total allocated power")
    void calculateTotalAllocatedPower() {
        SfbEnergyAllocation ea = new SfbEnergyAllocation();
        ea.setShieldAllocation(5);
        ea.setWeaponAllocation(3);
        ea.setShieldReinforcement(0, 2);
        assertEquals(10, ea.getTotalAllocatedPower());
    }

    @Test
    @DisplayName("Validate passes when allocated <= available")
    void validatePasses() {
        SfbEnergyAllocation ea = new SfbEnergyAllocation();
        ea.setWarpPower(10);
        ea.setShieldAllocation(5);
        assertTrue(ea.validate());
    }

    @Test
    @DisplayName("Validate fails when allocated > available")
    void validateFails() {
        SfbEnergyAllocation ea = new SfbEnergyAllocation();
        ea.setWarpPower(5);
        ea.setShieldAllocation(10);
        assertFalse(ea.validate());
    }

    @Test
    @DisplayName("Reserve power is available minus allocated")
    void reservePower() {
        SfbEnergyAllocation ea = new SfbEnergyAllocation();
        ea.setWarpPower(10);
        ea.setShieldAllocation(5);
        assertEquals(5, ea.getReservePower());
    }

    @Test
    @DisplayName("Reset allocation clears allocations")
    void resetAllocation() {
        SfbEnergyAllocation ea = new SfbEnergyAllocation();
        ea.setWarpPower(10);
        ea.setShieldAllocation(5);
        ea.resetAllocation();
        assertEquals(10, ea.getWarpPower());
        assertEquals(0, ea.getShieldAllocation());
    }
}

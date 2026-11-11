package com.yisy.modules.doramcu.hex.planet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmpireTradePolicyTest {

    private EmpireTradePolicy policy;

    @BeforeEach
    void setUp() {
        policy = new EmpireTradePolicy();
    }

    @Test
    void constructor_defaultValues() {
        // Default tariff is 10%
        assertEquals(10, policy.getTariffPercentage());
        assertTrue(policy.getEmbargoedCargo().isEmpty());
        assertTrue(policy.getFreeTradePlanets().isEmpty());
        assertFalse(policy.isWarZone());
    }

    @Test
    void setTariffPercentage_setsValue() {
        policy.setTariffPercentage(25);
        assertEquals(25, policy.getTariffPercentage());
    }

    @Test
    void setTariffPercentage_clampsToMax() {
        policy.setTariffPercentage(60);
        assertEquals(50, policy.getTariffPercentage());
    }

    @Test
    void setTariffPercentage_clampsToMin() {
        policy.setTariffPercentage(-10);
        assertEquals(0, policy.getTariffPercentage());
    }

    @Test
    void isCargoAllowed_notEmbargoed() {
        assertTrue(policy.isCargoAllowed(CargoType.GRAIN));
    }

    @Test
    void addFreeTradePlanet_addsToSet() {
        policy.addFreeTradePlanet("planet100");
        assertTrue(policy.getFreeTradePlanets().contains("planet100"));
    }

    @Test
    void setWarZone_setsFlag() {
        policy.setWarZone(true);
        assertTrue(policy.isWarZone());
        policy.setWarZone(false);
        assertFalse(policy.isWarZone());
    }

    @Test
    void getRouteRiskMultiplier_warZone() {
        policy.setWarZone(true);
        assertTrue(policy.getRouteRiskMultiplier() > 1.0);
    }

    @Test
    void getRouteRiskMultiplier_peaceTime() {
        policy.setWarZone(false);
        assertEquals(1.0, policy.getRouteRiskMultiplier(), 0.001);
    }

    @Test
    void calculateTariff_basic() {
        policy.setTariffPercentage(20);
        double tariff = policy.calculateTariff(CargoType.GRAIN, "planet100", 50);
        // Tariff should be a reasonable percentage value
        assertTrue(tariff >= 0);
    }

    @Test
    void calculateTariff_freeTradePlanet() {
        policy.setTariffPercentage(20);
        policy.addFreeTradePlanet("planet100");
        double normalTariff = policy.calculateTariff(CargoType.GRAIN, "planet999", 50);
        double freeTradeTariff = policy.calculateTariff(CargoType.GRAIN, "planet100", 50);
        assertTrue(freeTradeTariff <= normalTariff);
    }

    @Test
    void calculateTariff_withReputationDiscount() {
        policy.setTariffPercentage(20);
        double lowRep = policy.calculateTariff(CargoType.GRAIN, "planet100", 10);
        double highRep = policy.calculateTariff(CargoType.GRAIN, "planet100", 80);
        assertTrue(highRep <= lowRep);
    }

    @Test
    void setFreeTradePlanets_replacesAll() {
        policy.addFreeTradePlanet("planet100");
        policy.addFreeTradePlanet("planet200");
        var set = new java.util.HashSet<String>();
        set.add("planet300");
        policy.setFreeTradePlanets(set);
        assertEquals(1, policy.getFreeTradePlanets().size());
        assertTrue(policy.getFreeTradePlanets().contains("planet300"));
    }

}

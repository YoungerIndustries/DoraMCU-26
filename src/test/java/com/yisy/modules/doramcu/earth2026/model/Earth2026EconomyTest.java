package com.yisy.modules.doramcu.earth2026.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Earth2026Economy Tests")
class Earth2026EconomyTest {

    @Nested
    @DisplayName("Constructors")
    class ConstructorTests {

        @Test
        @DisplayName("no-arg constructor should create empty economy")
        void noArgConstructor() {
            Earth2026Economy economy = new Earth2026Economy();
            assertNull(economy.getId());
            assertNull(economy.getPlanetId());
            assertTrue(economy.isActive());
        }

        @Test
        @DisplayName("full constructor should set planet ID and default values")
        void fullConstructor() {
            Earth2026Economy economy = new Earth2026Economy(1L);
            assertEquals(1L, economy.getPlanetId());
            assertEquals(100.0, economy.getGdpTrillions());
            assertEquals(2.0, economy.getGrowthRate());
            assertEquals(2.5, economy.getInflationRate());
            assertEquals(5.0, economy.getUnemploymentRate());
            assertEquals(5, economy.getMarketSize());
            assertEquals(5, economy.getTradeOpenness());
            assertEquals(5, economy.getTechnologyIndex());
            assertEquals(5, economy.getResourceAbundance());
            assertEquals(5, economy.getFinancialStability());
            assertEquals(1.0, economy.getPriceLevel());
            assertEquals("stable", economy.getMarketCondition());
            assertFalse(economy.isTradeRestricted());
            assertTrue(economy.isActive());
        }
    }

    @Nested
    @DisplayName("Getters and Setters")
    class GetterSetterTests {

        @Test
        @DisplayName("should set and get all economic indicators")
        void economicIndicators() {
            Earth2026Economy economy = new Earth2026Economy();
            economy.setId(1L);
            economy.setPlanetId(1L);
            economy.setGdpTrillions(105.0);
            economy.setGrowthRate(3.0);
            economy.setInflationRate(2.0);
            economy.setUnemploymentRate(4.0);

            assertEquals(1L, economy.getId());
            assertEquals(1L, economy.getPlanetId());
            assertEquals(105.0, economy.getGdpTrillions());
            assertEquals(3.0, economy.getGrowthRate());
            assertEquals(2.0, economy.getInflationRate());
            assertEquals(4.0, economy.getUnemploymentRate());
        }

        @Test
        @DisplayName("should set and get market indices")
        void marketIndices() {
            Earth2026Economy economy = new Earth2026Economy();
            economy.setMarketSize(8);
            economy.setTradeOpenness(7);
            economy.setTechnologyIndex(9);
            economy.setResourceAbundance(6);
            economy.setFinancialStability(8);
            economy.setPriceLevel(1.2);

            assertEquals(8, economy.getMarketSize());
            assertEquals(7, economy.getTradeOpenness());
            assertEquals(9, economy.getTechnologyIndex());
            assertEquals(6, economy.getResourceAbundance());
            assertEquals(8, economy.getFinancialStability());
            assertEquals(1.2, economy.getPriceLevel());
        }

        @Test
        @DisplayName("should set and get market condition")
        void marketCondition() {
            Earth2026Economy economy = new Earth2026Economy();
            economy.setMarketCondition("booming");
            assertEquals("booming", economy.getMarketCondition());
            economy.setMarketCondition("recession");
            assertEquals("recession", economy.getMarketCondition());
        }

        @Test
        @DisplayName("should set and get trade data")
        void tradeData() {
            Earth2026Economy economy = new Earth2026Economy();
            economy.setDominantIndustries("Technology,Finance,Manufacturing");
            economy.setExportCommodities("Electronics,Software,Automobiles");
            economy.setImportCommodities("Oil,Rare Earth Metals,Textiles");
            economy.setTradeRestricted(true);

            assertEquals("Technology,Finance,Manufacturing", economy.getDominantIndustries());
            assertEquals("Electronics,Software,Automobiles", economy.getExportCommodities());
            assertEquals("Oil,Rare Earth Metals,Textiles", economy.getImportCommodities());
            assertTrue(economy.isTradeRestricted());
        }

        @Test
        @DisplayName("should set and get active status")
        void activeStatus() {
            Earth2026Economy economy = new Earth2026Economy();
            assertTrue(economy.isActive());
            economy.setActive(false);
            assertFalse(economy.isActive());
        }

        @Test
        @DisplayName("should set and get timestamps")
        void timestamps() {
            Earth2026Economy economy = new Earth2026Economy();
            LocalDateTime now = LocalDateTime.now();
            economy.setCreatedAt(now);
            economy.setUpdatedAt(now);
            assertEquals(now, economy.getCreatedAt());
            assertEquals(now, economy.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("Health Score")
    class HealthScoreTests {

        @Test
        @DisplayName("should calculate health score with default values")
        void defaultHealthScore() {
            Earth2026Economy economy = new Earth2026Economy(1L);
            // growthRate=2.0, financialStability=5, unemploymentRate=5.0, marketSize=5
            // growthScore = min(10, max(1, (2.0+5)*1.0)) = 7.0
            // stabilityScore = 5
            // employmentScore = max(1, 10-5.0) = 5.0
            // health = 7.0*0.25 + 5*0.3 + 5.0*0.25 + 5*0.2 = 1.75 + 1.5 + 1.25 + 1.0 = 5.5
            assertEquals(5.5, economy.getHealthScore(), 0.01);
        }

        @Test
        @DisplayName("should calculate high health score")
        void highHealthScore() {
            Earth2026Economy economy = new Earth2026Economy();
            economy.setGrowthRate(5.0);
            economy.setFinancialStability(10);
            economy.setUnemploymentRate(1.0);
            economy.setMarketSize(10);
            // growthScore = min(10, max(1, (5.0+5)*1.0)) = 10.0
            // stabilityScore = 10
            // employmentScore = max(1, 10-1.0) = 9.0
            // health = 10*0.25 + 10*0.3 + 9.0*0.25 + 10*0.2 = 2.5 + 3.0 + 2.25 + 2.0 = 9.75
            assertEquals(9.75, economy.getHealthScore(), 0.01);
        }

        @Test
        @DisplayName("should calculate low health score")
        void lowHealthScore() {
            Earth2026Economy economy = new Earth2026Economy();
            economy.setGrowthRate(-5.0);
            economy.setFinancialStability(1);
            economy.setUnemploymentRate(10.0);
            economy.setMarketSize(1);
            // growthScore = min(10, max(1, (-5.0+5)*1.0)) = 1.0
            // stabilityScore = 1
            // employmentScore = max(1, 10-10.0) = 1.0 (clamped to 1)
            // health = 1*0.25 + 1*0.3 + 1.0*0.25 + 1*0.2 = 0.25 + 0.3 + 0.25 + 0.2 = 1.0
            assertEquals(1.0, economy.getHealthScore(), 0.01);
        }

        @Test
        @DisplayName("should clamp growth score to max 10")
        void growthScoreClamped() {
            Earth2026Economy economy = new Earth2026Economy();
            economy.setGrowthRate(20.0);
            economy.setFinancialStability(10);
            economy.setUnemploymentRate(0.0);
            economy.setMarketSize(10);
            // growthScore = min(10, max(1, (20.0+5)*1.0)) = 10.0 (clamped)
            // stabilityScore = 10
            // employmentScore = max(1, 10-0.0) = 10.0
            // health = 10*0.25 + 10*0.3 + 10.0*0.25 + 10*0.2 = 10.0
            assertEquals(10.0, economy.getHealthScore(), 0.01);
        }
    }

    @Nested
    @DisplayName("Market Conditions")
    class MarketConditionTests {

        @Test
        @DisplayName("should support booming condition")
        void booming() {
            Earth2026Economy economy = new Earth2026Economy();
            economy.setMarketCondition("booming");
            assertEquals("booming", economy.getMarketCondition());
        }

        @Test
        @DisplayName("should support stable condition")
        void stable() {
            Earth2026Economy economy = new Earth2026Economy();
            economy.setMarketCondition("stable");
            assertEquals("stable", economy.getMarketCondition());
        }

        @Test
        @DisplayName("should support declining condition")
        void declining() {
            Earth2026Economy economy = new Earth2026Economy();
            economy.setMarketCondition("declining");
            assertEquals("declining", economy.getMarketCondition());
        }

        @Test
        @DisplayName("should support recession condition")
        void recession() {
            Earth2026Economy economy = new Earth2026Economy();
            economy.setMarketCondition("recession");
            assertEquals("recession", economy.getMarketCondition());
        }

        @Test
        @DisplayName("should support depression condition")
        void depression() {
            Earth2026Economy economy = new Earth2026Economy();
            economy.setMarketCondition("depression");
            assertEquals("depression", economy.getMarketCondition());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("toString should include key fields")
        void toStringContent() {
            Earth2026Economy economy = new Earth2026Economy(1L);
            economy.setId(1L);
            economy.setGdpTrillions(105.0);
            economy.setGrowthRate(3.0);
            economy.setMarketCondition("booming");

            String str = economy.toString();
            assertTrue(str.contains("id=1"));
            assertTrue(str.contains("planetId=1"));
            assertTrue(str.contains("gdp=105.0T"));
            assertTrue(str.contains("growth=3.0%"));
            assertTrue(str.contains("market='booming'"));
            assertTrue(str.contains("health="));
        }
    }
}

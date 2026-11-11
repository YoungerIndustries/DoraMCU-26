package com.yisy.modules.doramcu.earth2026.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Earth2026Job Tests")
class Earth2026JobTest {

    @Nested
    @DisplayName("Constructors")
    class ConstructorTests {

        @Test
        @DisplayName("no-arg constructor should create empty job")
        void noArgConstructor() {
            Earth2026Job job = new Earth2026Job();
            assertNull(job.getId());
            assertNull(job.getTitle());
            assertNull(job.getCategory());
            assertTrue(job.isActive());
        }

        @Test
        @DisplayName("full constructor should set title, category, level, and planet ID")
        void fullConstructor() {
            Earth2026Job job = new Earth2026Job("Software Engineer", "Technology", 5, 1L);
            assertEquals("Software Engineer", job.getTitle());
            assertEquals("Technology", job.getCategory());
            assertEquals(5, job.getJobLevel());
            assertEquals(1L, job.getPlanetId());
            assertEquals(30000, job.getMinSalary());
            assertEquals(80000, job.getMaxSalary());
            assertEquals(1, job.getMinCharLevel());
            assertEquals(10, job.getXpPerTurn());
            assertTrue(job.isActive());
        }
    }

    @Nested
    @DisplayName("Getters and Setters")
    class GetterSetterTests {

        @Test
        @DisplayName("should set and get all basic fields")
        void basicFields() {
            Earth2026Job job = new Earth2026Job();
            job.setId(1L);
            job.setTitle("CEO");
            job.setCategory("Government");
            job.setJobLevel(10);
            job.setDescription("Chief Executive Officer");

            assertEquals(1L, job.getId());
            assertEquals("CEO", job.getTitle());
            assertEquals("Government", job.getCategory());
            assertEquals(10, job.getJobLevel());
            assertEquals("Chief Executive Officer", job.getDescription());
        }

        @Test
        @DisplayName("should set and get salary range")
        void salaryRange() {
            Earth2026Job job = new Earth2026Job();
            job.setMinSalary(100000);
            job.setMaxSalary(500000);

            assertEquals(100000, job.getMinSalary());
            assertEquals(500000, job.getMaxSalary());
        }

        @Test
        @DisplayName("should set and get skill requirements")
        void skillRequirements() {
            Earth2026Job job = new Earth2026Job();
            job.setMinCharLevel(5);
            job.setMinInt(14);
            job.setMinStr(8);
            job.setMinCha(12);

            assertEquals(5, job.getMinCharLevel());
            assertEquals(14, job.getMinInt());
            assertEquals(8, job.getMinStr());
            assertEquals(12, job.getMinCha());
        }

        @Test
        @DisplayName("should set and get XP per turn")
        void xpPerTurn() {
            Earth2026Job job = new Earth2026Job();
            job.setXpPerTurn(50);
            assertEquals(50, job.getXpPerTurn());
        }

        @Test
        @DisplayName("should set and get location fields")
        void locationFields() {
            Earth2026Job job = new Earth2026Job();
            job.setPlanetId(1L);
            job.setCountryId(1L);

            assertEquals(1L, job.getPlanetId());
            assertEquals(1L, job.getCountryId());
        }

        @Test
        @DisplayName("should set and get benefits")
        void benefits() {
            Earth2026Job job = new Earth2026Job();
            job.setBenefits("Health insurance, 401k, stock options");
            assertEquals("Health insurance, 401k, stock options", job.getBenefits());
        }

        @Test
        @DisplayName("should set and get active status")
        void activeStatus() {
            Earth2026Job job = new Earth2026Job();
            assertTrue(job.isActive());
            job.setActive(false);
            assertFalse(job.isActive());
        }

        @Test
        @DisplayName("should set and get timestamps")
        void timestamps() {
            Earth2026Job job = new Earth2026Job();
            LocalDateTime now = LocalDateTime.now();
            job.setCreatedAt(now);
            assertEquals(now, job.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("Average Salary")
    class AverageSalaryTests {

        @Test
        @DisplayName("should calculate average salary")
        void averageSalary() {
            Earth2026Job job = new Earth2026Job();
            job.setMinSalary(50000);
            job.setMaxSalary(150000);
            assertEquals(100000, job.getAverageSalary());
        }

        @Test
        @DisplayName("should calculate average salary with odd numbers")
        void averageSalaryOdd() {
            Earth2026Job job = new Earth2026Job();
            job.setMinSalary(50000);
            job.setMaxSalary(150001);
            assertEquals(100000, job.getAverageSalary());
        }

        @Test
        @DisplayName("should calculate average salary with equal min and max")
        void averageSalaryEqual() {
            Earth2026Job job = new Earth2026Job();
            job.setMinSalary(75000);
            job.setMaxSalary(75000);
            assertEquals(75000, job.getAverageSalary());
        }
    }

    @Nested
    @DisplayName("Job Categories")
    class CategoryTests {

        @Test
        @DisplayName("should support Technology category")
        void technology() {
            Earth2026Job job = new Earth2026Job("Software Engineer", "Technology", 5, 1L);
            assertEquals("Technology", job.getCategory());
        }

        @Test
        @DisplayName("should support Government category")
        void government() {
            Earth2026Job job = new Earth2026Job("Senator", "Government", 8, 1L);
            assertEquals("Government", job.getCategory());
        }

        @Test
        @DisplayName("should support Military category")
        void military() {
            Earth2026Job job = new Earth2026Job("General", "Military", 10, 1L);
            assertEquals("Military", job.getCategory());
        }

        @Test
        @DisplayName("should support Finance category")
        void finance() {
            Earth2026Job job = new Earth2026Job("Investment Banker", "Finance", 7, 1L);
            assertEquals("Finance", job.getCategory());
        }

        @Test
        @DisplayName("should support Healthcare category")
        void healthcare() {
            Earth2026Job job = new Earth2026Job("Surgeon", "Healthcare", 8, 1L);
            assertEquals("Healthcare", job.getCategory());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("toString should include key fields")
        void toStringContent() {
            Earth2026Job job = new Earth2026Job("Software Engineer", "Technology", 5, 1L);
            job.setId(1L);
            job.setMinSalary(80000);
            job.setMaxSalary(160000);

            String str = job.toString();
            assertTrue(str.contains("id=1"));
            assertTrue(str.contains("title='Software Engineer'"));
            assertTrue(str.contains("category='Technology'"));
            assertTrue(str.contains("level=5"));
            assertTrue(str.contains("salary=80000-160000"));
        }
    }
}

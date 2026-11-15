package com.yisy.modules.doramcu.fps.core.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

class FpsEnemyTest {

    @Nested
    class ConstructorTests {
        @Test
        void defaultConstructorInitializesNulls() {
            FpsEnemy enemy = new FpsEnemy();
            assertNull(enemy.getId());
            assertNull(enemy.getName());
            assertNull(enemy.getBehavior());
            assertEquals(0, enemy.getHealth());
            assertEquals(0, enemy.getDamage());
        }

        @Test
        void fullConstructorGeneratesUuid() {
            FpsEnemy enemy = new FpsEnemy("Soldier", 100, 3.0, FpsEnemy.EnemyBehavior.PATROL, 20);
            assertNotNull(enemy.getId());
            assertEquals("Soldier", enemy.getName());
            assertEquals(100, enemy.getHealth());
            assertEquals(3.0, enemy.getSpeed());
            assertEquals(FpsEnemy.EnemyBehavior.PATROL, enemy.getBehavior());
            assertEquals(20, enemy.getDamage());
        }

        @Test
        void fullConstructorSetsDefaults() {
            FpsEnemy enemy = new FpsEnemy("Soldier", 100, 3.0, FpsEnemy.EnemyBehavior.PATROL, 20);
            assertEquals(50.0, enemy.getAttackRange());
            assertEquals(1.0, enemy.getAttackCooldown());
            assertEquals(0.3, enemy.getDropChance());
            assertEquals(100, enemy.getExperienceReward());
        }
    }

    @Nested
    class EnemyBehaviorTests {
        @Test
        void allBehaviorsExist() {
            assertSame(FpsEnemy.EnemyBehavior.PATROL, FpsEnemy.EnemyBehavior.valueOf("PATROL"));
            assertSame(FpsEnemy.EnemyBehavior.CHASE, FpsEnemy.EnemyBehavior.valueOf("CHASE"));
            assertSame(FpsEnemy.EnemyBehavior.ATTACK, FpsEnemy.EnemyBehavior.valueOf("ATTACK"));
            assertSame(FpsEnemy.EnemyBehavior.FLANK, FpsEnemy.EnemyBehavior.valueOf("FLANK"));
            assertSame(FpsEnemy.EnemyBehavior.RETREAT, FpsEnemy.EnemyBehavior.valueOf("RETREAT"));
            assertSame(FpsEnemy.EnemyBehavior.AMBUSH, FpsEnemy.EnemyBehavior.valueOf("AMBUSH"));
        }

        @Test
        void eachBehaviorCanBeAssigned() {
            FpsEnemy enemy = new FpsEnemy();
            for (FpsEnemy.EnemyBehavior behavior : FpsEnemy.EnemyBehavior.values()) {
                enemy.setBehavior(behavior);
                assertEquals(behavior, enemy.getBehavior());
            }
        }
    }

    @Nested
    class GetterSetterTests {
        @Test
        void idCanBeSet() {
            FpsEnemy enemy = new FpsEnemy();
            enemy.setId("custom-enemy");
            assertEquals("custom-enemy", enemy.getId());
        }

        @Test
        void nameCanBeSet() {
            FpsEnemy enemy = new FpsEnemy();
            enemy.setName("Elite Guard");
            assertEquals("Elite Guard", enemy.getName());
        }

        @Test
        void healthCanBeSet() {
            FpsEnemy enemy = new FpsEnemy();
            enemy.setHealth(200);
            assertEquals(200, enemy.getHealth());
        }

        @Test
        void speedCanBeSet() {
            FpsEnemy enemy = new FpsEnemy();
            enemy.setSpeed(5.0);
            assertEquals(5.0, enemy.getSpeed());
        }

        @Test
        void damageCanBeSet() {
            FpsEnemy enemy = new FpsEnemy();
            enemy.setDamage(50);
            assertEquals(50, enemy.getDamage());
        }

        @Test
        void attackRangeCanBeSet() {
            FpsEnemy enemy = new FpsEnemy();
            enemy.setAttackRange(100.0);
            assertEquals(100.0, enemy.getAttackRange());
        }

        @Test
        void attackCooldownCanBeSet() {
            FpsEnemy enemy = new FpsEnemy();
            enemy.setAttackCooldown(0.5);
            assertEquals(0.5, enemy.getAttackCooldown());
        }

        @Test
        void dropItemCanBeSet() {
            FpsEnemy enemy = new FpsEnemy();
            enemy.setDropItem("health_pack");
            assertEquals("health_pack", enemy.getDropItem());
        }

        @Test
        void dropChanceCanBeSet() {
            FpsEnemy enemy = new FpsEnemy();
            enemy.setDropChance(0.8);
            assertEquals(0.8, enemy.getDropChance());
        }

        @Test
        void experienceRewardCanBeSet() {
            FpsEnemy enemy = new FpsEnemy();
            enemy.setExperienceReward(500);
            assertEquals(500, enemy.getExperienceReward());
        }

        @Test
        void descriptionCanBeSet() {
            FpsEnemy enemy = new FpsEnemy();
            enemy.setDescription("A tough enemy");
            assertEquals("A tough enemy", enemy.getDescription());
        }
    }

    @Nested
    class UniqueIdTests {
        @Test
        void twoEnemiesHaveDifferentIds() {
            FpsEnemy e1 = new FpsEnemy("A", 100, 3.0, FpsEnemy.EnemyBehavior.PATROL, 20);
            FpsEnemy e2 = new FpsEnemy("B", 100, 3.0, FpsEnemy.EnemyBehavior.PATROL, 20);
            assertNotEquals(e1.getId(), e2.getId());
        }
    }

    @Nested
    class IntegrationTests {
        @Test
        void fullEnemyCanBeConfigured() {
            FpsEnemy enemy = new FpsEnemy("Boss", 500, 2.0, FpsEnemy.EnemyBehavior.ATTACK, 100);
            enemy.setAttackRange(200.0);
            enemy.setAttackCooldown(0.3);
            enemy.setDropItem("rare_loot");
            enemy.setDropChance(0.9);
            enemy.setExperienceReward(1000);
            enemy.setDescription("The final boss");

            assertEquals("Boss", enemy.getName());
            assertEquals(500, enemy.getHealth());
            assertEquals(2.0, enemy.getSpeed());
            assertEquals(FpsEnemy.EnemyBehavior.ATTACK, enemy.getBehavior());
            assertEquals(100, enemy.getDamage());
            assertEquals(200.0, enemy.getAttackRange());
            assertEquals(0.3, enemy.getAttackCooldown());
            assertEquals("rare_loot", enemy.getDropItem());
            assertEquals(0.9, enemy.getDropChance());
            assertEquals(1000, enemy.getExperienceReward());
            assertEquals("The final boss", enemy.getDescription());
        }
    }
}

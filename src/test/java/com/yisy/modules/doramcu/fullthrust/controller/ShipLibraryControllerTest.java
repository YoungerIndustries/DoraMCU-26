package com.yisy.modules.doramcu.fullthrust.controller;

import com.yisy.modules.doramcu.fullthrust.handler.GameWebSocketHandler;
import com.yisy.modules.doramcu.fullthrust.model.ShipClass;
import com.yisy.modules.doramcu.fullthrust.service.GameService;
import com.yisy.modules.doramcu.fullthrust.service.PlayerAccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ShipLibraryControllerTest {

    private MockMvc mockMvc;
    private GameService gameService;

    @BeforeEach
    void setUp() {
        gameService = mock(GameService.class);
        GameWebSocketHandler webSocketHandler = mock(GameWebSocketHandler.class);
        PlayerAccountService playerAccountService = mock(PlayerAccountService.class);
        GameController controller = new GameController(gameService, webSocketHandler, playerAccountService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private List<ShipClass> buildMockShipClasses() {
        List<ShipClass> classes = new ArrayList<>();

        ShipClass scout = new ShipClass();
        scout.setId("scout");
        scout.setName("Scout");
        scout.setHullPoints(10);
        scout.setArmorValue(1);
        scout.setMaxSpeed(8);
        scout.setAcceleration(3);
        scout.setDeceleration(2);
        scout.setMaxHeadingChange(2);
        scout.setShieldPoints(5);
        scout.setShieldRegeneration(1);
        scout.setFuelCapacity(20);
        scout.setDescription("Scout class vessel");
        scout.addWeaponSlot(new ShipClass.WeaponSlot("laser_0", 0, "laser"));
        scout.addWeaponSlot(new ShipClass.WeaponSlot("laser_1", 3, "laser"));
        classes.add(scout);

        ShipClass fighter = new ShipClass();
        fighter.setId("fighter");
        fighter.setName("Fighter");
        fighter.setHullPoints(20);
        fighter.setArmorValue(2);
        fighter.setMaxSpeed(6);
        fighter.setAcceleration(2);
        fighter.setDeceleration(2);
        fighter.setMaxHeadingChange(3);
        fighter.setShieldPoints(10);
        fighter.setShieldRegeneration(1);
        fighter.setFuelCapacity(30);
        fighter.setDescription("Fighter class vessel");
        fighter.addWeaponSlot(new ShipClass.WeaponSlot("laser_0", 0, "laser"));
        fighter.addWeaponSlot(new ShipClass.WeaponSlot("laser_1", 3, "laser"));
        fighter.addWeaponSlot(new ShipClass.WeaponSlot("missile_2", 1, "missile"));
        classes.add(fighter);

        ShipClass cruiser = new ShipClass();
        cruiser.setId("cruiser");
        cruiser.setName("Cruiser");
        cruiser.setHullPoints(40);
        cruiser.setArmorValue(3);
        cruiser.setMaxSpeed(5);
        cruiser.setAcceleration(2);
        cruiser.setDeceleration(1);
        cruiser.setMaxHeadingChange(4);
        cruiser.setShieldPoints(20);
        cruiser.setShieldRegeneration(2);
        cruiser.setFuelCapacity(50);
        cruiser.setDescription("Cruiser class vessel");
        cruiser.addWeaponSlot(new ShipClass.WeaponSlot("laser_0", 0, "laser"));
        cruiser.addWeaponSlot(new ShipClass.WeaponSlot("laser_1", 3, "laser"));
        cruiser.addWeaponSlot(new ShipClass.WeaponSlot("missile_2", 1, "missile"));
        cruiser.addWeaponSlot(new ShipClass.WeaponSlot("missile_3", 2, "missile"));
        classes.add(cruiser);

        ShipClass destroyer = new ShipClass();
        destroyer.setId("destroyer");
        destroyer.setName("Destroyer");
        destroyer.setHullPoints(30);
        destroyer.setArmorValue(2);
        destroyer.setMaxSpeed(7);
        destroyer.setAcceleration(3);
        destroyer.setDeceleration(2);
        destroyer.setMaxHeadingChange(3);
        destroyer.setShieldPoints(15);
        destroyer.setShieldRegeneration(1);
        destroyer.setFuelCapacity(40);
        destroyer.setDescription("Destroyer class vessel");
        destroyer.addWeaponSlot(new ShipClass.WeaponSlot("laser_0", 0, "laser"));
        destroyer.addWeaponSlot(new ShipClass.WeaponSlot("missile_1", 2, "missile"));
        destroyer.addWeaponSlot(new ShipClass.WeaponSlot("torpedo_2", 1, "torpedo"));
        classes.add(destroyer);

        ShipClass dreadnought = new ShipClass();
        dreadnought.setId("dreadnought");
        dreadnought.setName("Dreadnought");
        dreadnought.setHullPoints(80);
        dreadnought.setArmorValue(5);
        dreadnought.setMaxSpeed(3);
        dreadnought.setAcceleration(1);
        dreadnought.setDeceleration(1);
        dreadnought.setMaxHeadingChange(5);
        dreadnought.setShieldPoints(40);
        dreadnought.setShieldRegeneration(3);
        dreadnought.setFuelCapacity(80);
        dreadnought.setDescription("Dreadnought class vessel");
        dreadnought.addWeaponSlot(new ShipClass.WeaponSlot("laser_0", 0, "laser"));
        dreadnought.addWeaponSlot(new ShipClass.WeaponSlot("laser_1", 3, "laser"));
        dreadnought.addWeaponSlot(new ShipClass.WeaponSlot("missile_2", 1, "missile"));
        dreadnought.addWeaponSlot(new ShipClass.WeaponSlot("missile_3", 2, "missile"));
        dreadnought.addWeaponSlot(new ShipClass.WeaponSlot("torpedo_4", 1, "torpedo"));
        classes.add(dreadnought);

        return classes;
    }

    @Test
    void getShipClasses_returns200Ok() throws Exception {
        when(gameService.getDefaultShipClasses()).thenReturn(buildMockShipClasses());

        mockMvc.perform(get("/api/game/ship-classes"))
                .andExpect(status().isOk());
    }

    @Test
    void getShipClasses_returnsJsonContentType() throws Exception {
        when(gameService.getDefaultShipClasses()).thenReturn(buildMockShipClasses());

        mockMvc.perform(get("/api/game/ship-classes"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getShipClasses_returnsFiveShipClasses() throws Exception {
        when(gameService.getDefaultShipClasses()).thenReturn(buildMockShipClasses());

        mockMvc.perform(get("/api/game/ship-classes"))
                .andExpect(jsonPath("$.length()").value(5));
    }

    @Test
    void getShipClasses_containsScout() throws Exception {
        when(gameService.getDefaultShipClasses()).thenReturn(buildMockShipClasses());

        mockMvc.perform(get("/api/game/ship-classes"))
                .andExpect(jsonPath("$[?(@.id=='scout')].name").value("Scout"))
                .andExpect(jsonPath("$[?(@.id=='scout')].hullPoints").value(10))
                .andExpect(jsonPath("$[?(@.id=='scout')].maxSpeed").value(8));
    }

    @Test
    void getShipClasses_containsFighter() throws Exception {
        when(gameService.getDefaultShipClasses()).thenReturn(buildMockShipClasses());

        mockMvc.perform(get("/api/game/ship-classes"))
                .andExpect(jsonPath("$[?(@.id=='fighter')].name").value("Fighter"))
                .andExpect(jsonPath("$[?(@.id=='fighter')].hullPoints").value(20))
                .andExpect(jsonPath("$[?(@.id=='fighter')].maxSpeed").value(6));
    }

    @Test
    void getShipClasses_containsCruiser() throws Exception {
        when(gameService.getDefaultShipClasses()).thenReturn(buildMockShipClasses());

        mockMvc.perform(get("/api/game/ship-classes"))
                .andExpect(jsonPath("$[?(@.id=='cruiser')].name").value("Cruiser"))
                .andExpect(jsonPath("$[?(@.id=='cruiser')].hullPoints").value(40));
    }

    @Test
    void getShipClasses_containsDestroyer() throws Exception {
        when(gameService.getDefaultShipClasses()).thenReturn(buildMockShipClasses());

        mockMvc.perform(get("/api/game/ship-classes"))
                .andExpect(jsonPath("$[?(@.id=='destroyer')].name").value("Destroyer"))
                .andExpect(jsonPath("$[?(@.id=='destroyer')].hullPoints").value(30));
    }

    @Test
    void getShipClasses_containsDreadnought() throws Exception {
        when(gameService.getDefaultShipClasses()).thenReturn(buildMockShipClasses());

        mockMvc.perform(get("/api/game/ship-classes"))
                .andExpect(jsonPath("$[?(@.id=='dreadnought')].name").value("Dreadnought"))
                .andExpect(jsonPath("$[?(@.id=='dreadnought')].hullPoints").value(80));
    }

    @Test
    void getShipClasses_weaponSlotsSerialized() throws Exception {
        when(gameService.getDefaultShipClasses()).thenReturn(buildMockShipClasses());

        mockMvc.perform(get("/api/game/ship-classes"))
                .andExpect(jsonPath("$[?(@.id=='scout')].weaponSlots.length()").value(2))
                .andExpect(jsonPath("$[?(@.id=='dreadnought')].weaponSlots.length()").value(5));
    }

    @Test
    void getShipClasses_weaponSlotFieldsSerialized() throws Exception {
        when(gameService.getDefaultShipClasses()).thenReturn(buildMockShipClasses());

        mockMvc.perform(get("/api/game/ship-classes"))
                .andExpect(jsonPath("$[0].weaponSlots[0].weaponType").value("laser"))
                .andExpect(jsonPath("$[0].weaponSlots[0].arc").value(0))
                .andExpect(jsonPath("$[0].weaponSlots[0].damage").value(1))
                .andExpect(jsonPath("$[0].weaponSlots[0].range").value(6));
    }

    @Test
    void getShipClasses_weaponSlotRangeAndDamageSerialized() throws Exception {
        when(gameService.getDefaultShipClasses()).thenReturn(buildMockShipClasses());

        // Scout has 2 lasers (damage=1, range=6)
        mockMvc.perform(get("/api/game/ship-classes"))
                .andExpect(jsonPath("$[?(@.id=='scout')].weaponSlots[0].damage").value(1))
                .andExpect(jsonPath("$[?(@.id=='scout')].weaponSlots[0].range").value(6));

        // Fighter has missile (damage=2, range=12)
        mockMvc.perform(get("/api/game/ship-classes"))
                .andExpect(jsonPath("$[?(@.id=='fighter')].weaponSlots[2].damage").value(2))
                .andExpect(jsonPath("$[?(@.id=='fighter')].weaponSlots[2].range").value(12));

        // Destroyer has torpedo (damage=3, range=8)
        mockMvc.perform(get("/api/game/ship-classes"))
                .andExpect(jsonPath("$[?(@.id=='destroyer')].weaponSlots[2].damage").value(3))
                .andExpect(jsonPath("$[?(@.id=='destroyer')].weaponSlots[2].range").value(8));
    }

    @Test
    void getShipClasses_emptyListWhenServiceReturnsEmpty() throws Exception {
        when(gameService.getDefaultShipClasses()).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/game/ship-classes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getShipClasses_allFieldsPresent() throws Exception {
        when(gameService.getDefaultShipClasses()).thenReturn(buildMockShipClasses());

        mockMvc.perform(get("/api/game/ship-classes"))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].hullPoints").exists())
                .andExpect(jsonPath("$[0].armorValue").exists())
                .andExpect(jsonPath("$[0].maxSpeed").exists())
                .andExpect(jsonPath("$[0].acceleration").exists())
                .andExpect(jsonPath("$[0].deceleration").exists())
                .andExpect(jsonPath("$[0].maxHeadingChange").exists())
                .andExpect(jsonPath("$[0].shieldPoints").exists())
                .andExpect(jsonPath("$[0].shieldRegeneration").exists())
                .andExpect(jsonPath("$[0].fuelCapacity").exists())
                .andExpect(jsonPath("$[0].weaponSlots").exists())
                .andExpect(jsonPath("$[0].description").exists());
    }
}

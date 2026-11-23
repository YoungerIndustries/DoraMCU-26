// DTO classes mirroring Unity/Godot NetworkManager DTOs
// Used for type-safe serialization of API responses

class LoginResponse {
    constructor(data) {
        this.token = data.token || null;
        this.username = data.username || null;
        this.pin = data.pin || null;
        this.empireName = data.empireName || null;
        this.governmentType = data.governmentType || null;
        this.ethos = data.ethos || null;
        this.admin = data.admin || false;
        this.corporationId = data.corporationId || 0;
        this.corporationName = data.corporationName || null;
        this.corporationTicker = data.corporationTicker || null;
    }
}

class PlayerInfo {
    constructor(data) {
        this.id = data.id || 0;
        this.email = data.email || null;
        this.government = data.government || null;
        this.ethos = data.ethos || null;
        this.resources = data.resources || {};
        this.rawResources = data.rawResources || {};
        this.refinedResources = data.refinedResources || {};
        this.modules = data.modules || {};
        this.empires = (data.empires || []).map(e => new EmpireInfo(e));
        this.fleets = (data.fleets || []).map(f => new FleetInfo(f));
        this.planets = (data.planets || []).map(p => new PlanetInfo(p));
        this.ships = (data.ships || []).map(s => new ShipInfo(s));
        this.commander = data.commander ? new CharacterInfo(data.commander) : null;
    }
}

class EmpireInfo {
    constructor(data) {
        this.id = data.id || 0;
        this.name = data.name || null;
        this.government = data.government || null;
        this.ethos = data.ethos || null;
        this.integrity = data.integrity || 0;
        this.militaryStrength = data.militaryStrength || 0;
        this.memberCount = data.memberCount || 0;
    }
}

class FleetInfo {
    constructor(data) {
        this.id = data.id || 0;
        this.name = data.name || null;
        this.sectorRow = data.sectorRow || 0;
        this.sectorCol = data.sectorCol || 0;
        this.subSectorRow = data.subSectorRow || 0;
        this.subSectorCol = data.subSectorCol || 0;
        this.ships = (data.ships || []).map(s => new ShipInfo(s));
        this.combatPower = data.combatPower || 0;
        this.evasion = data.evasion || 0;
        this.fuel = data.fuel || 0;
        this.order = data.order || null;
    }
}

class PlanetInfo {
    constructor(data) {
        this.id = data.id || 0;
        this.name = data.name || null;
        this.type = data.type || null;
        this.size = data.size || 0;
        this.atmosphere = data.atmosphere || 0;
        this.temperature = data.temperature || 0;
        this.hydrographics = data.hydrographics || 0;
        this.population = data.population || 0;
        this.government = data.government || 0;
        this.lawLevel = data.lawLevel || 0;
        this.starPosition = data.starPosition || 0;
        this.orbitRadius = data.orbitRadius || 0;
        this.orbitAngle = data.orbitAngle || 0;
        this.rotationPeriod = data.rotationPeriod || 0;
        this.axialTilt = data.axialTilt || 0;
        this.stats = data.stats ? new PlanetStats(data.stats) : null;
        this.facilities = data.facilities ? new PlanetFacilities(data.facilities) : null;
        this.ownerId = data.ownerId || null;
    }
}

class PlanetStats {
    constructor(data) {
        this.population = data.population || 0;
        this.food = data.food || 0;
        this.industry = data.industry || 0;
        this.combat = data.combat || 0;
    }
}

class PlanetFacilities {
    constructor(data) {
        this.shipyard = data.shipyard || 0;
        this.mine = data.mine || 0;
        this.factory = data.factory || 0;
        this.farm = data.farm || 0;
        this.port = data.port || 0;
        this.communications = data.communications || 0;
    }
}

class ShipInfo {
    constructor(data) {
        this.id = data.id || 0;
        this.name = data.name || null;
        this.design = data.design || null;
        this.hull = data.hull || 0;
        this.drives = data.drives || 0;
        this.cargo = data.cargo || 0;
        this.fuel = data.fuel || 0;
        this.combat = data.combat ? new ShipCombat(data.combat) : null;
        this.sectorRow = data.sectorRow || 0;
        this.sectorCol = data.sectorCol || 0;
        this.subSectorRow = data.subSectorRow || 0;
        this.subSectorCol = data.subSectorCol || 0;
    }
}

class ShipCombat {
    constructor(data) {
        this.armor = data.armor || 0;
        this.weapons = data.weapons || 0;
        this.shields = data.shields || 0;
    }
}

class CharacterInfo {
    constructor(data) {
        this.id = data.id || 0;
        this.name = data.name || null;
        this.species = data.species || null;
        this.characteristics = data.characteristics || {};
        this.skills = data.skills || {};
        this.careers = data.careers || [];
        this.currentCareer = data.currentCareer || null;
        this.location = data.location || null;
        this.equipment = data.equipment || null;
        this.boonBane = data.boonBane || null;
        this.alive = data.alive !== false;
        this.status = data.status || null;
    }
}

class GalaxyInfo {
    constructor(data) {
        this.id = data.id || 0;
        this.name = data.name || null;
        this.sectors = (data.sectors || []).map(s => new SectorInfo(s));
        this.turn = data.turn || 0;
        this.timestamp = data.timestamp || 0;
        this.health = data.health || 0;
        this.completion = data.completion || 0;
    }
}

class SectorInfo {
    constructor(data) {
        this.id = data.id || 0;
        this.row = data.row || 0;
        this.col = data.col || 0;
        this.subSectors = (data.subSectors || []).map(s => new SubSectorInfo(s));
        this.controller = data.controller || null;
        this.tradeType = data.tradeType || null;
        this.explored = data.explored || false;
    }
}

class SubSectorInfo {
    constructor(data) {
        this.id = data.id || 0;
        this.row = data.row || 0;
        this.col = data.col || 0;
        this.solarSystem = data.solarSystem ? new SolarSystemInfo(data.solarSystem) : null;
    }
}

class SolarSystemInfo {
    constructor(data) {
        this.id = data.id || 0;
        this.star = data.star ? new StarInfo(data.star) : null;
        this.planets = (data.planets || []).map(p => new PlanetInfo(p));
        this.stations = (data.stations || []).map(s => new StationInfo(s));
        this.explored = data.explored || false;
    }
}

class StarInfo {
    constructor(data) {
        this.id = data.id || 0;
        this.spectralClass = data.spectralClass || null;
        this.mass = data.mass || 0;
        this.radius = data.radius || 0;
    }
}

class StationInfo {
    constructor(data) {
        this.id = data.id || 0;
        this.name = data.name || null;
        this.type = data.type || null;
        this.sectorRow = data.sectorRow || 0;
        this.sectorCol = data.sectorCol || 0;
        this.subSectorRow = data.subSectorRow || 0;
        this.subSectorCol = data.subSectorCol || 0;
        this.fuel = data.fuel || 0;
        this.repair = data.repair || 0;
    }
}

class TickInfo {
    constructor(data) {
        this.tick = data.tick || 0;
        this.running = data.running || false;
        this.entityCount = data.entityCount || 0;
        this.activeSessions = data.activeSessions || 0;
        this.connectedPlayers = data.connectedPlayers || 0;
    }
}

class GameState {
    constructor(data) {
        this.tick = data.tick ? new TickInfo(data.tick) : null;
        this.player = data.player ? new PlayerInfo(data.player) : null;
        this.galaxy = data.galaxy ? new GalaxyInfo(data.galaxy) : null;
    }
}

class AiAdminData {
    constructor(data) {
        this.id = data.id || 0;
        this.name = data.name || null;
        this.position = data.position || null;
        this.sectorRow = data.sectorRow || 0;
        this.sectorCol = data.sectorCol || 0;
    }
}

// Export as globals
window.LoginResponse = LoginResponse;
window.PlayerInfo = PlayerInfo;
window.EmpireInfo = EmpireInfo;
window.FleetInfo = FleetInfo;
window.PlanetInfo = PlanetInfo;
window.PlanetStats = PlanetStats;
window.PlanetFacilities = PlanetFacilities;
window.ShipInfo = ShipInfo;
window.ShipCombat = ShipCombat;
window.CharacterInfo = CharacterInfo;
window.GalaxyInfo = GalaxyInfo;
window.SectorInfo = SectorInfo;
window.SubSectorInfo = SubSectorInfo;
window.SolarSystemInfo = SolarSystemInfo;
window.StarInfo = StarInfo;
window.StationInfo = StationInfo;
window.TickInfo = TickInfo;
window.GameState = GameState;
window.AiAdminData = AiAdminData;

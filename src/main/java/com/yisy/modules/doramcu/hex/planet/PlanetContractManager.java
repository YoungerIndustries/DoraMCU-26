package com.yisy.modules.doramcu.hex.planet;

import com.yisy.modules.doramcu.hex.Hex3D;
import com.yisy.modules.doramcu.hex.Planet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Manages contracts for planet surface operations.
 * <p>
 * This class allows Empires to issue, accept, and execute contracts for
 * building settlements, industries, infrastructure, and deploying vehicles
 * on planet surfaces. It coordinates with {@link PlanetSurfaceBuilder} to
 * perform the actual building operations.
 * </p>
 * <p>
 * Contract execution flow:
 * <ol>
 *   <li>Empire issues a contract via {@link #issueContract}</li>
 *   <li>Contract becomes available for acceptance</li>
 *   <li>Empire (or another party) accepts the contract</li>
 *   <li>On each turn, {@link #processTurns} decrements the contract timer</li>
 *   <li>When timer expires, {@link #executeContract} performs the building action</li>
 *   <li>Contract is marked completed with the created entity ID</li>
 * </ol>
 * </p>
 */
public class PlanetContractManager {

    private final PlanetSurfaceBuilder surfaceBuilder;
    private final AtomicLong nextId = new AtomicLong(1);

    /** All contracts indexed by ID */
    private final Map<String, PlanetContract> contracts = new java.util.HashMap<>();

    /** Contracts by planet ID */
    private final Map<Integer, List<String>> contractsByPlanet = new java.util.HashMap<>();

    /** Contracts by empire ID */
    private final Map<String, List<String>> contractsByEmpire = new java.util.HashMap<>();

    /**
     * Creates a new contract manager with the given surface builder.
     *
     * @param surfaceBuilder the builder that performs actual surface operations
     */
    public PlanetContractManager(PlanetSurfaceBuilder surfaceBuilder) {
        if (surfaceBuilder == null) {
            throw new IllegalArgumentException("Surface builder must not be null");
        }
        this.surfaceBuilder = surfaceBuilder;
    }

    // ---- Contract Issuing ----

    /**
     * Issues a contract to found a new settlement.
     *
     * @param planet       the target planet
     * @param name         settlement name
     * @param centerHex    center hex for the settlement
     * @param empireId     empire issuing the contract
     * @param payment      payment credits
     * @param difficulty   difficulty (1-10)
     * @param durationTurns turns to complete
     * @return the created contract
     */
    public PlanetContract issueSettlementContract(Planet planet, String name, Hex3D centerHex,
                                                   String empireId, int payment, int difficulty,
                                                   int durationTurns) {
        String id = "CONTRACT-" + nextId.getAndIncrement();
        PlanetContract contract = new PlanetContract(
                id, PlanetContractType.FOUND_SETTLEMENT,
                "Found " + name,
                "Establish a new settlement named '" + name + "' on the planet surface.",
                planet.getId(), empireId, centerHex, null,
                payment, difficulty * 10, difficulty, durationTurns
        );
        registerContract(contract);
        return contract;
    }

    /**
     * Issues a contract to build an industry.
     *
     * @param planet       the target planet
     * @param industryType the type of industry
     * @param hex          hex cell for the industry
     * @param empireId     empire issuing the contract
     * @param payment      payment credits
     * @param difficulty   difficulty (1-10)
     * @param durationTurns turns to complete
     * @return the created contract
     */
    public PlanetContract issueIndustryContract(Planet planet, IndustryType industryType, Hex3D hex,
                                                 String empireId, int payment, int difficulty,
                                                 int durationTurns) {
        String id = "CONTRACT-" + nextId.getAndIncrement();
        PlanetContractType type = mapIndustryToContractType(industryType);
        PlanetContract contract = new PlanetContract(
                id, type,
                "Build " + industryType.getDisplayName(),
                "Construct a " + industryType.getDisplayName() + " on the planet surface.",
                planet.getId(), empireId, hex, null,
                payment, difficulty * 10, difficulty, durationTurns
        );
        registerContract(contract);
        return contract;
    }

    /**
     * Issues a contract to build a station.
     *
     * @param planet       the target planet
     * @param name         station name
     * @param hex          hex cell for the station
     * @param empireId     empire issuing the contract
     * @param payment      payment credits
     * @param difficulty   difficulty (1-10)
     * @param durationTurns turns to complete
     * @return the created contract
     */
    public PlanetContract issueStationContract(Planet planet, String name, Hex3D hex,
                                                String empireId, int payment, int difficulty,
                                                int durationTurns) {
        String id = "CONTRACT-" + nextId.getAndIncrement();
        PlanetContract contract = new PlanetContract(
                id, PlanetContractType.BUILD_STATION,
                "Build " + name + " Station",
                "Construct a cargo station named '" + name + "' on the planet surface.",
                planet.getId(), empireId, hex, null,
                payment, difficulty * 10, difficulty, durationTurns
        );
        registerContract(contract);
        return contract;
    }

    /**
     * Issues a contract to build infrastructure between two hex cells.
     *
     * @param planet       the target planet
     * @param infraType    the infrastructure type
     * @param startHex     starting hex cell
     * @param endHex       ending hex cell
     * @param empireId     empire issuing the contract
     * @param payment      payment credits
     * @param difficulty   difficulty (1-10)
     * @param durationTurns turns to complete
     * @return the created contract
     */
    public PlanetContract issueInfrastructureContract(Planet planet,
                                                       TerrainData.InfrastructureType infraType,
                                                       Hex3D startHex, Hex3D endHex,
                                                       String empireId, int payment, int difficulty,
                                                       int durationTurns) {
        String id = "CONTRACT-" + nextId.getAndIncrement();
        PlanetContractType type = mapInfraToContractType(infraType);
        PlanetContract contract = new PlanetContract(
                id, type,
                "Build " + infraType.name().toLowerCase() + " connection",
                "Construct " + infraType.name().toLowerCase() + " between two locations.",
                planet.getId(), empireId, startHex, endHex,
                payment, difficulty * 10, difficulty, durationTurns
        );
        registerContract(contract);
        return contract;
    }

    /**
     * Issues a contract to deploy a vehicle.
     *
     * @param planet       the target planet
     * @param vehicleType  the vehicle type
     * @param startStation the starting station
     * @param empireId     empire issuing the contract
     * @param payment      payment credits
     * @param difficulty   difficulty (1-10)
     * @param durationTurns turns to complete
     * @return the created contract
     */
    public PlanetContract issueVehicleContract(Planet planet, VehicleType vehicleType,
                                                Station startStation, String empireId,
                                                int payment, int difficulty, int durationTurns) {
        String id = "CONTRACT-" + nextId.getAndIncrement();
        PlanetContract contract = new PlanetContract(
                id, PlanetContractType.DEPLOY_VEHICLE,
                "Deploy " + vehicleType.getDisplayName(),
                "Deploy a " + vehicleType.getDisplayName() + " on the planet surface.",
                planet.getId(), empireId, startStation.getLocation(), null,
                payment, difficulty * 10, difficulty, durationTurns
        );
        registerContract(contract);
        return contract;
    }

    /**
     * Issues a generic contract.
     *
     * @param contract the contract to register
     */
    public void issueContract(PlanetContract contract) {
        registerContract(contract);
    }

    // ---- Contract Acceptance ----

    /**
     * Accepts a contract for execution.
     *
     * @param contractId       the contract ID
     * @param assigneeEmpireId the empire accepting the contract
     * @return true if the contract was accepted
     */
    public boolean acceptContract(String contractId, String assigneeEmpireId) {
        PlanetContract contract = contracts.get(contractId);
        if (contract == null) {
            return false;
        }
        return contract.accept(assigneeEmpireId);
    }

    // ---- Contract Execution ----

    /**
     * Executes a completed contract, performing the actual building operation.
     *
     * @param contract the contract to execute
     * @param planet   the target planet
     * @return true if execution was successful
     */
    public boolean executeContract(PlanetContract contract, Planet planet) {
        if (contract.getStatus() != PlanetContract.ContractStatus.ACTIVE) {
            return false;
        }

        String empireId = contract.getAssigneeEmpireId();
        try {
            switch (contract.getType()) {
                case FOUND_SETTLEMENT:
                    Settlement settlement = surfaceBuilder.foundSettlement(
                            planet, contract.getTitle().replace("Found ", ""),
                            contract.getTargetHex(), empireId);
                    contract.complete(settlement.getId());
                    break;

                case BUILD_PRIMARY_INDUSTRY:
                case BUILD_SECONDARY_INDUSTRY:
                case BUILD_TERTIARY_INDUSTRY:
                    IndustryType industryType = resolveIndustryType(contract);
                    if (industryType != null) {
                        Industry industry = surfaceBuilder.buildIndustry(
                                planet, industryType, contract.getTargetHex(), empireId);
                        contract.complete(industry.getId());
                    } else {
                        contract.fail();
                    }
                    break;

                case BUILD_STATION:
                    Station station = surfaceBuilder.buildStation(
                            planet, contract.getTitle().replace("Build ", "").replace(" Station", ""),
                            contract.getTargetHex(), empireId);
                    contract.complete(station.getId());
                    break;

                case BUILD_ROAD:
                case BUILD_RAIL:
                    TerrainData.InfrastructureType infraType = mapContractToInfra(contract.getType());
                    Hex3D startHex = contract.getTargetHex();
                    Hex3D endHex = contract.getSecondaryHex();
                    if (startHex != null) {
                        surfaceBuilder.buildInfrastructure(startHex, infraType, empireId);
                        if (endHex != null) {
                            surfaceBuilder.buildInfrastructure(endHex, infraType, empireId);
                        }
                        contract.complete("INFRA-" + contract.getId());
                    } else {
                        contract.fail();
                    }
                    break;

                case BUILD_PORT:
                    TerrainData.InfrastructureType portType = TerrainData.InfrastructureType.PORT;
                    if (contract.getTargetHex() != null) {
                        surfaceBuilder.buildInfrastructure(contract.getTargetHex(), portType, empireId);
                        contract.complete("PORT-" + contract.getId());
                    } else {
                        contract.fail();
                    }
                    break;

                case DEPLOY_VEHICLE:
                    VehicleType vehicleType = resolveVehicleType(contract);
                    if (vehicleType != null && contract.getTargetHex() != null) {
                        Station startStation = findStationNearHex(planet, contract.getTargetHex(), empireId);
                        if (startStation != null) {
                            SurfaceVehicle vehicle = surfaceBuilder.deployVehicle(
                                    planet, vehicleType, startStation, empireId);
                            contract.complete(vehicle.getId());
                        } else {
                            contract.fail();
                        }
                    } else {
                        contract.fail();
                    }
                    break;

                case SURVEY_PLANET:
                    int hexCount = surfaceBuilder.generateBiomes(planet, System.currentTimeMillis());
                    contract.complete("SURVEY-" + hexCount);
                    break;

                default:
                    // For contract types without specific execution, just complete
                    contract.complete(contract.getId());
                    break;
            }
        } catch (Exception e) {
            contract.fail();
        }

        return contract.isCompleted();
    }

    // ---- Turn Processing ----

    /**
     * Processes all active contracts for a planet.
     * <p>
     * Decrements turn counters and executes contracts that have completed
     * their duration.
     * </p>
     *
     * @param planet the planet
     */
    public void processTurns(Planet planet) {
        List<String> contractIds = contractsByPlanet.getOrDefault(planet.getId(), Collections.emptyList());
        for (String contractId : new ArrayList<>(contractIds)) {
            PlanetContract contract = contracts.get(contractId);
            if (contract == null || !contract.isActive()) {
                continue;
            }

            boolean finished = contract.processTurn();
            if (finished) {
                if (contract.getStatus() == PlanetContract.ContractStatus.ACTIVE) {
                    // Duration completed, execute the contract
                    executeContract(contract, planet);
                }
            }
        }
    }

    // ---- Query Methods ----

    /**
     * Returns a contract by ID.
     *
     * @param contractId the contract ID
     * @return the contract, or null if not found
     */
    public PlanetContract getContract(String contractId) {
        return contracts.get(contractId);
    }

    /**
     * Returns all contracts for a planet.
     *
     * @param planetId the planet ID
     * @return list of contracts
     */
    public List<PlanetContract> getContractsByPlanet(int planetId) {
        List<String> ids = contractsByPlanet.getOrDefault(planetId, Collections.emptyList());
        return ids.stream()
                .map(contracts::get)
                .filter(c -> c != null)
                .collect(Collectors.toList());
    }

    /**
     * Returns all contracts for an empire.
     *
     * @param empireId the empire ID
     * @return list of contracts
     */
    public List<PlanetContract> getContractsByEmpire(String empireId) {
        List<String> ids = contractsByEmpire.getOrDefault(empireId, Collections.emptyList());
        return ids.stream()
                .map(contracts::get)
                .filter(c -> c != null)
                .collect(Collectors.toList());
    }

    /**
     * Returns all available contracts for a planet.
     *
     * @param planetId the planet ID
     * @return list of available contracts
     */
    public List<PlanetContract> getAvailableContracts(int planetId) {
        return getContractsByPlanet(planetId).stream()
                .filter(PlanetContract::isAvailable)
                .collect(Collectors.toList());
    }

    /**
     * Returns all active contracts for an empire.
     *
     * @param empireId the empire ID
     * @return list of active contracts
     */
    public List<PlanetContract> getActiveContracts(String empireId) {
        return getContractsByEmpire(empireId).stream()
                .filter(PlanetContract::isActive)
                .collect(Collectors.toList());
    }

    /**
     * Returns all contracts.
     *
     * @return list of all contracts
     */
    public List<PlanetContract> getAllContracts() {
        return new ArrayList<>(contracts.values());
    }

    // ---- Internal Methods ----

    private void registerContract(PlanetContract contract) {
        contracts.put(contract.getId(), contract);
        contractsByPlanet.computeIfAbsent(contract.getPlanetId(), k -> new ArrayList<>())
                .add(contract.getId());
        contractsByEmpire.computeIfAbsent(contract.getIssuerEmpireId(), k -> new ArrayList<>())
                .add(contract.getId());
    }

    private PlanetContractType mapIndustryToContractType(IndustryType industryType) {
        switch (industryType.getLevel()) {
            case PRIMARY: return PlanetContractType.BUILD_PRIMARY_INDUSTRY;
            case SECONDARY: return PlanetContractType.BUILD_SECONDARY_INDUSTRY;
            default: return PlanetContractType.BUILD_TERTIARY_INDUSTRY;
        }
    }

    private PlanetContractType mapInfraToContractType(TerrainData.InfrastructureType infraType) {
        switch (infraType) {
            case ROAD: return PlanetContractType.BUILD_ROAD;
            case RAIL: return PlanetContractType.BUILD_RAIL;
            case STATION: return PlanetContractType.BUILD_STATION;
            case PORT: return PlanetContractType.BUILD_PORT;
            default: return PlanetContractType.UPGRADE_INFRASTRUCTURE;
        }
    }

    private TerrainData.InfrastructureType mapContractToInfra(PlanetContractType type) {
        switch (type) {
            case BUILD_ROAD: return TerrainData.InfrastructureType.ROAD;
            case BUILD_RAIL: return TerrainData.InfrastructureType.RAIL;
            default: return TerrainData.InfrastructureType.ROAD;
        }
    }

    private IndustryType resolveIndustryType(PlanetContract contract) {
        // Try to infer from the contract title
        String title = contract.getTitle().toLowerCase();
        for (IndustryType type : IndustryType.values()) {
            if (title.contains(type.getDisplayName().toLowerCase())) {
                return type;
            }
        }
        // Default to coal mine
        return IndustryType.COAL_MINE;
    }

    private VehicleType resolveVehicleType(PlanetContract contract) {
        String title = contract.getTitle().toLowerCase();
        for (VehicleType type : VehicleType.values()) {
            if (title.contains(type.getDisplayName().toLowerCase())) {
                return type;
            }
        }
        // Default to truck
        return VehicleType.TRUCK;
    }

    private Station findStationNearHex(Planet planet, Hex3D hex, String empireId) {
        List<Station> stations = surfaceBuilder.getStations(planet, empireId);
        for (Station station : stations) {
            if (station.getLocation().equals(hex)) {
                return station;
            }
        }
        // Return any station if exact match not found
        return stations.isEmpty() ? null : stations.get(0);
    }
}

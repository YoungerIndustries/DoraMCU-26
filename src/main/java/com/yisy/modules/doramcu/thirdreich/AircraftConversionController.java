package com.yisy.modules.doramcu.thirdreich;

import com.yisy.modules.doramcu.thirdreich.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST API for aircraft conversion.
 */
@RestController
@RequestMapping("/api/v1/third-reich/aircraft")
public class AircraftConversionController {

    private final AircraftConversionService conversionService;
    private final TravellerSpacecraftFactory spacecraftFactory;

    public AircraftConversionController(AircraftConversionService conversionService,
                                        TravellerSpacecraftFactory spacecraftFactory) {
        this.conversionService = conversionService;
        this.spacecraftFactory = spacecraftFactory;
    }

    @PostMapping("/convert")
    public ResponseEntity<AircraftConversionDto> convert(@RequestBody ConvertRequest request) {
        ThirdReichAircraft aircraft = ThirdReichAircraft.valueOf(request.getAircraftName().toUpperCase());
        TravellerSpacecraft spacecraft = conversionService.convert(aircraft);
        AircraftConversion conversion = conversionService.getConversion(aircraft);

        AircraftConversionDto dto = new AircraftConversionDto();
        dto.setThirdReichAircraftId(aircraft.name());
        dto.setTravellerSpacecraftName(spacecraft.getName());
        dto.setTechLevel(spacecraft.getTechLevel());
        dto.setCost((int) spacecraft.getCost());
        dto.setArmorClass(spacecraft.getArmorClass());
        dto.setHullPoints(spacecraft.getHullPoints());
        dto.setRequiredSkills(conversion.getRequiredSkills());
        dto.setDescription(conversion.getDescription());
        dto.setBuildable(true);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}/traveller")
    public ResponseEntity<AircraftConversionDto> getTravellerEquivalent(@PathVariable String id) {
        ThirdReichAircraft aircraft = ThirdReichAircraft.valueOf(id.toUpperCase());
        TravellerSpacecraft spacecraft = conversionService.convert(aircraft);
        AircraftConversion conversion = conversionService.getConversion(aircraft);

        AircraftConversionDto dto = new AircraftConversionDto();
        dto.setThirdReichAircraftId(aircraft.name());
        dto.setTravellerSpacecraftName(spacecraft.getName());
        dto.setTechLevel(spacecraft.getTechLevel());
        dto.setCost((int) spacecraft.getCost());
        dto.setArmorClass(spacecraft.getArmorClass());
        dto.setHullPoints(spacecraft.getHullPoints());
        dto.setRequiredSkills(conversion.getRequiredSkills());
        dto.setDescription(conversion.getDescription());
        dto.setBuildable(true);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/build")
    public ResponseEntity<TravellerSpacecraftDto> build(@RequestBody BuildRequest request) {
        TravellerSpacecraftType type = TravellerSpacecraftType.valueOf(request.getType().toUpperCase());
        TravellerSpacecraft spacecraft = spacecraftFactory.createSpacecraft(type, request.getTechLevel());

        TravellerSpacecraftDto dto = new TravellerSpacecraftDto();
        dto.setId(spacecraft.getId());
        dto.setName(spacecraft.getName());
        dto.setType(spacecraft.getType().name());
        dto.setTechLevel(spacecraft.getTechLevel());
        dto.setArmorClass(spacecraft.getArmorClass());
        dto.setHullPoints(spacecraft.getHullPoints());
        dto.setMovement(spacecraft.getMovement());
        dto.setCrew(spacecraft.getCrew());
        dto.setCost(spacecraft.getCost());
        dto.setPressurized(spacecraft.isPressurized());
        dto.setWeapons(spacecraft.getWeapons().stream()
                .map(AirWeaponMount::getWeaponName).collect(Collectors.toList()));
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/available/{tl}")
    public ResponseEntity<List<TravellerSpacecraftDto>> getAvailable(@PathVariable int tl) {
        List<TravellerSpacecraftType> types = spacecraftFactory.getAvailableSpacecraft(tl);
        List<TravellerSpacecraft> spacecraft = types.stream()
                .map(t -> spacecraftFactory.createSpacecraft(t, tl))
                .collect(Collectors.toList());
        List<TravellerSpacecraftDto> dtos = spacecraft.stream().map(sc -> {
            TravellerSpacecraftDto dto = new TravellerSpacecraftDto();
            dto.setId(sc.getId());
            dto.setName(sc.getName());
            dto.setType(sc.getType().name());
            dto.setTechLevel(sc.getTechLevel());
            dto.setArmorClass(sc.getArmorClass());
            dto.setHullPoints(sc.getHullPoints());
            dto.setMovement(sc.getMovement());
            dto.setCrew(sc.getCrew());
            dto.setCost(sc.getCost());
            dto.setPressurized(sc.isPressurized());
            dto.setWeapons(sc.getWeapons().stream()
                    .map(AirWeaponMount::getWeaponName).collect(Collectors.toList()));
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping
    public ResponseEntity<List<AircraftConversionDto>> getAllConversions() {
        List<AircraftConversionDto> dtos = conversionService.getAllConversions().stream().map(c -> {
            AircraftConversionDto dto = new AircraftConversionDto();
            dto.setThirdReichAircraftId(c.getThirdReichAircraftId());
            dto.setTravellerSpacecraftName(c.getTravellerSpacecraftName());
            dto.setTechLevel(c.getTechLevel());
            dto.setCost(c.getCost());
            dto.setRequiredSkills(c.getRequiredSkills());
            dto.setDescription(c.getDescription());
            dto.setBuildable(true);
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    public static class ConvertRequest {
        private String aircraftName;
        public String getAircraftName() { return aircraftName; }
        public void setAircraftName(String aircraftName) { this.aircraftName = aircraftName; }
    }

    public static class BuildRequest {
        private String type;
        private int techLevel;
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public int getTechLevel() { return techLevel; }
        public void setTechLevel(int techLevel) { this.techLevel = techLevel; }
    }
}

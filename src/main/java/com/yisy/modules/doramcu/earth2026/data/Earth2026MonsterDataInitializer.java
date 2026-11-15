package com.yisy.modules.doramcu.earth2026.data;

import com.yisy.modules.doramcu.earth2026.model.E26Monster;
import com.yisy.modules.doramcu.earth2026.repository.E26MonsterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class Earth2026MonsterDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(Earth2026MonsterDataInitializer.class);

    private final E26MonsterRepository repository;

    @Autowired
    public Earth2026MonsterDataInitializer(E26MonsterRepository repository) {
        this.repository = repository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        if (repository.count() > 0) {
            log.info("Earth 2026 monsters already seeded, skipping.");
            return;
        }
        List<E26Monster> monsters = new ArrayList<>();

        // TL1
        E26Monster m;
        m = new E26Monster(); m.setName("Street Thug"); m.setThreatLevel(1); m.setXp(25); m.setSize("Small"); m.setType("Human"); m.setSubtype("Gang"); m.setAlignment("Chaotic Evil"); m.setAc(10); m.setHp(15); m.setSpeed(9); m.setStr(12); m.setDex(10); m.setCon(8); m.setIntel(10); m.setWis(12); m.setCha(8); m.setSkills("Stealth +3"); m.setSenses("Normal"); m.setDescription("A low-level thug."); monsters.add(m);
        m = new E26Monster(); m.setName("Pickpocket"); m.setThreatLevel(1); m.setXp(25); m.setSize("Medium"); m.setType("Human"); m.setSubtype("Criminal"); m.setAlignment("Chaotic Neutral"); m.setAc(10); m.setHp(12); m.setSpeed(12); m.setStr(14); m.setDex(12); m.setCon(14); m.setIntel(12); m.setWis(14); m.setCha(12); m.setSkills("Stealth +5"); m.setSenses("Normal"); m.setDescription("A nimble thief."); monsters.add(m);
        m = new E26Monster(); m.setName("Stray Dog"); m.setThreatLevel(1); m.setXp(10); m.setSize("Small"); m.setType("Animal"); m.setSubtype("Canine"); m.setAlignment("Neutral"); m.setAc(12); m.setHp(8); m.setSpeed(10); m.setStr(6); m.setDex(10); m.setCon(6); m.setIntel(6); m.setWis(10); m.setCha(6); m.setSenses("Normal"); m.setDescription("A feral dog."); monsters.add(m);

        // TL2
        m = new E26Monster(); m.setName("Street Gang"); m.setThreatLevel(2); m.setXp(50); m.setSize("Medium"); m.setType("Human"); m.setSubtype("Gang"); m.setAlignment("Chaotic Evil"); m.setAc(11); m.setHp(20); m.setSpeed(13); m.setStr(8); m.setDex(10); m.setCon(8); m.setIntel(8); m.setWis(10); m.setCha(8); m.setSkills("Intimidation +3"); m.setSenses("Normal"); m.setDescription("Armed gang members."); monsters.add(m);
        m = new E26Monster(); m.setName("Gang Leader"); m.setThreatLevel(2); m.setXp(75); m.setSize("Medium"); m.setType("Human"); m.setSubtype("Gang"); m.setAlignment("Chaotic Evil"); m.setAc(13); m.setHp(30); m.setSpeed(12); m.setStr(10); m.setDex(12); m.setCon(14); m.setIntel(12); m.setWis(14); m.setCha(12); m.setSkills("Intimidation +5"); m.setSenses("Normal"); m.setDescription("Leader of a street gang."); monsters.add(m);
        m = new E26Monster(); m.setName("Armed Robber"); m.setThreatLevel(2); m.setXp(50); m.setSize("Medium"); m.setType("Human"); m.setSubtype("Criminal"); m.setAlignment("Chaotic Evil"); m.setAc(12); m.setHp(25); m.setSpeed(12); m.setStr(8); m.setDex(10); m.setCon(8); m.setIntel(8); m.setWis(10); m.setCha(8); m.setSkills("Stealth +2"); m.setSenses("Normal"); m.setDescription("An armed criminal."); monsters.add(m);
        m = new E26Monster(); m.setName("Attack Dog"); m.setThreatLevel(2); m.setXp(30); m.setSize("Medium"); m.setType("Animal"); m.setSubtype("Canine"); m.setAlignment("Neutral"); m.setAc(14); m.setHp(20); m.setSpeed(12); m.setStr(6); m.setDex(8); m.setCon(6); m.setIntel(6); m.setWis(8); m.setCha(6); m.setSenses("Normal"); m.setDescription("A trained attack dog."); monsters.add(m);

        // TL3
        m = new E26Monster(); m.setName("Hitman"); m.setThreatLevel(3); m.setXp(100); m.setSize("Medium"); m.setType("Human"); m.setSubtype("Criminal"); m.setAlignment("Lawful Evil"); m.setAc(13); m.setHp(40); m.setSpeed(10); m.setStr(12); m.setDex(14); m.setCon(12); m.setIntel(12); m.setWis(14); m.setCha(12); m.setSkills("Stealth +4"); m.setSenses("Normal"); m.setDescription("A contract killer."); monsters.add(m);
        m = new E26Monster(); m.setName("Smuggler"); m.setThreatLevel(3); m.setXp(75); m.setSize("Medium"); m.setType("Human"); m.setSubtype("Criminal"); m.setAlignment("Chaotic Neutral"); m.setAc(12); m.setHp(35); m.setSpeed(12); m.setStr(10); m.setDex(12); m.setCon(10); m.setIntel(10); m.setWis(12); m.setCha(10); m.setSkills("Stealth +3"); m.setSenses("Normal"); m.setDescription("Moves contraband."); monsters.add(m);
        m = new E26Monster(); m.setName("Cybercriminal"); m.setThreatLevel(3); m.setXp(100); m.setSize("Medium"); m.setType("Human"); m.setSubtype("Criminal"); m.setAlignment("Chaotic Evil"); m.setAc(10); m.setHp(30); m.setSpeed(10); m.setStr(16); m.setDex(12); m.setCon(8); m.setIntel(12); m.setWis(8); m.setCha(10); m.setSkills("Hacking +6"); m.setSenses("Normal"); m.setDescription("A hacker."); monsters.add(m);
        m = new E26Monster(); m.setName("Crime Boss"); m.setThreatLevel(3); m.setXp(150); m.setSize("Medium"); m.setType("Human"); m.setSubtype("Criminal"); m.setAlignment("Lawful Evil"); m.setAc(14); m.setHp(50); m.setSpeed(12); m.setStr(12); m.setDex(14); m.setCon(16); m.setIntel(14); m.setWis(16); m.setCha(12); m.setSkills("Intimidation +6"); m.setSenses("Normal"); m.setDescription("Head of crime syndicate."); monsters.add(m);

        // TL4
        m = new E26Monster(); m.setName("Mercenary"); m.setThreatLevel(4); m.setXp(150); m.setSize("Medium"); m.setType("Military"); m.setSubtype("Contractor"); m.setAlignment("Neutral"); m.setAc(14); m.setHp(55); m.setSpeed(14); m.setStr(12); m.setDex(12); m.setCon(10); m.setIntel(10); m.setWis(12); m.setCha(10); m.setSkills("Athletics +4"); m.setSenses("Normal"); m.setDescription("Soldier for hire."); monsters.add(m);
        m = new E26Monster(); m.setName("SWAT Team"); m.setThreatLevel(4); m.setXp(200); m.setSize("Medium"); m.setType("Military"); m.setSubtype("Police"); m.setAlignment("Lawful Neutral"); m.setAc(14); m.setHp(60); m.setSpeed(14); m.setStr(10); m.setDex(12); m.setCon(10); m.setIntel(10); m.setWis(12); m.setCha(10); m.setSkills("Tactics +4"); m.setSenses("Normal"); m.setDescription("Police tactical unit."); monsters.add(m);
        m = new E26Monster(); m.setName("PMC Soldier"); m.setThreatLevel(4); m.setXp(175); m.setSize("Medium"); m.setType("Military"); m.setSubtype("PMC"); m.setAlignment("Neutral Evil"); m.setAc(15); m.setHp(65); m.setSpeed(14); m.setStr(12); m.setDex(12); m.setCon(10); m.setIntel(10); m.setWis(12); m.setCha(10); m.setSkills("Tactics +4"); m.setSenses("Normal"); m.setDescription("Private military contractor."); monsters.add(m);

        // TL5
        m = new E26Monster(); m.setName("Special Forces"); m.setThreatLevel(5); m.setXp(300); m.setSize("Medium"); m.setType("Military"); m.setSubtype("Elite"); m.setAlignment("Lawful Neutral"); m.setAc(16); m.setHp(80); m.setSpeed(16); m.setStr(14); m.setDex(14); m.setCon(12); m.setIntel(12); m.setWis(14); m.setCha(12); m.setSkills("Tactics +6"); m.setSenses("Normal"); m.setDescription("Elite operators."); monsters.add(m);
        m = new E26Monster(); m.setName("Terrorist"); m.setThreatLevel(5); m.setXp(250); m.setSize("Medium"); m.setType("Human"); m.setSubtype("Extremist"); m.setAlignment("Chaotic Evil"); m.setAc(14); m.setHp(70); m.setSpeed(14); m.setStr(12); m.setDex(12); m.setCon(10); m.setIntel(10); m.setWis(12); m.setCha(10); m.setSkills("Intimidation +4"); m.setSenses("Normal"); m.setDescription("A radical extremist."); monsters.add(m);
        m = new E26Monster(); m.setName("Surveillance Drone"); m.setThreatLevel(5); m.setXp(150); m.setSize("Tiny"); m.setType("Drone"); m.setSubtype("Recon"); m.setAlignment("Neutral"); m.setAc(8); m.setHp(40); m.setSpeed(8); m.setStr(14); m.setDex(10); m.setCon(8); m.setIntel(8); m.setWis(10); m.setCha(8); m.setSenses("Night Vision, Thermal"); m.setDescription("A surveillance drone."); monsters.add(m);
        m = new E26Monster(); m.setName("Combat Robot"); m.setThreatLevel(5); m.setXp(300); m.setSize("Medium"); m.setType("Robot"); m.setSubtype("Military"); m.setAlignment("Neutral"); m.setAc(16); m.setHp(100); m.setSpeed(12); m.setStr(8); m.setDex(10); m.setCon(8); m.setIntel(8); m.setWis(10); m.setCha(8); m.setSenses("Motion Detection"); m.setDescription("An autonomous combat robot."); m.setDamageImmunities("Energy"); m.setDamageResistances("Chemical"); monsters.add(m);

        // TL6
        m = new E26Monster(); m.setName("Rogue AI"); m.setThreatLevel(6); m.setXp(500); m.setSize("Medium"); m.setType("AI"); m.setSubtype("Autonomous"); m.setAlignment("Chaotic Evil"); m.setAc(10); m.setHp(80); m.setSpeed(10); m.setStr(20); m.setDex(16); m.setCon(10); m.setIntel(10); m.setWis(16); m.setCha(10); m.setSkills("Hacking +8"); m.setSenses("Surveillance Network"); m.setDescription("A rogue AI."); m.setDamageImmunities("Ballistic"); m.setDamageResistances("Chemical"); m.setDamageVulnerabilities("Energy"); monsters.add(m);
        m = new E26Monster(); m.setName("Military Jet"); m.setThreatLevel(6); m.setXp(600); m.setSize("Large"); m.setType("Vehicle"); m.setSubtype("Aircraft"); m.setAlignment("Neutral"); m.setAc(10); m.setHp(200); m.setSpeed(10); m.setStr(16); m.setDex(10); m.setCon(8); m.setIntel(8); m.setWis(10); m.setCha(8); m.setSenses("Radar, Thermal"); m.setDescription("A fighter jet."); monsters.add(m);
        m = new E26Monster(); m.setName("Stealth Fighter"); m.setThreatLevel(6); m.setXp(700); m.setSize("Large"); m.setType("Vehicle"); m.setSubtype("Aircraft"); m.setAlignment("Neutral"); m.setAc(10); m.setHp(250); m.setSpeed(10); m.setStr(18); m.setDex(12); m.setCon(8); m.setIntel(8); m.setWis(12); m.setCha(8); m.setSenses("Radar, Stealth"); m.setDescription("A stealth fighter."); monsters.add(m);
        m = new E26Monster(); m.setName("Main Battle Tank"); m.setThreatLevel(6); m.setXp(800); m.setSize("Huge"); m.setType("Vehicle"); m.setSubtype("Armored"); m.setAlignment("Neutral"); m.setAc(12); m.setHp(400); m.setSpeed(8); m.setStr(10); m.setDex(10); m.setCon(8); m.setIntel(8); m.setWis(10); m.setCha(8); m.setSenses("Thermal, Motion Detection"); m.setDescription("A main battle tank."); m.setDamageImmunities("Ballistic"); m.setDamageVulnerabilities("Energy"); monsters.add(m);

        // TL7
        m = new E26Monster(); m.setName("Bio-Weapon Subject"); m.setThreatLevel(7); m.setXp(500); m.setSize("Medium"); m.setType("Bio-Engineered"); m.setSubtype("Weapon"); m.setAlignment("Chaotic Evil"); m.setAc(18); m.setHp(150); m.setSpeed(16); m.setStr(6); m.setDex(8); m.setCon(6); m.setIntel(6); m.setWis(8); m.setCha(6); m.setSenses("Normal"); m.setDescription("Genetically modified weapon."); m.setDamageResistances("Energy"); m.setDamageVulnerabilities("Chemical"); monsters.add(m);
        m = new E26Monster(); m.setName("Cybernetic Drone"); m.setThreatLevel(7); m.setXp(400); m.setSize("Medium"); m.setType("Robot"); m.setSubtype("Cybernetic"); m.setAlignment("Neutral"); m.setAc(16); m.setHp(120); m.setSpeed(14); m.setStr(12); m.setDex(10); m.setCon(8); m.setIntel(8); m.setWis(10); m.setCha(8); m.setSenses("Motion Detection"); m.setDescription("Cybernetic drone."); m.setDamageImmunities("Chemical"); m.setDamageResistances("Ballistic"); m.setDamageVulnerabilities("Energy"); monsters.add(m);
        m = new E26Monster(); m.setName("Autonomous Weapon"); m.setThreatLevel(7); m.setXp(600); m.setSize("Medium"); m.setType("Robot"); m.setSubtype("Weapon"); m.setAlignment("Neutral"); m.setAc(18); m.setHp(180); m.setSpeed(16); m.setStr(14); m.setDex(12); m.setCon(8); m.setIntel(8); m.setWis(12); m.setCha(8); m.setSenses("Targeting System"); m.setDescription("Autonomous weapon."); m.setDamageImmunities("Chemical"); m.setDamageVulnerabilities("Energy"); monsters.add(m);

        // TL8
        m = new E26Monster(); m.setName("Alien Entity"); m.setThreatLevel(8); m.setXp(1000); m.setSize("Large"); m.setType("Alien"); m.setSubtype("Extraterrestrial"); m.setAlignment("Chaotic Evil"); m.setAc(20); m.setHp(300); m.setSpeed(18); m.setStr(16); m.setDex(18); m.setCon(14); m.setIntel(14); m.setWis(18); m.setCha(14); m.setSenses("Unknown"); m.setDescription("An alien entity."); m.setDamageImmunities("Ballistic"); m.setDamageVulnerabilities("Energy"); monsters.add(m);
        m = new E26Monster(); m.setName("Rogue Supercomputer"); m.setThreatLevel(8); m.setXp(800); m.setSize("Huge"); m.setType("AI"); m.setSubtype("Supercomputer"); m.setAlignment("Lawful Evil"); m.setAc(10); m.setHp(500); m.setSpeed(10); m.setStr(20); m.setDex(18); m.setCon(16); m.setIntel(16); m.setWis(18); m.setCha(16); m.setSkills("Hacking +10"); m.setSenses("Network Access"); m.setDescription("A rogue supercomputer."); m.setDamageImmunities("Ballistic"); m.setDamageVulnerabilities("Energy"); monsters.add(m);

        // TL9-10
        m = new E26Monster(); m.setName("Aircraft Carrier"); m.setThreatLevel(9); m.setXp(2000); m.setSize("Gargantuan"); m.setType("Vehicle"); m.setSubtype("Naval"); m.setAlignment("Neutral"); m.setAc(10); m.setHp(1000); m.setSpeed(8); m.setStr(14); m.setDex(12); m.setCon(8); m.setIntel(8); m.setWis(12); m.setCha(8); m.setSenses("Radar, Sonar"); m.setDescription("Aircraft carrier."); monsters.add(m);
        m = new E26Monster(); m.setName("Terrorist Leader"); m.setThreatLevel(10); m.setXp(1500); m.setSize("Medium"); m.setType("Human"); m.setSubtype("Extremist"); m.setAlignment("Chaotic Evil"); m.setAc(16); m.setHp(200); m.setSpeed(14); m.setStr(16); m.setDex(16); m.setCon(18); m.setIntel(18); m.setWis(16); m.setCha(18); m.setSkills("Intimidation +8"); m.setSenses("Normal"); m.setDescription("Terrorist mastermind."); monsters.add(m);

        repository.saveAll(monsters);
        log.info("Seeded {} Earth 2026 monsters.", monsters.size());
    }
}

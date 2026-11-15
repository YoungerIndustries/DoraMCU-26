package com.yisy.modules.doramcu.earth2026.data;

import com.yisy.modules.doramcu.earth2026.model.E26Npc;
import com.yisy.modules.doramcu.earth2026.repository.E26NpcRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Seeds the database with Earth 2026 near-future NPCs.
 */
@Component
public class Earth2026NpcDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(Earth2026NpcDataInitializer.class);

    private final E26NpcRepository repository;

    @Autowired
    public Earth2026NpcDataInitializer(E26NpcRepository repository) {
        this.repository = repository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        if (repository.count() > 0) {
            log.info("Earth 2026 NPCs already seeded, skipping.");
            return;
        }
        List<E26Npc> npcs = new ArrayList<>();

        // === Civilians ===
        npcs.add(createNpc("Sarah Chen", "Civilian", "Teacher", "High school teacher in downtown", 1, 1, "A dedicated teacher who cares about her students.", "Inspire the next generation", 20, 10, 500, "Friendly", false, false, "Downtown School"));
        npcs.add(createNpc("Mike Johnson", "Civilian", "Delivery Driver", "Works for a delivery company", 1, 1, "A friendly delivery driver who knows the city well.", "Provide for his family", 25, 10, 300, "Friendly", false, false, "Citywide"));
        npcs.add(createNpc("Elena Rodriguez", "Civilian", "Nurse", "Works at the city hospital", 1, 1, "A compassionate nurse working long shifts.", "Help patients recover", 30, 11, 800, "Helpful", false, false, "City Hospital"));
        npcs.add(createNpc("Tom Williams", "Civilian", "Construction Worker", "Builds skyscrapers downtown", 2, 1, "A hardworking construction worker.", "Save for a house", 35, 12, 600, "Indifferent", false, false, "Construction Sites"));
        npcs.add(createNpc("Lisa Park", "Civilian", "Barista", "Runs a popular coffee shop", 1, 1, "A cheerful barista who remembers regulars' orders.", "Open her own cafe", 15, 10, 400, "Friendly", false, false, "Downtown Coffee Shop"));

        // === Professionals ===
        npcs.add(createNpc("Dr. James Wilson", "Professional", "Surgeon", "Chief surgeon at the city hospital", 3, 2, "A brilliant surgeon with a steady hand.", "Advance medical science", 25, 12, 5000, "Friendly", false, false, "City Hospital"));
        npcs.add(createNpc("Attorney Maria Garcia", "Professional", "Lawyer", "Defense attorney specializing in criminal law", 3, 2, "A sharp lawyer who fights for justice.", "Defend the innocent", 20, 11, 8000, "Indifferent", false, false, "Downtown Law Firm"));
        npcs.add(createNpc("Prof. Alan Turing Jr.", "Professional", "Professor", "Computer science professor at the university", 4, 3, "A renowned AI researcher.", "Push the boundaries of AI", 18, 11, 3000, "Helpful", false, false, "University"));
        npcs.add(createNpc("Dr. Yuki Tanaka", "Professional", "Researcher", "Leads a biotech research team", 4, 3, "A brilliant scientist working on gene therapy.", "Cure genetic diseases", 20, 11, 4000, "Friendly", false, false, "Biotech Lab"));

        // === Law Enforcement ===
        npcs.add(createNpc("Det. Jack Reacher", "Law Enforcement", "Detective", "Homicide detective with 15 years experience", 4, 3, "A seasoned detective who never gives up.", "Solve the case", 45, 14, 2000, "Indifferent", false, false, "Police Precinct"));
        npcs.add(createNpc("Officer Amy Smith", "Law Enforcement", "Police Officer", "Beat cop in the downtown district", 2, 2, "A dedicated officer who knows her neighborhood.", "Keep the peace", 35, 13, 1500, "Friendly", false, false, "Downtown Precinct"));
        npcs.add(createNpc("Capt. Robert Stone", "Law Enforcement", "Police Captain", "Commands the SWAT team", 5, 4, "A tough captain who leads from the front.", "Protect and serve", 60, 16, 3000, "Indifferent", false, false, "Police HQ"));
        npcs.add(createNpc("Agent Sarah Connor", "Law Enforcement", "FBI Agent", "Special agent in the cybercrime division", 5, 4, "A sharp agent tracking cybercriminals.", "Bring down cybercrime", 50, 15, 5000, "Indifferent", false, false, "FBI Field Office"));

        // === Military ===
        npcs.add(createNpc("Sgt. John Miller", "Military", "Special Forces", "Retired special forces operator", 5, 4, "A veteran with combat experience worldwide.", "Train the next generation", 65, 16, 3000, "Friendly", false, false, "Military Base"));
        npcs.add(createNpc("Col. Patricia Hayes", "Military", "Military Commander", "Commands a military installation", 6, 5, "A decorated officer with strategic mind.", "National security", 55, 15, 8000, "Indifferent", false, false, "Military HQ"));
        npcs.add(createNpc("Lt. David Kim", "Military", "Pilot", "F-35 fighter pilot", 5, 4, "An ace pilot with hundreds of flight hours.", "Defend the nation", 40, 14, 6000, "Friendly", false, false, "Air Force Base"));

        // === Criminals ===
        npcs.add(createNpc("Viktor Volkov", "Criminal", "Crime Boss", "Head of a criminal syndicate", 7, 6, "A ruthless crime boss who controls the underworld.", "Expand his empire", 80, 16, 50000, "Hostile", true, false, "Underworld"));
        npcs.add(createNpc("Zero Cool", "Criminal", "Hacker", "Notorious hacker known for bank heists", 6, 5, "A brilliant hacker who can breach any system.", "Hack the planet", 25, 12, 20000, "Hostile", true, false, "Dark Web"));
        npcs.add(createNpc("Razor", "Criminal", "Hitman", "Professional assassin for hire", 6, 5, "A cold-blooded killer with no traces.", "Complete the contract", 55, 15, 15000, "Hostile", true, false, "Unknown"));
        npcs.add(createNpc("Snake", "Criminal", "Smuggler", "Moves contraband across borders", 4, 3, "A resourceful smuggler with connections everywhere.", "Make money", 40, 13, 8000, "Unfriendly", true, false, "Border Regions"));

        // === Corporate ===
        npcs.add(createNpc("CEO Richard Sterling", "Corporate", "CEO", "CEO of a mega-corporation", 6, 5, "A powerful CEO who runs a global empire.", "Maximize shareholder value", 30, 12, 100000, "Indifferent", false, false, "Corporate HQ"));
        npcs.add(createNpc("VP Jennifer Walsh", "Corporate", "VP of Security", "Head of corporate security", 5, 4, "A former military officer now protecting corporate assets.", "Protect company interests", 50, 15, 25000, "Indifferent", false, false, "Corporate HQ"));
        npcs.add(createNpc("Marcus Chen", "Corporate", "Corporate Spy", "Industrial espionage specialist", 5, 4, "A spy who steals trade secrets for corporations.", "Advance career", 35, 13, 15000, "Unfriendly", true, false, "Corporate World"));

        // === Government ===
        npcs.add(createNpc("Senator Elizabeth Warren Jr.", "Government", "Politician", "Senator advocating for tech regulation", 5, 4, "A politician fighting for citizens' rights.", "Pass meaningful legislation", 25, 12, 10000, "Friendly", false, false, "Capitol"));
        npcs.add(createNpc("Dir. William Carter", "Government", "Intelligence Director", "Head of a national intelligence agency", 7, 6, "A cunning director who knows all the secrets.", "National security", 40, 14, 50000, "Indifferent", false, false, "Intelligence HQ"));
        npcs.add(createNpc("Amb. Sofia Petrov", "Government", "Diplomat", "Ambassador to international relations", 5, 4, "A skilled diplomat navigating global politics.", "Maintain peace", 25, 12, 20000, "Friendly", false, false, "Embassy"));

        // === Scientists ===
        npcs.add(createNpc("Dr. Alan Grant", "Scientist", "Paleontologist", "Studies ancient DNA for biotech applications", 4, 3, "A curious scientist bridging past and future.", "Understand evolution", 20, 11, 3000, "Helpful", false, false, "Research Lab"));
        npcs.add(createNpc("Dr. Emma Watson", "Scientist", "AI Researcher", "Works on ethical AI development", 5, 4, "A researcher ensuring AI benefits humanity.", "Create safe AI", 18, 11, 5000, "Helpful", false, false, "AI Lab"));
        npcs.add(createNpc("Dr. Raj Patel", "Scientist", "Engineer", "Designs next-gen renewable energy systems", 4, 3, "An engineer working to solve the energy crisis.", "Clean energy for all", 22, 12, 4000, "Friendly", false, false, "Energy Lab"));

        // === Media ===
        npcs.add(createNpc("Journalist Kate Moore", "Media", "Investigative Journalist", "Uncovers corruption and scandals", 4, 3, "A tenacious journalist who exposes the truth.", "Reveal the truth", 25, 12, 3000, "Friendly", false, false, "Newsroom"));
        npcs.add(createNpc("Anchor Bob Thompson", "Media", "News Anchor", "Popular evening news anchor", 3, 2, "A trusted voice delivering news to millions.", "Inform the public", 20, 11, 8000, "Indifferent", false, false, "TV Station"));
        npcs.add(createNpc("Influencer @TechGuru", "Media", "Social Media Influencer", "Tech reviewer with millions of followers", 3, 2, "A popular influencer shaping public opinion.", "Grow audience", 15, 10, 20000, "Friendly", false, false, "Online"));

        // === Allies ===
        npcs.add(createNpc("Dr. Helen Chen", "Professional", "Doctor", "A trusted doctor who treats the party", 3, 2, "A kind doctor who always helps those in need.", "Heal the wounded", 30, 12, 5000, "Helpful", false, true, "Clinic"));
        npcs.add(createNpc("Informant 'Rat'", "Criminal", "Informant", "Sells information to the highest bidder", 3, 2, "A street-smart informant with valuable contacts.", "Survive", 20, 11, 2000, "Friendly", false, true, "Underground"));
        npcs.add(createNpc("Mechanic 'Wrench'", "Professional", "Mechanic", "Can fix any vehicle or gadget", 3, 2, "A skilled mechanic who owes the party a favor.", "Pay back debts", 25, 12, 3000, "Helpful", false, true, "Garage"));

        repository.saveAll(npcs);
        log.info("Seeded {} Earth 2026 NPCs.", npcs.size());
    }

    private E26Npc createNpc(String name, String npcType, String occupation, String background,
                              int level, int threatLevel, String description, String motive,
                              int hitPoints, int armorClass, int cash, String disposition,
                              boolean isVillain, boolean isAlly, String location) {
        E26Npc npc = new E26Npc();
        npc.setName(name);
        npc.setNpcType(npcType);
        npc.setOccupation(occupation);
        npc.setBackground(background);
        npc.setLevel(level);
        npc.setThreatLevel(threatLevel);
        npc.setDescription(description);
        npc.setMotive(motive);
        npc.setHitPoints(hitPoints);
        npc.setArmorClass(armorClass);
        npc.setCash(cash);
        npc.setDisposition(disposition);
        npc.setVillain(isVillain);
        npc.setAlly(isAlly);
        npc.setLocation(location);
        return npc;
    }
}

package com.yisy.modules.doramcu.earth2026.controller;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Connection;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Organization;
import com.yisy.modules.doramcu.earth2026.model.Earth2026OrgMember;
import com.yisy.modules.doramcu.earth2026.service.Earth2026OrganizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for Earth 2026 Organizations.
 * <p>
 * Provides endpoints for organization CRUD, member management, loyalty operations,
 * and hierarchy management.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/earth2026/organizations")
public class Earth2026OrganizationController {

    private final Earth2026OrganizationService organizationService;

    public Earth2026OrganizationController(Earth2026OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    // --- Organization CRUD Endpoints ---

    @GetMapping
    public List<Earth2026Organization> getAllOrganizations() {
        return organizationService.findActiveOrganizations();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Earth2026Organization> getOrganization(@PathVariable Long id) {
        return organizationService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search/{name}")
    public List<Earth2026Organization> searchOrganizations(@PathVariable String name) {
        return organizationService.findByName(name);
    }

    @GetMapping("/type/{orgType}")
    public List<Earth2026Organization> getByType(@PathVariable String orgType) {
        return organizationService.findActiveByType(
                Earth2026Organization.OrganizationType.valueOf(orgType));
    }

    @PostMapping
    public ResponseEntity<?> createOrganization(@RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            String orgType = (String) request.get("orgType");

            if (name == null || name.trim().isEmpty() || orgType == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "name and orgType are required"));
            }

            Earth2026Organization organization = new Earth2026Organization(
                    name.trim(), Earth2026Organization.OrganizationType.valueOf(orgType));

            if (request.containsKey("description")) {
                organization.setDescription((String) request.get("description"));
            }
            if (request.containsKey("leaderType")) {
                organization.setLeaderType(Earth2026Connection.EntityType.valueOf((String) request.get("leaderType")));
            }
            if (request.containsKey("leaderId")) {
                organization.setLeaderId(Long.valueOf(request.get("leaderId").toString()));
            }
            if (request.containsKey("hierarchyLevel")) {
                organization.setHierarchyLevel(Integer.valueOf(request.get("hierarchyLevel").toString()));
            }
            if (request.containsKey("reputation")) {
                organization.setReputation(Integer.valueOf(request.get("reputation").toString()));
            }
            if (request.containsKey("wealth")) {
                organization.setWealth(Integer.valueOf(request.get("wealth").toString()));
            }
            if (request.containsKey("secrecyLevel")) {
                organization.setSecrecyLevel(Integer.valueOf(request.get("secrecyLevel").toString()));
            }
            if (request.containsKey("homeStructureId")) {
                organization.setHomeStructureId(Long.valueOf(request.get("homeStructureId").toString()));
            }

            Earth2026Organization saved = organizationService.saveOrganization(organization);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid enum value: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrganization(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Optional<Earth2026Organization> optional = organizationService.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Earth2026Organization organization = optional.get();

        if (request.containsKey("name")) {
            organization.setName((String) request.get("name"));
        }
        if (request.containsKey("description")) {
            organization.setDescription((String) request.get("description"));
        }
        if (request.containsKey("orgType")) {
            organization.setOrgType(Earth2026Organization.OrganizationType.valueOf((String) request.get("orgType")));
        }
        if (request.containsKey("leaderType")) {
            organization.setLeaderType(Earth2026Connection.EntityType.valueOf((String) request.get("leaderType")));
        }
        if (request.containsKey("leaderId")) {
            organization.setLeaderId(Long.valueOf(request.get("leaderId").toString()));
        }
        if (request.containsKey("hierarchyLevel")) {
            organization.setHierarchyLevel(Integer.valueOf(request.get("hierarchyLevel").toString()));
        }
        if (request.containsKey("reputation")) {
            organization.setReputation(Integer.valueOf(request.get("reputation").toString()));
        }
        if (request.containsKey("wealth")) {
            organization.setWealth(Integer.valueOf(request.get("wealth").toString()));
        }
        if (request.containsKey("secrecyLevel")) {
            organization.setSecrecyLevel(Integer.valueOf(request.get("secrecyLevel").toString()));
        }
        if (request.containsKey("homeStructureId")) {
            organization.setHomeStructureId(Long.valueOf(request.get("homeStructureId").toString()));
        }
        if (request.containsKey("active")) {
            organization.setActive((Boolean) request.get("active"));
        }

        return ResponseEntity.ok(organizationService.saveOrganization(organization));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteOrganization(@PathVariable Long id) {
        organizationService.deleteOrganization(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Organization deleted"));
    }

    // --- Member Management Endpoints ---

    @GetMapping("/{id}/members")
    public List<Earth2026OrgMember> getMembers(@PathVariable Long id) {
        return organizationService.findActiveMembersByOrganizationId(id);
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<?> addMember(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            String memberType = (String) request.get("memberType");
            String memberIdStr = request.get("memberId").toString();
            String role = (String) request.get("role");

            if (memberType == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "memberType is required"));
            }

            Earth2026Connection.EntityType mType = Earth2026Connection.EntityType.valueOf(memberType);
            Long mId = Long.valueOf(memberIdStr);

            Optional<Earth2026OrgMember> member = organizationService.addMember(id, mType, mId, role);
            return member.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid enum value: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/members/{memberId}")
    public ResponseEntity<Map<String, Object>> removeMember(@PathVariable Long memberId) {
        boolean removed = organizationService.removeMember(memberId);
        if (removed) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Member removed"));
        }
        return ResponseEntity.notFound().build();
    }

    // --- Loyalty Management Endpoints ---

    @PatchMapping("/members/{memberId}/loyalty/increase")
    public ResponseEntity<?> increaseLoyalty(@PathVariable Long memberId, @RequestParam int amount) {
        return organizationService.increaseLoyalty(memberId, amount)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/members/{memberId}/loyalty/decrease")
    public ResponseEntity<?> decreaseLoyalty(@PathVariable Long memberId, @RequestParam int amount) {
        return organizationService.decreaseLoyalty(memberId, amount)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- Hierarchy Endpoints ---

    @PatchMapping("/members/{memberId}/promote")
    public ResponseEntity<?> promoteMember(@PathVariable Long memberId, @RequestParam int levels) {
        return organizationService.promoteMember(memberId, levels)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/members/{memberId}/demote")
    public ResponseEntity<?> demoteMember(@PathVariable Long memberId, @RequestParam int levels) {
        return organizationService.demoteMember(memberId, levels)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- Organization Events ---

    @PostMapping("/{id}/meeting")
    public ResponseEntity<?> organizationMeeting(@PathVariable Long id, @RequestParam int loyaltyIncrease) {
        return organizationService.organizationMeeting(id, loyaltyIncrease)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- Query Endpoints ---

    @GetMapping("/{id}/member-count")
    public Map<String, Object> getMemberCount(@PathVariable Long id) {
        int count = organizationService.getActiveMemberCount(id);
        return Map.of("organizationId", id, "activeMemberCount", count);
    }

    @GetMapping("/entity/{memberType}/{memberId}")
    public List<Earth2026OrgMember> getMemberships(@PathVariable String memberType, @PathVariable Long memberId) {
        return organizationService.findActiveMembersByEntity(
                Earth2026Connection.EntityType.valueOf(memberType), memberId);
    }
}

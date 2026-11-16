package com.yisy.modules.doramcu.corporation.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity linking a CorporateHiring record to a Crew group.
 * <p>
 * Each crew member has a join date and a leader flag indicating
 * whether they are the crew leader.
 * </p>
 */
@Entity
@Table(name = "crew_member", indexes = {
        @Index(name = "idx_crew_member_crew", columnList = "crew_id"),
        @Index(name = "idx_crew_member_hiring", columnList = "hiring_id")
})
public class CrewMember {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crew_id", nullable = false)
    private Crew crew;

    @Column(name = "hiring_id", nullable = false)
    private Long hiringId;

    @Column(name = "is_leader", nullable = false)
    private Boolean isLeader = false;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    public CrewMember() {
    }

    public CrewMember(Crew crew, Long hiringId, boolean isLeader) {
        this.crew = crew;
        this.hiringId = hiringId;
        this.isLeader = isLeader;
        this.joinedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Crew getCrew() {
        return crew;
    }

    public void setCrew(Crew crew) {
        this.crew = crew;
    }

    public Long getHiringId() {
        return hiringId;
    }

    public void setHiringId(Long hiringId) {
        this.hiringId = hiringId;
    }

    public Boolean getIsLeader() {
        return isLeader;
    }

    public void setIsLeader(Boolean isLeader) {
        this.isLeader = isLeader;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    @Override
    public String toString() {
        return "CrewMember{" +
                "id=" + id +
                ", hiringId=" + hiringId +
                ", isLeader=" + isLeader +
                '}';
    }
}

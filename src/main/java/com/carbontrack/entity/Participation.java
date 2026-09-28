package com.carbontrack.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "participations",
       uniqueConstraints = @UniqueConstraint(name = "uk_participation_household_challenge",
               columnNames = {"household_id", "challenge_id"}))
public class Participation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @PrePersist
    protected void onJoin() { this.joinedAt = LocalDateTime.now(); }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ParticipationStatus status = ParticipationStatus.ACTIVE;

    public Participation() {}
    public Participation(Household household, Challenge challenge) {
        this.household = household;
        this.challenge = challenge;
    }

    public Long getId() { return id; }
    public Household getHousehold() { return household; }
    public Challenge getChallenge() { return challenge; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
    public ParticipationStatus getStatus() { return status; }
    public void setStatus(ParticipationStatus status) { this.status = status; }
}

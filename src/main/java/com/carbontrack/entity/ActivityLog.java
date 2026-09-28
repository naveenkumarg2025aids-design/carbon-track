package com.carbontrack.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "activity_logs")
public class ActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @NotNull
    @Column(nullable = false)
    private LocalDate activityDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommuteMode commuteMode;

    @NotNull
    @DecimalMin("0.0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal commuteDistanceKm = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal electricityKwh = BigDecimal.ZERO;

    @NotNull
    @DecimalMin("0.0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal wasteKg = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal estimatedKgCo2e = BigDecimal.ZERO;

    public ActivityLog() {}

    public Long getId() { return id; }
    public Household getHousehold() { return household; }
    public LocalDate getActivityDate() { return activityDate; }
    public CommuteMode getCommuteMode() { return commuteMode; }
    public BigDecimal getCommuteDistanceKm() { return commuteDistanceKm; }
    public BigDecimal getElectricityKwh() { return electricityKwh; }
    public BigDecimal getWasteKg() { return wasteKg; }
    public BigDecimal getEstimatedKgCo2e() { return estimatedKgCo2e; }

    public void setHousehold(Household household) { this.household = household; }
    public void setActivityDate(LocalDate activityDate) { this.activityDate = activityDate; }
    public void setCommuteMode(CommuteMode commuteMode) { this.commuteMode = commuteMode; }
    public void setCommuteDistanceKm(BigDecimal commuteDistanceKm) { this.commuteDistanceKm = commuteDistanceKm; }
    public void setElectricityKwh(BigDecimal electricityKwh) { this.electricityKwh = electricityKwh; }
    public void setWasteKg(BigDecimal wasteKg) { this.wasteKg = wasteKg; }
    public void setEstimatedKgCo2e(BigDecimal estimatedKgCo2e) { this.estimatedKgCo2e = estimatedKgCo2e; }
}

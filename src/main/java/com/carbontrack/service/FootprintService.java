package com.carbontrack.service;

import com.carbontrack.dto.*;
import com.carbontrack.entity.*;
import com.carbontrack.exception.BadRequestException;
import com.carbontrack.exception.ResourceNotFoundException;
import com.carbontrack.repository.ActivityLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class FootprintService {
    // Fixed illustrative project factors in kg CO2e per unit. See README for assumptions.
    private static final Map<CommuteMode, BigDecimal> COMMUTE_FACTORS = Map.of(
            CommuteMode.NONE, BigDecimal.ZERO,
            CommuteMode.WALKING, BigDecimal.ZERO,
            CommuteMode.CYCLING, BigDecimal.ZERO,
            CommuteMode.BUS, new BigDecimal("0.089"),
            CommuteMode.TRAIN, new BigDecimal("0.041"),
            CommuteMode.CAR, new BigDecimal("0.192"),
            CommuteMode.MOTORCYCLE, new BigDecimal("0.103")
    );
    private static final BigDecimal ELECTRICITY_FACTOR = new BigDecimal("0.716");
    private static final BigDecimal WASTE_FACTOR = new BigDecimal("0.570");

    private final ActivityLogRepository logs;
    private final HouseholdService households;

    public FootprintService(ActivityLogRepository logs, HouseholdService households) {
        this.logs = logs;
        this.households = households;
    }

    public ActivityLogResponse create(ActivityLogRequest request) {
        Household household = households.getEntity(request.householdId());
        if (request.commuteMode() == CommuteMode.NONE && request.commuteDistanceKm().signum() > 0) {
            throw new BadRequestException("Commute distance must be zero when commute mode is NONE.");
        }
        if (request.commuteMode() == CommuteMode.WALKING || request.commuteMode() == CommuteMode.CYCLING) {
            // Distance is retained for activity reporting; these modes have zero direct emissions.
        }
        if (request.commuteDistanceKm().signum() == 0 &&
                request.electricityKwh().signum() == 0 && request.wasteKg().signum() == 0) {
            throw new BadRequestException("At least one activity value must be greater than zero.");
        }

        BigDecimal commute = COMMUTE_FACTORS.get(request.commuteMode()).multiply(request.commuteDistanceKm());
        BigDecimal electricity = ELECTRICITY_FACTOR.multiply(request.electricityKwh());
        BigDecimal waste = WASTE_FACTOR.multiply(request.wasteKg());
        BigDecimal total = commute.add(electricity).add(waste).setScale(4, RoundingMode.HALF_UP);

        ActivityLog log = new ActivityLog();
        log.setHousehold(household);
        log.setActivityDate(request.activityDate());
        log.setCommuteMode(request.commuteMode());
        log.setCommuteDistanceKm(request.commuteDistanceKm());
        log.setElectricityKwh(request.electricityKwh());
        log.setWasteKg(request.wasteKg());
        log.setEstimatedKgCo2e(total);
        return toResponse(logs.save(log));
    }

    @Transactional(readOnly = true)
    public List<ActivityLogResponse> list(Long householdId) {
        households.getEntity(householdId);
        return logs.findByHouseholdIdOrderByActivityDateDesc(householdId).stream().map(this::toResponse).toList();
    }

    public void delete(Long id) {
        ActivityLog log = logs.findById(id).orElseThrow(() -> new ResourceNotFoundException("Activity log " + id + " not found."));
        logs.delete(log);
    }

    @Transactional(readOnly = true)
    public MonthlyFootprintResponse monthly(Long householdId, int year, int month) {
        households.getEntity(householdId);
        YearMonth ym = validMonth(year, month);
        List<ActivityLog> data = logs.findByHouseholdIdAndActivityDateBetween(
                householdId, ym.atDay(1), ym.atEndOfMonth());
        BigDecimal total = data.stream().map(ActivityLog::getEstimatedKgCo2e)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(4, RoundingMode.HALF_UP);
        return new MonthlyFootprintResponse(householdId, year, month, total, data.size());
    }

    @Transactional(readOnly = true)
    public BigDecimal householdTotal(Long householdId, YearMonth month) {
        return logs.findByHouseholdIdAndActivityDateBetween(householdId, month.atDay(1), month.atEndOfMonth())
                .stream().map(ActivityLog::getEstimatedKgCo2e)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private YearMonth validMonth(int year, int month) {
        try { return YearMonth.of(year, month); }
        catch (Exception ex) { throw new BadRequestException("Year/month is invalid."); }
    }

    private ActivityLogResponse toResponse(ActivityLog l) {
        return new ActivityLogResponse(l.getId(), l.getHousehold().getId(), l.getActivityDate(),
                l.getCommuteMode(), l.getCommuteDistanceKm(), l.getElectricityKwh(),
                l.getWasteKg(), l.getEstimatedKgCo2e());
    }
}

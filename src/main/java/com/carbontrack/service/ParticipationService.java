
package com.carbontrack.service;

import com.carbontrack.dto.*;
import com.carbontrack.entity.*;
import com.carbontrack.exception.BadRequestException;
import com.carbontrack.repository.ParticipationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class ParticipationService {

    private final ParticipationRepository participations;
    private final HouseholdService households;
    private final ChallengeService challenges;
    private final FootprintService footprints;

    public ParticipationService(
            ParticipationRepository participations,
            HouseholdService households,
            ChallengeService challenges,
            FootprintService footprints) {
        this.participations = participations;
        this.households = households;
        this.challenges = challenges;
        this.footprints = footprints;
    }

    public ParticipationResponse join(Long challengeId, Long householdId) {
        Challenge challenge = challenges.getEntity(challengeId);
        Household household = households.getEntity(householdId);

        LocalDate today = LocalDate.now();

        if (!challenge.isActive()) {
            throw new BadRequestException("This challenge is inactive.");
        }

        if (today.isBefore(challenge.getStartDate())
                || today.isAfter(challenge.getEndDate())) {
            throw new BadRequestException(
                    "Household can join only during the challenge date range.");
        }

        if (participations.existsByHouseholdIdAndChallengeId(
                householdId, challengeId)) {
            throw new BadRequestException(
                    "Household has already joined this challenge.");
        }

        Participation participation =
                new Participation(household, challenge);

        return toResponse(participations.save(participation));
    }

    @Transactional(readOnly = true)
    public List<ParticipationResponse> list(Long challengeId) {
        challenges.getEntity(challengeId);

        return participations
                .findByChallengeIdOrderByJoinedAtAsc(challengeId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ChallengeProgressResponse progress(
            Long challengeId, Long householdId) {

        Challenge challenge = challenges.getEntity(challengeId);
        households.getEntity(householdId);

        if (!participations.existsByHouseholdIdAndChallengeId(
                householdId, challengeId)) {
            throw new BadRequestException(
                    "Household has not joined this challenge.");
        }

        YearMonth current = YearMonth.from(LocalDate.now());

        BigDecimal previous = footprints.householdTotal(
                householdId, current.minusMonths(1));

        BigDecimal now = footprints.householdTotal(
                householdId, current);

        BigDecimal reduction = reduction(previous, now);

        return new ChallengeProgressResponse(
                challengeId,
                householdId,
                current.getYear(),
                current.getMonthValue(),
                previous.setScale(4, RoundingMode.HALF_UP),
                now.setScale(4, RoundingMode.HALF_UP),
                reduction,
                challenge.getTargetReductionPercent(),
                reduction.compareTo(
                        challenge.getTargetReductionPercent()) >= 0
        );
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntryResponse> leaderboard(
            int year, int month) {

        YearMonth current;

        try {
            current = YearMonth.of(year, month);
        } catch (Exception ex) {
            throw new BadRequestException("Year/month is invalid.");
        }

        YearMonth previous = current.minusMonths(1);

        List<LeaderboardEntryResponse> entries =
                households.findAll().stream()
                        .map(h -> {
                            BigDecimal before =
                                    footprints.householdTotal(
                                            h.id(), previous);

                            BigDecimal now =
                                    footprints.householdTotal(
                                            h.id(), current);

                            return new LeaderboardEntryResponse(
                                    0,
                                    h.id(),
                                    h.name(),
                                    before.setScale(
                                            4, RoundingMode.HALF_UP),
                                    now.setScale(
                                            4, RoundingMode.HALF_UP),
                                    reduction(before, now)
                            );
                        })
                        .sorted(
                                Comparator.comparing(
                                                LeaderboardEntryResponse
                                                        ::reductionPercent
                                        ).reversed()
                                        .thenComparing(
                                                LeaderboardEntryResponse
                                                        ::householdName,
                                                String.CASE_INSENSITIVE_ORDER
                                        )
                        )
                        .toList();

        List<LeaderboardEntryResponse> ranked =
                new ArrayList<>();

        for (int i = 0; i < entries.size(); i++) {
            LeaderboardEntryResponse entry = entries.get(i);

            ranked.add(new LeaderboardEntryResponse(
                    i + 1,
                    entry.householdId(),
                    entry.householdName(),
                    entry.previousMonthKgCo2e(),
                    entry.currentMonthKgCo2e(),
                    entry.reductionPercent()
            ));
        }

        return ranked;
    }

    private BigDecimal reduction(
            BigDecimal previous, BigDecimal current) {

        if (previous == null || previous.signum() <= 0) {
            return BigDecimal.ZERO.setScale(
                    2, RoundingMode.HALF_UP);
        }

        if (current == null) {
            current = BigDecimal.ZERO;
        }

        return previous.subtract(current)
                .multiply(new BigDecimal("100"))
                .divide(previous, 2, RoundingMode.HALF_UP);
    }

    private ParticipationResponse toResponse(Participation p) {
        return new ParticipationResponse(
                p.getId(),
                p.getHousehold().getId(),
                p.getChallenge().getId(),
                p.getJoinedAt(),
                p.getStatus()
        );
    }
}
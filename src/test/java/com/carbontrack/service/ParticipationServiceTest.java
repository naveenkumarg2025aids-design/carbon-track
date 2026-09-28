package com.carbontrack.service;

import com.carbontrack.dto.ChallengeProgressResponse;
import com.carbontrack.dto.HouseholdResponse;
import com.carbontrack.dto.LeaderboardEntryResponse;
import com.carbontrack.entity.Challenge;
import com.carbontrack.entity.Household;
import com.carbontrack.entity.Participation;
import com.carbontrack.repository.ParticipationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParticipationServiceTest {

    @Mock private ParticipationRepository participations;
    @Mock private HouseholdService householdsService;
    @Mock private ChallengeService challengesService;
    @Mock private FootprintService footprints;

    @InjectMocks
    private ParticipationService service;

    private Challenge challenge;
    private Household household;

    @BeforeEach
    void setUp() {
        challenge = new Challenge();
        challenge.setTitle("Test Challenge");
        challenge.setStartDate(LocalDate.now().minusDays(10));
        challenge.setEndDate(LocalDate.now().plusDays(20));
        challenge.setTargetReductionPercent(new BigDecimal("10.00"));
        challenge.setActive(true);

        household = new Household("Test Home", "test@example.com", "Location");
    }

    // ──────────────────────────── Reduction % tests ────────────────────────────

    @Test
    @DisplayName("R1: 100→80 = 20.00% reduction")
    void reductionNormalCase() {
        setupProgressMocks(new BigDecimal("100.00"), new BigDecimal("80.00"));
        ChallengeProgressResponse resp = service.progress(1L, 1L);
        assertEquals(new BigDecimal("20.00"), resp.reductionPercent());
    }

    @Test
    @DisplayName("R2: 100→120 = -20.00% reduction (emissions increased)")
    void reductionNegative() {
        setupProgressMocks(new BigDecimal("100.00"), new BigDecimal("120.00"));
        ChallengeProgressResponse resp = service.progress(1L, 1L);
        assertEquals(new BigDecimal("-20.00"), resp.reductionPercent());
    }

    @Test
    @DisplayName("R3: 0→50 = 0.00% (previous zero, avoid division by zero)")
    void reductionPreviousZero() {
        setupProgressMocks(BigDecimal.ZERO, new BigDecimal("50.00"));
        ChallengeProgressResponse resp = service.progress(1L, 1L);
        assertEquals(new BigDecimal("0.00"), resp.reductionPercent());
    }

    @Test
    @DisplayName("R4: 50→0 = 100.00% reduction")
    void reductionFullReduction() {
        setupProgressMocks(new BigDecimal("50.00"), BigDecimal.ZERO);
        ChallengeProgressResponse resp = service.progress(1L, 1L);
        assertEquals(new BigDecimal("100.00"), resp.reductionPercent());
    }

    @Test
    @DisplayName("R5: 0→0 = 0.00% (both zero)")
    void reductionBothZero() {
        setupProgressMocks(BigDecimal.ZERO, BigDecimal.ZERO);
        ChallengeProgressResponse resp = service.progress(1L, 1L);
        assertEquals(new BigDecimal("0.00"), resp.reductionPercent());
    }

    @Test
    @DisplayName("Target reached when reduction >= target")
    void targetReached() {
        setupProgressMocks(new BigDecimal("100.00"), new BigDecimal("85.00"));
        ChallengeProgressResponse resp = service.progress(1L, 1L);
        // 15% reduction >= 10% target
        assertTrue(resp.targetReached());
    }

    @Test
    @DisplayName("Target not reached when reduction < target")
    void targetNotReached() {
        setupProgressMocks(new BigDecimal("100.00"), new BigDecimal("95.00"));
        ChallengeProgressResponse resp = service.progress(1L, 1L);
        // 5% reduction < 10% target
        assertFalse(resp.targetReached());
    }

    // ──────────────────────────── Leaderboard tests ────────────────────────────

    @Test
    @DisplayName("Leaderboard ranks households by reduction % descending")
    void leaderboardRanking() {
        YearMonth current = YearMonth.now();
        YearMonth previous = current.minusMonths(1);

        when(householdsService.findAll()).thenReturn(List.of(
                new HouseholdResponse(1L, "Home A", null, null, null),
                new HouseholdResponse(2L, "Home B", null, null, null)
        ));
        // Home A: 100→80 = 20% reduction
        when(footprints.householdTotal(eq(1L), eq(previous))).thenReturn(new BigDecimal("100"));
        when(footprints.householdTotal(eq(1L), eq(current))).thenReturn(new BigDecimal("80"));
        // Home B: 100→60 = 40% reduction
        when(footprints.householdTotal(eq(2L), eq(previous))).thenReturn(new BigDecimal("100"));
        when(footprints.householdTotal(eq(2L), eq(current))).thenReturn(new BigDecimal("60"));

        List<LeaderboardEntryResponse> board = service.leaderboard(current.getYear(), current.getMonthValue());

        assertEquals(2, board.size());
        assertEquals(1, board.get(0).rank());
        assertEquals("Home B", board.get(0).householdName());
        assertEquals(new BigDecimal("40.00"), board.get(0).reductionPercent());
        assertEquals(2, board.get(1).rank());
        assertEquals("Home A", board.get(1).householdName());
        assertEquals(new BigDecimal("20.00"), board.get(1).reductionPercent());
    }

    // ──────────────────────────── Helper ────────────────────────────

    private void setupProgressMocks(BigDecimal previous, BigDecimal current) {
        when(challengesService.getEntity(anyLong())).thenReturn(challenge);
        when(householdsService.getEntity(anyLong())).thenReturn(household);
        when(participations.existsByHouseholdIdAndChallengeId(anyLong(), anyLong())).thenReturn(true);

        YearMonth now = YearMonth.from(LocalDate.now());
        when(footprints.householdTotal(anyLong(), eq(now.minusMonths(1)))).thenReturn(previous);
        when(footprints.householdTotal(anyLong(), eq(now))).thenReturn(current);
    }
}

package com.carbontrack.service;

import com.carbontrack.dto.ChallengeRequest;
import com.carbontrack.dto.ChallengeResponse;
import com.carbontrack.entity.Challenge;
import com.carbontrack.exception.BadRequestException;
import com.carbontrack.exception.ResourceNotFoundException;
import com.carbontrack.repository.ChallengeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChallengeServiceTest {

    @Mock private ChallengeRepository repository;
    @InjectMocks private ChallengeService service;

    private final LocalDate start = LocalDate.of(2026, 9, 1);
    private final LocalDate end = LocalDate.of(2026, 9, 30);

    @Test
    @DisplayName("Create challenge with valid dates succeeds")
    void createSuccess() {
        when(repository.save(any(Challenge.class))).thenAnswer(inv -> inv.getArgument(0));

        ChallengeRequest req = new ChallengeRequest("Test", "Desc", start, end, new BigDecimal("10"), true);
        ChallengeResponse resp = service.create(req);

        assertEquals("Test", resp.title());
        assertTrue(resp.active());
    }

    @Test
    @DisplayName("Create challenge with endDate before startDate is rejected")
    void createInvalidDatesRejected() {
        ChallengeRequest req = new ChallengeRequest("Test", null, end, start, new BigDecimal("10"), null);
        assertThrows(BadRequestException.class, () -> service.create(req));
    }

    @Test
    @DisplayName("Create challenge defaults active to true when null")
    void createDefaultsActiveTrue() {
        when(repository.save(any(Challenge.class))).thenAnswer(inv -> inv.getArgument(0));

        ChallengeRequest req = new ChallengeRequest("Test", null, start, end, new BigDecimal("5"), null);
        ChallengeResponse resp = service.create(req);
        assertTrue(resp.active());
    }

    @Test
    @DisplayName("Create challenge with active=false saves as inactive")
    void createExplicitInactive() {
        when(repository.save(any(Challenge.class))).thenAnswer(inv -> inv.getArgument(0));

        ChallengeRequest req = new ChallengeRequest("Test", null, start, end, new BigDecimal("5"), false);
        ChallengeResponse resp = service.create(req);
        assertFalse(resp.active());
    }

    @Test
    @DisplayName("Update challenge with null active preserves existing state")
    void updatePreservesActiveWhenNull() {
        Challenge existing = new Challenge();
        existing.setTitle("Old");
        existing.setStartDate(start);
        existing.setEndDate(end);
        existing.setTargetReductionPercent(new BigDecimal("10"));
        existing.setActive(false);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Challenge.class))).thenAnswer(inv -> inv.getArgument(0));

        ChallengeRequest req = new ChallengeRequest("Updated", null, start, end, new BigDecimal("15"), null);
        ChallengeResponse resp = service.update(1L, req);

        assertEquals("Updated", resp.title());
        assertFalse(resp.active()); // preserved from existing, not defaulted to true
    }

    @Test
    @DisplayName("Update challenge with active=true changes state")
    void updateChangesActiveState() {
        Challenge existing = new Challenge();
        existing.setTitle("Old");
        existing.setStartDate(start);
        existing.setEndDate(end);
        existing.setTargetReductionPercent(new BigDecimal("10"));
        existing.setActive(false);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Challenge.class))).thenAnswer(inv -> inv.getArgument(0));

        ChallengeRequest req = new ChallengeRequest("Updated", null, start, end, new BigDecimal("15"), true);
        ChallengeResponse resp = service.update(1L, req);

        assertTrue(resp.active());
    }

    @Test
    @DisplayName("Get non-existent challenge throws ResourceNotFoundException")
    void getNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findById(999L));
    }

    @Test
    @DisplayName("Title is trimmed on create")
    void titleTrimmed() {
        when(repository.save(any(Challenge.class))).thenAnswer(inv -> inv.getArgument(0));

        ChallengeRequest req = new ChallengeRequest("  Trimmed Title  ", null, start, end, new BigDecimal("10"), true);
        ChallengeResponse resp = service.create(req);
        assertEquals("Trimmed Title", resp.title());
    }

    @Test
    @DisplayName("Same start and end date is valid")
    void sameDayChallenge() {
        when(repository.save(any(Challenge.class))).thenAnswer(inv -> inv.getArgument(0));

        ChallengeRequest req = new ChallengeRequest("One Day", null, start, start, new BigDecimal("5"), true);
        assertDoesNotThrow(() -> service.create(req));
    }
}

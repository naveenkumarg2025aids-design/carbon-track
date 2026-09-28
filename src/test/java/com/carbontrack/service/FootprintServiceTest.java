package com.carbontrack.service;

import com.carbontrack.dto.ActivityLogRequest;
import com.carbontrack.dto.ActivityLogResponse;
import com.carbontrack.entity.ActivityLog;
import com.carbontrack.entity.CommuteMode;
import com.carbontrack.entity.Household;
import com.carbontrack.exception.BadRequestException;
import com.carbontrack.repository.ActivityLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FootprintServiceTest {

    @Mock
    private ActivityLogRepository logs;

    @Mock
    private HouseholdService households;

    @InjectMocks
    private FootprintService service;

    private Household household;
    private final LocalDate today = LocalDate.now();

    @BeforeEach
    void setUp() {
        household = new Household("Test Home", "test@example.com", "Test City");
        // Simulate an id via reflection or mock
        when(households.getEntity(anyLong())).thenReturn(household);
        when(logs.save(any(ActivityLog.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // ──────────────────────────── Calculation tests ────────────────────────────

    @Test
    @DisplayName("T1: BUS 12km + 5.5kWh + 1.2kg waste = 5.6940 kg CO2e")
    void busCommuteWithElectricityAndWaste() {
        ActivityLogRequest req = new ActivityLogRequest(1L, today, CommuteMode.BUS,
                new BigDecimal("12.00"), new BigDecimal("5.50"), new BigDecimal("1.20"));
        ActivityLogResponse resp = service.create(req);
        // 0.089×12 + 0.716×5.5 + 0.570×1.2 = 1.068 + 3.938 + 0.684 = 5.6900
        assertEquals(new BigDecimal("5.6900"), resp.estimatedKgCo2e());
    }

    @Test
    @DisplayName("T2: CAR 25km + 0kWh + 0kg = 4.8000 kg CO2e")
    void carCommuteOnly() {
        ActivityLogRequest req = new ActivityLogRequest(1L, today, CommuteMode.CAR,
                new BigDecimal("25.00"), BigDecimal.ZERO, BigDecimal.ZERO);
        ActivityLogResponse resp = service.create(req);
        assertEquals(new BigDecimal("4.8000"), resp.estimatedKgCo2e());
    }

    @Test
    @DisplayName("T3: WALKING 3km + 0kWh + 0kg = 0.0000 kg CO2e")
    void walkingZeroEmissions() {
        ActivityLogRequest req = new ActivityLogRequest(1L, today, CommuteMode.WALKING,
                new BigDecimal("3.00"), BigDecimal.ZERO, BigDecimal.ZERO);
        ActivityLogResponse resp = service.create(req);
        assertEquals(new BigDecimal("0.0000"), resp.estimatedKgCo2e());
    }

    @Test
    @DisplayName("T4: NONE 0km + 10kWh + 0kg = 7.1600 kg CO2e")
    void electricityOnly() {
        ActivityLogRequest req = new ActivityLogRequest(1L, today, CommuteMode.NONE,
                BigDecimal.ZERO, new BigDecimal("10.00"), BigDecimal.ZERO);
        ActivityLogResponse resp = service.create(req);
        assertEquals(new BigDecimal("7.1600"), resp.estimatedKgCo2e());
    }

    @Test
    @DisplayName("T5: NONE 0km + 0kWh + 5kg = 2.8500 kg CO2e")
    void wasteOnly() {
        ActivityLogRequest req = new ActivityLogRequest(1L, today, CommuteMode.NONE,
                BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("5.00"));
        ActivityLogResponse resp = service.create(req);
        assertEquals(new BigDecimal("2.8500"), resp.estimatedKgCo2e());
    }

    @Test
    @DisplayName("T6: TRAIN 50km + 3kWh + 2kg = 5.3380 kg CO2e")
    void trainCommuteWithElectricityAndWaste() {
        ActivityLogRequest req = new ActivityLogRequest(1L, today, CommuteMode.TRAIN,
                new BigDecimal("50.00"), new BigDecimal("3.00"), new BigDecimal("2.00"));
        ActivityLogResponse resp = service.create(req);
        // 0.041×50 + 0.716×3 + 0.570×2 = 2.05 + 2.148 + 1.14 = 5.3380
        assertEquals(new BigDecimal("5.3380"), resp.estimatedKgCo2e());
    }

    @Test
    @DisplayName("T7: MOTORCYCLE 10km + 2kWh + 1kg = 3.0020 kg CO2e")
    void motorcycleCommuteWithElectricityAndWaste() {
        ActivityLogRequest req = new ActivityLogRequest(1L, today, CommuteMode.MOTORCYCLE,
                new BigDecimal("10.00"), new BigDecimal("2.00"), new BigDecimal("1.00"));
        ActivityLogResponse resp = service.create(req);
        // 0.103×10 + 0.716×2 + 0.570×1 = 1.03 + 1.432 + 0.57 = 3.0320
        assertEquals(new BigDecimal("3.0320"), resp.estimatedKgCo2e());
    }

    @Test
    @DisplayName("T8: CYCLING 10km + 0kWh + 0kg = 0.0000 kg CO2e")
    void cyclingZeroEmissions() {
        ActivityLogRequest req = new ActivityLogRequest(1L, today, CommuteMode.CYCLING,
                new BigDecimal("10.00"), BigDecimal.ZERO, BigDecimal.ZERO);
        ActivityLogResponse resp = service.create(req);
        assertEquals(new BigDecimal("0.0000"), resp.estimatedKgCo2e());
    }

    // ──────────────────────────── Validation tests ────────────────────────────

    @Test
    @DisplayName("T9: NONE with distance > 0 should be rejected")
    void noneWithDistanceRejected() {
        ActivityLogRequest req = new ActivityLogRequest(1L, today, CommuteMode.NONE,
                new BigDecimal("5.00"), BigDecimal.ZERO, BigDecimal.ZERO);
        assertThrows(BadRequestException.class, () -> service.create(req));
    }

    @Test
    @DisplayName("T10: All zeros should be rejected")
    void allZerosRejected() {
        ActivityLogRequest req = new ActivityLogRequest(1L, today, CommuteMode.NONE,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        assertThrows(BadRequestException.class, () -> service.create(req));
    }

    // ──────────────────────────── Scale & rounding tests ────────────────────────────

    @Test
    @DisplayName("Result should always have scale 4")
    void resultHasScale4() {
        ActivityLogRequest req = new ActivityLogRequest(1L, today, CommuteMode.CAR,
                new BigDecimal("1"), BigDecimal.ZERO, BigDecimal.ZERO);
        ActivityLogResponse resp = service.create(req);
        assertEquals(4, resp.estimatedKgCo2e().scale());
    }

    @Test
    @DisplayName("Saved entity should contain correct CO2e value")
    void entitySavedWithCorrectValue() {
        ActivityLogRequest req = new ActivityLogRequest(1L, today, CommuteMode.BUS,
                new BigDecimal("10"), new BigDecimal("1"), new BigDecimal("1"));
        service.create(req);

        ArgumentCaptor<ActivityLog> captor = ArgumentCaptor.forClass(ActivityLog.class);
        verify(logs).save(captor.capture());
        ActivityLog saved = captor.getValue();

        // 0.089×10 + 0.716×1 + 0.570×1 = 0.89 + 0.716 + 0.570 = 2.1760
        assertEquals(new BigDecimal("2.1760"), saved.getEstimatedKgCo2e());
        assertEquals(CommuteMode.BUS, saved.getCommuteMode());
    }
}

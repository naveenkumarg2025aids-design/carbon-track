package com.carbontrack.service;

import com.carbontrack.dto.HouseholdRequest;
import com.carbontrack.dto.HouseholdResponse;
import com.carbontrack.entity.Household;
import com.carbontrack.exception.BadRequestException;
import com.carbontrack.exception.ResourceNotFoundException;
import com.carbontrack.repository.HouseholdRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HouseholdServiceTest {

    @Mock private HouseholdRepository repository;
    @InjectMocks private HouseholdService service;

    @Test
    @DisplayName("Create household with unique email succeeds")
    void createSuccess() {
        when(repository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(repository.save(any(Household.class))).thenAnswer(inv -> inv.getArgument(0));

        HouseholdRequest req = new HouseholdRequest("Green Home", "green@test.com", "City");
        HouseholdResponse resp = service.create(req);

        assertNotNull(resp);
        assertEquals("Green Home", resp.name());
        verify(repository).save(any(Household.class));
    }

    @Test
    @DisplayName("Create household with duplicate email throws BadRequestException")
    void createDuplicateEmailRejected() {
        when(repository.existsByEmailIgnoreCase("dup@test.com")).thenReturn(true);

        HouseholdRequest req = new HouseholdRequest("Home 2", "dup@test.com", null);
        assertThrows(BadRequestException.class, () -> service.create(req));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Create household with blank email succeeds (email is optional)")
    void createWithBlankEmail() {
        when(repository.save(any(Household.class))).thenAnswer(inv -> inv.getArgument(0));

        HouseholdRequest req = new HouseholdRequest("No Email Home", "", null);
        HouseholdResponse resp = service.create(req);

        assertNotNull(resp);
        assertNull(resp.email());
    }

    @Test
    @DisplayName("Update household keeps own email without conflict")
    void updateKeepsOwnEmail() {
        Household existing = new Household("Old Name", "same@test.com", "City");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsByEmailIgnoreCase("same@test.com")).thenReturn(true);
        when(repository.save(any(Household.class))).thenAnswer(inv -> inv.getArgument(0));

        HouseholdRequest req = new HouseholdRequest("New Name", "same@test.com", "City");
        HouseholdResponse resp = service.update(1L, req);

        assertEquals("New Name", resp.name());
        assertEquals("same@test.com", resp.email());
    }

    @Test
    @DisplayName("Update household with another household's email throws BadRequestException")
    void updateDuplicateEmailRejected() {
        Household existing = new Household("Home", "mine@test.com", "City");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsByEmailIgnoreCase("taken@test.com")).thenReturn(true);

        HouseholdRequest req = new HouseholdRequest("Home", "taken@test.com", "City");
        assertThrows(BadRequestException.class, () -> service.update(1L, req));
    }

    @Test
    @DisplayName("Get non-existent household throws ResourceNotFoundException")
    void getNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findById(999L));
    }

    @Test
    @DisplayName("Delete household calls repository.delete")
    void deleteSuccess() {
        Household existing = new Household("Home", null, null);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        service.delete(1L);
        verify(repository).delete(existing);
    }

    @Test
    @DisplayName("Name is trimmed on create")
    void nameTrimmed() {
        when(repository.save(any(Household.class))).thenAnswer(inv -> inv.getArgument(0));

        HouseholdRequest req = new HouseholdRequest("  Spacey Name  ", null, null);
        HouseholdResponse resp = service.create(req);

        assertEquals("Spacey Name", resp.name());
    }
}

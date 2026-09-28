package com.carbontrack.service;

import com.carbontrack.dto.HouseholdRequest;
import com.carbontrack.dto.HouseholdResponse;
import com.carbontrack.entity.Household;
import com.carbontrack.exception.ResourceNotFoundException;
import com.carbontrack.repository.HouseholdRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class HouseholdService {
    private final HouseholdRepository repository;
    public HouseholdService(HouseholdRepository repository) { this.repository = repository; }

    public HouseholdResponse create(HouseholdRequest request) {
        String email = normalize(request.email());
        if (email != null && repository.existsByEmailIgnoreCase(email)) {
            throw new com.carbontrack.exception.BadRequestException("Email is already used by another household.");
        }
        Household h = new Household(request.name().trim(), email, clean(request.location()));
        return toResponse(repository.save(h));
    }

    @Transactional(readOnly = true)
    public List<HouseholdResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public HouseholdResponse findById(Long id) { return toResponse(getEntity(id)); }

    public HouseholdResponse update(Long id, HouseholdRequest request) {
        Household h = getEntity(id);
        String email = normalize(request.email());
        if (email != null && repository.existsByEmailIgnoreCase(email) && !email.equalsIgnoreCase(h.getEmail())) {
            throw new com.carbontrack.exception.BadRequestException("Email is already used by another household.");
        }
        h.setName(request.name().trim());
        h.setEmail(email);
        h.setLocation(clean(request.location()));
        return toResponse(repository.save(h));
    }

    public void delete(Long id) { repository.delete(getEntity(id)); }

    public Household getEntity(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Household " + id + " not found."));
    }

    private String normalize(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private String clean(String value) { return normalize(value); }
    private HouseholdResponse toResponse(Household h) {
        return new HouseholdResponse(h.getId(), h.getName(), h.getEmail(), h.getLocation(), h.getCreatedAt());
    }
}

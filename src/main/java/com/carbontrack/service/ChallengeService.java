package com.carbontrack.service;

import com.carbontrack.dto.ChallengeRequest;
import com.carbontrack.dto.ChallengeResponse;
import com.carbontrack.entity.Challenge;
import com.carbontrack.exception.BadRequestException;
import com.carbontrack.exception.ResourceNotFoundException;
import com.carbontrack.repository.ChallengeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class ChallengeService {
    private final ChallengeRepository repository;
    public ChallengeService(ChallengeRepository repository) { this.repository = repository; }

    public ChallengeResponse create(ChallengeRequest request) {
        validateDates(request);
        Challenge c = new Challenge();
        apply(c, request);
        c.setActive(request.active() == null || request.active());
        return toResponse(repository.save(c));
    }

    @Transactional(readOnly = true)
    public List<ChallengeResponse> findAll() { return repository.findAll().stream().map(this::toResponse).toList(); }

    @Transactional(readOnly = true)
    public ChallengeResponse findById(Long id) { return toResponse(getEntity(id)); }

    public ChallengeResponse update(Long id, ChallengeRequest request) {
        validateDates(request);
        Challenge c = getEntity(id);
        apply(c, request);
        if (request.active() != null) {
            c.setActive(request.active());
        }
        return toResponse(repository.save(c));
    }

    public void delete(Long id) { repository.delete(getEntity(id)); }

    public Challenge getEntity(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Challenge " + id + " not found."));
    }

    private void validateDates(ChallengeRequest r) {
        if (r.endDate().isBefore(r.startDate())) throw new BadRequestException("Challenge endDate must be on or after startDate.");
    }
    private void apply(Challenge c, ChallengeRequest r) {
        c.setTitle(r.title().trim());
        c.setDescription(r.description());
        c.setStartDate(r.startDate());
        c.setEndDate(r.endDate());
        c.setTargetReductionPercent(r.targetReductionPercent());
    }
    private ChallengeResponse toResponse(Challenge c) {
        return new ChallengeResponse(c.getId(), c.getTitle(), c.getDescription(), c.getStartDate(),
                c.getEndDate(), c.getTargetReductionPercent(), c.isActive());
    }
}

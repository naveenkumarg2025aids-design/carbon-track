package com.carbontrack.controller;

import com.carbontrack.dto.*;
import com.carbontrack.service.ChallengeService;
import com.carbontrack.service.ParticipationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ChallengeController {
    private final ChallengeService challenges;
    private final ParticipationService participations;

    public ChallengeController(ChallengeService challenges, ParticipationService participations) {
        this.challenges = challenges;
        this.participations = participations;
    }

    @PostMapping("/challenges")
    @ResponseStatus(HttpStatus.CREATED)
    public ChallengeResponse create(@Valid @RequestBody ChallengeRequest request) { return challenges.create(request); }

    @GetMapping("/challenges")
    public List<ChallengeResponse> findAll() { return challenges.findAll(); }

    @GetMapping("/challenges/{id}")
    public ChallengeResponse findById(@PathVariable Long id) { return challenges.findById(id); }

    @PutMapping("/challenges/{id}")
    public ChallengeResponse update(@PathVariable Long id, @Valid @RequestBody ChallengeRequest request) {
        return challenges.update(id, request);
    }

    @DeleteMapping("/challenges/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { challenges.delete(id); }

    @PostMapping("/challenges/{challengeId}/participations")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationResponse join(@PathVariable Long challengeId, @RequestParam Long householdId) {
        return participations.join(challengeId, householdId);
    }

    @GetMapping("/challenges/{challengeId}/participations")
    public List<ParticipationResponse> participants(@PathVariable Long challengeId) {
        return participations.list(challengeId);
    }

    @GetMapping("/challenges/{challengeId}/progress")
    public ChallengeProgressResponse progress(@PathVariable Long challengeId, @RequestParam Long householdId) {
        return participations.progress(challengeId, householdId);
    }

    @GetMapping("/leaderboard")
    public List<LeaderboardEntryResponse> leaderboard(@RequestParam int year, @RequestParam int month) {
        return participations.leaderboard(year, month);
    }
}

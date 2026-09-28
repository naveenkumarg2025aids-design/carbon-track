package com.carbontrack.controller;

import com.carbontrack.dto.ActivityLogRequest;
import com.carbontrack.dto.ActivityLogResponse;
import com.carbontrack.dto.MonthlyFootprintResponse;
import com.carbontrack.service.FootprintService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ActivityController {
    private final FootprintService service;
    public ActivityController(FootprintService service) { this.service = service; }

    @PostMapping("/activities")
    @ResponseStatus(HttpStatus.CREATED)
    public ActivityLogResponse create(@Valid @RequestBody ActivityLogRequest request) { return service.create(request); }

    @GetMapping("/households/{householdId}/activities")
    public List<ActivityLogResponse> list(@PathVariable Long householdId) { return service.list(householdId); }

    @DeleteMapping("/activities/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }

    @GetMapping("/footprints/monthly")
    public MonthlyFootprintResponse monthly(@RequestParam Long householdId,
            @RequestParam int year, @RequestParam int month) {
        return service.monthly(householdId, year, month);
    }
}

package com.carbontrack.repository;

import com.carbontrack.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findByHouseholdIdOrderByActivityDateDesc(Long householdId);
    List<ActivityLog> findByHouseholdIdAndActivityDateBetween(Long householdId, LocalDate start, LocalDate end);
    List<ActivityLog> findByActivityDateBetween(LocalDate start, LocalDate end);
}

package com.carbontrack.repository;

import com.carbontrack.entity.Household;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HouseholdRepository extends JpaRepository<Household, Long> {
    boolean existsByEmailIgnoreCase(String email);
}

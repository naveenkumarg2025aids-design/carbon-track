package com.carbontrack.repository;

import com.carbontrack.entity.Participation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    boolean existsByHouseholdIdAndChallengeId(Long householdId, Long challengeId);
    List<Participation> findByChallengeIdOrderByJoinedAtAsc(Long challengeId);
}

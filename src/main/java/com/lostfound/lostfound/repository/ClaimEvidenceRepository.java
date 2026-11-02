package com.lostfound.lostfound.repository;

import com.lostfound.lostfound.model.ClaimEvidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimEvidenceRepository extends JpaRepository<ClaimEvidence, Long> {
    List<ClaimEvidence> findByClaimId(Long claimId);
}

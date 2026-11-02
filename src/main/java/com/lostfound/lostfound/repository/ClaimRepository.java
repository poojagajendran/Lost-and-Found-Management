package com.lostfound.lostfound.repository;

import com.lostfound.lostfound.model.Claim;
import com.lostfound.lostfound.model.ClaimStatus;
import com.lostfound.lostfound.model.Item;
import com.lostfound.lostfound.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {

    List<Claim> findByClaimantOrderByCreatedAtDesc(User claimant);

    List<Claim> findByItemOrderByCreatedAtDesc(Item item);

    List<Claim> findByStatusOrderByCreatedAtDesc(ClaimStatus status);

    List<Claim> findAllByOrderByCreatedAtDesc();

    boolean existsByItemIdAndClaimantIdAndStatus(Long itemId, Long claimantId, ClaimStatus status);

    long countByStatus(ClaimStatus status);
}

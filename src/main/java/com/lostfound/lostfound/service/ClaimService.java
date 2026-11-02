package com.lostfound.lostfound.service;

import com.lostfound.lostfound.dto.ClaimSubmissionDto;
import com.lostfound.lostfound.model.*;
import com.lostfound.lostfound.repository.ClaimEvidenceRepository;
import com.lostfound.lostfound.repository.ClaimRepository;
import com.lostfound.lostfound.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final ClaimEvidenceRepository claimEvidenceRepository;
    private final ItemRepository itemRepository;

    public ClaimService(ClaimRepository claimRepository,
                        ClaimEvidenceRepository claimEvidenceRepository,
                        ItemRepository itemRepository) {
        this.claimRepository = claimRepository;
        this.claimEvidenceRepository = claimEvidenceRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional
    public Claim submitClaim(Long itemId, ClaimSubmissionDto dto, User claimant) {
        if (claimant == null) {
            throw new IllegalArgumentException("Claimant must be authenticated.");
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item with ID " + itemId + " not found."));

        if (item.getType() != ItemType.FOUND) {
            throw new IllegalArgumentException("Ownership claims can only be submitted for FOUND items.");
        }

        if (item.getStatus() == ItemStatus.RETURNED) {
            throw new IllegalArgumentException("This item has already been marked as returned to its owner.");
        }

        // Prevent users from claiming an item they reported finding
        if (item.getReporter() != null && item.getReporter().getId().equals(claimant.getId())) {
            throw new IllegalArgumentException("You cannot submit an ownership claim for an item you reported finding.");
        }

        // Prevent duplicate active claims by the same user for the same item
        boolean hasPending = claimRepository.existsByItemIdAndClaimantIdAndStatus(itemId, claimant.getId(), ClaimStatus.PENDING);
        boolean hasApproved = claimRepository.existsByItemIdAndClaimantIdAndStatus(itemId, claimant.getId(), ClaimStatus.APPROVED);
        if (hasPending || hasApproved) {
            throw new IllegalArgumentException("You have already submitted an active ownership claim for this item.");
        }

        Claim claim = new Claim();
        claim.setItem(item);
        claim.setClaimant(claimant);
        claim.setExplanation(dto.getExplanation().trim());
        claim.setStatus(ClaimStatus.PENDING);

        Claim savedClaim = claimRepository.save(claim);

        // Add supporting evidence provided by claimant
        if (dto.getEvidenceDetails() != null && !dto.getEvidenceDetails().trim().isEmpty()) {
            ClaimEvidence evidence = new ClaimEvidence();
            evidence.setClaim(savedClaim);
            evidence.setEvidenceType(dto.getEvidenceType() != null ? dto.getEvidenceType().trim() : "IDENTIFYING_MARKS");
            evidence.setEvidenceDetails(dto.getEvidenceDetails().trim());
            claimEvidenceRepository.save(evidence);
            savedClaim.addEvidence(evidence);
        }

        // Mark item status as CLAIM_PENDING
        if (item.getStatus() == ItemStatus.OPEN) {
            item.setStatus(ItemStatus.CLAIM_PENDING);
            itemRepository.save(item);
        }

        return savedClaim;
    }

    @Transactional(readOnly = true)
    public List<Claim> getClaimsByClaimant(User claimant) {
        return claimRepository.findByClaimantOrderByCreatedAtDesc(claimant);
    }

    @Transactional(readOnly = true)
    public List<Claim> getClaimsByStatus(ClaimStatus status) {
        return claimRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    @Transactional(readOnly = true)
    public List<Claim> getAllClaims() {
        return claimRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public Optional<Claim> getClaimById(Long id) {
        return claimRepository.findById(id);
    }

    @Transactional
    public Claim approveClaim(Long claimId, User reviewer, String decisionReason) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim with ID " + claimId + " not found."));

        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new IllegalStateException("Only pending claims can be approved.");
        }

        claim.setStatus(ClaimStatus.APPROVED);
        claim.setReviewedBy(reviewer);
        claim.setReviewedAt(LocalDateTime.now());
        claim.setDecisionReason(decisionReason != null ? decisionReason.trim() : "Ownership verified and approved by administrator.");

        Claim approvedClaim = claimRepository.save(claim);

        // Mark the associated item as RETURNED
        Item item = claim.getItem();
        if (item != null) {
            item.setStatus(ItemStatus.RETURNED);
            itemRepository.save(item);

            // Automatically reject any other pending claims for this item
            List<Claim> itemClaims = claimRepository.findByItemOrderByCreatedAtDesc(item);
            for (Claim otherClaim : itemClaims) {
                if (!otherClaim.getId().equals(claimId) && otherClaim.getStatus() == ClaimStatus.PENDING) {
                    otherClaim.setStatus(ClaimStatus.REJECTED);
                    otherClaim.setReviewedBy(reviewer);
                    otherClaim.setReviewedAt(LocalDateTime.now());
                    otherClaim.setDecisionReason("Another claim for this item was successfully verified and approved.");
                    claimRepository.save(otherClaim);
                }
            }
        }

        return approvedClaim;
    }

    @Transactional
    public Claim rejectClaim(Long claimId, User reviewer, String decisionReason) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim with ID " + claimId + " not found."));

        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new IllegalStateException("Only pending claims can be rejected.");
        }

        claim.setStatus(ClaimStatus.REJECTED);
        claim.setReviewedBy(reviewer);
        claim.setReviewedAt(LocalDateTime.now());
        claim.setDecisionReason(decisionReason != null ? decisionReason.trim() : "Ownership evidence could not be verified.");

        Claim rejectedClaim = claimRepository.save(claim);

        // If there are no other pending claims, reset item status to OPEN
        Item item = claim.getItem();
        if (item != null && item.getStatus() == ItemStatus.CLAIM_PENDING) {
            List<Claim> otherClaims = claimRepository.findByItemOrderByCreatedAtDesc(item);
            boolean hasOtherPending = otherClaims.stream()
                    .anyMatch(c -> !c.getId().equals(claimId) && c.getStatus() == ClaimStatus.PENDING);
            if (!hasOtherPending) {
                item.setStatus(ItemStatus.OPEN);
                itemRepository.save(item);
            }
        }

        return rejectedClaim;
    }

    @Transactional(readOnly = true)
    public long countByStatus(ClaimStatus status) {
        return claimRepository.countByStatus(status);
    }
}

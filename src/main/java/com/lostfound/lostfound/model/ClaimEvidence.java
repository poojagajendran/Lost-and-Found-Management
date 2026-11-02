package com.lostfound.lostfound.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "claim_evidences")
public class ClaimEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;

    @Column(nullable = false)
    private String evidenceType; // e.g., "IDENTIFYING_MARKS", "SERIAL_NUMBER", "PURCHASE_RECEIPT", "DETAILED_DESCRIPTION"

    @Column(length = 3000, nullable = false)
    private String evidenceDetails;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ClaimEvidence() {
    }

    public ClaimEvidence(Claim claim, String evidenceType, String evidenceDetails) {
        this.claim = claim;
        this.evidenceType = evidenceType;
        this.evidenceDetails = evidenceDetails;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Claim getClaim() {
        return claim;
    }

    public void setClaim(Claim claim) {
        this.claim = claim;
    }

    public String getEvidenceType() {
        return evidenceType;
    }

    public void setEvidenceType(String evidenceType) {
        this.evidenceType = evidenceType;
    }

    public String getEvidenceDetails() {
        return evidenceDetails;
    }

    public void setEvidenceDetails(String evidenceDetails) {
        this.evidenceDetails = evidenceDetails;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
